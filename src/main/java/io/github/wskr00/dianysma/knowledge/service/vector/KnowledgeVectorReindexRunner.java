package io.github.wskr00.dianysma.knowledge.service.vector;

import io.github.wskr00.dianysma.knowledge.repository.ClaimRepository;
import io.github.wskr00.dianysma.knowledge.repository.EducationRepository;
import io.github.wskr00.dianysma.knowledge.repository.KnowledgeChunkRepository;
import io.github.wskr00.dianysma.knowledge.repository.ParticipationRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeVectorReindexRunner implements ApplicationRunner {

	private final ClaimRepository claimRepository;
	private final EducationRepository educationRepository;
	private final KnowledgeChunkRepository knowledgeChunkRepository;
	private final ParticipationRepository participationRepository;
	private final KnowledgeVectorIndexer knowledgeVectorIndexer;
	private final GoogleGenAiTextEmbeddingProperties embeddingProperties;

	@Override
	public void run(ApplicationArguments args) {
		String model = Objects.requireNonNull(this.embeddingProperties.getModel(),
				"O modelo de embedding precisa estar configurado.");
		if (!this.knowledgeChunkRepository.existsByModelNot(model)) {
			return;
		}

		log.info("O modelo de embeddings mudou; reindexando o conhecimento com o modelo '{}'.", model);
		this.claimRepository.findAllIdsForVectorReindexing().forEach(this.knowledgeVectorIndexer::reindexClaim);
		this.educationRepository.findAllIdsForVectorReindexing().forEach(this.knowledgeVectorIndexer::reindexEducation);
		this.participationRepository.findAllIdsForVectorReindexing()
				.forEach(this.knowledgeVectorIndexer::reindexParticipation);
		log.info("A reindexação do conhecimento foi concluída.");
	}
}
