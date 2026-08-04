# Implementation Plan: Corrupt JSON File Reporter

**Branch**: `001-corrupt-json-report` | **Date**: 2026-08-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-corrupt-json-report/spec.md` plus plan-time domain and stack directives from the operator.

## Summary

One-shot Spring Boot console application (`pl.tomaszko:s03e01`) that takes an input directory as the first program argument, scans top-level `*.json` sensor reading files, validates each against fixed sensor-type and reading-range rules, and reports invalid files with tagged console/file log lines (`PARSE:` vs `SCOPE:`). Spring AI is on the classpath for future use but is not invoked in this feature. Maven build targets JDK 23 (`C:\tools\jdk-23.0.2`).

## Technical Context

**Language/Version**: Java 23 (JDK at `C:\tools\jdk-23.0.2`)

**Primary Dependencies**: Spring Boot 4.1.0, Spring AI 2.0.0 (BOM; no LLM calls in this feature), Jackson (via Spring Boot), SLF4J/Logback (console + file)

**Storage**: N/A (read-only local JSON files)

**Testing**: JUnit 5 + Spring Boot Test (`spring-boot-starter-test`)

**Target Platform**: Windows/Linux console JVM process

**Project Type**: Maven single-module Spring Boot console application (`spring-boot-starter`; no web server required for this feature)

**Performance Goals**: Fully scan ≥1,000 JSON files in one run with brief start/finish progress only

**Constraints**: Directory path from argv[0] (first program parameter); no interactive prompts; exit 0 after completed scan even if invalid files found; exit non-zero only for unrecoverable setup failures; log to console and file

**Scale/Scope**: Thousands of sensor JSON files; any subset may be invalid

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Constitution expectation | Plan stance |
|------|--------------------------|-------------|
| I. One-Shot Console Execution | No CLI parameters | **Justified violation**: operator requires directory as first program parameter. Still one-shot, non-interactive. |
| II. Flag Discovery Goal | Capture challenge flag | **Deferred / out of feature scope**: this feature validates sensor JSON; flag/CTF flow is not part of this plan. |
| III. OpenRouter LLM Integration | LLM via OpenRouter now | **Justified deferral**: Spring AI dependency required for future functionalities; **no LLM integration in this feature**. OpenRouter wiring is out of scope here. |
| IV. Observable Progress | Human-readable console progress | **Pass**: start/finish (+ optional totals), tagged invalid-file lines; also file logging. |
| V. Simplicity | Smallest workable design | **Pass with note**: Spring Boot + Spring AI BOM adds framework weight for future AI work; no multi-command CLI, no web UI, no daemon. |
| Runtime: zero argv | Env/config only | **Justified violation**: argv directory (see Complexity Tracking). |
| Scope: no CLI parsing | Forbidden | **Justified violation**: single required path argument only. |

**Gate result**: Proceed with documented justifications. Recommend amending constitution in a later `/speckit-constitution` pass to match argv directory input, sensor-validation product goal, and deferred LLM usage.

## Project Structure

### Documentation (this feature)

```text
specs/001-corrupt-json-report/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md              # created by /speckit-tasks (not this command)
```

### Source Code (repository root)

```text
pom.xml
src/main/java/pl/tomaszko/s03e01/
├── S03e01Application.java
├── config/
│   └── AppProperties.java          # optional; primary input remains argv
├── runner/
│   └── SensorScanRunner.java       # ApplicationRunner / CommandLineRunner
├── scan/
│   ├── JsonFileScanner.java
│   └── ScanSummary.java
├── model/
│   └── SensorReading.java          # Jackson DTO for exact property set
├── validation/
│   ├── SensorType.java
│   ├── ReadingField.java
│   ├── ReadingRanges.java
│   └── SensorReadingValidator.java
└── report/
    └── InvalidFileReporter.java    # PARSE:/SCOPE: lines via logger
src/main/resources/
├── application.properties
└── logback-spring.xml              # or logging.* in application.properties
src/test/java/pl/tomaszko/s03e01/
├── validation/
│   └── SensorReadingValidatorTest.java
├── scan/
│   └── JsonFileScannerTest.java
└── runner/
    └── SensorScanRunnerIT.java
src/test/resources/fixtures/
└── sensors/                        # valid / invalid sample JSON files
```

**Structure Decision**: Single Maven module at repository root with package `pl.tomaszko.s03e01`, layered by scan / model / validation / report. No frontend or multi-module split.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| First program parameter for input directory | Operator directive for this exercise run | Env-only path conflicts with stated run mode |
| Spring Boot application | Required stack; annotation-driven lifecycle | Plain `main` lacks agreed Spring/Spring AI baseline |
| Spring AI on classpath without LLM calls | Required for future functionalities | Adding AI later would force larger retrofit; BOM now is cheaper |
| Extra JSON properties mark file invalid | Operator domain rule | Spec clarification “ignore extras” superseded by plan input |
| Constitution flag/OpenRouter goals unused | Current feature is sensor file validation | Implementing CTF/OpenRouter now expands scope beyond stated goal |
