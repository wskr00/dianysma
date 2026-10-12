package io.github.wskr00.dianysma.knowledge.controller.dto.result.knowledge;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record KnowledgeSearchResult(
		@JsonPropertyDescription(
				"Resultados mais relevantes da busca semântica. A lista pode estar vazia.")
		List<KnowledgeSearchHit> results) {
}
