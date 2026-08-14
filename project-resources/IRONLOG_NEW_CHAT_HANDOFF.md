# IronLog — New Chat Handoff

**Handoff version:** v3.1
**As of:** 2026-08-14
**Purpose:** Make a fresh chat inside the IronLog project immediately productive without relying on prior conversation memory.

## 1. First action in every new chat

Read, in order:
1. `00_PROJECT_INDEX.md`
2. `01_PROJECT_STATE.md`
3. `03_ROADMAP_AND_PR_LEDGER.md`
4. `11_ACTIVE_PR_SPEC.md`

Then read relevant technical/evidence resources.

If GitHub is connected, fetch `/project-resources/` from the repository and treat that as the current canonical stack.

## 2. Current verified baseline

- Phase 4 — Backup & Recovery.
- PR4.1 — MERGED / VERIFIED.
- PR4.2 — MERGED / VERIFIED.
- PR4.3 — MERGED / VERIFIED.
- PR4.4 / GitHub #29 — MERGED / VERIFIED.
- PR4.4 head: `50e77cbcb8da0fce58aae990166097b512174b1d`.
- PR4.4 merge: `fdfdfb47d4844983d5806287d103a81f8946869e`.
- PR4.5 — APPROVED / PLANNED.
- No other PR is authorized.

## 3. Current PR4.5 objective

Google Drive Cloud Restore:

```text
Google Drive appDataFolder
→ locate IronLog `.ironlog`
→ controlled download/staging
→ existing validation/import boundary
→ existing restore transaction
→ Room/UI
```

## 4. PR4.5 critical constraints

- validate before destructive mutation;
- preserve transactional replacement;
- preserve FK enforcement;
- preserve canonical identity/remapping;
- do not create a second restore engine;
- verify active Google account ↔ Drive authorization consistency;
- handle download/authorization/no-backup failures non-destructively;
- do not redesign backup format/schema unless separately re-approved.

## 5. Required first implementation action

Before writing code:
1. verify `master` and current Git state;
2. inspect PR4.4 cloud/account implementation;
3. inspect current `ImportService`;
4. inspect current `RestoreRepository`;
5. inspect current UI/ViewModel restore flow;
6. inspect schema/migrations;
7. confirm exact input boundary for local URI vs downloaded cloud artifact;
8. verify account/authorization lifecycle;
9. update/confirm the PR4.5 implementation plan if source contradicts assumptions.

Do not invent method signatures from this handoff.

## 6. Critical historical regression

PR4.3 exposed a populated-database restore bug: DELETE statements were ineffective because `query().close()` did not execute them.

The fix used `execSQL()` with FK-safe ordering and preserved transactional restore.

This is a critical regression boundary. Do not casually redesign restore internals.

## 7. Evidence rules

- Runtime behavior → emulator/device.
- Build → actual build output.
- Git/PR state → actual Git/GitHub.
- Schema → current repository.
- Product acceptance → Project Owner.

Agent reports are evidence, not proof.

## 8. Collaboration

Project Owner:
- final product decisions;
- PR authorization;
- runtime testing;
- final acceptance.

ChatGPT:
- architecture/reasoning;
- scope/risk/acceptance;
- review;
- continuity;
- documentation maintenance.

Gemini/implementation agent:
- substantial approved repository implementation;
- source inspection;
- build/tests;
- implementation evidence.

## 9. If resources disagree

Do not guess.

Use:

```text
repository code/schema/tests
→ Git/GitHub history
→ canonical resources
→ conversation
→ inference
```

Repair the canonical resource stack when necessary.

## 10. Exact next action

For the current baseline:

> Perform the PR4.5 repository pre-flight audit against the current `master` source before implementation. Do not begin implementation until the actual source confirms the locked architecture and account/authorization assumptions.
