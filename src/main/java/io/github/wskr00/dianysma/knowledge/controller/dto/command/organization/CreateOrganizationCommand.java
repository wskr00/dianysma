package io.github.wskr00.dianysma.knowledge.controller.dto.command.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationCommand(
		@NotBlank(message = "O nome da organização não pode ficar em branco.")
		@Size(max = 120, message = "O nome da organização deve ter no máximo 120 caracteres.") String name) {
}
