# IronLog — Phase 5B Implementation Specification

**Status:** DRAFT / PENDING PROJECT OWNER APPROVAL
**Implementation authority:** NOT ACTIVE
**Phase:** 5B — Profile Foundation & Body Progress
**Predecessors:** Phase 5A — Progress & History Presentation (COMPLETE / VERIFIED)

## 1. Purpose

Phase 5B establishes the first durable personal-profile and body-progress foundation for IronLog. It introduces user-owned body data as persistent local data while preserving the existing local-first architecture, current one-local-dataset-per-installation model, backup/restore portability, and deterministic/no-LLM analytics.

This document is a specification draft. It does not authorize implementation until the Project Owner approves it and the active implementation specification is explicitly locked.

## 2. Product scope

### 2.1 Profile foundation

The Profile experience should become a coherent personal hub while remaining focused and not becoming a mandatory registration form.

Phase 5B owns the following personal fields:
- sex: optional;
- date of birth: optional but supported because age is relevant to correct BMI interpretation;
- height: optional until supplied by the user.

Existing Google account display name/email remain account identity and should not be duplicated into a custom IronLog identity field in this phase.

### 2.2 Body Progress

Phase 5B includes:
- dated body-weight history;
- latest/current weight derived from the latest valid weight entry;
- weight change over supported periods;
- weight trend presentation;
- BMI as a deterministic local derived metric when sufficient inputs exist;
- age-appropriate BMI interpretation;
- dated waist measurement history;
- latest waist measurement;
- sex-aware waist reference presentation where the selected evidence standard requires it.

### 2.3 Units

Height:
- preferred user-facing input/display: feet + inches;
- metric alternative: centimeters;
- canonical internal representation: centimeters.

Weight:
- preferred user-facing input/display: kilograms;
- alternative: pounds;
- canonical internal representation: kilograms.

Waist:
- preferred user-facing input/display: centimeters;
- any alternative unit support must use canonical centimeters internally.

Changing display/input units must not alter the physical quantity represented by stored data.

## 3. Age and sex rules

### 3.1 Date of birth

Store date of birth, not a mutable age value. Age is derived locally from the current date and DOB when needed.

Do not store redundant calculated age in Room.

DOB remains optional. Users must be able to use IronLog normally without supplying it.

### 3.2 Sex

Sex is optional. The final implementation must use a clearly documented representation and must not conflate biological sex with gender identity.

Sex must only affect a calculation/reference when the selected evidence source explicitly requires it. Workout logging, workout history, PRs, volume calculations, and general app operation must remain sex-independent.

### 3.3 BMI interpretation

BMI calculation is:

`BMI = weightKg / (heightMeters * heightMeters)`

For adults, the implementation must use the explicitly approved Indian/Asian-Indian reference scheme documented with source/version in the final locked spec. The current planning baseline is:
- `<18.0`: underweight;
- `18.0–22.9`: normal;
- `23.0–24.9`: overweight;
- `>=25.0`: obesity.

For users below the adult age boundary, adult BMI cutoffs MUST NOT be applied. The implementation must use a separately verified BMI-for-age interpretation path with an authoritative pediatric reference and documented age/sex requirements, or explicitly defer pediatric interpretation if the selected evidence and product scope cannot support it safely.

The exact adult/pediatric source documents, effective date/version, age boundary, rounding policy, and handling of missing DOB/sex must be recorded before implementation approval.

BMI is a screening/derived metric, not a diagnosis. IronLog must not generate medical treatment recommendations from BMI.

## 4. Data model direction

The current database is Room schema version 21. Phase 5B requires a new schema version and migration.

The implementation must model weight as historical data independent of workout sessions. Weight must not be a mutable field attached to `WorkoutSession`.

The recommended persistence shape is:

### Personal profile

A single local profile record containing only the personal attributes needed by the approved Phase 5B scope, such as:
- sex;
- dateOfBirth;
- heightCm.

The exact table/entity name is implementation-level detail but must be stable and documented in the schema ledger.

### Body weight history

A dedicated table/entity containing at minimum:
- stable local ID;
- recorded timestamp/date;
- canonical weightKg.

The latest weight shown in Profile must be derived from this history rather than duplicated as a separate currentWeight field.

### Waist history

A dedicated dated measurement table/entity containing at minimum:
- stable local ID;
- recorded timestamp/date;
- canonical circumferenceCm.

Do not require a user to record waist whenever they record weight.

The exact schema, nullability, indexes, uniqueness constraints, timestamp precision, and DAO contracts must be finalized against the existing architecture before implementation.

## 5. Database migration

The migration target is the next schema version after the current Room v21.

The implementation must:
- add only the approved Phase 5B tables/columns;
- preserve all existing user data;
- provide a deterministic Room migration;
- include migration coverage from the current supported schema;
- verify fresh-install behavior;
- verify upgrade behavior with pre-existing workout/library/program/history/PR data;
- update the schema/migration ledger;
- avoid unrelated schema changes.

No destructive migration is permitted.

## 6. Account/data ownership boundary

Phase 5B does NOT introduce multi-account Room ownership.

IronLog continues to use the current model of one local dataset per app installation. Google account state remains the cloud identity/authorization layer.

Do not add accountId foreign keys to existing workout/profile tables as part of Phase 5B.

Do not attempt to retrofit multi-user local database architecture during this phase.

If the user signs into a different Google account, existing local-dataset behavior must remain unchanged unless a separate future architecture phase authorizes a change.

## 7. Backup and restore contract

Body/profile data is user-owned portable data and must participate in `.ironlog` backup/restore.

