package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.language.LanguageCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.language.LanguageDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.language.LanguageList;
import io.github.wskr00.dianysma.knowledge.domain.enums.LanguageProficiency;
import io.github.wskr00.dianysma.knowledge.service.LanguageService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LanguageTools {

	private final LanguageService languageService;

	@McpTool(
			name = "get_language",
			description = "Consulta um idioma do perfil pelo UUID.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Consultar idioma",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public LanguageDetails getLanguage(
			@McpToolParam(description = "UUID do idioma.") UUID languageId) {
		return McpToolErrors.call("get_language", () -> this.languageService.get(languageId));
	}

	@McpTool(
			name = "list_languages",
			description = "Lista em ordem alfabética os idiomas associados ao perfil pessoal. A resposta contém a coleção `languages`. Se ela já contiver o idioma, use `update_language` em vez de criar outro registro.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Listar idiomas",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public LanguageList listLanguages(
			@McpToolParam(description = "Máximo de registros. Padrão: 20; intervalo: 1–50.", required = false) Integer limit) {
		return McpToolErrors.call("list_languages", () -> new LanguageList(
				this.languageService.list(new ListKnowledgeRecordsCommand(limit))));
	}

	@McpTool(
			name = "create_language",
			description = "Registra um idioma no perfil pessoal e retorna seu UUID. Crie o perfil com `create_profile` antes. Consulte `list_languages` para evitar duplicar um idioma já registrado.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar idioma",
				readOnlyHint = false,
				destructiveHint = false,
				idempotentHint = false,
				openWorldHint = false))
	public LanguageDetails createLanguage(
			@McpToolParam(description = "Nome do idioma, obrigatório e limitado a 50 caracteres.") String name,
			@McpToolParam(description = "Proficiência: BASIC (básica), INTERMEDIATE (intermediária), ADVANCED (avançada), FLUENT (fluente) ou NATIVE (nativa).") LanguageProficiency proficiency) {
		return McpToolErrors.call("create_language",
				() -> this.languageService.create(new LanguageCommand(name, proficiency)));
	}

	@McpTool(
			name = "update_language",
			description = "Substitui o nome e a proficiência do idioma identificado. Consulte `get_language` ou `list_languages` para conferir o registro antes de alterá-lo.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Atualizar idioma",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public LanguageDetails updateLanguage(
			@McpToolParam(description = "UUID do idioma que será atualizado.") UUID languageId,
			@McpToolParam(description = "Novo nome do idioma, obrigatório e limitado a 50 caracteres.") String name,
			@McpToolParam(description = "Nova proficiência: BASIC (básica), INTERMEDIATE (intermediária), ADVANCED (avançada), FLUENT (fluente) ou NATIVE (nativa).") LanguageProficiency proficiency) {
		return McpToolErrors.call("update_language", () -> this.languageService.update(languageId,
				new LanguageCommand(name, proficiency)));
	}

	@McpTool(
			name = "delete_language",
			description = "Exclui um idioma do perfil. Se o ID não existir, retorna deleted=false.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Excluir idioma",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public DeleteResult deleteLanguage(
			@McpToolParam(description = "UUID do idioma que será excluído.") UUID languageId) {
		return McpToolErrors.call("delete_language", () -> this.languageService.delete(languageId));
	}
}
