# Validation & Consistency Checklist: Corrupt JSON File Reporter

**Purpose**: Formal gate for requirements quality on sensor validation rules and alignment between spec, plan, research, data-model, and console contract—before `/speckit-tasks`
**Created**: 2026-08-04
**Feature**: [spec.md](../spec.md) · [plan.md](../plan.md) · [data-model.md](../data-model.md) · [contracts/console-scan.md](../contracts/console-scan.md)

**Note**: This checklist tests whether requirements are complete, clear, consistent, and measurable—not whether an implementation works.

## Requirement Completeness

- [x] CHK001 Are all eight sample JSON properties named as the closed required set in normative requirements (not only in research examples)? [Completeness, Gap, Spec §FR-005] — Resolved: FR-005 lists exact property set
- [x] CHK002 Are reading fields explicitly distinguished from metadata fields (`timestamp`, `operator_notes`) in the spec or an authoritative linked artifact? [Completeness, data-model.md] — Resolved: FR-005 + data-model
- [x] CHK003 Is the full `sensor_type` token catalog (including whether `pressure` / `voltage` are allowed) stated as a normative requirement? [Completeness, Ambiguity, research.md] — Resolved: Q1:B → five tokens in FR-005
- [x] CHK004 Are inclusive min/max ranges documented for every reading field as acceptance-level requirements? [Completeness, data-model.md §ReadingField] — Resolved: data-model + FR-024
- [x] CHK005 Are inactive-reading “must equal zero” rules specified for every non-selected reading field? [Completeness, research.md] — Resolved: FR-024
- [x] CHK006 Are active-reading rules specified as both non-zero and in-range (not only one of the two)? [Completeness, research.md] — Resolved: FR-024
- [x] CHK007 Are invalidity criteria enumerated for type-mismatch, out-of-range, missing property, extra property, and unreadable JSON? [Completeness, research.md] — Resolved: FR-006/009/012/013/022/024 + edge cases
- [x] CHK008 Are non-JSON files in the input directory addressed in requirements (ignore vs report)? [Completeness, Spec §Edge Cases] — Resolved: ignored
- [x] CHK009 Are empty-directory and missing-argument behaviors both specified with exit-status expectations? [Completeness, Spec §FR-016/017, contracts/console-scan.md] — Resolved
- [x] CHK010 Are console + file logging destinations specified as requirements (not only plan tech notes)? [Completeness, Gap, plan.md] — Resolved: FR-023

## Requirement Clarity

- [x] CHK011 Is “non-zero” defined for integer vs floating readings (e.g. `0` vs `0.0`)? [Clarity, Ambiguity, data-model.md] — Resolved: ≠ 0 / ≠ 0.0 in FR-024 + data-model
- [x] CHK012 Is range inclusivity at boundaries (min and max) stated unambiguously for each reading? [Clarity, data-model.md §ReadingField] — Resolved: inclusive + edge case
- [x] CHK013 Is `sensor_type` split behavior defined for whitespace, trailing `/`, and empty segments? [Clarity, data-model.md §SensorTypeToken] — Resolved: trim, discard empty, case-sensitive
- [x] CHK014 Is duplicate token handling in `sensor_type` specified as invalid or allowed? [Clarity, data-model.md] — Resolved: duplicates → SCOPE
- [x] CHK015 Is the exact report-line prefix spelling (`PARSE: ` / `SCOPE: `) normative in requirements/contract? [Clarity, Spec §FR-015, contracts/console-scan.md] — Resolved
- [x] CHK016 Is “file name” vs path-qualified name clarified for report lines? [Clarity, contracts/console-scan.md] — Resolved: Q2:A basename only
- [x] CHK017 Is “first program parameter” defined as the sole required argument with behavior for missing/extra args? [Clarity, Ambiguity, contracts/console-scan.md] — Resolved: exactly one arg; extras → non-zero exit
- [x] CHK018 Is classification of “wrong type but still in range” (e.g. water sensor with non-zero temperature) explicitly required as invalid? [Clarity, research.md] — Resolved: FR-024

## Requirement Consistency

- [x] CHK019 Do input-directory requirements agree across Spec §FR-001/002 (no argv) and plan/contract (argv[0])? [Conflict, Spec §FR-001, plan.md] — Resolved: FR-001/002 rewritten for argv[0]
- [x] CHK020 Do extra-property rules agree across Spec §FR-013 (ignore) and plan/data-model (reject)? [Conflict, Spec §FR-013, research.md] — Resolved: FR-013 closed schema / extras → SCOPE
- [x] CHK021 Do Spec Clarifications session answers match current plan overrides, or is the override explicitly marked as superseding the spec? [Consistency, Spec §Clarifications, research.md] — Resolved: plan-overrides session added
- [x] CHK022 Are Spec §SC-002 (“no command-line arguments”) and contract invocation examples consistent? [Conflict, Spec §SC-002, contracts/console-scan.md] — Resolved: SC-002 uses first program argument
- [x] CHK023 Is folding structural corruption into `SCOPE:` (vs a third tag) consistent across Spec §FR-014/015, research, and contract? [Consistency, Spec §FR-015, research.md] — Resolved: FR-015 + US3 scenario 3 + override clarification
- [x] CHK024 Are “configured input directory” phrases in the spec updated or qualified where plan uses argv? [Consistency, Spec §User Story 2] — Resolved: stories/entities use argv directory
- [x] CHK025 Do Assumptions in the spec still claim “extras ignored” / “env config” while plan contradicts them? [Conflict, Spec §Assumptions] — Resolved: Assumptions rewritten
- [x] CHK026 Is constitution “no CLI parameters” reconciled with plan Complexity Tracking as an intentional, documented exception? [Consistency, plan.md §Constitution Check] — Resolved: FR-019 + plan Complexity Tracking

