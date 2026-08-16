# IronLog Phase 6 — Source Reconciliation

**Status:** DESIGN-LOCK RECONCILIATION / IMPLEMENTATION NOT AUTHORIZED  
**Date:** 2026-08-17  
**Repository baseline:** `master` @ `4baaa705f83258cf9b41d38482deb9a95226f71d`  
**Approved visual references:** `project-resources/phase-6/approved-mockups/` on the Phase 6 mockup branch  

## Purpose

Compare the locked Phase 6 UI/UX mockups with the current Android implementation before any implementation PR is authorized.

This document is a reconciliation artifact. It does **not** authorize code changes.

## Current source baseline

The current Android source already contains dedicated implementations for:

- Dashboard;
- Programs and program/day/exercise editing;
- Workout execution and session exercises;
- Progress/Analytics;
- Goals;
- Profile/Settings and backup/restore;
- History and workout details;
- Personal Records;
- Exercise Library.

Navigation is centralized in `Screen.kt` / `NavGraph.kt`, with `MainScreen.kt` currently owning the bottom navigation.

## Locked Phase 6 navigation

Approved navigation is:

**Dashboard · Programs · Progress · Goals · Profile**

History is **not** a bottom-navigation destination. It is reached from Dashboard Recent History and provides list/calendar/day/month/year history views.

The current source still includes **Workout** as a bottom-navigation item and therefore does not match the locked five-destination navigation model.

## Cross-cutting findings

### 1. Visual system — MAJOR GAP

The current theme still uses the standard Material 3 starter palette (`Purple80`, `Purple40`, `PurpleGrey`, `Pink`) rather than the approved IronLog Phase 6 visual system. The approved references require a dark premium surface system, purple primary gradients/accents, success/warning/error states, consistent cards, typography, icon treatment, and stronger visual hierarchy.

This is a cross-cutting UI foundation change, not a per-screen color tweak.

### 2. Bottom navigation — MAJOR GAP

`MainScreen.kt` currently builds six destinations:

`Dashboard, Programs, Workout, Progress, Goals, Profile`

The locked design requires five:

`Dashboard, Programs, Progress, Goals, Profile`

The Workout destination must become an action/workflow entry rather than a persistent navigation destination. Active-workout state must remain resumable without occupying bottom navigation.

### 3. History navigation — PARTIAL MATCH

The current source has a History route and Dashboard navigation into it, but `Screen.History` is still modeled as a normal screen with a navigation icon and `MainScreen` explicitly treats it as a Dashboard-selected child route. The behavior is close to the locked rule, but the final navigation structure and visual treatment must be reconciled with the approved History mockup.

### 4. Deterministic intelligence — PRESERVE

Goals and Progress already use local calculations and repository/database state. `GoalCalculator` computes progress, directional trend, trend rate, expected progress, and status locally. No LLM is required for these Phase 6 insights.

Do not replace this deterministic logic with an AI/LLM layer merely to reproduce the visual "Insights" or goal-status presentation.

---

# Screen-by-screen reconciliation

## 01 — Dashboard + Bottom Navigation

**Source:** `DashboardScreen.kt`, `DashboardViewModel.kt`, `MainScreen.kt`  
**Status:** FUNCTIONALLY PRESENT / VISUAL + IA GAP

### Existing strengths

- Dashboard already has Start Workout and Add Log actions.
- Today workout/current program/weekly volume/recent history/Personal Records are already represented.
- Active workout resume state exists.
- History and Personal Records are reachable from Dashboard.

### Locked mockup differences

- Approved dashboard has stronger dark premium visual hierarchy and card composition.
- Weekly Volume includes richer visualization and trend context.
- Quick Stats are a dedicated section.
- Recent History has richer contextual rows and stronger scanning hierarchy.
- Personal Records use richer record cards.
- Bottom navigation must be five destinations; Workout is removed.
- Approved empty states and micro-interactions need to be represented.

### Implementation classification

