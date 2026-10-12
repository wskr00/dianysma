package io.github.wskr00.dianysma.knowledge.controller.dto.command.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ListKnowledgeRecordsCommand(
		@Min(value = 1, message = "O limite de resultados deve estar entre 1 e 50.")
		@Max(value = 50, message = "O limite de resultados deve estar entre 1 e 50.") Integer limit) {
}
