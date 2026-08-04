# Tasks: Operator Notes Issue Check

**Input**: Design documents from `/specs/002-operator-notes-check/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Not requested in the feature specification — no TDD/test tasks included. Add via a follow-up if desired.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Single Maven module at repository root: `src/main/java/pl/tomaszko/s03e01/`, `src/main/resources/`, `pom.xml`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Dependencies and package layout for Spring AI OpenRouter + hub HTTP on top of the existing console app

- [ ] T001 Update `pom.xml` to add `spring-ai-starter-model-openai` (Spring AI BOM already present) and a Spring HTTP client dependency suitable for `RestClient` (e.g. `spring-boot-starter-web` kept non-web via `spring.main.web-application-type=none`, or equivalent Boot 4 RestClient starter)
- [ ] T002 [P] Create package directories `src/main/java/pl/tomaszko/s03e01/notes/` and `src/main/java/pl/tomaszko/s03e01/hub/` per plan.md
- [ ] T003 [P] Create `src/main/resources/prompts/` directory for the operator-notes system prompt template
- [ ] T004 [P] Extend `src/main/resources/application.properties` with placeholders for OpenRouter base URL (`https://openrouter.ai/api/v1`), default model `nvidia/nemotron-3-ultra-550b-a55b:free`, hub verify URL `https://hub.ag3nts.org/verify`, and prompt-template location/property keys (values filled in foundational/story tasks)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared scan collections, reporting primitives, secrets/config beans, and removal of local-only flag capture — required before any user story

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 [P] Implement `ClassifiedFile` in `src/main/java/pl/tomaszko/s03e01/scan/ClassifiedFile.java` with `basename`, `stem`, `category` (`VALID`/`PARSE`/`SCOPE`/`OPERATOR`), and optional `SensorReading` per data-model.md
- [ ] T006 Extend `ScanSummary` in `src/main/java/pl/tomaszko/s03e01/scan/ScanSummary.java` to retain lists of classified files (at least VALID and invalid-by-category) and an `operatorInvalid` counter, not only aggregate counts
- [ ] T007 Update `JsonFileScanner` in `src/main/java/pl/tomaszko/s03e01/scan/JsonFileScanner.java` to populate `ClassifiedFile` records into `ScanSummary` (VALID with reading; PARSE/SCOPE without re-scan) while keeping existing PARSE/SCOPE reporting behavior
- [ ] T008 [P] Extend `InvalidFileReporter` in `src/main/java/pl/tomaszko/s03e01/report/InvalidFileReporter.java` with `OPERATOR: <basename>`, `FLAG: captured`, `FLAG: {FLG:...}`, and `ERROR: <detail>` methods (remove or stop using local-only flag capture as success)
- [ ] T009 [P] Implement `HubProperties` in `src/main/java/pl/tomaszko/s03e01/config/HubProperties.java` reading `HUB_API_KEY` from the environment and configurable verify URL (default `https://hub.ag3nts.org/verify`)
- [ ] T010 [P] Implement OpenRouter/LLM config bindings in `src/main/java/pl/tomaszko/s03e01/config/OpenRouterProperties.java` (and/or `application.properties`) for `OPENROUTER_API_KEY` via `spring.ai.openai.api-key`, base-url `/v1`, and configurable `app.llm.model`
- [ ] T011 [P] Implement `OperatorNotesPromptProperties` in `src/main/java/pl/tomaszko/s03e01/config/OperatorNotesPromptProperties.java` for the configurable system-prompt template (classpath resource and/or property)
- [ ] T012 Implement startup hard-fail for missing/blank `OPENROUTER_API_KEY` and `HUB_API_KEY` (and keep existing `JSON_DIR`/argv checks) in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` and/or a dedicated `@Component` validator, logging `ERROR: ` and non-zero exit
- [ ] T013 Remove immediate post-scan `FLAG: captured` success path from `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` so local categorization alone never exits 0 as flag success (FR-012; constitution v3.0.0)

**Checkpoint**: Foundation ready — scan retains collections; reporter supports OPERATOR/flag/ERROR; secrets required at startup; local flag disabled

---

## Phase 3: User Story 1 - Reclassify structurally valid files whose notes report issues (Priority: P1) 🎯 MVP

**Goal**: After PARSE/SCOPE, classify VALID files’ `operator_notes` via Spring AI and move issue-bearing groups to INVALID with `OPERATOR: <basename>`

**Independent Test**: Structurally valid fixtures sharing an issue-bearing notes string become `OPERATOR:`; clean-notes fixtures stay silent; PARSE/SCOPE files keep original tags and are not re-tagged OPERATOR

### Implementation for User Story 1

- [ ] T014 [P] [US1] Implement `OperatorNotesIndexer` in `src/main/java/pl/tomaszko/s03e01/notes/OperatorNotesIndexer.java` to build the notes map from VALID `ClassifiedFile`s only (exact string keys) and assign distinct `nr` starting at 1 in first-seen order
- [ ] T015 [P] [US1] Create default system prompt template at `src/main/resources/prompts/operator-notes-system.st` (or equivalent) instructing issue detection and the `{ "notes": [ { "nr", "operator_notes" } ] }` / return-`nr`s-only contract
- [ ] T016 [US1] Implement `NotesPromptFactory` in `src/main/java/pl/tomaszko/s03e01/notes/NotesPromptFactory.java` using Spring AI `PromptTemplate` to render the configurable system prompt and build the user JSON body `{ "notes": [ ... ] }`
- [ ] T017 [US1] Implement `AiClientConfig` in `src/main/java/pl/tomaszko/s03e01/config/AiClientConfig.java` exposing a `ChatClient` `@Bean` from `ChatClient.Builder`
- [ ] T018 [US1] Implement `OperatorNotesClassifier` in `src/main/java/pl/tomaszko/s03e01/notes/OperatorNotesClassifier.java` to call `ChatClient` once, parse a JSON array of `nr` integers, ignore unknown `nr`s, and on API/parse failure log `ERROR: ` and signal hard failure (no retry)
- [ ] T019 [US1] In `OperatorNotesClassifier` / `NotesPromptFactory`, log to console and file: rendered system prompt, `tools: none` (or tool definitions), user notes JSON, and raw model response (FR-014)
- [ ] T020 [US1] Apply classification results: for each returned known `nr`, move all mapped VALID files to OPERATOR, call `InvalidFileReporter.operator`, update `ScanSummary` lists/counters in a service method used by the runner (e.g. extend classifier or add `src/main/java/pl/tomaszko/s03e01/notes/OperatorNotesReclassifier.java`)
- [ ] T021 [US1] Wire notes pass into `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` after structural scan: skip LLM when VALID empty; otherwise index→classify→reclassify; abort with non-zero on classifier failure before hub
- [ ] T022 [US1] Ensure PARSE/SCOPE files are never re-tagged OPERATOR in `src/main/java/pl/tomaszko/s03e01/notes/` + runner path (FR-007)
- [ ] T022b [US1] Assert clean-notes VALID files (notes whose `nr` was not returned) produce no `OPERATOR:` lines and remain in the VALID collection in `src/main/java/pl/tomaszko/s03e01/notes/` + `ScanSummary` / runner path (FR-006, SC-001 clean path)

**Checkpoint**: MVP — OPERATOR reclassification works end-to-end after scan (hub may still be stubbed/absent)

---

## Phase 4: User Story 2 - Deduplicate notes before asking the model (Priority: P2)

**Goal**: Prove unique-notes numbering and a single request with wrapped `notes` array; shared notes share one `nr` and move together

**Independent Test**: Multiple VALID files share one issue notes string and others share a clean string; logs show one LLM request with M unique `notes` items; all files under flagged `nr`s become OPERATOR together

### Implementation for User Story 2

- [ ] T023 [US2] Verify/harden `OperatorNotesIndexer` in `src/main/java/pl/tomaszko/s03e01/notes/OperatorNotesIndexer.java` so shared exact `operator_notes` yield one map entry / one `nr` (not one item per file)
- [ ] T024 [US2] Verify/harden request body builder in `src/main/java/pl/tomaszko/s03e01/notes/NotesPromptFactory.java` to emit exactly one ChatClient call payload `{ "notes": [ ... K items ... ] }` with no chunking
- [ ] T025 [US2] Ensure model output handling in `src/main/java/pl/tomaszko/s03e01/notes/OperatorNotesClassifier.java` keys only on returned `nr`s (never note-text equality) and treats duplicate returned `nr`s as a single move set
- [ ] T026 [US2] Confirm finish progress in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` includes operator-invalid totals from `ScanSummary` after reclassification

