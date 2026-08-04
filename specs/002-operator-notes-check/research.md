# Research: Operator Notes Issue Check

## Decision: Spring AI OpenAI starter → OpenRouter gateway

- **Decision**: Add `spring-ai-starter-model-openai` (BOM already `2.0.0`). Configure OpenAI-compatible client with:
  - `spring.ai.openai.api-key=${OPENROUTER_API_KEY}`
  - `spring.ai.openai.base-url=https://openrouter.ai/api/v1` (include `/v1` for Spring AI 2.x / OpenAI SDK)
  - `spring.ai.openai.chat.options.model=${app.llm.model:nvidia/nemotron-3-ultra-550b-a55b:free}`
- **Rationale**: Constitution requires OpenRouter; Spring AI has no dedicated OpenRouter starter; OpenAI-compatible path is the supported approach in Spring AI 2.0. Model id is parametrized in application config with the operator-specified default.
- **Alternatives considered**: Direct OpenRouter HTTP without Spring AI (rejects “use Spring AI”); Anthropic/Google starters (wrong gateway); base-url without `/v1` (breaks Spring AI 2.x chat completions).

## Decision: ChatClient + annotations for LLM use

- **Decision**: Expose a `ChatClient` `@Bean` from autoconfigured `ChatClient.Builder`. Implement `OperatorNotesClassifier` as a `@Service` that builds messages via Spring AI APIs (system + user). Prefer constructor injection and stereotype annotations (`@Service`, `@Component`, `@Configuration`, `@ConfigurationProperties`).
- **Rationale**: Matches operator rule to use Spring / Spring AI annotations where possible; keeps LLM behind framework abstractions.
- **Alternatives considered**: Manual `OpenAiApi` low-level calls; non-Spring OkHttp client.

## Decision: Numbered notes collection + return `nr` list only

- **Decision**: From VALID files, build `Map<String, List<ClassifiedFile>>` keyed by exact `operator_notes`. Assign each unique key a distinct integer `nr` (1..K, stable order by first-seen while scanning or by sorted key—prefer first-seen for reproducibility with scan order). Send **one** user/system payload:

  ```json
  {
    "notes": [
      { "nr": 1, "operator_notes": "some notes" },
      { "nr": 2, "operator_notes": "some other notes" }
    ]
  }
  ```

  Instruct the model to return **only** a collection of issue-bearing `nr` integers (no note text). Resolve returned numbers back through `nr → notes → files` and move those files to INVALID / `OPERATOR:`. Ignore unknown `nr`s.
- **Rationale**: Spec clarifications + operator plan rules; minimizes output tokens; preserves dedupe optimization.
- **Alternatives considered**: Return full note strings (higher tokens, brittle matching); one LLM call per file (rejected by scale); chunked requests (rejected in clarify).

## Decision: Parametrized system prompt via PromptTemplate

- **Decision**: Store the main system prompt as a configurable string template (property and/or `classpath:prompts/operator-notes-system.st`) rendered with Spring AI `PromptTemplate` / string-template variables. Template MUST instruct the model to identify `operator_notes` that suggest any issue and MUST describe the numbered JSON collection shape under a `notes` array. User message carries the rendered JSON collection (or the template embeds `{notesJson}`).
- **Rationale**: Operator requires parametrized system prompt via string template lib; Spring AI `PromptTemplate` is the idiomatic Spring AI mechanism (StringTemplate-style placeholders).
- **Alternatives considered**: Hard-coded Java text blocks (not parametrized); FreeMarker (extra dependency without benefit).

## Decision: Model I/O logging

- **Decision**: Before each ChatClient call, log at INFO (or dedicated logger `pl.tomaszko.s03e01.notes.llm`): system prompt text, tool definitions (empty/none → log explicitly “tools: none”), user prompt / payload, and after the call the raw model response content. Use existing Logback console + file appenders (`logs/s03e01.log`).
- **Rationale**: Operator requires full model communication visibility for CTF debugging; constitution IV requires reconstructable runs.
- **Alternatives considered**: Debug-only logging (too easy to miss); omitting tools line when unused (operator asked to log tool definitions—log absence explicitly).

## Decision: Hub verification service

- **Decision**: `@Service HubVerificationService` POSTs JSON to `https://hub.ag3nts.org/verify` (URL overridable in config) via Spring `RestClient`:

  ```json
  {
    "apikey": "<HUB_API_KEY>",
    "task": "evaluation",
    "answer": {
      "recheck": ["0001", "0002", "0003"]
    }
  }
  ```

  `recheck` = bare file names **without path and without extension** for every finalized invalid file (`PARSE`, `SCOPE`, `OPERATOR`). Always call hub (including empty `recheck`). Parse response body for substring matching `{FLG:...}`. On match: log exactly `FLAG: captured` then `FLAG: {FLG:...}` (no `SUCCESS` prefix); exit 0. Otherwise: log `ERROR: ` + full hub response body; hard failure (non-zero exit). Transport/HTTP failures → `ERROR: ` + exception detail; hard failure.
- **Rationale**: Exact operator contract; aligns with clarified hub payload and flag criteria.
- **Alternatives considered**: Submit basenames with `.json` (rejected); skip hub when empty (rejected in clarify); treat HTTP 200 alone as success (rejected—must find `{FLG:...}`).

## Decision: Environment and config parameters

- **Decision**:
  | Parameter | Source |
  |-----------|--------|
  | `HUB_API_KEY` | Environment variable (required for hub step) |
  | `OPENROUTER_API_KEY` | Environment variable (required when VALID notes exist / LLM step runs; also required if Spring AI auto-config demands it at startup—prefer failing fast if missing when AI enabled) |
  | Main system prompt | Application config / template resource (parametrized) |
  | LLM model name | `app.llm.model` (default `nvidia/nemotron-3-ultra-550b-a55b:free`) wired to `spring.ai.openai.chat.options.model` |
  | `JSON_DIR` | Existing env (unchanged) |
- **Rationale**: Operator parametrization list; secrets never committed.
- **Alternatives considered**: Hard-coded keys/model (rejected); `.env` committed (rejected by constitution).

## Decision: Flag success supersedes local categorization flag

- **Decision**: Remove “local categorization ⇒ `FLAG: captured`” from the run path. Flag lines appear only after hub returns `{FLG:...}` (`FLAG: captured` then `FLAG: {FLG:...}`). Constitution Principle II already amended to v3.0.0.
- **Rationale**: Spec FR-010–FR-012 and operator plan rules.
- **Alternatives considered**: Dual success (local + hub)—ambiguous and conflicts with hard-failure rule.

## Decision: Extend scan collections without rewriting schema validation

- **Decision**: Keep existing PARSE/SCOPE validation and reporting. Change `ScanSummary` / scanner to retain lists of valid `ClassifiedFile` (basename + `SensorReading`) and invalid stems/basenames by category so the notes pass and hub can consume them. Add `OPERATOR:` to `InvalidFileReporter`.
- **Rationale**: Spec FR-001; YAGNI—reuse 001 validation.
- **Alternatives considered**: Re-scan disk after LLM (wasteful); re-validate notes on invalid files (out of scope).

## Decision: Logging categories and hub stem format

- **Decision**: Console/file problem lines keep basename **with** extension for `PARSE:` / `SCOPE:` / `OPERATOR:` (unchanged contract for local observability). Hub `recheck` entries strip the `.json` extension (and any path). Example: file `0001.json` → log `OPERATOR: 0001.json`, hub entry `0001`.
- **Rationale**: Preserves existing report contract while matching hub example payload.
- **Alternatives considered**: Change local logs to bare stems (breaks 001 consistency).
