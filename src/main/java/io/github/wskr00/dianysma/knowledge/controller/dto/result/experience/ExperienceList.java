package io.github.wskr00.dianysma.knowledge.controller.dto.result.experience;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record ExperienceList(
		@JsonPropertyDescription(
				"Experiências profissionais encontradas, ordenadas pela atualização mais recente. A lista pode estar vazia.")
		List<ExperienceDetails> experiences) {
}
