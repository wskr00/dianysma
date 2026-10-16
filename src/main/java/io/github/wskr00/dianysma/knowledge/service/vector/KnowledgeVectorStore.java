package io.github.wskr00.dianysma.knowledge.service.vector;

import java.util.List;
import java.util.UUID;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import io.github.wskr00.dianysma.knowledge.controller.dto.result.knowledge.KnowledgeSearchHit;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KnowledgeVectorStore {

	private static final String QUERY_PREFIX = "task: search result | query: ";
	private static final int MAX_EXCERPT_LENGTH = 500;

	private final VectorStore vectorStore;

	void add(org.springframework.ai.document.Document document) {
		this.vectorStore.add(List.of(document));
	}

	void delete(UUID sourceId) {
		this.vectorStore.delete(List.of(sourceId.toString()));
	}

	public List<KnowledgeSearchHit> search(String query, int limit) {
		return this.vectorStore.similaritySearch(SearchRequest.builder()
				.query(QUERY_PREFIX + query)
				.topK(limit)
				.build())
				.stream()
				.map(document -> new KnowledgeSearchHit(
						UUID.fromString((String) document.getMetadata().get("sourceId")),
						(String) document.getMetadata().get("sourceType"),
						excerpt(document.getText()),
						document.getScore()))
				.toList();
	}

	private static String excerpt(String indexedText) {
		if (indexedText.length() <= MAX_EXCERPT_LENGTH) {
			return indexedText;
		}
		return indexedText.substring(0, MAX_EXCERPT_LENGTH - 1).stripTrailing() + "…";
	}
}
