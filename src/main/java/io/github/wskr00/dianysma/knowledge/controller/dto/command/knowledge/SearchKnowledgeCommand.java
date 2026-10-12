package io.github.wskr00.dianysma.knowledge.controller.dto.command.knowledge;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchKnowledgeCommand(
		@NotBlank(message = "A consulta não pode ficar em branco.")
		@Size(max = 2_000, message = "A consulta deve ter no máximo 2.000 caracteres.") String query,
		@Min(value = 1, message = "O limite de resultados deve estar entre 1 e 10.")
		@Max(value = 10, message = "O limite de resultados deve estar entre 1 e 10.") Integer limit) {
}