Primarily **UI/component refactor + navigation change**, with data reuse wherever current ViewModels already expose the required values. Some dashboard chart/stat presentation may require derived UI models, but no new AI layer is indicated.

---

## 02 — Programs

**Source:** `ProgramsScreen.kt`, `ProgramsViewModel.kt`, `WorkoutDaysScreen.kt`, `ExercisesScreen.kt`, `WorkoutLoggingScreen.kt` and related ViewModels  
**Status:** FUNCTIONALLY PRESENT / MAJOR VISUAL + FLOW RECONCILIATION

### Existing strengths

- Program list and search exist.
- Active program state exists.
- Set Active, Rename, Duplicate, Archive and Delete actions exist.
- Program → Workout Days → Exercises navigation exists.
- Exercise logging/editing code already exists.

### Locked mockup differences

- Approved Programs list/action presentation is substantially more structured and compact.
- Workout Days requires active/inactive state presentation, reorder interaction, summary data, and clearer day actions.
- Workout Day exercise list needs the approved chips/status treatment.
- Exercise Prescription and Add Exercise Library are visually richer and more explicitly structured.
- Empty states and iconography need the locked Phase 6 treatment.

### Implementation classification

Mostly **UI modernization**, with targeted interaction/state changes for day reorder and approved flow details. Preserve existing program/exercise data relationships.

---

## 03 — Start Workout

**Source:** `WorkoutScreen.kt`, `SessionExercisesScreen.kt`, `WorkoutViewModel.kt`, `SessionExercisesViewModel.kt`, `WorkoutLoggingScreen.kt` and related workout components  
**Status:** FUNCTIONALLY PRESENT / MAJOR UX + VISUAL GAP

### Existing strengths

- Active workout sessions exist.
- Session exercise progression exists.
- Set logging exists.
- Rest timer state exists globally.
- Finish confirmation and completion summary exist.
- Workout comparison and PR achievement data already exist.
- Background-workout handling exists.

### Locked mockup differences

The approved Start Workout flow explicitly establishes:

**Choose Workout → Active Workout → Rest Timer → Workout Progress → Finish Confirmation → Workout Summary**

with additional states for:

- partially completed exercise;
- skipped exercise;
- warm-up set;
- back-off set;
- PR achieved;
- paused workout;
- set notes;
- background workout.

The current implementation contains several of these semantics, but they are distributed across existing screens/components and use standard Material presentation rather than the locked unified workout visual language.

### Implementation classification

**Reuse the existing workout engine and state model.** Concentrate Phase 6 work on the screen/state composition, interaction polish, and navigation. Do not create a second workout engine.

---

## 04 — Add Log / Add Previous Workout

**Source:** `DashboardScreen.kt` / `DashboardViewModel.kt` plus existing workout/logging infrastructure  
**Status:** FUNCTIONALLY PRESENT / FLOW GAP

### Existing behavior

The current Dashboard opens an `AddLogDialog`; it selects a workout day first and then proceeds to date selection and session logging.

### Locked mockup

The approved flow is:

**Select Date → Choose Workout → Workout Overview → Log Exercise → Set Entry → Exercise Completed → Workout Summary → Performance Comparison → Workout Logged**

### Required reconciliation

- Replace the current dialog-first entry with the approved dedicated workflow.
- Date must be the first step.
- Reuse the same logging engine/state semantics as Start Workout.
- Use Start Workout styling as the visual authority.
- Preserve historical-date semantics; do not turn Add Log into a live workout session with incorrect timestamps/state.

### Implementation classification

**Navigation/state-flow change plus UI implementation.** This is more than a visual facelift.

---

## 05 — Progress

**Source:** `ProgressScreen.kt`, `ProgressGoalsIntegrationScreen.kt`, `ProgressViewModel.kt`  
**Status:** STRONG FUNCTIONAL BASE / MAJOR PRESENTATION GAP

### Existing strengths

Current Progress already contains:

