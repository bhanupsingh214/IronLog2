# IronLog Roadmap & PR Ledger

**Documentation version:** v4.1  
**As of:** 2026-08-16

## Purpose

Canonical development timeline, roadmap, and PR ledger. Roadmap context does not authorize implementation; the active PR specification is the implementation authority.

## Phase 4 — Backup & Recovery

### PR4.1 — Backup Export
**Status: MERGED / VERIFIED**

Portable `.ironlog` export, metadata, versioning/integrity information, and local export.

### PR4.2 — Backup Restore
**Status: MERGED / VERIFIED**

Validation-before-destruction, transactional restore, relational ID remapping, identity preservation, legacy compatibility, and restore safety.

### PR4.3 — Backup Management & Recovery UX
**Status: MERGED / VERIFIED**

Picker-first import, metadata confirmation, cancel safety, restore-over-existing-data replacement, clear feedback, and preservation of PR4.2 behavior.

### PR4.4 — Google Account & Google Drive Backup
**Status: MERGED / VERIFIED**

GitHub PR #29:
- Head commit: `50e77cbcb8da0fce58aae990166097b512174b1d`
- Merge commit: `fdfdfb47d4844983d5806287d103a81f8946869e`

Verified:
- Google Sign-In;
- sign-out → sign-in;
- app restart;
- separate Drive authorization;
- Drive API access;
- `.ironlog` cloud upload;
- repeat cloud backup;
- local export/import regression;
- final build and repository checks.

Cloud download/restore was intentionally excluded from PR4.4.

### PR4.5 — Google Drive Cloud Restore
**Status: MERGED / VERIFIED**

GitHub PR #31:
- Head commit: `806f09e694699511d4c1ae11fcf11689b4b47df5`
- Merge commit: `6317c2f2c3aa12e56709d5b62cf600e8f1bca7d4`

Verified in the merged PR record:
- JVM test suite;
- clean debug build;
- connected Android tests with 5 tests passing;
- real-emulator Google Drive restore flow, including backup metadata confirmation and restore flow;
- File-based backup validation instrumentation coverage;
- no schema/migration change.

The merged PR intentionally excluded Drive browsing/history, synchronization, scheduled restore, backup-format redesign, and schema redesign.

## Phase 4 stability closeout

### GitHub #32 — Workout finish confirmation duration
**Status: MERGED / VERIFIED**

- Head commit: `8dc08ce1b426f681a9e97afc43c5fa751f0f8521`
- Merge commit: `eb3bcf34aeb656528b8354d5fb2c46cb478109e`

The fix calculates live elapsed duration for active sessions while retaining persisted duration for completed sessions. Verification recorded 7/7 connected tests, a successful debug build, and manual emulator verification.

### Connected-test data-safety mitigation
**Status: VERIFIED / CLOSED**

Development instrumentation testing was found to remove the production package after a connected test run, which could wipe the local Room database on a data-bearing emulator.

Mitigation:
```properties
android.injected.androidTest.leaveApksInstalledAfterRun=true
```

Verification:
- 7/7 connected tests passed;
- production package remained installed;
- Programs and History remained present;
- weekly-volume baseline remained intact;
- manual smoke test passed;
- data loss was zero.

This is a development/test-environment safeguard and is not a product feature.

## Phase 4 closeout state

**Status: COMPLETE / VERIFIED.**

PR4.5, the workout-duration stability fix, and the connected-test data-safety mitigation are complete. The canonical Project Resource stack was reconciled and audited.

## Phase 4 future candidates

### Candidate — Cloud Backup Lifecycle Hardening
**Status: CANDIDATE / NOT APPROVED**

Possible future work:
- backup history/version handling;
- stale-backup policies;
- broader retry/recovery UX;
- lifecycle features not required by PR4.5.

These remain unapproved.

## Phase 5

### Phase 5A — Progress & History Presentation
**Status: MERGED / VERIFIED — GitHub #35**

- Head commit: `2ffee60df85271b2b68de96a96f8602d6f41a505`
- Merge commit: `e35f28eb62148ab25525c2c6c9483840d0e3eaf7`

Delivered:
- Progress overview and training-frequency presentation;
- strength/PR progression presentation;
- volume trend and period filtering;
- training-focus / muscle-group presentation;
- enhanced History workout cards;
- calendar-based history presentation;
- monthly recap;
- yearly recap;
- deterministic analytics models and repository queries;
- focused analytics/recap instrumentation coverage.

Verified constraints:
- established workout-history model reused;
- no Room schema/migration change;
- no backup/restore change;
- no Google Drive change;
- no new AI/LLM dependency;
- no backend/cloud analytics dependency;
- historical snapshot semantics preserved;
- body-weight/body-measurement history excluded from this phase.

Verification included repository implementation review, automated tests/build, clean diff hygiene, and manual emulator verification of Progress, History/Calendar, monthly recap, yearly recap, and volume-filter presentation.

### Phase 5B — Profile Foundation & Body Progress
**Status: MERGED / VERIFIED — GitHub #41**

- Head commit: `e6ab49fd2ce8e91d7e5f3f090bf1b9f615c60410`
- Merge commit: `f1fd9d91f4fbaee7df508b0819a3a478f8f46e31`

