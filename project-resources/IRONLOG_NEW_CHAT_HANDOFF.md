# IronLog — New Chat Handoff

**Handoff version:** v3.9
**As of:** 2026-08-16
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
- Phase 5B / GitHub #41 — Profile Foundation & Body Progress — MERGED / VERIFIED.
- Phase 5A and Phase 5B feature branches — DELETED after merge.
- Phase 5C — Goals & Deterministic Progress Intelligence — APPROVED / LOCKED; implementation not yet started.
- Phase 5 ends after 5C; no Phase 5D AI/LLM work is planned or authorized.
- Phase 6 — UI/UX Modernization & Polish — planned after 5C, not yet approved for implementation.
- AI/LLM work is deferred until after Phase 6 and requires explicit Project Owner authorization.

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

The implementation reused the established `WorkoutSession → SessionExercise → SessionSet` model and introduced no Room schema/migration, backup/restore, Google Drive, AI/LLM, or backend/cloud analytics change. Body-weight/body-measurement history was explicitly excluded from Phase 5A and was subsequently introduced through Phase 5B's separate persistent body-progress model.

## 4. Phase 5A verification

GitHub #35 recorded repository review PASS, automated tests/build PASS, clean diff hygiene, and manual emulator verification of Progress, History/Calendar, monthly recap, yearly recap, and volume filters. Existing user data remained present during verification. `0m` average duration was accepted for test workouts mostly under one minute, and intentionally large test values were accepted as test data.

## 5. Phase 5B completed boundary

Phase 5B / GitHub #41 established the durable local profile and body-progress foundation:
- optional sex, DOB, and height;
- feet/inches-preferred height input with canonical metric storage;
- dated body-weight history;
- dated waist history;
- deterministic local age and BMI calculations;
- adult Indian/Asian-Indian BMI screening classification with separate non-adult handling;
- Profile hub integration;
- Room migration v21→v22;
- backward-compatible `.ironlog` backup/restore extension.

Phase 5B preserved the one-local-dataset-per-installation ownership model and introduced no LLM/backend dependency, nutrition/calorie tracking, medical diagnosis/treatment, goals/achievements, or multi-account Room redesign.

## 6. Phase 5B verification

GitHub #41 recorded:
- build PASS;
- JVM/body-metric tests PASS;
- `git diff --check` PASS;
- manual Profile/Body Progress verification PASS;
- local export/import round-trip PASS;
- Workout/History/Progress/PR regression smoke test PASS.

Instrumentation tests for migration/body-progress repository behavior were implemented but not executed locally because no connected device was available. Do not represent those unexecuted instrumentation tests as runtime PASS.

## 7. Phase 5C locked boundary

Phase 5C — Goals & Deterministic Progress Intelligence — is the approved next objective.

Initial goal categories:
- target body weight;
- target waist circumference;
- target exercise/PR value;
- workout-frequency target.

Deterministic local intelligence may calculate:
- absolute/percentage change where meaningful;
- goal completion percentage;
- current vs target state;
- workout-frequency adherence;
- simple trend direction/rate;
- bounded status such as Not Started, In Progress, On Track, Behind, Completed, No Meaningful Trend, or Insufficient Data.

Phase 5C must not introduce:
- LLMs;
- generative AI;
- local language-model inference;
- model training/fine-tuning;
- backend/cloud analytics;
- network dependency;
- nutrition/calorie tracking;
- medical diagnosis/treatment;
- achievements/ranks/badges;
- broad personalization;
- unrelated UI/UX modernization;
- multi-account Room redesign.

## 8. Phase 5/AI boundary

**Phase 5 ends after 5C.** There is no Phase 5D AI/LLM phase.

The word `intelligence` in Phase 5C means deterministic application logic, not AI.

After Phase 5C, the planned next phase is Phase 6 — UI/UX Modernization & Polish. No AI/LLM implementation is planned during Phase 6.

After Phase 6, the Project Owner may explicitly decide whether an AI phase is desirable and what form it should take. Until that decision is made, AI remains `DEFERRED / NOT APPROVED`.

## 9. Stability fixes completed

### Workout finish duration
Active workout confirmation now uses live elapsed duration while completed sessions use persisted duration. GitHub #32 merged this fix after 7/7 connected tests, successful debug build, and manual emulator verification.

### Connected-test data safety
The connected instrumentation lifecycle was found to remove the production package after a run, which could wipe the local Room database on the primary emulator.

Verified mitigation:

```properties
android.injected.androidTest.leaveApksInstalledAfterRun=true
```

Verified with 7/7 connected tests, package-preservation checks, Programs/History preservation, and manual smoke testing with zero data loss.

## 10. Evidence rules

- Runtime behavior → emulator/device.
- Build → actual build output.
- Git/PR state → actual Git/GitHub.
- Schema → current repository.
- Product acceptance → Project Owner.

Agent reports are evidence, not proof.

## 11. Collaboration

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

## 12. If resources disagree

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

## 13. Documentation workflow

For normal work:

```text
Project Owner objective approval
→ ChatGPT canonical planning/resource update
→ Gemini implementation/evidence
→ Project Owner runtime verification
→ ChatGPT reconciliation against Git/GitHub
→ ChatGPT Project Resource maintenance
→ Project Owner final acceptance / merge lifecycle
```

Do not duplicate documentation maintenance through Gemini when ChatGPT has connected GitHub access.

## 14. Exact next action

Gemini must perform a **read-only source audit** against the locked Phase 5C specification. ChatGPT and the Project Owner must reconcile that audit before implementation approval.

After the Phase 5C implementation is complete and merged, close Phase 5. The next planned product phase is Phase 6 UI/UX Modernization & Polish. Do not introduce or plan an AI/LLM phase unless the Project Owner explicitly re-authorizes it after Phase 6.
