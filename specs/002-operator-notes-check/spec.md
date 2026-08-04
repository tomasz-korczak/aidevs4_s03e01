# Feature Specification: Operator Notes Issue Check

**Feature Branch**: `002-operator-notes-check`

**Created**: 2026-08-04

**Status**: Draft

**Input**: User description (authoritative intent after clarifications): Add a post-structural check on VALID files’ `operator_notes` using a single AI request. Unique notes are sent as `{ "notes": [ { "nr", "operator_notes" }, ... ] }`; the model returns only issue-bearing `nr` values; matching files move to INVALID as `OPERATOR:`. Then POST invalid file stems to the hub; flag success only when the response contains `{FLG:...}` (else hard failure).

## Clarifications

### Session 2026-08-04

- Q: Which file names should be submitted to the hub after categorization (including OPERATOR moves) finishes? → A: Only finalized invalid files (`PARSE`, `SCOPE`, and `OPERATOR`) — clarified further below as **stems** for hub
- Q: When the hub response contains a `{FLG:...}` token, what should the application log as proof of success? → A: `FLAG: captured` then `FLAG: {FLG:...}` (no SUCCESS prefix)
- Q: If every file ends up valid (empty invalid list after OPERATOR checks), should the application still call the hub? → A: Always call the hub, even with an empty invalid list
- Q: How should unique `operator_notes` values be sent to the model when there are many of them? → A: Single model request containing all unique notes from the valid set
- Q: How strictly must model-returned notes match? → A: Do not match on note text; numbered collection; move by returned `nr`s

### Session 2026-08-04 (pipeline checklist resolution)

- Q: LLM request body envelope? → A: Wrapped `{ "notes": [ { "nr", "operator_notes" }, ... ] }`
- Q: Hub `recheck` identity? → A: Stems without extension (e.g. `0001`); local logs keep basename with `.json`
- Q: When is `OPENROUTER_API_KEY` required? → A: Always at startup (hard fail if missing/blank)
- Q: Logging sinks and model I/O? → A: Console + file for all pipeline logs; MUST log system prompt, tools/none, user prompt, and response
- Q: Exact hub success/failure log lines? → A: Success: `FLAG: captured` then `FLAG: {FLG:...}`; Failure: `ERROR: ` + hub body (or failure detail)
- Q: Bound “any type of issue” in Spec? → A: No — intentionally left to model judgment; prompt may add examples without Spec taxonomy
- Q: `recheck` ordering? → A: Don’t-care (explicit)
- Q: Retries? → A: No retries for hub or LLM (single attempt each)
- Q: ≥1000 files as Spec success criterion? → A: Plan-only goal; not a Spec requirement
- Q: Parametrized prompt + model in Spec? → A: Spec MUST require configurable system-prompt template and model name (defaults allowed)
- Q: Constitution Principle II? → A: Amend now to hub-`{FLG:...}` success; feature 001 flag rule superseded for product success
- Q: Stale Input blurb vs Clarifications? → A: Clarifications authoritative; Input rewritten to match
- Q: Secrets handling in Spec? → A: Spec FR: `OPENROUTER_API_KEY` and `HUB_API_KEY` from env only; never commit

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reclassify structurally valid files whose notes report issues (Priority: P1)

An operator runs the existing one-shot scan. After structural classification finishes, files still on the valid list are checked for issue language in `operator_notes`. Notes that indicate problems cause those files to be moved to invalid and logged with the new `OPERATOR:` category, even when all sensor readings passed scope rules.

**Why this priority**: Catching “readings fine but notes say otherwise” is the core new detection capability.

**Independent Test**: Provide structurally valid fixtures that share a notes string reporting an issue; after the notes check, those files must leave the valid set and appear as `OPERATOR: <basename>`.

**Acceptance Scenarios**:

1. **Given** files that pass PARSE/SCOPE checks but whose `operator_notes` text indicates a problem, **When** the notes check runs, **Then** those files are moved from valid to invalid and each is logged once as `OPERATOR: <basename>`.
2. **Given** structurally valid files whose `operator_notes` do not suggest issues, **When** the notes check runs, **Then** those files remain valid and produce no `OPERATOR:` line.
3. **Given** files already classified invalid (PARSE or SCOPE), **When** the notes check runs, **Then** they are not re-evaluated via notes grouping and keep their original invalid category.

