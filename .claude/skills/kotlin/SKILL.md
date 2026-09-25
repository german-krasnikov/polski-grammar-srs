---
name: kotlin
description: "Implement and review Kotlin language, standard-library, serialization and coroutine behavior in multiplatform code. Use for Kotlin correctness and JS-to-Kotlin semantic differences; architecture, formatting and Compose UI have separate skills."
---

# Kotlin semantics for multiplatform work

Read actual Kotlin/dependency versions and target configuration. This skill describes semantic pitfalls and project decisions; use [code-style](../code-style/SKILL.md) for formatting and [module-architecture](../module-architecture/SKILL.md) for boundaries.

## Values and type contracts

- Model absence with nullable types when absence is valid. Safe calls and Elvis expressions should encode an intentional default, not silently hide malformed external input. `!!` can throw; validate JSON/platform values before using them. [Null safety](https://kotlinlang.org/docs/null-safety.html)
- `val` prevents reassignment, not mutation of the referenced object. `List` is a read-only interface; mutable aliases can still change its contents. Copy at ownership boundaries when isolation is promised. [Collections](https://kotlinlang.org/docs/collections-overview.html)
- Data-class `copy()` is shallow; only primary-constructor properties participate in generated equality/copy behavior. Check nested state, arrays and mutable elements before using a data class as a snapshot/key. [Data classes](https://kotlinlang.org/docs/data-classes.html)
- Distinguish structural `==` from reference `===`; use content comparison for arrays. Do not port JavaScript identity/equality assumptions mechanically. [Equality](https://kotlinlang.org/docs/equality.html)
- Choose `Int`, `Long`, `Double`, instant/date and duration types from their contract. Kotlin integer division, conversion, overflow and floating-point behavior need explicit fixtures when replacing JavaScript calculations. Avoid unexamined epoch milliseconds in `Int`. [Numbers](https://kotlinlang.org/docs/numbers.html)
- Keep ordered data ordered where order is observable. Do not depend on incidental map/set iteration or platform locale defaults for Polish grammar and queue sorting.

## Errors and serialization

- Use exceptions for exceptional failures and typed outcomes where callers must handle domain alternatives. `require`/`check` describe programmer contracts; decoding user data still needs a recoverable validation path.
- Separate serialized DTOs from domain objects where it protects persisted compatibility. With `kotlinx.serialization`, choose field names, defaults, unknown-field policy, enum representation and date encoding explicitly. A compiler-generated serializer is not schema validation. [Serialization](https://kotlinlang.org/docs/serialization.html)
- Preserve the existing JSON contract during migration. Compare legacy missing fields, malformed content, timestamps and numbers through fixtures; do not silently replace unreadable progress with a new empty save.
- Scope experimental opt-ins to the affected declaration/module and explain the compatibility risk. Avoid global suppression as a substitute for selecting supported APIs.

## Coroutines and flows

- A `suspend` function does not inherently move work to a background thread. Use structured scopes; callers own request lifetime and platform owners own long-running jobs. Check dispatcher availability on every target. [Coroutines basics](https://kotlinlang.org/docs/coroutines-basics.html)
- Preserve cancellation through error boundaries. Catch specific expected failures; if catching a broad exception around suspending work, propagate `CancellationException` before converting other failures. `runCatching` can capture it too. Cancellation is control flow, not a user-facing storage/network failure. [CancellationException](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-cancellation-exception/)
- Release owned resources in `finally` or the platform lifecycle. Avoid fire-and-forget work, swallowed `async` failures and independent `Job`s that accidentally detach work from its owner.
- Expose read-only flow/state contracts. Decide whether collectors need latest state, every event, replay or buffering; do not use conflated state as an event queue. Verify API semantics against the selected coroutine version. [StateFlow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/)
- Make dispatcher/clock dependencies replaceable where tests need control. Time-based domain decisions must not read the machine clock or default zone invisibly.

## Migration checks

Translate observable behavior, not TypeScript syntax. Inventory null/undefined distinctions, defaulting (`0`/`false`/empty values), date parsing, regular expressions, rounding, string normalization and persistence. Reuse deterministic input/output fixtures; equal seeds in JavaScript and Kotlin do not imply equal random sequences.

Use [testing-tdd](../testing-tdd/SKILL.md) for common tests and real target evidence. Official sources checked 2026-09-23; portability and migration safeguards above are project conventions.