The current backup contract predates Phase 5B and does not contain body/profile data. The implementation must deliberately extend the payload rather than relying on Room data being implicitly portable.

### Backup must include
- approved profile fields;
- body-weight history;
- waist history.

### Restore must
- recreate profile data;
- recreate all body-weight records;
- recreate all waist records;
- preserve existing workout/library/program/history/PR data;
- remain transactional with the existing restore architecture.

### Backward compatibility

A pre-Phase-5B backup with no body/profile section must remain restorable. Missing Phase 5B data should resolve to an empty/unset body profile rather than causing restore failure.

The exact backup versioning strategy must be documented in the schema/backup ledger before implementation. Do not silently change the backup contract without recording the compatibility rule.

## 8. Profile UX

The Profile screen should evolve into a personal hub with focused sections, not a large mandatory form.

### About You
- account identity summary using existing Google account information;
- sex;
- date of birth;
- height.

### Body & Progress
- latest weight;
- BMI when computable;
- BMI reference category when computable;
- latest waist;
- weight history/trend entry point;
- measurement history entry point.

### Account & Data
- preserve existing backup/restore and Google Drive functionality.

### Existing settings
- preserve existing workout/app settings without duplicating them unnecessarily.

The final UI must retain the current working account/backup/restore flows.

## 9. Empty and incomplete states

The feature must behave correctly when:
- no profile exists;
- height is missing;
- weight history is empty;
- DOB is missing;
- sex is missing;
- waist history is empty;
- height exists but no weight exists;
- weight exists but height is missing;
- DOB/sex requirements for a selected BMI interpretation are missing.

Do not fabricate BMI or classifications from incomplete inputs.

The UI should clearly indicate what information is needed to calculate a derived metric.

## 10. Deterministic/local computation

All Phase 5B calculations must be local and deterministic.

No LLM call is permitted for:
- BMI calculation;
- age calculation;
- unit conversion;
- weight change;
- trend aggregation;
- waist aggregation;
- classification lookup based on the approved reference tables.

No network dependency should be required for normal Body Progress display.

## 11. Data validation

The implementation must validate physical measurements before persistence.

The exact acceptable ranges and precision must be justified and documented in the final locked spec. The implementation must reject clearly invalid values rather than silently persisting nonsense.

Conversions must avoid avoidable floating-point display drift and must use a single canonical representation.

## 12. Testing requirements

### Unit tests

Cover:
- height conversion ft/in ↔ cm;
- weight conversion kg ↔ lb;
- BMI calculation;
- adult BMI classification;
- age derivation from DOB;
- BMI-for-age decision path and missing-input behavior;
- weight change calculations;
- latest-weight selection;
- waist selection/reference behavior;
- invalid-input validation.

### Room tests

Cover:
- profile persistence;
- weight insert/update/delete/read;
- waist insert/update/delete/read;
- latest measurement queries;
- migration from current supported schema;
- preservation of existing data through migration.

### Backup/restore tests

Cover:
- new backup includes Phase 5B data;
- new backup restores Phase 5B data;
- old backup without Phase 5B data restores successfully;
- restore preserves existing workout data;
- failed restore does not leave partial Phase 5B data;
- account/cloud backup flow remains functional.

### UI/manual regression

Verify:
- Profile opens normally;
- existing Google account information still displays;
- backup/restore remains accessible and functional;
- profile can be incomplete without breaking workout logging;
- weight entry and history work;
- BMI appears only when appropriate inputs exist;
- unit switching preserves values;
- empty states are understandable;
- existing Progress and History functionality remains intact.

## 13. Non-goals

Phase 5B does NOT include:
- goals/goal tracking;
- achievements/ranks;
- AI coach;
- nutrition logging;
- calorie tracking;
- BMR/TDEE;
- body-fat percentage estimation;
- medical diagnosis;
- treatment recommendations;
- medication/clinical data;
- social sharing;
- multi-account local Room ownership;
- extensive measurement catalogs beyond the explicitly approved initial scope.

## 14. Acceptance criteria

Phase 5B is complete only when all of the following are true:

1. Approved profile fields persist locally.
2. Weight history persists independently of workouts.
3. Waist history persists independently of workouts.
4. Height is represented canonically and displayed in the user's selected units.
5. Weight is represented canonically and displayed in the user's selected units.
6. DOB is stored and age is derived locally rather than persisted as a mutable value.
7. BMI is deterministic and does not use an LLM or network service.
8. Adult BMI classification uses the explicitly approved Indian/Asian-Indian source.
9. Non-adult BMI is not evaluated with adult cutoffs; the approved pediatric path is implemented or explicitly excluded by the locked spec.
10. Missing inputs do not produce fabricated metrics.
11. Room migration preserves all existing data.
12. New `.ironlog` backups contain approved Phase 5B data.
13. Pre-Phase-5B backups remain restorable.
14. Restore is transactional and preserves existing workout data.
15. Existing account, Drive, backup, Progress, History, and workout flows regress-free.
16. Required tests pass.
17. Documentation/schema/migration ledgers are updated as part of the implementation closeout.

## 15. Implementation workflow gate

Before Gemini implementation begins:
1. Project Owner approves this draft.
2. Exact BMI adult/pediatric references and age boundaries are recorded.
3. Exact Room entities/columns/indexes are locked.
4. Migration plan is locked.
5. Backup versioning/compatibility plan is locked.
6. Acceptance tests are locked.
7. `11_ACTIVE_PR_SPEC.md` is updated to authorize only Phase 5B.
8. Gemini receives the implementation prompt with explicit scope and non-goals.

Until those gates are satisfied, this document remains planning-only and no implementation work is authorized.
