package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.participation.ParticipationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.participation.ParticipationDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.participation.ParticipationList;
import io.github.wskr00.dianysma.knowledge.service.ParticipationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipationTools {

	private final ParticipationService participationService;

	@McpTool(
			name = "get_participation",
			description = "Busca uma participação profissional pelo UUID. Use-a para conferir os vínculos com experiência e projeto antes de atualizar ou associar fatos profissionais.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Consultar participação",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public ParticipationDetails getParticipation(
			@McpToolParam(description = "UUID da participação profissional.") UUID participationId) {
		return McpToolErrors.call("get_participation", () -> this.participationService.get(participationId));
	}

	@McpTool(
			name = "list_participations",
			description = "Lista as participações da atualização mais recente à mais antiga. A resposta contém a coleção `participations`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Listar participações",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public ParticipationList listParticipations(
			@McpToolParam(description = "Máximo de registros. Padrão: 20; intervalo: 1–50.", required = false) Integer limit) {
		return McpToolErrors.call("list_participations", () -> new ParticipationList(
				this.participationService.list(new ListKnowledgeRecordsCommand(limit))));
	}

	@McpTool(
			name = "create_participation",
			description = "Cria uma participação e retorna seu UUID para associação a fatos profissionais. Informe pelo menos uma experiência ou um projeto já existente; pode informar ambos. A mesma combinação de experiência e projeto só pode existir uma vez.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar participação",
				readOnlyHint = false,
				destructiveHint = false,
				idempotentHint = false,
				openWorldHint = false))
	public ParticipationDetails createParticipation(
			@McpToolParam(description = "UUID de uma experiência existente para associar, se aplicável. Informe este campo, `projectId` ou ambos.", required = false) UUID experienceId,
			@McpToolParam(description = "UUID de um projeto existente para associar, se aplicável. Informe este campo, `experienceId` ou ambos.", required = false) UUID projectId,
			@McpToolParam(description = "Descrição opcional da participação, até 4.000 caracteres.", required = false) String description) {
		return McpToolErrors.call("create_participation",
				() -> this.participationService.create(new ParticipationCommand(experienceId, projectId, description)));
	}

	@McpTool(
			name = "update_participation",
			description = "Substitui os vínculos e a descrição da participação. Consulte `get_participation` antes: cada vínculo ou descrição opcional omitido/null será removido. Informe pelo menos `experienceId` ou `projectId`; envie todos os vínculos que deseja manter.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Atualizar participação",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public ParticipationDetails updateParticipation(
			@McpToolParam(description = "UUID da participação que será atualizada.") UUID participationId,
			@McpToolParam(description = "UUID opcional da experiência; omitir ou enviar null limpa o vínculo.", required = false) UUID experienceId,
			@McpToolParam(description = "UUID opcional do projeto; omitir ou enviar null limpa o vínculo.", required = false) UUID projectId,
			@McpToolParam(description = "Nova descrição, até 4.000 caracteres; omitir ou enviar null limpa o campo.", required = false) String description) {
		return McpToolErrors.call("update_participation", () -> this.participationService.update(participationId,
				new ParticipationCommand(experienceId, projectId, description)));
	}

	@McpTool(
			name = "delete_participation",
			description = "Exclui permanentemente a participação quando não há fatos profissionais associados. Se houver fatos vinculados, a exclusão é recusada; se o UUID não existir, retorna `deleted=false`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Excluir participação",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public DeleteResult deleteParticipation(
			@McpToolParam(description = "UUID da participação que será excluída.") UUID participationId) {
		return McpToolErrors.call("delete_participation", () -> this.participationService.delete(participationId));
	}
}
