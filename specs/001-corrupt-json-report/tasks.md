# Tasks: Corrupt JSON File Reporter

**Input**: Design documents from `/specs/001-corrupt-json-report/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/console-scan.md, quickstart.md

**Tests**: Not requested in the feature specification â€” no TDD/test tasks included. Add via a follow-up if desired.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- Single Maven module at repository root: `src/main/java/pl/tomaszko/s03e01/`, `src/main/resources/`, `pom.xml`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Maven/Spring Boot project skeleton and logging baseline

- [x] T001 Create Maven project files `pom.xml` with groupId `pl.tomaszko`, artifactId `s03e01`, Java 23, Spring Boot 4.1.0 parent, Spring AI 2.0.0 BOM import, `spring-boot-starter`, a minimal Spring AI dependency (no LLM usage), and `spring-boot-starter-test` (test scope only)
- [x] T002 [P] Create package directories under `src/main/java/pl/tomaszko/s03e01/` for `config/`, `runner/`, `scan/`, `model/`, `validation/`, and `report/` per plan.md
- [x] T003 [P] Create `src/main/java/pl/tomaszko/s03e01/S03e01Application.java` with `@SpringBootApplication` and a `main` that runs `SpringApplication` and exits after runners complete
- [x] T004 [P] Create `src/main/resources/application.properties` with non-web app settings (`spring.main.web-application-type=none`) and logging level defaults
- [x] T005 [P] Create `src/main/resources/logback-spring.xml` (or equivalent logging config in `application.properties`) so logs go to **console and a log file**

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared domain model, validation rules, and reporting primitives required by all stories

**âš ï¸ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 [P] Implement closed-schema DTO `SensorReading` in `src/main/java/pl/tomaszko/s03e01/model/SensorReading.java` with exact properties from data-model.md and Jackson fail-on-unknown / required binding behavior
- [x] T007 [P] Implement `SensorType` token enum/map in `src/main/java/pl/tomaszko/s03e01/validation/SensorType.java` for `humidity`, `temperature`, `water`, `pressure`, `voltage` â†’ reading fields
- [x] T008 [P] Implement `ReadingField` and inclusive ranges in `src/main/java/pl/tomaszko/s03e01/validation/ReadingField.java` and `src/main/java/pl/tomaszko/s03e01/validation/ReadingRanges.java` per data-model.md
- [x] T009 Implement `SensorReadingValidator` in `src/main/java/pl/tomaszko/s03e01/validation/SensorReadingValidator.java` applying sensor_type split/trim/duplicates/unknown rules, non-empty `operator_notes`, inactive-zero / active non-zero+range rules (FR-020/021/024)
- [x] T010 Implement `InvalidFileReporter` in `src/main/java/pl/tomaszko/s03e01/report/InvalidFileReporter.java` emitting exact prefixes `PARSE: ` and `SCOPE: ` plus **basename only**, and `FLAG: captured`, via SLF4J
- [x] T011 [P] Implement `ScanSummary` in `src/main/java/pl/tomaszko/s03e01/scan/ScanSummary.java` with scanned / parseInvalid / scopeInvalid / valid counters

**Checkpoint**: Foundation ready â€” user story implementation can now begin

---

## Phase 3: User Story 1 - Report files with out-of-scope values (Priority: P1) ðŸŽ¯ MVP

**Goal**: Scan a directory of JSON sensor files and report each scope/schema-invalid file once with `SCOPE: <basename>`

**Independent Test**: Place mix of valid and SCOPE-invalid fixtures in a directory; run the scanner path (or full app once runner exists) and confirm only invalid basenames appear with `SCOPE:` and valid files are silent

### Implementation for User Story 1

- [x] T012 [US1] Implement `JsonFileScanner` in `src/main/java/pl/tomaszko/s03e01/scan/JsonFileScanner.java` to list top-level `*.json` only (non-recursive), ignore non-`.json` files
- [x] T013 [US1] Extend `JsonFileScanner` (or a collaborator in the same package) to deserialize each file to `SensorReading`, run `SensorReadingValidator`, and on post-bind validation failure call `InvalidFileReporter` with `SCOPE:` exactly once per file
- [x] T014 [US1] Ensure multi-rule failures in one file still produce a single `SCOPE:` line and update `ScanSummary` scopeInvalid/valid counters in `src/main/java/pl/tomaszko/s03e01/scan/JsonFileScanner.java`

**Checkpoint**: User Story 1 scanner delivers SCOPE reporting (runner/`JSON_DIR` wiring in US2)

---

## Phase 4: User Story 2 - Complete a full directory scan and capture the flag (Priority: P2)

**Goal**: Zero CLI args; `JSON_DIR` env; brief start/finish; categorize all `.json` files; on â‰¥1 file fully categorized log `FLAG: captured` and exit 0; non-zero on bad setup or zero `.json` files

**Independent Test**: Set `JSON_DIR` with â‰¥1 fixtures; run jar with no args â†’ progress, optional PARSE/SCOPE, `FLAG: captured`, exit 0. Unset `JSON_DIR`, empty of json, or pass argv â†’ non-zero, no flag

### Implementation for User Story 2

- [x] T015 [US2] Implement `JsonDirProperties` (or equivalent) in `src/main/java/pl/tomaszko/s03e01/config/JsonDirProperties.java` reading `JSON_DIR` from the environment
- [x] T016 [US2] Harden `SensorScanRunner` in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` to require **zero** CLI args and a usable `JSON_DIR`; any argv or missing/unusable `JSON_DIR` â†’ clear error, non-zero exit, no flag (FR-001/002/017)
- [x] T017 [US2] If `JSON_DIR` is readable but contains **zero** `.json` files, exit non-zero without `FLAG: captured` in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java`
- [x] T018 [US2] Add brief start progress (include `JSON_DIR`) and finish progress (optional totals from `ScanSummary`) in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` without per-file progress (FR-018)
- [x] T019 [US2] After all discovered `.json` files (â‰¥1) are categorized, log exact line `FLAG: captured` via `InvalidFileReporter` or logger in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` / `src/main/java/pl/tomaszko/s03e01/report/InvalidFileReporter.java` (FR-025)
- [x] T020 [US2] Ensure successful flag-capture runs exit with status 0 even when PARSE/SCOPE lines were emitted, via `SpringApplication.exit` / `ExitCodeGenerator` in `src/main/java/pl/tomaszko/s03e01/runner/SensorScanRunner.java` and/or `S03e01Application.java`

**Checkpoint**: User Stories 1 and 2: env-driven scan, flag capture, correct exit codes

---

## Phase 5: User Story 3 - Survive individual bad files during a large scan (Priority: P3)

**Goal**: Unreadable / wrong-type / non-object root JSON yields `PARSE:` and the scan continues so all files are categorized and the flag can be captured

**Independent Test**: Mix valid, SCOPE-invalid, and PARSE-invalid `.json` files; run once; both tags; `FLAG: captured`; exit 0

### Implementation for User Story 3

- [x] T021 [US3] In `src/main/java/pl/tomaszko/s03e01/scan/JsonFileScanner.java`, catch read/parse/bind failures (malformed JSON, wrong field JSON types, **non-object root**) and report `PARSE: <basename>` via `InvalidFileReporter` without aborting the scan (FR-014/022)
- [x] T022 [US3] Keep post-bind failures (missing/extra properties, empty `operator_notes`, sensor rules) as `SCOPE:` in `src/main/java/pl/tomaszko/s03e01/scan/JsonFileScanner.java`
- [x] T023 [US3] Increment `parseInvalid` vs `scopeInvalid` correctly in `ScanSummary` from `src/main/java/pl/tomaszko/s03e01/scan/JsonFileScanner.java` and surface totals on finish in `SensorScanRunner`
- [x] T024 [US3] Verify continue-to-next-file with no retry semantics remains the only recovery path in `src/main/java/pl/tomaszko/s03e01/scan/JsonFileScanner.java` (FR-008)

**Checkpoint**: All user stories independently functional under `JSON_DIR` + flag capture

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Repo hygiene and quickstart validation

- [x] T025 [P] Add `.gitignore` entries for Maven `target/`, IDE files, and log output files at repository root
- [x] T026 [P] Add sample fixture directory notes aligned with `specs/001-corrupt-json-report/quickstart.md` (no secrets)
- [x] T027 Run end-to-end validation against `specs/001-corrupt-json-report/quickstart.md` using JDK `C:\tools\jdk-23.0.2` (`JSON_DIR` set; jar with **no** args; confirm PARSE/SCOPE, `FLAG: captured`, exit codes, console + file logs)
- [x] T028 Confirm Spring AI is on the classpath but unused (no ChatClient/OpenRouter calls) across `pom.xml` and `src/main/java/pl/tomaszko/s03e01/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies â€” start immediately
- **Foundational (Phase 2)**: Depends on Setup â€” **BLOCKS** all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational â€” MVP
- **User Story 2 (Phase 4)**: Depends on Foundational; builds on US1 runner/scanner
- **User Story 3 (Phase 5)**: Depends on Foundational; extends US1 scanner error paths; benefits from US2 runner
- **Polish (Phase 6)**: Depends on desired stories being complete (ideally US1â€“US3)

