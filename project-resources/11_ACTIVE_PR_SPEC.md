# IronLog — Active PR Specification

**Documentation version:** v3.3  
**As of:** 2026-08-14  
**Status:** APPROVED / IMPLEMENTATION READY  
**Current PR:** PR4.5  
**Implementation authorization:** APPROVED by Project Owner

## 1. PR Identity

**Phase:** Phase 4 — Backup & Recovery  
**PR:** PR4.5 — Google Drive Cloud Restore  
**Objective:** Restore the user's existing IronLog `.ironlog` backup from the Google Drive `appDataFolder` through the established validation/import/restore pipeline, without redesigning the backup format or creating a second restore engine.

## 2. Approval State

The Project Owner approved proceeding with this PR after:
- repository/resource feasibility review;
- restore-safety review;
- cloud/account boundary review;
- scope/risk/acceptance/verification review;
- ChatGPT repository pre-flight audit;
- Gemini implementation-readiness audit;
- reconciliation of the two audits against the current repository.

**Gemini verdict:** `READY WITH CHANGES`. The required changes have been reconciled below and are now part of the locked implementation plan.

This document authorizes implementation only within the locked scope below.

## 3. Locked Architecture

```text
Signed-in Google identity
        ↓
verified Drive authorization for the active identity
        ↓
Google Drive appDataFolder
        ↓
locate the current IronLog `.ironlog`
        ↓
download to controlled temporary/local representation
        ↓
common backup validation/parsing boundary
        ↓
BackupPayload
        ↓
existing RestoreRepository / restore transaction
        ↓
restored Room database
        ↓
refresh UI
        ↓
cleanup temporary artifact
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
9. Exact source signatures and library APIs must be verified from the repository before use; do not invent APIs from this specification or from an agent recommendation.
10. A Gemini audit finding is evidence, not authority; only reconciled/approved decisions in this specification authorize implementation.

## 4. Input Boundary Decision

The current local import flow is URI-oriented. PR4.5 must support a cloud-downloaded artifact without duplicating validation logic.

Preferred direction:

```text
Local URI ───────────────┐
                         ├→ common backup validation/parsing → BackupPayload
