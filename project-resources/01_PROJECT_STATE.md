# IronLog — Project State

**Documentation version:** v3.8
**As of:** 2026-08-16
**State classification:** CURRENT / VERIFIED BASELINE

## Repository baseline

- Repository: `bhanupsingh214/IronLog2`
- Default branch: `master`
- Latest merged feature PR: Phase 5B / GitHub #41
- Phase 5B head commit: `e6ab49fd2ce8e91d7e5f3f090bf1b9f615c60410`
- Phase 5B merge commit: `f1fd9d91f4fbaee7df508b0819a3a478f8f46e31`
- Phase 5A / GitHub #35 and prior stability work remain part of the verified baseline.

The current `master` includes the completed Phase 5B Profile Foundation & Body Progress work. Any new implementation session must re-check the actual current `master` state before editing.

## Current phase

**Phase 5B — Profile Foundation & Body Progress COMPLETE / VERIFIED**

Phase 5B implementation, automated verification available in the local environment, manual emulator verification, merge, and branch deletion are complete. The documentation closeout is now being reconciled. No new feature implementation is authorized until a new objective is explicitly selected and a new `11_ACTIVE_PR_SPEC.md` is locked.

## Completed milestones

- Phase 3 identity/records/analytics work — MERGED / VERIFIED.
- PR4.1 — Backup Export — MERGED / VERIFIED.
- PR4.2 — Backup Restore — MERGED / VERIFIED.
- PR4.3 — Backup Management & Recovery UX — MERGED / VERIFIED.
- PR4.4 — Google Account & Google Drive Backup — MERGED / VERIFIED.
- PR4.5 / GitHub #31 — Google Drive Cloud Restore — MERGED / VERIFIED.
- GitHub #32 — Workout finish confirmation live duration — MERGED / VERIFIED.
- Phase 4 documentation/stability closeout — MERGED / VERIFIED.
- Phase 5A / GitHub #35 — Progress & History Presentation — MERGED / VERIFIED.
- Phase 5B / GitHub #41 — Profile Foundation & Body Progress — MERGED / VERIFIED.

## Phase 5B verified boundary

GitHub #41 delivered the first durable local profile and body-progress foundation while preserving the one-local-dataset-per-installation ownership model. Verified scope included:
- optional sex, date of birth, and height profile fields;
- feet/inches-preferred height entry with canonical metric persistence;
- durable dated body-weight history;
- durable dated waist history;
- deterministic local age and BMI calculations;
- adult Indian/Asian-Indian BMI classification with a separate non-adult path;
- Profile hub integration while preserving existing account, backup/data, workout settings, and navigation ownership;
- Room migration from v21 to the Phase 5B schema;
- backward-compatible `.ironlog` backup/restore extension;
- unit, repository, calculation, and migration test coverage.

No multi-account Room redesign, nutrition/calorie tracking, medical diagnosis/treatment, goals/achievements, AI/LLM coaching, or unrelated UI redesign was introduced.

## Phase 5B verification

Recorded evidence included implementation/audit PASS, JVM/body-metric tests PASS, successful debug build, clean `git diff --check`, manual Profile/Body Progress verification PASS, local export/import round-trip PASS, and Workout/History/Progress/PR regression smoke verification PASS. Instrumentation tests for migration/body-progress repository behavior were implemented but were not executed in the local environment because no connected device was available. The merged PR records this distinction explicitly.

For adult Indian/Asian-Indian BMI interpretation, the implementation uses the reviewed 2009 Indian consensus direction of 18.0–22.9 normal, 23.0–24.9 overweight, and ≥25.0 obesity; the UI treats BMI as a screening/derived metric and keeps non-adult interpretation separate. Contemporary reviews continue to describe the Indian 23/25 cut points. The implementation does not provide diagnosis or treatment advice.

## Current authorization

**No new product feature PR is currently authorized.**

Phase 5B is complete. The next step is explicit Project Owner selection of the next phase/objective, followed by a new locked `11_ACTIVE_PR_SPEC.md` before any implementation begins.

## Business constraint

The Project Owner does not want recurring IronLog infrastructure costs to be assumed casually. Prefer user-owned resources where technically appropriate and distinguish user-owned storage from potentially billable Google/API/Firebase/backend services.

## State discipline

This document records current verified state. It does not replace source inspection, test evidence, or the active PR specification.
