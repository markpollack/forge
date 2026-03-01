# Learnings: Forge

> **Last compacted**: 2026-03-01T11:35-08:00
> **Covers through**: Stage 1 (initial implementation)

This is the **Tier 1 compacted summary**. Read this first for the current state of project knowledge.

---

## Key Discoveries

1. **Template + AgentClient customization pattern works well** — Clone a standard template, then use AgentClient to refactor package names, update pom.xml, and generate domain-specific stubs. The agent does ALL customization via a single prompt.
   - *Source*: Stage 1 implementation
   - *Impact*: Validates the "Spring Initializr for agents" metaphor

2. **SnakeYAML handles brief parsing simply** — No need for a schema validation library at this stage. Simple map-based parsing with defaults covers the brief format.
   - *Source*: ExperimentBrief.parse()
   - *Impact*: Keep parser simple until brief format stabilizes

## Patterns Established

- **Brief YAML as contract**: The brief defines everything the forge needs — package, GAV, agents, judges, variants, knowledge files, dataset items
- **Agent-does-all customization**: The CustomizationPromptBuilder generates a single comprehensive prompt; the agent handles file moves, renames, and content generation

## Deviations from Design

| Design says | Implementation does | Why |
|-------------|-------------------|-----|
| BSL license | No license file yet | Deferred to first public release |

## Common Pitfalls

_(none yet)_

---

## Per-Step Detail Files (Tier 2)

| File | Step | Topic |
|------|------|-------|
| _(Stage 1 was pre-committed)_ | 1.0 | Initial scaffolding |

---

## Revision History

| Timestamp | Change | Trigger |
|-----------|--------|---------|
| 2026-03-01T11:35-08:00 | Initial draft | Plan conversion |
