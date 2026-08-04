<!--
Sync Impact Report
- Version change: (none) → 1.0.0
- Modified principles: (initial fill from template placeholders)
  - [PRINCIPLE_1_NAME] → I. One-Shot Console Execution
  - [PRINCIPLE_2_NAME] → II. Flag Discovery Goal
  - [PRINCIPLE_3_NAME] → III. OpenRouter LLM Integration
  - [PRINCIPLE_4_NAME] → IV. Observable Progress
  - [PRINCIPLE_5_NAME] → V. Simplicity
- Added sections: Runtime Constraints, Scope Boundaries
- Removed sections: none (template placeholders replaced)
- Follow-up TODOs: none
-->

# s03e01 CTF Constitution

## Core Principles

### I. One-Shot Console Execution
The application MUST be a console program that accepts no command-line
parameters. A single invocation MUST start the full capture-the-flag flow,
run its sequence of actions to completion (or failure), and exit. The app
MUST NOT require interactive prompts for normal operation after start.

Rationale: This is a one-time exercise run; configuration belongs in the
environment or code, not in argv.

### II. Flag Discovery Goal
The sole product goal is to obtain the challenge flag. After each action
(or batch of actions), the application MUST inspect results for a flag
pattern or known flag indicator and stop successfully when the flag is
found. Progress toward the flag MUST drive control flow; unrelated features
MUST NOT be added.

Rationale: Success is defined only by capturing the flag.

### III. OpenRouter LLM Integration
LLM capabilities MUST be accessed exclusively through the OpenRouter API.
API credentials MUST come from environment variables or a local env file
excluded from version control; secrets MUST NOT be committed. Failures from
the LLM or API MUST surface clearly on stderr (or equivalent console error
output) and MUST NOT be silently ignored.

Rationale: The exercise depends on LLM reasoning via a single, agreed API
gateway.

### IV. Observable Progress
The application MUST log human-readable progress of major steps and
inspection outcomes to the console (stdout for normal progress, stderr for
errors). Logs MUST be sufficient to reconstruct which actions ran and why
the run succeeded or failed.

Rationale: CTF debugging requires a clear trail of actions and results.

### V. Simplicity
Implement only what is required to run the action loop, call OpenRouter,
inspect results, and report the flag. Prefer the smallest workable design
(YAGNI). No CLI framework, no multi-command interface, and no persistent
service mode unless the challenge itself requires it.

Rationale: This is a focused exercise, not a general-purpose product.

## Runtime Constraints

- Entry point: console process with zero required CLI arguments.
- Lifecycle: one process run = one attempt to capture the flag via a series
  of actions and result inspections.
- Configuration: OpenRouter API key and any challenge URLs/tokens via
  environment (or non-committed local config), not argv.
- Exit: non-zero exit code on unrecoverable failure; zero when the flag is
  obtained (or when the run completes its defined success path).

## Scope Boundaries

In scope: orchestrating challenge actions, LLM calls via OpenRouter,
result inspection, console reporting, flag capture.

Out of scope: CLI argument parsing, interactive REPL modes, web UI,
daemon/long-running servers, multi-user features, and unrelated product
polish.

## Governance

This constitution supersedes conflicting informal practices for this
exercise. Amendments MUST update this file, bump `CONSTITUTION_VERSION`
using semantic versioning (MAJOR for incompatible principle removals or
redefinitions, MINOR for new or materially expanded principles/sections,
PATCH for clarifications), and set **Last Amended** to the change date.
Compliance reviews (spec, plan, tasks, and implementation) MUST check
alignment with the Core Principles and Runtime Constraints before treating
work as complete.

**Version**: 1.0.0 | **Ratified**: 2026-08-04 | **Last Amended**: 2026-08-04
