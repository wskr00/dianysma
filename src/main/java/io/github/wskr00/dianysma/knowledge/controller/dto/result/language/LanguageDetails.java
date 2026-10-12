package io.github.wskr00.dianysma.knowledge.controller.dto.result.language;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.wskr00.dianysma.knowledge.domain.enums.LanguageProficiency;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LanguageDetails(
		@JsonPropertyDescription("UUID do idioma no perfil pessoal.")
		UUID id,
		@JsonPropertyDescription("Nome do idioma.")
		String name,
		@JsonPropertyDescription("Proficiência: BASIC (básica), INTERMEDIATE (intermediária), ADVANCED (avançada), FLUENT (fluente) ou NATIVE (nativa).")
		LanguageProficiency proficiency,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
