# IronLog — Active PR Specification

**Documentation version:** v4.0  
**As of:** 2026-08-16  
**Status:** ACTIVE PR — PHASE 5C IMPLEMENTATION IN PROGRESS  
**Current objective:** Goals & Deterministic Progress Intelligence  
**Implementation authorization:** APPROVED — GitHub #44 is the active implementation PR

## 1. Authorization boundary

Phase 5C — Goals & Deterministic Progress Intelligence — is the explicitly approved objective following the completed Phase 5B Profile Foundation & Body Progress work.

This specification is the sole implementation authorization boundary for Phase 5C. Gemini must audit the current repository against this specification before implementation. Any ambiguity, schema conflict, backup-contract conflict, or requested scope expansion must stop implementation and return to ChatGPT/Project Owner.

GitHub #44 currently contains the Phase 5C implementation and remains open/draft. The implementation is not yet merged.

## 2. Objective

Build a local-first goal and deterministic progress-intelligence layer on top of the existing workout, history, progress, profile, and body-progress foundations.

IronLog must calculate ordinary goal progress, trends, consistency, and status locally from persisted application data. No LLM, AI model, backend analytics service, or network connection is required for ordinary Phase 5C functionality.

## 3. Approved scope

### 3.1 Goal foundation

Support a deliberately small initial goal system covering:
- target body weight;
- target waist circumference;
- target exercise/PR value;
- workout-frequency target.

Goals must contain only the durable fields actually required to calculate and present progress. Exact Room entities/columns are TBD until source audit and must follow the current repository architecture.

### 3.2 Deterministic progress calculations

Calculate, where sufficient data exists:
- absolute change;
- percentage change where meaningful;
- goal completion percentage;
- current vs target state;
- workout-frequency adherence;
- simple historical trend direction and rate.

Calculations must be deterministic, local, testable, and explainable from the underlying persisted data.

### 3.3 Trend/status classification

Support deterministic states such as:
- Not started;
- In progress;
- On track;
- Behind;
- Completed;
- No meaningful trend;
- Insufficient data.

Exact boundary rules for these classifications must be explicitly implemented and unit-tested. Do not invent medical or physiological predictions.

### 3.4 Goal UI

Provide a coherent user-facing flow to:
- view goals;
- create a goal;
- edit a goal;
- delete a goal;
- view current progress/status;
- handle empty and insufficient-data states.

Reuse existing IronLog UI patterns. Do not perform unrelated UI modernization in this PR.

### 3.5 Progress-intelligence presentation

Present deterministic facts such as:
- current value;
- target value;
- progress percentage;
- trend direction/rate;
- workout consistency;
- goal status.

The UI must make clear that these are calculations derived from IronLog data, not AI-generated medical or coaching advice.

### 3.6 Persistence and migration

If goals require Room persistence, implement the smallest appropriate schema extension and forward migration after source audit confirms the current version. Preserve all existing workout, history, profile, body-progress, settings, and other data.

No destructive migration is permitted.

### 3.7 Backup/restore

If persistent goal data is introduced, extend the existing `.ironlog` backup/restore contract so that:
- new backups include Phase 5C goal data;
- old backups without goal data remain restorable;
- round-trip restore preserves goals;
- restore remains transactional;
- existing workout/profile/body-progress data remains intact.

Do not redesign the backup architecture.

## 4. Deterministic/local intelligence boundary

Phase 5C intelligence means ordinary application logic such as:
- arithmetic;
- date calculations;
- rolling/period comparisons;
- trend/slope calculations;
- adherence calculations;
- goal-state rules.

It does NOT mean an LLM or generative AI model.

No Phase 5C feature may require Gemini, OpenAI, a local language model, a remote AI API, backend inference, or a network connection.

## 5. Explicit non-goals

Do NOT implement:
- AI coach;
- LLM integration;
- generative AI;
- local language model inference;
- model training/fine-tuning;
- nutrition or food logging;
- calorie tracking;
- BMR/TDEE;
- calorie prescriptions;
- medical diagnosis or treatment;
- body-fat estimation;
- social/cloud goals;
- achievements/ranks/badges;
- broad personalization;
- unrelated UI/UX modernization;
- multi-account Room redesign;
- backend/cloud analytics;
- new recurring infrastructure costs.

AI/LLM work is explicitly deferred beyond Phase 5. After Phase 5C, the planned next phase is Phase 6 UI/UX modernization/polish. AI must not be considered for implementation until the Project Owner explicitly re-authorizes an AI phase after Phase 6.

## 6. Architecture/invariants

