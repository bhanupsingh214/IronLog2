# IronLog — Project Roles & Collaboration Protocol

**Documentation version:** v3.1
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
- GitHub documentation maintenance where connected access is available.

ChatGPT must not invent missing source facts.

## 3. Gemini / implementation agent

Primary responsibilities:
- repository/source inspection;
- substantial approved implementation;
- builds/tests;
- implementation reports;
- code-level debugging within approved scope.

The implementation agent must not silently redefine requirements or broaden scope.

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

## 6. Agent completion rule

An agent's completion claim is evidence, not proof.

Require appropriate evidence:
- runtime → emulator/device;
- build → build output;
- Git → Git/GitHub state;
- schema → source/schema evidence;
- product acceptance → Owner decision.

## 7. Scope-change protocol

If implementation discovers work outside scope:
1. classify the discovery;
2. determine whether it is required for correctness/security or merely useful;
3. if not required, defer it;
4. if required, document the reason and update the active PR specification before proceeding;
5. never silently expand.

## 8. Fresh-chat rule

A fresh chat should begin from `/project-resources/`, not from assumptions inherited from an earlier conversation.

If a chat encounters contradictory resources:
- inspect GitHub/source evidence;
- do not choose a convenient document;
- repair the canonical stack before continuing.

## 9. Documentation ownership

ChatGPT may maintain the Project Resources through the connected GitHub integration.

Material product decisions remain the Project Owner's authority.

## Final rule

> The project remains efficient when reasoning, implementation, authority, and evidence are clearly separated.
