# Sensor fixtures

Sample JSON files for local validation live in this directory.

Set `JSON_DIR` to this folder and run the packaged jar with no CLI arguments.
Also set `OPENROUTER_API_KEY` and `HUB_API_KEY`.

See `specs/002-operator-notes-check/quickstart.md`.

## Files

| File | Structural | Notes intent |
|------|------------|--------------|
| `ok.json` | VALID | Clean / healthy notes (should stay VALID unless model flags) |
| `ok-issue-notes.json` | VALID | Issue-suggesting `operator_notes` (candidate for `OPERATOR:`) |
| `ok-issue-notes-dup.json` | VALID | Same issue notes as `ok-issue-notes.json` (dedupe / shared `nr`) |
| `bad-parse.json` | PARSE | Malformed JSON |
| `bad-root.json` | PARSE | Non-object root |
| `bad-type.json` | SCOPE | Invalid `sensor_type` |
| `bad-range.json` | SCOPE | Out-of-range reading |

Hub `recheck` uses bare stems (`ok`, `bad-parse`, …), not basenames with `.json`.
