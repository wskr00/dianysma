package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.project.ProjectCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.project.ProjectDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.project.ProjectList;
import io.github.wskr00.dianysma.knowledge.service.ProjectService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectTools {

	private final ProjectService projectService;

	@McpTool(
			name = "get_project",
			description = "Busca um projeto pelo UUID. Use-o para consultar os dados atuais antes de atualizar ou associar o projeto a uma participação.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Consultar projeto",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public ProjectDetails getProject(
			@McpToolParam(description = "UUID do projeto.") UUID projectId) {
		return McpToolErrors.call("get_project", () -> this.projectService.get(projectId));
	}

	@McpTool(
			name = "list_projects",
			description = "Lista os projetos da atualização mais recente à mais antiga. A resposta contém a coleção `projects`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Listar projetos",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public ProjectList listProjects(
			@McpToolParam(description = "Máximo de registros. Padrão: 20; intervalo: 1–50.", required = false) Integer limit) {
		return McpToolErrors.call("list_projects", () -> new ProjectList(
				this.projectService.list(new ListKnowledgeRecordsCommand(limit))));
	}

	@McpTool(
			name = "create_project",
			description = "Cria um projeto e retorna seu UUID para uso em uma participação. A organização é opcional; quando informada, deve existir e ser selecionada por seu UUID.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar projeto",
				readOnlyHint = false,
				destructiveHint = false,
				idempotentHint = false,
				openWorldHint = false))
	public ProjectDetails createProject(
			@McpToolParam(description = "UUID de uma organização existente que contextualiza o projeto. Pesquise com `search_organizations` e, se necessário, crie-a com `create_organization`; omita se não houver organização associada.", required = false) UUID organizationId,
			@McpToolParam(description = "Nome obrigatório do projeto, até 200 caracteres.") String name,
			@McpToolParam(description = "Descrição opcional do projeto.", required = false) String description) {
		return McpToolErrors.call("create_project",
				() -> this.projectService.create(new ProjectCommand(organizationId, name, description)));
	}

	@McpTool(
			name = "update_project",
			description = "Substitui os dados do projeto. Consulte `get_project` antes para preservar valores atuais: o nome é obrigatório; organização e descrição omitidas/null serão removidas.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Atualizar projeto",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public ProjectDetails updateProject(
			@McpToolParam(description = "UUID do projeto que será atualizado.") UUID projectId,
			@McpToolParam(description = "UUID opcional da organização; omitir ou enviar null limpa o vínculo.", required = false) UUID organizationId,
			@McpToolParam(description = "Novo nome obrigatório do projeto, até 200 caracteres.") String name,
			@McpToolParam(description = "Nova descrição; omitir ou enviar null limpa o campo.", required = false) String description) {
		return McpToolErrors.call("update_project", () -> this.projectService.update(projectId,
				new ProjectCommand(organizationId, name, description)));
	}

	@McpTool(
			name = "delete_project",
			description = "Exclui permanentemente o projeto somente quando não há participações associadas. Se houver vínculos, a exclusão é recusada; se o UUID não existir, retorna `deleted=false`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Excluir projeto",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public DeleteResult deleteProject(
			@McpToolParam(description = "UUID do projeto que será excluído.") UUID projectId) {
		return McpToolErrors.call("delete_project", () -> this.projectService.delete(projectId));
	}
}
