# Quickstart: Corrupt JSON File Reporter

## Prerequisites

- JDK 23 at `C:\tools\jdk-23.0.2` (`JAVA_HOME` / `PATH` pointed at this JDK)
- Maven 3.6.3+
- Feature docs: [plan.md](./plan.md), [data-model.md](./data-model.md), [contracts/console-scan.md](./contracts/console-scan.md)

## Setup

```powershell
$env:JAVA_HOME = "C:\tools\jdk-23.0.2"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
mvn -version
```

From repository root (after implementation exists):

```powershell
mvn -q -DskipTests package
```

## Validation fixtures

Create a temp directory with at least:

1. **Valid** — `sensor_type` matches non-zero in-range readings; inactive readings are `0`; exact property set from [data-model.md](./data-model.md).
2. **SCOPE invalid (type mismatch)** — e.g. `sensor_type=water` with `temperature_K=700`.
3. **SCOPE invalid (out of range)** — e.g. `sensor_type=temperature` with `temperature_K=0` or `900`.
4. **SCOPE invalid (extra property)** — known fields plus an unknown key.
5. **PARSE invalid** — truncated / non-JSON content with `.json` extension.

## Run

```powershell
mvn -q spring-boot:run "-Dspring-boot.run.arguments=C:\path\to\fixtures"
```

Or:

```powershell
java -jar target\s03e01-*.jar C:\path\to\fixtures
```

## Expected outcomes

- Start progress mentions the directory.
- Each invalid fixture appears once as `PARSE: <name>` or `SCOPE: <name>` per [contracts/console-scan.md](./contracts/console-scan.md).
- Valid fixture is not listed as PARSE/SCOPE.
- Finish progress (optional totals) appears.
- Process exit code `0` after a completed scan.
- Same problem lines appear in the configured log file (console + file logging).

## Failure checks

```powershell
java -jar target\s03e01-*.jar
# missing argument → non-zero exit, clear error

java -jar target\s03e01-*.jar C:\path\to\fixtures C:\extra
# more than one argument → non-zero exit, clear error

java -jar target\s03e01-*.jar C:\path\does-not-exist
# non-zero exit
```

## Tests

```powershell
mvn test
```

Unit tests should cover validator matrix (token sets × zero/range). Integration test should run the scanner against `src/test/resources/fixtures/sensors`.
