package io.github.wskr00.dianysma.knowledge.controller.dto.result.participation;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ParticipationDetails(
		@JsonPropertyDescription("UUID da participação; use-o para associar fatos profissionais.")
		UUID id,
		@JsonPropertyDescription("UUID da experiência associada; null quando não há experiência.")
		UUID experienceId,
		@JsonPropertyDescription("UUID do projeto associado; null quando não há projeto.")
		UUID projectId,
		@JsonPropertyDescription("Descrição da participação, quando informada.")
		String description,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
