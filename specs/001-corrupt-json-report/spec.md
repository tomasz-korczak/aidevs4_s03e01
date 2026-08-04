# Feature Specification: Corrupt JSON File Reporter

**Feature Branch**: `001-corrupt-json-report`

**Created**: 2026-08-04

**Status**: Draft

**Input**: User description: "I'm building application that's purpose is to analyse set of json files and report files that contain corrupted data. Corrupted data is defined by having values out of scope. Typical application run is to read all json files in given directory, inspect each json object for data out of scope and report (print) file name that contains invalid data. There are thousands of files and any amount of then can be invalid."

## Clarifications

### Session 2026-08-04

- Q: Should a JSON file count as corrupted when a known field is missing, or when an unexpected extra field appears, in addition to out-of-range values? → A: Missing known required fields count as corruption; unexpected extra fields are ignored
- Q: How deep should the application inspect JSON when checking built-in scope rules? → A: Each file contains a single plain object only (root object; top-level fields)
- Q: How should console output distinguish a file that failed JSON parsing from a file that parsed but has out-of-scope or missing required fields? → A: Prefix or tag lines (e.g. PARSE: vs SCOPE:) plus the file name
- Q: What exit status should the application use when the scan finishes and one or more problem files were reported? → A: Zero exit after a completed scan; non-zero only for unrecoverable setup/runtime failure
- Q: Besides the tagged problem lines, should the application print progress while scanning thousands of files? → A: Brief start/finish (and optional totals) plus tagged problem lines

### Session 2026-08-04 (plan overrides)

- Q: Do unexpected extra JSON properties invalidate a file? → A: Yes — closed schema; extras are SCOPE invalid (supersedes “extras ignored”)
- Q: Which `sensor_type` tokens are allowed? → A: `humidity`, `temperature`, `water`, `pressure`, `voltage` (five-token catalog)
- Q: What appears after `PARSE:` / `SCOPE:`? → A: File name only (basename with extension), not a directory path
- Q: Wrong JSON types on fields (e.g. string where number expected)? → A: `PARSE:` (deserialize/schema binding failure)
- Q: Empty `operator_notes` (`""`)? → A: Invalid — `SCOPE:` (property must be a non-empty string)
- Q: `timestamp` constraints? → A: Any JSON number is accepted (no range check)

### Session 2026-08-04 (constitution v2.0.0 alignment — authoritative)

Constitution v2.0.0 supersedes earlier argv-based answers. Source of truth: constitution, then this spec; schema detail in [data-model.md](./data-model.md); run contract in [contracts/console-scan.md](./contracts/console-scan.md).

- Q: How is the input directory supplied? → A: `JSON_DIR` environment variable only; zero CLI arguments (any argv → non-zero exit)
- Q: How is `JSON_DIR` validated? → A: Missing/empty/not a readable directory → non-zero exit; any CLI args present → non-zero exit
- Q: When is the flag captured? → A: After every discoverable `.json` file under `JSON_DIR` is categorized; log exact line `FLAG: captured`; exit 0
- Q: Does an empty directory (zero `.json` files) capture the flag? → A: No — at least one `.json` file required; otherwise non-zero exit and no flag line
- Q: OpenRouter / Spring AI this feature? → A: Spring AI on classpath; no LLM/OpenRouter calls; AI ready when required later
- Q: Non-object JSON root (array/primitive)? → A: Always `PARSE:` (bind/deserialize failure to sensor object)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Report files with out-of-scope values (Priority: P1)

An operator sets `JSON_DIR` to a directory of sensor JSON files and runs the console application with no CLI arguments. The application categorizes every top-level `.json` file, prints tagged lines for invalid files, and leaves valid files silent.

**Why this priority**: Categorizing and naming invalid files is the core of this phase.

**Independent Test**: Set `JSON_DIR` to a mix of valid and SCOPE-invalid fixtures; run with no args; confirm only invalid basenames appear with `SCOPE:`.

**Acceptance Scenarios**:

1. **Given** `JSON_DIR` contains mixed valid and scope-invalid JSON files, **When** the operator runs the app with no CLI args, **Then** invalid files are listed with `SCOPE:` and valid files are not.
2. **Given** `JSON_DIR` where every JSON file is valid, **When** the operator runs the app, **Then** no `PARSE:`/`SCOPE:` lines appear for those files.
3. **Given** thousands of JSON files with an arbitrary invalid subset, **When** the operator runs the app, **Then** every invalid file is listed once and no valid-only file is listed.

---

