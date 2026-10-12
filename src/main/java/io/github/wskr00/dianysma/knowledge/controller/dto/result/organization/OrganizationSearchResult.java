package io.github.wskr00.dianysma.knowledge.controller.dto.result.organization;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record OrganizationSearchResult(
		@JsonPropertyDescription(
				"Organizações que correspondem à busca. A lista pode estar vazia.")
		List<OrganizationDetails> organizations) {
}
