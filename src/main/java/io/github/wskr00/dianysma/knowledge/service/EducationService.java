package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.education.EducationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.education.EducationDetails;
import io.github.wskr00.dianysma.knowledge.domain.Education;
import io.github.wskr00.dianysma.knowledge.repository.EducationRepository;
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
public class EducationService {

	private static final int DEFAULT_LIMIT = 20;
	private final EducationRepository educationRepository;
	private final KnowledgeVectorIndexer knowledgeVectorIndexer;

	@PreAuthorize("hasAuthority('KNOWLEDGE_READ')")
	@Transactional(readOnly = true)
	public EducationDetails get(@NotNull UUID educationId) {
		return this.educationRepository.findById(educationId)
				.map(EducationService::toDetails)
				.orElseThrow(() -> new KnowledgeNotFoundException("Formação não encontrada."));
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_READ')")
	@Transactional(readOnly = true)
	public List<EducationDetails> list(@NotNull @Valid ListKnowledgeRecordsCommand command) {
		return this.educationRepository.findAllByOrderByUpdatedAtDesc(toLimit(command.limit()))
				.stream()
				.map(EducationService::toDetails)
				.toList();
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public EducationDetails create(@NotNull @Valid EducationCommand command) {
		Education education = new Education(command.institution().strip(), command.type(), command.title().strip(),
				stripToNull(command.degree()), command.description(), command.workloadHours(), command.startedAt(),
				command.completedAt(), command.expected());
		education = this.educationRepository.saveAndFlush(education);
		this.knowledgeVectorIndexer.index(education);
		return toDetails(education);
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public EducationDetails update(@NotNull UUID educationId, @NotNull @Valid EducationCommand command) {
		Education education = this.educationRepository.findById(educationId)
				.orElseThrow(() -> new KnowledgeNotFoundException("Formação não encontrada."));
		education.setInstitution(command.institution().strip());
		education.setType(command.type());
		education.setTitle(command.title().strip());
		education.setDegree(stripToNull(command.degree()));
		education.setDescription(command.description());
		education.setWorkloadHours(command.workloadHours());
		education.setStartedAt(command.startedAt());
		education.setCompletedAt(command.completedAt());
		education.setExpected(command.expected());
		education = this.educationRepository.saveAndFlush(education);
		this.knowledgeVectorIndexer.index(education);
		return toDetails(education);
	}

	@PreAuthorize("hasAuthority('KNOWLEDGE_WRITE')")
	@Transactional
	public DeleteResult delete(@NotNull UUID educationId) {
		Education education = this.educationRepository.findById(educationId).orElse(null);
		if (education == null) {
			return new DeleteResult(educationId, false);
		}
		this.knowledgeVectorIndexer.delete(educationId);
		this.educationRepository.delete(education);
		return new DeleteResult(educationId, true);
	}

	private static String stripToNull(String value) {
		if (value == null) {
			return null;
		}
		String stripped = value.strip();
		return stripped.isEmpty() ? null : stripped;
	}

	private static Limit toLimit(Integer limit) {
		return Limit.of(limit == null ? DEFAULT_LIMIT : limit);
	}

	private static EducationDetails toDetails(Education education) {
		return new EducationDetails(education.getId(), education.getInstitution(), education.getType(),
				education.getTitle(), education.getDegree(), education.getDescription(), education.getWorkloadHours(),
				education.getStartedAt(), education.getCompletedAt(), education.getExpected(), education.getCreatedAt(),
				education.getUpdatedAt());
	}
}
