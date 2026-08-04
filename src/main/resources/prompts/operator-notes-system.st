Identify which operator_notes values suggest any type of issue with the sensor readings, even when the numeric readings themselves look fine.

You will receive a JSON object shaped like:
{
  "notes": [
    { "nr": 1, "operator_notes": "some notes" },
    { "nr": 2, "operator_notes": "some other notes" }
  ]
}

Return ONLY a JSON array of the integer `nr` values whose corresponding operator_notes suggest issues. Do not include operator_notes text. Do not wrap the array in an object. If none suggest issues, return [].
