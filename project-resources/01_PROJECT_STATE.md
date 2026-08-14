# IronLog — Project State

**Documentation version:** v3.1
**As of:** 2026-08-14
**State classification:** CURRENT / VERIFIED BASELINE

## Repository baseline

- Repository: `bhanupsingh214/IronLog2`
- Default branch: `master`
- Latest verified completed milestone: PR4.4 / GitHub #29
- PR4.4 head commit: `50e77cbcb8da0fce58aae990166097b512174b1d`
- PR4.4 merge commit: `fdfdfb47d4844983d5806287d103a81f8946869e`

The merged PR4.4 baseline is the starting point for PR4.5. Any implementation session must re-check the actual current `master` state before editing.

## Current phase

**Phase 4 — Backup & Recovery**

## Completed milestones

- Phase 3 identity/records/analytics work — MERGED / VERIFIED.
- PR4.1 — Backup Export — MERGED / VERIFIED.
- PR4.2 — Backup Restore — MERGED / VERIFIED.
- PR4.3 — Backup Management & Recovery UX — MERGED / VERIFIED.
- PR4.4 — Google Account & Google Drive Backup — MERGED / VERIFIED.

## PR4.4 verified boundary

Real-device verification established:
- Google account sign-in;
- sign-out → sign-in;
- app restart;
- separate Google Drive authorization;
- Drive API access;
- `.ironlog` cloud upload;
- repeat cloud backup;
- cloud backup status/last-backup information;
- local export/import regression.

PR4.4 did not implement cloud download/restore, Drive browsing, backup-format redesign, database/schema redesign, scheduled backups, or centralized IronLog-owned backup storage.

## Current authorization

**PR4.5 — Google Drive Cloud Restore: APPROVED / PLANNED.**

Implementation authorization is recorded in `11_ACTIVE_PR_SPEC.md`.

Locked objective:
download the user's `.ironlog` backup from Google Drive `appDataFolder` and pass it through the established validation/import/restore pipeline.

No other PR is concurrently authorized.

## PR4.5 pre-implementation state

Before code changes:
- sync/verify the intended base branch;
- inspect current PR4.4 code;
- verify current account/authorization lifecycle;
- inspect `ImportService` and `RestoreRepository`;
- confirm the input-boundary design against current source;
- verify no schema change is required;
- confirm clean working tree.

## Business constraint

The Project Owner does not want recurring IronLog infrastructure costs to be assumed casually. Prefer user-owned resources where technically appropriate and distinguish user-owned storage from potentially billable Google/API/Firebase/backend services.

## State discipline

This document records current verified state. It does not replace source inspection, test evidence, or the active PR specification.
