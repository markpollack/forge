package com.markpollack.forge.customization;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.markpollack.forge.brief.ExperimentBrief;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs deterministic template customization — no LLM needed.
 *
 * <p>Handles: package rename (file moves + declaration/import rewrites),
 * pom.xml GAV updates, file generation (dataset, config, prompts, knowledge).</p>
 */
public class TemplateCustomizer {

	private static final Logger logger = LoggerFactory.getLogger(TemplateCustomizer.class);

	private static final String TEMPLATE_PACKAGE = "com.example.experiment";

	private static final String TEMPLATE_GROUP_ID = "com.example";

	private static final String TEMPLATE_ARTIFACT_ID = "agent-experiment-template";

	private static final String TEMPLATE_INVOKER = "TemplateAgentInvoker";

	/**
	 * Apply all deterministic customizations to the cloned template.
	 */
	public void customize(ExperimentBrief brief, Path projectDir) {
		logger.info("Applying deterministic customizations...");

		refactorPackage(brief, projectDir);
		updatePom(brief, projectDir);
		renameAgentInvoker(brief, projectDir);
		generateDatasetItems(brief, projectDir);
		generateExperimentConfig(brief, projectDir);
		generatePromptPlaceholders(brief, projectDir);
		generateKnowledgeFiles(brief, projectDir);
		renameTemplateFiles(projectDir);

		logger.info("Deterministic customization complete.");
	}

	private void refactorPackage(ExperimentBrief brief, Path projectDir) {
		String targetPackage = brief.packageName();
		logger.info("Refactoring package: {} → {}", TEMPLATE_PACKAGE, targetPackage);

		Path srcMain = projectDir.resolve("src/main/java");
		Path srcTest = projectDir.resolve("src/test/java");

		for (Path srcRoot : List.of(srcMain, srcTest)) {
			if (!Files.exists(srcRoot)) {
				continue;
			}
			Path oldPackageDir = srcRoot.resolve(TEMPLATE_PACKAGE.replace('.', '/'));
			Path newPackageDir = srcRoot.resolve(targetPackage.replace('.', '/'));

			if (Files.exists(oldPackageDir)) {
				movePackageDirectory(oldPackageDir, newPackageDir, srcRoot);
			}
		}

		// Rewrite package declarations and imports in all Java files
		rewriteJavaFiles(projectDir, TEMPLATE_PACKAGE, targetPackage);
	}

