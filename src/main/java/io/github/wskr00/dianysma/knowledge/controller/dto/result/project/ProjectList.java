package io.github.wskr00.dianysma.knowledge.controller.dto.result.project;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record ProjectList(
		@JsonPropertyDescription(
				"Projetos encontrados, ordenados pela atualização mais recente. A lista pode estar vazia.")
		List<ProjectDetails> projects) {
}
