# IronLog — Project State

**Documentation version:** v3.4
**As of:** 2026-08-15
**State classification:** CURRENT / VERIFIED BASELINE

## Repository baseline

- Repository: `bhanupsingh214/IronLog2`
- Default branch: `master`
- Latest merged feature PR: PR4.5 / GitHub #31
- PR4.5 head commit: `806f09e694699511d4c1ae11fcf11689b4b47df5`
- PR4.5 merge commit: `6317c2f2c3aa12e56709d5b62cf600e8f1bca7d4`
- Latest merged stability PR: GitHub #32 — workout finish duration
- PR32 head commit: `8dc08ce1b426f681a9e97afc43c5fa751f0f8521`
- PR32 merge commit: `eb3bcf34aeb656528b835a4d5fb2c46cb478109e`

The merged PR4.5 and PR32 baseline is the starting point for Phase 4 closeout. Any new implementation session must re-check the actual current `master` state before editing.

## Current phase

**Phase 4 — Backup & Recovery / Stability Closeout COMPLETE**

The implementation and stability work is complete. The documentation closeout and canonical-stack audit are now complete as well. No new feature implementation is authorized until a new objective is explicitly selected.

## Completed milestones

- Phase 3 identity/records/analytics work — MERGED / VERIFIED.
- PR4.1 — Backup Export — MERGED / VERIFIED.
- PR4.2 — Backup Restore — MERGED / VERIFIED.
- PR4.3 — Backup Management & Recovery UX — MERGED / VERIFIED.
- PR4.4 — Google Account & Google Drive Backup — MERGED / VERIFIED.
- PR4.5 / GitHub #31 — Google Drive Cloud Restore — MERGED / VERIFIED.
- GitHub #32 — Workout finish confirmation live duration — MERGED / VERIFIED.
- Phase 4 documentation/stability closeout — MERGED / VERIFIED.

## PR4.5 verified boundary

PR #31 merged the approved Google Drive cloud-restore flow. Its recorded verification included:
- JVM test suite/build checks;
- clean debug build;
- connected Android tests with 5 tests passing;
- real-emulator Google Drive restore flow, including backup metadata confirmation and restore flow;
- no schema/migration change;
- existing restore pipeline retained as authoritative.

PR4.5 did not implement general Drive browsing, backup history/version management, cloud synchronization, scheduled restore, backup-format redesign, or schema redesign.

## Stability verification

The workout finish-duration issue was fixed in GitHub #32. The active confirmation dialog now uses live elapsed duration for active sessions while completed sessions retain persisted duration. Verification recorded 7/7 connected Android tests passing, a successful debug build, and manual emulator verification.

A separate development-test data-loss regression was also investigated. The connected instrumentation-test lifecycle could remove the production IronLog package after a run, wiping the local Room database on the primary emulator. The verified mitigation is:

```properties
android.injected.androidTest.leaveApksInstalledAfterRun=true
```

A controlled verification passed with 7/7 connected tests, the production package remaining installed, Programs and History remaining present, and a manual smoke test passing with zero data loss. The mitigation is included in the stability-closeout change set.

## Current authorization

**No new product feature PR is currently authorized.**

Phase 4 documentation/stability closeout is complete. The next step is explicit Project Owner selection of the next phase/objective, followed by a new locked `11_ACTIVE_PR_SPEC.md` before any implementation begins.

## Business constraint

The Project Owner does not want recurring IronLog infrastructure costs to be assumed casually. Prefer user-owned resources where technically appropriate and distinguish user-owned storage from potentially billable Google/API/Firebase/backend services.

## State discipline

This document records current verified state. It does not replace source inspection, test evidence, or the active PR specification.
