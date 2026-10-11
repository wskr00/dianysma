package io.github.wskr00.dianysma.knowledge.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "knowledge_chunk", indexes = {
		@Index(name = "idx_knowledge_chunk_participation_id", columnList = "participation_id"),
		@Index(name = "idx_knowledge_chunk_claim_id", columnList = "claim_id"),
		@Index(name = "idx_knowledge_chunk_education_id", columnList = "education_id")
}, check = @CheckConstraint(
		name = "ck_knowledge_chunk_one_source",
		constraint = "num_nonnulls(participation_id, claim_id, education_id) = 1"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class KnowledgeChunk {

	@Column(name = "id", nullable = false, updatable = false)
	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "participation_id", foreignKey = @ForeignKey(name = "fk_knowledge_chunk_participation"))
	private Participation participation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "claim_id", foreignKey = @ForeignKey(name = "fk_knowledge_chunk_claim"))
	private Claim claim;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "education_id", foreignKey = @ForeignKey(name = "fk_knowledge_chunk_education"))
	private Education education;

	@Column(nullable = false, length = 128)
	private String hash;

	@Column(nullable = false, length = 100)
	private String model;

	@Column(name = "indexed_at", nullable = false)
	private OffsetDateTime indexedAt;

	@PrePersist
	@PreUpdate
	private void validateSource() {
		int sourceCount = (participation == null ? 0 : 1)
				+ (claim == null ? 0 : 1)
				+ (education == null ? 0 : 1);
		if (sourceCount != 1) {
			throw new IllegalStateException("A knowledge chunk must reference exactly one source.");
		}
	}
}
