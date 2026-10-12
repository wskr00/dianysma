package io.github.wskr00.dianysma.knowledge.controller.dto.result.language;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record LanguageList(
		@JsonPropertyDescription(
				"Idiomas do perfil, em ordem alfabética. A lista pode estar vazia.")
		List<LanguageDetails> languages) {
}
