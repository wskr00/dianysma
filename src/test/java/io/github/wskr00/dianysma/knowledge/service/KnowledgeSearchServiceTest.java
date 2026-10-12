package io.github.wskr00.dianysma.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.knowledge.SearchKnowledgeCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.knowledge.KnowledgeSearchHit;
import io.github.wskr00.dianysma.knowledge.service.vector.KnowledgeVectorStore;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KnowledgeSearchServiceTest {

	@Mock
	private KnowledgeVectorStore knowledgeVectorStore;

	@InjectMocks
	private KnowledgeSearchService knowledgeSearchService;

	@Test
	void trimsQueryAndUsesDefaultResultLimit() {
		var hit = new KnowledgeSearchHit(UUID.randomUUID(), "claim", "Experiência com Java", 0.91);
		when(this.knowledgeVectorStore.search("experiência com Java", 5)).thenReturn(List.of(hit));

		var results = this.knowledgeSearchService.search(new SearchKnowledgeCommand("  experiência com Java  ", null));

		assertThat(results).containsExactly(hit);
		verify(this.knowledgeVectorStore).search("experiência com Java", 5);
	}

	@Test
	void respectsAnExplicitResultLimitAndAllowsNoMatches() {
		when(this.knowledgeVectorStore.search("Spring", 3)).thenReturn(List.of());

		var results = this.knowledgeSearchService.search(new SearchKnowledgeCommand("Spring", 3));

		assertThat(results).isEmpty();
		verify(this.knowledgeVectorStore).search("Spring", 3);
	}

}
