package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.profile.ProfileCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.profile.ProfileDetails;
import io.github.wskr00.dianysma.knowledge.domain.Profile;
import io.github.wskr00.dianysma.knowledge.repository.ProfileRepository;
import io.github.wskr00.dianysma.knowledge.service.exception.InvalidKnowledgeInputException;
import io.github.wskr00.dianysma.knowledge.service.exception.KnowledgeNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class ProfileService {

	private static final String READ_SCOPE = "hasAuthority('SCOPE_dianysma/knowledge.read')";
	private static final String WRITE_SCOPE = "hasAuthority('SCOPE_dianysma/knowledge.write')";
	private final ProfileRepository profileRepository;

	@PreAuthorize(READ_SCOPE)
	@Transactional(readOnly = true)
	public ProfileDetails get() {
		return this.profileRepository.findFirstByOrderByCreatedAtAsc()
				.map(ProfileService::toDetails)
				.orElseThrow(() -> new KnowledgeNotFoundException("O perfil ainda não foi criado."));
	}

	@PreAuthorize(WRITE_SCOPE)
	@Transactional
	public ProfileDetails create(@NotNull @Valid ProfileCommand command) {
		if (this.profileRepository.count() > 0) {
			throw new InvalidKnowledgeInputException("Já existe um perfil nesta instalação. Use update_profile para alterá-lo.");
		}

		Profile profile = fromCommand(command);
		try {
			return toDetails(this.profileRepository.saveAndFlush(profile));
		}
		catch (DataIntegrityViolationException exception) {
			throw new InvalidKnowledgeInputException("Já existe um perfil nesta instalação. Use update_profile para alterá-lo.");
		}
	}

	@PreAuthorize(WRITE_SCOPE)
	@Transactional
	public ProfileDetails update(@NotNull @Valid ProfileCommand command) {
		Profile profile = this.profileRepository.findFirstByOrderByCreatedAtAsc()
				.orElseThrow(() -> new KnowledgeNotFoundException("O perfil ainda não foi criado; use create_profile primeiro."));
		apply(profile, command);
		return toDetails(this.profileRepository.saveAndFlush(profile));
	}

	private static Profile fromCommand(ProfileCommand command) {
		return new Profile(command.name(), command.email(), command.phone(), command.city(), command.state(),
				command.country(), command.linkedinUrl(), command.githubUrl(), command.portfolioUrl());
	}

	private static void apply(Profile profile, ProfileCommand command) {
		profile.setName(command.name());
		profile.setEmail(command.email());
		profile.setPhone(command.phone());
		profile.setCity(command.city());
		profile.setState(command.state());
		profile.setCountry(command.country());
		profile.setLinkedinUrl(command.linkedinUrl());
		profile.setGithubUrl(command.githubUrl());
		profile.setPortfolioUrl(command.portfolioUrl());
	}

	private static ProfileDetails toDetails(Profile profile) {
		return new ProfileDetails(profile.getId(), profile.getName(), profile.getEmail(), profile.getPhone(),
				profile.getCity(), profile.getState(), profile.getCountry(), profile.getLinkedinUrl(),
				profile.getGithubUrl(), profile.getPortfolioUrl(), profile.getCreatedAt(), profile.getUpdatedAt());
	}
}
