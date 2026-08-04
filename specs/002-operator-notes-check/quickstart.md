# Quickstart: Operator Notes Issue Check

Validate the end-to-end flow described in [spec.md](./spec.md) and contracts under [contracts/](./contracts/).

## Prerequisites

- JDK 23 (`C:\tools\jdk-23.0.2` or equivalent on `PATH`)
- Maven
- Env vars: `JSON_DIR`, `OPENROUTER_API_KEY`, `HUB_API_KEY`
- Built jar or `mvn spring-boot:run`

## Build

```powershell
cd C:\priv\aidevs4-cwiczenia\s03e01
mvn -q -DskipTests package
```

## Configuration check

Confirm `application.properties` (or equivalent) exposes:

- LLM model default `nvidia/nemotron-3-ultra-550b-a55b:free` (overridable)
- Parametrized system prompt template for operator-notes classification
- OpenRouter base URL `https://openrouter.ai/api/v1`
- Hub verify URL `https://hub.ag3nts.org/verify`
- Logging to console and `logs/s03e01.log`

See [research.md](./research.md) and [contracts/operator-notes-llm.md](./contracts/operator-notes-llm.md).

## Scenario A — Structural invalid only (smoke)

1. Point `JSON_DIR` at fixtures with PARSE/SCOPE invalids and no VALID files needing notes (or only clean notes).
2. Run with zero CLI args.
3. Expect: `PARSE:` / `SCOPE:` lines as applicable; LLM skipped if VALID empty; hub called with invalid stems; success **only** if hub returns `{FLG:...}`.

## Scenario B — OPERATOR reclassification

1. Fixtures: several structurally valid files sharing an issue-bearing `operator_notes` string; others with clean notes.
2. Run once.
3. Expect:
   - One model request logged (system prompt, tools: none, user notes JSON, response)
   - All files under flagged `nr` logged as `OPERATOR: <basename.json>`
   - Hub `recheck` contains bare stems for PARSE+SCOPE+OPERATOR (not VALID)
4. If hub returns flag: log `FLAG: captured` then `FLAG: {FLG:...}`, exit 0 (no `SUCCESS` prefix).
5. If hub returns error body: `ERROR: ` + response text, non-zero exit.

## Scenario C — Hub hard failure

1. Use a deliberate wrong `HUB_API_KEY` or mock/intercept to return a non-flag body.
2. Expect non-zero exit, no successful flag claim, ERROR log with hub body.

## Scenario D — Guardrails

| Condition | Expected |
|-----------|----------|
| Any CLI arg | non-zero; no hub success |
| Missing `JSON_DIR` | non-zero |
| Empty directory (0 json) | non-zero |
| Missing `OPENROUTER_API_KEY` when VALID notes exist | non-zero; clear ERROR |
| Missing `HUB_API_KEY` | non-zero; clear ERROR |

## References

- Data shapes: [data-model.md](./data-model.md)
- Console contract: [contracts/console-run.md](./contracts/console-run.md)
- LLM contract: [contracts/operator-notes-llm.md](./contracts/operator-notes-llm.md)
- Hub contract: [contracts/hub-verify.md](./contracts/hub-verify.md)
