# IronLog Phase 6 — Goals UX Design Lock

**Status:** APPROVED / LOCKED

**Scope:** Goals screen visual/UX direction only. No implementation authorization is implied.

## Approved direction

- Dark-first IronLog visual theme, consistent with the approved Phase 6 Dashboard, Workout, Programs, and Progress directions.
- Goals should feel like a motivating destination for targets and progression, not a CRUD/database screen.
- The screen should surface goal state at a glance: active goals, on-track goals, goals needing attention, and completed goals.
- Goal cards should visually communicate *movement toward the target*, not rely primarily on a static progress bar and percentage.
- Use compact, aesthetic data visualizations such as mini trend/sparkline graphs, directional indicators, milestone/target markers, and state-aware visual accents where data supports them.
- Trend visuals must remain deterministic and explainable; no LLM-generated goal insights are required.
- Goal-specific presentation should adapt to goal type:
  - Exercise PR: historical strength trend, current PR, target marker, and direction toward target.
  - Weight/Waist: measurement trend with target line/marker and current distance to target.
  - Workout Frequency: session/day or period visualization showing completed sessions against the target and whether the target has been met/exceeded.
- State should be immediately recognizable through visual treatment: On Track, Moving Closer, Moving Away, Insufficient Data, Behind, Overdue, and Completed as supported by the deterministic goal calculator.
- Recent/active goals needing attention should receive stronger visual priority; completed goals should remain discoverable but visually quieter.
- Goal cards should avoid excessive nested cards and repetitive text such as large standalone percentage labels.
- Create Goal should use a guided, visually clear goal-type selection rather than a generic form-first dropdown experience.
- Goal creation should adapt its fields to the selected goal type and provide a concise review/preview before creation where appropriate.
- Exercise PR creation should expose the canonical exercise and current PR directly rather than making the user manually reason about a baseline.
- Workout Frequency creation should explicitly ask for sessions and period (week/month) rather than using a generic target field.
- Deadline selection should use an IronLog-consistent date-picker experience rather than the current legacy-looking teal calendar dialog.
- Edit Goal should clearly identify the goal being edited and show relevant current/target/deadline context.
- Delete confirmation should identify the specific goal and preserve the existing safety intent.
- Completed goals should remain available in a completed/history section rather than disappearing from the user's record.
- Normal-app bottom navigation uses five destinations: Dashboard, Programs, Progress, Goals, Profile. Workout is not a persistent bottom-nav destination.
- Animation should be subtle and data-driven: trend lines drawing/transitioning, progress/milestone movement, state transitions, and restrained emphasis for newly completed or improved goals.
- Primary IronLog accent remains indigo/purple; green indicates positive/on-track/completed states; amber/orange can indicate attention/moving-away states; red is reserved for destructive/serious negative states; dark neutral surfaces provide the base.

## Analytics/product prerequisites

- Preserve deterministic goal calculations and target-aware directional trend logic.
- Define workout-frequency window semantics explicitly before implementation (calendar period versus goal-relative period).
- Ensure visual trend series use correct historical data and canonical exercise identity.
- Do not invent trends when there is insufficient data; use an explicit insufficient-data state.

## Lock boundary

This document locks the approved Goals UX/visual direction. It does **not** authorize Kotlin/Compose, navigation, database, repository, ViewModel, analytics, or animation implementation. Implementation requires separate Phase 6 approval.
