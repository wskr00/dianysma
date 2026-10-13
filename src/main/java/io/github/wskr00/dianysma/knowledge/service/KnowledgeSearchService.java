package io.github.wskr00.dianysma.knowledge.service;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.knowledge.SearchKnowledgeCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.knowledge.KnowledgeSearchHit;
import io.github.wskr00.dianysma.knowledge.service.vector.KnowledgeVectorStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class KnowledgeSearchService {

	private static final int DEFAULT_LIMIT = 5;
	private final KnowledgeVectorStore knowledgeVectorStore;

	public List<KnowledgeSearchHit> search(@NotNull @Valid SearchKnowledgeCommand command) {
		int limit = command.limit() == null ? DEFAULT_LIMIT : command.limit();
		return this.knowledgeVectorStore.search(command.query().trim(), limit);
	}
}
