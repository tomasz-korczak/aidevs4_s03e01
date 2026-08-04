# Data Model: Operator Notes Issue Check

Extends [001 data-model](../001-corrupt-json-report/data-model.md) for sensor schema. This document adds post-structural notes classification, hub submission, and flag-token entities.

## Entity: SensorReading

Unchanged closed schema from feature 001 (required fields, ranges, `sensor_type` catalog, non-empty `operator_notes`, etc.).

**Identity**: File basename within `JSON_DIR` (e.g. `0001.json`).

## Entity: ClassifiedFile

In-memory record produced by structural scan.

| Field | Type | Notes |
|-------|------|-------|
| `basename` | string | File name with extension, no directory |
| `stem` | string | Basename without `.json` extension (hub identity) |
| `category` | enum | `VALID`, `PARSE`, `SCOPE`, `OPERATOR` |
| `reading` | SensorReading? | Present when bind succeeded (VALID / SCOPE / later OPERATOR); absent on PARSE |

## Entity: FileCategory

| Value | Meaning | Log prefix |
|-------|---------|------------|
| `VALID` | Passed PARSE/SCOPE; not moved by notes check | (none) |
| `PARSE` | Bind/deserialize failure | `PARSE: ` |
| `SCOPE` | Post-bind structural/sensor failure | `SCOPE: ` |
| `OPERATOR` | Moved from VALID because notes `nr` flagged by model | `OPERATOR: ` |

**Transitions**:

```text
(discovered .json)
    → PARSE | SCOPE | VALID   (structural pass)
VALID → OPERATOR              (when assigned nr returned by model)
VALID → VALID                 (nr not returned)
PARSE/SCOPE → (terminal)      (notes check does not re-tag)
```

## Entity: OperatorNotesMap

| Field | Type | Notes |
|-------|------|-------|
| entries | map | key = exact `operator_notes` string (whitespace-sensitive) |
| per entry: `nr` | int | Distinct positive integer for model exchange |
| per entry: `files` | list\<ClassifiedFile\> | VALID files sharing that notes string |

**Population**: VALID collection only. Empty VALID ⇒ no map / no LLM call.

**Numbering**: Assign `nr` sequentially starting at `1` in first-seen order of unique notes while iterating VALID files.

## Entity: NotesClassificationRequest

Payload described to / sent with the model (user message JSON body):

```json
{
  "notes": [
    { "nr": 1, "operator_notes": "some notes" },
    { "nr": 2, "operator_notes": "some other notes" }
  ]
}
```

| Field | Rules |
|-------|-------|
| `notes` | Array; one object per unique map key |
| `notes[].nr` | Integer identity |
| `notes[].operator_notes` | Exact map key string |

## Entity: NotesClassificationResult

| Field | Type | Notes |
|-------|------|-------|
| `issueNrs` | set/list\<int\> | Numbers returned by the model |

**Validation rules**:
- Unknown `nr` → ignore
- Duplicates → treat as single move set
- Empty → no OPERATOR moves

## Entity: HubVerifyRequest

| Field | Type | Notes |
|-------|------|-------|
| `apikey` | string | From `HUB_API_KEY` |
| `task` | string | Constant `evaluation` |
| `answer.recheck` | string[] | Bare stems of all invalid files (PARSE+SCOPE+OPERATOR); may be empty; order don’t-care |

## Entity: HubVerifyOutcome

| Field | Type | Notes |
|-------|------|-------|
| `success` | boolean | `true` iff response body contains `{FLG:...}` substring |
| `flagToken` | string? | Extracted `{FLG:...}` when present |
| `rawBody` | string | Full response body for ERROR logging |

**Flag pattern**: Substring `{FLG:` … `}` (non-greedy inner content).

## Entity: RunOutcome

| Field | Type | Notes |
|-------|------|-------|
| `exitCode` | 0 / non-zero | 0 only when hub success (flag token found) |
| `flagCaptured` | boolean | Mirrors hub success |

## Validation rules (feature-specific)

1. Structural rules: inherit 001.
2. Notes check runs only on VALID after structural pass.
3. OPERATOR logging uses basename **with** extension.
4. Hub stems omit extension.
5. LLM or hub failure without flag ⇒ hard failure (no silent success).
