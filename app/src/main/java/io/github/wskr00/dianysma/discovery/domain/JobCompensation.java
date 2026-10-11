package io.github.wskr00.dianysma.discovery.domain;

import java.util.UUID;

import io.github.wskr00.dianysma.discovery.domain.enums.SalaryPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "job_compensation")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JobCompensation {

	@Id
	@Column(name = "job_id", nullable = false, updatable = false)
	private UUID id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_job_compensation_job"))
	private Job job;

	@Column(length = 120)
	private String salary;

	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Enumerated(EnumType.STRING)
	@ColumnDefault("'MONTHLY'")
	private SalaryPeriod salaryPeriod = SalaryPeriod.MONTHLY;

	@JdbcTypeCode(SqlTypes.ARRAY)
	@Column(nullable = false, columnDefinition = "text[]")
	private String[] benefits;
}
