---
name: code-style
description: "Apply Kotlin code conventions, KDoc and existing formatter/linter rules in shared and platform code. Use for implementation or style review; Kotlin semantics belong to kotlin and Compose layout/state to compose-multiplatform-ui."
---

# Kotlin code style

## Authority and scope

Follow explicit project rules, configured tooling and nearby code. Where no convention exists, use the selected fallback below from the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). Style is a team convention, not language law. Preserve legacy React/TypeScript formatting during the migration.

## Fallback for new Kotlin code

| Item | Selected convention |
|---|---|
| Text | UTF-8, consistent line endings, final newline |
| Indentation | Four spaces; no alignment padding |
| Syntax | Omit unnecessary semicolons; use trailing commas in multiline declarations |
| Packages | Lowercase names reflecting responsibility |
| Types/files | `PascalCase`; file name follows its main declaration or cohesive purpose |
| Functions/properties | `camelCase`; `PascalCase` for `Unit`-returning composables |
| Constants | `UPPER_SNAKE_CASE` for constants; normal local `val` stays camelCase |
| Values | Prefer `val` and read-only collection interfaces |
| Platform files | Descriptive platform suffix for corresponding top-level declarations |
| Arguments | Named arguments when booleans or same-typed parameters hide meaning |

Group related declarations instead of enforcing one class per file. Prefer readable expressions, early returns and explicit lambda names when nested. Keep wrapping under one formatter; line length is a readability choice.

## Readability and contracts

- Name units (`elapsedMillis`, `retentionRate`) and domain concepts. Do not rename public symbols for cosmetic consistency.
- Keep inference for obvious locals; make API boundary types and visibility deliberate. `internal` is module visibility, not a substitute for deciding module boundaries.
- Use `when` for meaningful alternatives and sealed results where they describe a closed domain. Do not introduce wrappers or extension functions simply to look idiomatic.
- Avoid nested scope-function chains, opaque `it` variables and dense expression bodies. Choose the clearest control flow.
- Treat `!!`, unsafe casts and broad suppressions as exceptional, justified choices rather than routine null/error handling. Actual null-safety rules live in [kotlin](../kotlin/SKILL.md).
- Document non-obvious behavior with KDoc: units, mutation, errors, cancellation, persistence compatibility and resource ownership. Link symbols and parameters; avoid narrating obvious types. [KDoc](https://kotlinlang.org/docs/kotlin-doc.html)
- Label review preferences as style. An optional convention is not a correctness blocker.

## Tooling

- Inspect build files, `.editorconfig`, IDE settings and CI before selecting checks. This skill does not require ktlint, detekt, Spotless or any specific plugin.
- If formatter/static-analysis setup is in scope, select compatible maintained tooling, pin it in build metadata, and distinguish formatting checks from semantic analysis. Verify its Kotlin and target support from official tool documentation.
- Use one formatting authority and avoid competing formatter rules. Review autofix diffs and exclude generated sources/build output.
- Scope checks to changed files/modules when supported. Do not mass-format the React baseline or unrelated Kotlin files as part of a behavior change.
- A static analyzer passing does not prove coroutine safety, serialization compatibility or platform support; use appropriate tests/builds.

## Anti-patterns

| Avoid | Prefer |
|---|---|
| Presenting preferences as Kotlin requirements | State the convention and follow project configuration |
| Blind `!!` or `as` to satisfy compilation | Model nullability or validate the boundary |
| Comments repeating each line | Explain constraints, intent and public behavior |
| Blanket lint suppression | Fix the issue or justify a narrow exception |
| Formatting the whole repository during migration | Keep changes scoped and reviewable |

For composable parameter/API conventions use [compose-multiplatform-ui](../compose-multiplatform-ui/SKILL.md). Official sources checked 2026-09-23; this is a chosen project fallback.
