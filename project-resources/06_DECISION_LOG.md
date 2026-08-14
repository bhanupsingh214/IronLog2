# IronLog — Decision Log

**Documentation version:** v3.2  
**As of:** 2026-08-14

## Durable Decisions

### D017 — Phase/PR roadmap tracking
Maintain a durable phase/PR development timeline and larger-picture roadmap. Future sequencing is planning inference unless explicitly approved.

### D018 — User-owned Google Drive as PR4.4 cloud boundary
PR4.4 uses the user's authorized Google Drive app-data area as the cloud storage/transport boundary rather than centralized IronLog-owned backup storage. This does not imply zero total cost; potentially billable Google/API/Firebase/Cloud/backend services must be evaluated separately.

### D019 — PR4.4 is upload-only cloud backup
PR4.4 implements cloud backup/upload only. Google Drive download/restore is a separate PR.

### D020 — Pre-implementation risk and mitigation gate
Every meaningful PR must undergo pre-implementation risk assessment. Material risks require reasonable mitigations/precautions and verification where applicable.

### D021 — Canonical Project Resource stack
There must be exactly one canonical copy of each Project Resource. Duplicate or stale copies are documentation defects.

### D022 — Three-pass documentation finalization audit
A rebuilt/materially updated resource stack is not finalized until it passes individual-file correctness, cross-document consistency, and stack-integrity audits.

### D023 — Documentation role separation
Each Project Resource has a defined responsibility and should not become a duplicate authority for another resource.

### D024 — PR4.5 cloud restore reuses the established restore pipeline
Google Drive cloud restore must download the existing `.ironlog` artifact and pass it through the established validation/import/restore pipeline. PR4.5 must not create a second cloud-specific restore engine.

**Reason:** `.ironlog` is the canonical portable backup artifact and the existing restore engine owns validation, identity remapping, transactional replacement, and relational safety.

### D025 — Cloud restore must prove account/authorization consistency
PR4.5 must not rely on stale Drive authorization across Google-account changes. Cloud restore is permitted only when the active signed-in account and active Drive authorization are valid and consistent.

**Reason:** cloud restore acts on user data and must not risk another account's backup being used after account switching.

### D026 — Cloud restore uses staged download before destructive mutation
The cloud `.ironlog` artifact must be downloaded/staged before restore mutation. Download and validation failures must leave existing local data unchanged.

### D027 — PR4.5 scope is cloud restore, not general cloud lifecycle management
PR4.5 includes cloud discovery/download/restore and correctness-critical account/authorization handling. General backup history, synchronization, scheduled behavior, Drive browsing, and unrelated lifecycle work remain outside scope.

### D028 — GitHub `/project-resources/` is the canonical documentation location
Project Resources are maintained in the repository under `/project-resources/` so every future chat/agent can fetch the current canonical stack without relying on uploaded copies.

**Reason:** repository-backed documentation provides version history, diffs, rollback, and one durable source instead of manually synchronized chat uploads.

### D029 — Fresh-chat continuity must be repository-first
A new chat should read the canonical resource stack and verify current repository/GitHub state before implementation reasoning. Conversation memory is continuity aid, not authority.

### D030 — Gemini audit is an implementation-readiness evidence gate
For meaningful PRs, Gemini must perform an audit-only repository inspection before implementation. ChatGPT reconciles that audit against the repository and locked PR. An audit finding does not become an implementation requirement until it is reconciled; material changes require the appropriate Project Owner approval.

**Reason:** the implementing agent can identify implementation-specific API, dependency, test, and source-level risks that should be caught before code changes begin, while preserving Project Owner authority and preventing unverified agent recommendations from becoming project truth.

### D031 — PR4.5 account-binding mechanism must be API-verified
PR4.5 requires active Google identity / Drive authorization consistency, but no specific account-binding API call is pre-approved until verified against the exact installed Google Identity Services library/API. Agent-proposed method signatures are not authoritative.

**Reason:** the Gemini audit correctly identified the account-binding requirement but proposed a specific API call that must be validated against the actual dependency contract before implementation.

### D032 — PR4.5 uses focused cloud-backup discovery
PR4.5 does not introduce a general `listBackups()`/backup-history API by default. Cloud discovery should locate the current IronLog backup using the established deterministic identity/filename behavior unless the actual approved UX proves broader listing necessary.

**Reason:** a generalized backup-history/listing capability would expand the PR beyond the approved cloud-restore objective without a demonstrated requirement.

## Decision Change Rule

When changing a durable decision:
1. identify the affected decision;
2. explain why it no longer applies;
3. assess compatibility/risks;
4. record the new decision;
5. update affected Project Resources.

Do not silently overwrite historical decisions.

## Boundary

This log records durable decisions and their reasons. It does not replace current state, architecture, roadmap, invariants, testing protocol, backup contract, schema history, regression evidence, active PR authorization, or collaboration protocol.

## Final Rule

> Record durable decisions and their reasons clearly enough that a future chat understands not only what IronLog does, but why important rules exist.
