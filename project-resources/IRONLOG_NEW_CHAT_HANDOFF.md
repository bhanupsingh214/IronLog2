# IronLog — New Chat Handoff

**Handoff version:** v4.1
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
- Phase 5C / GitHub #44 — Goals & Deterministic Progress Intelligence — MERGED / VERIFIED.
- Phase 5C head: `7b20dd0f58c10ab12a30e930d170cdaab63a29fb`.
- Phase 5C merge commit: `f3406f227672ac4cd9f14ea1ee97bf2021d4ea1c`.
- Phase 5C feature branch was deleted after merge.
- Phase 5 ends after 5C; no Phase 5D AI/LLM work is planned or authorized.
- Phase 6 — UI/UX Modernization & Polish — planned after 5C, not yet approved for implementation.
- AI/LLM work is deferred until after Phase 6 and requires explicit Project Owner authorization.

## 3. Phase 5C completed boundary

Phase 5C delivered a local-first deterministic goal/progress layer over existing workout, history, progress, profile, and body-progress foundations.

Delivered goal categories:
- target body weight;
- target waist circumference;
- target exercise/PR value;
- workout-frequency target.

Deterministic local intelligence calculates:
- absolute/percentage change where meaningful;
- goal completion percentage;
- current vs target state;
- workout-frequency adherence;
- simple trend direction/rate;
- bounded status such as Not Started, In Progress, On Track, Behind, Completed, No Meaningful Trend, or Insufficient Data.

Phase 5C introduced no LLM, generative AI, local language-model inference, model training/fine-tuning, backend/cloud analytics, network dependency, nutrition/calorie tracking, medical diagnosis/treatment, achievements/ranks/badges, broad personalization, unrelated UI/UX modernization, or multi-account Room redesign.

## 4. Phase 5C verification and observation closeout

Project Owner reported a full current-cycle acceptance pass, including:
- Goal CRUD and supported goal flows;
- deterministic goal calculations;
- trend/status and insufficient-data behavior;
- migration behavior;
- local backup/export/restore;
- Google Drive backup/restore after fresh sign-in;
- Workout/History/Progress/Profile/Body Progress/PR regression.

Two manual UI findings were discovered during testing and actively managed rather than forgotten:
- Progress goal-card overlap/obscuring — VERIFIED FIXED after emulator re-test;
- BMI faded/disabled-looking presentation — VERIFIED FIXED after emulator re-test.

The transient Google Drive OAuth refresh error observed during one test cycle was resolved by signing out/signing in again and is recorded as `RESOLVED / NOT A DEFECT — current cycle`. No Drive-auth code change was justified by that evidence alone.

## 5. Phase 5/AI boundary

**Phase 5 ends after 5C.** There is no Phase 5D AI/LLM phase.

The word `intelligence` in Phase 5C means deterministic application logic, not AI.

After Phase 5C, the planned next phase is Phase 6 — UI/UX Modernization & Polish. No AI/LLM implementation is planned during Phase 6.

After Phase 6, the Project Owner may explicitly decide whether an AI phase is desirable and what form it should take. Until that decision is made, AI remains `DEFERRED / NOT APPROVED`.

## 6. Stability fixes completed

### Workout finish duration
Active workout confirmation uses live elapsed duration while completed sessions use persisted duration. GitHub #32 merged this fix after 7/7 connected tests, successful debug build, and manual emulator verification.

### Connected-test data safety
The connected instrumentation lifecycle was found to remove the production package after a run, which could wipe the local Room database on the primary emulator.

Verified mitigation:

```properties
android.injected.androidTest.leaveApksInstalledAfterRun=true
```

Verified with 7/7 connected tests, package-preservation checks, Programs/History preservation, and manual smoke testing with zero data loss.

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
Project Owner objective approval
→ ChatGPT canonical planning/resource update
→ Gemini implementation/evidence
→ Project Owner runtime verification
→ ChatGPT reconciliation against Git/GitHub
→ ChatGPT Project Resource maintenance
→ Project Owner final acceptance / merge lifecycle
```

Do not duplicate documentation maintenance through Gemini when ChatGPT has connected GitHub access.

## 11. Exact next action

There is currently **no active implementation PR**.

The next planned product phase is Phase 6 — UI/UX Modernization & Polish. Before implementation begins, the Project Owner must explicitly approve the Phase 6 objective/scope and ChatGPT must establish a new locked `11_ACTIVE_PR_SPEC.md`.

Do not introduce or plan an AI/LLM implementation unless the Project Owner explicitly re-authorizes it after Phase 6.
