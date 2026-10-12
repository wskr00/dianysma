package io.github.wskr00.dianysma.knowledge.controller.dto.command.claim;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateClaimCommand(
		@NotBlank(message = "O conteúdo do fato profissional não pode ficar em branco.")
		@Size(max = 4_000, message = "O conteúdo do fato profissional deve ter no máximo 4.000 caracteres.") String content,
		UUID participationId,
		@Size(max = 50, message = "Um fato profissional pode ser associado a no máximo 50 competências.")
		List<@NotNull(message = "A lista de IDs de competência não pode conter valores nulos.") UUID> skillIds) {
}
