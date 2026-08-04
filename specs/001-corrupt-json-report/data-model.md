# Data Model: Corrupt JSON File Reporter

## Entity: SensorReading

Root JSON object for one sensor capture file.

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `sensor_type` | string | yes | Slash-separated tokens from the allowed catalog |
| `timestamp` | number | yes | Metadata; any JSON number; no range check |
| `temperature_K` | number | yes | Reading |
| `pressure_bar` | number | yes | Reading |
| `water_level_meters` | number | yes | Reading |
| `voltage_supply_v` | number | yes | Reading |
| `humidity_percent` | number | yes | Reading |
| `operator_notes` | string | yes | Metadata; MUST be non-empty (length ≥ 1) |

**Identity**: File basename within the input directory (not a field on the object).

**Cardinality**: Exactly one `SensorReading` object per `.json` file.

**Closed schema**: Any additional property → invalid (`SCOPE:`). Any missing property → invalid (`SCOPE:`). Root must be a JSON object (not array/primitive).

**Wrong JSON types** (e.g. string where number required): deserialize/binding failure → `PARSE:`.

## Entity: SensorTypeToken

Allowed `sensor_type` segments (confirmed catalog):

| Token | Expected reading field |
|-------|------------------------|
| `humidity` | `humidity_percent` |
| `temperature` | `temperature_K` |
| `water` | `water_level_meters` |
| `pressure` | `pressure_bar` |
| `voltage` | `voltage_supply_v` |

**Parsing**: Split `sensor_type` on `/`. Trim whitespace on each segment. Discard empty segments after trim. Empty list after split → invalid (`SCOPE:`). Duplicate tokens → invalid (`SCOPE:`). Unknown token → invalid (`SCOPE:`). Token match is case-sensitive exact match to the table above.

## Entity: ReadingField

| Field name | Zero when inactive | Active range (inclusive) |
|------------|--------------------|--------------------------|
| `temperature_K` | numeric `== 0` | 553–873 |
| `pressure_bar` | numeric `== 0` | 60–160 |
| `water_level_meters` | numeric `== 0` | 5.0–15.0 |
| `voltage_supply_v` | numeric `== 0` | 229.0–231.0 |
| `humidity_percent` | numeric `== 0` | 40.0–80.0 |

**Active reading**: Must be non-zero (value ≠ 0, including ≠ `0.0`) **and** inside the inclusive range (min and max allowed).

**Inactive reading**: Must equal numeric zero (`0` / `0.0`). Negatives and any non-zero value are invalid even if they fall inside an active range for that field.

## Entity: FileClassification

| Status | Meaning |
|--------|---------|
| `VALID` | Parsed and passed all rules; not reported |
| `PARSE_INVALID` | Unreadable JSON, not deserializable, or wrong JSON types for schema fields |
| `SCOPE_INVALID` | Structure or sensor/reading rule failure after successful bind to schema types |

## Entity: ScanSummary

In-memory run totals (for finish progress).

| Field | Meaning |
|-------|---------|
| `filesScanned` | Count of `.json` files discovered |
| `parseInvalid` | Count of `PARSE:` reports |
| `scopeInvalid` | Count of `SCOPE:` reports |
| `valid` | Count of valid files |

## Validation Rules (normative)

1. Discover only top-level `*.json` in the directory argument (non-recursive). Non-`.json` files are ignored.
2. Attempt deserialize/bind to the closed `SensorReading` schema (fail on unknown properties / wrong types).
3. If deserialize/bind fails (including wrong types or non-object root) → `PARSE_INVALID`.
4. Else validate closed property set, non-empty `operator_notes`, sensor tokens, and reading zero/range rules → on any failure `SCOPE_INVALID`.
5. Emit at most one tagged report line per invalid file using basename only.
6. Continue until all discovered files are categorized (no retry).
7. If zero `.json` files discovered → setup failure (no flag). If ≥1 and all categorized → `FLAG: captured`.

## State Transitions

```text
[Discovered .json file]
        │
        ▼
   try parse/deserialize/bind
        │
        ├─ failure ──► PARSE_INVALID ──► report PARSE: <basename> ──► next file
        │
        └─ success ──► validate schema + sensor rules
                         │
                         ├─ failure ──► SCOPE_INVALID ──► report SCOPE: <basename> ──► next file
                         └─ success ──► VALID (silent) ──► next file
```

No persistent storage; classifications exist only for the duration of the run (logs + console).
