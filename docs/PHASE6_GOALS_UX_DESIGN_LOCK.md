# IronLog Phase 6 — Goals UX Design Lock

**Status:** APPROVED / LOCKED  
**Date:** 2026-08-16

## Approved mockup

The latest approved Goals mockup is the visual reference for Phase 6 Goals implementation. It establishes the premium dark-first direction and the intended information hierarchy for the Goals destination.

## Locked visual / UX direction

- Premium **dark-first** IronLog visual theme.
- Goals should feel like a motivating destination for targets and progression, not a CRUD/database screen.
- A compact top summary communicates **Active Goals, On Track, Needs Attention, and Completed** at a glance.
- Active goal cards prioritize visual trajectory: current value → target, completion, status, and an aesthetic mini trend/sparkline.
- Progress should be understandable in one glance; avoid relying primarily on static bars and large standalone percentages.
- Goal cards use state-aware visual accents:
  - Purple — Moving Closer / primary progress.
  - Green — On Track / positive / Completed.
  - Amber/orange — Moving Away / Needs Attention.
  - Neutral — Insufficient Data.
  - Red is reserved for destructive or serious negative states.
- Goal types have appropriate visualizations:
  - **Exercise PR:** strength trend, current PR, target marker, and direction toward target.
  - **Weight / Waist:** measurement trend with target marker and current distance to target.
  - **Workout Frequency:** sessions/day or period visualization showing completion against target and whether the target was met/exceeded.
- Completed goals remain discoverable in a visually quieter completed section.
- Avoid excessive nested cards and repetitive text.
- Goal creation uses a guided multi-step flow rather than a generic form-first dropdown:
  1. Select goal type / exercise.
  2. Set target.
  3. Optional deadline.
  4. Review goal and trajectory.
  5. Create / success state.
- Exercise PR creation exposes the canonical exercise and current PR directly.
- Workout Frequency creation explicitly captures sessions and period.
- Deadline selection uses an IronLog-consistent dark date-picker treatment.
- Edit Goal clearly identifies the selected goal and its current/target/deadline context.
- Delete confirmation identifies the specific goal and preserves the existing safety intent.
- Animation is subtle and data-driven: trend-line drawing/transition, milestone movement, state transitions, and restrained completion/improvement emphasis.
- Trend visuals are deterministic and explainable. No LLM-generated goal insights are required.

## Navigation lock

The permanent bottom navigation is:

**Dashboard · Programs · Progress · Goals · Profile**

**Workout is NOT a persistent bottom-navigation destination.** Workout remains accessible through Dashboard / active workout flows as already approved.

## Analytics / implementation prerequisites

- Preserve deterministic goal calculations and target-aware directional trend logic.
- Define workout-frequency window semantics before implementation.
- Ensure trend series use correct historical data and canonical exercise identity.
- Never invent a trend when data is insufficient; show an explicit insufficient-data state.

## Lock boundary

This document locks the **approved Goals mockup and UX/visual direction only**. It does **not** authorize Kotlin/Compose, navigation, database, repository, ViewModel, analytics, or animation implementation. Implementation requires separate Phase 6 approval after the relevant code-level audit and planning.
