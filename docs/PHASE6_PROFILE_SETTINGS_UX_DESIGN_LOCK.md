# Phase 6 — Profile & Settings UX Design Lock

**Status:** APPROVED / LOCKED  
**Phase:** 6  
**Scope:** Profile, Settings, account identity, body-entry UX, Backup & Data UX  
**Implementation status:** Design only — implementation not yet authorized

## Approved direction

The Profile destination is a premium dark-mode hub rather than a long generic settings form. It separates personal identity, body/progress access, account, preferences, and data management into clear destinations.

### Navigation

The final Phase 6 bottom navigation is locked to:

- Dashboard
- Programs
- Progress
- Goals
- Profile

**Workout is NOT a bottom-navigation destination.** Active workouts are entered from the Dashboard / relevant workout actions, consistent with the previously locked navigation decision.

## Personal identity

- The user explicitly enters an IronLog display name during initial personal-information setup.
- The IronLog display name is user-owned and independent from Google account metadata.
- Google sign-in must not overwrite the user's IronLog display name.
- Google account identity is displayed separately as connection/account information.
- Signed-out state shows a clear Sign in with Google action.
- Signed-in state shows the connected Google email/account.
- Profile identity should be persisted as part of the user's local profile and included in backup/restore.

## Personal information

The redesigned personal-information experience includes:

- Name
- Sex
- Date of birth
- Height
- Weight/body progress access
- Waist measurement

Canonical storage remains metric/local-first; display and input units are controlled by user preferences.

## Body measurements

- Preserve historical weight and waist measurements.
- Weight remains stored canonically in kg.
- Waist remains stored canonically in cm.
- Waist input must support both cm and inches.
- Height input must support cm and ft/in.
- Unit conversion occurs at the UI/domain boundary; switching units must not alter canonical stored values.
- Body & Progress is presented as a visually engaging destination with trend-oriented summaries rather than a plain settings list.

## Settings structure

Profile should act as a hub with focused destinations, including:

- Personal Information
- Body & Progress
- Goals
- Workout Settings
- Units
- Appearance
- Backup & Data
- Google Account
- Exercise Library / other existing profile utilities as appropriate

## Appearance

- IronLog owns the visual theme rather than allowing Android dynamic colors to arbitrarily redefine the product palette.
- Phase 6 visual direction is premium dark-first, with controlled IronLog colors, gradients, hierarchy, and purposeful animation.
- Theme preference should eventually be persisted as an explicit user setting rather than relying solely on `isSystemInDarkTheme()`.

## Units

Introduce a persisted unit preference while retaining canonical metric storage:

- Weight: kg / lb
- Height: cm / ft+in
- Waist: cm / in

## Backup & Data

Backup and restore are first-class user-data experiences.

### Actions

**Local / device**
- Export Backup
- Import Backup

**Google Drive**
- Back Up to Google Drive
- Restore from Google Drive

### Completion feedback

Successful operations must provide clear, persistent-enough confirmation rather than relying only on a disappearing snackbar.

Examples:

- Local backup complete
- Google Drive backup complete
- Local restore complete
- Google Drive restore complete

Success feedback should show useful details where available, such as timestamp, workout/session count, program count, and whether profile/body data was included.

The Backup & Data destination should also show a persistent last-backup state.

### Terminology

Use clear action semantics:

- Export Backup = save an `.ironlog` file to the device
- Import Backup = restore from an `.ironlog` file
- Back Up to Google Drive = upload latest backup
- Restore from Google Drive = download and restore latest backup

### Restore safety

Existing destructive restore confirmation remains required. The redesigned UI should make the irreversible nature of restore clear without making the experience visually alarming except where appropriate.

## Visual language

The approved Profile/Settings mockup establishes:

- Premium dark background
- Controlled purple/blue primary accents
- Green success states
- Strong visual hierarchy
- Rounded elevated cards
- Compact metric/trend previews
- Clear section grouping
- Purposeful micro-animations
- First-class fitness-app feel
- No generic light Material settings appearance

## Architectural constraints

The existing local-first architecture remains the foundation.

Preserve:

- Room-backed profile
- Historical weight/waist storage
- Deterministic calculations
- Local backup/restore
- Google account separate from local dataset ownership
- No LLM dependency for Profile/Settings logic

Phase 6 implementation should avoid unnecessary data-model rewrites. Refactoring ProfileViewModel responsibilities into focused use cases/services may be considered before or during implementation where it reduces coupling.

## Explicitly not locked as implementation details

The following remain implementation-planning decisions until code work is authorized:

- Exact Profile sub-screen/ViewModel decomposition
- Exact DataStore keys/schema for units/theme
- Exact success-card duration/animation
- Exact backup result DTO/state model
- Any Room migration required for display name or preferences

## Approval

This document records the approved Profile & Settings UX direction following screenshot review and code-level audit. The design is locked for implementation planning; **do not implement until Phase 6 implementation is explicitly authorized.**
