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

### Session 2026-08-04 (plan overrides — authoritative)

Plan-time operator input and design artifacts supersede earlier clarification answers where they conflict. Source of truth for functional behavior: this spec (as amended below); for exact schema/ranges/tokens: [data-model.md](./data-model.md); for CLI contract: [contracts/console-scan.md](./contracts/console-scan.md); for stack: [plan.md](./plan.md).

- Q: How is the input directory supplied? → A: First program argument (argv); not environment-only configuration
- Q: Do unexpected extra JSON properties invalidate a file? → A: Yes — closed schema; extras are SCOPE invalid (supersedes “extras ignored”)
- Q: How are structural failures tagged after a successful JSON parse attempt? → A: Missing required properties, extra properties, and non-object root use `SCOPE:` (no third STRUCT tag); only unreadable/non-deserializable content uses `PARSE:`
- Q: If more than one program argument is passed, what should happen? → A: Fail with non-zero exit (exactly one argument required: the input directory)
- Q: Which `sensor_type` tokens are allowed? → A: `humidity`, `temperature`, `water`, `pressure`, `voltage` (five-token catalog)
- Q: What appears after `PARSE:` / `SCOPE:`? → A: File name only (basename with extension), not a directory path
- Q: Wrong JSON types on fields (e.g. string where number expected)? → A: `PARSE:` (deserialize/schema binding failure)
- Q: Empty `operator_notes` (`""`)? → A: Invalid — `SCOPE:` (property must be a non-empty string)
- Q: `timestamp` constraints? → A: Any JSON number is accepted (no range check)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Report files with out-of-scope values (Priority: P1)

An operator runs the console application once, passing the input directory as the first program argument. The application reads every JSON file in that directory, inspects each file's JSON content against built-in sensor/schema rules, and prints a tagged line for every invalid file. The operator uses the printed list to identify which files need attention.

**Why this priority**: Detecting and naming invalid files is the core purpose of the application.

**Independent Test**: Place a mix of valid and intentionally invalid JSON files in the input directory, run the application with that directory as the first argument, and confirm only the invalid file names are printed with the correct tags.

**Acceptance Scenarios**:

1. **Given** an input directory containing JSON files where some violate scope/schema rules and some do not, **When** the operator runs the application with that directory as the first argument, **Then** the console lists exactly those invalid files with `SCOPE:` tags (and does not list valid-only files).
2. **Given** an input directory where every JSON file is valid under built-in rules, **When** the operator runs the application, **Then** the console lists no `PARSE:` or `SCOPE:` problem lines for those files.
3. **Given** an input directory with thousands of JSON files and an arbitrary subset invalid, **When** the operator runs the application, **Then** every invalid file is listed once and no valid-only file is listed.

---

### User Story 2 - Complete a full directory scan in one run (Priority: P2)

An operator starts the application with the input directory as the first program argument (no interactive prompts). The application prints a brief start message, scans all applicable JSON files in that directory in a single run, prints tagged problem lines as issues are found, prints a brief finish message (optionally with totals), and exits so the operator can review the printed results.

**Why this priority**: One-shot, non-interactive execution is required for batch analysis of large directories.

**Independent Test**: Run the application with a directory argument and no prompts; verify it processes the full set and exits.

**Acceptance Scenarios**:

1. **Given** a readable input directory with many JSON files passed as the first argument, **When** the operator starts the application, **Then** the application shows brief start and finish progress, scans all applicable JSON files in that directory, and exits with success status when finished.
2. **Given** a readable input directory as the first argument, **When** the operator starts the application, **Then** the application does not ask for interactive input to choose files or directory.

---

### User Story 3 - Survive individual bad files during a large scan (Priority: P3)

While scanning thousands of files, some files may be unreadable or not valid JSON. The operator still receives a useful report for the rest of the directory rather than a hard stop on the first bad file.

**Why this priority**: Large datasets often include malformed files; partial progress must remain useful.

**Independent Test**: Include at least one unreadable or non-JSON-as-content `.json` file among valid and scope-invalid files; confirm the run continues and still reports scope invalidations from other files.

**Acceptance Scenarios**:

1. **Given** a directory that mixes well-formed JSON (valid and scope-invalid) with files that cannot be read as JSON, **When** the application runs, **Then** it continues scanning remaining files and still reports files with scope/schema violations using `SCOPE:`.
2. **Given** a file that cannot be read as JSON, **When** the application encounters it, **Then** the operator sees a `PARSE:` report line with that file name, distinct from `SCOPE:` lines and from clean valid files.
3. **Given** a file that parses as JSON but has missing required properties, extra properties, or a non-object root, **When** the application validates it, **Then** the operator sees a `SCOPE:` report line (not `PARSE:`).

---

### Edge Cases

