package io.github.wskr00.dianysma.knowledge.service.vector;

import io.github.wskr00.dianysma.knowledge.domain.Claim;
import io.github.wskr00.dianysma.knowledge.domain.Education;
import io.github.wskr00.dianysma.knowledge.domain.KnowledgeChunk;
import io.github.wskr00.dianysma.knowledge.domain.Participation;
import io.github.wskr00.dianysma.knowledge.domain.Skill;
import io.github.wskr00.dianysma.knowledge.repository.ClaimRepository;
import io.github.wskr00.dianysma.knowledge.repository.EducationRepository;
import io.github.wskr00.dianysma.knowledge.repository.KnowledgeChunkRepository;
import io.github.wskr00.dianysma.knowledge.repository.ParticipationRepository;
import io.github.wskr00.dianysma.knowledge.service.exception.InvalidKnowledgeInputException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class KnowledgeVectorIndexer {

	private static final String CLAIM_SOURCE_TYPE = "claim";
	private static final String PARTICIPATION_SOURCE_TYPE = "participation";
	private static final String EDUCATION_SOURCE_TYPE = "education";
	private static final int MAX_INDEXED_TEXT_CODE_POINTS = 6_000;

	private final KnowledgeVectorStore knowledgeVectorStore;
	private final ClaimRepository claimRepository;
	private final EducationRepository educationRepository;
	private final KnowledgeChunkRepository knowledgeChunkRepository;
	private final ParticipationRepository participationRepository;
	private final GoogleGenAiTextEmbeddingProperties embeddingProperties;

	@Transactional
	public void index(Claim claim) {
		UUID claimId = claim.getId();
		String indexedText = toIndexedText(claim);
		index(claimId, CLAIM_SOURCE_TYPE, indexedText,
				(hash, model) -> KnowledgeChunk.forClaim(claimId, claim, hash, model));
	}

	@Transactional
	public void index(Participation participation) {
		String description = participation.getDescription();
		if (description == null || description.isBlank()) {
			delete(participation.getId());
			return;
		}
		UUID participationId = participation.getId();
		String indexedText = toIndexedText(participation);
		index(participationId, PARTICIPATION_SOURCE_TYPE, indexedText,
				(hash, model) -> KnowledgeChunk.forParticipation(participationId, participation, hash, model));
	}

	@Transactional
	public void index(Education education) {
		UUID educationId = education.getId();
		String indexedText = toIndexedText(education);
		index(educationId, EDUCATION_SOURCE_TYPE, indexedText,
				(hash, model) -> KnowledgeChunk.forEducation(educationId, education, hash, model));
	}

	@Transactional
	public void delete(UUID sourceId) {
		this.knowledgeVectorStore.delete(sourceId);
	}

	@Transactional
	public void reindexClaim(UUID claimId) {
		this.claimRepository.findDetailedById(claimId).ifPresent(this::index);
	}

	@Transactional
	public void reindexEducation(UUID educationId) {
		this.educationRepository.findById(educationId).ifPresent(this::index);
	}

	@Transactional
	public void reindexParticipation(UUID participationId) {
		this.participationRepository.findDetailedById(participationId).ifPresent(this::index);
	}

	@Transactional
	public void reindexParticipationsForExperience(UUID experienceId) {
		this.participationRepository.findAllDetailedByExperience_Id(experienceId).forEach(this::index);
	}

	@Transactional
	public void reindexParticipationsForProject(UUID projectId) {
		this.participationRepository.findAllDetailedByProject_Id(projectId).forEach(this::index);
	}

	@Transactional
	public void reindexParticipationsForOrganization(UUID organizationId) {
		this.participationRepository.findAllDetailedByOrganizationId(organizationId).forEach(this::index);
	}

	private void index(UUID sourceId, String sourceType, String indexedText,
			BiFunction<String, String, KnowledgeChunk> chunkFactory) {
		if (indexedText.codePointCount(0, indexedText.length()) > MAX_INDEXED_TEXT_CODE_POINTS) {
			throw new InvalidKnowledgeInputException(
					"O texto combinado ultrapassa o limite seguro para indexação semântica. Reduza o conteúdo ou a quantidade de competências associadas.");
		}
		String embeddingModel = currentModel();
		String hash = hash(embeddingModel, indexedText);
		Optional<KnowledgeChunk> existingChunk = this.knowledgeChunkRepository.findById(sourceId);
		if (existingChunk.filter(chunk -> hash.equals(chunk.getHash())).isPresent()) {
			return;
		}

		Document document = Document.builder()
			.id(sourceId.toString())
			.text(indexedText)
			.metadata(Map.of("sourceType", sourceType, "sourceId", sourceId.toString()))
			.build();
		this.knowledgeVectorStore.add(document);

		KnowledgeChunk chunk;
		if (existingChunk.isPresent()) {
			chunk = existingChunk.orElseThrow();
			chunk.updateIndex(hash, embeddingModel);
		} else {
			chunk = chunkFactory.apply(hash, embeddingModel);
		}
		this.knowledgeChunkRepository.saveAndFlush(chunk);
	}

	private static String toIndexedText(Claim claim) {
		String skillNames = claim.getSkills().stream()
			.map(Skill::getName)
			.sorted(String.CASE_INSENSITIVE_ORDER)
			.collect(Collectors.joining(", "));
		String title = skillNames.isBlank() ? "none" : skillNames;
		return "title: %s | text: %s".formatted(title, claim.getContent());
	}

	private static String toIndexedText(Education education) {
		return "title: %s | text: institution: %s | type: %s | degree: %s | description: %s"
				.formatted(education.getTitle(), education.getInstitution(), education.getType(),
						valueOrNone(education.getDegree()), valueOrNone(education.getDescription()));
	}

	private static String toIndexedText(Participation participation) {
		String experience = "none";
		if (participation.getExperience() != null) {
			var role = participation.getExperience().getRole();
			var employer = participation.getExperience().getEmployer().getName();
			experience = "%s at %s".formatted(role, employer);
		}

		String project = "none";
		if (participation.getProject() != null) {
			var projectName = participation.getProject().getName();
			var organization = participation.getProject().getOrganization();
			project = organization == null
					? projectName
					: "%s at %s".formatted(projectName, organization.getName());
		}

		return "title: %s | project: %s | text: %s"
				.formatted(experience, project, participation.getDescription().strip());
	}

	private String currentModel() {
		return Objects.requireNonNull(this.embeddingProperties.getModel(),
				"O modelo de embedding precisa estar configurado.");
	}

	private static String valueOrNone(String value) {
		return value == null || value.isBlank() ? "none" : value.strip();
	}

	private static String hash(String model, String indexedText) {
		try {
			byte[] bytes = MessageDigest.getInstance("SHA-256")
					.digest((model + "\n" + indexedText).getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(bytes);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 está indisponível.", exception);
		}
	}
}