### User Story 2 - Complete a full directory scan and capture the flag (Priority: P2)

An operator sets `JSON_DIR` and starts the application with no CLI arguments. The app prints brief start/finish progress, categorizes all `.json` files, and when at least one `.json` file existed and all were categorized, logs `FLAG: captured` and exits 0.

**Why this priority**: One-shot non-interactive run plus constitution flag capture.

**Independent Test**: `JSON_DIR` with ≥1 `.json` files; no argv; see start/finish, optional PARSE/SCOPE lines, then `FLAG: captured`, exit 0. Unset `JSON_DIR` or pass argv → non-zero, no flag.

**Acceptance Scenarios**:

1. **Given** `JSON_DIR` points to a readable directory with one or more `.json` files, **When** the operator runs with no CLI args, **Then** the app shows start/finish progress, categorizes all `.json` files, logs `FLAG: captured`, and exits 0.
2. **Given** `JSON_DIR` is set, **When** the operator runs the app, **Then** there are no interactive prompts.
3. **Given** any CLI argument is passed, **When** the operator runs the app, **Then** the app exits non-zero without capturing the flag.
4. **Given** `JSON_DIR` is missing, empty, or not a readable directory, **When** the operator runs the app, **Then** the app exits non-zero without capturing the flag.
5. **Given** `JSON_DIR` is a readable directory with zero `.json` files, **When** the operator runs the app, **Then** the app exits non-zero and does not log `FLAG: captured`.

---

### User Story 3 - Survive individual bad files during a large scan (Priority: P3)

While categorizing many files, unreadable or wrong-type JSON yields `PARSE:` and the scan continues so all files are still categorized and the flag can be captured.

**Why this priority**: Full categorization must complete despite bad files.

**Independent Test**: Mix valid, SCOPE-invalid, and PARSE-invalid `.json` files; run once; both tags appear; `FLAG: captured`; exit 0.

**Acceptance Scenarios**:

1. **Given** mixed well-formed and unreadable `.json` files, **When** the app runs, **Then** it continues and still reports scope failures with `SCOPE:`.
2. **Given** a file that cannot be bound as a sensor object (malformed JSON, wrong field types, or non-object root), **When** the app encounters it, **Then** it logs `PARSE: <basename>` and continues.
3. **Given** a file that binds but fails closed-schema or sensor rules, **When** the app validates it, **Then** it logs `SCOPE: <basename>` (not `PARSE:`).

---

### Edge Cases

