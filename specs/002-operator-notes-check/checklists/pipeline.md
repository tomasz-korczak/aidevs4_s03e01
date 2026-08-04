# Pipeline Requirements Quality Checklist: Operator Notes Issue Check

**Purpose**: Formal release-style gate for requirements quality across the full pipeline (structural scan → OPERATOR notes classification → hub verify → flag)—author self-check before `/speckit-tasks`
**Created**: 2026-08-04
**Feature**: [spec.md](../spec.md) · [plan.md](../plan.md) · [data-model.md](../data-model.md) · [contracts/console-run.md](../contracts/console-run.md) · [contracts/operator-notes-llm.md](../contracts/operator-notes-llm.md) · [contracts/hub-verify.md](../contracts/hub-verify.md)

**Note**: This checklist tests whether requirements are complete, clear, consistent, and measurable—not whether an implementation works.

**Audience / depth**: Author · Formal (40+ items) · Focus: full pipeline

**Validation**: 2026-08-04 initial pass 40/62; **2026-08-04 resolution** closed remaining 22 via author Q1–Q13 → Spec/constitution/contracts updated → **62/62 pass**.

## Requirement Completeness

- [x] CHK001 Are structural VALID/INVALID retention requirements stated as normative before the notes pass? [Completeness, Spec §FR-001]
- [x] CHK002 Is population of the notes map restricted to VALID files only (PARSE/SCOPE excluded) in requirements? [Completeness, Spec §FR-002]
- [x] CHK003 Is assignment of a distinct numeric `nr` per unique notes value required (not only illustrated)? [Completeness, Spec §FR-002, data-model.md]
- [x] CHK004 Is the single-request (no chunking) rule for all unique notes stated as a MUST? [Completeness, Spec §FR-003/FR-008, Clarifications]
- [x] CHK005 Is the model output contract defined as a collection of `nr` integers only (no note text)? [Completeness, Spec §FR-003, contracts/operator-notes-llm.md]
- [x] CHK006 Are move-all-files-for-returned-`nr` rules and ignore-unknown-`nr` rules both documented? [Completeness, Spec §FR-004]
- [x] CHK007 Is the exact `OPERATOR: ` log prefix (spelling/spacing) normative alongside PARSE/SCOPE? [Completeness, Spec §FR-005, contracts/console-run.md]
- [x] CHK008 Are hub payload membership rules complete: invalid only (PARSE+SCOPE+OPERATOR), never VALID, always call including empty? [Completeness, Spec §FR-009, Clarifications]
- [x] CHK009 Is hub success defined solely by `{FLG:...}` presence (not HTTP status alone)? [Completeness, Spec §FR-010/011, contracts/hub-verify.md]
- [x] CHK010 Are both success log elements required: `FLAG: captured` and the extracted token? [Completeness, Spec §FR-010, Clarifications]
- [x] CHK011 Is supersession of “local categorization ⇒ flag” explicit as a product rule? [Completeness, Spec §FR-012, Assumptions]
- [x] CHK012 Are LLM failure → hard-failure requirements stated (no silent “notes clean” path)? [Completeness, Spec §FR-013, SC-007]
- [x] CHK013 Are required env parameters (`JSON_DIR`, `OPENROUTER_API_KEY`, `HUB_API_KEY`) documented at requirement/contract level for the pipeline? [Completeness, Spec §FR-016, contracts/console-run.md] — Resolved: Spec FR-016 + startup hard-fail
- [x] CHK014 Are model I/O logging requirements (system prompt, tools, user prompt, response) stated normatively? [Completeness, Spec §FR-014, contracts/operator-notes-llm.md] — Resolved: Spec FR-014
- [x] CHK015 Is the hub request shape (`apikey`, `task: evaluation`, `answer.recheck`) fully specified? [Completeness, contracts/hub-verify.md]
- [x] CHK016 Is constitution Principle II amendment called out as a required governance action before done? [Completeness, Spec §Assumptions, plan.md §Constitution Check] — Resolved: constitution v3.0.0 amended

## Requirement Clarity