## Acceptance Criteria Quality

- [x] CHK027 Can a reviewer objectively decide pass/fail for a fixture using only documented ranges and token maps? [Measurability, data-model.md] — Resolved: FR-005 + data-model normative
- [x] CHK028 Are success criteria updated to reflect argv-based runs where they still require “no command-line arguments”? [Measurability, Conflict, Spec §SC-002] — Resolved with SC-002 rewrite
- [x] CHK029 Are PARSE vs SCOPE acceptance scenarios still accurate given closed-schema failures map to `SCOPE:`? [Acceptance Criteria, Spec §User Story 3] — Resolved: US3 scenario 3 added
- [x] CHK030 Is “exactly once per invalid file” measurable for multi-rule failures in the same file? [Measurability, Spec §FR-006] — Resolved: FR-006 + edge cases
- [x] CHK031 Are exit-code criteria measurable for completed scan with findings vs missing directory? [Measurability, Spec §FR-016/017, contracts/console-scan.md] — Resolved

## Scenario Coverage

- [x] CHK032 Are primary happy-path requirements defined for a fully valid multi-token `sensor_type`? [Coverage, Spec §User Story 1] — Resolved: US1 + FR-005/024
- [x] CHK033 Are alternate requirements defined for single-token types (`water`, `temperature`, `humidity`)? [Coverage, research.md] — Resolved: same rules apply to each catalog token
- [x] CHK034 Are exception requirements defined for unreadable JSON continuing the scan? [Coverage, Spec §User Story 3] — Resolved
- [x] CHK035 Are requirements defined for active reading at exact min and exact max boundaries? [Coverage, Edge Case, Gap] — Resolved: inclusive + edge case
- [x] CHK036 Are requirements defined for active reading equal to zero (must fail)? [Coverage, research.md] — Resolved: FR-024 + edge case
- [x] CHK037 Are requirements defined for inactive reading slightly above zero (must fail)? [Coverage, research.md] — Resolved: FR-024 + edge case
- [x] CHK038 Are requirements defined for unknown `sensor_type` tokens? [Coverage, data-model.md] — Resolved: SCOPE + edge case
- [x] CHK039 Are recovery/partial-run requirements intentionally limited to “continue to next file” (no retry), and is that stated? [Coverage, Spec §FR-008] — Resolved: FR-008

## Edge Case Coverage

- [x] CHK040 Are requirements stated for root JSON array or primitive (not plain object)? [Edge Case, Spec §FR-014] — Resolved
- [x] CHK041 Are requirements stated for wrong JSON types on fields (string where number expected)? [Edge Case, Gap, Ambiguity] — Resolved: Q3:A → PARSE (FR-022)
- [x] CHK042 Are requirements stated for negative inactive readings (not zero)? [Edge Case, Gap] — Resolved: FR-024 + edge case
- [x] CHK043 Are requirements stated for empty `operator_notes` string (allowed vs invalid)? [Edge Case, Gap, Ambiguity] — Resolved: Q4:B → SCOPE (FR-020)
- [x] CHK044 Are requirements stated for `timestamp` validation (any number vs constrained)? [Edge Case, data-model.md] — Resolved: Q5:A → any number (FR-021)
- [x] CHK045 Are edge cases for thousands of files and “any number invalid” reflected as requirements without vague adjectives only? [Edge Case, Spec §SC-003] — Resolved: SC-003 (≥1,000) + FR-008

## Dependencies & Assumptions

- [x] CHK046 Is the assumption that plan-time operator input supersedes clarifications documented as the source of truth for tasks? [Assumption, research.md] — Resolved: Spec Clarifications plan-overrides session
- [x] CHK047 Are deferred items (LLM/OpenRouter/flag capture) explicitly out of scope for this feature’s requirements? [Dependency, plan.md §Constitution Check] — Resolved: FR-019
- [x] CHK048 Is Spring AI “present but unused” stated as a requirement boundary so it is not mistaken for functional scope? [Assumption, contracts/console-scan.md §Non-goals] — Resolved: FR-019 + contract non-goals
- [x] CHK049 Is the inferred `pressure`/`voltage` token mapping flagged as confirmed vs provisional relative to operator examples? [Assumption, Ambiguity, research.md] — Resolved: Q1:B confirmed

## Ambiguities & Conflicts

- [x] CHK050 Is there a single authoritative artifact (updated spec vs plan+data-model) designated when documents disagree? [Ambiguity, Conflict] — Resolved: precedence in Clarifications + Assumptions
- [x] CHK051 Are Spec §FR-001, §FR-002, §FR-013, and related Assumptions marked obsolete or rewritten to match argv + closed schema? [Conflict, Gap] — Resolved: requirements rewritten
- [x] CHK052 Is “corrupted” terminology aligned with `PARSE`/`SCOPE` classifications without overlapping definitions? [Clarity, Consistency, Spec §Key Entities] — Resolved: prefer PARSE/SCOPE; “corrupted” = invalid under those tags
- [x] CHK053 Are contract “additional arguments ignored vs rejected” left open, and is a single choice required before tasks? [Ambiguity, contracts/console-scan.md] — Resolved: B — reject if not exactly one argument
- [x] CHK054 Does the checklist gate require resolving CHK019–CHK025 conflicts before `/speckit-tasks`? [Traceability, Gap] — Resolved: blockers cleared 2026-08-04

## Notes

- Depth: Formal gate · Audience: Author (pre-tasks) · Focus: validation rules + spec/plan consistency
- **2026-08-04**: All 54 items passing after plan overrides, argv policy B, and Q1–Q5 answers (tokens B, basename A, wrong types PARSE A, empty notes SCOPE B, timestamp any number A).
- Gate ready for `/speckit-tasks`.
