package io.github.wskr00.dianysma.knowledge.controller.dto.result.profile;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileDetails(
		@JsonPropertyDescription("UUID do único perfil pessoal desta instalação.")
		UUID id,
		@JsonPropertyDescription("Nome pessoal, quando informado.")
		String name,
		@JsonPropertyDescription("E-mail pessoal, quando informado.")
		String email,
		@JsonPropertyDescription("Telefone como texto, quando informado.")
		String phone,
		@JsonPropertyDescription("Cidade, quando informada.")
		String city,
		@JsonPropertyDescription("Estado ou região, quando informado.")
		String state,
		@JsonPropertyDescription("País, quando informado.")
		String country,
		@JsonPropertyDescription("URL do perfil do LinkedIn, quando informada.")
		String linkedinUrl,
		@JsonPropertyDescription("URL do perfil do GitHub, quando informada.")
		String githubUrl,
		@JsonPropertyDescription("URL do portfólio, quando informada.")
		String portfolioUrl,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
