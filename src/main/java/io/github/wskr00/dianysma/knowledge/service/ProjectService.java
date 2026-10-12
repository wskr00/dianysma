package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.project.ProjectCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.project.ProjectDetails;
import io.github.wskr00.dianysma.knowledge.domain.Organization;
import io.github.wskr00.dianysma.knowledge.domain.Project;
import io.github.wskr00.dianysma.knowledge.repository.OrganizationRepository;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class ProjectService {

	private static final int DEFAULT_LIMIT = 20;
	private final ProjectRepository projectRepository;
	private final OrganizationRepository organizationRepository;
	private final ParticipationRepository participationRepository;
	private final KnowledgeVectorIndexer knowledgeVectorIndexer;

	@PreAuthorize("hasAuthority('KNOWLEDGE_READ')")
	@Transactional(readOnly = true)
	public ProjectDetails get(@NotNull UUID projectId) {
		return this.projectRepository.findDetailedById(projectId)
				.map(ProjectService::toDetails)
				.orElseThrow(() -> new KnowledgeNotFoundException("Projeto não encontrado."));
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_READ')")
	@Transactional(readOnly = true)
	public List<ProjectDetails> list(@NotNull @Valid ListKnowledgeRecordsCommand command) {
		return this.projectRepository.findAllByOrderByUpdatedAtDesc(toLimit(command.limit()))
				.stream()
				.map(ProjectService::toDetails)
				.toList();
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public ProjectDetails create(@NotNull @Valid ProjectCommand command) {
		Project project = new Project(resolveOrganization(command.organizationId()), command.name().strip(),
				command.description());
		return toDetails(this.projectRepository.saveAndFlush(project));
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public ProjectDetails update(@NotNull UUID projectId, @NotNull @Valid ProjectCommand command) {
		Project project = this.projectRepository.findDetailedById(projectId)
				.orElseThrow(() -> new KnowledgeNotFoundException("Projeto não encontrado."));
		project.setOrganization(resolveOrganization(command.organizationId()));
		project.setName(command.name().strip());
		project.setDescription(command.description());
		project = this.projectRepository.saveAndFlush(project);
		this.knowledgeVectorIndexer.reindexParticipationsForProject(projectId);
		return toDetails(project);
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public DeleteResult delete(@NotNull UUID projectId) {
		Project project = this.projectRepository.findById(projectId).orElse(null);
		if (project == null) {
			return new DeleteResult(projectId, false);
		}
		if (this.participationRepository.existsByProject_Id(projectId)) {
			throw new InvalidKnowledgeInputException(
					"O projeto possui participações. Exclua ou atualize essas participações antes de excluí-lo.");
		}
		this.projectRepository.delete(project);
		return new DeleteResult(projectId, true);
	}

	private Organization resolveOrganization(UUID organizationId) {
		if (organizationId == null) {
			return null;
		}
		return this.organizationRepository.findById(organizationId)
				.orElseThrow(() -> new KnowledgeNotFoundException("A organização do projeto não existe."));
	}

	private static Limit toLimit(Integer limit) {
		return Limit.of(limit == null ? DEFAULT_LIMIT : limit);
	}

	private static ProjectDetails toDetails(Project project) {
		return new ProjectDetails(project.getId(), project.getOrganization() == null ? null : project.getOrganization().getId(),
				project.getName(), project.getDescription(), project.getCreatedAt(), project.getUpdatedAt());
	}
}
