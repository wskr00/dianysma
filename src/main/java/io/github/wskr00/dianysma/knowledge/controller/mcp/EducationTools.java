package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.education.EducationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.education.EducationDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.education.EducationList;
import io.github.wskr00.dianysma.knowledge.domain.enums.EducationType;
import io.github.wskr00.dianysma.knowledge.service.EducationService;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EducationTools {

	private final EducationService educationService;

	@McpTool(
			name = "get_education",
			description = "Busca uma formação acadêmica ou profissional pelo UUID. Use-a para consultar os dados atuais antes de atualizar.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Consultar formação",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public EducationDetails getEducation(
			@McpToolParam(description = "UUID da formação.") UUID educationId) {
		return McpToolErrors.call("get_education", () -> this.educationService.get(educationId));
	}

	@McpTool(
			name = "list_education",
			description = "Lista as formações do registro atualizado mais recentemente ao mais antigo. A resposta contém a coleção `educations`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Listar formações",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public EducationList listEducation(
			@McpToolParam(description = "Máximo de registros. Padrão: 20; intervalo: 1–50.", required = false) Integer limit) {
		return McpToolErrors.call("list_education", () -> new EducationList(
				this.educationService.list(new ListKnowledgeRecordsCommand(limit))));
	}

	@McpTool(
			name = "create_education",
			description = "Registra uma formação acadêmica ou profissional e retorna o UUID criado. Escolha `CERTIFICATION` para certificação, `COURSE` para curso ou `DEGREE` para graduação ou pós-graduação. Use datas no formato AAAA-MM-DD.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar formação",
				readOnlyHint = false,
				destructiveHint = false,
				idempotentHint = false,
				openWorldHint = false))
	public EducationDetails createEducation(
			@McpToolParam(description = "Instituição de ensino, obrigatória e limitada a 100 caracteres.") String institution,
			@McpToolParam(description = "Categoria da formação: CERTIFICATION (certificação), COURSE (curso) ou DEGREE (graduação ou pós-graduação).") EducationType type,
			@McpToolParam(description = "Título do curso ou formação, obrigatório e limitado a 120 caracteres.") String title,
			@McpToolParam(description = "Grau ou título acadêmico opcional, até 100 caracteres.", required = false) String degree,
			@McpToolParam(description = "Descrição opcional, até 4.000 caracteres.", required = false) String description,
			@McpToolParam(description = "Carga horária opcional, em horas; use zero ou um valor positivo.", required = false) Integer workloadHours,
			@McpToolParam(description = "Data inicial opcional no formato AAAA-MM-DD.", required = false) LocalDate startedAt,
			@McpToolParam(description = "Data de conclusão opcional ou prevista, no formato AAAA-MM-DD; não pode ser anterior a startedAt.", required = false) LocalDate completedAt,
			@McpToolParam(description = "Informe true quando completedAt for uma previsão, false quando for a data efetiva; omita se não houver data de conclusão.", required = false) Boolean expected) {
		EducationCommand command = new EducationCommand(institution, type, title, degree, description,
				workloadHours, startedAt, completedAt, expected);
		return McpToolErrors.call("create_education", () -> this.educationService.create(command));
	}

	@McpTool(
			name = "update_education",
			description = "Substitui os dados da formação. Consulte `get_education` antes para preservar valores existentes: instituição, tipo e título são obrigatórios; cada campo opcional omitido ou null será apagado.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Atualizar formação",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public EducationDetails updateEducation(
			@McpToolParam(description = "UUID da formação que será atualizada.") UUID educationId,
			@McpToolParam(description = "Nova instituição, obrigatória e limitada a 100 caracteres.") String institution,
			@McpToolParam(description = "Novo tipo: CERTIFICATION, COURSE ou DEGREE.") EducationType type,
			@McpToolParam(description = "Novo título, obrigatório e limitado a 120 caracteres.") String title,
			@McpToolParam(description = "Novo grau ou título acadêmico; omitir ou enviar null limpa o campo.", required = false) String degree,
			@McpToolParam(description = "Nova descrição, até 4.000 caracteres; omitir ou enviar null limpa o campo.", required = false) String description,
			@McpToolParam(description = "Nova carga horária em horas; informe zero ou valor positivo. Omitir ou enviar null limpa o campo.", required = false) Integer workloadHours,
			@McpToolParam(description = "Nova data inicial; omitir ou enviar null limpa o campo.", required = false) LocalDate startedAt,
			@McpToolParam(description = "Nova data de conclusão ou previsão; não pode ser anterior a startedAt. Omitir ou enviar null limpa o campo.", required = false) LocalDate completedAt,
			@McpToolParam(description = "true se completedAt for uma previsão, false se for efetiva; omitir limpa o campo.", required = false) Boolean expected) {
		EducationCommand command = new EducationCommand(institution, type, title, degree, description,
				workloadHours, startedAt, completedAt, expected);
		return McpToolErrors.call("update_education", () -> this.educationService.update(educationId, command));
	}

	@McpTool(
			name = "delete_education",
			description = "Exclui permanentemente a formação e remove seu índice de busca semântica, se houver. Se o UUID não existir, retorna `deleted=false`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Excluir formação",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public DeleteResult deleteEducation(
			@McpToolParam(description = "UUID da formação que será excluída.") UUID educationId) {
		return McpToolErrors.call("delete_education", () -> this.educationService.delete(educationId));
	}
}