---

### User Story 2 - Deduplicate notes before asking the model (Priority: P2)

Many valid files share identical `operator_notes` text. The run groups valid files by that exact notes string, assigns each unique notes value a stable number `nr`, sends one request body `{ "notes": [ { "nr", "operator_notes" }, ... ] }` to the model, and applies each returned `nr` to every file that shares that notes value.

**Why this priority**: Deduplication is required for correct bulk reclassification and for keeping model usage proportional to unique notes, not file count.

**Independent Test**: Multiple valid files share one issue-bearing notes string and others share a clean notes string; the model receives one numbered item per unique string under `notes` and returns only issue `nr`s; all files under flagged numbers become `OPERATOR:`, clean-string files stay valid.

**Acceptance Scenarios**:

1. **Given** N structurally valid files that share the same `operator_notes` value whose assigned `nr` the model returns as issue-bearing, **When** the notes check completes, **Then** all N files are moved to invalid and each is logged as `OPERATOR: <basename>`.
2. **Given** M distinct `operator_notes` values among valid files, **When** the notes check prepares model input, **Then** the model receives a single request whose body is `{ "notes": [ ... M items ... ] }` with objects `{ "nr": <id>, "operator_notes": "<text>" }` (not one request per file, and not chunked).
3. **Given** the model returns only a subset of those `nr` values, **When** reclassification applies, **Then** only files whose notes map to the returned numbers move to invalid; others stay valid.

---

### User Story 3 - Submit results to hub and capture flag only from hub response (Priority: P1)

After final categorization (including OPERATOR moves), the application submits the finalized invalid file **stems** (`PARSE`, `SCOPE`, and `OPERATOR`) to the challenge hub, inspects the HTTP response, and treats the flag as acquired only when the response body contains a token resembling `{FLG:...}`. Any other hub outcome is a hard failure (no flag success).

**Why this priority**: Flag acquisition is the product goal and now depends on hub validation, not local categorization alone.

**Independent Test**: Complete a run against a hub response that embeds `{FLG:...}` → success path with flag lines; repeat with a response lacking that pattern → hard failure, no flag lines.

**Acceptance Scenarios**:

1. **Given** categorization (including OPERATOR) has finished, **When** the application submits only the finalized invalid stems to the hub and the HTTP response body contains `{FLG:...}`, **Then** the run exits 0 and logs exactly `FLAG: captured` and `FLAG: {FLG:...}` (extracted token).
2. **Given** the hub HTTP response does not contain `{FLG:...}`, **When** the application inspects the response, **Then** it logs `ERROR: ` plus the hub body, exits non-zero, and does not log flag success lines.
3. **Given** the hub call fails unrecoverably (unreachable host, non-success transport/setup), **When** the run cannot obtain a usable response, **Then** it logs `ERROR: ` with failure detail, exits non-zero, and does not claim flag success.
4. **Given** categorization finishes with zero invalid files, **When** the application performs hub validation, **Then** it still calls the hub with an empty `recheck` array and applies the same `{FLG:...}` success/failure rules.

---

### Edge Cases

