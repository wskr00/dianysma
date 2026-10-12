package io.github.wskr00.dianysma.knowledge.domain;

import java.time.LocalDate;

import io.github.wskr00.dianysma.core.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "experience", indexes = {
		@Index(name = "idx_experience_employer_id", columnList = "employer_id"),
		@Index(name = "idx_experience_client_id", columnList = "client_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Experience extends Auditable {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "employer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_experience_employer"))
	private Organization employer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "fk_experience_client"))
	private Organization client;

	@Column(nullable = false, length = 200)
	private String role;

	@Column(name = "employment_type", nullable = false, length = 40)
	private String employmentType;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "started_at")
	private LocalDate startedAt;

	@Column(name = "ended_at")
	private LocalDate endedAt;

}
