# Forge

Spring Initializr for agent experiments — creates runnable experiment projects from a YAML brief.

## Build

```bash
./mvnw compile     # Compile
./mvnw test        # Run tests
./mvnw package     # Build JAR
```

## Usage

```bash
forge new --brief path/to/brief.yaml --output ~/projects/my-experiment/
```

## Implementation Progress

**Source of truth**: `plans/ROADMAP.md` — execute steps individually, capture learnings at each step.

## Architecture

- `ForgeApp` — Main CLI entry point, routes commands
- `ExperimentBrief` — Parses YAML brief into structured data
- `TemplateCloner` — Clones `markpollack/agent-experiment-template` via git
- `CustomizationPromptBuilder` — Generates AgentClient prompt from brief

## Dependencies

- `agent-client` (0.10.0-SNAPSHOT) — AgentClient for code customization
- `spring-ai-claude-agent` — ClaudeAgentModel for agent execution
- SnakeYAML — Brief parsing
- Jackson — JSON/YAML serialization
