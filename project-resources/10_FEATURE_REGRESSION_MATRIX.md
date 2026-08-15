# IronLog — Feature Regression Matrix

**Documentation version:** v3.4
**As of:** 2026-08-15

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

## PR4.5 merge evidence

| ID | Scenario | Evidence |
|---|---|---|
| P45-1 | PR4.5 implementation/build verification | PASS — GitHub #31; JVM tests, clean debug build, connected Android tests |
| P45-2 | Real-emulator Google Drive restore flow | PASS — GitHub #31 PR record; metadata confirmation and restore flow manually verified |
| P45-3 | File-based backup validation instrumentation coverage | PASS — GitHub #31; 5 connected Android tests passed |

The detailed PR4.5 scenario rows below remain `[ ]` unless their individual evidence is explicitly recorded. PR4.5 merge status must not be used to infer PASS for every row.

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

## Stability regressions

| ID | Scenario | Result | Evidence |
|---|---|---|---|
| S1 | Connected instrumentation tests preserve the production package and local data | PASS | 7/7 connected tests passed; package remained installed; Programs/History remained present; manual smoke test passed; zero data loss |
| S2 | Finish Workout confirmation shows live duration for an active session | PASS | GitHub #32; 7/7 connected tests, successful debug build, manual emulator verification |
| S3 | Completed-workout duration remains correct after the live-duration fix | PASS | GitHub #32 manual emulator verification; completed-workout screen remained correct |

## Phase 5A — Progress & History Presentation

| ID | Scenario | Result | Evidence |
|---|---|---|---|
| P5A-1 | Progress overview and training-frequency presentation uses real persisted data | PASS | GitHub #35; automated verification and manual emulator smoke test |
| P5A-2 | Strength/PR progression presentation renders correctly | PASS | GitHub #35; automated verification and manual Progress smoke test |
| P5A-3 | Volume trend and period-filter controls render correctly | PASS | GitHub #35; manual Progress verification confirmed 7 Days / 30 Days / 3 Months / 6 Months controls |
| P5A-4 | Training-focus / muscle-group presentation renders correctly | PASS | GitHub #35; manual Progress verification |
| P5A-5 | Enhanced History workout cards render existing workout data | PASS | GitHub #35; manual History verification |
| P5A-6 | Calendar history presentation renders and navigates known workout dates | PASS | GitHub #35; manual History/Calendar verification |
| P5A-7 | Monthly recap renders deterministic local aggregation | PASS | GitHub #35; automated tests and manual emulator verification |
| P5A-8 | Yearly recap renders deterministic local aggregation | PASS | GitHub #35; automated tests and manual emulator verification |
| P5A-9 | Existing user data remains present during feature verification | PASS | Manual emulator verification; restored/manual workout history remained visible |
| P5A-10 | Phase 5A introduces no schema/migration, backup/restore, Drive, or AI/backend analytics change | PASS | GitHub #35 diff/review |

## Evidence rule

Do not mark a current-cycle test PASS from code inspection or an agent report alone.

The populated-database replacement test is a critical regression boundary for any restore-path change.

For merged PRs, preserve the distinction between PR-level merge verification and individual scenario evidence. Do not infer PASS for untested rows.
