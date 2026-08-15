# IronLog — Schema & Migration Ledger

**Documentation version:** v3.8
**As of:** 2026-08-16
**Status:** CANONICAL SCHEMA REFERENCE

## Current verified schema state

- Room database version: **22**
- `exportSchema = false`
- Explicit migrations verified from **6→7 through 21→22**
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
| 21→22 | Phase 5B profile/body-progress tables: single-row `user_profile`, dated `body_weight_history`, and dated `waist_history`; initializes the profile row |

## Phase 5B schema impact

**VERIFIED — ROOM VERSION 21→22.**

Phase 5B added durable local profile/body-progress persistence while preserving existing workout/program/history/PR/settings tables. The profile uses the existing one-local-dataset-per-installation ownership model.

Verified new persistent areas:
- `user_profile` — single profile row (`id = 1`) containing profile attributes such as sex, DOB, and height;
- `body_weight_history` — dated body-weight entries;
- `waist_history` — dated waist entries.

Latest weight/waist values are derived from historical entries rather than duplicated mutable current-value state. Height/circumference persistence uses canonical metric values.

## PR4.4 schema impact

**NONE VERIFIED.**

PR4.4 cloud authentication/Drive storage operates outside the Room schema.

## PR4.5 schema impact

**NONE VERIFIED.**

PR4.5 consumed the existing backup/restore system without a Room schema change.

## Restore/schema relationship

Restore changes are schema-sensitive even when the PR adds no migration. Backup payloads, ID remapping, entity relationships, and clear/restore ordering must be checked against the current schema.

Phase 5B extended backup/restore for the new profile/body-progress data and retained transactional restore behavior plus backward compatibility for pre-Phase-5B backups.

## Change rule

Any schema change requires:
1. entity/DAO review;
2. migration design;
3. migration verification;
4. backup/restore compatibility review;
5. regression coverage;
6. ledger update after verification.

Never invent missing migration history.
