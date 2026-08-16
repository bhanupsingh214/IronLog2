# IronLog — Phase 6A Implementation Specification

**Status:** DRAFT — implementation not yet authorized
**Phase:** 6A — Shared Design System + Navigation
**Design source:** IronLog Figma file `7eoWIBepB351Sv8XlZScfQ`
**Reference archive:** `project-resources/phase-6/approved-mockups/`
**Target platform:** Android / Jetpack Compose; structure must remain suitable for later iOS implementation

## Objective

Establish the shared IronLog Phase 6 visual foundation and root navigation before modernizing individual feature screens. The implementation must reuse existing product/data behavior and introduce no LLM dependency.

## Design source of truth

Use the existing IronLog Figma file as the primary visual/design-system source. Use the eight GitHub-approved mockups as locked visual references. Figma may be used to create/edit reusable implementation-side components and tokens, but approved mockup references must not be silently changed.

Figma foundation currently specifies:
- Background / Surface 0: `#050811`
- Surface 1: `#0B1020`
- Surface 2: `#121A2A`
- Primary: `#6C3DFF`
- Primary Light: `#A78BFA`
- Accent: `#00D4FF`
- Success: `#22C55E`
- Warning: `#F59E0B`
- Danger: `#EF4444`
- Typography: Inter
- Heading sizes: 32 / 28 / 24
- Body: 16 / 14
- Caption: 12
- Touch targets: at least 44 logical points
- States must not rely on color alone
- Android: Compose + Material semantics where useful
- iOS: SwiftUI + HIG where appropriate

## Scope

### 1. Shared theme/tokens
Create the IronLog Compose theme foundation for the approved dark visual language. Map the Figma tokens into named project-level tokens rather than scattering raw colors throughout screens.

Cover at minimum:
- surfaces/backgrounds;
- primary and secondary accents;
- semantic success/warning/danger states;
- typography hierarchy;
- common spacing/radius/elevation conventions;
- content/icon emphasis levels;
- accessible disabled/focus/pressed states.

### 2. Shared UI primitives
Establish reusable primitives needed by later Phase 6 screens, prioritizing components that appear across the approved mockups:
- primary/secondary action buttons;
- cards/surfaces;
- section headers;
- chips/status badges;
- text/input treatments;
- progress indicators;
- list rows;
- navigation items;
- common empty/state messaging;
- common icon containers.

Do not build speculative components that are not needed by the approved screens.

### 3. Root navigation
Replace the current six-item bottom navigation with the locked five-item navigation:

**Dashboard · Programs · Progress · Goals · Profile**

Workout must not remain a bottom-nav destination. Workout execution is entered as a focused workflow from the appropriate actions.

History must not become a bottom-nav destination. It remains reachable from Dashboard / Recent History.

Navigation must preserve existing ViewModel/data behavior and deep workflow entry points where possible.

### 4. Navigation states
Implement the visual states required by the approved bottom navigation:
- active destination;
- inactive destination;
- touch/pressed state;
- disabled/unavailable state where applicable;
- accessibility semantics/content descriptions.

Navigation must preserve adequate touch targets and must not communicate state by color alone.

### 5. Figma implementation bridge
Use the connected Figma file during implementation to inspect and refine the design system. Where reusable Figma components correspond to reusable Compose components, consider Code Connect mappings after the component structure is stable. Do not create mappings merely for the sake of having mappings.

## Reuse / preservation requirements

- Preserve existing Room/data models.
- Preserve existing deterministic analytics and goal calculation logic.
- Preserve existing workout/session engine behavior.
- Preserve existing backup/restore architecture.
- Do not introduce an LLM or AI service for Phase 6A.
- Prefer adapting existing Compose components/ViewModels over rewriting working business logic.

## Explicit non-goals

- No individual Dashboard redesign implementation in 6A.
- No Programs redesign implementation in 6A.
- No Start Workout redesign implementation in 6A.
- No Add Log redesign implementation in 6A.
- No Progress redesign implementation in 6A.
- No Goals redesign implementation in 6A.
- No Profile redesign implementation in 6A.
- No History redesign implementation in 6A.
- No database/schema redesign.
- No new analytics model.
- No LLM/AI integration.
- No iOS implementation in this PR.

## Risks and mitigations

**Risk: theme tokens diverge from Figma.**
Mitigation: derive named tokens from the Figma foundation and perform visual comparison before screen work proceeds.

**Risk: navigation refactor breaks workflow entry points.**
Mitigation: inspect existing navigation routes and ViewModel state flows before changing route ownership; regression-test all existing workout entry points.

**Risk: shared components become over-generalized.**
Mitigation: implement only primitives required by approved Phase 6 references.

**Risk: visual modernization accidentally changes product behavior.**
Mitigation: keep Phase 6A UI-focused and preserve existing repositories/ViewModels/calculators.

## Acceptance criteria

1. IronLog Compose theme uses the approved Phase 6 dark foundation and named tokens.
2. Shared primitives needed by subsequent Phase 6 screens exist and are reusable.
3. Root bottom navigation contains exactly five destinations: Dashboard, Programs, Progress, Goals, Profile.
4. Workout is no longer a bottom-nav destination.
5. History is not a bottom-nav destination.
6. Existing navigation/workflow entry points continue to function.
7. Accessibility semantics and minimum touch-target requirements are preserved.
8. No business/data model changes are required for Phase 6A.
9. No LLM/AI dependency is introduced.
10. Figma and Git mockup references are used for visual verification before 6A is considered complete.

## Verification plan

- `git diff --check`.
- Clean Android build.
- Existing relevant unit tests.
- Navigation regression checks for Dashboard, Programs, Progress, Goals, Profile, Start Workout and Add Log entry points.
- Verify History remains reachable from Dashboard.
- Verify Workout remains reachable as a focused workflow.
- Visual comparison against the Figma design-system foundation and approved Dashboard/bottom-navigation reference.
- Confirm no raw-token proliferation in the new shared theme/components.

## Implementation authorization

This document defines the intended Phase 6A scope. **Code implementation requires explicit implementation approval after review of this specification.**
