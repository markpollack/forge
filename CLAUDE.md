# Forge

Creates agent experiment projects from a YAML brief — clone template, customize deterministically, run.

## Build

```bash
./mvnw compile     # Compile
./mvnw test        # Run tests
./mvnw package     # Build JAR
```

## Usage

```bash
./mvnw exec:java -Dexec.args="new --brief path/to/brief.yaml --output ~/projects/my-experiment/"
```

## Implementation Progress

**Source of truth**: `plans/ROADMAP.md` — execute steps individually, capture learnings at each step.

## Architecture

Two-phase approach: deterministic customization first, optional LLM for creative tasks.

- `ForgeApp` — Main CLI entry point, routes commands
- `ExperimentBrief` — Parses YAML brief into structured data
- `TemplateCloner` — Clones `markpollack/agent-experiment-template` via git
- `TemplateCustomizer` — Deterministic customization: package rename, POM GAV, file generation (~1 second)
- `CustomizationPromptBuilder` — Generates AgentClient prompt (currently unused, available for creative tasks)

### Template

- Repo: `markpollack/agent-experiment-template`
- Default package: `com.example.experiment` (compilable as-is)
- Forge renames to brief's package via line-based string replacement + file moves

## Dependencies

- SnakeYAML — Brief parsing
- Jackson — JSON/YAML serialization
- SLF4J + Logback — Logging
- `agent-client` (0.10.0-SNAPSHOT) — Available for optional LLM customization
- `spring-ai-claude-agent` — ClaudeAgentModel for agent execution
