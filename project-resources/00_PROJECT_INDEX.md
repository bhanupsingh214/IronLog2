# IronLog — Project Resource Index

**Documentation version:** v3.1
**Canonical location:** `/project-resources/`
**As of:** 2026-08-14
**Purpose:** Canonical continuity and governance index for planning, implementation, verification, release, and fresh-chat handoff.

## Canonical resource set

Exactly these 14 files constitute the Project Resource stack:

1. `00_PROJECT_INDEX.md` — authority map and reading order.
2. `01_PROJECT_STATE.md` — verified current project state.
3. `02_ARCHITECTURE_AND_DATA_MODEL.md` — current architecture and data/identity model.
4. `03_ROADMAP_AND_PR_LEDGER.md` — development timeline, PR history, candidates, and approved roadmap.
5. `04_ENGINEERING_INVARIANTS.md` — non-negotiable engineering rules.
6. `05_TESTING_AND_RELEASE_PROTOCOL.md` — PR lifecycle, evidence, testing, Git/release protocol.
7. `06_DECISION_LOG.md` — durable decisions and rationale.
8. `07_BACKUP_PORTABILITY_SPEC.md` — `.ironlog` backup/restore contract.
9. `08_DOCUMENTATION_MAINTENANCE.md` — documentation governance and audit protocol.
10. `09_SCHEMA_AND_MIGRATION_LEDGER.md` — verified Room schema/migration history.
11. `10_FEATURE_REGRESSION_MATRIX.md` — feature/regression evidence.
12. `11_ACTIVE_PR_SPEC.md` — current implementation authorization boundary.
13. `IRONLOG_PROJECT_ROLES_AND_COLLABORATION_PROTOCOL.md` — roles and authority.
14. `IRONLOG_NEW_CHAT_HANDOFF.md` — fresh-chat continuity packet.

No `(1)` copies, ad-hoc replacements, ZIP files, or conversation exports are canonical resources.

## Required fresh-chat reading order

For a substantial engineering task:

1. `00_PROJECT_INDEX.md`
2. `01_PROJECT_STATE.md`
3. `03_ROADMAP_AND_PR_LEDGER.md`
4. `11_ACTIVE_PR_SPEC.md`
5. `04_ENGINEERING_INVARIANTS.md`
6. `05_TESTING_AND_RELEASE_PROTOCOL.md`
7. relevant technical resources (`02`, `07`, `09`, `10`)
8. `06_DECISION_LOG.md`
9. collaboration/continuity resources as needed.

A fresh chat must confirm the current state from the repository and these resources before implementation advice.

## Evidence labels

- **VERIFIED** — supported by current repository code/schema/tests, merged Git history, or tangible runtime evidence.
- **CURRENT** — observed in the current repository but not necessarily a durable product decision.
- **APPROVED / PLANNED** — explicitly authorized future implementation.
- **CANDIDATE / NOT APPROVED** — possible future work only.
- **TBD** — unresolved and requiring investigation/decision.
- **INFERENCE** — reasoning/planning; never implementation authority.
- **HISTORICAL** — preserved past fact that is not current state.

## Authority hierarchy

When sources disagree:

1. current repository code/schema/tests;
2. merged Git/GitHub history and actual PR state;
3. canonical Project Resources;
4. conversation/handoff notes;
5. model inference.

For product/PR authorization, explicit Project Owner approval recorded in `11_ACTIVE_PR_SPEC.md` is required even when an idea is technically feasible.

## Anti-hallucination rules

Never invent:
- class/method signatures;
- database columns or migrations;
- API behavior;
- test results;
- Git status;
- PR merge state;
- account/authorization semantics;
- undocumented product requirements.

If not verified, label it `TBD` and inspect the repository.

## PR authorization rule

`03_ROADMAP_AND_PR_LEDGER.md` describes planning. It does not authorize implementation.

`11_ACTIVE_PR_SPEC.md` is the implementation authorization boundary.

No implementation may begin from a candidate, conversation statement, or roadmap entry alone.

## Documentation lifecycle

After a meaningful merge:
1. verify actual Git/GitHub state;
2. verify runtime behavior where required;
3. update affected resources;
4. record durable decisions;
5. close/reset active PR state;
6. run the three-pass documentation audit;
7. verify canonical-stack integrity.

## Repository source

Repository: `bhanupsingh214/IronLog2`
Default branch: `master`

The repository is the durable source for code/history. `/project-resources/` is the durable source for project knowledge and governance.
