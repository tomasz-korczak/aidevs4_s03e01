# Contract: Console Sensor Scan CLI

## Invocation

```text
java -jar s03e01-*.jar <input-directory>
```

Maven / IDE equivalent: pass `<input-directory>` as the first application argument.

| Argument | Required | Description |
|----------|----------|-------------|
| `input-directory` | yes (exactly one argument) | Absolute or relative path to a readable directory containing top-level `*.json` files |

Additional arguments: not allowed. If argument count is not exactly one, the application MUST fail with a clear error and non-zero exit.

## Exit codes

| Code | When |
|------|------|
| `0` | Scan completed (including zero files, or any number of PARSE/SCOPE reports) |
| `non-zero` | Unrecoverable failure (missing argument, more than one argument, path not a directory, not readable, fatal startup error) |

## Console / log report lines

Stable prefixes (exact spelling):

| Prefix | Condition |
|--------|-----------|
| `PARSE: ` | File is `.json` but content cannot be deserialized/bound as a single sensor object (malformed JSON, wrong field JSON types, etc.) |
| `SCOPE: ` | File bound to schema types but fails closed-schema, non-empty `operator_notes`, sensor_type, or reading zero/range rules |

Format:

```text
PARSE: <file-name>
SCOPE: <file-name>
```

`<file-name>` is the file **basename** only (with extension), not a relative or absolute path.

Valid files produce **no** PARSE/SCOPE line.

## Progress messages

| Phase | Requirement |
|-------|-------------|
| Start | Brief message that scan began, including input directory |
| Finish | Brief message that scan finished; MAY include totals (scanned / parse / scope / valid) |
| Per-file | MUST NOT emit progress for every file |

Problem lines MAY appear between start and finish.

## Non-goals (this contract)

- No HTTP API
- No interactive prompts
- No LLM / Spring AI runtime calls
- No recursive subdirectory scan

## Schema reference

See [data-model.md](../data-model.md) for property set, ranges, and sensor_type token mapping.

## Example

Given `sensors/` with `ok.json` (valid) and `bad-temp.json` (temperature out of range):

```text
Scanning directory: sensors
SCOPE: bad-temp.json
Scan finished. scanned=2 valid=1 parseInvalid=0 scopeInvalid=1
```

Exit code: `0`
