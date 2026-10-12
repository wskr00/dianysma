package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.experience.ExperienceCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.experience.ExperienceDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.experience.ExperienceList;
import io.github.wskr00.dianysma.knowledge.service.ExperienceService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExperienceTools {

	private final ExperienceService experienceService;

	@McpTool(
			name = "get_experience",
			description = "Busca uma experiência profissional pelo UUID. Consulte-a antes de atualizar se precisar preservar os valores atuais.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Consultar experiência",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public ExperienceDetails getExperience(
			@McpToolParam(description = "UUID da experiência profissional.") UUID experienceId) {
		return McpToolErrors.call("get_experience", () -> this.experienceService.get(experienceId));
	}

	@McpTool(
			name = "list_experiences",
			description = "Lista as experiências profissionais da atualização mais recente à mais antiga. A resposta contém a coleção `experiences`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Listar experiências",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public ExperienceList listExperiences(
			@McpToolParam(description = "Máximo de registros. Padrão: 20; intervalo: 1–50.", required = false) Integer limit) {
		return McpToolErrors.call("list_experiences", () -> new ExperienceList(
				this.experienceService.list(new ListKnowledgeRecordsCommand(limit))));
	}

	@McpTool(
			name = "create_experience",
			description = "Registra uma experiência profissional e retorna seu UUID. `employerId` deve identificar a organização empregadora; `clientId` é opcional e identifica a organização cliente, que pode ser a própria empregadora. O tipo de vínculo é texto livre, por exemplo CLT, PJ, estágio ou freelancer. Datas e descrição são opcionais.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar experiência",
				readOnlyHint = false,
				destructiveHint = false,
				idempotentHint = false,
				openWorldHint = false))
	public ExperienceDetails createExperience(
			@McpToolParam(description = "UUID da organização empregadora. Use `search_organizations` para localizá-la; se não existir, crie-a com `create_organization` primeiro.") UUID employerId,
			@McpToolParam(description = "UUID de uma organização cliente, quando aplicável. Use uma organização existente; pode ser igual à empregadora.", required = false) UUID clientId,
			@McpToolParam(description = "Cargo ou função, obrigatório e limitado a 200 caracteres.") String role,
			@McpToolParam(description = "Tipo de vínculo em texto livre, obrigatório e limitado a 40 caracteres; por exemplo, CLT, PJ, estágio ou freelancer.") String employmentType,
			@McpToolParam(description = "Descrição opcional da experiência.", required = false) String description,
			@McpToolParam(description = "Data inicial opcional no formato ISO 8601 (AAAA-MM-DD).", required = false) LocalDate startedAt,
			@McpToolParam(description = "Data final opcional no formato ISO 8601 (AAAA-MM-DD); não pode ser anterior a startedAt.", required = false) LocalDate endedAt) {
		ExperienceCommand command = new ExperienceCommand(employerId, clientId, role, employmentType,
				description, startedAt, endedAt);
		return McpToolErrors.call("create_experience", () -> this.experienceService.create(command));
	}

	@McpTool(
			name = "update_experience",
			description = "Substitui os dados da experiência. Consulte `get_experience` antes para preservar valores atuais. Empregadora, cargo e tipo de vínculo são obrigatórios; campos opcionais omitidos ou null serão apagados.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Atualizar experiência",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public ExperienceDetails updateExperience(
			@McpToolParam(description = "UUID da experiência que será atualizada.") UUID experienceId,
			@McpToolParam(description = "UUID da organização empregadora existente.") UUID employerId,
			@McpToolParam(description = "UUID opcional da organização cliente; omitir ou enviar null limpa o vínculo.", required = false) UUID clientId,
			@McpToolParam(description = "Novo cargo ou função, obrigatório e limitado a 200 caracteres.") String role,
			@McpToolParam(description = "Novo tipo de vínculo em texto, obrigatório e limitado a 40 caracteres.") String employmentType,
			@McpToolParam(description = "Nova descrição; omitir ou enviar null limpa o campo.", required = false) String description,
			@McpToolParam(description = "Nova data inicial; omitir ou enviar null limpa o campo.", required = false) LocalDate startedAt,
			@McpToolParam(description = "Nova data final; não pode ser anterior a startedAt. Omitir ou enviar null limpa o campo.", required = false) LocalDate endedAt) {
		ExperienceCommand command = new ExperienceCommand(employerId, clientId, role, employmentType,
				description, startedAt, endedAt);
		return McpToolErrors.call("update_experience", () -> this.experienceService.update(experienceId, command));
	}

	@McpTool(
			name = "delete_experience",
			description = "Exclui permanentemente uma experiência quando não há participações associadas. Se houver participações, a exclusão é recusada; se o UUID não existir, retorna `deleted=false`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Excluir experiência",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public DeleteResult deleteExperience(
			@McpToolParam(description = "UUID da experiência que será excluída.") UUID experienceId) {
		return McpToolErrors.call("delete_experience", () -> this.experienceService.delete(experienceId));
	}
}