- No structurally valid files after PARSE/SCOPE: skip notes grouping and model call; still call the hub with the finalized invalid stems.
- Zero invalid files after all checks (all remain valid): still call the hub with `recheck: []`; success only if response contains `{FLG:...}`.
- Valid files exist but every `operator_notes` value is unique: model still receives one numbered item per value under `notes`; no incorrect cross-file moves.
- Model returns empty number collection (no issue-bearing notes): no OPERATOR moves; valid list unchanged; still call hub.
- Model returns an `nr` not present in the submitted collection: ignore unknown numbers; do not invent moves.
- Duplicate returned numbers or partial overlap: each matching file moves at most once; still one `OPERATOR:` line per file.
- Files already PARSE/SCOPE invalid: never appear under `OPERATOR:` for the same run.
- Empty `operator_notes` remains a SCOPE failure from prior classification (never reaches VALID notes check).
- Hub returns a flag-like token with surrounding text/noise: still counts as acquired if a `{FLG:...}` substring is present; log `FLAG: captured` and `FLAG: {FLG:...}`.
- Hub returns HTTP success but body has no `{FLG:...}`: hard flag failure (`ERROR: ` + body; no flag lines).
- Model/API failure during notes classification: hard failure with `ERROR: ` detail; do not silently treat notes as clean; do not call hub pretending success; no flag claim.
- Missing/blank `OPENROUTER_API_KEY` or `HUB_API_KEY` at startup: hard failure before/at run start.
- Hub/`recheck` entry order: don’t-care.
- No automatic retries for LLM or hub (single attempt each).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The application MUST retain the existing structural classification flow that produces separate VALID and INVALID collections for discovered `.json` files under `JSON_DIR`.
- **FR-002**: After structural classification, the application MUST build a notes map whose keys are exact `operator_notes` string values and whose values are the lists of valid JSON file records that share that key. The map MUST be populated only from the VALID collection. Each unique map key MUST be assigned a distinct numeric `nr` for model exchange.
- **FR-003**: The application MUST send those unique notes to an AI language model in a single request whose body is exactly shaped as `{ "notes": [ { "nr": <id>, "operator_notes": "<text>" }, ... ] }` (one array element per unique notes value; no chunking into multiple model calls), asking the model to return only the collection of `nr` values whose corresponding notes suggest any issue with the readings—despite otherwise-valid readings. Issue detection criteria are intentionally left to the model (no Spec taxonomy).
- **FR-004**: For every `nr` the model returns, the application MUST resolve that number to its `operator_notes` map entry and MUST move all corresponding files from VALID to INVALID. Returned numbers that do not match a submitted `nr` MUST be ignored.
- **FR-005**: Files invalidated by the notes check MUST be logged with the exact category prefix `OPERATOR: ` followed by the file basename **with extension** (same basename style as existing `PARSE:` / `SCOPE:` lines).
- **FR-006**: Files that remain valid after the notes check MUST NOT produce `OPERATOR:` lines.
- **FR-007**: Structurally invalid files (PARSE/SCOPE) MUST keep their original category tags and MUST NOT be re-tagged as `OPERATOR:` solely because of notes content.
- **FR-008**: The notes classifier MUST operate on unique notes strings (deduplicated), not one model classification item per file, so shared notes yield a single `nr` applied to all sharing files. The numbered `notes` collection MUST be submitted in one model request. Reclassification MUST key off returned `nr` values, not off returned note text.
- **FR-009**: After final VALID/INVALID membership is settled (including OPERATOR moves), the application MUST submit only the finalized invalid file **stems** (`PARSE`, `SCOPE`, and `OPERATOR`) to the challenge hub for validation and MUST inspect the HTTP response body. A stem is the basename without path and without the `.json` extension (e.g. log `0001.json` → hub `0001`). Valid files MUST NOT be included. The hub MUST be called even when the invalid list is empty (`recheck: []`). Order of `recheck` entries is don’t-care.
- **FR-010**: Flag acquisition MUST succeed only when the hub response body contains a string matching `{FLG:...}` (opening `{FLG:`, payload, closing `}`). Presence of that pattern is the sole success criterion for capturing the flag. On success the application MUST log exactly these two lines (in order): `FLAG: captured` and `FLAG: {FLG:...}` where the second line contains the extracted token. No `SUCCESS` prefix is used.
- **FR-011**: If the hub response lacks a `{FLG:...}`-like string, OR the hub request/response cannot be completed successfully, the application MUST treat flag acquisition as a hard failure: non-zero exit; log `ERROR: ` followed by the hub response body or failure detail; no `FLAG: captured` and no `FLAG: {FLG:...}` lines.
- **FR-012**: Local completion of file categorization alone MUST NOT be treated as flag success under this feature (supersedes feature 001 / prior constitution “all files categorized ⇒ flag captured”).
- **FR-013**: LLM / AI integration failures (including non-parsable model output) during notes classification MUST log `ERROR: ` with detail and MUST cause a hard failure of the run (non-zero exit; no silent skip that pretends notes are clean; no flag claim). LLM and hub calls MUST NOT be retried automatically (single attempt each).
- **FR-014**: The application MUST log to **console and file**. Logs MUST be sufficient to reconstruct: structural categories, OPERATOR moves, hub submission outcome, flag lines when present, and—for each LLM call—the fully rendered system prompt, tool definitions or explicit `tools: none`, user prompt/notes JSON payload, and raw model response.
- **FR-015**: Existing one-shot console constraints remain: zero CLI arguments; input directory from `JSON_DIR`; non-interactive run.
- **FR-016**: `JSON_DIR`, `OPENROUTER_API_KEY`, and `HUB_API_KEY` MUST be supplied via environment variables (or non-committed local env files). Secrets MUST NOT be committed to version control. Missing or blank `OPENROUTER_API_KEY` or `HUB_API_KEY` at application startup MUST be a hard failure (non-zero exit). Missing/unusable `JSON_DIR` remains a hard failure as in prior feature rules.
- **FR-017**: The LLM system prompt MUST be supplied via a configurable string template (application configuration / template resource). The LLM model name MUST be configurable in application configuration (a default MAY be provided). Both MUST be changeable without code edits to business logic.

