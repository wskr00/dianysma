package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.profile.ProfileCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.profile.ProfileDetails;
import io.github.wskr00.dianysma.knowledge.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileTools {

	private final ProfileService profileService;

	@McpTool(
			name = "get_profile",
			description = "Consulta o único perfil pessoal desta instalação. Se ainda não existir, use `create_profile` antes de cadastrar idiomas.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Consultar perfil",
				readOnlyHint = true,
				destructiveHint = false,
				idempotentHint = true,
				openWorldHint = false))
	public ProfileDetails getProfile() {
		return McpToolErrors.call("get_profile", this.profileService::get);
	}

	@McpTool(
			name = "create_profile",
			description = "Cria o único perfil pessoal desta instalação e retorna seu UUID. Todos os campos são opcionais. Use `get_profile` primeiro para verificar se já existe; para alterá-lo, use `update_profile`.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Criar perfil",
				readOnlyHint = false,
				destructiveHint = false,
				idempotentHint = false,
				openWorldHint = false))
	public ProfileDetails createProfile(
			@McpToolParam(description = "Nome pessoal, até 120 caracteres.", required = false) String name,
			@McpToolParam(description = "E-mail pessoal, até 50 caracteres.", required = false) String email,
			@McpToolParam(description = "Telefone como texto, até 20 caracteres.", required = false) String phone,
			@McpToolParam(description = "Cidade, até 40 caracteres.", required = false) String city,
			@McpToolParam(description = "Estado, até 40 caracteres.", required = false) String state,
			@McpToolParam(description = "País, até 40 caracteres.", required = false) String country,
			@McpToolParam(description = "URL do LinkedIn, até 150 caracteres.", required = false) String linkedinUrl,
			@McpToolParam(description = "URL do GitHub, até 150 caracteres.", required = false) String githubUrl,
			@McpToolParam(description = "URL do portfólio, até 150 caracteres.", required = false) String portfolioUrl) {
		ProfileCommand command = new ProfileCommand(name, email, phone, city, state, country, linkedinUrl,
				githubUrl, portfolioUrl);
		return McpToolErrors.call("create_profile", () -> this.profileService.create(command));
	}

	@McpTool(
			name = "update_profile",
			description = "Substitui todos os dados do perfil; não é uma atualização parcial. Consulte `get_profile` e reenvie os valores que deseja manter. Qualquer campo omitido ou null será apagado. Por exemplo, para alterar só a cidade, envie também os demais valores atuais.",
			generateOutputSchema = true,
			annotations = @McpTool.McpAnnotations(
				title = "Atualizar perfil",
				readOnlyHint = false,
				destructiveHint = true,
				idempotentHint = true,
				openWorldHint = false))
	public ProfileDetails updateProfile(
			@McpToolParam(description = "Nome pessoal, até 120 caracteres. Informe o valor atual para mantê-lo; omita ou envie null para apagar.", required = false) String name,
			@McpToolParam(description = "E-mail pessoal, até 50 caracteres. Informe o valor atual para mantê-lo; omita ou envie null para apagar.", required = false) String email,
			@McpToolParam(description = "Telefone como texto, até 20 caracteres. Informe o valor atual para mantê-lo; omita ou envie null para apagar.", required = false) String phone,
			@McpToolParam(description = "Cidade, até 40 caracteres. Informe o valor atual para mantê-la; omita ou envie null para apagar.", required = false) String city,
			@McpToolParam(description = "Estado ou região, até 40 caracteres. Informe o valor atual para mantê-lo; omita ou envie null para apagar.", required = false) String state,
			@McpToolParam(description = "País, até 40 caracteres. Informe o valor atual para mantê-lo; omita ou envie null para apagar.", required = false) String country,
			@McpToolParam(description = "URL do LinkedIn, até 150 caracteres. Informe a URL atual para mantê-la; omita ou envie null para apagar.", required = false) String linkedinUrl,
			@McpToolParam(description = "URL do GitHub, até 150 caracteres. Informe a URL atual para mantê-la; omita ou envie null para apagar.", required = false) String githubUrl,
			@McpToolParam(description = "URL do portfólio, até 150 caracteres. Informe a URL atual para mantê-la; omita ou envie null para apagar.", required = false) String portfolioUrl) {
		ProfileCommand command = new ProfileCommand(name, email, phone, city, state, country, linkedinUrl,
				githubUrl, portfolioUrl);
		return McpToolErrors.call("update_profile", () -> this.profileService.update(command));
	}
}
