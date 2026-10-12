package io.github.wskr00.dianysma.knowledge.controller.dto.result.skill;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import java.util.UUID;

public record SkillDetails(
		@JsonPropertyDescription("UUID da competência canônica; use-o ao associar a competência a um fato profissional.") UUID id,
		@JsonPropertyDescription("Nome canônico da competência.") String name,
		@JsonPropertyDescription("Nomes alternativos usados para localizar esta competência; não são competências distintas.") List<String> aliases) {
}
