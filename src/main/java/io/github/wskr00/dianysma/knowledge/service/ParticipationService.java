package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.participation.ParticipationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.participation.ParticipationDetails;
import io.github.wskr00.dianysma.knowledge.domain.Experience;
import io.github.wskr00.dianysma.knowledge.domain.Participation;
import io.github.wskr00.dianysma.knowledge.domain.Project;
import io.github.wskr00.dianysma.knowledge.repository.ClaimRepository;
import io.github.wskr00.dianysma.knowledge.repository.ExperienceRepository;
import io.github.wskr00.dianysma.knowledge.repository.ParticipationRepository;
import io.github.wskr00.dianysma.knowledge.repository.ProjectRepository;
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
public class ParticipationService {

	private static final int DEFAULT_LIMIT = 20;
	private final ParticipationRepository participationRepository;
	private final ExperienceRepository experienceRepository;
	private final ProjectRepository projectRepository;
	private final ClaimRepository claimRepository;
	private final KnowledgeVectorIndexer knowledgeVectorIndexer;

	@Transactional(readOnly = true)
	public ParticipationDetails get(@NotNull UUID participationId) {
		return this.participationRepository.findDetailedById(participationId)
				.map(ParticipationService::toDetails)
				.orElseThrow(() -> new KnowledgeNotFoundException("Participação não encontrada."));
	}

	@Transactional(readOnly = true)
	public List<ParticipationDetails> list(@NotNull @Valid ListKnowledgeRecordsCommand command) {
		return this.participationRepository.findAllByOrderByUpdatedAtDesc(toLimit(command.limit()))
				.stream()
				.map(ParticipationService::toDetails)
				.toList();
	}

	@Transactional
	public ParticipationDetails create(@NotNull @Valid ParticipationCommand command) {
		validateContext(command);
		if (this.participationRepository.existsByExperience_IdAndProject_Id(command.experienceId(), command.projectId())) {
			throw duplicateContext();
		}
		Participation participation = new Participation(resolveExperience(command.experienceId()),
				resolveProject(command.projectId()), command.description());
		participation = this.participationRepository.saveAndFlush(participation);
		this.knowledgeVectorIndexer.index(participation);
		return toDetails(participation);
	}

	@Transactional
	public ParticipationDetails update(@NotNull UUID participationId, @NotNull @Valid ParticipationCommand command) {
		validateContext(command);
		Participation participation = this.participationRepository.findDetailedById(participationId)
				.orElseThrow(() -> new KnowledgeNotFoundException("Participação não encontrada."));
		if (this.participationRepository.existsByExperience_IdAndProject_IdAndIdNot(
				command.experienceId(), command.projectId(), participationId)) {
			throw duplicateContext();
		}
		participation.setExperience(resolveExperience(command.experienceId()));
		participation.setProject(resolveProject(command.projectId()));
		participation.setDescription(command.description());
		participation = this.participationRepository.saveAndFlush(participation);
		this.knowledgeVectorIndexer.index(participation);
		return toDetails(participation);
	}

	@Transactional
	public DeleteResult delete(@NotNull UUID participationId) {
		Participation participation = this.participationRepository.findById(participationId).orElse(null);
		if (participation == null) {
			return new DeleteResult(participationId, false);
		}
		if (this.claimRepository.existsByParticipation_Id(participationId)) {
			throw new InvalidKnowledgeInputException(
					"A participação possui fatos profissionais vinculados. Desassocie-os ou atualize-os antes de excluí-la.");
		}
		this.knowledgeVectorIndexer.delete(participationId);
		this.participationRepository.delete(participation);
		return new DeleteResult(participationId, true);
	}

	private static void validateContext(ParticipationCommand command) {
		if (command.experienceId() == null && command.projectId() == null) {
			throw new InvalidKnowledgeInputException("Informe uma experiência, um projeto ou ambos para contextualizar a participação.");
		}
	}

	private Experience resolveExperience(UUID experienceId) {
		if (experienceId == null) {
			return null;
		}
		return this.experienceRepository.findById(experienceId)
				.orElseThrow(() -> new KnowledgeNotFoundException("A experiência informada para a participação não existe."));
	}

	private Project resolveProject(UUID projectId) {
		if (projectId == null) {
			return null;
		}
		return this.projectRepository.findById(projectId)
				.orElseThrow(() -> new KnowledgeNotFoundException("O projeto informado para a participação não existe."));
	}

	private static InvalidKnowledgeInputException duplicateContext() {
		return new InvalidKnowledgeInputException("Já existe uma participação com esta combinação de experiência e projeto.");
	}

	private static Limit toLimit(Integer limit) {
		return Limit.of(limit == null ? DEFAULT_LIMIT : limit);
	}

	private static ParticipationDetails toDetails(Participation participation) {
		return new ParticipationDetails(participation.getId(),
				participation.getExperience() == null ? null : participation.getExperience().getId(),
				participation.getProject() == null ? null : participation.getProject().getId(),
				participation.getDescription(), participation.getCreatedAt(), participation.getUpdatedAt());
	}
}
