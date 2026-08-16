# IronLog — Feature Regression Matrix

**Documentation version:** v4.1
**As of:** 2026-08-16

## Evidence legend

- PASS — verified with appropriate evidence.
- HISTORICAL PASS — previously verified baseline.
- IMPLEMENTED / NOT EXECUTED — test exists and was reviewed, but required runtime/instrumentation execution was unavailable.
- [ ] — required/current-cycle test not yet verified.
- OPEN — current-cycle observation exists and remains unresolved.
- IN PROGRESS — observation is actively being addressed.
- VERIFIED FIXED — original observation was re-tested with appropriate runtime evidence after implementation.
- DEFERRED — intentionally moved outside the current PR with a recorded reason.
- NOT REPRODUCED — investigated but could not be reproduced.
- RESOLVED / NOT A DEFECT — investigation established that the observed behavior is not currently a product defect.
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

## Phase 5B — Profile Foundation & Body Progress

| ID | Scenario | Result | Evidence |
|---|---|---|---|
| P5B-1 | Profile empty/incomplete state remains usable | PASS | Manual Profile verification |
| P5B-2 | Sex, DOB, and height persist and display correctly | PASS | Manual Profile verification |
| P5B-3 | Height ft/in entry and display preserve the physical value | PASS | Manual Profile verification; ft/in selection/display confirmed |
| P5B-4 | Metric height alternative is available and conversion is accurate | PASS | JVM calculator tests + manual Profile verification |
| P5B-5 | Body weight entry and latest-value display work | PASS | Manual Profile verification |
| P5B-6 | Body weight history is durable and independent of workout sessions | PASS | Repository tests + manual Profile verification |
| P5B-7 | Waist entry/history and latest-value display work | PASS | Repository tests + manual Profile verification |
| P5B-8 | BMI calculation is deterministic and uses canonical height/weight | PASS | BodyMetricsCalculatorTest + manual BMI verification |
| P5B-9 | Adult Indian/Asian-Indian BMI classification uses 18/23/25 boundaries | PASS | BodyMetricsCalculatorTest + manual BMI verification |
| P5B-10 | Non-adult BMI path is separated from adult classification | PASS | Unit-test implementation/audit + manual age-aware behavior verification |
| P5B-11 | Weight/waist edit and delete flows work | PASS | Manual Profile/body-history verification |
| P5B-12 | Local export/import round-trip preserves profile/body data | PASS | Manual export/import round-trip verification |
| P5B-13 | Existing Workout/History/Progress/PR behavior remains intact after Phase 5B | PASS | Manual regression smoke test |
| P5B-14 | v21→v22 migration creates profile/body tables and preserves existing data | IMPLEMENTED / NOT EXECUTED | Migration test implemented and reviewed; connected execution unavailable locally because no connected device was available |
| P5B-15 | Backup/restore remains backward compatible and transactional | PASS | Backup/restore implementation review + manual local round-trip; connected instrumentation execution unavailable locally |
| P5B-16 | Phase 5B introduces no multi-account Room redesign or LLM/backend dependency | PASS | GitHub #41 diff/review |

## Phase 5C — Goals & Deterministic Progress Intelligence

