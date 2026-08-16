# IronLog — New Chat Handoff

**Handoff version:** v4.2
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

If a design/UX question is involved, also review the locked Phase 6 design decisions recorded in this handoff before proposing new screens or navigation.

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
- Phase 6 — UI/UX Modernization & Polish — design direction has now been worked through and the principal screen mockups are locked, but **implementation is not yet authorized**.
- AI/LLM work remains deferred until after Phase 6 and requires explicit Project Owner authorization.

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

## 6. Phase 6 design-lock status

Phase 6 is now at the **design-definition / locked-mockup stage**. The mockups are product/design decisions, not implementation authorization.

The agreed design direction is:
- modern, premium, dark visual language;
- purple primary/action accent with restrained semantic colors;
- strong hierarchy, spacing, cards/surfaces, readable typography, and scan-friendly layouts;
- consistent iconography and interaction patterns;
- the Start Workout visual language is the preferred reference styling for workout-related flows;
- the Dashboard is the primary home surface;
- History is **not** a bottom-navigation destination and is accessed from the Dashboard's Recent History area;
- the bottom navigation has exactly five main destinations: **Dashboard · Programs · Progress · Goals · Profile**;
- the same product structure and interaction model must be designed to work cleanly on both Android and iOS;
- platform-specific conventions may be respected where necessary, but product semantics and information architecture should remain consistent.

### Locked principal screen/workflow mockups

The Project Owner has supplied/locked the principal mockups for:

1. **Dashboard + bottom navigation**
   - Dashboard is the home surface.
   - Recent History remains on Dashboard.
   - Bottom navigation is Dashboard · Programs · Progress · Goals · Profile.

2. **Programs**
   - Programs list, program actions, workout days, workout-day exercises, exercise prescription, and exercise library/add-exercise flow.

3. **Start Workout**
   - Choose Workout → Active Workout → Rest Timer → Workout Progress → Finish Confirmation → Workout Summary.
   - This is the preferred styling reference for the workout experience.

4. **Add Log / Add Previous Workout**
   - Select Date → Choose Workout → Workout Overview → Log Exercise → Set Entry → Exercise Completed → Workout Summary → Performance Comparison → Workout Logged.
   - Add Log uses the same underlying workout/logging experience and preferred visual language as Start Workout.
   - Date selection must ask which date the user trained, not imply that the user trained multiple times on a day.

5. **Progress**
   - At-a-glance training metrics, recent wins, strength progress, workload, training balance, active goals, insights, and personal records.

6. **Goals**
   - Goals overview, goal status/insight states, create-goal flow, review, and goal-created confirmation.
   - The previously locked Goals mockup remains the reference; do not replace it casually.

7. **Profile + Settings**
   - Profile foundation plus Personal Information, Body Progress, Goals, Workout Settings, Units, Appearance, Backup & Data, and Google Account destinations.
   - Backup/Data flows include local export/import and Google Drive backup/restore presentation.

8. **History — FINAL LOCK**
   - Dashboard Recent History entry → History List → Workout Details → Calendar View (Day) → Monthly Summary → Yearly Summary.
   - History is accessed from Dashboard and is **not** added to bottom navigation.
   - Calendar view shows daily workout history and the monthly/yearly summary below it.
   - The final locked History mockup uses the same modern styling direction as Dashboard and Start Workout while retaining the required five-item bottom navigation: Dashboard · Programs · Progress · Goals · Profile.

### Important locked History correction

Do not reintroduce the earlier incorrect design where History appeared as a bottom-navigation destination. The final approved structure is:

```text
Dashboard
  └── Recent History
       └── History
            ├── List
            ├── Workout Details
            └── Calendar
                 ├── Day workouts
                 ├── Monthly Summary
                 └── Yearly Summary
```

## 7. Cross-platform design requirement

All Phase 6 UI/UX decisions must be evaluated for both Android and iOS before implementation is locked.

Required approach:
- keep core information architecture and semantics platform-neutral;
- use responsive layouts rather than Android-only assumptions;
- avoid relying on Android-only navigation metaphors or controls where an equivalent cross-platform pattern is required;
- preserve appropriate platform conventions for back behavior, system bars, dialogs/sheets, text input, date selection, accessibility, and touch targets;
- validate dense screens at practical phone sizes on both platforms before final UI lock.

This is a design/architecture constraint for Phase 6, not a request to rewrite the current Android implementation yet.

## 8. Figma status

Figma is the intended design workspace for Phase 6 polishing/redesign work because it can provide editable, inspectable design artifacts and a useful bridge toward implementation.

The Project Owner has connected a Figma account and confirmed that the IronLog Phase 6 design file is present in the account. The live Figma connector was **not successfully callable in the last verification attempt**, so account identity/file permissions must be re-checked before treating live Figma access as verified.

Do not claim that Figma was read or modified unless the connector actually returns the file data.

The Project Owner is new to Figma, so workflow/tooling should be handled as much as possible by the assistant when the connector is available; the user should not be required to manually reproduce design-system work unless necessary.

## 9. Stability fixes completed

### Workout finish duration
Active workout confirmation uses live elapsed duration while completed sessions use persisted duration. GitHub #32 merged this fix after 7/7 connected tests, successful debug build, and manual emulator verification.

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
- Design lock → Project Owner-approved mockup/design artifact.

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
→ locked design artifacts
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

There is currently **no active implementation PR**.

The next product phase is Phase 6 — UI/UX Modernization & Polish. The principal mockups have been locked as the design reference, but implementation scope is still **not authorized** until the Project Owner explicitly approves the Phase 6 objective/scope and a new locked `11_ACTIVE_PR_SPEC.md` is created.

Before implementation:
1. Reconcile the locked mockups against the current Android implementation.
2. Perform a read-only source audit screen by screen.
3. Confirm cross-platform Android/iOS feasibility and any platform-specific adaptations.
4. Verify/reconnect live Figma access and establish the Phase 6 design workspace/design-system structure if available.
5. Define Phase 6 scope, non-goals, acceptance criteria, risk/mitigation, and verification plan.
6. Obtain explicit Project Owner implementation approval.

Do not introduce or plan an AI/LLM implementation unless the Project Owner explicitly re-authorizes it after Phase 6.
