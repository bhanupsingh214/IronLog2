# IronLog — Backup Portability Specification

**Documentation version:** v3.1
**As of:** 2026-08-14
**Status:** CANONICAL CONTRACT

## 1. Canonical artifact

The portable IronLog backup artifact is `.ironlog`.

The artifact is the portable data contract. Storage/transport providers do not redefine its meaning.

The archive currently contains:
- `data.json`;
- `metadata.json`;
- integrity/version information as defined by current implementation.

Exact serialization must be verified against source when implementation changes.

## 2. Required properties

A valid backup must provide enough information for the supported restore pipeline to:
- identify format/version;
- validate structure;
- validate integrity where supported;
- reconstruct supported user data;
- preserve required relationships and identity.

## 3. Validation-before-restore contract

Before destructive mutation:
1. validate archive structure;
2. validate required files;
3. validate supported version;
4. validate integrity/checksum where applicable;
5. validate structural/relational requirements of the restore pipeline.

If validation fails, existing local data remains protected.

## 4. Restore contract

```text
artifact
  ↓
validation
  ↓
BackupPayload
  ↓
transactional restore
  ↓
identity remapping
  ↓
Room
```

Restore must preserve:
- transactional replacement;
- foreign-key safety;
- canonical exercise identity;
- required relationship resolution;
- rollback on failure.

## 5. Compatibility

Older backups remain supported only where current implementation explicitly provides compatibility.

A file successfully deserializing does not prove semantic compatibility.

Any format change requires:
- export review;
- import review;
- restore review;
- compatibility review;
- integrity/version review;
- regression evidence;
- portability review.

## 6. Storage-provider independence

The contract remains independent of:
- local filesystem location;
- Google Drive;
- future cloud providers;
- UI.

## 7. PR4.4 Google Drive boundary

PR4.4 uses user-owned Google Drive `appDataFolder` as storage/transport for the existing `.ironlog` artifact.

It does not change the artifact format or restore engine.

## 8. PR4.5 cloud restore

PR4.5 is approved and planned.

Locked flow:

```text
Google Drive appDataFolder
        ↓
locate current IronLog `.ironlog`
        ↓
controlled download/staging
        ↓
existing validation/import boundary
        ↓
existing restore transaction
```

Cloud restore must not introduce a second backup parser or restore engine.

## 9. Cloud/account safety

The cloud restore path must verify valid signed-in/Drive-authorized state and must protect against stale authorization after account switching.

A missing backup, authorization failure, download failure, or validation failure is non-destructive.

## 10. Storage independence rule

Cloud implementation details must remain below the portable artifact boundary. A future storage provider should be able to transport the same `.ironlog` without changing its serialized meaning.

## Final rule

> `.ironlog` is the canonical portable backup artifact; transport and storage are separate concerns.