### User Story Dependencies

- **User Story 1 (P1)**: After Phase 2 â€” no dependency on US2/US3
- **User Story 2 (P2)**: After Phase 2 â€” `JSON_DIR` runner, flag capture
- **User Story 3 (P3)**: After Phase 2 â€” extends scanner PARSE path; uses US2 runner

### Within Each User Story

- Models/validators (Phase 2) before scanner
- Scanner before runner hardening
- SCOPE path (US1) before PARSE path refinements (US3)
- Argv/exit/progress (US2) before polish quickstart run

### Parallel Opportunities

- T002â€“T005 after T001 can proceed in parallel
- T006â€“T008 and T011 in parallel after Setup
- T025â€“T026 in parallel during Polish

---

## Parallel Example: Foundational

```text
Task: "Implement SensorReading in src/main/java/pl/tomaszko/s03e01/model/SensorReading.java"
Task: "Implement SensorType in src/main/java/pl/tomaszko/s03e01/validation/SensorType.java"
Task: "Implement ReadingField/ReadingRanges in src/main/java/pl/tomaszko/s03e01/validation/"
Task: "Implement ScanSummary in src/main/java/pl/tomaszko/s03e01/scan/ScanSummary.java"
```

## Parallel Example: User Story 1

```text
# After T012 listing works, T013â€“T014 are sequential on JsonFileScanner.java
# T015 runner stub can start once T013 reporting path exists
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: SCOPE reporting on mixed fixtures
5. Continue to US2 for real CLI contract

### Incremental Delivery

1. Setup + Foundational â†’ foundation ready
2. US1 â†’ SCOPE MVP
- US2 â†’ `JSON_DIR`, progress, `FLAG: captured`, exit codes
- US3 â†’ PARSE resilience (including non-object root)
- Polish â†’ quickstart.md gate (`JSON_DIR`, no argv)

### Parallel Team Strategy

1. Team completes Setup + Foundational together
2. After Foundational: one developer can finish US1 scanner while another prepares fixtures/logging checks; US2/US3 mostly serialize on the same runner/scanner files

---

## Notes

- [P] = different files, no incomplete-task dependencies
- [USn] maps to spec user stories
- No automated test tasks (not requested); optional follow-up: unit tests under `src/test/java/pl/tomaszko/s03e01/`
- Commit after each task or logical group
- JDK: `C:\tools\jdk-23.0.2`
