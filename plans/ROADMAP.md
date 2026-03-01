# Roadmap: Forge

> **Created**: 2026-03-01T11:35-08:00
> **Last updated**: 2026-03-01T12:30-08:00
> **Status**: Stage 1 complete (Steps 1.0–1.2)

## Overview

Forge creates agent experiment projects from a brief — `forge new` scaffolds a runnable experiment project from a YAML brief. Two-phase approach: deterministic customization (package rename, POM GAV, file generation) then optional LLM for creative tasks. Template repo (`markpollack/agent-experiment-template`) uses `com.example` as generic default; forge renames to the brief's package via deterministic Java tooling.

> **Before every commit**: Verify ALL exit criteria for the current step are met. Do NOT remove exit criteria to mark a step complete — fulfill them.

---

## Stage 1: Initial Implementation (Complete)

### Step 1.0: Project Scaffolding

**Status**: Complete (committed: `457281c`)

**Deliverables**: ForgeApp CLI, ExperimentBrief YAML parser, TemplateCloner, CustomizationPromptBuilder, ExperimentBrief unit test.

---

### Step 1.1: Deterministic TemplateCustomizer

**Status**: Complete (committed: `d06fd0a`)

**Deliverables**: TemplateCustomizer replacing AgentClient-based customization. Handles package rename (file moves + declaration/import rewrites), POM GAV updates, dataset/config/prompt/knowledge file generation, template file renaming. Runs in ~1 second.

**Key decisions**:
- Deterministic Java code over LLM for mechanical scaffolding tasks
- Inspired by refactoring-agent `tools/javax-to-jakarta` regex pattern
- Template must be a compilable project (no placeholders) — `com.example.experiment`
- Package rename only when brief specifies a different package

---

### Step 1.2: Template Compilation Fixes

**Status**: Complete (committed: `05afb7b` template, `b5ef6fc` forge)

**Deliverables**: Template compiles on Java 17. Fixed CascadedJury.builder() usage, removed @Nullable from record components, set Java target to 17.

**Key decisions**:
- Template uses `com.example` as generic default (anyone can use without modification)
- `io.github.markpollack` is applied by forge when brief specifies it
- No tuvium references in template or forge source code

---

## Stage 2: Variant Execution (`forge grow`)

### Step 2.0: Design Review

**Entry criteria**:
- [ ] Read: `plans/ROADMAP.md`
- [ ] Read: ForgeApp.java — understand current CLI structure

**Work items**:
- [ ] DESIGN `forge grow` command: reads experiment-config.yaml, iterates variants, invokes ExperimentRunner per variant
- [ ] DESIGN variant result persistence (JSON to results/ directory)
- [ ] DOCUMENT API contract: `forge grow --config experiment-config.yaml`

**Exit criteria**:
- [ ] Design documented in this roadmap
- [ ] Create: `plans/learnings/step-2.0-grow-design.md`
- [ ] Update `CLAUDE.md` with distilled learnings
- [ ] Update `ROADMAP.md` checkboxes
- [ ] COMMIT

**Deliverables**: Design for `forge grow` command

---

### Step 2.1: Implement `forge grow`

**Entry criteria**:
- [ ] Step 2.0 complete
- [ ] Read: `plans/learnings/step-2.0-grow-design.md`

**Work items**:
- [ ] IMPLEMENT `GrowCommand` class — parses config, builds ExperimentVariantConfig
- [ ] IMPLEMENT variant iteration using ExperimentApp.runAllVariants()
- [ ] INTEGRATE with ForgeApp CLI (add "grow" command routing)
- [ ] WRITE unit test for config parsing

**Exit criteria**:
- [ ] `forge grow --config experiment-config.yaml` runs all variants
- [ ] All tests pass
- [ ] Create: `plans/learnings/step-2.1-forge-grow.md`
- [ ] Update `CLAUDE.md` with distilled learnings
- [ ] Update `ROADMAP.md` checkboxes
- [ ] COMMIT

**Deliverables**: Working `forge grow` command

---

## Stage 3: Comparison (`forge evaluate`)

### Step 3.0: Implement `forge evaluate`

**Entry criteria**:
- [ ] Stage 2 complete
- [ ] Read: all Stage 2 learnings

**Work items**:
- [ ] IMPLEMENT `EvaluateCommand` — loads results from ResultStore, runs ComparisonEngine
- [ ] IMPLEMENT growth-story.md generation from comparison results
- [ ] INTEGRATE with ForgeApp CLI
- [ ] WRITE test for comparison output

**Exit criteria**:
- [ ] `forge evaluate` produces analysis/growth-story.md
- [ ] Create: `plans/learnings/step-3.0-evaluate.md`
- [ ] COMMIT

**Deliverables**: Working `forge evaluate` command

---

## Stage 4: Graduation (`forge graduate`)

### Step 4.0: Design Agent Extraction

**Entry criteria**:
- [ ] Stage 3 complete

**Work items**:
- [ ] DESIGN agent extraction: best variant → standalone project
- [ ] DESIGN ACP marketplace packaging

**Exit criteria**:
- [ ] Graduation design documented
- [ ] COMMIT

---

## Learnings Structure

```
plans/learnings/
├── LEARNINGS.md              # Tier 1: Compacted summary
├── step-2.0-grow-design.md
├── step-2.1-forge-grow.md
├── step-3.0-evaluate.md
└── step-4.0-graduate-design.md
```

---

## Revision History

| Timestamp | Change | Trigger |
|-----------|--------|---------|
| 2026-03-01T11:35-08:00 | Initial draft — Stage 1 already complete | Plan conversion |
| 2026-03-01T12:30-08:00 | Added Steps 1.1 (TemplateCustomizer) and 1.2 (compilation fixes). Updated overview with two-phase approach and com.example template default. | Bootstrapping session |
