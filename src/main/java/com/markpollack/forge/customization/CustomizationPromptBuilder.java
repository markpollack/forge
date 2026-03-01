package com.markpollack.forge.customization;

import java.util.stream.Collectors;

import com.markpollack.forge.brief.ExperimentBrief;

/**
 * Builds the AgentClient prompt that instructs the agent to customize the
 * template project based on the experiment brief.
 *
 * <p>The agent performs all customization:</p>
 * <ul>
 *   <li>Refactors package name from org.example.experiment to the brief's package</li>
 *   <li>Updates pom.xml GAV coordinates</li>
 *   <li>Renames TemplateAgentInvoker to {Domain}AgentInvoker</li>
 *   <li>Generates judge stubs per the brief's judge table</li>
 *   <li>Generates dataset/items.yaml from the brief's benchmark section</li>
 *   <li>Generates knowledge/index.md from the brief's KB design</li>
 *   <li>Fills in VISION.md from brief content</li>
 * </ul>
 */
public class CustomizationPromptBuilder {

	/**
	 * Build the customization prompt from the experiment brief.
	 */
	public String build(ExperimentBrief brief) {
		String domain = brief.domainName();

		StringBuilder sb = new StringBuilder();
		sb.append("You are customizing an agent experiment project from a template.\n\n");

		sb.append("## Project Identity\n\n");
		sb.append("- Experiment name: ").append(brief.name()).append("\n");
		sb.append("- Package: ").append(brief.packageName()).append("\n");
		sb.append("- GroupId: ").append(brief.groupId()).append("\n");
		sb.append("- ArtifactId: ").append(brief.artifactId()).append("\n");
		sb.append("- Domain name: ").append(domain).append("\n\n");

		sb.append("## Tasks\n\n");

		sb.append("### 1. Refactor Package\n");
		sb.append("Move all Java files from `org.example.experiment` to `").append(brief.packageName()).append("`.\n");
		sb.append("Update all import statements and package declarations.\n\n");

		sb.append("### 2. Update pom.xml\n");
		sb.append("- Set groupId to `").append(brief.groupId()).append("`\n");
		sb.append("- Set artifactId to `").append(brief.artifactId()).append("`\n");
		sb.append("- Update mainClass to `").append(brief.packageName()).append(".ExperimentApp`\n\n");

		sb.append("### 3. Rename AgentInvoker\n");
		sb.append("Rename `TemplateAgentInvoker.java` to `").append(domain).append("AgentInvoker.java`.\n");
		sb.append("Update the class name and all references.\n");
		sb.append("Add a TODO comment for domain-specific implementation:\n");
		sb.append("Agent description: ").append(brief.agent().description()).append("\n");
		sb.append("Agent goal: ").append(brief.agent().goal()).append("\n\n");

		sb.append("### 4. Generate Judge Stubs\n");
		for (ExperimentBrief.JudgeConfig judge : brief.judges()) {
			if ("custom".equals(judge.source())) {
				sb.append("Create stub class `").append(judge.name()).append(".java` in the judges subpackage.\n");
				sb.append("  - Tier: ").append(judge.tier()).append("\n");
				sb.append("  - Policy: ").append(judge.policy()).append("\n");
				sb.append("  - Extend DeterministicJudge from agent-judge-core\n");
				sb.append("  - Add TODO for implementation\n\n");
			}
		}

		sb.append("### 5. Generate Dataset\n");
		sb.append("Replace `dataset/items.yaml` with:\n```yaml\nitems:\n");
		for (ExperimentBrief.DatasetItemConfig item : brief.benchmark().dataset()) {
			sb.append("  - id: \"").append(item.name()).append("\"\n");
			sb.append("    slug: \"").append(item.name()).append("\"\n");
			sb.append("    task: \"").append(brief.benchmark().task()).append("\"\n");
			sb.append("    source: \"").append(item.url()).append("\"\n");
			sb.append("    subdirectory: \"").append(item.subdirectory()).append("\"\n");
			sb.append("    knowledgeRefs: []\n");
		}
		sb.append("```\n\n");

		sb.append("### 6. Generate Knowledge Index\n");
		sb.append("Update `knowledge/index.md` routing table with these files:\n");
		for (String file : brief.knowledge().files()) {
			sb.append("- `").append(file).append("` — (add description)\n");
		}
		sb.append("\nCreate empty placeholder files for each knowledge file in the knowledge/ directory.\n\n");

		sb.append("### 7. Generate Prompt Placeholders\n");
		String promptFiles = brief.variants().stream()
				.map(ExperimentBrief.VariantConfig::prompt)
				.distinct()
				.collect(Collectors.joining(", "));
		sb.append("Create placeholder prompt files in prompts/: ").append(promptFiles).append("\n\n");

		sb.append("### 8. Generate Variant Config\n");
		sb.append("Create `experiment-config.yaml` in the project root with:\n");
		sb.append("```yaml\n");
		sb.append("experimentName: ").append(brief.name()).append("\n");
		sb.append("defaultModel: claude-sonnet-4-20250514\n");
		sb.append("timeoutMinutes: 15\n");
		sb.append("variants:\n");
		for (ExperimentBrief.VariantConfig variant : brief.variants()) {
			sb.append("  - name: ").append(variant.name()).append("\n");
			sb.append("    promptFile: ").append(variant.prompt()).append("\n");
			if (!variant.knowledge().isEmpty()) {
				sb.append("    knowledgeDir: knowledge\n");
				sb.append("    knowledgeFiles:\n");
				for (String kf : variant.knowledge()) {
					sb.append("      - ").append(kf).append("\n");
				}
			}
			else {
				sb.append("    knowledgeDir: null\n");
				sb.append("    knowledgeFiles: []\n");
			}
		}
		sb.append("```\n\n");

		sb.append("### 9. Fill VISION.md\n");
		sb.append("Rename `plans/VISION-TEMPLATE.md` to `plans/VISION.md` and fill in:\n");
		sb.append("- Problem: ").append(brief.agent().description()).append("\n");
		sb.append("- Goal: ").append(brief.agent().goal()).append("\n");
		sb.append("- Dataset: ").append(brief.benchmark().dataset().size()).append(" items\n");
		sb.append("- Variants: ").append(brief.variants().size()).append("\n\n");

		sb.append("### 10. Rename Other Templates\n");
		sb.append("Rename `plans/DESIGN-TEMPLATE.md` to `plans/DESIGN.md`\n");
		sb.append("Rename `plans/ROADMAP-TEMPLATE.md` to `plans/ROADMAP.md`\n\n");

		sb.append("## Important\n");
		sb.append("- Do NOT delete any pre-wired classes (ExperimentApp, JuryFactory, etc.)\n");
		sb.append("- Only add domain-specific code, don't modify the experiment loop\n");
		sb.append("- All judge stubs should compile but have TODO implementations\n");

		return sb.toString();
	}

}
