package io.github.wskr00.dianysma.knowledge.controller.dto.result.participation;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ParticipationDetails(
		@JsonPropertyDescription("UUID da participação; use-o para associar fatos profissionais.")
		UUID id,
		@JsonPropertyDescription("UUID da experiência associada; null quando não há experiência.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		UUID experienceId,
		@JsonPropertyDescription("UUID do projeto associado; null quando não há projeto.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		UUID projectId,
		@JsonPropertyDescription("Descrição da participação, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String description,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
