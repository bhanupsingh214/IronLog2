# IronLog — Testing & Release Protocol

**Documentation version:** v3.2
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
11. For meaningful repository changes, request a Gemini implementation-readiness audit before coding.
12. Reconcile the Gemini audit against repository evidence and the locked PR. If a material risk, blocker, or architecture/scope change is found, pause and resolve it before implementation.

### During implementation
- Keep changes inside locked scope.
- Build frequently.
- Do not silently expand scope.
- Record discoveries affecting correctness, security, compatibility, or scope.
- Stop and re-audit if an assumption in the resources is contradicted by source.
- If implementation reveals a material discrepancy from the pre-implementation Gemini audit, classify it and pause when it changes correctness, security, architecture, or scope.

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

## 2. Pre-implementation Gemini audit gate

For meaningful PRs, especially those involving authentication, cloud/storage, backup/restore, database/schema, identity, multi-file architecture, or high-risk regressions, Gemini should perform an **audit-only repository inspection before implementation**.

The audit must not modify application code or create implementation commits unless separately authorized.

Minimum audit output:
- repository/base-branch state inspected;
- confirmed assumptions/facts;
- source/API/class/method findings;
- implementation-specific risks;
- blockers or unknowns/TBDs;
- likely files/components affected;
- dependencies/build/test constraints;
- recommended tests;
- scope/architecture conflicts;
- verdict: `READY`, `READY WITH CHANGES`, or `BLOCKED`.

ChatGPT then reconciles the report with the active PR and canonical resources.

### Audit verdict handling

- `READY` → proceed only if no unresolved material discrepancy exists and Owner authorization remains valid.
- `READY WITH CHANGES` → resolve the listed changes; update the active PR if objective/scope/architecture/risks/verification materially change; obtain required Owner approval before implementation.
- `BLOCKED` → do not implement; resolve the blocker first.

An audit verdict never overrides the Project Owner's authority or the locked active PR specification.

## 3. Risk → Mitigation → Verification gate

Every meaningful PR must record:
- risk;
- likelihood;
- impact;
- mitigation;
- verification;
- residual risk if material.

The PR-specific table belongs in `11_ACTIVE_PR_SPEC.md`.

Gemini may add implementation-specific risks through its audit. Those risks become part of the locked PR only after ChatGPT review and, when material, Project Owner approval.

## 4. Evidence discipline

Do not mark PASS from code inspection, build success, screenshots without context, or agent completion reports alone.

Evidence mapping:
- runtime behavior → emulator/device evidence;
- build → actual build output;
- Git state → actual Git/GitHub state;
- schema → repository/schema evidence;
- product acceptance → Project Owner decision;
- implementation readiness → repository inspection plus Gemini audit where required.

Historical evidence must be labeled historical and does not automatically equal current-cycle PASS.

## 5. Backup testing

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

## 6. Populated-database replacement

When restore logic/input changes:
1. populate a test device;
2. restore a known-good backup;
3. verify replacement rather than append;
4. verify no duplicate programs/history;
5. verify identity mappings and relationships;
6. verify UI reflects restored data.

This is a critical regression because PR4.3 exposed and fixed ineffective DELETE execution.

## 7. PR4.5 cloud-restore testing

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

## 8. Fresh-device rule

For portability work, use a disposable/fresh emulator or device where practical. Do not clear data manually to hide a restore failure.

## 9. Release evidence

Before declaring a PR complete, preserve enough evidence to reconstruct:
- what changed;
- which commit/PR merged;
- what was tested;
- which tests passed/failed;
- which risks were exercised;
- which limitations remain;
- what the Gemini audit identified before implementation;
- whether any audit finding changed the implementation plan.
