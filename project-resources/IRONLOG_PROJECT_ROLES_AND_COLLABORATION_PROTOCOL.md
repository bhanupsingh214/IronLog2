# IronLog — Project Roles & Collaboration Protocol

**Documentation version:** v3.2
**As of:** 2026-08-14

## 1. Project Owner

The Project Owner has final authority over:
- product decisions;
- PR authorization;
- scope changes;
- acceptance;
- real-device testing decisions;
- Git commit/push/merge lifecycle when performed manually.

Approval must be explicit. Technical feasibility is not product authorization.

## 2. ChatGPT

Primary responsibilities:
- architecture and reasoning;
- continuity across chats;
- scope/non-goal definition;
- risk and mitigation analysis;
- acceptance criteria;
- verification planning;
- implementation review;
- evidence interpretation;
- Project Resource maintenance;
- GitHub documentation maintenance where connected access is available;
- reconciliation of Gemini audit reports with the locked PR and repository evidence.

ChatGPT must not invent missing source facts.

## 3. Gemini / implementation agent

Primary responsibilities:
- repository/source inspection;
- **pre-implementation audit of implementation-specific feasibility and risks when requested by the PR workflow**;
- substantial approved implementation;
- builds/tests;
- implementation reports;
- code-level debugging within approved scope;
- post-implementation technical/reporting evidence.

Gemini must not silently redefine requirements or broaden scope.

### Gemini audit-report rule

For meaningful PRs, Gemini should provide an **audit report before implementation** when the PR touches multiple files, authentication, cloud/storage, backup/restore, database/schema, identity, or other material architectural behavior.

The audit is not an authorization mechanism. It is implementation-specific evidence for ChatGPT and the Project Owner.

The audit should:
1. inspect the actual current repository state;
2. compare source reality with the active PR specification;
3. identify confirmed assumptions and unknowns;
4. identify implementation-specific risks/blockers;
5. identify likely files/components affected;
6. identify dependency/API/build/test constraints;
7. identify required or recommended tests;
8. flag any scope or architecture conflict;
9. give a verdict: `READY`, `READY WITH CHANGES`, or `BLOCKED`.

**Audit-only means audit-only:** when explicitly requested before implementation, Gemini must not modify application code, commit, push, or create a PR unless separately authorized.

### Gemini implementation report

After implementation, Gemini should report:
- exact changes made;
- files changed;
- implementation decisions made within scope;
- builds/tests executed and their actual results;
- known limitations/failures;
- remaining risks;
- Git/diff observations where available.

Completion reports are evidence, not proof.

## 4. GitHub

GitHub is the durable source for:
- code;
- branches;
- commits;
- pull requests;
- merge history;
- canonical `/project-resources/` documentation.

## 5. Authority model

```text
Current code/schema/tests
        ↓
Git/GitHub history
        ↓
Canonical Project Resources
        ↓
Conversation/handoff
        ↓
Inference
```

For product authorization:

```text
Project Owner approval
        ↓
11_ACTIVE_PR_SPEC.md
        ↓
implementation
```

For implementation readiness:

```text
Locked PR specification
        ↓
Gemini pre-implementation audit
        ↓
ChatGPT reconciliation/review
        ↓
Owner decision if material change is proposed
        ↓
implementation
```

No audit report by itself authorizes implementation.

## 6. Agent completion and audit rule

An agent's completion or audit claim is evidence, not proof.

Require appropriate evidence:
- runtime → emulator/device;
- build → build output;
- Git → Git/GitHub state;
- schema → source/schema evidence;
- product acceptance → Owner decision;
- implementation readiness → repository inspection plus Gemini audit report where required.

Do not convert an agent's `READY` verdict into a factual claim without checking the underlying evidence.

## 7. Scope-change protocol

If implementation or audit discovers work outside scope:
1. classify the discovery;
2. determine whether it is required for correctness/security or merely useful;
3. if not required, defer it;
4. if required, document the reason and update the active PR specification before proceeding;
5. never silently expand.

If Gemini's audit materially disagrees with the approved architecture, stop and reconcile the disagreement before implementation.

## 8. Fresh-chat rule

A fresh chat should begin from `/project-resources/`, not from assumptions inherited from an earlier conversation.

If a chat encounters contradictory resources:
- inspect GitHub/source evidence;
- do not choose a convenient document;
- repair the canonical stack before continuing.

When a meaningful PR is active, a fresh chat should also request/review the latest relevant Gemini audit report before authorizing implementation work that depends on repository-specific details.

## 9. Documentation ownership

ChatGPT may maintain the Project Resources through the connected GitHub integration.

Material product decisions remain the Project Owner's authority.

Gemini audit reports are working evidence. Durable conclusions belong in the appropriate Project Resource only after they are reviewed and supported.

## 10. Standard collaboration loop

```text
Project Owner requirement
        ↓
ChatGPT resource + architecture review
        ↓
Scope / risks / mitigations / acceptance / verification
        ↓
Project Owner approval
        ↓
Active PR specification locked
        ↓
Gemini implementation-readiness audit
        ↓
ChatGPT audit reconciliation
        ↓
If material change: Owner decision + spec update
        ↓
Gemini implementation
        ↓
Gemini implementation report + build/test evidence
        ↓
ChatGPT technical/scope review
        ↓
Project Owner runtime verification
        ↓
ChatGPT evidence interpretation
        ↓
Project Owner final acceptance
        ↓
Git merge
        ↓
Documentation closeout
```

For small, mechanical tasks that do not need repository implementation-agent involvement, ChatGPT may use the connected GitHub/terminal workflow directly when low-risk and unambiguous.

## Final rule

> You own IronLog. ChatGPT reasons, scopes, risk-assesses, plans, reconciles evidence, reviews implementation, and maintains continuity. Gemini inspects the actual repository, provides implementation-readiness audits when required, and implements substantial approved changes. You verify real application behavior and make final decisions. No audit or completion claim replaces appropriate evidence, and no agent may silently expand or authorize PR scope.
