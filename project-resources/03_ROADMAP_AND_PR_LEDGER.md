# IronLog Roadmap & PR Ledger

**Documentation version:** v3.1  
**As of:** 2026-08-14

## Purpose

Canonical development timeline, roadmap, and PR ledger. Roadmap context does not authorize implementation; the active PR specification is the implementation authority.

## Phase 4 — Backup & Recovery

### PR4.1 — Backup Export
**Status: MERGED / VERIFIED**

Portable `.ironlog` export, metadata, versioning/integrity information, and local export.

### PR4.2 — Backup Restore
**Status: MERGED / VERIFIED**

Validation-before-destruction, transactional restore, relational ID remapping, identity preservation, legacy compatibility, and restore safety.

### PR4.3 — Backup Management & Recovery UX
**Status: MERGED / VERIFIED**

Picker-first import, metadata confirmation, cancel safety, restore-over-existing-data replacement, clear feedback, and preservation of PR4.2 behavior.

### PR4.4 — Google Account & Google Drive Backup
**Status: MERGED / VERIFIED**

GitHub PR #29:
- Head commit: `50e77cbcb8da0fce58aae990166097b512174b1d`
- Merge commit: `fdfdfb47d4844983d5806287d103a81f8946869e`

Verified:
- Google Sign-In;
- sign-out → sign-in;
- app restart;
- separate Drive authorization;
- Drive API access;
- `.ironlog` cloud upload;
- repeat cloud backup;
- local export/import regression;
- final build and repository checks.

Cloud download/restore was intentionally excluded from PR4.4.

## Current PR — PR4.5

### PR4.5 — Google Drive Cloud Restore
**Status: APPROVED / PLANNED**

**Locked objective:** download the user's IronLog `.ironlog` backup from Google Drive `appDataFolder` and restore it through the established validation/import/restore pipeline.

**Locked architecture:**
```text
Google Drive
→ locate `.ironlog`
→ controlled download/staging
→ existing validation/import boundary
→ existing restore transaction
→ Room
```

**Locked safety requirements:**
- validate before destructive mutation;
- preserve transactional replacement;
- preserve FK enforcement;
- preserve identity resolution;
- do not create a second restore engine;
- verify signed-in account / Drive authorization consistency.

**Locked non-goals:**
- Drive browsing/folder management;
- multiple backup history UI;
- scheduled/automatic restore;
- cloud synchronization;
- backup-format redesign;
- schema/database redesign;
- centralized IronLog storage;
- unrelated UI/analytics work.

**Implementation authorization:** granted in `11_ACTIVE_PR_SPEC.md`.

## Phase 4 future candidates

### Candidate — Cloud Backup Lifecycle Hardening
**Status: CANDIDATE / NOT APPROVED**

Possible future work:
- backup history/version handling;
- stale-backup policies;
- broader retry/recovery UX;
- lifecycle features not required by PR4.5.

These are not part of PR4.5 unless separately approved.

## Phase 5 candidates

### Phase 5A — Progress & History Presentation
**Status: CANDIDATE / NOT APPROVED**
- Body Progress;
- fitness timeline;
- monthly/yearly recap.

### Phase 5B — Goals & Engagement
**Status: CANDIDATE / NOT APPROVED**
- goal tracking;
- achievements/ranks.

### Phase 5C — AI Assistance
**Status: CANDIDATE / NOT APPROVED**
- AI coach.

## Scope-First Development Rule

For every PR:
1. identify the phase;
2. define one coherent objective;
3. lock scope;
4. lock non-goals;
5. define risks and mitigations;
6. define acceptance criteria;
7. define verification plan;
8. record explicit approval;
9. only then implement.

## Source-of-Truth Rule

Current repository code/schema/tests outrank documentation. If implementation differs from this plan, stop and inspect the repository before changing code.

## Planning Rule

Do not invent calendar deadlines. Future items remain CANDIDATE/PLANNED/TBD/INFERENCE as appropriate.

## Current Orientation

**Completed:** PR4.1 → PR4.2 → PR4.3 → PR4.4  
**Current:** PR4.5 — APPROVED / PLANNED  
**Next after PR4.5:** TBD; select only after PR4.5 is merged and resources are refreshed.
