# Contract: Console Sensor Scan (env-driven)

## Invocation

```text
set JSON_DIR=C:\path\to\fixtures
java -jar s03e01-*.jar
```

PowerShell:

```powershell
$env:JSON_DIR = "C:\path\to\fixtures"
java -jar target\s03e01-*.jar
```

| Input | Required | Description |
|-------|----------|-------------|
| Env `JSON_DIR` | yes | Absolute or relative path to a readable directory containing top-level `*.json` files |
| CLI args | none | Argument count MUST be 0; any argument → non-zero exit |

## Exit codes

| Code | When |
|------|------|
| `0` | ≥1 `.json` file discovered, all categorized, `FLAG: captured` logged (PARSE/SCOPE may also appear) |
| `non-zero` | Unrecoverable failure: missing/empty/unusable `JSON_DIR`, any CLI args, zero `.json` files discovered, or fatal startup error |

## Console / log report lines

| Prefix | Condition |
|--------|-----------|
| `PARSE: ` | Cannot bind file as sensor object (malformed JSON, wrong field types, non-object root, etc.) |
| `SCOPE: ` | Bound but fails closed-schema / notes / sensor_type / reading rules |
| `FLAG: captured` | ≥1 `.json` discovered and every such file categorized |

Format:

```text
PARSE: <file-name>
SCOPE: <file-name>
FLAG: captured
```

`<file-name>` is basename only. Valid files produce no PARSE/SCOPE line.

## Progress messages

| Phase | Requirement |
|-------|-------------|
| Start | Brief message including `JSON_DIR` path |
| Finish | Brief finish; MAY include totals |
| Per-file | MUST NOT emit progress for every file |

## Non-goals

- No HTTP API
- No interactive prompts
- No LLM / OpenRouter / Spring AI runtime calls in this feature
- No recursive subdirectory scan
- No CLI directory argument

## Schema reference

See [data-model.md](../data-model.md).

## Example

```text
Scanning directory: C:\fixtures
SCOPE: bad-temp.json
Scan finished. scanned=2 valid=1 parseInvalid=0 scopeInvalid=1
FLAG: captured
```

Exit code: `0`
