package io.github.wskr00.dianysma.knowledge.controller.dto.result.education;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record EducationList(
		@JsonPropertyDescription(
				"Formações encontradas, ordenadas pela atualização mais recente. A lista pode estar vazia.")
		List<EducationDetails> educations) {
}
