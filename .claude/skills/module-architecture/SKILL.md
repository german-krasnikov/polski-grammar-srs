---
name: module-architecture
description: "Design and review Kotlin Multiplatform boundaries, portable contracts, source sets, dependencies, state ownership and migration compatibility. Use for substantial module/API/architecture changes; shared UI details belong to compose-multiplatform-ui."
---

# Kotlin Multiplatform module architecture

## Start with the consumer contract

Identify capability, callers, target runtimes, inputs/outputs, errors, cancellation and ownership before choosing folders or classes. Inspect actual callers, Gradle configuration and the React baseline. Mark new paths `NEW`; do not describe planned Kotlin modules as existing.

For this project, [the migration plan](../../../Plans/Kotlin/Plan.md) owns sequencing. Keep grammar, exercise chains and scheduling behavior independently testable. Web migration comes first; preserve export/import and existing progress until proven compatible. Shared UI is a product choice, not a consequence of using KMP.

## Design recommendations

These are project design conventions, not an official Kotlin architecture standard. Use SOLID/DRY/KISS as criteria rather than a required number of layers.

| Principle | Apply | Avoid |
|---|---|---|
| Cohesion | Keep one domain capability/invariant together | Universal managers and unrelated helpers |
| Narrow contracts | Expose only what callers need | Publishing storage details or mutable state |
| Dependency inversion | Inject a small replaceable capability at a real boundary | Interfaces/DI containers for every function |
| Substitutability | Preserve defaults, errors, ordering and side effects | Platform adapters silently strengthening requirements |
| Composition | Combine simple functions and adapters | Deep inheritance to share incidental code |
| DRY | Share established domain rules | Prematurely unifying superficially similar UI |
| KISS | Implement the accepted requirement | Speculative plugin systems and empty layers |

## Source sets and dependency direction

- Begin with the default source-set hierarchy. Add custom intermediate sets only for real shared implementations; manual `dependsOn` changes can affect the default template. Source sets share code; Gradle modules provide separate dependency/API boundaries. Do not create one module per screen by habit. [Hierarchy](https://kotlinlang.org/docs/multiplatform/multiplatform-hierarchy.html)
- `commonMain` uses common APIs. Keep `java.*`, Android framework, DOM/JS interop and Apple APIs in their supported platform source sets. A dependency marketed as KMP may not publish every target: verify Wasm, JS fallback when selected, JVM/Android and Native variants before choosing it. [Dependencies](https://kotlinlang.org/docs/multiplatform/multiplatform-add-dependencies.html)
- Pure grammar, queue decisions and schedule calculations should not depend on Compose, browser storage or platform lifecycle. UI calls the domain through explicit contracts; platform entrypoints assemble adapters.
- Prefer injected interfaces/functions for replaceable services. Use small `expect`/`actual` declarations where a platform capability genuinely requires them; do not mirror an entire application/service class in every target. Implement every declared target, with tests for adapter behavior. [Expected/actual declarations](https://kotlinlang.org/docs/multiplatform/multiplatform-expect-actual.html)
- Do not move Android-only ViewModel, saved-state, navigation or database APIs into common code by name alone. Verify the selected artifact/API supports every target; see [shared UI](../compose-multiplatform-ui/SKILL.md).

## Dependency and resource ownership

- Give mutable state one authority. Expose read-only state and events; document whether inputs/outputs are copied or borrowed. A read-only collection type does not guarantee deep immutability.
- Inject time and randomness where they affect outcomes. Separate wall-clock instants, local calendar days and monotonic durations. Time-zone/day-boundary behavior is part of the SRS contract.
- Attach coroutines to an explicit owner and lifetime. Avoid unowned `GlobalScope` jobs; define cancellation, repeated calls, stale responses and cleanup. Dispatchers are platform-dependent capabilities, not universal common defaults. [Coroutines](https://kotlinlang.org/docs/coroutines-basics.html)
- Prefer direct calls for request/response. Use events for notifications only, with ordering, collector lifetime and buffering understood.
- Keep storage/network side effects outside pure calculations. Decode external data through versioned DTOs, validate it, then map to domain types. Preserve unknown/legacy cases intentionally; do not overwrite unreadable progress.

Illustrative replaceable boundary, not a required repository layer:

```kotlin
interface ProgressStore {
    suspend fun read(): String?
    suspend fun write(serializedProgress: String)
}
```

Choose sync/suspend and error semantics from the actual platform contracts. A wrapper with no behavior or substitution value is unnecessary.

## Compatibility and builds

- Treat Kotlin, Compose, Gradle, AGP, JDK, Xcode and dependency versions as a verified set. Record selected versions in build metadata; consult the current [KMP compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html) before adopting an upgrade. Do not copy a tutorial version into the plan as a permanent requirement.
- Inspect actual Gradle targets/tasks and CI host capabilities. Common compilation is not proof that browser, Android or iOS artifacts build or run. [Gradle DSL](https://kotlinlang.org/docs/multiplatform/multiplatform-dsl-reference.html)
- Preserve public Kotlin APIs, serialized field names, defaults, enum values, timestamps and failure behavior across adapters. Changes can break stored data even when all code compiles.
- For this migration, evaluate any FSRS implementation against deterministic baseline fixtures and the locked `ts-fsrs` behavior. No library-name resemblance proves parity.
- For delivered libraries, verify the built artifact in a representative consumer. Publication and deployment require their own authorized scope.

## Architecture deliverable

Record scope, actual files/symbols or `NEW`, public contracts, source-set/target dependencies, lifecycle owner, relevant alternatives, compatibility risks, acceptance cases and documentation owner. Prefer a useful consumer example to an elaborate class diagram.

Use [kotlin](../kotlin/SKILL.md) for semantics, [testing-tdd](../testing-tdd/SKILL.md) for evidence, and [web](../kmp-web/SKILL.md), [Android](../kmp-android/SKILL.md) or [iOS](../kmp-ios/SKILL.md) only for affected platform contracts.

Official sources checked 2026-09-23. The architectural choices above are a project synthesis; source links establish platform constraints.
