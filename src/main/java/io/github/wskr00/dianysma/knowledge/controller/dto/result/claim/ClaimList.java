package io.github.wskr00.dianysma.knowledge.controller.dto.result.claim;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record ClaimList(
		@JsonPropertyDescription(
				"Fatos profissionais encontrados, ordenados pela atualização mais recente. A lista pode estar vazia.")
		List<ClaimDetails> claims) {
}
