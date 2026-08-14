# IronLog — Feature Regression Matrix

**Documentation version:** v3.1
**As of:** 2026-08-14

## Evidence legend

- PASS — verified with appropriate evidence.
- HISTORICAL PASS — previously verified baseline.
- [ ] — required/current-cycle test not yet verified.
- TBD — requires investigation/decision.

## Established backup/restore baseline

| ID | Scenario | Evidence |
|---|---|---|
| B1 | Import Backup opens picker | HISTORICAL PASS — PR4.3 |
| B2 | Valid backup metadata confirmation | HISTORICAL PASS — PR4.3 |
| B3 | Cancel preserves existing data | HISTORICAL PASS — PR4.3 |
| B4 | Restore over populated DB replaces rather than duplicates | HISTORICAL PASS — PR4.3 fix |
| B5 | Legacy PR4.1 backup compatibility | HISTORICAL PASS |

## PR4.4 baseline

| ID | Scenario | Evidence |
|---|---|---|
| G1 | Google Sign-In | PASS — PR4.4 |
| G2 | Sign-out → sign-in | PASS — PR4.4 |
| G3 | App restart | PASS — PR4.4 |
| G4 | Separate Drive authorization | PASS — PR4.4 |
| G5 | Drive API access | PASS — PR4.4 |
| G6 | `.ironlog` cloud upload | PASS — PR4.4 |
| G7 | Repeat cloud backup | PASS — PR4.4 |
| G8 | Local export/import regression | PASS — PR4.4 |

## PR4.5 required tests

### Discovery/download

| ID | Scenario | Result |
|---|---|---|
| C1 | Signed-in + authorized user starts cloud restore | [ ] |
| C2 | Existing `.ironlog` found in Drive appDataFolder | [ ] |
| C3 | Valid cloud artifact downloads | [ ] |
| C4 | No cloud backup produces safe explicit state | [ ] |
| C5 | Network/download failure leaves local data unchanged | [ ] |

### Validation

| ID | Scenario | Result |
|---|---|---|
| V1 | Cloud artifact enters common validation/import path | [ ] |
| V2 | Malformed archive rejected | [ ] |
| V3 | Missing required archive component rejected | [ ] |
| V4 | Unsupported version rejected | [ ] |
| V5 | Integrity/checksum failure rejected | [ ] |
| V6 | Validation failure preserves local data | [ ] |

### Restore

| ID | Scenario | Result |
|---|---|---|
| R1 | Valid cloud restore on empty DB | [ ] |
| R2 | Valid cloud restore on populated DB replaces data | [ ] |
| R3 | No duplicate programs/history | [ ] |
| R4 | Identity resolution remains correct | [ ] |
| R5 | Transaction rollback remains correct | [ ] |
| R6 | Post-restore UI reflects restored data | [ ] |

### Account/authorization safety

| ID | Scenario | Result |
|---|---|---|
| A1 | Account A restores its authorized backup | [ ] |
| A2 | A sign-out → B sign-in cannot reuse stale A Drive authorization | [ ] |
| A3 | Revoked/expired authorization fails safely | [ ] |
| A4 | No valid signed-in/authorized state blocks cloud restore | [ ] |

### Local regressions

| ID | Scenario | Result |
|---|---|---|
| L1 | Existing local import still works | [ ] |
| L2 | Existing local export still works | [ ] |
| L3 | Existing populated restore replacement remains correct | [ ] |
| L4 | Programs/history/progress/PR/settings integrity remains intact | [ ] |

## Evidence rule

Do not mark a current-cycle test PASS from code inspection or an agent report alone.

The populated-database replacement test is a critical regression boundary for any restore-path change.

After PR4.5 verification, replace `[ ]` with PASS/FAIL plus concise evidence references. Do not erase historical failures; preserve them as historical context.
