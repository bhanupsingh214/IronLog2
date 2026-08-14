# IronLog — Testing & Release Protocol

**Documentation version:** v3.1
**As of:** 2026-08-14

## 1. Standard PR lifecycle

### Before implementation
1. Verify intended base branch and current Git state.
2. Confirm clean working tree.
3. Read canonical Project Resources.
4. Inspect affected repository code.
5. Lock objective, scope, and non-goals.
6. Identify material risks.
7. Define mitigations/precautions.
8. Define acceptance criteria.
9. Define verification plan.
10. Obtain Project Owner approval and lock `11_ACTIVE_PR_SPEC.md`.

### During implementation
- Keep changes inside locked scope.
- Build frequently.
- Do not silently expand scope.
- Record discoveries affecting correctness, security, compatibility, or scope.
- Stop and re-audit if an assumption in the resources is contradicted by source.

### Before commit
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

Review every staged path manually.

### Before merge
Verify:
- build succeeds;
- affected feature passes;
- required regressions pass;
- material mitigations were exercised;
- no debug logging;
- no IDE/environment noise;
- diff matches approved scope;
- Project Owner accepts the result.

### After merge
1. Confirm actual GitHub merge.
2. Verify merged commit.
3. Delete feature branch when approved.
4. Sync `master`.
5. Confirm clean working tree.
6. Update affected resources.
7. Run three-pass documentation audit.
8. Verify canonical-stack integrity.

## 2. Risk → Mitigation → Verification gate

Every meaningful PR must record:
- risk;
- likelihood;
- impact;
- mitigation;
- verification;
- residual risk if material.

The PR-specific table belongs in `11_ACTIVE_PR_SPEC.md`.

## 3. Evidence discipline

Do not mark PASS from code inspection, build success, screenshots without context, or agent completion reports alone.

Evidence mapping:
- runtime behavior → emulator/device evidence;
- build → actual build output;
- Git state → actual Git/GitHub state;
- schema → repository/schema evidence;
- product acceptance → Project Owner decision.

Historical evidence must be labeled historical and does not automatically equal current-cycle PASS.

## 4. Backup testing

Verify:
- `.ironlog` creation;
- required archive components;
- metadata;
- integrity/checksum;
- transfer/selection;
- valid restore;
- backward compatibility where required.

Restore failure paths:
- malformed ZIP;
- missing required files;
- checksum mismatch;
- unsupported version;
- structurally valid but relationally invalid payload.

Expected failure behavior:
- clear user-facing error;
- no partial restore;
- existing database protected.

## 5. Populated-database replacement

When restore logic/input changes:
1. populate a test device;
2. restore a known-good backup;
3. verify replacement rather than append;
4. verify no duplicate programs/history;
5. verify identity mappings and relationships;
6. verify UI reflects restored data.

This is a critical regression because PR4.3 exposed and fixed ineffective DELETE execution.

## 6. PR4.5 cloud-restore testing

Required scenarios are in `10_FEATURE_REGRESSION_MATRIX.md` and include:
- valid cloud backup on empty DB;
- valid cloud backup on populated DB;
- no cloud backup;
- network/download failure;
- corrupt/integrity-failed artifact;
- unsupported version;
- Account A → sign out → Account B;
- revoked/expired Drive authorization;
- local import/export regressions.

## 7. Fresh-device rule

For portability work, use a disposable/fresh emulator or device where practical. Do not clear data manually to hide a restore failure.

## 8. Release evidence

Before declaring a PR complete, preserve enough evidence to reconstruct:
- what changed;
- which commit/PR merged;
- what was tested;
- which tests passed/failed;
- which risks were exercised;
- which limitations remain.
