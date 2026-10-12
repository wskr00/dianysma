package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.skill.CreateSkillCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.skill.SearchSkillsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.skill.SkillDetails;
import io.github.wskr00.dianysma.knowledge.domain.Skill;
import io.github.wskr00.dianysma.knowledge.repository.SkillRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class SkillService {

	private static final String READ_SCOPE = "hasAuthority('SCOPE_dianysma/knowledge.read')";
	private static final String WRITE_SCOPE = "hasAuthority('SCOPE_dianysma/knowledge.write')";
	private static final int DEFAULT_LIMIT = 10;
	private final SkillRepository skillRepository;

	@PreAuthorize(READ_SCOPE)
	@Transactional(readOnly = true)
	public List<SkillDetails> search(@NotNull @Valid SearchSkillsCommand command) {
		Limit limit = Limit.of(command.limit() == null ? DEFAULT_LIMIT : command.limit());
		return this.skillRepository.searchByNameOrAlias(command.query().strip(), limit)
				.stream()
				.map(SkillDetailsMapper::toDetails)
				.toList();
	}

	@PreAuthorize(WRITE_SCOPE)
	public SkillDetails create(@NotNull @Valid CreateSkillCommand command) {
		String normalizedName = command.name().strip();

		Skill existing = this.skillRepository.findFirstByNameIgnoreCase(normalizedName).orElse(null);
		if (existing != null) {
			return SkillDetailsMapper.toDetails(existing);
		}

		try {
			Skill skill = this.skillRepository.saveAndFlush(
					new Skill(normalizedName, normalizeAliases(command.aliases())));
			return SkillDetailsMapper.toDetails(skill);
		}
		catch (DataIntegrityViolationException exception) {
			return this.skillRepository.findFirstByNameIgnoreCase(normalizedName)
					.map(SkillDetailsMapper::toDetails)
					.orElseThrow(() -> exception);
		}
	}

	private static String[] normalizeAliases(List<String> aliases) {
		if (aliases == null) {
			return new String[0];
		}
		Map<String, String> distinctAliases = new TreeMap<>();
		for (String alias : aliases) {
			String normalized = alias.strip();
			distinctAliases.putIfAbsent(normalized.toLowerCase(Locale.ROOT), normalized);
		}
		return distinctAliases.values().toArray(String[]::new);
	}
}
