package io.github.wskr00.dianysma.knowledge.controller.dto.command.skill;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchSkillsCommand(
		@NotBlank(message = "A consulta por competências não pode ficar em branco.")
		@Size(max = 200, message = "A consulta por competências deve ter no máximo 200 caracteres.") String query,
		@Min(value = 1, message = "O limite de resultados deve estar entre 1 e 25.")
		@Max(value = 25, message = "O limite de resultados deve estar entre 1 e 25.") Integer limit) {
}
