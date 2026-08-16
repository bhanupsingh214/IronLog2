# Phase 6 — Workout Experience Design Lock

**Status:** DESIGN LOCKED — implementation not approved
**Date:** 2026-08-16

## Scope

This document records the Owner-approved visual/UX direction for the Phase 6 active-workout journey. It is a design decision only. No application implementation is authorized by this document.

## Locked Journey

Dashboard → Choose Workout → Active Workout → Set Logging → Rest Timer → Workout Progress → Finish Confirmation → Workout Summary

The experience must also account for:
- partially completed exercises
- skipped exercises
- warmup sets
- working sets
- backoff sets
- PR achievements
- paused workouts
- notes
- background-active workouts
- discard confirmation
- finish confirmation

## Locked Direction

The Workout experience should feel like an immersive, premium training mode rather than a generic Material form flow.

Core principles:
- clear hierarchy and one primary action at a time
- reduce nested-card/box overload
- large, thumb-friendly set inputs
- previous performance presented as actionable training context
- rest timer treated as a major interaction state
- strong visual distinction between normal app mode and active workout mode
- state-driven motion and feedback rather than decorative animation
- meaningful visual feedback for completed sets and PRs
- concise workout progress representation
- premium completion/summary experience
- preserve accessibility, touch targets, readability, and performance

## Visual Direction

The approved storyboard establishes:
- dark-first premium workout presentation
- deep surfaces with strong contrast
- purple/indigo primary action language
- green success/completion language
- amber warning/attention language
- red destructive language
- restrained celebratory accents for PR/completion
- bold workout metrics and strong numeric hierarchy
- rounded but purposeful surfaces
- minimal persistent navigation during active workout
- consistent iconography and motion language

The broader Phase 6 design system may refine exact colors, typography, spacing, and motion tokens, but it must remain consistent with this approved direction unless separately re-approved.

## Navigation

The previously approved Dashboard navigation lock remains in force:
- Dashboard
- Programs
- Progress
- Goals
- Profile

The Workout destination is removed from persistent bottom navigation. Workout is a contextual action/state flow entered from Dashboard or other relevant actions.

During an active workout, the persistent bottom navigation should not dominate the experience; the workout should use an immersive task-focused presentation.

## Implementation Boundary

This lock does **not** authorize:
- Compose implementation
- navigation changes
- database changes
- repository changes
- ViewModel refactors
- design-system code changes
- animation implementation

Those require the Phase 6 implementation specification and explicit Owner approval.

## Reference

Owner approved the generated high-fidelity Phase 6 workout-flow storyboard after review in the project conversation on 2026-08-16.
