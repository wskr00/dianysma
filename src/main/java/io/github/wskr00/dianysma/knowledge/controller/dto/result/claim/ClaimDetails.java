package io.github.wskr00.dianysma.knowledge.controller.dto.result.claim;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.skill.SkillDetails;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ClaimDetails(
		@JsonPropertyDescription("UUID do fato profissional.")
		UUID id,
		@JsonPropertyDescription("Conteúdo factual do registro profissional.")
		String content,
		@JsonPropertyDescription("UUID da participação associada; null quando o fato é autônomo.")
		UUID participationId,
		@JsonPropertyDescription("Competências canônicas associadas ao fato.")
		List<SkillDetails> skills,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
