package io.github.wskr00.dianysma.knowledge.controller.dto.command.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateSkillCommand(
		@NotBlank(message = "O nome da competência não pode ficar em branco.")
		@Size(max = 80, message = "O nome da competência deve ter no máximo 80 caracteres.") String name,
		@Size(max = 50, message = "Uma competência pode ter no máximo 50 nomes alternativos.")
		List<@NotBlank(message = "Os nomes alternativos da competência não podem ficar em branco.")
			@Size(max = 80, message = "Cada nome alternativo da competência deve ter no máximo 80 caracteres.") String> aliases) {
}
