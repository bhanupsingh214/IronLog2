# IronLog — Active PR Specification

**Documentation version:** v3.8  
**As of:** 2026-08-16  
**Status:** NO ACTIVE PR — PHASE 5B CLOSED  
**Current objective:** NONE SELECTED  
**Implementation authorization:** NOT AUTHORIZED

## 1. Authorization boundary

Phase 5B — Profile Foundation & Body Progress — is complete and merged as GitHub #41. Its implementation branch has been deleted.

There is currently **no active implementation objective**. No source-code implementation is authorized until the Project Owner explicitly selects the next objective and a new locked specification is created here.

This document remains the implementation boundary for future work. Any ambiguity, schema conflict, backup-contract conflict, or requested scope expansion must stop implementation and return to ChatGPT/Project Owner for resolution.

## 2. Closed phase reference

The immediately preceding active specification was Phase 5B — Profile Foundation & Body Progress. That scope is now closed and recorded in the roadmap, project state, schema ledger, decision log, and regression matrix.

Phase 5B merged commit:
- Head commit: `e6ab49fd2ce8e91d7e5f3f090bf1b9f615c60410`
- Merge commit: `f1fd9d91f4fbaee7df508b0819a3a478f8f46e31`
- GitHub PR: #41

## 3. Current authorization state

**No new product feature PR is authorized.**

Do not treat roadmap candidates, conversation ideas, Gemini recommendations, or prior phase specifications as implementation authorization.

## 4. Required workflow for the next objective

```text
Project Owner selects the next objective
→ ChatGPT defines/reconciles scope, non-goals, risks, acceptance criteria and verification plan
→ new 11_ACTIVE_PR_SPEC.md is explicitly locked
→ Gemini audits current source against the locked spec
→ Project Owner approves implementation
→ Gemini implements only approved changes + tests
→ Project Owner builds/runs the app and performs manual verification
→ ChatGPT reviews diff/evidence and canonical Project Resources
→ Project Owner reviews/merges implementation PR and deletes branch
→ ChatGPT performs documentation closeout
```

## 5. Standing non-negotiable boundaries

Until a new objective is selected and specified:
- no feature implementation;
- no schema/migration changes;
- no backup/restore contract changes;
- no account-ownership redesign;
- no new LLM/backend dependency for ordinary deterministic app behavior;
- no unrelated UI redesign;
- no silent roadmap-to-implementation promotion.

## 6. Stop conditions

Work must stop and return to ChatGPT/Project Owner if a proposed future implementation:
- conflicts with current repository schema or invariants;
- requires destructive migration/data loss;
- requires an undefined backup-versioning choice;
- requires account-keyed Room ownership or a multi-user local dataset;
- requires a medical/health interpretation beyond the approved product scope;
- introduces an unapproved AI/LLM/backend/network dependency;
- expands beyond the locked objective;
- or produces test/runtime evidence that contradicts acceptance criteria.

No workaround may silently expand scope.
