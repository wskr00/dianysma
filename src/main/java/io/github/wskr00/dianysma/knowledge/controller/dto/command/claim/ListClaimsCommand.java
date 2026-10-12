package io.github.wskr00.dianysma.knowledge.controller.dto.command.claim;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

public record ListClaimsCommand(
		UUID participationId,
		@Min(value = 1, message = "O limite de resultados deve estar entre 1 e 50.")
		@Max(value = 50, message = "O limite de resultados deve estar entre 1 e 50.") Integer limit) {
}
