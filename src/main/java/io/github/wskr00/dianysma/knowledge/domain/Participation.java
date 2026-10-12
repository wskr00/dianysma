package io.github.wskr00.dianysma.knowledge.domain;

import io.github.wskr00.dianysma.core.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participation", indexes = {
		@Index(name = "idx_participation_experience_id", columnList = "experience_id"),
		@Index(name = "idx_participation_project_id", columnList = "project_id")
}, check = @CheckConstraint(
		name = "ck_participation_has_context",
		constraint = "num_nonnulls(experience_id, project_id) >= 1"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Participation extends Auditable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "experience_id", foreignKey = @ForeignKey(name = "fk_participation_experience"))
	private Experience experience;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_participation_project"))
	private Project project;

	@Column(columnDefinition = "text")
	private String description;

	@PrePersist
	@PreUpdate
	private void validateContext() {
		if (experience == null && project == null) {
			throw new IllegalStateException("Uma participação deve estar vinculada a uma experiência, a um projeto ou a ambos.");
		}
	}
}
