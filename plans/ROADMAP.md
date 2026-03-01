# Roadmap: Forge

> **Created**: 2026-03-01T11:35-08:00
> **Last updated**: 2026-03-01T11:35-08:00
> **Status**: Stage 1 complete (initial implementation committed)

## Overview

Forge is Spring Initializr for agent experiments — `forge new` creates a runnable experiment project from a YAML brief. Stage 1 (initial implementation) is complete: ForgeApp, ExperimentBrief, TemplateCloner, and CustomizationPromptBuilder are committed and pushed. Stage 2 adds `forge grow` (run variants), Stage 3 adds `forge evaluate` (compare runs), and Stage 4 adds `forge graduate` (extract agent).

> **Before every commit**: Verify ALL exit criteria for the current step are met. Do NOT remove exit criteria to mark a step complete — fulfill them.

---

## Stage 1: Initial Implementation (Complete)

### Step 1.0: Project Scaffolding

**Status**: Complete (committed: `457281c`)

**Deliverables**: ForgeApp CLI, ExperimentBrief YAML parser, TemplateCloner, CustomizationPromptBuilder, ExperimentBrief unit test.

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
