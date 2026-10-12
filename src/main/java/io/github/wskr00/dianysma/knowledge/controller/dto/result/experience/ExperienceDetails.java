package io.github.wskr00.dianysma.knowledge.controller.dto.result.experience;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExperienceDetails(
		@JsonPropertyDescription("UUID da experiência profissional.")
		UUID id,
		@JsonPropertyDescription("UUID da organização empregadora.")
		UUID employerId,
		@JsonPropertyDescription("UUID da organização cliente; null quando não há cliente associado.")
		UUID clientId,
		@JsonPropertyDescription("Cargo ou função exercida.")
		String role,
		@JsonPropertyDescription("Tipo de vínculo informado como texto livre, por exemplo CLT, PJ, estágio ou freelancer.")
		String employmentType,
		@JsonPropertyDescription("Descrição da experiência, quando informada.")
		String description,
		@JsonPropertyDescription("Data de início no formato AAAA-MM-DD, quando informada.")
		LocalDate startedAt,
		@JsonPropertyDescription("Data de término no formato AAAA-MM-DD, quando informada.")
		LocalDate endedAt,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
