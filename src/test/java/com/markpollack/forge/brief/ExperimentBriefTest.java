package com.markpollack.forge.brief;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ExperimentBriefTest {

	@Test
	void shouldParseBriefYaml(@TempDir Path tempDir) throws IOException {
		Path briefPath = tempDir.resolve("test-brief.yaml");
		Files.writeString(briefPath, """
				name: test-experiment
				package: org.test.experiment
				groupId: org.test
				artifactId: test-experiment

				agent:
				  description: "Test agent"
				  goal: "Test goal"

				benchmark:
				  task: "Do the thing"
				  dataset:
				    - name: project-a
				      url: https://github.com/example/project-a
				      subdirectory: src

				judges:
				  - name: TestJudge
				    tier: 0
				    source: custom
				    policy: FINAL_TIER

				variants:
				  - name: control
				    prompt: v0.txt
				    knowledge: []
				  - name: variant-a
				    prompt: v1.txt
				    knowledge:
				      - docs.md

				knowledge:
				  files:
				    - docs.md
				    - patterns.md
				""");

		ExperimentBrief brief = ExperimentBrief.parse(briefPath);

		assertThat(brief.name()).isEqualTo("test-experiment");
		assertThat(brief.packageName()).isEqualTo("org.test.experiment");
		assertThat(brief.groupId()).isEqualTo("org.test");
		assertThat(brief.artifactId()).isEqualTo("test-experiment");
		assertThat(brief.agent().description()).isEqualTo("Test agent");
		assertThat(brief.benchmark().dataset()).hasSize(1);
		assertThat(brief.judges()).hasSize(1);
		assertThat(brief.judges().getFirst().name()).isEqualTo("TestJudge");
		assertThat(brief.variants()).hasSize(2);
		assertThat(brief.knowledge().files()).containsExactly("docs.md", "patterns.md");
		assertThat(brief.domainName()).isEqualTo("Test");
	}

}
