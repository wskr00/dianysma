package io.github.wskr00.dianysma.knowledge.controller.dto.command.education;

import io.github.wskr00.dianysma.knowledge.domain.enums.EducationType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EducationCommand(
		@NotBlank(message = "A instituição de ensino não pode ficar em branco.")
		@Size(max = 100, message = "A instituição de ensino deve ter no máximo 100 caracteres.") String institution,
		@NotNull(message = "O tipo de formação é obrigatório.") EducationType type,
		@NotBlank(message = "O título da formação não pode ficar em branco.")
		@Size(max = 120, message = "O título da formação deve ter no máximo 120 caracteres.") String title,
		@Size(max = 100, message = "O grau ou título acadêmico deve ter no máximo 100 caracteres.") String degree,
		@Size(max = 4_000, message = "A descrição deve ter no máximo 4.000 caracteres.") String description,
		@PositiveOrZero(message = "A carga horária não pode ser negativa.") Integer workloadHours,
		LocalDate startedAt,
		LocalDate completedAt,
		Boolean expected) {

	@AssertTrue(message = "A data de conclusão não pode ser anterior à data inicial da formação.")
	public boolean isDateRangeValid() {
		return startedAt == null || completedAt == null || !completedAt.isBefore(startedAt);
	}
}
