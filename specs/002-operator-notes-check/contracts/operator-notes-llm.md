# Contract: Operator Notes LLM Classification

## Transport

- Framework: Spring AI `ChatClient`
- Gateway: OpenRouter via OpenAI-compatible Spring AI starter
- Auth: `OPENROUTER_API_KEY` (required at startup)
- Model: configurable application property (default MAY be `nvidia/nemotron-3-ultra-550b-a55b:free`)
- System prompt: configurable string template (application config / template resource)
- Calls: **at most one** chat completion per application run (skipped if VALID is empty)
- Retries: none (single attempt)

## System prompt (parametrized)

- Loaded from application configuration / template resource via Spring AI `PromptTemplate` (string-template placeholders).
- MUST instruct the model to identify `operator_notes` values that suggest **any** type of issue with readings (no Spec-mandated taxonomy).
- MUST describe input as a numbered JSON collection under `notes`, e.g.:

```json
{
  "notes": [
    { "nr": 1, "operator_notes": "some notes" },
    { "nr": 2, "operator_notes": "some other notes" }
  ]
}
```

- MUST instruct the model to return **only** the collection of `nr` integers whose notes suggest issues (no `operator_notes` text in the output).

## User / data message

JSON object with the `notes` array built from unique VALID `operator_notes` (see [data-model.md](../data-model.md)).

## Expected model output

A collection of integers (JSON array preferred), e.g. `[1, 3]`. Parser MUST accept a reasonable JSON array of numbers; non-parsable output ⇒ LLM hard failure (`ERROR: ` + detail).

## Application of results

| Returned `nr` | Action |
|---------------|--------|
| Known | Move all files in that notes group VALID → OPERATOR; log `OPERATOR: <basename>` each |
| Unknown | Ignore |
| Empty | No moves |

## Model communication logging (required)

For each call, log clearly to **console and file**:

1. System prompt (fully rendered)
2. Tool definitions (or explicit `tools: none`)
3. User prompt / notes JSON payload
4. Raw model response content

## Failure

Any ChatClient/API/parse failure ⇒ log `ERROR: ` with detail and abort run with non-zero exit (do not call hub after pretending notes are clean). No automatic retry.
