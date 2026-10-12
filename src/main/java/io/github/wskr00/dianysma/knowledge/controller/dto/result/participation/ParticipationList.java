package io.github.wskr00.dianysma.knowledge.controller.dto.result.participation;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record ParticipationList(
		@JsonPropertyDescription(
				"Participações profissionais encontradas, ordenadas pela atualização mais recente. A lista pode estar vazia.")
		List<ParticipationDetails> participations) {
}