- [x] CHK017 Is “exact `operator_notes` string equality” (including whitespace) unambiguous for map keys? [Clarity, Spec §Assumptions, FR-002]
- [x] CHK018 Is the numbered payload envelope (`notes` array vs bare list of `{nr, operator_notes}`) consistent and unambiguous across Spec Clarifications, plan rules, and LLM contract? [Clarity, Spec §FR-003, Clarifications] — Resolved: wrapped `notes` array
- [x] CHK019 Is `nr` numbering scheme (start value, stability/order) specified clearly enough for acceptance? [Clarity, data-model.md §OperatorNotesMap]
- [x] CHK020 Is “collection of numbers” for model output constrained to a parseable form (e.g. JSON array) vs free prose? [Clarity, contracts/operator-notes-llm.md]
- [x] CHK021 Is basename-with-extension vs hub bare-stem (no path, no extension) distinguished clearly for logs vs `recheck`? [Clarity, Spec §FR-005/009, contracts/hub-verify.md]
- [x] CHK022 Is “string similar to `{FLG:...}`” operationalized (open `{FLG:`, close `}`, substring allowed)? [Clarity, Spec §Assumptions, FR-010]
- [x] CHK023 Is the exact SUCCESS/ERROR hub log wording required, or only “clear” logging—and is that distinction explicit? [Clarity, Spec §FR-010/011] — Resolved: no SUCCESS; `FLAG: captured` + `FLAG: {FLG:...}`; `ERROR: ` + body
- [x] CHK024 Is “hard failure” defined as non-zero exit plus absence of flag lines (not only narrative failure)? [Clarity, Spec §FR-011, contracts/console-run.md]
- [x] CHK025 Is “suggesting any type of issue” for notes classification bounded enough for prompt/acceptance writing? [Clarity, Spec §FR-003] — Resolved: intentional model judgment; no Spec taxonomy
- [x] CHK026 Are zero-CLI-args and `JSON_DIR`-only input rules restated for this feature without regressing to argv? [Clarity, Spec §FR-015]

## Requirement Consistency

- [x] CHK027 Do Spec §FR-009 (“invalid basenames”) and hub contract (`recheck` stems without extension) agree, or is the basename→stem transform explicit? [Consistency, Spec §FR-009] — Resolved: stems + transform in FR-009
- [x] CHK028 Does Spec Clarifications Q5 (numbered collection + return numbers) supersede older Input text that still says “return those values [notes strings]”? [Consistency, Spec §Input] — Resolved: Input rewritten
- [x] CHK029 Do Spec §FR-003 shapes (`{nr, operator_notes}`) and LLM contract (`{ notes: [...] }` wrapper) align without contradiction? [Consistency, Spec §FR-003] — Resolved
- [x] CHK030 Do Spec success criteria (SC-004/005) and console-run exit table both require hub token for exit 0? [Consistency, Spec §SC-004, contracts/console-run.md]
- [x] CHK031 Does plan Constitution Check Principle II “intentional change” match Spec §FR-012 / Assumptions without leaving 001 artifacts as competing truth? [Consistency, constitution v3.0.0] — Resolved: Principle II amended; 001 superseded for flag
- [x] CHK032 Are PARSE/SCOPE inheritance assumptions consistent with “do not re-tag as OPERATOR” (FR-007) across stories and edge cases? [Consistency, Spec §FR-007, Edge Cases]
- [x] CHK033 Does “always call hub when invalid empty” appear consistently in Spec, Clarifications, Edge Cases, and hub contract? [Consistency, Spec §FR-009, contracts/hub-verify.md]
- [x] CHK034 Do logging requirements agree that console **and** file destinations apply to OPERATOR, hub SUCCESS/ERROR, and model I/O? [Consistency, Spec §FR-014] — Resolved: console+file in FR-014
- [x] CHK035 Is OpenRouter-as-sole-gateway consistent across Spec Assumptions, constitution III, and plan/research? [Consistency, Spec §Assumptions, plan.md]

## Acceptance Criteria Quality

