package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.CreateOrganizationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.SearchOrganizationsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.UpdateOrganizationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.organization.OrganizationDetails;
import io.github.wskr00.dianysma.knowledge.domain.Organization;
import io.github.wskr00.dianysma.knowledge.repository.ExperienceRepository;
import io.github.wskr00.dianysma.knowledge.repository.OrganizationRepository;
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
public class OrganizationService {

	private static final int DEFAULT_LIMIT = 10;
	private final OrganizationRepository organizationRepository;
	private final ExperienceRepository experienceRepository;
	private final ProjectRepository projectRepository;
	private final KnowledgeVectorIndexer knowledgeVectorIndexer;

	@Transactional(readOnly = true)
	public OrganizationDetails get(@NotNull UUID organizationId) {
		return this.organizationRepository.findById(organizationId)
				.map(OrganizationService::toDetails)
				.orElseThrow(() -> new KnowledgeNotFoundException("Organização não encontrada."));
	}

	@Transactional(readOnly = true)
	public List<OrganizationDetails> search(@NotNull @Valid SearchOrganizationsCommand command) {
		Limit limit = Limit.of(command.limit() == null ? DEFAULT_LIMIT : command.limit());
		return this.organizationRepository.findAllByNameContainingIgnoreCaseOrderByName(command.query().strip(), limit)
				.stream()
				.map(OrganizationService::toDetails)
				.toList();
	}

	@Transactional
	public OrganizationDetails create(@NotNull @Valid CreateOrganizationCommand command) {
		Organization organization = this.organizationRepository.saveAndFlush(new Organization(command.name().strip()));
		return toDetails(organization);
	}

	@Transactional
	public OrganizationDetails update(@NotNull @Valid UpdateOrganizationCommand command) {
		Organization organization = this.organizationRepository.findById(command.organizationId())
				.orElseThrow(() -> new KnowledgeNotFoundException("Organização não encontrada."));
		organization.setName(command.name().strip());
		organization = this.organizationRepository.saveAndFlush(organization);
		this.knowledgeVectorIndexer.reindexParticipationsForOrganization(organization.getId());
		return toDetails(organization);
	}

	@Transactional
	public DeleteResult delete(@NotNull UUID organizationId) {
		Organization organization = this.organizationRepository.findById(organizationId).orElse(null);
		if (organization == null) {
			return new DeleteResult(organizationId, false);
		}
		if (this.experienceRepository.existsByEmployer_Id(organizationId)
				|| this.experienceRepository.existsByClient_Id(organizationId)
				|| this.projectRepository.existsByOrganization_Id(organizationId)) {
			throw new InvalidKnowledgeInputException(
					"A organização está vinculada a experiências ou projetos. Remova esses vínculos antes de excluí-la.");
		}
		this.organizationRepository.delete(organization);
		return new DeleteResult(organizationId, true);
	}

	private static OrganizationDetails toDetails(Organization organization) {
		return new OrganizationDetails(organization.getId(), organization.getName(), organization.getCreatedAt(),
				organization.getUpdatedAt());
	}
}
