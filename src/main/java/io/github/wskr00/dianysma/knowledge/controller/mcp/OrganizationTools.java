package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.CreateOrganizationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.SearchOrganizationsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.UpdateOrganizationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.organization.OrganizationDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.organization.OrganizationSearchResult;
import io.github.wskr00.dianysma.knowledge.service.OrganizationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationTools {

	private final OrganizationService organizationService;

	@McpTool(
			name = "get_organization",
			description = "Busca uma organização pelo UUID. Use o registro retornado para confirmar o ID antes de associá-lo a uma experiência ou projeto.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Consultar organização",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public OrganizationDetails getOrganization(
			@McpToolParam(description = "UUID da organização.") UUID organizationId) {
		return McpToolErrors.call("get_organization", () -> this.organizationService.get(organizationId));
	}

	@McpTool(
			name = "search_organizations",
			description = "Pesquisa organizações por trecho do nome, sem diferenciar maiúsculas de minúsculas. A resposta contém a coleção `organizations`. Use o UUID retornado ao criar uma experiência ou projeto; reutilize uma organização existente em vez de duplicá-la.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Pesquisar organizações",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public OrganizationSearchResult searchOrganizations(
			@McpToolParam(description = "Nome completo ou trecho do nome da organização. Obrigatório e limitado a 120 caracteres.") String query,
			@McpToolParam(description = "Máximo de resultados. Padrão: 10; intervalo: 1–50.", required = false) Integer limit) {
		return McpToolErrors.call("search_organizations", () -> new OrganizationSearchResult(
				this.organizationService.search(new SearchOrganizationsCommand(query, limit))));
	}

	@McpTool(
			name = "create_organization",
			description = "Cria uma organização pelo nome e retorna seu UUID. Antes, use `search_organizations` para reutilizar uma organização já cadastrada; o servidor não combina registros duplicados automaticamente.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar organização",
				readOnlyHint = false,
				destructiveHint = false,
				idempotentHint = false,
				openWorldHint = false))
	public OrganizationDetails createOrganization(
			@McpToolParam(description = "Nome obrigatório da organização, até 120 caracteres.") String name) {
		return McpToolErrors.call("create_organization",
				() -> this.organizationService.create(new CreateOrganizationCommand(name)));
	}

	@McpTool(
			name = "update_organization",
			description = "Substitui o nome da organização identificada sem alterar seus vínculos. Consulte `get_organization` antes para confirmar o registro e evitar sobrescrever o nome errado.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Atualizar organização",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public OrganizationDetails updateOrganization(
			@McpToolParam(description = "UUID da organização que será atualizada.") UUID organizationId,
			@McpToolParam(description = "Novo nome obrigatório, até 120 caracteres.") String name) {
		return McpToolErrors.call("update_organization",
				() -> this.organizationService.update(new UpdateOrganizationCommand(organizationId, name)));
	}

	@McpTool(
			name = "delete_organization",
			description = "Exclui permanentemente uma organização somente quando não há experiências nem projetos associados. Se houver vínculos, a exclusão é recusada; se o UUID não existir, retorna `deleted=false`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Excluir organização",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public DeleteResult deleteOrganization(
			@McpToolParam(description = "UUID da organização que será excluída.") UUID organizationId) {
		return McpToolErrors.call("delete_organization", () -> this.organizationService.delete(organizationId));
	}
}
