---
name: compose-multiplatform-ui
description: "Implement or review shared Compose Multiplatform UI: state, effects, adaptive layout, learning-screen hierarchy, settings, accessibility and animation. Use for shared screens/components; load the relevant platform skill for host behavior."
---

# Shared Compose Multiplatform UI

Use the selected Kotlin/Compose versions and target source sets. Android documentation explains many shared APIs, but does not establish that an Android artifact is available for Wasm, JS or Native. Verify the actual dependency variants before adding it. Read [module-architecture](../module-architecture/SKILL.md) for state ownership changes and [code-style](../code-style/SKILL.md) for Kotlin conventions.

## State and effects

- Render immutable screen state and send user actions to its owner. Keep domain calculations, persistence and random exercise selection out of composable execution. Hoist state only as far as its consumers require; a local expanded panel does not automatically need a ViewModel. [State hoisting](https://developer.android.com/develop/ui/compose/state-hoisting)
- Distinguish ephemeral UI state, restorable navigation/input state and durable learning progress. `remember`, `rememberSaveable` and ViewModel retention do not replace a progress repository or prove recovery after process termination.
- Prefer components that receive values and callbacks, with an optional `modifier: Modifier = Modifier` applied to their root. Do not pass a whole application controller to every leaf component.
- Run UI work in lifecycle-owned effects. Choose `LaunchedEffect` keys to express restart conditions; use `rememberUpdatedState` when a changing callback must be observed without restarting. Use `DisposableEffect` to unregister owned listeners. Do not launch work directly while rendering. [Effects](https://developer.android.com/develop/ui/compose/side-effects)
- Choose collection/lifecycle APIs supported by the selected multiplatform artifacts. Inspect host lifecycle mappings rather than assuming every browser or iOS state transition matches an Android activity. [Multiplatform lifecycle](https://kotlinlang.org/docs/multiplatform/compose-lifecycle.html)

## Layout and text

- Adapt to available window/container constraints, not device names. Use bounded readable content widths, intentional single/two-pane transitions and a clear scrolling owner. Do not put an unbounded lazy list inside a same-direction scrolling container.
- Keep essential sentences, answers and explanations readable at large text sizes. Avoid fixed heights or ellipsis for learning content. Wide reference matrices may use their own labelled horizontal scrolling region; ordinary forms and cards should reflow.
- Handle system bars, safe areas and software keyboard insets at a defined boundary. Avoid applying the same inset twice. Exercise input and primary actions must remain reachable when the keyboard is open. [Host differences](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)
- Keep design tokens and reusable components small and explicit. Shared UI does not require identical navigation or typography on every host. Do not create a component framework for one screen.
- Use Compose resources for UI strings/assets when they improve localization and packaging. Keep grammar records as domain data. Verify font coverage for Polish diacritics and Russian text, resource loading and fallback behavior on each target. [Resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html)

## Accessibility and input

- Prefer standard interactive components and their semantics. Custom controls need an accessible name, role, state and action; avoid announcing an icon twice when adjacent text already names it.
- Keep keyboard focus visible and traversal aligned with visual order. Preserve text selection and standard text-field input behavior. Make correctness understandable without color alone.
- Hidden answers must be absent from the accessibility tree as well as visually hidden. When a card changes, old controls must not remain actionable during an exit animation. Accessibility behavior is part of the interaction contract.
- Verify semantics on actual targets. Compose UI tests or screenshots alone do not establish screen-reader behavior. [Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility)

## Learning-screen hierarchy and settings

- Give each study screen one dominant task: read prompt, respond/reveal, then judge recall. Show reference material and secondary controls on demand; keep the next action stable when the keyboard appears. Do not turn method preference into a claim about fixed “tech” or “humanities” brain types.
- Use a restrained semantic palette: neutral surfaces, one primary action, and consistent accents for old → new forms. Preserve the red source and distinct ending/target colors chosen in the product contract, but label the relation in text as well. Dense matrices may use stronger grid structure than a card. Sparse cues are a design hypothesis to verify with learners, not a guaranteed memory improvement.
- Define a small shared contract for spacing, readable line length, type scale, contrast, focus, touch size and motion intent. Map it to each host's native components and dynamic type; do not force pixel-identical mobile/desktop/web chrome.
- Expose three appearance choices: System, Light and Dark. System observes live OS/browser changes; manual choices persist across restart. Use semantic surface/text/error/source/target/focus tokens with distinct light and dark values, including dialogs and elevated cards. Check contrast and non-color labels in both palettes; switching appearance must keep the same card, typed draft, focus and FSRS history.
- Keep preference state separate from FSRS/review history. Changing theme, explanation method, native language, gesture choice or reminder schedule must not rate a card, reset a queue or silently alter saved progress. Define migrations and platform capability reporting before exposing new settings.
- For reminders, distinguish current due count from the next future due instant. A completed session can have zero due cards and a valid review tomorrow; schedule from persisted grammar **and** selected vocabulary queues without merging their FSRS histories. Notification permission, user preference and scheduled requests are three different states.
- Provide a visible control equivalent for every swipe/drag and disclose gesture directions at first use or in Settings. A partially dragged card never commits a review; commit only after release and a clear threshold, with one accessible action per rating.

Checked 2026-09-24: [W3C reflow and focus requirements](https://www.w3.org/WAI/WCAG22/understanding/), [Apple accessibility and motion guidance](https://developer.apple.com/design/human-interface-guidelines/accessibility), [instructional signaling meta-analysis](https://www.sciencedirect.com/science/article/pii/S1747938X17300581). The research supports testing concise cues; it does not validate a cognitive-type classifier.

## Animation decisions

Choose the smallest state-driven API that expresses the transition; check its availability for the pinned Compose version. [Animation API selection](https://developer.android.com/develop/ui/compose/animation/choose-api)

| Need | Starting point |
|---|---|
| One value follows a target | `animate*AsState` |
| Content enters or exits | `AnimatedVisibility` |
| Content/state replacement | `AnimatedContent` or `Crossfade` |
| Coordinated properties | `updateTransition` |
| Interruptible gesture/sequence | `Animatable` in an owned coroutine |
| Container size changes | `animateContentSize`, with deliberate modifier order |

- Motion explains a change; it must not determine whether an answer is revealed, rated or saved. Commit each action once in the state owner, independently of animation completion or cancellation.
- Keep rapid repeated input, reversal, navigation away and zero-duration motion correct. Stable content keys should identify the actual card/item, not a list position.
- Read the host's reduced-motion preference through a small adapter where framework support is incomplete. Check both initial preference and changes. Provide a non-moving path with the same content and controls; do not assume CSS changes affect canvas animation.
- Prefer translation/opacity when only visual movement is needed; animate layout only when sibling reflow is intentional. Do not add infinite motion to idle study screens. Durations/easing are design choices, not universal Kotlin standards.

## Performance and evidence

- Measure slow interactions in a representative release build before adding stability annotations or caches. Use stable lazy-list keys, avoid repeated expensive work during composition and use `derivedStateOf` only when it actually reduces observation frequency. A mutable object is not made immutable by annotating it. [Compose performance](https://developer.android.com/develop/ui/compose/performance/bestpractices)
- Test observable interaction and state transitions with controlled clocks when appropriate. Check small/large layouts, enlarged text, input, interrupted animation and reduced motion. Pixel identity across operating systems is not a correctness requirement.
- Report the exact target/runtime and distinguish UI tests, visual inspection, accessibility checks and release performance measurements. Use [testing-tdd](../testing-tdd/SKILL.md) for evidence rules and the host skill for platform checks.

Sources checked 2026-09-23. Design recommendations above are a synthesis; project-specific functional contracts take precedence over optional visual conventions.
