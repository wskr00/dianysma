package io.github.wskr00.dianysma.discovery.domain;

import java.util.HashSet;
import java.util.Set;

import io.github.wskr00.dianysma.core.audit.Auditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Job extends Auditable {

	@Column(nullable = false, length = 160)
	private String title;

	@Column(name = "company_name", nullable = false, length = 100)
	private String companyName;

	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@OneToOne(mappedBy = "job", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private JobRequirements requirements;

	@OneToOne(mappedBy = "job", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private JobCompensation compensation;

	@OneToOne(mappedBy = "job", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private JobLocation location;

	@OneToMany(mappedBy = "job", fetch = FetchType.LAZY)
	private Set<JobPosting> postings = new HashSet<>();
}
