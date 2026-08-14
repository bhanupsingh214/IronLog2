# IronLog — Architecture & Data / Identity Model

**Documentation version:** v3.1
**Status:** CURRENT WORKING REFERENCE
**Rule:** verify source signatures before implementation.

## Architectural layers

Observed backup/recovery layers:
- UI: Profile/Settings screens.
- ViewModel: `ProfileViewModel`.
- Backup models: `data/local/backup/BackupModels.kt` and related current models.
- Repositories: backup/restore repositories.
- Services: export/import services.
- Database: Room entities, DAOs, and `AppDatabase`.
- Cloud layer: account/authorization and Google Drive services introduced by PR4.4.

This document describes responsibilities, not immutable method signatures.

## Data hierarchy

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

The backup model currently includes workout settings such as default rest timer, auto-start timer, haptic feedback, and sound alert.

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
commit
```

Any failure must roll back. Foreign-key enforcement remains active.

## Historical snapshot principle

Workout history is not simply a live view of today's exercise library. Snapshot fields preserve historical context and should remain stable unless a deliberate migration/feature explicitly changes that contract.

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
