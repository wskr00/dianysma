package io.github.wskr00.dianysma.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.profile.ProfileCommand;
import io.github.wskr00.dianysma.knowledge.domain.Profile;
import io.github.wskr00.dianysma.knowledge.repository.ProfileRepository;
import io.github.wskr00.dianysma.knowledge.service.exception.InvalidKnowledgeInputException;
import io.github.wskr00.dianysma.knowledge.service.exception.KnowledgeNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

	@Mock
	private ProfileRepository profileRepository;

	@InjectMocks
	private ProfileService profileService;

	@Test
	void createsProfileWithOnlyRequiredPersonalData() {
		when(this.profileRepository.count()).thenReturn(0L);
		when(this.profileRepository.saveAndFlush(any(Profile.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		var result = this.profileService.create(new ProfileCommand("Lucas", null, null, null, null, null, null, null, null));

		assertThat(result.name()).isEqualTo("Lucas");
		assertThat(result.email()).isNull();
		assertThat(result.city()).isNull();
		verify(this.profileRepository).saveAndFlush(any(Profile.class));
	}

	@Test
	void rejectsSecondProfileBeforePersistingIt() {
		when(this.profileRepository.count()).thenReturn(1L);

		assertThatThrownBy(() -> this.profileService.create(
				new ProfileCommand("Outra pessoa", null, null, null, null, null, null, null, null)))
			.isInstanceOf(InvalidKnowledgeInputException.class)
			.hasMessageContaining("Já existe um perfil");

		verify(this.profileRepository, never()).saveAndFlush(any(Profile.class));
	}

	@Test
	void translatesConcurrentSingletonConstraintViolationToDomainError() {
		when(this.profileRepository.count()).thenReturn(0L);
		when(this.profileRepository.saveAndFlush(any(Profile.class)))
			.thenThrow(new DataIntegrityViolationException("unique profile index"));

		assertThatThrownBy(() -> this.profileService.create(
				new ProfileCommand("Lucas", null, null, null, null, null, null, null, null)))
			.isInstanceOf(InvalidKnowledgeInputException.class)
			.hasMessageContaining("Já existe um perfil");
	}

	@Test
	void updateReplacesAllFieldsAndClearsOmittedOptionalValues() {
		var existing = new Profile("Lucas", "lucas@example.com", "+5511999999999", "Belém", "PA", "Brasil",
				"https://linkedin.example/lucas", "https://github.example/lucas", "https://portfolio.example");
		when(this.profileRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(existing));
		when(this.profileRepository.saveAndFlush(existing)).thenReturn(existing);

		var result = this.profileService.update(
				new ProfileCommand("Lucas", null, null, "Ananindeua", null, "Brasil", null, null, null));

		assertThat(result.city()).isEqualTo("Ananindeua");
		assertThat(result.email()).isNull();
		assertThat(result.phone()).isNull();
		assertThat(result.linkedinUrl()).isNull();
		assertThat(existing.getGithubUrl()).isNull();
	}

	@Test
	void getReportsWhenProfileHasNotBeenCreated() {
		when(this.profileRepository.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());

		assertThatThrownBy(this.profileService::get)
			.isInstanceOf(KnowledgeNotFoundException.class)
			.hasMessageContaining("ainda não foi criado");
	}

}
