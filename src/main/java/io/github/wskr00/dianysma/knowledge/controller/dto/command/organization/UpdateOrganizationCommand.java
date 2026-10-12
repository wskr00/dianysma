package io.github.wskr00.dianysma.knowledge.controller.dto.command.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateOrganizationCommand(
		@NotNull(message = "O ID da organização é obrigatório.") UUID organizationId,
		@NotBlank(message = "O nome da organização não pode ficar em branco.")
		@Size(max = 120, message = "O nome da organização deve ter no máximo 120 caracteres.") String name) {
}
