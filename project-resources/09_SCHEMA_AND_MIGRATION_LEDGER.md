# IronLog — Schema & Migration Ledger

**Documentation version:** v3.1
**As of:** 2026-08-14
**Status:** CANONICAL SCHEMA REFERENCE

## Current verified schema state

- Room database version: **21**
- `exportSchema = false`
- Explicit migrations verified from **6→7 through 20→21**
- Pre-v6 migration history is intentionally undocumented because it has not been verified from the current source.

## Migration ledger

| Migration | Observed purpose |
|---|---|
| 6→7 | workout-session history tables and FK/index structure |
| 7→8 | session metadata/completed exercise IDs and set type |
| 8→9 | session duration |
| 9→10 | personal records |
| 10→11 | current-exercise/background/timer-progress fields |
| 11→12 | session-exercise status/notes/rest timer |
| 12→13 | exercise rest/timer fields and `workout_settings` default row |
| 13→14 | historical exercise name/muscle-group snapshots |
| 14→15 | exercise library and identity indexes |
| 15→16 | exercise-library FK/rebuild/backfill |
| 16→17 | session identity/prescription/status structure |
| 17→18 | session-exercise library identity |
| 18→19 | identity repair |
| 19→20 | Personal Record composite identity migration |
| 20→21 | session-exercise library/template identity index |

Exact SQL and current migration behavior must be rechecked in `AppDatabase.kt` before any migration-dependent implementation.

## PR4.4 schema impact

**NONE VERIFIED.**

PR4.4 cloud authentication/Drive storage operates outside the Room schema.

## PR4.5 expected schema impact

**NONE PLANNED / TBD BY SOURCE INSPECTION.**

PR4.5 is intended to consume the existing backup/restore system and should not require a Room schema change. This must be verified against current source before implementation.

If a schema change becomes necessary, stop and reclassify it as a material scope change.

## Restore/schema relationship

Restore changes are schema-sensitive even when the PR adds no migration. Backup payloads, ID remapping, entity relationships, and clear/restore ordering must be checked against the current schema.

## Change rule

Any schema change requires:
1. entity/DAO review;
2. migration design;
3. migration verification;
4. backup/restore compatibility review;
5. regression coverage;
6. ledger update after verification.

Never invent missing migration history.