	private void movePackageDirectory(Path oldDir, Path newDir, Path srcRoot) {
		try {
			Files.createDirectories(newDir.getParent());
			// Move each file individually (handles nested packages)
			Files.walkFileTree(oldDir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Path relative = oldDir.relativize(file);
					Path target = newDir.resolve(relative);
					Files.createDirectories(target.getParent());
					Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
					logger.debug("Moved: {} → {}", file, target);
					return FileVisitResult.CONTINUE;
				}
			});
			// Clean up empty directories (including parent chain up to srcRoot)
			deleteEmptyDirectories(oldDir);
			cleanEmptyParents(oldDir.getParent(), srcRoot);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to move package directory", ex);
		}
	}

	private void deleteEmptyDirectories(Path dir) throws IOException {
		// Walk bottom-up and delete empty dirs
		Files.walkFileTree(dir, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult postVisitDirectory(Path d, IOException exc) throws IOException {
				try (var entries = Files.list(d)) {
					if (entries.findAny().isEmpty()) {
						Files.delete(d);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		// Try to delete the root too if empty
		if (Files.exists(dir)) {
			try (var entries = Files.list(dir)) {
				if (entries.findAny().isEmpty()) {
					Files.delete(dir);
				}
			}
		}
	}

	private void cleanEmptyParents(Path dir, Path stopAt) throws IOException {
		while (dir != null && !dir.equals(stopAt) && Files.exists(dir)) {
			try (var entries = Files.list(dir)) {
				if (entries.findAny().isEmpty()) {
					Files.delete(dir);
					dir = dir.getParent();
				}
				else {
					break;
				}
			}
		}
	}

	private void rewriteJavaFiles(Path projectDir, String oldPackage, String newPackage) {
		try {
			Files.walkFileTree(projectDir, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(".java")) {
						rewriteFile(file, oldPackage, newPackage);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to rewrite Java files", ex);
		}
	}

	private void rewriteFile(Path file, String oldPackage, String newPackage) throws IOException {
		String content = Files.readString(file, StandardCharsets.UTF_8);
		String updated = content
				.replace("package " + oldPackage, "package " + newPackage)
				.replace("import " + oldPackage, "import " + newPackage);
		if (!content.equals(updated)) {
			Files.writeString(file, updated, StandardCharsets.UTF_8);
			logger.debug("Rewrote: {}", file);
		}
	}

	private void updatePom(ExperimentBrief brief, Path projectDir) {
		Path pomFile = projectDir.resolve("pom.xml");
		logger.info("Updating pom.xml GAV: {}:{}", brief.groupId(), brief.artifactId());

		try {
			String content = Files.readString(pomFile, StandardCharsets.UTF_8);
			content = content
					.replace("<groupId>" + TEMPLATE_GROUP_ID + "</groupId>",
							"<groupId>" + brief.groupId() + "</groupId>")
					.replace("<artifactId>" + TEMPLATE_ARTIFACT_ID + "</artifactId>",
							"<artifactId>" + brief.artifactId() + "</artifactId>")
					.replace("<name>Agent Experiment Template</name>",
							"<name>" + brief.name() + "</name>")
					.replace(TEMPLATE_PACKAGE + ".ExperimentApp",
							brief.packageName() + ".ExperimentApp");
			Files.writeString(pomFile, content, StandardCharsets.UTF_8);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to update pom.xml", ex);
		}
	}

	private void renameAgentInvoker(ExperimentBrief brief, Path projectDir) {
		String domain = brief.domainName();
		String newName = domain + "AgentInvoker";
		logger.info("Renaming AgentInvoker: {} → {}", TEMPLATE_INVOKER, newName);

		Path srcMain = projectDir.resolve("src/main/java");
		Path newPackageDir = srcMain.resolve(brief.packageName().replace('.', '/'));

		Path oldFile = newPackageDir.resolve(TEMPLATE_INVOKER + ".java");
		Path newFile = newPackageDir.resolve(newName + ".java");

		if (!Files.exists(oldFile)) {
			logger.warn("TemplateAgentInvoker not found at {}, skipping rename", oldFile);
			return;
		}

		try {
			String content = Files.readString(oldFile, StandardCharsets.UTF_8);
			content = content.replace(TEMPLATE_INVOKER, newName);
			// Add TODO with agent description
			content = content.replace("public class " + newName,
					"/**\n * " + brief.agent().description() + "\n * Goal: " + brief.agent().goal()
							+ "\n * TODO: Implement domain-specific invocation logic\n */\npublic class " + newName);
			Files.writeString(newFile, content, StandardCharsets.UTF_8);
			Files.delete(oldFile);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to rename AgentInvoker", ex);
		}
	}

	private void generateDatasetItems(ExperimentBrief brief, Path projectDir) {
		Path itemsFile = projectDir.resolve("dataset/items.yaml");
		logger.info("Generating dataset/items.yaml with {} items", brief.benchmark().dataset().size());

		StringBuilder sb = new StringBuilder();
		sb.append("items:\n");
		for (ExperimentBrief.DatasetItemConfig item : brief.benchmark().dataset()) {
			sb.append("  - id: \"").append(item.name()).append("\"\n");
			sb.append("    slug: \"").append(item.name()).append("\"\n");
			sb.append("    task: \"").append(brief.benchmark().task()).append("\"\n");
			sb.append("    source: \"").append(item.url()).append("\"\n");
			sb.append("    subdirectory: \"").append(item.subdirectory()).append("\"\n");
			sb.append("    knowledgeRefs: []\n");
		}

		try {
			Files.writeString(itemsFile, sb.toString(), StandardCharsets.UTF_8);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to generate dataset items", ex);
		}
	}

	private void generateExperimentConfig(ExperimentBrief brief, Path projectDir) {
		Path configFile = projectDir.resolve("experiment-config.yaml");
		logger.info("Generating experiment-config.yaml");

		StringBuilder sb = new StringBuilder();
		sb.append("experimentName: ").append(brief.name()).append("\n");
		sb.append("defaultModel: claude-haiku-4-5-20251001\n");
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

		try {
			Files.writeString(configFile, sb.toString(), StandardCharsets.UTF_8);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to generate experiment config", ex);
		}
	}

	private void generatePromptPlaceholders(ExperimentBrief brief, Path projectDir) {
		Path promptsDir = projectDir.resolve("prompts");
		List<String> promptFiles = brief.variants().stream()
				.map(ExperimentBrief.VariantConfig::prompt)
				.distinct()
				.collect(Collectors.toList());

		logger.info("Generating prompt placeholders: {}", promptFiles);

		try {
			Files.createDirectories(promptsDir);
			for (String promptFile : promptFiles) {
				Path file = promptsDir.resolve(promptFile);
				if (!Files.exists(file)) {
					Files.writeString(file,
							"# TODO: Write prompt for " + promptFile + "\n",
							StandardCharsets.UTF_8);
				}
			}
			// Remove the template default.txt if it's not in the brief
			Path defaultPrompt = promptsDir.resolve("default.txt");
			if (Files.exists(defaultPrompt) && !promptFiles.contains("default.txt")) {
				Files.delete(defaultPrompt);
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to generate prompt placeholders", ex);
		}
	}

	private void generateKnowledgeFiles(ExperimentBrief brief, Path projectDir) {
		Path knowledgeDir = projectDir.resolve("knowledge");
		logger.info("Generating knowledge files: {}", brief.knowledge().files());

		try {
			Files.createDirectories(knowledgeDir);

			// Generate index.md
			StringBuilder index = new StringBuilder();
			index.append("# Knowledge Base\n\n");
			index.append("| File | Read when... |\n");
			index.append("|------|-------------|\n");
			for (String file : brief.knowledge().files()) {
				index.append("| `").append(file).append("` | TODO: describe when to read |\n");
			}
			Files.writeString(knowledgeDir.resolve("index.md"), index.toString(), StandardCharsets.UTF_8);

			// Generate placeholder files
			for (String file : brief.knowledge().files()) {
				Path kbFile = knowledgeDir.resolve(file);
				if (!Files.exists(kbFile)) {
					Files.writeString(kbFile,
							"# " + file.replace(".md", "").replace("-", " ") + "\n\nTODO: Write content\n",
							StandardCharsets.UTF_8);
				}
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to generate knowledge files", ex);
		}
	}

	private void renameTemplateFiles(Path projectDir) {
		Path plansDir = projectDir.resolve("plans");

		renameIfExists(plansDir.resolve("VISION-TEMPLATE.md"), plansDir.resolve("VISION.md"));
		renameIfExists(plansDir.resolve("DESIGN-TEMPLATE.md"), plansDir.resolve("DESIGN.md"));
		renameIfExists(plansDir.resolve("ROADMAP-TEMPLATE.md"), plansDir.resolve("ROADMAP.md"));
	}

	private void renameIfExists(Path from, Path to) {
		try {
			if (Files.exists(from)) {
				Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
				logger.debug("Renamed: {} → {}", from, to);
			}
		}
		catch (IOException ex) {
			logger.warn("Failed to rename {} → {}: {}", from, to, ex.getMessage());
		}
	}

}