### Key Entities

- **VALID collection**: Files that passed structural PARSE/SCOPE checks and have not been moved by the notes check.
- **INVALID collection**: Files failed for PARSE, SCOPE, or OPERATOR reasons.
- **Operator notes map**: Mapping from unique `operator_notes` text → list of valid file records sharing that text, each unique key also paired with a distinct numeric `nr`.
- **Notes classification request**: JSON object `{ "notes": [ { "nr", "operator_notes" }, ... ] }` sent to the model.
- **Notes classification result**: Collection of `nr` values the model judges issue-bearing; used to select map entries for OPERATOR moves.
- **OPERATOR category**: Invalid reason tag for files moved because notes suggest issues despite fine readings.
- **Hub stem**: Basename without `.json` extension, used in hub `recheck`.
- **Hub validation response**: HTTP response from the challenge hub inspected for a `{FLG:...}`-like token.
- **Flag token**: Substring resembling `{FLG:...}` that constitutes successful flag acquisition.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of structurally valid files whose notes’ assigned `nr` the model returns as issue-bearing appear exactly once as `OPERATOR: <basename>`; none remain silently valid.
- **SC-002**: When K unique notes strings exist among valid files, they are classified in one model request as `{ "notes": [ ... K items ... ] }` (not once per file and not across multiple chunked requests), and every file sharing a notes value whose `nr` was returned is reclassified together.
- **SC-003**: Structurally invalid files never appear under `OPERATOR:` for notes that were only evaluated on the valid set.
- **SC-004**: A hub response containing a `{FLG:...}`-like token results in exit 0 with log lines `FLAG: captured` and `FLAG: {FLG:...}` present.
- **SC-005**: A hub response without a `{FLG:...}`-like token results in hard failure (`ERROR: ` + body; no flag lines) in 100% of such runs.
- **SC-006**: An operator can reconstruct from console and file logs which files were OPERATOR-invalidated, the LLM I/O for the notes call (when made), and on success the exact `{FLG:...}` value via `FLAG: {FLG:...}`.
- **SC-007**: Model/API failure during notes classification never produces a silent “all notes clean” success path.
- **SC-008**: Missing `OPENROUTER_API_KEY` or `HUB_API_KEY` at startup never proceeds to a successful flag claim.

## Assumptions

- Prior feature behavior for PARSE/SCOPE structural validation remains in force; this feature adds a post-pass notes check and changes flag success criteria.
- Hub `recheck` contains stems of finalized invalid files (PARSE, SCOPE, OPERATOR); local report lines use basenames with extension.
- “String similar to `{FLG:...}`” means a substring matching the pattern `{FLG:` … `}` (non-greedy content inside); surrounding response text is allowed.
- Exact `operator_notes` string equality (including whitespace) defines map keys and shared grouping.
- Spring AI is the integration layer; OpenRouter remains the required LLM gateway per constitution when AI is invoked.
- Constitution Principle II is amended to hub-`{FLG:...}` success; feature 001’s “all categorized ⇒ flag” rule is superseded for product success.
- When the valid set is empty, no LLM call is required for notes classification. `OPENROUTER_API_KEY` MUST still be present at startup (hard fail if missing/blank)—stricter than constitution Runtime wording “when AI is required,” by deliberate product choice for this feature.
- Model output is a collection of issue-bearing `nr` values only (JSON array preferred); unknown `nr`s are ignored; note-text matching is not used for moves.
- Unique notes from the valid set are classified in one model request (no batch/chunk splitting) as a wrapped `notes` array.
- ≥1,000-file throughput remains a plan performance goal, not a Spec success criterion.
- No automatic retries for LLM or hub.
