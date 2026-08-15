# IronLog — Active PR Specification

**Documentation version:** v3.7  
**As of:** 2026-08-15  
**Status:** ACTIVE — PHASE 5B LOCKED  
**Current objective:** Phase 5B — Profile Foundation & Body Progress  
**Implementation authorization:** PENDING PROJECT OWNER APPROVAL

## 1. Authorization boundary

Phase 5B is the next implementation candidate after completion of Phase 5A and approval of the Profile Product Architecture and Phase 5B implementation specification.

This document is the implementation boundary. **No source-code implementation is authorized until the Project Owner explicitly approves this locked specification.**

Gemini may implement only the scope below after approval. Any ambiguity, schema conflict, backup-contract conflict, or requested scope expansion must stop implementation and return to ChatGPT/Project Owner for resolution.

## 2. Objective

Establish the first durable IronLog personal-profile foundation and Body Progress capability without redesigning the existing one-local-dataset-per-installation ownership model.

The feature must remain local-first and deterministic. Ordinary profile calculations and body-progress analytics must not require an LLM, backend, cloud analytics service, or network connection.

## 3. Locked scope

### 3.1 Profile foundation

Implement the minimum durable profile data required for Body Progress:
- optional sex field;
- date of birth;
- height.

Google account display name/email remain account identity sourced from the existing account/preferences architecture; do not duplicate or replace them with a new custom-name system in this PR.

The profile must remain usable when optional profile information has not been entered.

### 3.2 Height

User-facing preference:
- feet + inches as the preferred entry/display format;
- centimeters as the alternative.

Persist a canonical metric representation suitable for deterministic calculation. Unit conversion must not change the underlying physical value.

Validation must reject impossible/invalid height values and avoid silent precision loss.

### 3.3 Date of birth and age

Persist date of birth rather than mutable age.

Age must be derived locally from DOB and the current date. Do not persist a mutable age field.

DOB is collected only because it has a concrete product purpose for age-aware body metrics. Do not introduce unrelated age-based personalization.

### 3.4 Sex

Sex is optional.

The implementation must not make workout logging, history, PRs, volume calculations, or general navigation dependent on sex.

Sex-specific behavior is permitted only where the locked body-metric reference explicitly requires it.

Do not introduce a separate gender-identity system in this PR.

### 3.5 Body weight

Create durable dated body-weight history independent of workout sessions.

Required behavior:
- add a dated weight entry;
- show latest/current weight from the latest valid entry rather than maintaining a second mutable current-weight field;
- edit an entry;
- delete an entry;
- show weight history/trend for supported periods;
- handle empty state cleanly.

Preferred user-facing unit: kilograms. Pounds are an alternative display/input unit.

Persist canonical kilograms. Unit changes must not alter the stored physical measurement.

### 3.6 Waist measurement

Include an initial dated waist-measurement history capability.

Required behavior:
- add measurement;
- edit measurement;
- delete measurement;
- show latest measurement;
- show history/trend where the UI supports it;
- handle empty state cleanly.

Persist a canonical metric circumference representation. User-facing unit handling must be consistent with the established unit strategy.

Do not expand this PR into a broad catalog of chest/arm/thigh/etc. measurements unless explicitly approved as a scope change.

### 3.7 BMI

BMI must be calculated locally and deterministically from canonical weight and height.

Adult BMI and non-adult BMI interpretation must be separate. Adult cutoffs must never be applied universally to children/adolescents.

The exact authoritative adult and pediatric/BMI-for-age reference sources and age boundaries must be verified and recorded before release. Do not invent thresholds during implementation.

For the planned Indian/Asian-Indian adult reference, the current product planning direction is the commonly used Indian classification around 18.0 / 23.0 / 25.0, but the exact source/version must be recorded in the implementation evidence and regression expectations before release.

BMI must be presented as a screening/derived metric, not a diagnosis. Do not provide medical treatment recommendations, disease diagnoses, or prescriptive health advice.

### 3.8 Profile UX

Expand Profile into a coherent personal hub without forcing a large onboarding questionnaire.

Initial implemented areas may include:
- About You: sex, DOB, height;
- Body & Progress: weight, BMI, waist, history/trends;
- existing Account & Data controls;
- existing workout/app settings without duplicating or breaking their current ownership.

Goals, achievements, training-profile personalization, and AI assistance remain future scope.

### 3.9 Persistence and migration

The current Room database is version 21. Phase 5B requires a deliberate forward schema migration for the new persistent profile/body data.

The exact table/entity/column/index design must follow existing repository conventions and must not break existing entities or migrations.

Required verification includes:
- fresh install;
- upgrade from an existing v21 database containing existing workout data;
- migration correctness;
- existing workout/program/history/PR/settings data remains intact.

### 3.10 Backup/restore

Extend the existing `.ironlog` backup/restore contract to include the Phase 5B persistent profile/body-progress data.

Required portable data includes:
- profile fields introduced by this PR;
- body-weight history;
- waist history.

