package io.github.wskr00.dianysma.knowledge.controller.dto.command.language;

import io.github.wskr00.dianysma.knowledge.domain.enums.LanguageProficiency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LanguageCommand(
		@NotBlank(message = "O nome do idioma não pode ficar em branco.")
		@Size(max = 50, message = "O nome do idioma deve ter no máximo 50 caracteres.") String name,
		@NotNull(message = "A proficiência no idioma é obrigatória.") LanguageProficiency proficiency) {
}
