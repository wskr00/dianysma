package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.skill.CreateSkillCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.skill.SearchSkillsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.skill.SkillDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.skill.SkillSearchResult;
import io.github.wskr00.dianysma.knowledge.service.SkillService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SkillTools {

	private final SkillService skillService;

	@McpTool(
			name = "search_skills",
		description = "Pesquisa por trecho do nome canônico ou de um nome alternativo, sem diferenciar maiúsculas de minúsculas. A resposta contém a coleção `skills`. Use o UUID retornado ao criar fatos profissionais; reutilize a competência existente para manter o catálogo canônico.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Pesquisar competências",
					readOnlyHint = true,
					destructiveHint = false,
					idempotentHint = true,
					openWorldHint = false))
	public SkillSearchResult searchSkills(
			@McpToolParam(description = "Nome completo ou trecho do nome ou de um nome alternativo. Obrigatório e limitado a 200 caracteres.") String query,
			@McpToolParam(description = "Quantidade máxima de competências retornadas. Padrão: 10; deve estar entre 1 e 25.", required = false)
				Integer limit) {
		return McpToolErrors.call("search_skills", () -> new SkillSearchResult(
				this.skillService.search(new SearchSkillsCommand(query, limit))));
	}

	@McpTool(
			name = "create_skill",
			description = "Cria uma competência canônica para associação a fatos profissionais e retorna seu UUID. Se já existir uma com o mesmo nome, ignorando maiúsculas e minúsculas, retorna a existente sem alterar nome ou aliases. Informe aliases apenas para uma competência nova.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar competência",
					readOnlyHint = false,
					destructiveHint = false,
					idempotentHint = true,
					openWorldHint = false))
	public SkillDetails createSkill(
			@McpToolParam(description = "Nome canônico da competência. Obrigatório, não pode ficar em branco e aceita até 80 caracteres.") String name,
			@McpToolParam(description = "Até 50 nomes alternativos para facilitar buscas. Cada um deve ser preenchido e ter até 80 caracteres. Só são gravados quando uma nova competência é criada.", required = false)
				String[] aliases) {
		return McpToolErrors.call("create_skill", () -> this.skillService.create(
				new CreateSkillCommand(name, aliases == null ? null : Arrays.asList(aliases))));
	}
}