**Checkpoint**: User Stories 1 and 2 — deduped single-request notes classification with bulk OPERATOR moves

---

## Phase 5: User Story 3 - Submit results to hub and capture flag only from hub response (Priority: P1)

**Note**: Scheduled after US2 for dependency (finalized OPERATOR stems), not because US3 priority is lower than US2—both US1 and US3 are P1.

**Goal**: POST invalid stems to hub `/verify`; success only on `{FLG:...}` with `FLAG: captured` + `FLAG: {FLG:...}`; otherwise `ERROR:` and non-zero exit

**Independent Test**: After classification, hub body with `{FLG:...}` → exit 0 and both flag lines; body without token or transport failure → `ERROR:` + non-zero, no flag lines; empty invalid list still calls hub with `recheck: []`

### Implementation for User Story 3

- [ ] T027 [P] [US3] Implement `HubVerificationService` in `src/main/java/pl/tomaszko/s03e01/hub/HubVerificationService.java` using Spring `RestClient` to POST `{ apikey, task: "evaluation", answer: { recheck: [...] } }` to the configured verify URL
- [ ] T028 [US3] Build `recheck` from finalized invalid `ClassifiedFile` stems only (PARSE+SCOPE+OPERATOR; no VALID; strip `.json`; order don’t-care; allow `[]`) inside `HubVerificationService` or a small helper in `src/main/java/pl/tomaszko/s03e01/hub/`
- [ ] T029 [US3] Parse hub response body for first `{FLG:...}` substring in `src/main/java/pl/tomaszko/s03e01/hub/HubVerificationService.java`; on match return success + token; on miss or transport error return failure with raw body/detail (no retry)
- [ ] T030 [US3] On hub success, log `FLAG: captured` then `FLAG: {FLG:...}` via `InvalidFileReporter` in `src/main/java/pl/tomaszko/s03e01/report/InvalidFileReporter.java` / runner; set exit code 0
- [ ] T031 [US3] On hub failure, log `ERROR: ` + body/detail via reporter/logger and set non-zero exit in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java`
- [ ] T032 [US3] Wire hub call after notes pass (always, including empty invalid set) in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java`; never claim flag success from local categorization alone

