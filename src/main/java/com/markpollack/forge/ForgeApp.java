package com.markpollack.forge;

import java.nio.file.Path;

import com.markpollack.forge.brief.ExperimentBrief;
import com.markpollack.forge.customization.CustomizationPromptBuilder;
import com.markpollack.forge.template.TemplateCloner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agents.client.AgentClient;
import org.springaicommunity.agents.claude.ClaudeAgentModel;
import org.springaicommunity.agents.claude.ClaudeAgentOptions;
import org.springaicommunity.agents.model.AgentModel;

/**
 * Main entry point for Forge — Spring Initializr for agent experiments.
 *
 * <p>Commands:</p>
 * <ul>
 *   <li>{@code forge new --brief path/to/brief.yaml --output ~/projects/my-experiment/}
 *       — Creates a new experiment project from a brief</li>
 * </ul>
 *
 * <p>Future commands: {@code forge grow}, {@code forge evaluate}, {@code forge graduate}</p>
 */
public class ForgeApp {

	private static final Logger logger = LoggerFactory.getLogger(ForgeApp.class);

	private static final String TEMPLATE_REPO_URL = "https://github.com/markpollack/agent-experiment-template.git";

	private final TemplateCloner templateCloner;

	private final CustomizationPromptBuilder promptBuilder;

	public ForgeApp() {
		this.templateCloner = new TemplateCloner();
		this.promptBuilder = new CustomizationPromptBuilder();
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

		// 3. Agent does ALL customization
		String prompt = promptBuilder.build(brief);
		logger.info("Customization prompt built ({} chars)", prompt.length());

		AgentModel agentModel = createAgentModel(outputDir);
		AgentClient agent = AgentClient.create(agentModel);

		logger.info("Invoking agent for project customization...");
		agent.goal(prompt).workingDirectory(outputDir).run();

		logger.info("Project created at {}", outputDir);
		logger.info("Next steps:");
		logger.info("  1. Implement your AgentInvoker");
		logger.info("  2. Write knowledge files");
		logger.info("  3. Populate dataset workspaces");
		logger.info("  4. Run: java -jar {}.jar --run-all-variants", brief.artifactId());
	}

	private AgentModel createAgentModel(Path workingDirectory) {
		ClaudeAgentOptions options = ClaudeAgentOptions.builder()
				.model("claude-sonnet-4-20250514")
				.yolo(true)
				.build();

		return ClaudeAgentModel.builder()
				.workingDirectory(workingDirectory)
				.defaultOptions(options)
				.build();
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
