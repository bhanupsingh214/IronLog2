# IronLog — Active PR Specification

**Documentation version:** v3.4  
**As of:** 2026-08-15  
**Status:** NO ACTIVE PR / PHASE 4 CLOSEOUT COMPLETE  
**Current PR:** None  
**Implementation authorization:** None

## 1. Current authorization state

PR4.5 — Google Drive Cloud Restore was approved and implemented, then merged as GitHub #31.

GitHub #32 separately fixed the Finish Workout confirmation duration issue and was merged after runtime verification.

The connected-test data-safety mitigation was verified and documented as D033.

The Phase 4 implementation/stability cycle and documentation closeout are now complete. No new product feature implementation is authorized by this document.

## 2. Completed PR4.5 boundary

PR4.5 delivered:
- Google Drive cloud-backup discovery/download;
- controlled local staging;
- reuse of the established validation/import/restore pipeline;
- account-bound Drive authorization using the active signed-in Google account;
- cloud restore UI/confirmation flow;
- instrumentation coverage for File-based backup validation;
- no schema/migration change.

GitHub #31:
- Head: `806f09e694699511d4c1ae11fcf11689b4b47df5`
- Merge: `6317c2f2c3aa12e56709d5b62cf600e8f1bca7d4`

Recorded PR verification:
- `.\gradlew.bat test` — BUILD SUCCESSFUL;
- `.\gradlew.bat clean assembleDebug` — BUILD SUCCESSFUL;
- `.\gradlew.bat connectedAndroidTest` — BUILD SUCCESSFUL, 5 tests passed on Pixel 10 / Android 17;
- real-emulator Google Drive restore flow manually verified;
- `git diff --cached --check` clean before commit.

## 3. Completed stability fix

GitHub #32 fixed the Finish Workout confirmation dialog so active sessions use live elapsed duration while completed sessions retain persisted duration.

Verification:
- JVM unit tests passed;
- `connectedDebugAndroidTest` — 7/7 passed;
- `clean assembleDebug` passed;
- manual emulator verification passed.

## 4. Connected-test data-safety mitigation

A development-test lifecycle issue was identified during the stability verification cycle: connected instrumentation tests could remove the production IronLog package after the run, wiping the local Room database on a data-bearing emulator.

Verified mitigation:

```properties
android.injected.androidTest.leaveApksInstalledAfterRun=true
```

Controlled verification:
- 7/7 connected tests passed;
- production package remained installed;
- Programs and History remained present;
- weekly-volume baseline remained intact;
- manual smoke test passed;
- zero data loss.

This is a test-environment safeguard, not a production backup mechanism.

## 5. Closeout requirements

Phase 4 closeout is complete:
- [x] PR4.5 merged and verified;
- [x] workout finish-duration fix merged and verified;
- [x] connected-test data-safety mitigation verified;
- [x] documentation PR merged;
- [x] canonical Project Resource stack three-pass audit completed;
- [x] fresh-chat handoff refreshed;
- [ ] next phase/PR explicitly selected by Project Owner.

The final unchecked item is intentionally an Owner decision boundary, not a documentation defect.

## 6. Next authorization rule

No new implementation should begin until a new objective is selected, scope/non-goals are locked, risks and acceptance criteria are defined, and the Project Owner explicitly approves the next active PR.

## 7. Owner approval boundary

The absence of an active PR in this document is intentional. It prevents roadmap candidates or conversation ideas from being treated as implementation authorization.
