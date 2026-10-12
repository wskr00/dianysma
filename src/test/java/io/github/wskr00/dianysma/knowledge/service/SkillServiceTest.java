package io.github.wskr00.dianysma.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.skill.CreateSkillCommand;
import io.github.wskr00.dianysma.knowledge.domain.Skill;
import io.github.wskr00.dianysma.knowledge.repository.SkillRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

	@Mock
	private SkillRepository skillRepository;

	@InjectMocks
	private SkillService skillService;

	@Test
	void trimsNewSkillAndDeduplicatesAliasesIgnoringCase() {
		when(this.skillRepository.findFirstByNameIgnoreCase("Java")).thenReturn(Optional.empty());
		when(this.skillRepository.saveAndFlush(any(Skill.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		var result = this.skillService.create(new CreateSkillCommand(" Java ",
				List.of("JVM", " java ", "jvm", "Spring")));

		assertThat(result.name()).isEqualTo("Java");
		assertThat(result.aliases()).containsExactly("java", "JVM", "Spring");
		verify(this.skillRepository).saveAndFlush(argThat(skill ->
				skill.getName().equals("Java")
						&& List.of(skill.getAliases()).equals(List.of("java", "JVM", "Spring"))));
	}

	@Test
	void reusesExistingCanonicalSkillWithoutOverwritingItsAliases() {
		var existing = new Skill("Java", new String[] { "JVM" });
		when(this.skillRepository.findFirstByNameIgnoreCase("java")).thenReturn(Optional.of(existing));

		var result = this.skillService.create(new CreateSkillCommand(" java ", List.of("HotSpot")));

		assertThat(result.name()).isEqualTo("Java");
		assertThat(result.aliases()).containsExactly("JVM");
		verify(this.skillRepository, never()).saveAndFlush(any(Skill.class));
	}

	@Test
	void recoversConcurrentDuplicateInsertByReturningTheCanonicalSkill() {
		var existing = new Skill("Java", new String[] { "JVM" });
		when(this.skillRepository.findFirstByNameIgnoreCase("Java"))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(existing));
		when(this.skillRepository.saveAndFlush(any(Skill.class)))
			.thenThrow(new DataIntegrityViolationException("case-insensitive unique index"));

		var result = this.skillService.create(new CreateSkillCommand(" Java ", List.of("HotSpot")));

		assertThat(result.name()).isEqualTo("Java");
		assertThat(result.aliases()).containsExactly("JVM");
		verify(this.skillRepository).saveAndFlush(any(Skill.class));
	}

}
