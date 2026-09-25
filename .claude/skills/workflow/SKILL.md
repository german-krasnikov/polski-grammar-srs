---
name: workflow
description: "Coordinate Kotlin Multiplatform work and the existing React/TypeScript migration baseline through Architect → Developer → Tester → Reviewer, with explicit handoffs, proportional quality gates and current verification evidence. Use for a requested full pipeline or substantial coordinated development; a trivial edit or standalone review can start at its relevant stage."
---

# Module development workflow

This skill belongs to the main agent coordinating work. It is not a separate workflow agent and does not impose a runtime, framework, package layout or build system. Specialists use the relevant handoff contract and return results; they do not dispatch each other.

## Scope and routing

1. Establish the target module, requirement, consumers, environment and current authorization. Read actual project instructions and configuration.
2. Reuse existing plans, approved steps, implementation and evidence. Resume at the first unfinished stage.
3. Use [module-architecture](../module-architecture/SKILL.md) for significant design, [kotlin](../kotlin/SKILL.md) for language/coroutine behavior, [code-style](../code-style/SKILL.md) for conventions and [testing-tdd](../testing-tdd/SKILL.md) for verification. For UI use [compose-multiplatform-ui](../compose-multiplatform-ui/SKILL.md) plus the affected [web](../kmp-web/SKILL.md), [macOS desktop](../kmp-desktop/SKILL.md), [Android](../kmp-android/SKILL.md) or [iOS](../kmp-ios/SKILL.md) skill. Use [playwright-testing](../playwright-testing/SKILL.md) for browser parity and [documentation-maintenance](../documentation-maintenance/SKILL.md) for documentation. Load only what applies.
4. A simple edit does not need a full pipeline. A standalone review starts at reviewer; a question about workflow should be answered without starting implementation.

## Roles and execution

```text
Requirement and target
  → senior-architect: grounded plan and contracts
  → senior-developer: blueprint if needed, then implementation and checks
  → senior-tester: independent acceptance checks and reproducible evidence
  → code-reviewer: inspect final diff and evidence
      → blockers: developer fixes, architect revises only if design must change
      → no blockers: main agent reconciles completion and reports
```

Use the role configuration for the current client. Agent configurations and model choices are separate; both clients use the same skills.

| Role | Claude | Codex |
|---|---|---|
| Architect | [senior-architect](../../../.claude/agents/senior-architect.md) | [senior-architect](../../../.codex/agents/senior-architect.toml) |
| Developer | [senior-developer](../../../.claude/agents/senior-developer.md) | [senior-developer](../../../.codex/agents/senior-developer.toml) |
| Tester | [senior-tester](../../../.claude/agents/senior-tester.md) | [senior-tester](../../../.codex/agents/senior-tester.toml) |
| Reviewer | [code-reviewer](../../../.claude/agents/code-reviewer.md) | [code-reviewer](../../../.codex/agents/code-reviewer.toml) |

The developer owns test-first implementation and production fixes; tester adds focused regression/acceptance checks and runs relevant verification; reviewer assesses the final diff and evidence read-only. Documentation is maintained by developer/main agent using the documentation skill.

Invoke available agents when delegation is authorized. If the client cannot invoke them, apply the role contracts sequentially and disclose that no independent review occurred. Keep dependent stages sequential; parallel work needs independent scope and clear file/runtime ownership.

## Gates

| Gate | Evidence |
|---|---|
| Design, for substantial work | Persisted plan with actual paths/symbols, API, ownership, compatibility and acceptance cases |
| Blueprint, when needed | Concrete steps/tests/risks accepted by the main agent; skip when already authorized or trivial |
| Testable behavior | Observed relevant RED → GREEN and successful affected checks, per testing skill |
| Independent verification, for substantial work | Tester maps acceptance cases to current results and reports failures or missing required evidence |
| Review | No unresolved Critical/Major findings against the actual contract |
| Documentation | Required public examples/API notes updated with the behavior |
| Completion | Final diff meets the request; evidence is current; remaining limitations are explicit |

Blueprint approval is an internal coordination step. Do not ask the user to approve an already authorized implementation again. New decisions outside scope need the relevant input; elapsed time is not approval.

## Handoff contracts

### Main agent → Architect

Pass requirement, target module, language, consumers/runtimes, scope/authorization, known constraints and real entrypoints. Name an existing or intended plan location within the allowed target.

Architect returns a persisted plan with concrete symbols or clearly marked NEW files, public contracts, state/resource ownership, choices/tradeoffs and acceptance tests.

### Main agent → Developer

Pass plan path, target, implementation/blueprint approval status, ordered behavior scenarios, relevant instructions/skills, available checks, baseline diagnostics and user changes to preserve.

Developer returns a blueprint only when required; otherwise implements the accepted scope. Required API documentation/examples belong with the implementation, not an unassigned future stage.

### Main agent → Tester

Pass the accepted plan and acceptance IDs, exact implementation/test diff, developer evidence, tested revision/artifact, target environments, available commands and assigned test-file ownership. The tester may add focused tests and test-only fixtures/harness changes; production fixes remain with the developer.

Tester returns PASS / FAIL / NOT RUN per required criterion, commands and environments, reproducible failures, any test diff and remaining gaps. Reuse current evidence for unchanged behavior; run missing or affected checks. Documentation-only work needs static validation, not application suites. Tester findings go through the main agent to the developer; after fixes recheck the failed and affected cases before review.

### Main agent → Reviewer

Pass the plan/criteria, exact changed paths and diff baseline, contract decisions, required documentation changes and developer/tester evidence, including tester-authored tests. Evidence includes commands, working directories, actual results and NOT RUN gaps for tests/lint/compilation/target builds/integration.

Reviewer is read-only. Clean/minor approval uses a short verdict + scope + evidence digest. Rejection names severity, path/line, trigger, consequence, correction direction and missing acceptance evidence.

### Rejection → correction

Send the developer concrete blockers, current plan and evidence needed to resolve them. Return to architect only when the contract/design needs revision. Assign missing verification or test-only defects to tester. After fixes run failed checks first, then affected checks through tester, and review the updated relevant diff. Do not repeat an unchanged failure without a new corrective action or evidence.

## Completion and limits

- Report checks separately. A passing build, test or review does not imply all other checks passed. Missing optional evidence is not automatically a code defect; required unverified criteria remain unresolved.
- Do not impose repository-wide tests or a fixed order of unrelated technology suites. Use the target runner and dependency graph.
- Preserve plans and evidence unless cleanup is part of the authorized task. Do not remove failing tests to manufacture a passing gate.
- Required module documentation is handled by developer/main agent using [documentation-maintenance](../documentation-maintenance/SKILL.md). A separate documentation agent is not part of this four-role workflow.
- Commits, version bumps, publication and deployments are separate scope decisions, not automatic consequences of completing this pipeline.
- Final report: behavior changed, module/entrypoints, actual validation, review status and remaining limitations. If asked only for the next handoff, return the next role, concrete inputs and unresolved gate.

For this migration, use `Plans/Kotlin/Plan.md`. The React↔Kotlin parity gate for stage 11 was dropped 2026-09-25 in favor of per-host native UI (DOM/CSS web, Compose M3 Android, SwiftUI iOS/macOS); the web parity gate no longer blocks native work, though the React implementation remains a useful behavior reference. Gradle task names must come from the actual build; do not claim native or browser checks from common/JVM tests. Documentation-only setup needs link/frontmatter/diff checks, not invented RED evidence.

This workflow is a coordination convention for the included roles. It does not claim to be a language-standard requirement.
