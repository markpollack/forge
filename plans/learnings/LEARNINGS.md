# Learnings: Forge

> **Last compacted**: 2026-03-01T12:30-08:00
> **Covers through**: Stage 1 (Steps 1.0–1.2)

This is the **Tier 1 compacted summary**. Read this first for the current state of project knowledge.

---

## Key Discoveries

1. **Deterministic customization >> LLM for scaffolding** — Package rename, POM updates, file generation are mechanical. TemplateCustomizer does it in ~1 second vs minutes with LLM (which was also unreliable — partial completion). LLM should only be used for creative/interpretive tasks.
   - *Source*: Step 1.1 implementation
   - *Impact*: Core architectural decision — two-phase approach

2. **Template must compile as-is** — No placeholders. Template uses `com.example.experiment`, forge renames deterministically. Users who don't customize still get a working project.
   - *Source*: User requirement
   - *Impact*: Template is a real, standalone project

3. **refactoring-agent tools are reusable patterns** — `javax-to-jakarta` regex pattern works for package rename. `pom-upgrader` dom4j approach works for XML edits. All follow `transformDirectory(Path) → TransformationReport`.
   - *Source*: Step 1.1 research
   - *Impact*: Future extraction into shared `java-refactoring-tools` library

4. **Nested Claude invocation needs `claude-run.sh`** — Claude Code 2.1.39+ blocks nested `claude` CLI. Use `~/scripts/claude-run.sh` (systemd-run escape) when AgentClient spawns Claude.
   - *Source*: Step 1.1 debugging
   - *Impact*: Required for any forge command that uses AgentClient

## Patterns Established

- **Two-phase customization**: Deterministic (TemplateCustomizer) then optional LLM (AgentClient via Haiku)
- **Template repo as compilable project**: `com.example` default, forge renames to brief's package
- **Brief YAML as contract**: Defines package, GAV, agents, judges, variants, knowledge files, dataset items
- **Package rename**: File moves + `package`/`import` statement string replacement (line-based, not AST)

## Deviations from Design

| Design says | Implementation does | Why |
|-------------|-------------------|-----|
| AgentClient does ALL customization | TemplateCustomizer does deterministic, LLM optional | Reliability + speed |
| BSL license | No license file yet | Deferred to first public release |

## Common Pitfalls

- Java 17 target — template was initially set to 21, machine runs 17
- `CascadedJury` has private constructor — must use `.builder().tier(...).build()`
- `@Nullable` on record compact constructor parameters fails with jspecify type-use annotations
- Empty parent directories left behind after package move — need `cleanEmptyParents()` walking up to srcRoot

---

## Revision History

| Timestamp | Change | Trigger |
|-----------|--------|---------|
| 2026-03-01T11:35-08:00 | Initial draft | Plan conversion |
| 2026-03-01T12:30-08:00 | Added Steps 1.1–1.2 learnings | Bootstrapping session |
