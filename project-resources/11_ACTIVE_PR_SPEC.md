# IronLog — Active PR Specification

**Documentation version:** v3.5  
**As of:** 2026-08-15  
**Status:** PHASE 5A PLANNING / IMPLEMENTATION NOT YET AUTHORIZED  
**Current PR:** Phase 5A — Progress & History Presentation  
**Implementation authorization:** PENDING FINAL SPEC APPROVAL

## 1. Current authorization state

The Project Owner has explicitly approved **Phase 5A — Progress & History Presentation** as the next product objective.

This approval authorizes planning and repository inspection for Phase 5A. It does **not** yet authorize source-code implementation. Implementation begins only after the scope, non-goals, risks, acceptance criteria, and verification plan below are reviewed and explicitly approved.

## 2. Objective

Improve how IronLog presents the workout data it already records so the user can understand progress over time without changing the underlying workout/history data contract unnecessarily.

Phase 5A candidate areas from the roadmap are:
- Body Progress;
- fitness timeline;
- monthly/yearly recap.

The implementation must first inspect the current repository and data model to determine which of these can be delivered from existing persisted data without unnecessary schema changes.

## 3. Initial scope boundary

### In scope for investigation and design
- Current workout-history presentation and navigation;
- existing persisted session/set/history data;
- existing personal-record/analytics data already present in the repository;
- reusable aggregation/query boundaries for progress presentation;
- timeline presentation of completed workout history;
- progress/recap presentation using data already available to the app;
- deterministic, local computation of metrics where possible.

### Explicit non-goals
- No AI/LLM dependency for ordinary progress calculations or history presentation;
- no cloud/backend analytics service;
- no new recurring infrastructure cost;
- no workout logging redesign;
- no backup/restore redesign;
- no Google Drive feature work;
- no social/sharing system;
- no goals/achievements system from Phase 5B;
- no AI coach from Phase 5C;
- no schema migration unless repository inspection proves an existing-data requirement cannot reasonably be met otherwise and the Owner separately approves it.

## 4. Engineering constraints

1. Prefer existing Room data and deterministic application-side aggregation.
2. Preserve historical workout snapshots and established identity semantics.
3. Do not rewrite restore internals merely to support presentation.
4. Do not infer new data semantics from UI labels; inspect entities, DAOs, repositories, ViewModels, and tests first.
5. Keep progress calculations explainable and testable without an LLM.
6. Preserve existing Programs, History, PR, workout logging, and backup/restore behavior.
7. Follow the project's evidence hierarchy: repository code/schema/tests → Git/GitHub history → canonical resources → conversation → inference.

## 5. Required repository audit before implementation

The implementation agent must inspect and report the actual current signatures and data flow for:
- Room entities and DAOs for `WorkoutSession`, `SessionExercise`, `SessionSet`, and relevant PR/analytics records;
- existing History UI/ViewModels/repositories;
- existing progress/analytics calculations;
- existing date/time and duration handling;
- current navigation structure;
- current test coverage for history, PRs, analytics, and workout completion;
- current schema/migrations before proposing any persistence change.

No invented method names, fields, or schema assumptions are permitted.

## 6. Risks and mitigations

| Risk | Mitigation |
|---|---|
| Historical snapshots are accidentally recomputed from current library data | Read established snapshot fields and preserve their contract |
| Progress metrics disagree with existing workout/PR semantics | Reuse verified repository/domain calculations where available; add deterministic tests |
| Large history causes slow UI queries | Prefer database-side aggregation or bounded queries after measuring current access patterns |
| A presentation feature silently requires new persistence | Prove the requirement from source/schema first; obtain separate approval for migrations |
| Test data is damaged during connected tests | Preserve the established `leaveApksInstalledAfterRun=true` safeguard |
| AI is introduced unnecessarily | All ordinary metrics must be deterministic/local unless a future separate decision authorizes AI |

## 7. Acceptance criteria — draft pending implementation audit

A final implementation acceptance list must be locked after the repository audit, but the feature must satisfy these principles:

- Existing workout history remains intact and unchanged.
- Progress/history views use real persisted IronLog data rather than fabricated/sample values.
- Calculations are deterministic and reproducible.
- Historical dates, durations, sets, reps, and weights are presented consistently with existing data semantics.
- Existing workout logging, completion, Programs, History, PRs, and backup/restore regressions remain green.
- No unnecessary schema migration is introduced.
- No LLM/API/backend dependency is introduced for normal progress analysis.
- Manual emulator verification confirms the new presentation against known workout history.

## 8. Verification plan — draft

1. Complete repository/data-flow audit.
2. Report proposed exact files and implementation boundary.
3. Lock final acceptance scenarios in this document.
4. Run relevant JVM/unit tests.
5. Run connected Android tests with the established data-safety safeguard enabled.
6. Verify existing populated history remains intact.
7. Manually verify the new progress/history presentation on the emulator using known data.
8. Run build and repository hygiene checks.
9. Review the final diff before PR creation.

## 9. Implementation authorization boundary

**STOP HERE until the Project Owner explicitly approves the final scope produced from the repository audit.**

Phase 5A is selected and planning is authorized. Source-code modification is not yet authorized by this document.

## 10. Collaboration workflow

```text
ChatGPT: repository audit + scope/risk/acceptance specification
→ Project Owner: approve final implementation scope
→ Gemini/implementation agent: implement approved source changes + tests
→ Project Owner: runtime/manual verification
→ ChatGPT: review diff/evidence + maintain canonical Project Resources
→ Project Owner: PR merge/delete lifecycle
```

Gemini should not independently rewrite the canonical Project Resource stack or perform documentation closeout when ChatGPT has GitHub access.
