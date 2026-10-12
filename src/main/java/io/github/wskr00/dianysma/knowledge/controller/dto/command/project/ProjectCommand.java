package io.github.wskr00.dianysma.knowledge.controller.dto.command.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProjectCommand(
		UUID organizationId,
		@NotBlank(message = "O nome do projeto não pode ficar em branco.")
		@Size(max = 200, message = "O nome do projeto deve ter no máximo 200 caracteres.") String name,
		String description) {
}
