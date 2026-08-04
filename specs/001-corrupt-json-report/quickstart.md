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

Create a temp directory with at least one `.json` file, including:

1. **Valid** — per [data-model.md](./data-model.md).
2. **SCOPE invalid** — type mismatch and/or out of range and/or extra property.
3. **PARSE invalid** — truncated JSON, wrong field types, or non-object root.

## Run

```powershell
$env:JSON_DIR = "C:\path\to\fixtures"
mvn -q spring-boot:run
```

Or:

```powershell
$env:JSON_DIR = "C:\path\to\fixtures"
java -jar target\s03e01-*.jar
```

Do **not** pass CLI arguments.

## Expected outcomes

- Start progress mentions `JSON_DIR`.
- Invalid fixtures appear once as `PARSE: <name>` or `SCOPE: <name>`.
- Valid fixtures are not listed as PARSE/SCOPE.
- Finish progress (optional totals) appears.
- Line `FLAG: captured` appears.
- Exit code `0`.
- Same lines in the log file.

## Failure checks

```powershell
Remove-Item Env:JSON_DIR -ErrorAction SilentlyContinue
java -jar target\s03e01-*.jar
# missing JSON_DIR → non-zero, no FLAG

$env:JSON_DIR = "C:\path\to\fixtures"
java -jar target\s03e01-*.jar extra
# any CLI arg → non-zero, no FLAG

$env:JSON_DIR = "C:\path\does-not-exist"
java -jar target\s03e01-*.jar
# unusable JSON_DIR → non-zero, no FLAG

$env:JSON_DIR = "C:\path\to\empty-dir"
java -jar target\s03e01-*.jar
# zero .json files → non-zero, no FLAG
```

## Tests

```powershell
mvn test
```
