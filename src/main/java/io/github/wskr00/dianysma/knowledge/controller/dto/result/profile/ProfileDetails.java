package io.github.wskr00.dianysma.knowledge.controller.dto.result.profile;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileDetails(
		@JsonPropertyDescription("UUID do único perfil pessoal desta instalação.")
		UUID id,
		@JsonPropertyDescription("Nome pessoal, quando informado.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String name,
		@JsonPropertyDescription("E-mail pessoal, quando informado.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String email,
		@JsonPropertyDescription("Telefone como texto, quando informado.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String phone,
		@JsonPropertyDescription("Cidade, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String city,
		@JsonPropertyDescription("Estado ou região, quando informado.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String state,
		@JsonPropertyDescription("País, quando informado.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String country,
		@JsonPropertyDescription("URL do perfil do LinkedIn, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String linkedinUrl,
		@JsonPropertyDescription("URL do perfil do GitHub, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String githubUrl,
		@JsonPropertyDescription("URL do portfólio, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String portfolioUrl,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