Old backups created before Phase 5B must remain restorable with body/profile data absent/defaulted rather than failing solely because the new fields are missing.

New backups containing Phase 5B data must restore that data correctly.

Restore failure must remain transactional and must not leave partially restored profile/body data or corrupt existing workout data.

Do not redesign the overall backup system or introduce multi-account cloud ownership in this PR.

### 3.11 Ownership boundary

Preserve the current architecture:

> one local IronLog dataset per app installation.

Do not introduce account-keyed Room ownership, multi-user local datasets, account-switching data partitioning, or a broad identity architecture in Phase 5B.

Google account identity remains the existing authentication/Drive authorization layer.

## 4. Explicit non-goals

Do NOT implement in Phase 5B:
- multi-account Room redesign;
- nutrition/food logging;
- calorie tracking;
- BMR/TDEE or calorie prescriptions;
- body-fat estimation;
- medical diagnosis;
- treatment/medication advice;
- AI/LLM health coaching or interpretation;
- social sharing;
- goals/target management;
- achievements/ranks;
- broad training-profile personalization;
- a large measurement catalog beyond the approved initial waist capability;
- replacement of existing workout/settings architecture without a direct Phase 5B need.

## 5. Data and architecture invariants

1. Body weight is historical data, not a workout-session field.
2. Latest weight is derived from the latest weight entry; do not maintain duplicate mutable current-weight state.
3. Height is a profile attribute, not a workout property.
4. DOB is stored; age is derived.
5. Canonical persistence uses metric values; UI units are presentation/input preferences.
6. Calculations are deterministic and local.
7. Existing workout data must survive migration and restore.
8. Backup/restore must remain backward compatible with pre-Phase-5B backups.
9. Existing instrumentation-test data-preservation safeguards must remain intact.
10. No LLM/network dependency may be introduced for ordinary Body Progress calculations.

## 6. Required tests

### Unit/repository/data tests
- height conversion ft/in ↔ cm;
- weight conversion kg ↔ lb;
- BMI calculation with representative values;
- age derivation from DOB around birthday boundaries;
- adult vs non-adult BMI interpretation path;
- waist reference selection where sex-specific logic applies;
- empty profile/body-data behavior;
- add/edit/delete weight;
- add/edit/delete waist;
- latest-value selection;
- trend/period aggregation;
- invalid input rejection;
- rounding/precision behavior.

### Room/migration tests
- v21 → new schema migration;
- existing workout/program/history/PR/settings data survives;
- new profile/body tables are correctly created;
- fresh-install schema matches migrated schema expectations.

### Backup/restore tests
- old backup without Phase 5B data restores successfully;
- new backup includes Phase 5B data;
- new backup restores profile/body data correctly;
- failed restore remains transactional;
- existing workout data remains correct after restore.

### UI/manual verification
Project Owner must manually verify at minimum:
- Profile navigation;
- profile empty/incomplete state;
- height ft/in entry and display;
- metric height alternative;
- weight entry and unit switching;
- weight history/trend;
- waist entry/history;
- BMI display;
- age-aware BMI behavior;
- edit/delete flows;
- backup/restore round trip;
- existing workout/history functionality remains intact.

## 7. Acceptance criteria

Phase 5B is acceptable only when all are true:

- Profile foundation works without requiring every field;
- height, DOB, and optional sex persist correctly;
- weight and waist histories persist independently of workouts;
- BMI is deterministic/local;
- adult and non-adult BMI interpretation paths are not conflated;
- exact reference sources are recorded before release;
- ft/in and metric unit flows preserve physical values;
- Room migration succeeds from the current v21 database;
- existing user data survives migration;
- old backups remain restorable;
- new backups round-trip Phase 5B data;
- restore remains transactional;
- no multi-account Room redesign is introduced;
- no LLM/backend dependency is introduced;
- automated tests pass;
- Project Owner manual verification passes;
- `git diff --check` is clean;
- no unrelated production-code changes are included.

## 8. Implementation workflow

```text
Project Owner approves this locked spec
→ Gemini audits current source against this spec before editing
→ Gemini implements only approved Phase 5B changes + tests
→ Gemini reports exact files, schema/migration, backup changes, tests and any blockers
→ Project Owner builds/runs the app and performs manual verification
→ ChatGPT reviews diff/evidence and canonical Project Resources
→ Project Owner reviews/merges implementation PR and deletes branch
→ ChatGPT performs Phase 5B documentation closeout
```

Gemini must not independently alter canonical Project Resources, change scope, rewrite this active spec, perform documentation closeout, merge PRs, or delete branches.

## 9. Stop conditions

Gemini must stop and ask for clarification if:
- the existing schema differs materially from this specification;
- backup versioning requires a choice not defined here;
- a proposed design requires account-keyed Room ownership;
- a required BMI reference cannot be verified from the approved evidence;
- a migration would require destructive data loss;
- an existing invariant conflicts with this scope;
- implementation requires adding any non-goal feature;
- a test or runtime result contradicts the acceptance criteria.

No workaround may silently expand scope.
