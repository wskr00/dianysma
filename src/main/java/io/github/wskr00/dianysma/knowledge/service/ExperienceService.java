package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.experience.ExperienceCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.experience.ExperienceDetails;
import io.github.wskr00.dianysma.knowledge.domain.Experience;
import io.github.wskr00.dianysma.knowledge.domain.Organization;
import io.github.wskr00.dianysma.knowledge.repository.ExperienceRepository;
import io.github.wskr00.dianysma.knowledge.repository.OrganizationRepository;
import io.github.wskr00.dianysma.knowledge.repository.ParticipationRepository;
import io.github.wskr00.dianysma.knowledge.service.exception.InvalidKnowledgeInputException;
import io.github.wskr00.dianysma.knowledge.service.exception.KnowledgeNotFoundException;
import io.github.wskr00.dianysma.knowledge.service.vector.KnowledgeVectorIndexer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class ExperienceService {

	private static final int DEFAULT_LIMIT = 20;
	private final ExperienceRepository experienceRepository;
	private final OrganizationRepository organizationRepository;
	private final ParticipationRepository participationRepository;
	private final KnowledgeVectorIndexer knowledgeVectorIndexer;

	@Transactional(readOnly = true)
	public ExperienceDetails get(@NotNull UUID experienceId) {
		return this.experienceRepository.findDetailedById(experienceId)
				.map(ExperienceService::toDetails)
				.orElseThrow(() -> new KnowledgeNotFoundException("Experiência não encontrada."));
	}

	@Transactional(readOnly = true)
	public List<ExperienceDetails> list(@NotNull @Valid ListKnowledgeRecordsCommand command) {
		return this.experienceRepository.findAllByOrderByUpdatedAtDesc(toLimit(command.limit()))
				.stream()
				.map(ExperienceService::toDetails)
				.toList();
	}

	@Transactional
	public ExperienceDetails create(@NotNull @Valid ExperienceCommand command) {
		Experience experience = new Experience(resolveOrganization(command.employerId(), "empregadora"),
				resolveOptionalOrganization(command.clientId(), "cliente"), command.role().strip(),
				command.employmentType().strip(), command.description(), command.startedAt(), command.endedAt());
		return toDetails(this.experienceRepository.saveAndFlush(experience));
	}

	@Transactional
	public ExperienceDetails update(@NotNull UUID experienceId, @NotNull @Valid ExperienceCommand command) {
		Experience experience = this.experienceRepository.findDetailedById(experienceId)
				.orElseThrow(() -> new KnowledgeNotFoundException("Experiência não encontrada."));
		experience.setEmployer(resolveOrganization(command.employerId(), "empregadora"));
		experience.setClient(resolveOptionalOrganization(command.clientId(), "cliente"));
		experience.setRole(command.role().strip());
		experience.setEmploymentType(command.employmentType().strip());
		experience.setDescription(command.description());
		experience.setStartedAt(command.startedAt());
		experience.setEndedAt(command.endedAt());
		experience = this.experienceRepository.saveAndFlush(experience);
		this.knowledgeVectorIndexer.reindexParticipationsForExperience(experienceId);
		return toDetails(experience);
	}

	@Transactional
	public DeleteResult delete(@NotNull UUID experienceId) {
		Experience experience = this.experienceRepository.findById(experienceId).orElse(null);
		if (experience == null) {
			return new DeleteResult(experienceId, false);
		}
		if (this.participationRepository.existsByExperience_Id(experienceId)) {
			throw new InvalidKnowledgeInputException(
					"A experiência possui participações. Exclua ou atualize essas participações antes de excluí-la.");
		}
		this.experienceRepository.delete(experience);
		return new DeleteResult(experienceId, true);
	}

	private Organization resolveOrganization(UUID organizationId, String relationship) {
		return this.organizationRepository.findById(organizationId)
				.orElseThrow(() -> new KnowledgeNotFoundException("A organização informada como " + relationship + " não existe."));
	}

	private Organization resolveOptionalOrganization(UUID organizationId, String relationship) {
		return organizationId == null ? null : resolveOrganization(organizationId, relationship);
	}

	private static Limit toLimit(Integer limit) {
		return Limit.of(limit == null ? DEFAULT_LIMIT : limit);
	}

	private static ExperienceDetails toDetails(Experience experience) {
		return new ExperienceDetails(experience.getId(), experience.getEmployer().getId(),
				experience.getClient() == null ? null : experience.getClient().getId(), experience.getRole(),
				experience.getEmploymentType(), experience.getDescription(), experience.getStartedAt(),
				experience.getEndedAt(), experience.getCreatedAt(), experience.getUpdatedAt());
	}
}
