# Contract: Hub Verify

## Endpoint

- Method: `POST`
- URL: `https://hub.ag3nts.org/verify` (overridable via config)
- Content-Type: `application/json`
- Retries: none (single attempt)

## Request body

```json
{
  "apikey": "<HUB_API_KEY>",
  "task": "evaluation",
  "answer": {
    "recheck": ["0001", "0002", "0003"]
  }
}
```

| Field | Rule |
|-------|------|
| `apikey` | Value of environment variable `HUB_API_KEY` |
| `task` | Exact string `evaluation` |
| `answer.recheck` | Array of bare file **stems**: **no path, no extension**; all finalized invalid files (PARSE + SCOPE + OPERATOR); order is **don’t-care**; **always send**, including `[]` |

### Stem derivation

| On disk | Log basename | Hub `recheck` entry |
|---------|--------------|---------------------|
| `...\0001.json` | `0001.json` | `0001` |

## Response handling

Inspect **response body text** (regardless of optimistic HTTP status interpretation):

| Body condition | Application behavior |
|----------------|----------------------|
| Contains `{FLG:...}` substring | Log `FLAG: captured` then `FLAG: {FLG:...}` (extracted token); exit `0`. No `SUCCESS` prefix. |
| No `{FLG:...}` (error or other payload) | Log `ERROR: ` + full hub response body; exit non-zero |
| Transport / client failure | Log `ERROR: ` + failure detail; exit non-zero |

Flag extraction: first substring matching `{FLG:` … `}`.

## Auth / secrets

- `HUB_API_KEY` from environment only; never commit.
- Missing/blank key at startup ⇒ `ERROR: ` + non-zero exit.

## Non-goals

- No other hub tasks in this feature
- No automatic retries
