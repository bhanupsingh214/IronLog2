# IronLog — Engineering Invariants

**Documentation version:** v3.1
**Status:** NON-NEGOTIABLE unless a durable decision explicitly changes them.

## Data integrity

1. Never invent required IDs or foreign keys.
2. `0L` is valid only where the model explicitly defines it as a special marker, including manual/historical PR session markers.
3. Preserve canonical physical exercise identity where the data model requires it.
4. Never guess ambiguous identity.
5. Preserve historical workout snapshots.
6. Do not silently drop unsupported backup data.

## Restore safety

7. **Validate before destructive mutation.**
8. Restore must remain transactional.
9. Keep foreign-key enforcement enabled.
10. Required relationship failure must be explicit and must prevent a successful-looking partial restore.
11. A failed download or validation must leave existing local data unchanged.
12. Restoring over populated data must replace existing data according to the established restore contract, not append duplicates.

## Portability

13. `.ironlog` is the canonical portable backup artifact.
14. Storage providers must not redefine backup semantics.
15. Google Drive cloud restore must reuse the established validation/import/restore engine.
16. Do not create a second restore engine for a different transport.

## Compatibility

17. Backward compatibility must be deliberate and tested.
18. Deserialization success alone does not prove semantic compatibility.
19. Format/schema changes require export/import/restore and regression review.

## Account/cloud safety

20. Do not assume Drive authorization remains valid after account changes.
21. Do not restore data from an authorization context that cannot be tied to the active account.
22. Cloud failures must fail safely and must not corrupt or partially replace local data.

## Scope

23. One PR, one coherent objective.
24. Explicit non-goals are binding.
25. Correctness/security discoveries required by the approved objective may be fixed; unrelated improvements must be deferred.

## Quality

26. Build must pass.
27. `git diff --check` must be clean.
28. Only intended files may be committed.
29. No temporary debug logging in production.
30. No IDE/environment noise.

## Verification discipline

31. Runtime claims require runtime evidence.
32. Build claims require actual build output.
33. Git claims require actual Git/GitHub state.
34. Schema claims require repository/schema evidence.
35. Agent completion reports are evidence, not proof.
36. Fresh-device testing is required for portability behavior when practical.

## Documentation

37. Documentation never outranks current repository evidence.
38. Unknown facts are labeled `TBD`, not invented.
39. Canonical Project Resources must not contain conflicting active-state claims.
40. A materially rebuilt resource stack requires the three-pass documentation audit.
