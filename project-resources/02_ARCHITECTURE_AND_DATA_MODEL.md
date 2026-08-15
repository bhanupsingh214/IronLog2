# IronLog — Architecture & Data / Identity Model

**Documentation version:** v3.8
**As of:** 2026-08-16
**Status:** CURRENT WORKING REFERENCE
**Rule:** verify source signatures before implementation.

## Architectural layers

Observed application layers:
- UI: Profile/Settings, Progress, History, Workout, and related screens.
- ViewModels: screen-specific state and user interaction orchestration.
- Data models: Room entities and backup DTOs/models.
- Repositories: workout/history, analytics, body progress, backup, and restore repositories.
- Services: export/import and Google Drive transport/authentication services.
- Database: Room entities, DAOs, migrations, and `AppDatabase`.
- Cloud layer: account/authorization and Google Drive services introduced by PR4.4.

This document describes responsibilities, not immutable method signatures.

## Ownership model

IronLog currently uses:

> one local IronLog dataset per app installation.

Google account identity is an authentication/Drive authorization boundary, not a Room dataset-partitioning key. Phase 5B preserved this model and did not introduce multi-account local ownership.

## Data hierarchy

### User profile

Phase 5B introduced a durable single-row local profile containing optional personal profile attributes such as:
- sex;
- date of birth;
- canonical metric height.

Age is derived locally from DOB and the current date; mutable age is not stored.

### Body progress

Body progress is independent of workout sessions:
- `body_weight_history` stores dated canonical-kilogram weight entries;
- `waist_history` stores dated canonical-metric circumference entries.

Latest weight/waist values are derived from the latest valid historical entry rather than duplicated mutable current-value fields.

Body metrics such as BMI are derived locally and deterministically from canonical measurements.

### Exercise library

`LibraryExerciseDto` includes identity/metadata such as:
- `id`;
- `systemKey`;
- `name`;
- `normalizedName`;
- `muscleGroup`;
- `equipment`;
- `exerciseType`.

### Program hierarchy

`Program → WorkoutDay → Exercise → Set`

Program exercises carry library/template identity and prescription/configuration data.

### Workout history hierarchy

`WorkoutSession → SessionExercise → SessionSet`

Historical session exercises preserve snapshot information such as exercise name/muscle group and can contain swap/original-exercise state. Historical snapshots must not be casually rewritten from current library values.

### Personal Records

PRs use library/template identity where applicable and may reference a session. `0L` is intentionally valid for a manual/historical PR marker; non-zero session references must resolve during restore.

### Settings

The backup model includes workout settings such as default rest timer, auto-start timer, haptic feedback, and sound alert.

## Canonical exercise identity

Restore resolution must preserve established identity rules:

1. existing old-ID → new-ID mapping;
2. `systemKey` where applicable and verified;
3. deterministic normalized-name resolution where safe;
4. create a new library record only as a genuine last resort.

Never assume normalized names are globally unique without source/schema evidence. Never resolve ambiguity with arbitrary `LIMIT 1` behavior.

## Restore ID remapping

Restore-generated IDs may differ from backup IDs. Relevant mappings can include:
- library;
- program;
- workout day;
- program exercise;
- session;
- session exercise.

References such as `currentExerciseId`, `originalExerciseId`, `completedExerciseIds`, and non-zero PR session references require correct remapping.

Phase 5B profile/body data is restored as its own persistent data set and does not participate in workout-session identity remapping.

## Transaction model

The restore contract is:

```text
validate complete incoming artifact
        ↓
begin destructive transaction
        ↓
clear/rebuild in FK-safe order
        ↓
resolve/remap identities
        ↓
restore profile/body-progress data
        ↓
commit
```

Any failure must roll back. Foreign-key enforcement remains active.

## Historical snapshot principle

Workout history is not simply a live view of today's exercise library. Snapshot fields preserve historical context and should remain stable unless a deliberate migration/feature explicitly changes that contract.

## Phase 5B calculation boundary

Ordinary profile/body calculations are local and deterministic:
- height unit conversion;
- age derivation from DOB;
- BMI calculation and adult/non-adult interpretation path.

The implementation does not require an LLM, backend analytics service, or network connection for these calculations.

## PR4.4 cloud architecture

PR4.4 adds:
- Google account authentication;
- separate Google Drive authorization;
- Drive API access;
- user-owned Drive `appDataFolder` storage;
- upload of the existing `.ironlog` artifact.

The cloud layer transports/stores the artifact; it does not redefine the backup format or restore semantics.

## PR4.5 architecture

Locked flow:

```text
current signed-in + Drive-authorized account
        ↓
Google Drive appDataFolder
        ↓
locate IronLog `.ironlog`
        ↓
controlled download/staging
        ↓
common validation/import boundary
        ↓
existing BackupPayload
        ↓
existing RestoreRepository
        ↓
Room transaction
```

The exact input abstraction for the local URI and downloaded artifact is **TBD until current source inspection**. Do not invent a signature.

## Account/authorization boundary

PR4.5 must verify that the active Drive authorization belongs to the currently signed-in Google account. Stale authorization must not be reused across account changes.

Required lifecycle checks:
- A sign-in → Drive authorization → restore;
- A sign-out → B sign-in → stale A authorization cannot authorize B restore;
- revoked/expired authorization fails safely;
- cloud restore is unavailable without valid signed-in/authorized state.

## Architecture change rule

Before modifying relationships, restore behavior, or input boundaries:
1. inspect entities;
2. inspect DAOs;
3. inspect migrations/schema;
4. inspect repository/service code;
5. inspect UI/ViewModel state flow;
6. inspect tests;
7. implement only after confirming the real signatures.