- Empty input directory: application completes successfully, may still show brief start/finish progress, and prints no PARSE/SCOPE problem lines; exit status success (zero).
- Directory contains non-JSON files: they are ignored (not inspected, not reported).
- File contains multiple rule violations: the file name is reported once with a single tag (`PARSE:` or `SCOPE:`).
- Known required field missing: `SCOPE:` report.
- Unexpected extra fields present: `SCOPE:` report (closed schema; extras are invalid).
- Empty `operator_notes` (`""`): `SCOPE:` report.
- Wrong JSON types on required fields (e.g. string where number expected): `PARSE:` report.
- Inactive reading not exactly numeric zero (including negatives and non-zero values): `SCOPE:` report.
- Active reading at exact min or exact max of its range: valid (ranges are inclusive).
- Active reading equal to zero: `SCOPE:` report.
- Unknown or duplicate `sensor_type` token: `SCOPE:` report.
- Root is not a single plain object (array, number, string, null, etc.): `SCOPE:` report when content is otherwise readable as JSON; otherwise `PARSE:` if not deserializable.
- Nested structures inside values are not walked for additional scope rules beyond the top-level plain object fields defined by built-in rules.
- Valid files produce no PARSE/SCOPE lines.
- Very large directories (thousands of files): the run completes without requiring the operator to restart mid-scan for normal volume.
- Missing first argument, more than one argument, or path missing/inaccessible/not a directory: the application fails clearly on the console and exits with non-zero status without a silent empty success.
- Scan completes with one or more PARSE/SCOPE report lines: exit status is still success (zero).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The application MUST run as a one-shot console program without interactive prompts for normal operation.
- **FR-002**: The application MUST obtain the input directory from the first program argument and MUST require exactly one program argument. Missing argument, more than one argument, or an unusable path MUST be an unrecoverable failure (non-zero exit).
- **FR-003**: On each run, the application MUST discover all JSON files in the input directory (non-recursive; only that directory level).
- **FR-004**: For each discovered JSON file that can be read as JSON, the application MUST expect a single plain JSON object at the root and MUST validate that object's top-level fields against the built-in closed schema and sensor reading rules (see [data-model.md](./data-model.md)).
- **FR-005**: Allowed-value / out-of-scope rules MUST be fixed rules shipped with the application. The closed property set MUST be exactly: `sensor_type`, `timestamp`, `temperature_K`, `pressure_bar`, `water_level_meters`, `voltage_supply_v`, `humidity_percent`, `operator_notes`. Reading fields are `temperature_K`, `pressure_bar`, `water_level_meters`, `voltage_supply_v`, `humidity_percent`; metadata fields are `sensor_type`, `timestamp`, `operator_notes`. Allowed `sensor_type` tokens MUST be exactly: `humidity`, `temperature`, `water`, `pressure`, `voltage` (slash-separated). Inclusive reading ranges and zero/non-zero rules MUST follow [data-model.md](./data-model.md). The same built-in checks MUST apply on every run; operators MUST NOT need an external rules file.
- **FR-012**: A JSON file MUST be treated as invalid (`SCOPE:`) if any known required field is missing.
- **FR-013**: A JSON file MUST be treated as invalid (`SCOPE:`) if any unexpected extra property is present (closed schema; extras are not ignored).
- **FR-020**: `operator_notes` MUST be present as a non-empty string; an empty string MUST be reported as `SCOPE:`.
- **FR-021**: `timestamp` MUST be present as a JSON number; no further numeric range validation is required.
- **FR-022**: If a property has a wrong JSON type for the sensor schema (for example a string where a number is required), the application MUST report `PARSE:` (deserialize / binding failure).
- **FR-014**: If the JSON root is not a single plain object (for example an array or primitive), the application MUST treat the file as invalid and print a `SCOPE:` report line with its file name when the content is JSON-readable as a non-object value; otherwise use `PARSE:` when it cannot be deserialized.
- **FR-006**: If a JSON file fails any post-parse validation (including missing/extra properties, empty `operator_notes`, non-object root, sensor_type/reading rule failures), the application MUST print a `SCOPE:` report line that includes that file's name exactly once for that run.
- **FR-007**: The application MUST NOT print PARSE/SCOPE lines for JSON files that fully satisfy the built-in rules.
- **FR-008**: The application MUST complete inspection of all discoverable JSON files in one run, regardless of how many are invalid (including zero or all). There is no retry of failed files within the same run; the application MUST continue to the next file.
- **FR-009**: If a discovered `.json` file cannot be read or parsed/deserialized as JSON (including wrong field JSON types for the schema), the application MUST print a `PARSE:` report line that includes that file name and continue with remaining files.
- **FR-010**: The application MUST exit when the scan finishes (or when the input directory cannot be used), without entering an interactive loop.
- **FR-011**: Console output MUST be sufficient for an operator to see which files were reported, whether each report is `PARSE:` or `SCOPE:`, and whether the run failed due to argument/path problems.
- **FR-015**: Report lines MUST use the exact prefixes `PARSE: ` and `SCOPE: ` (including the space after the colon) followed by the file name only (basename with extension; no directory path). Structural post-parse failures MUST use `SCOPE:` (no separate STRUCT tag).
- **FR-016**: After a completed scan of the input directory, the application MUST exit with success status (zero) even when one or more PARSE or SCOPE problems were reported.
- **FR-017**: The application MUST exit with failure status (non-zero) only for unrecoverable setup or runtime failures (for example missing argument, more than one argument, or missing/inaccessible input directory), not merely because problem files were found.
- **FR-018**: The application MUST print brief start and finish progress messages for each run and MAY include simple totals; it MUST NOT print per-file progress for every processed file.
- **FR-019**: Constitution “no CLI parameters” does not apply to this feature’s required directory argument; that exception is documented in [plan.md](./plan.md) Complexity Tracking. Challenge-flag and OpenRouter LLM runtime goals remain out of scope for this feature. Spring AI MAY be on the classpath for future use but MUST NOT be invoked by this feature.
- **FR-023**: The application MUST log to both the console and a log file (file destination configured via application logging settings).
- **FR-024**: For each `sensor_type` token, the mapped reading MUST be non-zero (numeric value ≠ 0, including `0.0`) and within its inclusive range. Every reading field not selected by `sensor_type` MUST equal numeric zero (`0` / `0.0`). A non-zero inactive reading (including negatives) is invalid (`SCOPE:`). An active reading of zero or outside range is invalid (`SCOPE:`). A non-zero inactive reading that happens to fall in another field’s range is still invalid.

