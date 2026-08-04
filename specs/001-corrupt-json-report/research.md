# Research: Corrupt JSON File Reporter

## Decision: Spring Boot 4.1.0 + Java 23

- **Decision**: Use Spring Boot `4.1.0` with `java.version=23` and JDK `C:\tools\jdk-23.0.2`.
- **Rationale**: Latest stable Boot line as of plan date; supports Java 17–26, so Java 23 is in range. Maven `spring-boot-starter-parent` manages versions.
- **Alternatives considered**: Boot 3.5.x (EOL for OSS around mid-2026; weaker future Spring AI 2.x fit); Boot 4.0.x (stable but not latest).

## Decision: Spring AI 2.0.0 BOM, no LLM calls yet

- **Decision**: Import `org.springframework.ai:spring-ai-bom:2.0.0` and include a minimal Spring AI starter/module dependency so auto-configuration and annotations are available later. Do **not** configure ChatClient / OpenRouter or call any model in this feature.
- **Rationale**: Operator requires Spring AI integration readiness with latest stable AI compatible with Boot 4.1.x. Spring AI 2.0.x targets Boot 4.0/4.1.
- **Alternatives considered**: Spring AI 1.1.8 (Boot 3.5 only); omit Spring AI until needed (rejected by requirement).

## Decision: Console app via `ApplicationRunner` + non-web starter

- **Decision**: `spring-boot-starter` (no `spring-boot-starter-web` unless later needed). Implement `ApplicationRunner` (or `CommandLineRunner`) that reads `args[0]` as directory, scans, reports, then `SpringApplication.exit` / return so the JVM exits.
- **Rationale**: Matches one-shot console lifecycle; uses Spring annotations (`@SpringBootApplication`, `@Component`, `@Service`) as requested.
- **Alternatives considered**: Web MVC controller (unnecessary); plain Java main without Spring (violates stack requirement).

## Decision: Directory from first program argument

- **Decision**: Require exactly one meaningful argument: input directory path. Missing/invalid path → log error, non-zero exit. Prefer ignoring extra args or treating only the first as directory.
- **Rationale**: Explicit operator plan input. Supersedes spec FR-001/FR-002 and constitution “no argv” for this feature (documented in plan Complexity Tracking).
- **Alternatives considered**: Environment variable only (rejected by operator).

## Decision: Exact sensor JSON schema and validation semantics

- **Decision**: Each file must deserialize to a single plain object with **exactly** these properties (no more, no less):

  | Property | Role |
  |----------|------|
  | `sensor_type` | Slash-separated capability tokens |
  | `timestamp` | Metadata (not a reading) |
  | `operator_notes` | Metadata (not a reading) |
  | `temperature_K` | Reading |
  | `pressure_bar` | Reading |
  | `water_level_meters` | Reading |
  | `voltage_supply_v` | Reading |
  | `humidity_percent` | Reading |

- **Sensor type tokens → expected non-zero readings**:

  | Token | Reading field |
  |-------|---------------|
  | `temperature` | `temperature_K` |
  | `pressure` | `pressure_bar` |
  | `water` | `water_level_meters` |
  | `voltage` | `voltage_supply_v` |
  | `humidity` | `humidity_percent` |

  Tokens are split on `/` with whitespace trimmed per segment. Unknown tokens → invalid (`SCOPE`). Empty `sensor_type` → invalid. Duplicate tokens → invalid. Allowed tokens (confirmed): `humidity`, `temperature`, `water`, `pressure`, `voltage`.

- **Reading rules**:
  - For each token in `sensor_type`, the mapped reading MUST be **non-zero** (≠ 0 / ≠ 0.0) and within its fixed **inclusive** range.
  - Every reading field **not** selected by `sensor_type` MUST equal **0** (numeric zero). Negatives are invalid.
  - Fixed ranges (inclusive):
    - `temperature_K`: 553–873
    - `pressure_bar`: 60–160
    - `water_level_meters`: 5.0–15.0
    - `voltage_supply_v`: 229.0–231.0
    - `humidity_percent`: 40.0–80.0
  - `operator_notes` MUST be a non-empty string; `timestamp` is any JSON number (no range).
  - Wrong JSON field types → `PARSE:`.

- **Invalid if any of**:
  1. JSON cannot be parsed / wrong types for schema → `PARSE:`
  2. Missing any required property, unknown/extra property, or empty `operator_notes` → `SCOPE:`
  3. Reading present for a type not declared in `sensor_type` (non-zero when must be zero) → `SCOPE:`
  4. Declared reading is zero or outside range → `SCOPE:`

- **Rationale**: Matches operator examples and sample payload. `pressure` and `voltage` tokens confirmed by operator (Q1:B).
- **Alternatives considered**: Ignore extra properties (earlier clarification; superseded); only three sensor tokens (rejected; Q1:B).

## Decision: Report tags and logging

- **Decision**: Emit `PARSE: <basename>` for unreadable/non-bindable content (including wrong JSON types); `SCOPE: <basename>` for all post-bind validation failures (structure, empty notes, type mismatch, out-of-range). Use SLF4J logger configured for **console and file** appenders. Brief start/finish (+ totals) at INFO; problem lines at WARN (or INFO with stable prefixes). One report line per invalid file.
- **Rationale**: Preserves PARSE vs SCOPE distinction; basename-only reporting confirmed (Q2:A); wrong types → PARSE (Q3:A); empty notes → SCOPE (Q4:B).
- **Alternatives considered**: Third `STRUCT:` tag; path-qualified names; stdout only.

## Decision: Maven coordinates

- **Decision**: `groupId=pl.tomaszko`, `artifactId=s03e01`, packaging jar with Spring Boot repackage plugin.
- **Rationale**: Operator directive.
- **Alternatives considered**: None.

## Decision: Spec clarification overrides for this plan

- **Decision**: Plan input overrides these prior clarification outcomes:
  - Extra properties → **invalid** (was: ignore).
  - Input directory → **first CLI argument** (was: env/config, no argv).
- **Rationale**: Later operator plan instructions are authoritative for implementation planning.
- **Alternatives considered**: Keep old clarifications (would contradict explicit plan input).