Downloaded cloud file ──┘
```

The exact source-level abstraction is an implementation detail to verify against the current repository. A small `File`/stream adapter is acceptable if it preserves one validation implementation. Do not create separate cloud validation/checksum/version logic.

## 5. Account / Authorization Safety

This is a **high-priority correctness/security requirement** identified independently by both audits.

Before cloud restore is allowed, establish that the active Drive authorization is valid for the current signed-in Google identity.

Required lifecycle cases:
- Account A sign-in → authorize Drive → restore A backup;
- sign out → Account B sign-in → stale A authorization cannot be used for B restore;
- revoked/expired authorization fails safely;
- no signed-in account cannot enter cloud restore;
- no valid Drive authorization cannot enter cloud restore.

### Implementation constraint

Gemini proposed using `AuthorizationRequest` account binding via a specific `setAccount(...)` call. **That exact API recommendation is not pre-approved.** Before coding, verify the supported account-binding mechanism for the exact installed Google Identity Services library/API used by the repository.

The requirement is approved; the exact mechanism is not. Do not invent or assume a method signature.

If current code cannot establish account/authorization consistency safely, the correctness/security fix is in scope for PR4.5.

## 6. Cloud Backup Discovery

Use the established PR4.4 cloud-storage boundary.

Expected behavior:
- locate the current IronLog backup in `appDataFolder`;
- use deterministic backup identity/filename behavior where current code confirms it;
- do not add a general Drive browser;
- do not search arbitrary user Drive folders;
- do not introduce a general backup-history/version-management feature.

### API scope decision

Gemini suggested a general `listBackups()` DTO. That is **not required or approved by default**. Prefer a focused operation that locates the current IronLog backup and provides the minimum metadata needed by the restore UX. A broader listing API may be introduced only if the actual UI/implementation proves it necessary, and any scope expansion must be classified under the scope-change rule.

No backup → clear non-destructive state; local database unchanged.

## 7. Download Safety

- Download to controlled temporary/local representation.
- Do not mutate the database during download.
- Stream to the target file where the Drive API supports it; avoid unnecessary whole-file memory buffering.
- Download failure leaves local data unchanged.
- A partial artifact must never reach restore mutation.
- Clean temporary artifacts after success/failure using structured cleanup (`use`/`try-finally` or equivalent).
- Validate the completed artifact before invoking the restore transaction.

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
- download of the user's current `.ironlog` artifact;
- safe temporary/local staging;
- reuse of existing validation/import/restore behavior;
- minimal input-boundary refactoring required for reuse;
- cloud restore confirmation UX;
- clear success/failure/no-backup/authorization states;
- account-switch/authorization protection required for correctness;
- network/download failure handling;
- empty and populated database regression coverage;
- local import/export regression protection;
- correctness-critical authorization/account handling required to safely perform restore.

### Explicit non-goals
- user-visible Drive browsing;
- arbitrary Drive file/folder management;
- multiple cloud backup history/version browser;
- generalized cloud backup listing unless proven necessary for the locked UX;
- scheduled/automatic restore;
- automatic restore;
- cloud synchronization;
- backup-format redesign;
- schema/database redesign unless a previously unknown correctness requirement proves it necessary;
- centralized IronLog cloud storage;
- unrelated UI redesign;
- unrelated analytics;
- unrelated account-system redesign;
- cloud lifecycle features not required for safe restore.

## 10. Material Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation | Verification | Residual risk |
|---|---|---|---|---|---|
| Wrong account/authorization used | Medium | High | Verify active identity and Drive authorization consistency; reject ambiguous/stale authorization | Real-device A→B tests; revoked/expired auth test | Must depend on supported Google authorization semantics |
| Corrupt/partial artifact reaches restore | Medium | High | Stage download and fully validate before mutation | Corrupt/truncated tests | Low after validation gate |
| Data destroyed before validation | Low | Critical | No destructive call before validation succeeds | Populated DB failure tests | Low |
| Cloud path duplicates restore logic | Medium | Medium | Reuse common parser/import + existing RestoreRepository | Code review + regression | Low |
| Populated restore duplicates data | Low | High | Preserve PR4.3 replacement behavior | Populated restore test | Low |
| Authorization revoked/expired | Medium | Medium | Detect failure and require valid authorization | Runtime auth-failure test | Low/Medium |
| No cloud backup | Medium | Low | Explicit non-destructive state | No-backup test | Low |
| Large backup memory pressure | Medium | Medium | Controlled file/stream staging; inspect parser behavior | Large-backup inspection/test | Parser still reads required payload structures |
| API/account-binding assumption is wrong | Medium | High | Verify exact installed API/library contract before coding | Source/API inspection + build | Low after verification |
| Documentation/source mismatch | Medium | High | Repository source outranks prose; stop and reconcile material discrepancies | Pre-flight + Gemini audit | Low |

## 11. Acceptance Criteria

### Discovery/download
- [ ] Signed-in, Drive-authorized user can initiate cloud restore.
- [ ] Existing IronLog `.ironlog` can be located in Drive app-data area.
- [ ] Backup downloads without modifying database.
- [ ] No-backup state is clear and non-destructive.
- [ ] Download/network failure leaves local data unchanged.
- [ ] Partial/failed downloads are not passed to validation/restore.

### Validation
- [ ] Cloud artifact uses the established validation/import path.
- [ ] Malformed archive rejected.
- [ ] Missing required components rejected.
- [ ] Unsupported version rejected.
- [ ] Integrity/checksum failure rejected.
- [ ] Validation failure leaves existing data unchanged.
- [ ] Local URI import and cloud-file input share one validation implementation.

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
- [ ] Exact account-binding mechanism is supported by the installed library/API and verified before implementation is considered complete.

### Regression
- [ ] Local picker import still works.
- [ ] Local export still works.
- [ ] PR4.2/PR4.3 restore semantics remain intact.
- [ ] Legacy PR4.1 compatibility remains intact.

## 12. Verification Plan

### Pre-implementation source audit — completed
- [x] inspected PR4.4 cloud/account code;
- [x] inspected `ImportService`;
- [x] inspected `RestoreRepository`;
- [x] inspected current UI/ViewModel direction;
- [x] inspected schema/migration requirement at planning level;
- [x] confirmed cloud download capability is new work;
- [x] confirmed URI/File boundary issue;
- [x] identified account/authorization consistency risk;
- [x] ChatGPT audit completed;
- [x] Gemini audit completed;
- [x] audits reconciled.

### Implementation rules
- [ ] verify exact Google Identity Services account-binding API before editing auth code;
- [ ] verify exact Drive download API signature before editing cloud service;
- [ ] verify exact importer abstraction before refactoring `ImportService`;
- [ ] verify no schema/migration change is required;
- [ ] keep implementation within locked scope.

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

## 13. Gemini Audit Record

**Audit:** PR4.5 Cloud Restore — Comprehensive Audit Report  
**Auditor:** Gemini  
**Result:** `READY WITH CHANGES`

### Accepted findings
- cloud interface requires retrieval capability;
- account/authorization consistency is a high-priority risk;
- downloaded cloud artifact needs a safe file/stream input boundary;
- temporary artifact cleanup is required;
- cloud restore UI/state must be added;
- download, corruption, checksum, authorization, account-switch, and restore regressions require testing.

### Reconciled / constrained findings
- General `listBackups()` is not approved by default; use focused current-backup discovery unless UX proves broader listing necessary.
- The proposed exact `setAccount(Account(email, "com.google"))` call is not approved without verifying the installed API/library contract. The requirement is account consistency, not a preselected method signature.
- Memory pressure is a secondary risk; avoid unnecessary buffering, but do not redesign the backup format or parser without evidence.

### Final implementation-readiness decision
**READY FOR IMPLEMENTATION**, subject to the implementation rules above. No further audit is required before coding unless implementation reveals a material discrepancy.

## 14. Scope-Change Rule

Classify discoveries as:
- required for PR4.5;
- required for correctness/security;
- regression caused by PR4.5;
- related but outside scope;
- unrelated;
- future feature.

Only the first three may justify implementation changes without a new product decision. Other expansion must be deferred or separately approved.

If implementation reveals a material discrepancy from the Gemini audit, stop and classify it. If it changes correctness, security, architecture, scope, or acceptance criteria, update this specification and obtain required Owner approval before continuing.

## 15. Completion

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

## 16. Owner Approval

**Project Owner decision:** APPROVED — proceed with PR4.5 implementation under this reconciled specification.
