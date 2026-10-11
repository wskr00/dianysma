package io.github.wskr00.dianysma.discovery.domain;

import java.time.OffsetDateTime;
import java.util.Map;

import io.github.wskr00.dianysma.core.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "job_posting",
		indexes = @Index(name = "idx_job_posting_job_id", columnList = "job_id"),
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_job_posting_provider_url",
						columnNames = { "provider", "normalized_url" })
		})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JobPosting extends Auditable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_job_posting_job"))
	private Job job;

	@Column(nullable = false, length = 40)
	private String provider;

	@Column(name = "normalized_url", nullable = false, length = 512)
	private String normalizedUrl;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "raw_data", nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> rawData;

	@Column(name = "published_at")
	private OffsetDateTime publishedAt;

	/** Marks this posting as observed again, including when its data is unchanged. */
	public void markObserved() {
		touch();
	}
}
