# IronLog Phase 6 — Programs UX Design Lock

**Status:** LOCKED — design only
**Phase:** 6
**Implementation:** NOT APPROVED

## Scope
This lock covers the Programs experience and Exercise Library picker redesign reviewed during Phase 6 pre-planning.

## Locked direction

### Programs
- Programs is the user's training-plan area, not a generic CRUD list.
- The active/current program receives stronger visual hierarchy.
- Program cards communicate useful planning context such as training days, exercise count, and next workout where the existing data contract supports it.
- Program actions remain available through a clean contextual action surface, with destructive actions visually separated.
- Programs uses the Phase 6 five-item persistent navigation: Dashboard, Programs, Progress, Goals, Profile.
- Workout is not a persistent bottom-navigation destination.

### Workout days
- Program structure is presented as an intentional training-plan hierarchy.
- Workout days expose useful summary information without unnecessary nested cards.
- Enabled/disabled day state must have clear semantics and accessible labeling.
- Reordering should support a modern drag/reorder interaction where appropriate, while retaining precise actions where required.

### Day exercises
- Exercises are presented as an ordered training sequence.
- Exercise summaries emphasize exercise identity and prescription: sets, rep range, rest, and relevant metadata.
- Nested-card clutter should be reduced in favor of hierarchy, spacing, typography, and selective surfaces.
- Exercise actions retain edit prescription, disable, duplicate, reorder, and delete capabilities as supported by the existing domain model.

### Exercise Library / Add Exercise
- The current nested bottom-sheet/list scrolling behavior is considered a UX defect to eliminate in the redesign.
- The preferred direction is a stable full-height/full-screen Exercise Library selection experience, with the header/search area fixed and the exercise list owning vertical scrolling.
- Search and filtering remain prominent.
- Library identity remains the source of truth; UI must not create duplicate exercise identities or bypass repository/domain rules.
- Added/selected state must be visually obvious and unambiguous.
- Exercise metadata can include muscle group/equipment where available without overloading the picker.

## Visual language
- Derived from the approved Dashboard and Workout UX locks.
- Premium, modern, focused IronLog visual identity.
- Calm organization for Programs; more intense/immersive treatment remains reserved for active Workout.
- State-driven motion only where it improves comprehension.
- Consistent success, warning, destructive, and neutral semantics.

## Explicitly not approved by this document
- No Compose implementation.
- No navigation changes.
- No database schema changes.
- No repository/domain refactor.
- No animation implementation.
- No replacement of existing business rules.

Implementation requires a separate explicit Phase 6 implementation approval after the complete audit and planning process.
