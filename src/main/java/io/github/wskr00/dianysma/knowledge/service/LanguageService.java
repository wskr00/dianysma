package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.language.LanguageCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.language.LanguageDetails;
import io.github.wskr00.dianysma.knowledge.domain.Language;
import io.github.wskr00.dianysma.knowledge.domain.Profile;
import io.github.wskr00.dianysma.knowledge.repository.LanguageRepository;
import io.github.wskr00.dianysma.knowledge.repository.ProfileRepository;
import io.github.wskr00.dianysma.knowledge.service.exception.KnowledgeNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class LanguageService {

	private static final int DEFAULT_LIMIT = 20;
	private final LanguageRepository languageRepository;
	private final ProfileRepository profileRepository;

	@PreAuthorize("hasAuthority('KNOWLEDGE_READ')")
	@Transactional(readOnly = true)
	public LanguageDetails get(@NotNull UUID languageId) {
		return this.languageRepository.findById(languageId)
				.map(LanguageService::toDetails)
				.orElseThrow(() -> new KnowledgeNotFoundException("Idioma não encontrado."));
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_READ')")
	@Transactional(readOnly = true)
	public List<LanguageDetails> list(@NotNull @Valid ListKnowledgeRecordsCommand command) {
		return this.profileRepository.findFirstByOrderByCreatedAtAsc()
				.map(profile -> this.languageRepository.findAllByProfile_IdOrderByName(profile.getId(), toLimit(command.limit())))
				.orElseGet(List::of)
				.stream()
				.map(LanguageService::toDetails)
				.toList();
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public LanguageDetails create(@NotNull @Valid LanguageCommand command) {
		Profile profile = this.profileRepository.findFirstByOrderByCreatedAtAsc()
				.orElseThrow(() -> new KnowledgeNotFoundException("Crie o perfil antes de registrar idiomas."));
		Language language = new Language(profile, command.name().strip(), command.proficiency());
		return toDetails(this.languageRepository.saveAndFlush(language));
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public LanguageDetails update(@NotNull UUID languageId, @NotNull @Valid LanguageCommand command) {
		Language language = this.languageRepository.findById(languageId)
				.orElseThrow(() -> new KnowledgeNotFoundException("Idioma não encontrado."));
		language.setName(command.name().strip());
		language.setProficiency(command.proficiency());
		return toDetails(this.languageRepository.saveAndFlush(language));
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public DeleteResult delete(@NotNull UUID languageId) {
		Language language = this.languageRepository.findById(languageId).orElse(null);
		if (language == null) {
			return new DeleteResult(languageId, false);
		}
		this.languageRepository.delete(language);
		return new DeleteResult(languageId, true);
	}

	private static Limit toLimit(Integer limit) {
		return Limit.of(limit == null ? DEFAULT_LIMIT : limit);
	}

	private static LanguageDetails toDetails(Language language) {
		return new LanguageDetails(language.getId(), language.getName(), language.getProficiency(),
				language.getCreatedAt(), language.getUpdatedAt());
	}
}