| ID | Scenario | Result | Evidence |
|---|---|---|---|
| P5C-1 | Goal create/view/edit/delete flows work | [ ] | PR #44 implementation exists; current-cycle runtime acceptance remains in progress |
| P5C-2 | Body-weight goal progress is calculated deterministically from persisted data | [ ] | PR #44 implementation exists; current-cycle runtime verification required |
| P5C-3 | Waist goal progress is calculated deterministically from persisted data | [ ] | PR #44 implementation exists; current-cycle runtime verification required |
| P5C-4 | Exercise/PR goal progress is calculated deterministically from persisted data | [ ] | PR #44 implementation exists; current-cycle runtime verification required |
| P5C-5 | Workout-frequency goal adherence is calculated deterministically | [ ] | PR #44 implementation exists; current-cycle runtime verification required |
| P5C-6 | Trend direction/rate handles sufficient history correctly | [ ] | PR #44 includes deterministic implementation/tests; current-cycle runtime/integration verification required |
| P5C-7 | Insufficient-data and no-meaningful-trend states do not fabricate conclusions | [ ] | PR #44 includes deterministic implementation/tests; current-cycle runtime verification required |
| P5C-8 | Goal status boundaries are deterministic and unit-tested | PASS | PR #44; `:app:testDebugUnitTest` reported 17 passed, 0 skipped, 0 failed, including the updated GoalCalculator tests |
| P5C-9 | Persistent goals survive required Room migration without existing-data loss | [ ] | GoalMigrationTest was implemented/updated; connected execution remains a current-cycle verification requirement |
| P5C-10 | New backup includes goal data and old backups remain restorable | [ ] | PR #44 changes Backup/Restore; current-cycle manual/runtime regression verification required |
| P5C-11 | Existing Workout/History/Progress/Profile/Body Progress/PR behavior remains intact | PASS | Project Owner ran the updated app on the emulator and confirmed the two reported Phase 5C UI findings were fixed: Progress goal-card overlap/obscuring is resolved, and BMI is normally readable while remaining read-only |
| P5C-12 | Phase 5C introduces no LLM/AI/backend/network dependency | PASS | PR #44 scope/diff review; PR description explicitly states no AI/LLM/ML/cloud dependency introduced |

## Active Manual-Test Observations — Phase 5C

These observations are retained as durable evidence even after resolution; closure records the re-test result rather than deleting the original finding.

| ID | Observation | Status | Closure evidence |
|---|---|---|---|
| 5C-UI-01 | Active Goal card/Goals section on the Progress screen could mask or obscure Progress information underneath it. Existing Progress content had to remain fully reachable and readable. | VERIFIED FIXED | Project Owner re-ran the updated app on the emulator and confirmed the overlap/obscuring issue is resolved. Gemini's implementation report attributes the fix to integrating the Goals section into the Progress scrollable layout instead of using a Box overlay. |
| 5C-UI-02 | BMI on Profile & Settings appeared faded/disabled-looking even though it was intentionally read-only. BMI should be normally readable while remaining non-editable. | VERIFIED FIXED | Project Owner re-ran the updated app on the emulator and confirmed BMI is readable while remaining read-only. Gemini's implementation report attributes the fix to separating disabled alpha presentation from the non-interactive state for this item. |
| 5C-AUTH-01 | Google Drive backup/restore initially displayed an OAuth credential refresh error. Signing out and signing in again restored the cloud backup/restore flow. | RESOLVED / NOT A DEFECT — current cycle | Do not modify Drive authentication based on this observation alone. Re-open only if the error becomes reproducible after valid re-authentication or new evidence establishes a product defect. |

### Observation-management rule

A new manual-test observation never erases a previous unresolved observation. Every meaningful finding must have an explicit lifecycle (`OPEN`, `IN PROGRESS`, `VERIFIED FIXED`, `DEFERRED`, `NOT REPRODUCED`, or `RESOLVED / NOT A DEFECT`). UI/runtime findings are not closed by code compilation or an agent completion report alone; the original behavior must be re-tested with appropriate runtime evidence.

## Evidence rule

Do not mark a current-cycle test PASS from code inspection or an agent report alone.

For Phase 5B, rows that depend on instrumentation execution distinguish implementation/manual evidence from unavailable connected-device execution. The merged PR explicitly records that instrumentation tests were implemented but not executed locally because no connected device was available.

For Phase 5C, `[ ]` rows are intentionally unverified until the relevant implementation and actual runtime/test evidence exist.

The populated-database replacement test is a critical regression boundary for any restore-path change.

For merged PRs, preserve the distinction between PR-level merge verification and individual scenario evidence. Do not infer PASS for untested rows.

For active manual findings, preserve the distinction between `OPEN`/`IN PROGRESS` observations and verified fixes. A UI observation remains a current-cycle defect until the original behavior is re-tested successfully or it is explicitly deferred with Project Owner acceptance.
