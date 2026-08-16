# IronLog — Phase 6B Implementation Specification

**Status:** DRAFT — implementation starting
**Phase:** 6B — Dashboard + Bottom Navigation integration
**Design source:** approved Phase 6 Dashboard mockup in `project-resources/phase-6/approved-mockups/`
**Secondary visual reference:** IronLog Figma file `7eoWIBepB351Sv8XlZScfQ`

## Scope

Implement the approved Dashboard visual direction on top of the Phase 6A shared design system and five-destination navigation.

### Required dashboard hierarchy

1. Greeting / profile header
2. Primary actions: Start Today's Workout and Add Previous Log
3. Today's Workout state card
4. Current Program card
5. Weekly Volume visualization
6. Quick Stats
7. Recent History
8. Personal Records
9. Persistent five-destination bottom navigation

## Constraints

- Preserve existing dashboard state, calculations, repository/data behavior, and workout/log entry points.
- Do not introduce an LLM dependency for dashboard analysis or presentation.
- Do not add Workout as a bottom-navigation destination.
- Workout remains an internal workflow route.
- Use Phase 6A tokens/components instead of introducing screen-specific styling constants where possible.
- Figma is an ambiguity resolver only; it is not a required implementation step.
- No image generation is required.

## Visual requirements

- IronLog dark premium foundation.
- Purple primary actions with clear hierarchy.
- Strong card hierarchy and consistent spacing.
- Quick Stats presented as compact scan-friendly cards.
- Weekly volume shown as a compact trend visualization.
- Recent history provides workout context rather than only timestamps.
- Personal Records use clear achievement/status treatment.
- Five-item bottom navigation remains visible and consistent with Phase 6A.

## Validation

Validate against the approved Dashboard mockup and ensure existing dashboard interactions remain wired. Local Android build/install validation remains required because GitHub Actions are not currently configured for the project.