### Key Entities

- **Input Directory**: Directory path supplied as the first program argument, containing the JSON files to analyze.
- **JSON File**: A `.json` file in the input directory treated as a document candidate for inspection.
- **JSON Object / Document**: A single plain JSON object at the file root whose top-level fields are checked against the closed schema and sensor rules; not an array or primitive root.
- **Out-of-Scope / Invalid Reading**: A value that violates sensor_type correspondence or fixed ranges, or a missing required field, or an extra property under the closed schema.
- **Built-in Scope Rules**: Fixed closed property set, sensor_type token map, requiredness, zero/range rules packaged with the application (detail in [data-model.md](./data-model.md)). Unexpected fields outside this catalog are invalid.
- **Corruption Report Line**: A console/log line with prefix `PARSE: ` or `SCOPE: ` plus the file basename only.
- **Invalid file**: Prefer the terms `PARSE:` / `SCOPE:` invalid over the informal word “corrupted”; “corrupted” in older notes means the same as invalid under those tags.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For a directory with a known set of invalid and clean files, 100% of scope/schema-invalid files are listed with `SCOPE:` and 0% of clean valid files are listed as problems.
- **SC-002**: An operator can obtain the full list of problem file names from a single run by passing the directory as the first program argument, with no interactive prompts.
- **SC-003**: A directory of at least 1,000 JSON files can be fully scanned in one run, and the operator receives the complete problem-file list without restarting the application.
- **SC-004**: When some files are unreadable as JSON, the run still reports all remaining scope/schema invalidations with `SCOPE:` and identifies the unreadable files with `PARSE:`.
- **SC-005**: After a successful scan, the operator can decide next steps using only the printed tagged file names (no secondary UI required).
- **SC-006**: A completed scan that finds problem files still ends with success exit status; only unrecoverable setup/runtime failures end with failure exit status.
- **SC-007**: On a completed run, the operator sees brief start and finish progress (optionally with totals) without a per-file progress line for every scanned file.

## Assumptions

- The product is a one-shot console analyzer: exactly one argument = input directory; non-interactive after start.
- Only files with a `.json` extension in the input directory (top level, not subdirectories) are inspected; other files are ignored.
- Each valid candidate file contains a single plain JSON object; inspection applies to top-level fields only.
- "Report" means `PARSE: ` / `SCOPE: ` basename lines on console and in the log file; detailed field-level diagnostics are out of scope unless later requested.
- Progress output is limited to brief start/finish messages (optional totals), not per-file chatter.
- A file with multiple violations is still reported once.
- Unreadable JSON or wrong field JSON types → `PARSE:`; post-parse schema/sensor failures (including empty `operator_notes`) → `SCOPE:`.
- Closed schema and sensor rules in [data-model.md](./data-model.md) / [research.md](./research.md) are normative for validation details referenced by this spec.
- Spring AI may be present for future use but is not invoked in this feature; challenge-flag and OpenRouter runtime goals are out of scope.
- When documents disagree, amended spec + data-model + console contract win for behavior; plan wins for stack/tooling.
