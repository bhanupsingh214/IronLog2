# IronLog Roadmap & PR Ledger

**Documentation version:** v4.0  
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
**Status: IMPLEMENTATION IN PROGRESS — GitHub #44 OPEN / DRAFT**

Objective:
- establish a small durable goal system;
- calculate goal progress, trends, adherence, and deterministic status locally;
- present understandable progress facts from existing workout/history/profile/body-progress data;
- preserve local-first, one-dataset-per-installation architecture.

Initial goal categories:
- target body weight;
- target waist circumference;
- target exercise/PR value;
- workout-frequency target.

Current implementation state:
- GitHub #44 contains the Phase 5C implementation and is not yet merged;
- deterministic goal logic/unit verification is present;
- Project Owner emulator verification has confirmed the two previously reported Phase 5C UI findings are fixed;
- the regression matrix retains those findings as `VERIFIED FIXED` observations rather than deleting them;
- remaining Phase 5C acceptance/verification rows must still be completed before merge.

Required boundaries:
- no LLM, AI model, generative AI, or network dependency;
- no backend/cloud analytics;
- no nutrition/calorie system;
- no medical diagnosis/treatment;
- no achievements/ranks/badges;
- no broad personalization;
- no unrelated UI/UX modernization;
- no multi-account Room redesign.

Acceptance and verification are locked in `11_ACTIVE_PR_SPEC.md`.

### Phase 5 completion boundary

Phase 5 is planned to end after 5C. No Phase 5D AI/LLM phase is planned or authorized. After 5C, the next planned product phase is Phase 6 — UI/UX Modernization & Polish.

### Phase 6 — UI/UX Modernization & Polish
**Status: PLANNED / NOT APPROVED**

Future direction after Phase 5C:
- modern visual system and component consistency;
- typography, spacing, surfaces, colors, and hierarchy;
- navigation and information-architecture polish;
- screen-by-screen UX refinement;
- empty/loading/error states;
- transitions/interaction quality;
- accessibility and touch-target refinement;
- polished presentation of workout, history, progress, profile, body progress, goals, backup/data, and settings.

Phase 6 is not part of Phase 5C implementation scope.

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

**Completed:** PR4.1 → PR4.2 → PR4.3 → PR4.4 → PR4.5 → stability fix #32 → Phase 4 closeout → Phase 5A #35 → Phase 5B #41  
**Current:** Phase 5C implementation in progress on GitHub #44; two reported UI findings are VERIFIED FIXED by Project Owner emulator re-test  
**Next:** complete remaining Phase 5C acceptance/verification, final evidence review, Project Owner acceptance, merge, and branch cleanup