**Checkpoint**: Full pipeline — scan → OPERATOR notes → hub verify → hub-token flag

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Config polish, docs alignment, and quickstart validation

- [ ] T033 [P] Finalize `src/main/resources/application.properties` bindings for model name, prompt template, OpenRouter base-url, and hub URL so FR-017 parametrization works without code changes
- [ ] T034 [P] Confirm `src/main/resources/logback-spring.xml` still appends all pipeline/LLM/hub logs to console and `logs/s03e01.log`
- [ ] T035 Align finish/start messaging in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` with contracts/console-run.md (counts include operatorInvalid; no per-file spam)
- [ ] T036 [P] Add or update fixture notes under `src/test/resources/fixtures/sensors/` (or README) documenting VALID-with-issue-notes vs clean notes for manual quickstart runs
- [ ] T037 Run validation scenarios from `specs/002-operator-notes-check/quickstart.md` (build, env vars, Scenario A–D expectations) and fix any gaps found in runner/config

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Setup — **BLOCKS** all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational — MVP notes → OPERATOR
- **User Story 2 (Phase 4)**: Depends on US1 classifier/indexer existing — hardens dedupe/single-request contract
- **User Story 3 (Phase 5)**: Depends on Foundational; practically after US1 so hub sees OPERATOR stems — can stub empty OPERATOR for hub-only dry runs
- **Polish (Phase 6)**: After desired stories complete

### User Story Dependencies

- **US1 (P1)**: After Foundational — no dependency on US2/US3
- **US2 (P2)**: After US1 implementation artifacts (indexer/classifier/prompt factory)
- **US3 (P1)**: After Foundational; integrate after US1 for full answer set (PARSE+SCOPE+OPERATOR)

### Parallel Opportunities

- T002–T004 setup in parallel
- T005, T008–T011 foundational in parallel after T001
- T014–T015 (US1) in parallel before prompt factory/classifier
- T027 (US3) can start in parallel with late US1 once `HubProperties` exists

---

## Parallel Example: User Story 1

```text
Task: "Implement OperatorNotesIndexer in src/main/java/pl/tomaszko/s03e01/notes/OperatorNotesIndexer.java"
Task: "Create default system prompt template at src/main/resources/prompts/operator-notes-system.st"
```

## Parallel Example: User Story 3

```text
Task: "Implement HubVerificationService in src/main/java/pl/tomaszko/s03e01/hub/HubVerificationService.java"
# (after HubProperties from Phase 2)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL)
3. Complete Phase 3: User Story 1 (OPERATOR reclassification)
4. **STOP and VALIDATE**: VALID issue-notes → `OPERATOR:`; clean notes silent; LLM failure → `ERROR:` + non-zero
5. Continue to US2 hardening, then US3 hub flag

### Incremental Delivery

1. Setup + Foundational → collections, secrets, no local flag
2. US1 → OPERATOR MVP
3. US2 → dedupe / single-request guarantees
4. US3 → hub verify + `{FLG:...}` success
5. Polish → quickstart.md scenarios

### Parallel Team Strategy

1. Team completes Setup + Foundational together
2. Dev A: US1 notes/LLM path
3. Dev B: US3 hub client (against stem lists) once T009 done
4. Merge US2 hardening on indexer/classifier after US1 lands

---

## Notes

- [P] = different files, no incomplete-task dependencies
- [USn] maps to spec user stories
- No automated test tasks (not requested); validate via quickstart.md
- Commit after each task or logical group
- Stop at checkpoints to validate independently
