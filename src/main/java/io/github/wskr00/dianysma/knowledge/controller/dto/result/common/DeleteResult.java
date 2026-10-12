package io.github.wskr00.dianysma.knowledge.controller.dto.result.common;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.UUID;

public record DeleteResult(
		@JsonPropertyDescription("UUID do registro solicitado para exclusão.") UUID id,
		@JsonPropertyDescription("true quando o registro foi excluído; false quando o ID não existia.") boolean deleted) {
}
