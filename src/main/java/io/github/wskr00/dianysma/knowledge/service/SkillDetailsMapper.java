package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.result.skill.SkillDetails;
import io.github.wskr00.dianysma.knowledge.domain.Skill;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SkillDetailsMapper {

	static SkillDetails toDetails(Skill skill) {
		List<String> aliases = skill.getAliases() == null ? List.of() : Arrays.stream(skill.getAliases())
				.filter(StringUtils::hasText)
				.map(String::strip)
				.toList();
		return new SkillDetails(skill.getId(), skill.getName(), aliases);
	}
}
