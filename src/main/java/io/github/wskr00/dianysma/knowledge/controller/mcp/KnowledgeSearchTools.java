package io.github.wskr00.dianysma.knowledge.controller.mcp;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.knowledge.SearchKnowledgeCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.knowledge.KnowledgeSearchResult;
import io.github.wskr00.dianysma.knowledge.service.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KnowledgeSearchTools {

	private final KnowledgeSearchService knowledgeSearchService;

	@McpTool(
			name = "search_knowledge",
			description = "Recupera por significado fatos profissionais, participações e formações indexados. A resposta contém a coleção `results`; cada resultado traz `sourceType`, `sourceId`, um trecho e uma pontuação de similaridade. A pontuação ordena relevância, não é probabilidade nem confirmação factual. Para obter os dados completos, use `get_claim`, `get_participation` ou `get_education` conforme `sourceType` (`claim`, `participation` ou `education`).",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Pesquisar conhecimento profissional",
					readOnlyHint = true,
					destructiveHint = false,
					idempotentHint = true,
					openWorldHint = false))
	public KnowledgeSearchResult searchKnowledge(
			@McpToolParam(description = "Descreva em linguagem natural o conhecimento que procura, por exemplo uma entrega, responsabilidade, participação ou formação. A busca é semântica, não uma correspondência literal; obrigatório, até 2.000 caracteres.") String query,
			@McpToolParam(description = "Quantidade máxima de resultados. Padrão: 5; deve estar entre 1 e 10.", required = false)
				Integer limit) {
		return McpToolErrors.call("search_knowledge", () -> new KnowledgeSearchResult(
				this.knowledgeSearchService.search(new SearchKnowledgeCommand(query, limit))));
	}
}
