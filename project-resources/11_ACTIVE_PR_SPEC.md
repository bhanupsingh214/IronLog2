# IronLog — Active PR Specification

**Documentation version:** v3.1
**As of:** 2026-08-14
**Status:** APPROVED / PLANNED
**Current PR:** PR4.5
**Implementation authorization:** APPROVED by Project Owner

## 1. PR Identity

**Phase:** Phase 4 — Backup & Recovery  
**PR:** PR4.5 — Google Drive Cloud Restore  
**Objective:** Restore the user's existing IronLog `.ironlog` backup from the Google Drive `appDataFolder` through the established validation/import/restore pipeline, without redesigning the backup format or creating a second restore engine.

## 2. Approval State

The Project Owner approved proceeding with this PR after repository/resource feasibility review, restore-safety review, cloud/account boundary review, and scope/risk/acceptance/verification review.

This document authorizes implementation only within the locked scope below.

## 3. Locked Architecture

```text
Signed-in + Drive-authorized user
        ↓
Google Drive appDataFolder
        ↓
locate current IronLog `.ironlog`
        ↓
download to controlled temporary/local representation
        ↓
existing backup validation/import boundary
        ↓
BackupPayload
        ↓
existing RestoreRepository / restore transaction
        ↓
restored Room database
```

Rules:
1. `.ironlog` remains the canonical portable backup artifact.
2. Google Drive is transport/storage only.
3. Existing restore engine remains authoritative.
4. Validation must complete before destructive database mutation.
5. Restore remains transactional and FK-safe.
6. Do not create a cloud-specific second restore implementation.
7. Do not require user-visible Drive browsing.
8. Stage the download rather than unnecessarily buffering the entire archive in memory.
9. Exact source signatures must be verified before implementation; do not invent APIs from this specification.

## 4. Input Boundary Decision

The local import flow is URI-oriented. PR4.5 must support a cloud-downloaded artifact without duplicating validation logic.

Preferred direction:

```text
Local URI ───────────────┐
                         ├→ common backup validation/parsing → BackupPayload
Downloaded cloud file ──┘
```

The exact source-level abstraction is **TBD until repository inspection**. Preserve existing restore semantics.

## 5. Account / Authorization Safety

Before cloud restore is allowed, establish that the active Drive authorization is valid for the current signed-in account.

Required lifecycle cases:
- Account A sign-in → authorize Drive → restore A backup;
- sign out → Account B sign-in → stale A authorization cannot be used for B restore;
- revoked/expired authorization fails safely;
- no signed-in account cannot enter cloud restore;
- no valid Drive authorization cannot enter cloud restore.

If current code cannot establish this safely, the correctness/security fix is in scope for PR4.5.

## 6. Cloud Backup Discovery

Use the established PR4.4 cloud-storage boundary.

Expected behavior:
- locate the IronLog backup in `appDataFolder`;
- use deterministic backup identity/filename behavior where current code confirms it;
- do not add a general Drive browser;
- do not search arbitrary user Drive folders.

No backup → clear non-destructive state; local database unchanged.

## 7. Download Safety

- Download to controlled temporary/local representation.
- Do not mutate the database during download.
- Download failure leaves local data unchanged.
- Clean temporary artifacts after success/failure where practical.
- A partial artifact must never reach restore mutation.

## 8. Validation / Restore Safety

Before destructive database work:
- archive structure must validate;
- required files must exist;
- backup version must be supported;
- integrity/checksum validation must pass where required;
- structural/relational validation required by the existing importer must pass.

Only then invoke the established restore path. Preserve ID remapping, canonical identity, FK enforcement, transaction rollback, and replacement semantics.

## 9. Explicit Scope

### In scope
- Google Drive cloud-backup discovery;
- download of the user's `.ironlog` artifact;
- safe temporary/local staging;
- reuse of existing validation/import/restore behavior;
- minimal input-boundary refactoring required for reuse;
- cloud restore confirmation UX;
- clear success/failure/no-backup/authorization states;
- account-switch/authorization protection required for correctness;
- network/download failure handling;
- empty and populated database regression coverage;
- local import/export regression protection.

### Explicit non-goals
- user-visible Drive browsing;
- arbitrary Drive file/folder management;
- multiple cloud backup history/version browser;
- scheduled/automatic restore;
- automatic restore;
- cloud synchronization;
- backup-format redesign;
- schema/database redesign;
- centralized IronLog cloud storage;
- unrelated UI redesign;
- unrelated analytics;
- unrelated account-system redesign;
- cloud lifecycle features not required for safe restore.

