package io.github.wskr00.dianysma.knowledge.controller.dto.command.organization;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchOrganizationsCommand(
		@NotBlank(message = "A consulta por organizações não pode ficar em branco.")
		@Size(max = 120, message = "A consulta por organizações deve ter no máximo 120 caracteres.") String query,
		@Min(value = 1, message = "O limite de resultados deve estar entre 1 e 50.")
		@Max(value = 50, message = "O limite de resultados deve estar entre 1 e 50.") Integer limit) {
}
