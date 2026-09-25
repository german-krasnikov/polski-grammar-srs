---
name: documentation-maintenance
description: "Keep polski-grammar-srs user docs, Kotlin migration plans and contributor skills/agents consistent with source and verification evidence. Use for documentation-only work or implementation handoffs; do not implement source, publish or change release versions."
---

# Documentation maintenance

## Non-negotiables

1. Implementation/tests define observed current behavior; `docs/product-contract.md` records the intended learning product. The Kotlin plan is future work until evidence says otherwise. Report conflicts rather than making prose choose a convenient behavior.
2. Keep one canonical explanation per fact. Other surfaces summarize and link.
3. Edit affected content surgically; preserve unrelated wording, language, structure and generated markers if present.
4. User docs and contributor workflows have different audiences. Shared skills and client-specific agent configurations are contributor guidance; `README.md` and `docs/` explain the product.
5. Historical verification remains dated history. A past test count or passing run cannot be relabelled current.
6. Do not invent source-project policies, generators, `spec/` submodules or npm release scripts that do not exist here. Inspect actual configuration before naming commands.
7. Versions, tags, commits, publication and deployment are separate authorization decisions. Documentation maintenance does not authorize them.

## Ownership map

| Surface | Audience and purpose |
|---|---|
| `README.md` | Product use, supported capabilities and real run/build instructions |
| `docs/product-contract.md` | Agreed learning behavior and scope |
| `docs/verification.md` | Dated baseline evidence and known gaps |
| `Plans/Kotlin/Plan.md` | Ordered migration, decisions, gates and target status |
| `.claude/skills/**`, `.agents/skills/**` | Canonical shared skills and Codex discovery links to the same files |
| `.claude/agents/**`, `.codex/agents/**`, `.codex/config.toml` | Separate client configurations with aligned role contracts and client-specific models |
| `AGENTS.md`, `CLAUDE.md` | Agent discovery, routing and repository instructions |
| Build metadata | Actual versions, tasks and dependencies; read as evidence |
| `CHANGELOG.md`, if present | Factual unreleased changes only when relevant |

## Workflow

### 1. Establish the change set

Read the caller's scope, accepted plan, diff baseline and verification evidence. Inspect staged and unstaged changes plus relevant committed changes. Use a supplied base ref; otherwise derive the actual upstream/default-branch merge-base rather than a fixed commit count or assumed `master` branch.

For documentation-only work, inspect the relevant source/configuration and primary external sources. Do not trigger implementation or app test suites simply because prose changed.

### 2. Build a coverage ledger

For substantive changes record affected surfaces and an action. At completion resolve each to `updated`, `checked - unchanged` or `not applicable` with a reason and concrete source evidence. A short handoff ledger is sufficient; do not create a new tracking document for a trivial edit.

| Changed area | Surfaces to inspect |
|---|---|
| Grammar, chains, grading, FSRS | Product contract, relevant user examples, parity plan |
| Progress format, time, storage | Import/export guidance, migration risks, recovery cases |
| Screen, layout, input, animation | User guidance if behavior changes; platform acceptance cases |
| Shared/platform boundaries | Architecture skill, plan, actual Gradle/source-set docs |
| Build/tooling/CI | README commands, plan gates, agent routing and testing skill |
| Role/skill changes | Relative links, YAML/TOML configuration, discoverability, handoff contracts and consistency of paired role prompts |
| Added/moved docs | Inbound links, anchors and the nearest index/navigation |

### 3. Verify and edit

- Trace behavioral claims to implementation and meaningful tests. Label unexecuted checks `NOT RUN` and proposed code `NEW`/planned.
- Keep Kotlin snippets compatible with their declared target and actual dependency versions. Legacy TS examples remain TS while they document the baseline.
- Use professional plain language in the document's established language. Agent guidance should emphasize decisions, contracts and failure recovery; user docs explain tasks and expected results.
- Link current primary sources for external claims and record the check date. Distinguish verified platform capability from selected team conventions and unresolved experiments.
- Version facts belong in build metadata or the dated compatibility decision. Avoid copying unstable version numbers across every skill.
- Preserve curated assets and examples unless the task requires changing them. Edit generated output through its real generator if one exists.

### 4. Validate and reread

- Run `git diff --check`; remember untracked files need direct inspection too.
- Resolve changed local Markdown links and anchors relative to each file; verify agent skill targets exist.
- Parse Claude YAML frontmatter and Codex TOML separately. Preserve each client's model choice, keep paired role instructions aligned and verify `.agents/skills/` links resolve to the canonical skills.
- Validate skill frontmatter/names with the available skill validator. If unavailable, perform equivalent static validation and state its limits.
- Check examples against source/API signatures; run a focused example check only if warranted and configured.
- Run existing documentation generators/checks when their output is affected. Do not invent `docs:check` or `readme:check` commands.
- Re-read edited files and review the diff. Tool exit zero alone does not establish truthful instructions or complete coverage.
- After a failed check, change the cause before retrying. Stop and report an external blocker rather than weakening validation.

## Conditional skills

Use [module-architecture](../module-architecture/SKILL.md) for module/contract descriptions, [kotlin](../kotlin/SKILL.md) for language claims, [testing-tdd](../testing-tdd/SKILL.md) for evidence and [workflow](../workflow/SKILL.md) for coordinated handoffs. Read platform/UI skills only when the changed document makes those claims.

## Completion report

Return scope/base, changed files with reasons, coverage ledger, validation results, reread evidence and remaining discrepancies. A documentation-only result should say application tests/builds were not rerun. Leave work uncommitted unless committing is explicitly authorized.

Adapted from the source project's documentation-maintenance protocol for this repository on 2026-09-23; DSL-specific publication and release infrastructure is intentionally not inherited.
