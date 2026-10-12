package io.github.wskr00.dianysma.knowledge.service.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.wskr00.dianysma.knowledge.domain.Claim;
import io.github.wskr00.dianysma.knowledge.domain.KnowledgeChunk;
import io.github.wskr00.dianysma.knowledge.domain.Skill;
import io.github.wskr00.dianysma.knowledge.repository.ClaimRepository;
import io.github.wskr00.dianysma.knowledge.repository.EducationRepository;
import io.github.wskr00.dianysma.knowledge.repository.KnowledgeChunkRepository;
import io.github.wskr00.dianysma.knowledge.repository.ParticipationRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingProperties;

@ExtendWith(MockitoExtension.class)
class KnowledgeVectorIndexerTest {

	@Mock
	private KnowledgeVectorStore knowledgeVectorStore;

	@Mock
	private ClaimRepository claimRepository;

	@Mock
	private EducationRepository educationRepository;

	@Mock
	private KnowledgeChunkRepository knowledgeChunkRepository;

	@Mock
	private ParticipationRepository participationRepository;

	@Mock
	private GoogleGenAiTextEmbeddingProperties embeddingProperties;

	@InjectMocks
	private KnowledgeVectorIndexer indexer;

	@Test
	void indexesClaimAndPersistsItsModelAndContentHashWithoutCallingAnEmbeddingProvider() {
		UUID claimId = UUID.randomUUID();
		Claim claim = claim("Built a Java service", claimId);
		when(this.embeddingProperties.getModel()).thenReturn("gemini-embedding-2");
		when(this.knowledgeChunkRepository.findById(claimId)).thenReturn(Optional.empty());
		when(this.knowledgeChunkRepository.saveAndFlush(any(KnowledgeChunk.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		this.indexer.index(claim);

		var documentCaptor = ArgumentCaptor.forClass(Document.class);
		verify(this.knowledgeVectorStore).add(documentCaptor.capture());
		assertThat(documentCaptor.getValue().getText()).isEqualTo("title: Java | text: Built a Java service");
		assertThat(documentCaptor.getValue().getMetadata())
			.containsEntry("sourceType", "claim")
			.containsEntry("sourceId", claimId.toString());

		var chunkCaptor = ArgumentCaptor.forClass(KnowledgeChunk.class);
		verify(this.knowledgeChunkRepository).saveAndFlush(chunkCaptor.capture());
		assertThat(chunkCaptor.getValue().getId()).isEqualTo(claimId);
		assertThat(chunkCaptor.getValue().getModel()).isEqualTo("gemini-embedding-2");
		assertThat(chunkCaptor.getValue().getHash()).hasSize(64);
	}

	@Test
	void skipsVectorStoreWriteWhenContentAndModelHaveNotChanged() {
		UUID claimId = UUID.randomUUID();
		Claim claim = claim("Built a Java service", claimId);
		when(this.embeddingProperties.getModel()).thenReturn("gemini-embedding-2");
		when(this.knowledgeChunkRepository.findById(claimId)).thenReturn(Optional.empty());
		when(this.knowledgeChunkRepository.saveAndFlush(any(KnowledgeChunk.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));

		this.indexer.index(claim);
		var chunkCaptor = ArgumentCaptor.forClass(KnowledgeChunk.class);
		verify(this.knowledgeChunkRepository).saveAndFlush(chunkCaptor.capture());
		KnowledgeChunk indexedChunk = chunkCaptor.getValue();

		clearInvocations(this.knowledgeVectorStore, this.knowledgeChunkRepository);
		doReturn(Optional.of(indexedChunk)).when(this.knowledgeChunkRepository).findById(claimId);

		this.indexer.index(claim);

		verify(this.knowledgeVectorStore, never()).add(any(Document.class));
		verify(this.knowledgeChunkRepository, never()).saveAndFlush(any(KnowledgeChunk.class));
	}

	private static Claim claim(String content, UUID id) {
		Claim claim = org.mockito.Mockito.mock(Claim.class);
		when(claim.getId()).thenReturn(id);
		when(claim.getContent()).thenReturn(content);
		when(claim.getSkills()).thenReturn(Set.of(new Skill("Java", new String[0])));
		return claim;
	}

}
