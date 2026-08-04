<!--
Sync Impact Report
- Version change: 1.0.0 → 2.0.0
- Modified principles:
  - I. One-Shot Console Execution → clarified: zero CLI args; input dir via JSON_DIR
  - II. Flag Discovery Goal → redefined: flag captured when all JSON files are categorized
  - III. OpenRouter LLM Integration → III. OpenRouter LLM Integration (AI ready, used when required)
  - V. Simplicity → aligned with deferred/on-demand LLM use and JSON categorization flow
- Added sections: none
- Removed sections: none
- Follow-up TODOs: Align feature spec/plan/tasks/contracts with JSON_DIR, flag=all-categorized, no argv (see Next Actions)
-->

# s03e01 CTF Constitution

## Core Principles

### I. One-Shot Console Execution
The application MUST be a console program that accepts no command-line
parameters. The JSON input directory MUST be supplied exclusively via the
`JSON_DIR` environment variable. A single invocation MUST start the full
capture-the-flag flow, run its sequence of actions to completion (or
failure), and exit. The app MUST NOT require interactive prompts for
normal operation after start.

Rationale: Configuration belongs in the environment, not in argv. One
invocation performs the complete exercise run.

### II. Flag Discovery Goal
The sole product goal is to obtain the challenge flag. At this stage the
flag MUST be considered captured when every JSON file under `JSON_DIR` has
been categorized (valid or invalid under the exercise rules). The
application MUST complete categorization of all discoverable JSON files in
the run and MUST treat successful full categorization as flag capture for
this phase. Unrelated features MUST NOT be added.

Rationale: Success is defined by capturing the flag; full file
categorization is the current capture condition.

### III. OpenRouter LLM Integration (AI ready, used when required)
The application MUST remain AI-ready: when LLM capabilities are required,
they MUST be accessed exclusively through the OpenRouter API. API
credentials MUST come from environment variables or a local env file
excluded from version control; secrets MUST NOT be committed. When an LLM
call is required, failures from the LLM or API MUST surface clearly on
stderr (or equivalent console error output) and MUST NOT be silently
ignored. Features that do not require an LLM MUST NOT invent LLM calls.

Rationale: OpenRouter is the single agreed gateway when AI is needed;
unused AI readiness MUST NOT force calls on every run.

### IV. Observable Progress
The application MUST log human-readable progress of major steps and
inspection outcomes to the console (stdout for normal progress, stderr for
errors). Logs MUST be sufficient to reconstruct which actions ran and why
the run succeeded or failed.

Rationale: CTF debugging requires a clear trail of actions and results.

### V. Simplicity
Implement only what is required to categorize JSON files under `JSON_DIR`,
report outcomes, capture the flag per Principle II, and invoke OpenRouter
only when required. Prefer the smallest workable design (YAGNI). No CLI
framework, no multi-command interface, and no persistent service mode
unless the challenge itself requires it.

Rationale: This is a focused exercise, not a general-purpose product.

## Runtime Constraints

- Entry point: console process with zero required CLI arguments.
- Input directory: `JSON_DIR` environment variable (MUST be set to a usable
  directory path for a successful run).
- Lifecycle: one process run = categorize all JSON files in `JSON_DIR` and
  evaluate flag capture per Principle II.
- Configuration: `JSON_DIR`, and when AI is required OpenRouter API key and
  any challenge URLs/tokens, via environment (or non-committed local
  config), not argv.
- Exit: non-zero exit code on unrecoverable failure (including missing or
  unusable `JSON_DIR`); zero when the flag is obtained (all JSON files
  categorized) or when the run completes its defined success path.

## Scope Boundaries

In scope: reading JSON files from `JSON_DIR`, categorization/validation,
result inspection, console reporting, flag capture when all files are
categorized, OpenRouter LLM use when required.

Out of scope: CLI argument parsing for the input directory, interactive
REPL modes, web UI, daemon/long-running servers, multi-user features, and
unrelated product polish.

## Governance

This constitution supersedes conflicting informal practices for this
exercise. Amendments MUST update this file, bump `CONSTITUTION_VERSION`
using semantic versioning (MAJOR for incompatible principle removals or
redefinitions, MINOR for new or materially expanded principles/sections,
PATCH for clarifications), and set **Last Amended** to the change date.
Compliance reviews (spec, plan, tasks, and implementation) MUST check
alignment with the Core Principles and Runtime Constraints before treating
work as complete. Feature artifacts that conflict with this constitution
MUST be amended; the constitution MUST NOT be diluted to match conflicting
specs.

**Version**: 2.0.0 | **Ratified**: 2026-08-04 | **Last Amended**: 2026-08-04