## 10. Material Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation | Verification |
|---|---|---|---|---|
| Wrong account/authorization used | Medium | High | Verify active account and Drive authorization consistency | Real-device A→B tests |
| Corrupt/partial artifact reaches restore | Medium | High | Stage and fully validate before mutation | Corrupt/truncated tests |
| Data destroyed before validation | Low | Critical | No destructive call before validation succeeds | Populated DB failure tests |
| Cloud path duplicates restore logic | Medium | Medium | Reuse common parser/import + RestoreRepository | Code review + regression |
| Populated restore duplicates data | Low | High | Preserve PR4.3 replacement behavior | Populated restore test |
| Authorization revoked/expired | Medium | Medium | Fail safely and require valid authorization | Runtime auth-failure test |
| No cloud backup | Medium | Low | Explicit non-destructive state | No-backup test |
| Large backup memory pressure | Medium | Medium | Controlled staging/streaming | Large-backup inspection/test |
| Documentation/source mismatch | Medium | High | Inspect repository signatures before coding | Pre-flight source audit |

## 11. Acceptance Criteria

### Discovery/download
- [ ] Signed-in, Drive-authorized user can initiate cloud restore.
- [ ] Existing IronLog `.ironlog` can be located in Drive app-data area.
- [ ] Backup downloads without modifying database.
- [ ] No-backup state is clear and non-destructive.
- [ ] Download/network failure leaves local data unchanged.

### Validation
- [ ] Cloud artifact uses established validation/import path.
- [ ] Malformed archive rejected.
- [ ] Missing required components rejected.
- [ ] Unsupported version rejected.
- [ ] Integrity/checksum failure rejected.
- [ ] Validation failure leaves existing data unchanged.

### Restore
- [ ] Clear confirmation precedes replacement.
- [ ] Valid cloud restore succeeds on empty DB.
- [ ] Valid cloud restore replaces populated DB.
- [ ] No duplicate programs/history.
- [ ] Identity resolution remains correct.
- [ ] Restore remains transactional.
- [ ] Post-restore UI reflects restored data.

### Account safety
- [ ] Account A restores its authorized backup.
- [ ] A sign-out → B sign-in cannot reuse stale A authorization.
- [ ] Revoked/expired authorization fails safely.
- [ ] Restore unavailable without valid signed-in/authorized state.

### Regression
- [ ] Local picker import still works.
- [ ] Local export still works.
- [ ] PR4.2/PR4.3 restore semantics remain intact.
- [ ] Legacy PR4.1 compatibility remains intact.

## 12. Verification Plan

### Pre-implementation source audit
- [ ] verify clean working tree and intended base;
- [ ] inspect PR4.4 cloud/account code;
- [ ] inspect `ImportService`;
- [ ] inspect `RestoreRepository`;
- [ ] inspect current UI/ViewModel restore flow;
- [ ] inspect schema/migrations;
- [ ] confirm exact source signatures;
- [ ] confirm account/authorization lifecycle;
- [ ] confirm no schema change is required.

### Repository checks
```powershell
git status
git diff --check
git diff --stat
```

After staging:
```powershell
git diff --cached --check
git diff --cached --stat
git status
```

Also require successful build and intended-only staged paths.

### Runtime verification
Required scenarios are tracked in `10_FEATURE_REGRESSION_MATRIX.md`.

### Evidence rule
Do not mark PASS from code inspection alone. Runtime claims require emulator/device evidence.

## 13. Scope-Change Rule

Classify discoveries as:
- required for PR4.5;
- required for correctness/security;
- regression caused by PR4.5;
- related but outside scope;
- unrelated;
- future feature.

Only the first three may justify implementation changes without a new product decision. Other expansion must be deferred or separately approved.

## 14. Completion

After merge:
1. verify merged Git state;
2. update current state;
3. update roadmap;
4. record durable decisions;
5. update backup/architecture/schema resources only if contracts changed;
6. update regression evidence;
7. refresh handoff;
8. reset/advance the active PR state;
9. run three-pass audit.

## 15. Owner Approval

**Project Owner decision:** APPROVED — proceed with PR4.5 implementation under this locked specification.
