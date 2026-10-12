package io.github.wskr00.dianysma.knowledge.controller.mcp;

import java.util.List;
import java.util.UUID;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.CreateClaimCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.ListClaimsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.UpdateClaimCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.claim.ClaimDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.claim.ClaimList;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.service.ClaimService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClaimTools {

	private final ClaimService claimService;

	@McpTool(
			name = "get_claim",
			description = "Busca um fato profissional pelo UUID. Consulte-o antes de atualizar se precisar confirmar o conteúdo ou os vínculos atuais.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Consultar fato profissional",
					readOnlyHint = true,
					destructiveHint = false,
					idempotentHint = true,
					openWorldHint = false))
	public ClaimDetails getClaim(
			@McpToolParam(description = "UUID do fato profissional que será consultado.") UUID claimId) {
		return McpToolErrors.call("get_claim", () -> this.claimService.get(claimId));
	}

	@McpTool(
			name = "list_claims",
			description = "Lista fatos profissionais do mais recentemente atualizado ao mais antigo. A resposta contém a coleção `claims`. Informe `participationId` para filtrar por participação; omita para incluir fatos associados e autônomos.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Listar fatos profissionais",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public ClaimList listClaims(
			@McpToolParam(description = "UUID opcional da participação; omita para incluir todos os fatos.", required = false)
				UUID participationId,
			@McpToolParam(description = "Máximo de registros. Padrão: 20; intervalo: 1–50.", required = false)
				Integer limit) {
		return McpToolErrors.call("list_claims", () -> new ClaimList(
				this.claimService.list(new ListClaimsCommand(participationId, limit))));
	}

	@McpTool(
			name = "create_claim",
			description = "Cria um fato profissional específico e confirmado, como uma entrega, responsabilidade ou resultado. Antes de criar, use `search_knowledge` para procurar fatos semelhantes; a busca retorna apenas os resultados mais relevantes, então a ausência de resultados não garante que o fato não esteja cadastrado. Informe apenas IDs existentes; use `search_skills` para encontrar competências. Cada chamada cria um novo registro e retorna seu UUID.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar fato profissional",
					readOnlyHint = false,
					destructiveHint = false,
					idempotentHint = false,
					openWorldHint = false))
	public ClaimDetails createClaim(
			@McpToolParam(description = "Declaração factual que será registrada. Obrigatória, não pode ficar em branco e aceita até 4.000 caracteres.") String content,
			@McpToolParam(description = "UUID de uma participação existente para contextualizar o fato. Omita se o fato for autônomo.", required = false)
				UUID participationId,
			@McpToolParam(description = "UUIDs de competências existentes para associar, até 50. Use `search_skills` para obter os IDs. Omita ou envie [] se não houver competências.", required = false)
				List<UUID> skillIds) {
		return McpToolErrors.call("create_claim",
				() -> this.claimService.create(new CreateClaimCommand(content, participationId, skillIds)));
	}

	@McpTool(
			name = "update_claim",
			description = "Substitui o conteúdo completo de um fato profissional. Consulte `get_claim` antes se precisar preservar dados atuais. Os vínculos são mantidos quando seus parâmetros são omitidos ou null: informe `participationId` para associar, `clearParticipation=true` para desassociar, e `skillIds` para substituir as competências (`[]` remove todas). Não combine `participationId` com `clearParticipation=true`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Atualizar fato profissional",
					readOnlyHint = false,
					destructiveHint = true,
					idempotentHint = true,
					openWorldHint = false))
	public ClaimDetails updateClaim(
			@McpToolParam(description = "UUID do fato profissional retornado por `get_claim`, `list_claims` ou `search_knowledge`.") UUID claimId,
			@McpToolParam(description = "Conteúdo completo que substituirá o conteúdo atual. Obrigatório, não pode ficar em branco e deve ter até 4.000 caracteres.") String content,
			@McpToolParam(description = "UUID de uma participação existente para associar. Omita ou envie null para manter o vínculo atual.", required = false)
				UUID participationId,
			@McpToolParam(description = "Informe true para remover o vínculo atual com a participação; omita ou informe false/null para mantê-lo.", required = false)
				Boolean clearParticipation,
			@McpToolParam(description = "UUIDs das competências que substituirão os vínculos atuais, até 50. Omita ou envie null para mantê-los; envie [] para remover todos.", required = false)
				List<UUID> skillIds) {
		return McpToolErrors.call("update_claim", () -> this.claimService.update(
				new UpdateClaimCommand(claimId, content, participationId, clearParticipation, skillIds)));
	}

	@McpTool(
			name = "delete_claim",
			description = "Exclui permanentemente o fato profissional e remove seu índice de busca semântica. Se o UUID não existir, retorna `deleted=false` sem alterar outros registros.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Excluir fato profissional",
					readOnlyHint = false,
					destructiveHint = true,
					idempotentHint = true,
					openWorldHint = false))
	public DeleteResult deleteClaim(
			@McpToolParam(description = "UUID do fato profissional que será excluído.") UUID claimId) {
		return McpToolErrors.call("delete_claim", () -> this.claimService.delete(claimId));
	}
}
