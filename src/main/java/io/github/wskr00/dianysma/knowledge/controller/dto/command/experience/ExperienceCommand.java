package io.github.wskr00.dianysma.knowledge.controller.dto.command.experience;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record ExperienceCommand(
		@NotNull(message = "O ID da organização empregadora é obrigatório.") UUID employerId,
		UUID clientId,
		@NotBlank(message = "O cargo não pode ficar em branco.")
		@Size(max = 200, message = "O cargo deve ter no máximo 200 caracteres.") String role,
		@NotBlank(message = "O tipo de vínculo não pode ficar em branco.")
		@Size(max = 40, message = "O tipo de vínculo deve ter no máximo 40 caracteres.") String employmentType,
		String description,
		LocalDate startedAt,
		LocalDate endedAt) {

	@AssertTrue(message = "A data final não pode ser anterior à data inicial da experiência.")
	public boolean isDateRangeValid() {
		return startedAt == null || endedAt == null || !endedAt.isBefore(startedAt);
	}
}
