# IronLog — New Chat Handoff

**Handoff version:** v3.5
**As of:** 2026-08-15
**Purpose:** Make a fresh chat inside the IronLog project immediately productive without relying on prior conversation memory.

## 1. First action in every new chat

Read, in order:
1. `00_PROJECT_INDEX.md`
2. `01_PROJECT_STATE.md`
3. `03_ROADMAP_AND_PR_LEDGER.md`
4. `11_ACTIVE_PR_SPEC.md`

Then read relevant technical/evidence resources.

If GitHub is connected, fetch `/project-resources/` from the repository and treat that as the current canonical stack.

## 2. Current verified baseline

- Phase 4 — Backup & Recovery / stability closeout COMPLETE.
- PR4.1 — MERGED / VERIFIED.
- PR4.2 — MERGED / VERIFIED.
- PR4.3 — MERGED / VERIFIED.
- PR4.4 / GitHub #29 — MERGED / VERIFIED.
- PR4.5 / GitHub #31 — MERGED / VERIFIED.
- GitHub #32 — workout finish confirmation duration — MERGED / VERIFIED.
- Connected-test data-safety mitigation — VERIFIED / CLOSED.
- Phase 4 documentation/stability closeout — VERIFIED / CLOSED.
- Phase 5A / GitHub #35 — Progress & History Presentation — MERGED / VERIFIED.
- Phase 5A feature branch — DELETED after merge.
- No new feature PR is currently authorized.

## 3. Phase 5A completed boundary

Phase 5A delivered deterministic/local presentation over the established workout-history data:
- Progress overview and training-frequency presentation;
- strength/PR progression;
- volume trend and period filtering;
- training-focus / muscle-group presentation;
- enhanced History cards;
- calendar history;
- monthly recap;
- yearly recap;
- focused analytics/recap tests.

The implementation reused the established `WorkoutSession → SessionExercise → SessionSet` model and introduced no Room schema/migration, backup/restore, Google Drive, AI/LLM, or backend/cloud analytics change. Body-weight/body-measurement history remains excluded.

## 4. Phase 5A verification

GitHub #35 recorded repository review PASS, automated tests/build PASS, clean diff hygiene, and manual emulator verification of Progress, History/Calendar, monthly recap, yearly recap, and volume filters. Existing user data remained present during verification. `0m` average duration was accepted for test workouts mostly under one minute, and intentionally large test values were accepted as test data.

## 5. Stability fixes completed

### Workout finish duration
Active workout confirmation now uses live elapsed duration while completed sessions use persisted duration. GitHub #32 merged this fix after 7/7 connected tests, successful debug build, and manual emulator verification.

### Connected-test data safety
The connected instrumentation lifecycle was found to remove the production package after a run, which could wipe the local Room database on the primary emulator.

Verified mitigation:

```properties
android.injected.androidTest.leaveApksInstalledAfterRun=true
```

Verified with 7/7 connected tests, package-preservation checks, Programs/History preservation, and manual smoke testing with zero data loss.

## 6. Critical historical regression

PR4.3 exposed a populated-database restore bug: DELETE statements were ineffective because `query().close()` did not execute them.

The fix used `execSQL()` with FK-safe ordering and preserved transactional restore.

This is a critical regression boundary. Do not casually redesign restore internals.

## 7. Evidence rules

- Runtime behavior → emulator/device.
- Build → actual build output.
- Git/PR state → actual Git/GitHub.
- Schema → current repository.
- Product acceptance → Project Owner.

Agent reports are evidence, not proof.

## 8. Collaboration

Project Owner:
- final product decisions;
- PR authorization;
- runtime testing;
- final acceptance;
- final release authority.

ChatGPT:
- architecture/reasoning;
- scope/risk/acceptance;
- review;
- continuity;
- canonical Project Resource maintenance and GitHub documentation closeout.

Gemini/implementation agent:
- substantial approved repository implementation;
- source inspection;
- implementation-readiness audits when requested;
- build/tests;
- implementation evidence.

Gemini should not independently maintain the canonical Project Resources or perform documentation closeout unless explicitly delegated.

## 9. If resources disagree

Do not guess.

Use:

```text
repository code/schema/tests
→ Git/GitHub history
→ canonical resources
→ conversation
→ inference
```

Repair the canonical resource stack when necessary.

## 10. Documentation workflow

For normal work:

```text
Gemini implementation/evidence
→ Project Owner runtime verification
→ ChatGPT reconciliation against Git/GitHub
→ ChatGPT Project Resource maintenance
→ Project Owner final acceptance / merge lifecycle
```

Do not duplicate documentation maintenance through Gemini when ChatGPT has connected GitHub access.

## 11. Exact next action

Phase 5A is complete and its documentation closeout is in progress. After closeout, wait for explicit Project Owner selection of the next phase/PR. A new locked `11_ACTIVE_PR_SPEC.md` with objective, scope, non-goals, risks, acceptance criteria, and verification plan is required before implementation.
