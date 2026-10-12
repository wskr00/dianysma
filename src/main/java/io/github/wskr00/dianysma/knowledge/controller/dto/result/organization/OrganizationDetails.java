package io.github.wskr00.dianysma.knowledge.controller.dto.result.organization;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizationDetails(
		@JsonPropertyDescription("UUID da organização; use-o nos campos employerId, clientId ou organizationId.") UUID id,
		@JsonPropertyDescription("Nome da organização.") String name,
		@JsonPropertyDescription("Data e hora de criação, com fuso horário.") OffsetDateTime createdAt,
		@JsonPropertyDescription("Data e hora da última atualização, com fuso horário.") OffsetDateTime updatedAt) {
}