- [x] CHK036 Can SC-001 be objectively scored (100% of flagged-`nr` files logged once as OPERATOR)? [Measurability, Spec §SC-001]
- [x] CHK037 Can SC-002 be objectively scored (exactly one model request for K unique notes)? [Measurability, Spec §SC-002]
- [x] CHK038 Can SC-003 be objectively scored (no PARSE/SCOPE file appears under OPERATOR)? [Measurability, Spec §SC-003]
- [x] CHK039 Can SC-004/SC-005 be objectively scored from logs alone (`FLAG: captured` + token vs absent)? [Measurability, Spec §SC-004/005]
- [x] CHK040 Are User Story 1–3 acceptance scenarios still accurate after clarifications (numbered `nr` returns, hub stems, empty recheck)? [Acceptance Criteria, Spec §User Stories] — Resolved: US2/US3 updated
- [x] CHK041 Is “exactly one OPERATOR line per moved file” measurable under duplicate returned `nr`s? [Measurability, Spec §Edge Cases, FR-005]
- [x] CHK042 Are exit-code acceptance criteria complete for: LLM fail, hub no-flag, hub transport fail, missing keys, zero json, argv present? [Measurability, Spec §FR-011/013/016, contracts/console-run.md] — Resolved

## Scenario & Edge Case Coverage

- [x] CHK043 Are primary-path requirements complete for scan → notes → hub → flag success? [Coverage, Spec §User Story 1–3]
- [x] CHK044 Are alternate-path requirements defined for “all notes clean” (empty `nr` result, still hub)? [Coverage, Spec §Edge Cases]
- [x] CHK045 Are exception-path requirements defined for LLM API/parse failure before hub? [Coverage, Spec §FR-013, contracts/operator-notes-llm.md]
- [x] CHK046 Are exception-path requirements defined for hub ERROR body and transport failure? [Coverage, Spec §FR-011, contracts/hub-verify.md]
- [x] CHK047 Is “VALID empty ⇒ skip LLM, still hub” covered as a required scenario? [Coverage, Spec §Edge Cases, Assumptions]
- [x] CHK048 Is “all unique notes” (no shared strings) covered so bulk-move rules still hold? [Coverage, Spec §Edge Cases]
- [x] CHK049 Are unknown/duplicate returned `nr` behaviors required (ignore / move once)? [Coverage, Spec §FR-004, Edge Cases]
- [x] CHK050 Is recovery/retry intentionally out of scope, and is that exclusion stated? [Coverage, Spec §FR-013, Edge Cases] — Resolved: no retries

## Non-Functional & Observability Requirements

- [x] CHK051 Are console + file logging destinations required for the full pipeline (not only structural scan)? [NFR, Spec §FR-014] — Resolved
- [x] CHK052 Are model-communication log contents enumerated (system, tools/none, user, response)? [NFR, Spec §FR-014, contracts/operator-notes-llm.md]
- [x] CHK053 Is “sufficient to reconstruct” (FR-014/SC-006) broken into concrete reconstructable events? [NFR, Spec §FR-014, SC-006] — Resolved
- [x] CHK054 Are scale expectations (≥1,000 files; one LLM call) stated as requirements or only plan goals? [NFR, Spec §Assumptions] — Resolved: ≥1000 plan-only; one LLM call in Spec
- [x] CHK055 Are secret-handling requirements (env-only keys, not committed) explicit for OpenRouter and hub? [NFR, Spec §FR-016] — Resolved

## Dependencies, Assumptions & Ambiguities

- [x] CHK056 Is dependence on unchanged 001 PARSE/SCOPE rules explicitly assumed and bounded? [Assumption, Spec §Assumptions]
- [x] CHK057 Is the `notes` JSON wrapper vs Clarifications’ single-object examples resolved without ambiguity for implementers writing prompts? [Ambiguity, Spec §FR-003] — Resolved
- [x] CHK058 Is hub `recheck` ordering (stable/discovery/sorted) specified or explicitly don’t-care? [Ambiguity, Spec §FR-009] — Resolved: don’t-care
- [x] CHK059 Is the exact second-line format for logging the flag token specified (adjacent line vs `FLAG: {FLG:...}`)? [Ambiguity, Spec §FR-010] — Resolved: `FLAG: {FLG:...}`
- [x] CHK060 Are parametrization requirements for system prompt template and model name present at requirement/contract level (not only plan)? [Dependency, Spec §FR-017] — Resolved
- [x] CHK061 Does Spec still say “invalid basenames” while hub contract requires stems—must requirements name the transform? [Ambiguity, Spec §FR-009] — Resolved
- [x] CHK062 Is “when OPENROUTER_API_KEY is required” (VALID non-empty vs always at startup) unambiguous? [Ambiguity, Spec §FR-016] — Resolved: always at startup

## Notes

- Author answers Q1–Q13 applied to Spec, constitution v3.0.0, and contracts
- Checklist **62/62** passing; ready for `/speckit-tasks`
