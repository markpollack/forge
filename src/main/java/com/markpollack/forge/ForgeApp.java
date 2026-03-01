package com.markpollack.forge;

import java.nio.file.Path;

import com.markpollack.forge.brief.ExperimentBrief;
import com.markpollack.forge.customization.CustomizationPromptBuilder;
import com.markpollack.forge.customization.TemplateCustomizer;
import com.markpollack.forge.template.TemplateCloner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for Forge — creates agent experiment projects from a brief.
 *
 * <p>Uses a two-phase approach:</p>
 * <ol>
 *   <li>Deterministic customization — package rename, pom update, file generation</li>
 *   <li>LLM customization (optional) — judge stubs, VISION.md content</li>
 * </ol>
 */
public class ForgeApp {

	private static final Logger logger = LoggerFactory.getLogger(ForgeApp.class);

	private static final String TEMPLATE_REPO_URL = "https://github.com/markpollack/agent-experiment-template.git";

	private final TemplateCloner templateCloner;

	private final TemplateCustomizer templateCustomizer;

	public ForgeApp() {
		this.templateCloner = new TemplateCloner();
		this.templateCustomizer = new TemplateCustomizer();
	}

	/**
	 * Create a new experiment project from a brief.
	 * @param briefPath path to the experiment brief YAML
	 * @param outputDir directory to create the project in
	 */
	public void create(Path briefPath, Path outputDir) {
		logger.info("Forge: Creating new experiment project");
		logger.info("  Brief: {}", briefPath);
		logger.info("  Output: {}", outputDir);

		// 1. Parse brief
		ExperimentBrief brief = ExperimentBrief.parse(briefPath);
		logger.info("  Experiment: {}", brief.name());
		logger.info("  Package: {}", brief.packageName());
		logger.info("  Variants: {}", brief.variants().size());

		// 2. Clone template
		logger.info("Cloning template from {}", TEMPLATE_REPO_URL);
		templateCloner.cloneTemplate(TEMPLATE_REPO_URL, outputDir);

		// 3. Deterministic customization (no LLM)
		templateCustomizer.customize(brief, outputDir);

		logger.info("Project created at {}", outputDir);
		logger.info("Next steps:");
		logger.info("  1. Implement your AgentInvoker: {}AgentInvoker.java", brief.domainName());
		logger.info("  2. Write knowledge files in knowledge/");
		logger.info("  3. Write prompts in prompts/");
		logger.info("  4. Populate dataset workspaces");
	}

	public static void main(String[] args) {
		if (args.length < 4) {
			printUsage();
			System.exit(1);
		}

		String command = args[0];
		if (!"new".equals(command)) {
			logger.error("Unknown command: {}. Only 'new' is currently supported.", command);
			printUsage();
			System.exit(1);
		}

		Path briefPath = null;
		Path outputDir = null;

		for (int i = 1; i < args.length; i++) {
			switch (args[i]) {
				case "--brief" -> {
					if (i + 1 < args.length) briefPath = Path.of(args[++i]);
				}
				case "--output" -> {
					if (i + 1 < args.length) outputDir = Path.of(args[++i]);
				}
			}
		}

		if (briefPath == null || outputDir == null) {
			logger.error("Both --brief and --output are required");
			printUsage();
			System.exit(1);
		}

		ForgeApp forge = new ForgeApp();
		forge.create(briefPath, outputDir);
	}

	private static void printUsage() {
		System.out.println("Usage: forge new --brief <path-to-brief.yaml> --output <output-directory>");
		System.out.println();
		System.out.println("Commands:");
		System.out.println("  new    Create a new experiment project from a brief");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  --brief   Path to experiment brief YAML file");
		System.out.println("  --output  Directory to create the project in");
	}

}
