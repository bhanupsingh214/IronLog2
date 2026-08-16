# Phase 6 Dashboard UI/UX Design Lock

Status: **APPROVED DESIGN — IMPLEMENTATION NOT AUTHORIZED**

Approved by project owner during Phase 6 discovery on 2026-08-16.

## Scope

This lock applies only to the **Dashboard screen and bottom navigation strip**. It does not authorize implementation or changes to other screens.

## Approved direction

The approved visual direction is the latest reviewed IronLog Dashboard v2.1 mockup from the Phase 6 discovery discussion.

### Dashboard

- Premium dark-first visual direction with restrained neon/gradient brand accents.
- Stronger visual hierarchy between greeting, primary actions, current workout/program context, weekly volume, quick stats, recent history, and personal records.
- Primary action: **Start Today's Workout**.
- Secondary action: **Add Previous Log** for logging a completed/past workout.
- Dashboard should remain the entry point for starting today's workout and adding previous workout logs.
- Weekly volume should become more visual and trend-oriented rather than presenting only a large number.
- Recent History should be scannable, contextual, and provide clear access to the full history.
- Personal Records should be visually prominent but compact, with achievement-oriented treatment.
- Use consistent premium surfaces, typography, iconography, spacing, and semantic color states.
- Motion should be purposeful: action feedback, metric updates, achievement/PR feedback, and transitions should communicate state rather than decorate the UI.

### Bottom navigation

The **Workout** tab is removed from the proposed bottom navigation.

Approved primary destinations:

1. Dashboard
2. Programs
3. Progress
4. Goals
5. Profile

Rationale: workout actions are directly accessible from Dashboard through **Start Today's Workout** and **Add Previous Log**, so a permanent Workout destination is redundant in the primary navigation.

## Navigation interaction requirements

- Five destinations only.
- Active destination must have a strong but restrained visual indicator.
- Touch targets should be first-class/mobile-accessible.
- Navigation transitions should be smooth and consistent with the overall motion system.
- Removing the Workout tab must not remove or weaken access to active-workout functionality; the Dashboard remains the launch point.
- Existing deep links/routes and active-workout state must be audited before implementation.

## Explicit non-goals

This design lock does **not** authorize:

- code implementation;
- navigation graph changes;
- database/schema changes;
- repository changes;
- analytics changes;
- changes to workout business logic;
- redesign of Programs, Progress, Goals, Profile, History, or other screens;
- AI/LLM integration.

## Implementation gate

Implementation requires a separate explicit Phase 6 implementation approval after the full backend/UI audit and screen-by-screen design review are complete.

## Design-system implications to carry forward

The Dashboard lock establishes the initial visual direction for the broader Phase 6 design system, including:

- dark premium surface hierarchy;
- purple/indigo brand accents with restrained supporting semantic colors;
- high-contrast typography hierarchy;
- rounded but controlled surfaces;
- consistent icon treatment;
- compact high-density fitness metrics;
- meaningful micro-interactions and state-driven motion;
- accessible touch targets and readable contrast.

This is a **design decision record**, not an implementation specification for immediate coding.
