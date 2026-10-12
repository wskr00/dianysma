package io.github.wskr00.dianysma.knowledge.controller.dto.result.project;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectDetails(
		@JsonPropertyDescription("UUID do projeto; use-o para criar uma participação.")
		UUID id,
		@JsonPropertyDescription("UUID da organização associada; null quando o projeto não pertence a uma organização.")
		UUID organizationId,
		@JsonPropertyDescription("Nome do projeto.")
		String name,
		@JsonPropertyDescription("Descrição do projeto, quando informada.")
		String description,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
