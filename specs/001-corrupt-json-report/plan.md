# Implementation Plan: Corrupt JSON File Reporter

**Branch**: `001-corrupt-json-report` | **Date**: 2026-08-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-corrupt-json-report/spec.md` aligned to constitution v2.0.0.

## Summary

One-shot Spring Boot console application (`pl.tomaszko:s03e01`) that reads the input directory from environment variable `JSON_DIR` (zero CLI args), categorizes top-level `*.json` sensor files with fixed schema/sensor rules, reports `PARSE:` / `SCOPE:` lines, and on successful full categorization of ≥1 file logs `FLAG: captured` and exits 0. Spring AI is on the classpath (AI ready) but is not invoked. Maven build targets JDK 23 (`C:\tools\jdk-23.0.2`).

## Technical Context

**Language/Version**: Java 23 (JDK at `C:\tools\jdk-23.0.2`)

**Primary Dependencies**: Spring Boot 4.1.0, Spring AI 2.0.0 (BOM; no LLM calls in this feature), Jackson (via Spring Boot), SLF4J/Logback (console + file)

**Storage**: N/A (read-only local JSON files)

**Testing**: JUnit 5 + Spring Boot Test (`spring-boot-starter-test`)

**Target Platform**: Windows/Linux console JVM process

**Project Type**: Maven single-module Spring Boot console application (`spring-boot-starter`; no web server required)

**Performance Goals**: Fully categorize ≥1,000 JSON files in one run with brief start/finish progress only

**Constraints**: `JSON_DIR` env only; zero CLI args; exit 0 + `FLAG: captured` after categorizing ≥1 `.json` file; non-zero if `JSON_DIR` bad, any argv, or zero `.json` files; log to console and file

**Scale/Scope**: Thousands of sensor JSON files; any subset may be invalid

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Constitution expectation | Plan stance |
|------|--------------------------|-------------|
| I. One-Shot Console Execution | No CLI params; dir via `JSON_DIR` | **Pass** |
| II. Flag Discovery Goal | Flag when all JSON files categorized | **Pass** — `FLAG: captured` after ≥1 file fully categorized |
| III. OpenRouter LLM (AI ready, when required) | AI ready; call only when required | **Pass** — Spring AI on classpath; no calls this feature |
| IV. Observable Progress | Human-readable progress | **Pass** |
| V. Simplicity | Smallest workable design | **Pass** |
| Runtime | `JSON_DIR`; zero argv | **Pass** |
| Scope | No CLI dir parsing | **Pass** |

**Gate result**: Pass against constitution v2.0.0.

## Project Structure

### Documentation (this feature)

```text
specs/001-corrupt-json-report/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md
```

### Source Code (repository root)

```text
pom.xml
src/main/java/pl/tomaszko/s03e01/
├── S03e01Application.java
├── config/
│   └── JsonDirProperties.java      # reads JSON_DIR
├── runner/
│   └── SensorScanRunner.java
├── scan/
│   ├── JsonFileScanner.java
│   └── ScanSummary.java
├── model/
│   └── SensorReading.java
├── validation/
│   ├── SensorType.java
│   ├── ReadingField.java
│   ├── ReadingRanges.java
│   └── SensorReadingValidator.java
└── report/
    └── InvalidFileReporter.java    # PARSE:/SCOPE:/FLAG:
src/main/resources/
├── application.properties
└── logback-spring.xml
src/test/java/pl/tomaszko/s03e01/
├── validation/
│   └── SensorReadingValidatorTest.java
├── scan/
│   └── JsonFileScannerTest.java
└── runner/
    └── SensorScanRunnerIT.java
src/test/resources/fixtures/
└── sensors/
```

**Structure Decision**: Single Maven module at repository root; package `pl.tomaszko.s03e01`.

## Complexity Tracking

> No constitution violations. Optional notes only:

| Note | Why |
|------|-----|
| Spring Boot + Spring AI BOM | Required stack / AI readiness without LLM calls this phase |
| Closed schema (reject extras) | Domain rule from operator |
