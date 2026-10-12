package io.github.wskr00.dianysma.knowledge.controller.dto.command.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProfileCommand(
		@Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.") String name,
		@Email(message = "Informe um endereço de e-mail válido.")
		@Size(max = 50, message = "O e-mail deve ter no máximo 50 caracteres.") String email,
		@Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.") String phone,
		@Size(max = 40, message = "A cidade deve ter no máximo 40 caracteres.") String city,
		@Size(max = 40, message = "O estado deve ter no máximo 40 caracteres.") String state,
		@Size(max = 40, message = "O país deve ter no máximo 40 caracteres.") String country,
		@Size(max = 150, message = "A URL do LinkedIn deve ter no máximo 150 caracteres.") String linkedinUrl,
		@Size(max = 150, message = "A URL do GitHub deve ter no máximo 150 caracteres.") String githubUrl,
		@Size(max = 150, message = "A URL do portfólio deve ter no máximo 150 caracteres.") String portfolioUrl) {
}