Delivered:
- optional sex, date of birth, and height profile foundation;
- feet/inches-preferred height entry with canonical metric storage;
- dated body-weight history;
- dated waist history;
- deterministic local age and BMI calculations;
- adult Indian/Asian-Indian BMI classification with separate non-adult handling;
- Profile hub integration;
- Room migration from v21;
- backward-compatible `.ironlog` backup/restore extension;
- calculation, repository, and migration test coverage.

Verified constraints:
- one local IronLog dataset per installation preserved;
- no multi-account Room redesign;
- no nutrition/calorie tracking;
- no medical diagnosis/treatment;
- no goals/achievements;
- no AI/LLM dependency;
- no backend/cloud analytics dependency;
- existing workout/history/progress/PR behavior preserved.

Verification included build/JVM tests, clean diff hygiene, manual Profile/Body Progress verification, local export/import round-trip, and regression smoke testing. Instrumentation tests were implemented but not executed locally because no connected device was available.

### Phase 5C — Goals & Deterministic Progress Intelligence
**Status: MERGED / VERIFIED — GitHub #44**

- Head commit: `7b20dd0f58c10ab12a30e930d170cdaab63a29fb`
- Merge commit: `f3406f227672ac4cd9f14ea1ee97bf2021d4ea1c`
- Feature branch `feature/phase-5c-goals` deleted after merge.

Delivered:
- target body-weight goals;
- target waist goals;
- target exercise/PR goals;
- workout-frequency goals;
- deterministic goal progress/completion calculations;
- deterministic trend/rate and bounded status handling;
- Goal CRUD UI and Progress goal-card integration;
- insufficient-data/no-meaningful-trend handling;
- Room persistence/migration support;
- backward-compatible backup/restore goal support;
- deterministic tests and migration/verification coverage.

Verified constraints:
- no LLM/AI model, generative AI, or network dependency;
- no backend/cloud analytics;
- no nutrition/calorie system;
- no medical diagnosis/treatment;
- no achievements/ranks/badges;
- no broad personalization;
- no unrelated UI/UX modernization;
- no multi-account Room redesign.

Verification included automated tests/build and clean-diff checks, Project Owner manual emulator verification of goal flows/calculations/status behavior, backup/restore regression, existing-product regression, and final re-tests of two manual UI observations discovered during the cycle.

### Phase 5C manual-test observation closeout

Two meaningful UI observations were discovered during manual testing and actively managed rather than forgotten:

1. **Progress goal-card overlap/obscuring — VERIFIED FIXED.** The goal section was moved into the normal Progress scrollable layout so it no longer masked existing Progress information. Project Owner re-tested the original behavior on the emulator.
2. **BMI faded/disabled-looking presentation — VERIFIED FIXED.** BMI remains read-only/non-editable but is now rendered with normal readable presentation. Project Owner re-tested the original behavior on the emulator.

A Google Drive OAuth refresh error also occurred during one test cycle. Sign-out/sign-in restored the cloud backup/restore flow; it is recorded as `RESOLVED / NOT A DEFECT — current cycle`, with no code change made from that observation alone.

### Phase 5 completion boundary

**Status: COMPLETE / VERIFIED.**

Phase 5 ends after 5C. No Phase 5D AI/LLM phase is planned or authorized. After 5C, the next planned product phase is Phase 6 — UI/UX Modernization & Polish.

### Phase 6 — UI/UX Modernization & Polish
**Status: PLANNED / NOT APPROVED**

Future direction after completed Phase 5:
- modern visual system and component consistency;
- typography, spacing, surfaces, colors, and hierarchy;
- navigation and information-architecture polish;
- screen-by-screen UX refinement;
- empty/loading/error states;
- transitions/interaction quality;
- accessibility and touch-target refinement;
- polished presentation of workout, history, progress, profile, body progress, goals, backup/data, and settings.

Phase 6 is not yet implementation-authorized. A new `11_ACTIVE_PR_SPEC.md` must be approved before implementation begins.

### Future AI direction
**Status: DEFERRED / NOT APPROVED**

No AI/LLM implementation is planned during Phase 5 or Phase 6. After Phase 6, the Project Owner may explicitly decide whether an AI phase is desirable and, if so, whether it should use a local model, user-authorized external LLM, hybrid architecture, or another approach. Until that explicit decision, AI must not be promoted from a future idea into implementation scope.

## Scope-First Development Rule

For every PR:
1. identify the phase;
2. define one coherent objective;
3. lock scope;
4. lock non-goals;
5. define risks and mitigations;
6. define acceptance criteria;
7. define verification plan;
8. record explicit approval;
9. only then implement.

## Source-of-Truth Rule

Current repository code/schema/tests outrank documentation. If implementation differs from this plan, stop and inspect the repository before changing code.

## Planning Rule

Do not invent calendar deadlines. Future items remain CANDIDATE/PLANNED/TBD/INFERENCE as appropriate.

## Current Orientation

**Completed:** PR4.1 → PR4.2 → PR4.3 → PR4.4 → PR4.5 → stability fix #32 → Phase 4 closeout → Phase 5A #35 → Phase 5B #41 → Phase 5C #44  
**Current:** Phase 5 complete / verified; no active implementation PR  
**Next planned:** define and explicitly approve Phase 6 UI/UX Modernization & Polish before implementation
