# IronLog Roadmap & PR Ledger

**Documentation version:** v3.2  
**As of:** 2026-08-15

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
- Merge commit: `eb3bcf34aeb656528b835a4d5fb2c46cb478109e`

The fix calculates live elapsed duration for active sessions while retaining persisted duration for completed sessions. Verification recorded 7/7 connected tests, a successful debug build, and manual emulator verification.

### Connected-test data-safety mitigation
**Status: VERIFIED / IN CLOSEOUT**

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

**Current:** documentation and stability closeout.

No new feature PR is authorized by this ledger. After closeout, the next phase/PR must be selected explicitly by the Project Owner and recorded in `11_ACTIVE_PR_SPEC.md` before implementation.

## Phase 4 future candidates

### Candidate — Cloud Backup Lifecycle Hardening
**Status: CANDIDATE / NOT APPROVED**

Possible future work:
- backup history/version handling;
- stale-backup policies;
- broader retry/recovery UX;
- lifecycle features not required by PR4.5.

These are not authorized without a new decision.

## Phase 5 candidates

### Phase 5A — Progress & History Presentation
**Status: CANDIDATE / NOT APPROVED**
- Body Progress;
- fitness timeline;
- monthly/yearly recap.

### Phase 5B — Goals & Engagement
**Status: CANDIDATE / NOT APPROVED**
- goal tracking;
- achievements/ranks.

### Phase 5C — AI Assistance
**Status: CANDIDATE / NOT APPROVED**
- AI coach.

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

**Completed:** PR4.1 → PR4.2 → PR4.3 → PR4.4 → PR4.5 → stability fix #32  
**Current:** Phase 4 documentation/stability closeout  
**Next:** TBD; choose only after closeout and explicit Project Owner approval.