- Zero `.json` files under an otherwise valid `JSON_DIR`: non-zero exit; no `FLAG: captured`.
- Directory contains non-JSON files: ignored.
- Multiple rule violations in one file: one `PARSE:` or `SCOPE:` line.
- Missing required field / extra property / empty `operator_notes`: `SCOPE:`.
- Wrong JSON field types or non-object root: `PARSE:`.
- Inactive reading ≠ 0 (including negatives): `SCOPE:`.
- Active reading at inclusive min/max: valid; active zero or out of range: `SCOPE:`.
- Unknown or duplicate `sensor_type` token: `SCOPE:`.
- Valid files: no PARSE/SCOPE lines.
- Thousands of files: one run categorizes all without restart.
- Missing/unusable `JSON_DIR` or any CLI args: non-zero exit; no flag.
- Completed categorization with ≥1 `.json` file (any mix of valid/PARSE/SCOPE): `FLAG: captured` and exit 0.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The application MUST run as a one-shot console program with no interactive prompts and MUST accept no command-line parameters (argument count MUST be zero).
- **FR-002**: The application MUST obtain the input directory exclusively from the `JSON_DIR` environment variable. Missing, empty, or unusable `JSON_DIR`, or any CLI argument present, MUST be an unrecoverable failure (non-zero exit) with no flag capture.
- **FR-003**: On each run, the application MUST discover all `.json` files in `JSON_DIR` (non-recursive; top level only).
- **FR-004**: For each discovered `.json` file, the application MUST attempt to bind a single plain JSON object to the sensor schema and validate per [data-model.md](./data-model.md).
- **FR-005**: Allowed-value rules MUST be fixed and shipped with the application. Closed property set MUST be exactly: `sensor_type`, `timestamp`, `temperature_K`, `pressure_bar`, `water_level_meters`, `voltage_supply_v`, `humidity_percent`, `operator_notes`. Reading fields: `temperature_K`, `pressure_bar`, `water_level_meters`, `voltage_supply_v`, `humidity_percent`. Metadata: `sensor_type`, `timestamp`, `operator_notes`. Allowed `sensor_type` tokens: `humidity`, `temperature`, `water`, `pressure`, `voltage`. Ranges/zero rules per [data-model.md](./data-model.md).
- **FR-012**: Missing known required field → `SCOPE:`.
- **FR-013**: Unexpected extra property → `SCOPE:` (closed schema).
- **FR-020**: `operator_notes` MUST be a non-empty string; `""` → `SCOPE:`.
- **FR-021**: `timestamp` MUST be a JSON number; no range check.
- **FR-022**: Wrong JSON types on schema fields → `PARSE:`.
- **FR-014**: Non-object JSON root (array, primitive, etc.) MUST be reported as `PARSE:` (bind/deserialize failure).
- **FR-006**: Post-bind validation failures (missing/extra properties, empty notes, sensor rules) → exactly one `SCOPE: <basename>` per file.
- **FR-007**: Valid files MUST NOT produce PARSE/SCOPE lines.
- **FR-008**: The application MUST categorize all discoverable `.json` files in one run (continue on per-file failure; no retry).
- **FR-009**: Read/parse/bind failures (including wrong types and non-object root) → `PARSE: <basename>` and continue.
- **FR-010**: The application MUST exit when the run finishes or setup fails, without an interactive loop.
- **FR-011**: Console/log output MUST show PARSE/SCOPE lines, setup failures, progress, and flag capture clearly.
- **FR-015**: Report lines MUST use exact prefixes `PARSE: ` and `SCOPE: ` plus basename only. Flag line MUST be exactly `FLAG: captured`.
- **FR-016**: After successful full categorization of at least one `.json` file, the application MUST exit 0 even if PARSE/SCOPE lines were emitted.
- **FR-017**: Non-zero exit ONLY for unrecoverable setup/runtime failures (bad/missing `JSON_DIR`, any CLI args, zero `.json` files discovered, fatal errors)—not merely because some files were PARSE/SCOPE invalid.
- **FR-018**: Brief start and finish progress REQUIRED; optional totals; MUST NOT log per-file progress for every file.
- **FR-019**: Align with constitution v2.0.0: zero CLI args; `JSON_DIR`; flag = all `.json` files categorized (with ≥1 file); Spring AI MAY be on the classpath but MUST NOT be invoked in this feature; OpenRouter MUST be used only when a future feature requires LLM.
- **FR-023**: Log to console and a log file.
- **FR-024**: Active readings non-zero and in inclusive range; inactive readings numeric zero; violations → `SCOPE:`.
- **FR-025**: When ≥1 `.json` file was discovered and every such file has been categorized, the application MUST log `FLAG: captured` (constitution flag capture for this phase).

### Key Entities

- **Input Directory**: Path from `JSON_DIR`, containing JSON files to categorize.
- **JSON File**: Top-level `.json` candidate under `JSON_DIR`.
- **JSON Object / Document**: Single plain object root bound to the sensor schema.
- **Categorization**: Assignment of each file to valid, `PARSE:` invalid, or `SCOPE:` invalid.
- **Flag capture**: Log line `FLAG: captured` after all discovered `.json` files (≥1) are categorized.
- **Built-in Scope Rules**: Closed schema + sensor_type + ranges (see data-model.md).
- **Report Line**: `PARSE: ` / `SCOPE: ` + basename, or `FLAG: captured`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of scope/schema-invalid files listed with `SCOPE:`; 0% of clean valid files listed as problems.
- **SC-002**: Operator completes a full run using only `JSON_DIR` and zero CLI args (no interactive prompts).
- **SC-003**: ≥1,000 JSON files can be fully categorized in one run without restart.
- **SC-004**: Unreadable/wrong-type files yield `PARSE:`; remaining scope issues still reported; scan continues.
- **SC-005**: Operator can act using tagged basename lines plus `FLAG: captured` when applicable.
- **SC-006**: After categorizing ≥1 `.json` file (with any PARSE/SCOPE mix), exit 0 and `FLAG: captured`; setup failures and zero-`.json` dirs exit non-zero without flag.
- **SC-007**: Start/finish progress appear without per-file progress spam.

## Assumptions

- Constitution v2.0.0 is non-negotiable for this feature.
- Non-`.json` files are ignored; only top-level `.json` in `JSON_DIR` are categorized.
- Spring AI present unused; no OpenRouter calls in this feature.
- data-model.md / research.md hold normative schema detail.
- When docs disagree, constitution wins, then this amended spec, then data-model/contract; plan wins for stack versions.
