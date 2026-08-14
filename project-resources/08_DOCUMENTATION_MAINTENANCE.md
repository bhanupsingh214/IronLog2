# IronLog — Documentation Maintenance Protocol

**Documentation version:** v3.1
**As of:** 2026-08-14

## 1. Purpose

Project Resources are a durable, machine-readable continuity layer for future chats, agents, and developers.

They are not a conversation transcript and must not become a second source of implementation truth.

## 2. Canonical location

The canonical Project Resource directory is:

```text
/project-resources/
```

Exactly the 14 files listed in `00_PROJECT_INDEX.md` are canonical.

No duplicate `(1)` files, ZIPs, exported chat transcripts, or ad-hoc notes are canonical.

## 3. Authority

Repository code/schema/tests and merged Git history outrank documentation for implementation facts.

The active PR specification is the authorization boundary for future work.

The roadmap is planning, not authorization.

## 4. Resource responsibilities

- Index → authority/read order.
- Project State → verified current state.
- Architecture → technical structure and data/identity model.
- Roadmap → PR timeline and candidates.
- Invariants → non-negotiable engineering rules.
- Testing → lifecycle/evidence protocol.
- Decisions → durable rationale.
- Backup Spec → portable artifact contract.
- Documentation Maintenance → documentation governance.
- Schema Ledger → Room/schema history.
- Regression Matrix → verification evidence.
- Active PR → current authorization/scope.
- Roles → responsibility/authority boundaries.
- Handoff → fresh-chat continuity.

Avoid duplicating detailed facts across resources unless a cross-reference is needed.

## 5. Update triggers

Update resources after:
- merged PRs;
- material architecture decisions;
- schema/migration changes;
- verified regression discoveries;
- material workflow/governance changes.

Do not update docs merely because an idea was discussed.

## 6. Three-pass audit

### Pass 1 — Individual-file correctness
Check:
- facts;
- dates;
- status labels;
- evidence classification;
- filenames;
- commands;
- stale claims;
- unsupported assertions;
- internal contradictions.

### Pass 2 — Cross-document consistency
Compare:
- state ↔ roadmap ↔ active PR;
- architecture ↔ invariants ↔ backup spec ↔ schema;
- decisions ↔ implementation;
- testing ↔ regression matrix ↔ active PR;
- index ↔ actual file set;
- roles ↔ actual workflow.

### Pass 3 — Stack integrity
Verify:
- exactly 14 canonical files;
- exact filenames;
- no duplicate/stale copies;
- no accidental files;
- no competing authority claims;
- no old active-state text.

## 7. Conflict protocol

If a resource conflicts with code:
1. stop implementation based on the conflicting claim;
2. inspect current code/schema/tests;
3. inspect Git history;
4. verify runtime if required;
5. classify actual state;
6. correct affected resources;
7. re-run cross-document audit.

## 8. Historical preservation

Do not rewrite historical facts to make them look current.

Use `HISTORICAL` where needed and explain replacements when durable decisions change.

## 9. Fresh-chat continuity standard

A fresh chat should be able to determine:
- what IronLog is;
- current phase;
- latest merged PR;
- current active PR/authorization;
- exact scope/non-goals;
- important invariants;
- relevant architecture;
- schema baseline;
- regression boundaries;
- collaboration roles;
- exact next action.

If a new chat cannot determine these without conversation history, the resource stack is incomplete.

## 10. Repository migration rule

Project Resources are now intended to live in GitHub under `/project-resources/`.

The old repository documentation was legacy and was audited during migration. Unique useful information must be preserved or explicitly retired before deletion.

## 11. Post-merge closeout

```text
merge confirmed
→ runtime/build/Git evidence verified
→ affected resources updated
→ decisions recorded
→ active PR reset/advanced
→ three-pass audit
→ canonical stack verified
```

## Final rule

> Documentation exists to preserve verified knowledge, explicit decisions, approved plans, engineering constraints, and continuity—not to invent facts or compete with source code.