- workout count;
- total volume;
- weight PRs and e1RM PRs;
- frequency;
- strength progression chart;
- volume trend chart with time filters;
- volume summary;
- muscle distribution;
- recent PRs;
- goal integration.

### Locked mockup differences

The approved Progress story is:

**Training → Wins → Strength → Workload → Balance → Goals → Insights → PRs**

The current implementation has most underlying data concepts but presents them as a conventional vertically stacked Material screen. The approved mockup requires the richer story flow, stronger metric context, trend feedback, active-goal presentation, insights, and premium visual system.

### Implementation classification

Primarily **UI/data-presentation composition**. Preserve existing deterministic analytics and chart calculations.

---

## 06 — Goals

**Source:** `GoalsScreen.kt`, `GoalViewModel.kt`, `GoalCalculator.kt`  
**Status:** STRONG LOGIC BASE / MAJOR PRESENTATION + CREATION-FLOW GAP

### Existing strengths

`GoalCalculator` already calculates locally:

- goal progress;
- directional trend;
- trend rate;
- expected progress against deadline;
- On Track / Behind / Overdue / Completed status.

Goal types already include weight, waist, exercise PR and workout frequency.

### Locked mockup differences

The approved Goals screen requires:

- summary counters for Active / On Track / Needs Attention / Completed;
- richer goal cards with Current → Target, percentage, graph and directional state;
- explicit visual states: Moving Closer, On Track, Moving Away, Insufficient Data;
- completed-goal presentation;
- dedicated Create Goal flow:
  **Select Exercise → Set Target → Set Deadline → Review Goal → Goal Created**;
- goal type selection for Exercise PR, Waist, Weight, Frequency.

The current implementation uses a single editor dialog with type/target/baseline/deadline fields.

### Implementation classification

**UI/state-flow refactor while preserving `GoalCalculator` and existing persistence.** No LLM/AI insight layer is required.

---

## 07 — Profile + Settings

**Source:** `ProfileScreen.kt`, `ProfileViewModel.kt` and backup/account infrastructure  
**Status:** FUNCTIONALLY PRESENT / MAJOR INFORMATION-ARCHITECTURE GAP

### Existing strengths

The current screen already supports:

- personal information;
- body weight/waist/BMI;
- Google account state;
- Google Drive authorization;
- local export/import;
- Google Drive backup;
- Google Drive restore;
- workout settings;
- exercise library entry.

### Locked mockup differences

The approved design changes Profile from a large combined settings screen into a Profile hub with dedicated destinations:

- Personal Information;
- Body & Progress;
- Goals;
- Workout Settings;
- Units;
- Appearance;
- Backup & Data;
- Google Account.

Backup & Data also gets a dedicated screen with explicit last-backup status, local export/import, Drive backup/restore, included-data explanation, and success/restore confirmation states.

### Implementation classification

**Navigation + information-architecture refactor**, while preserving the existing backup/account engines. This should not be treated as a simple restyle of the current monolithic screen.

---

## 08 — History

**Source:** `HistoryScreen.kt`, `HistoryViewModel.kt`, `WorkoutDetailsScreen.kt`, `WorkoutDetailsViewModel.kt`  
**Status:** STRONG FUNCTIONAL BASE / VISUAL + IA REFINEMENT

### Existing strengths

Current History already supports:

- List / Calendar tabs;
- search;
- filters;
- program/day/date filtering;
- PR-only filtering;
- sorting;
- calendar navigation;
- daily workout selection;
- monthly recap;
- yearly recap;
- workout details.

### Locked mockup differences

The approved History workflow emphasizes:

**Dashboard Recent History → History List → Workout Details**

and:

**Calendar View (Day) → Monthly Summary → Yearly Summary**

The visual hierarchy, daily-selection presentation, summary cards, muscle-group presentation, and period comparison treatment need to match the approved reference. History must remain outside bottom navigation.

### Implementation classification

Mostly **UI/presentation refactor**, with targeted navigation cleanup and calendar/day interaction refinement.

---

# Cross-screen implementation strategy

## Reuse first

