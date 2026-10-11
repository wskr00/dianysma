package io.github.wskr00.dianysma.knowledge.domain;

import java.time.LocalDate;

import io.github.wskr00.dianysma.core.audit.Auditable;
import io.github.wskr00.dianysma.knowledge.domain.enums.EducationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "education")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Education extends Auditable {

	@Column(nullable = false, length = 100)
	private String institution;

	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EducationType type;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(length = 100)
	private String degree;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "workload_hours")
	private Integer workloadHours;

	@Column(name = "started_at")
	private LocalDate startedAt;

	@Column(name = "completed_at")
	private LocalDate completedAt;

	private Boolean expected;
}
