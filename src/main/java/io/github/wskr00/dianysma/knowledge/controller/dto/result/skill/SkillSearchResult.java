package io.github.wskr00.dianysma.knowledge.controller.dto.result.skill;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record SkillSearchResult(
		@JsonPropertyDescription(
				"Competências que correspondem à busca, ordenadas por nome. A lista pode estar vazia.")
		List<SkillDetails> skills) {
}
