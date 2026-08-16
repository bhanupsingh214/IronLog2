# IronLog Phase 6 — Progress UX Design Lock

**Status:** APPROVED / LOCKED

**Scope:** Progress screen visual and UX direction only. No implementation authorization is implied by this document.

## Approved direction

- Dark-first IronLog visual theme, consistent with the approved Phase 6 Dashboard, Workout, and Programs directions.
- Progress header moved upward to remove excessive empty space above the title.
- Five-item normal-app bottom navigation: Dashboard, Programs, Progress, Goals, Profile. Workout is not a persistent bottom-nav destination.
- Progress is a vertically scrollable training-intelligence experience with a clear narrative rather than a collection of disconnected statistic cards.
- Primary narrative: Training → Recent Wins → Strength Progress → Workload → Training Balance → Active Goals → Insights / deeper records.
- Strong emphasis on meaningful trends and comparisons rather than raw lifetime totals.
- Recent personal records are surfaced as motivating wins without excessive gamification.
- Strength Progress uses full historical progression data, with Weight / 1RM switching and separate PR indication where appropriate.
- Workload uses time-range controls and an interpretable volume trend rather than redundant summary cards.
- Training Balance presents muscle-group distribution cleanly and calmly.
- Active Goals are integrated into the Progress story but remain secondary to actual training progress.
- Charts should use restrained, data-driven animation: line drawing, bar growth, smooth range transitions, and subtle PR emphasis.
- Purple/indigo remains the primary IronLog progress/action accent; green indicates positive change/success; red is reserved for destructive or negative states; neutral dark surfaces carry supporting information.
- Reduce nested-card clutter, oversized chart containers, duplicated summaries, and clipped range selectors.
- Progress should remain deterministic and explainable; no LLM-generated analytics are required for these features.

## Analytics prerequisites identified during audit

Before implementation, the analytics layer should be reviewed so the UI is backed by correct contracts. In particular:

- Strength charts should retain complete historical series rather than only PR/new-best points.
- Volume date bucketing should use correct local calendar-day semantics.
- Time-filter semantics should be explicitly defined for each range.
- Muscle-group distribution should remain reactive to underlying training changes.
- Monthly/yearly recap calculations should be checked for date/denominator correctness, including leap years.
- Larger historical aggregates should be evaluated for SQL-side aggregation where appropriate.

## Lock boundary

This document locks the approved UX/visual direction. It does **not** authorize Kotlin/Compose, navigation, database, repository, ViewModel, analytics, or animation implementation. Implementation requires separate Phase 6 approval.
