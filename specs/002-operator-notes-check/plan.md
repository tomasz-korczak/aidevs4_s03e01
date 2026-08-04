# Implementation Plan: Operator Notes Issue Check

**Branch**: `002-operator-notes-check` | **Date**: 2026-08-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/002-operator-notes-check/spec.md` plus operator plan rules (Spring AI / OpenRouter, hub `/verify`, numbered notes payload, parametrized prompts/keys/model, console+file logging including model I/O).

## Summary

Extend the existing one-shot Spring Boot console app so that after PARSE/SCOPE structural classification it (1) groups VALID files by exact `operator_notes`, (2) classifies unique notes via Spring AI → OpenRouter in one request using a numbered `{ notes: [{ nr, operator_notes }] }` collection, (3) moves flagged `nr`s to INVALID as `OPERATOR:`, (4) POSTs bare invalid stems to `https://hub.ag3nts.org/verify`, and (5) treats success only when the hub body contains `{FLG:...}` (log exactly `FLAG: captured` then `FLAG: {FLG:...}`; no `SUCCESS` prefix). Failures of LLM or hub without a flag token log `ERROR: ` and hard-fail.

## Technical Context

**Language/Version**: Java 23 (JDK at `C:\tools\jdk-23.0.2`)

**Primary Dependencies**: Spring Boot 4.1.0; Spring AI 2.0.0 (`spring-ai-starter-model-openai` pointed at OpenRouter); Spring Web `RestClient` for hub; Jackson; SLF4J/Logback (console + file); Spring AI `PromptTemplate` (string-template style) for the system prompt

**Storage**: N/A (read-only local JSON files under `JSON_DIR`)

**Testing**: JUnit 5 + Spring Boot Test; mock `ChatClient` / hub HTTP for unit tests; fixtures under `src/test/resources/fixtures`

**Target Platform**: Windows/Linux console JVM process

**Project Type**: Maven single-module Spring Boot console application (`spring.main.web-application-type=none`)

**Performance Goals**: Fully process ≥1,000 JSON files in one run; one LLM request for all unique valid notes; brief start/finish progress only

**Constraints**: Zero CLI args; `JSON_DIR`, `OPENROUTER_API_KEY`, `HUB_API_KEY` from environment; model id + system prompt template parametrized in application config; log console + file; log full model I/O (system prompt, tool definitions if any, user prompt, response); hub answer stems without path/extension

**Scale/Scope**: Thousands of sensor JSON files; many share identical `operator_notes`; new `OPERATOR:` category; hub task `evaluation`

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Constitution expectation (v3.0.0) | Plan stance |
|------|-----------------------------------|-------------|
| I. One-Shot Console Execution | No CLI params; dir via `JSON_DIR` | **Pass** |
| II. Flag Discovery Goal | Flag only when hub response contains `{FLG:...}` | **Pass** — amended 2026-08-04; local categorization alone is not flag capture |
| III. OpenRouter LLM (when required) | OpenRouter only; secrets from env; failures visible | **Pass** — Spring AI ChatClient → OpenRouter; `OPENROUTER_API_KEY` always required at startup (stricter than constitution “when AI required”); hard-fail on LLM errors |
| IV. Observable Progress | Console + file progress | **Pass** — PARSE/SCOPE/OPERATOR + `FLAG:` / `ERROR:` + model I/O logs |
| V. Simplicity | Smallest workable design | **Pass** — add notes classifier + hub client only |
| Runtime | `JSON_DIR`; zero argv; non-zero on unrecoverable failure | **Pass** — `OPENROUTER_API_KEY` + `HUB_API_KEY` required at startup |
| Scope | Categorization + OpenRouter when required | **Pass** |

**Gate result**: Pass against constitution v3.0.0.

### Post-design re-check

Design artifacts (`research.md`, `data-model.md`, `contracts/*`, `quickstart.md`) and Spec clarifications (pipeline checklist resolution) align with constitution v3.0.0 hub-token flag success. Feature 001 artifacts that still equate local categorization with flag capture are superseded for product success.

## Project Structure

### Documentation (this feature)

```text
specs/002-operator-notes-check/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── console-run.md
│   ├── operator-notes-llm.md
│   └── hub-verify.md
└── tasks.md                 # /speckit-tasks (not created here)
```

### Source Code (repository root)

```text
pom.xml
src/main/java/pl/tomaszko/s03e01/
├── S03e01Application.java
├── config/
│   ├── JsonDirProperties.java
│   ├── OpenRouterProperties.java      # model + base URL bindings
│   ├── HubProperties.java             # HUB_API_KEY / verify URL
│   ├── OperatorNotesPromptProperties.java  # system prompt template
│   └── AiClientConfig.java            # ChatClient bean
├── runner/
│   └── SensorScanRunner.java          # orchestrate scan → notes → hub → flag
├── scan/
│   ├── JsonFileScanner.java
│   ├── ScanSummary.java               # retain VALID/INVALID collections
│   └── ClassifiedFile.java            # basename + category + reading (valid path)
├── model/
│   └── SensorReading.java
├── validation/
│   └── … (unchanged structural rules)
├── notes/
│   ├── OperatorNotesIndexer.java      # map notes → files; assign nr
│   ├── OperatorNotesClassifier.java   # Spring AI call + parse returned nrs
│   └── NotesPromptFactory.java        # PromptTemplate render + I/O logging
├── hub/
│   └── HubVerificationService.java    # POST /verify; FLAG:/ERROR: logging
└── report/
    └── InvalidFileReporter.java       # PARSE:/SCOPE:/OPERATOR: + FLAG lines
src/main/resources/
├── application.properties             # model, prompt template, hub URL
├── prompts/
│   └── operator-notes-system.st       # parametrized system prompt (optional file)
└── logback-spring.xml
src/test/java/pl/tomaszko/s03e01/
├── notes/
├── hub/
└── runner/
src/test/resources/fixtures/
└── sensors/
```

**Structure Decision**: Keep single Maven module / package `pl.tomaszko.s03e01`; add `notes` and `hub` packages; extend scan summary to retain lists needed for reclassification and hub payload.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| (none outstanding) | Constitution v3.0.0 amended for hub `{FLG:...}` | — |
| Spring AI OpenAI starter + RestClient | Required for OpenRouter + hub HTTP | Raw JDK HTTP/LLM clients duplicate Spring AI/Boot integration and annotations |
| PromptTemplate + dedicated notes/hub packages | Clear boundaries for LLM I/O logging and verify contract | Inlining everything in `SensorScanRunner` hurts testability |
