# Contract: Console Run (env-driven)

## Invocation

```powershell
$env:JSON_DIR = "C:\path\to\fixtures"
$env:OPENROUTER_API_KEY = "sk-or-..."
$env:HUB_API_KEY = "..."
java -jar target\s03e01-*.jar
```

| Input | Required | Description |
|-------|----------|-------------|
| Env `JSON_DIR` | yes (startup) | Readable directory of top-level `*.json` |
| Env `OPENROUTER_API_KEY` | yes (startup) | OpenRouter API key; missing/blank → hard fail |
| Env `HUB_API_KEY` | yes (startup) | Hub API key; missing/blank → hard fail |
| CLI args | none | Any argv → non-zero exit |

## Pipeline

1. Structural scan → VALID / PARSE / SCOPE (existing rules)
2. Build numbered unique notes from VALID; one Spring AI call (if VALID non-empty)
3. Move flagged `nr` files → OPERATOR
4. POST hub verify with all invalid stems (order don’t-care; may be `[]`)
5. Success only if response contains `{FLG:...}`

## Exit codes

| Code | When |
|------|------|
| `0` | Hub response contains `{FLG:...}`; logs include `FLAG: captured` and `FLAG: {FLG:...}` |
| `non-zero` | Bad/missing `JSON_DIR`, missing/blank `OPENROUTER_API_KEY` or `HUB_API_KEY`, any CLI args, zero `.json` files, LLM failure, hub failure, or hub body without `{FLG:...}` |

## Report lines (console + file)

| Line | Meaning |
|------|---------|
| `PARSE: <basename>` | Structural bind failure |
| `SCOPE: <basename>` | Structural/sensor rule failure |
| `OPERATOR: <basename>` | Notes check invalidation |
| `FLAG: captured` | Hub returned flag token (first success line) |
| `FLAG: {FLG:...}` | Extracted flag token (second success line) |
| `ERROR: <detail>` | Hub/LLM/setup failure (include hub body when applicable) |

`<basename>` includes `.json`. Hub stems do not. No `SUCCESS` prefix.

## Progress

Brief start (directory) and finish (counts including operatorInvalid). No per-file progress spam. Model I/O logged per [operator-notes-llm.md](./operator-notes-llm.md).

## Non-goals

- Interactive prompts
- Recursive directory scan
- CLI directory argument
- Web UI / long-running server
- Automatic retries for LLM or hub
