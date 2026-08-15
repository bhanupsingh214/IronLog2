# IronLog — Profile Product Architecture

**Status:** PLANNING / NOT IMPLEMENTATION AUTHORITY
**Date:** 2026-08-16
**Purpose:** Holistic product and data-planning reference for the future IronLog Profile experience.

## 1. Planning intent

IronLog's current Profile screen is primarily Account & Settings. The long-term product direction should evolve it into a coherent personal hub without turning it into an unnecessarily large registration form.

This document defines the intended information architecture and planning boundaries. It does **not** authorize implementation.

Implementation authority remains `11_ACTIVE_PR_SPEC.md`.

## 2. Profile information architecture

The Profile experience should be organized into six logical areas:

### About You

Potential user-owned identity/profile information:
- display name;
- optional profile image/avatar;
- optional sex field;
- date of birth;
- optional country/region where justified.

Date of birth is included in the Phase 5B profile foundation because age can determine which BMI interpretation is appropriate. It should be collected only with a clear product purpose and should not be used for unrelated app behavior.

All other fields should be optional unless a future feature establishes a clear requirement.

### Body & Progress

Potential physical-progress information:
- height;
- current/latest weight;
- dated weight history;
- BMI as a derived metric;
- waist measurement;
- future body measurements;
- body-progress trends.

This is the primary planned home for the Phase 5B Body Progress capability.

### Goals

Future goal-oriented information may include:
- primary training goal;
- target weight;
- future strength or training targets;
- other explicit user goals.

Goals are intentionally separated from Body Progress so goal mechanics do not own the underlying measurement data.

### Training Profile

Future personalization may include:
- training experience;
- training frequency/preferences;
- equipment access;
- other training-profile attributes only where they have a concrete product purpose.

Existing workout settings should not be duplicated merely because they are related to training.

### Account & Data

Existing account/data functions belong here:
- Google account state;
- Google Drive authorization;
- local export/import;
- Google Drive backup/restore;
- backup status.

### Preferences

Existing and future app preferences may include:
- unit preferences;
- workout settings;
- notifications;
- appearance/theme;
- language or other app-level preferences where later supported.

## 3. Data classification

Profile architecture should distinguish:

1. **Identity** — who the user is.
2. **Body & Progress** — measurements and derived physical-progress metrics.
3. **Training & Preferences** — how the user trains and configures IronLog.
4. **Account & Data** — authentication, backup, and portability controls.

Not every piece of information shown in Profile should be stored in the same subsystem.

## 4. Storage direction

This is a planning rule pending implementation-level schema inspection:

- dated body measurements should be modeled as persistent local data, not merely a single preference value;
- profile attributes that affect derived calculations, including date of birth where required for age-dependent interpretation, should have durable local storage;
- UI/display preferences may remain appropriate for preference storage;
- body-progress data should participate in `.ironlog` backup/restore so portability does not silently omit personal progress data.

The exact Room entities, columns, keys, indexes, DAO methods, migrations, and backup DTOs remain **TBD until implementation authorization and source inspection**.

## 5. Unit strategy

### Height

Preferred user-facing entry/display:
- feet + inches.

Metric alternative:
- centimeters.

Internal calculations should use a canonical metric representation so display-unit changes do not alter stored physical meaning.

### Weight

Preferred user-facing unit:
- kilograms.

Alternative:
- pounds.

Internal calculations should use canonical kilograms.

## 6. Sex and sex-specific logic

Sex should be optional and should not be required merely to use IronLog.

Potential UI values require final product/privacy review; the current planning direction is to avoid conflating biological sex with gender identity.

Sex must affect app behavior only where an explicit, evidence-based calculation or reference range requires it. Workout logging, workout history, volume calculations, PR calculations, and general navigation should not become sex-dependent without a concrete reason.

## 7. Date of birth and age planning

Date of birth should be stored as a durable profile attribute rather than storing a mutable age value.

Age should be derived locally from date of birth when needed. This keeps age current without requiring annual profile edits.

Age is included in Phase 5B primarily because BMI interpretation is age-dependent. The implementation specification must distinguish adult BMI interpretation from BMI-for-age interpretation for users who are not adults, rather than applying adult BMI cutoffs universally.

