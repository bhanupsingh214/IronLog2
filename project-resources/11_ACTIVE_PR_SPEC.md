# IronLog — Active PR Specification

**Documentation version:** v4.1  
**As of:** 2026-08-16  
**Status:** NO ACTIVE IMPLEMENTATION PR  
**Current objective:** None — Phase 5C is complete; Phase 6 requires explicit approval and a new active PR specification  
**Implementation authorization:** NONE CURRENTLY ACTIVE

## 1. Authorization boundary

Phase 5C — Goals & Deterministic Progress Intelligence — was the explicitly approved objective following the completed Phase 5B Profile Foundation & Body Progress work. GitHub #44 has now been reviewed, merged, and its feature branch deleted.

There is currently **no active implementation authorization**. A future Phase 6 implementation may begin only after the Project Owner explicitly approves its scope and a new version of this document is established as the active PR specification.

## 2. Phase 5C closeout

GitHub #44 delivered the local-first deterministic goal/progress layer over the existing workout, history, progress, profile, and body-progress foundations.

Merged PR:
- GitHub #44;
- head commit: `7b20dd0f58c10ab12a30e930d170cdaab63a29fb`;
- merge commit: `f3406f227672ac4cd9f14ea1ee97bf2021d4ea1c`;
- feature branch `feature/phase-5c-goals` deleted after merge.

Project Owner manual verification reported PASS for:
- goal creation/view/edit/delete;
- supported goal calculations;
- trend/status behavior;
- insufficient-data behavior;
- migration behavior;
- backup/restore regression;
- existing Workout/History/Progress/Profile/Body Progress/PR regression;
- final Progress goal-card integration;
- final BMI readability/read-only behavior.

The two manual UI findings discovered during the verification cycle were preserved and closed in the Regression Matrix as `VERIFIED FIXED`:
- 5C-UI-01 — Progress goal-card overlap/obscuring;
- 5C-UI-02 — BMI faded/disabled-looking presentation.

The transient Google Drive OAuth refresh error observed during one test cycle was re-tested successfully after sign-out/sign-in and is recorded as `RESOLVED / NOT A DEFECT — current cycle`.

## 3. Phase 5C deterministic/local intelligence boundary — historical closeout

Phase 5C intelligence means ordinary application logic such as:
- arithmetic;
- date calculations;
- period comparisons;
- trend/slope calculations;
- adherence calculations;
- goal-state rules.

Phase 5C did **not** introduce an LLM, generative AI model, local language-model inference, model training, backend inference, or network dependency.

This deterministic boundary is now a completed historical phase constraint, not an active implementation authorization.

## 4. Phase 5C explicit non-goals — historical closeout

Phase 5C did not implement:
- AI coach;
- LLM integration;
- generative AI;
- local language model inference;
- model training/fine-tuning;
- nutrition or food logging;
- calorie tracking;
- BMR/TDEE;
- calorie prescriptions;
- medical diagnosis or treatment;
- body-fat estimation;
- social/cloud goals;
- achievements/ranks/badges;
- broad personalization;
- unrelated UI/UX modernization;
- multi-account Room redesign;
- backend/cloud analytics;
- new recurring infrastructure costs.

AI/LLM work remains explicitly deferred beyond Phase 5 and Phase 6. After Phase 6, an AI phase may be considered only after explicit Project Owner authorization.

## 5. Next planned objective — Phase 6

Phase 6 is currently **PLANNED / NOT APPROVED**.

The intended direction is UI/UX Modernization & Polish, potentially covering:
- modern visual system and component consistency;
- typography, spacing, surfaces, colors, and hierarchy;
- navigation and information-architecture polish;
- screen-by-screen UX refinement;
- empty/loading/error states;
- transitions/interaction quality;
- accessibility and touch-target refinement;
- polished presentation across workout, history, progress, profile, body progress, goals, backup/data, and settings.

These are planning directions only. They are not implementation authority.

## 6. Required workflow for the next PR

```text
Project Owner selects/approves Phase 6 objective
→ ChatGPT updates/creates locked active PR specification
→ Gemini performs read-only source audit
→ ChatGPT/Project Owner reconcile audit
→ implementation approval
→ Gemini implements only approved scope + tests
→ Project Owner builds/runs app + manual verification
→ ChatGPT reviews diff/evidence
→ Project Owner accepts/reviews/merges implementation PR + deletes branch
→ ChatGPT performs documentation closeout
```

## 7. Stop conditions for future work

Stop and return to ChatGPT/Project Owner if:
- the repository architecture conflicts with the proposed design;
- a destructive migration is required;
- backup compatibility is affected without explicit approval;
- a feature expands beyond the approved phase objective;
- AI/LLM/backend/network dependency becomes necessary without explicit authorization;
- implementation expands into unrelated modernization or personalization;
- runtime evidence contradicts the approved acceptance criteria.

No workaround may silently expand scope.

## 8. Current state

There is **no active PR** and therefore no current implementation scope to execute.

The next action is to define and explicitly approve Phase 6 before any code implementation begins.
