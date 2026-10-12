package io.github.wskr00.dianysma.knowledge.controller.dto.result.education;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.github.wskr00.dianysma.knowledge.domain.enums.EducationType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EducationDetails(
		@JsonPropertyDescription("UUID da formação.")
		UUID id,
		@JsonPropertyDescription("Nome da instituição de ensino ou certificadora.")
		String institution,
		@JsonPropertyDescription("Tipo da formação: CERTIFICATION, COURSE ou DEGREE.")
		EducationType type,
		@JsonPropertyDescription("Nome do curso, certificação ou formação.")
		String title,
		@JsonPropertyDescription("Grau ou título acadêmico, quando informado.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String degree,
		@JsonPropertyDescription("Descrição da formação, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		String description,
		@JsonPropertyDescription("Carga horária total em horas, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		Integer workloadHours,
		@JsonPropertyDescription("Data de início no formato AAAA-MM-DD, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		LocalDate startedAt,
		@JsonPropertyDescription("Data efetiva ou prevista de conclusão no formato AAAA-MM-DD, quando informada.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		LocalDate completedAt,
		@JsonPropertyDescription("true se completedAt for uma previsão; false se for a data efetiva; null se não houver essa informação.")
		@Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Nullable
		Boolean expected,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.")
		OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.")
		OffsetDateTime updatedAt) {
}
