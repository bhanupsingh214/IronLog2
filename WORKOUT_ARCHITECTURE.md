# IronLog Workout Architecture

This document describes the decoupled architecture of the workout execution layer in IronLog.

## Aggregate Root: Workout Session

The `WorkoutSessionAggregate` is the source of truth for an active or historical workout. It is composed of four logical sections that protect data integrity and separate concerns.

### 1. Session Metadata
**Responsibility**: Identity, Lifecycle, and Timestamps.
- Tracks the current state of the workout (Created, InProgress, Paused, Completed, Discarded).
- Owns the official `startedAt` and `endedAt` timestamps.
- Manages the session ID used for relating all execution data.

### 2. Snapshot (Immutable)
**Responsibility**: Preservation of the training plan.
- A deep-copy of the Program Blueprint taken at the moment the workout starts.
- Includes Exercise Name, Muscle Group, Equipment, and the Prescription (Target Sets, Reps, RPE, Rest).
- **Rule**: Once created, the Snapshot never changes. Edits to the Exercise Library or Program Templates will never alter historical workout records.

### 3. Execution (Mutable)
**Responsibility**: Capturing actual performance.
- Records what the user actually did: Actual Weight, Actual Reps, Actual RPE.
- Tracks set completion and exercise status (Completed, Skipped, InProgress).
- Owns user-entered notes specific to this session.

### 4. Derived Statistics
**Responsibility**: Calculated insights.
- Not stored directly in the database (unless cached for performance).
- Calculated on-the-fly from the Snapshot and Execution data.
- Includes: Total Volume, Percentage Completion, Duration, and PR detection.

---

## Future Extension Points

The aggregate architecture is designed to support the following upcoming features without requiring core structural changes:

### Advanced Set Types
- **Warm-up Sets**: Supported by the `setType` field in the Execution data.
- **Drop Sets / Failure Sets**: Can be added as new `setType` values or flags in the `SessionSet` entity.

### supersets
- The `ExerciseSnapshot` contains an `order` field. Supersets can be implemented by adding a `supersetGroupId` to the snapshot, allowing the UI to group exercises while maintaining individual snapshot integrity.

### AI Coach & Analytics
- The separation of Snapshot (Plan) vs. Execution (Actual) allows the AI Coach to calculate **Adherence Metrics** and suggest progressive overload by comparing the blueprint to historical performance.

### Cloud Sync & Sharing
- The independent, immutable nature of the `WorkoutSessionAggregate` makes it the perfect unit for serialization. Completed workouts can be exported as a single JSON object for backup or community sharing.
