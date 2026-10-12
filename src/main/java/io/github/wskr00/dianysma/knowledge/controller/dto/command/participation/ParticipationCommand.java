package io.github.wskr00.dianysma.knowledge.controller.dto.command.participation;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ParticipationCommand(
		UUID experienceId,
		UUID projectId,
		@Size(max = 4_000, message = "A descrição da participação deve ter no máximo 4.000 caracteres.") String description) {
}
