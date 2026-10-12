package io.github.wskr00.dianysma.knowledge.controller.dto.result.knowledge;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.UUID;

public record KnowledgeSearchHit(
		@JsonPropertyDescription("UUID do registro canônico que originou este resultado.") UUID sourceId,
		@JsonPropertyDescription("Tipo do registro: claim, participation ou education. Use esse tipo para escolher a ferramenta get correspondente.") String sourceType,
		@JsonPropertyDescription("Trecho do conteúdo indexado, limitado a 500 caracteres. Consulte o registro canônico para obter os dados completos.") String excerpt,
		@JsonPropertyDescription("Pontuação de similaridade semântica usada para ordenar os resultados; não representa probabilidade nem confirmação factual.") Double similarity) {
}