Mandatory:
1. Preserve one local IronLog dataset per installation.
2. Reuse existing workout/history/progress/profile/body-progress repositories and models where appropriate.
3. Do not duplicate values that can be derived from existing persisted data.
4. Keep calculations deterministic and locally testable.
5. Preserve existing backup/restore transaction and compatibility behavior.
6. Preserve existing connected-test data-safety configuration.
7. Preserve existing Workout, History, Progress, Profile, Body Progress, and PR behavior.
8. Do not introduce an LLM, AI, backend, or network dependency.

## 7. Risks and mitigations

### Goal-state ambiguity
**Risk:** Users receive misleading On Track/Behind classifications.  
**Mitigation:** Lock explicit deterministic boundary rules and unit-test boundary values.

### Sparse history
**Risk:** Trend calculations become misleading with too little data.  
**Mitigation:** Return an explicit Insufficient Data / No Meaningful Trend state instead of fabricating a trend.

### Data duplication
**Risk:** Goal state or current values drift from source data.  
**Mitigation:** Derive progress from authoritative persisted records; avoid duplicate mutable current values.

### Schema/backup regression
**Risk:** New goal persistence breaks migration or portability.  
**Mitigation:** Forward migration testing, old-backup compatibility testing, round-trip testing, and transactional restore verification.

### Scope creep toward AI
**Risk:** "Intelligence" is misread as LLM/AI implementation.  
**Mitigation:** This specification explicitly defines intelligence as deterministic local application logic and explicitly defers AI beyond Phase 5.

## 8. Acceptance criteria

Phase 5C is complete only when:
- goals can be created, viewed, edited, and deleted;
- supported goal types calculate deterministic progress correctly;
- trend/status classifications are explicit, bounded, and unit-tested;
- insufficient-data cases do not fabricate progress/trends;
- persistent goal data survives the required Room migration if schema changes;
- backup/restore remains backward compatible if goal data is persisted;
- existing Workout/History/Progress/Profile/Body Progress/PR behavior remains intact;
- no LLM/AI/backend/network dependency is introduced;
- automated tests/build verification pass in the available environment;
- Project Owner performs manual emulator verification;
- ChatGPT performs final diff/evidence and Project Resource review;
- the implementation PR is reviewed/merged by the Project Owner and its branch deleted.

## 9. Verification plan

Before implementation:
- Gemini performs a read-only source audit against this locked specification.

During implementation:
- pure calculation tests;
- repository tests;
- migration tests if schema changes;
- backup/restore compatibility tests if backup contract changes;
- build and `git diff --check`.

Manual verification:
- goal creation/edit/delete;
- progress calculation from real persisted data;
- trend/status states;
- insufficient-data state;
- Profile/Body Progress regression;
- Workout/History/Progress/PR regression;
- backup/restore regression where applicable.

Current manual verification update:
- Project Owner ran the updated Phase 5C app on the emulator.
- 5C-UI-01 Progress goal-card overlap/obscuring — VERIFIED FIXED.
- 5C-UI-02 BMI readability/read-only presentation — VERIFIED FIXED.
- These findings remain recorded in the regression matrix as durable observations with closure evidence.
- This does not close the entire Phase 5C PR; remaining acceptance and verification rows must still be completed.

Do not claim instrumentation PASS unless the relevant connected tests actually execute.

## 10. Required implementation workflow

```text
Locked Phase 5C spec
→ Gemini read-only source audit
→ ChatGPT/Project Owner reconcile audit
→ implementation approval
→ Gemini implements only approved scope + tests
→ Project Owner builds/runs app + manual verification
→ ChatGPT reviews diff/evidence
→ Project Owner reviews/merges implementation PR + deletes branch
→ ChatGPT performs documentation closeout
```

Current position in workflow:

```text
Implementation PR #44
→ Project Owner emulator verification
→ two reported UI findings VERIFIED FIXED
→ remaining Phase 5C acceptance/verification
→ final ChatGPT diff/evidence review
→ Project Owner acceptance
→ merge + branch cleanup
→ documentation closeout
```

## 11. Stop conditions

Stop and return to ChatGPT/Project Owner if:
- the current repository schema conflicts with the proposed design;
- a destructive migration is required;
- backup versioning/compatibility is undefined;
- goal-state rules cannot be made deterministic and testable;
- implementation requires medical interpretation beyond approved scope;
- an AI/LLM/backend/network dependency becomes necessary;
- implementation expands into Phase 6 UI/UX modernization;
- implementation expands into achievements, nutrition, or broad personalization;
- runtime evidence contradicts the acceptance criteria.

No workaround may silently expand scope.