Preserve the established data/logic layers unless the reconciliation proves a real requirement to change them:

- Room entities/DAOs;
- repositories;
- workout session state;
- workout logging state;
- deterministic Progress analytics;
- deterministic GoalCalculator;
- Personal Records identity semantics;
- backup/restore services and repositories;
- Google account/Drive integration.

## New shared UI foundation

Phase 6 should establish a reusable IronLog design system rather than manually restyling every screen independently:

- dark surface/background tokens;
- primary purple gradient/solid action treatment;
- success/warning/error/info tokens;
- card/container styles;
- typography hierarchy;
- icon containers and iconography rules;
- spacing/radius tokens;
- buttons and segmented controls;
- progress indicators;
- charts/data visualization styling;
- empty states;
- bottom navigation;
- common top bars;
- micro-interaction hooks.

## No LLM dependency

The Phase 6 mockups contain "Insights", trend labels, tips and goal-status language, but the current codebase already provides deterministic analytics/status calculations. These UI elements should be driven by deterministic local data/rules unless a future product decision explicitly authorizes AI.

## Android + iOS

The locked Phase 6 direction is intended for both Android and iOS. Shared information architecture and semantics should remain platform-neutral; platform-specific navigation, system dialogs/sheets, date pickers, text input, accessibility and touch-target conventions may differ where necessary.

# Proposed implementation grouping

This is a planning recommendation, not implementation authorization.

### Group A — Shared Phase 6 UI foundation + navigation

- IronLog theme/design tokens;
- reusable cards/buttons/chips/empty states;
- five-item bottom navigation;
- History-as-Dashboard-child behavior;
- workout action/resume behavior without Workout bottom-nav destination.

### Group B — Dashboard + Programs

- Dashboard redesign;
- Programs list/actions;
- Workout Days;
- Exercise Prescription;
- Add Exercise Library presentation.

### Group C — Workout engine presentation

- Start Workout flow;
- active workout;
- rest timer presentation;
- progress/paused/background states;
- finish confirmation;
- summary/PR/comparison presentation.

### Group D — Add Log

- dedicated Date-first workflow;
- shared workout/logging components;
- historical summary/comparison/completion states.

### Group E — Progress + Goals

- Progress story layout;
- deterministic insights presentation;
- Goals main screen;
- Create Goal workflow;
- goal state visualization.

### Group F — Profile + History

- Profile hub and dedicated settings destinations;
- Backup & Data screen and success/restore states;
- History list/calendar/day/month/year presentation.

# Key risks

1. **Navigation regression:** removing Workout from bottom navigation must not break active-session resume.
2. **Workout-state regression:** visual refactoring must not duplicate or fork the existing workout engine.
3. **Historical logging semantics:** Add Log must create historical data correctly without pretending it is a live session.
4. **Backup safety:** Profile/Backup UI changes must preserve existing validation-before-destruction and transactional restore guarantees.
5. **Analytics correctness:** new charts/cards must consume existing deterministic aggregates rather than recomputing inconsistently in UI.
6. **Identity correctness:** Progress/PR/History presentation must preserve canonical exercise identity semantics.
7. **Cross-platform drift:** Android-specific UI changes must not redefine product semantics that iOS will need to implement.

# Acceptance direction for the future implementation PR

Before implementation is considered complete, each approved mockup must be verified against the running app for:

- navigation and back-stack behavior;
- visual hierarchy/layout;
- states and empty states;
- loading/error states;
- touch targets and accessibility;
- deterministic data correctness;
- workout/goal/history state transitions;
- regression of backup/restore and existing core data behavior;
- Android build/tests plus real-device verification.

No implementation PR should be considered complete from source inspection alone; tangible app behavior and repository/build evidence remain required.

# Current decision

**Phase 6 design is locked. Source reconciliation is complete enough to define implementation boundaries. Implementation itself remains NOT AUTHORIZED until the Project Owner explicitly approves the Phase 6 implementation scope and a new active PR specification is created.**