The exact age boundaries, pediatric reference source, adult reference source, and presentation rules must be verified and recorded in the implementation specification before release.

Date of birth should not automatically drive unrelated training, UI, or personalization behavior without a documented product purpose.

## 8. BMI planning

BMI should be a deterministic/local derived metric based on canonical weight and height.

For adults, the planned Indian/Asian-Indian reference scheme is:
- `<18.0` — underweight;
- `18.0–22.9` — normal;
- `23.0–24.9` — overweight;
- `≥25.0` — obesity.

For users who are not adults, IronLog must **not** apply adult BMI cutoffs. The implementation specification must identify the appropriate BMI-for-age reference method and source before any such interpretation is released.

The exact reference source/version must be recorded in the implementation specification before release. BMI should be presented as a screening/derived metric, not a medical diagnosis, and should not be used to generate medical treatment advice.

## 9. Measurement planning

Weight should be represented as dated measurements rather than a mutable field attached to a workout session.

Initial body-measurement scope should consider:
- weight;
- waist.

Additional measurements such as chest, arms, thighs, or other circumference measurements should remain future/planned until their UX, storage, and evidence requirements are explicitly defined.

## 10. Privacy and data minimization

IronLog should collect only profile information with a clear product purpose.

Date of birth is included because it has a concrete role in age-dependent BMI interpretation; it should not become a general-purpose personalization signal by default.

Avoid collecting unnecessary sensitive personal or medical information. Optional fields should remain optional unless a later feature has a documented requirement.

Body/profile calculations should be local and deterministic. No LLM dependency is planned for ordinary profile calculations, BMI, trends, or measurement aggregation.

## 11. Backup and account/data ownership principle

Body/profile data is user-owned personal data. Future implementation must preserve:
- the current local-dataset ownership model;
- local-first behavior;
- backup/export inclusion where the data is part of the portable user dataset;
- restore correctness and transactional safety;
- preservation of existing workout data.

Phase 5B must **not** introduce a new multi-account local Room architecture. The current app has one local training dataset per installation; Google account state is the cloud identity/authorization layer rather than a Room ownership key for every local record.

The existing `.ironlog` contract currently covers library, programs, workout history, PRs, and workout settings; it does not currently include body/profile data. Any future extension must be deliberate and versioned rather than silently changing the contract.

## 12. Profile UX principle

Profile should be a central personal hub, not a giant form.

The top-level experience should provide a compact personal summary and navigation into focused sections. Users should be able to log workouts without being forced to complete every profile field.

A future high-level structure is:

```text
PROFILE
├── About You
├── Body & Progress
├── Goals
├── Training Profile
├── Account & Data
└── Preferences
```

Exact screen layout, navigation, and component hierarchy remain TBD.

## 13. Phase relationship

Phase 5A — Progress & History Presentation is complete and intentionally excluded body-weight/body-measurement history.

The current product planning recommendation is to make **Phase 5B — Profile Foundation & Body Progress** the next candidate, followed by:

- Phase 5C — Goals & Engagement;
- Phase 5D — AI Assistance.

This sequencing is a planning recommendation only until the Project Owner explicitly approves and locks the next implementation specification.

## 14. Non-goals for the initial Body Progress capability

Unless separately authorized, the first body-progress implementation should not include:
- calorie tracking;
- food/nutrition logging;
- medical diagnosis;
- treatment recommendations;
- prescription or medication advice;
- AI-generated health coaching;
- social sharing;
- achievement/rank mechanics;
- full goal-management mechanics;
- multi-account local database support.

## 15. Required pre-implementation audit

Before implementation is authorized, inspect and document:
1. current Room entities and schema version;
2. migration strategy and schema ledger;
3. current local-dataset/account ownership boundaries;
4. existing DataStore preferences;
5. backup DTO/payload and restore mapping;
6. Profile UI/ViewModel flow;
7. existing tests and instrumentation conventions;
8. exact evidence/source for adult BMI and any BMI-for-age and sex-specific reference ranges;
9. date-of-birth validation and age-calculation rules;
10. acceptance criteria and regression coverage.

No implementation should infer missing signatures or schema relationships from this planning document.
