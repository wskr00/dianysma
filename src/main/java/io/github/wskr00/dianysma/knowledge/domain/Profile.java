package io.github.wskr00.dianysma.knowledge.domain;

import io.github.wskr00.dianysma.core.audit.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Profile extends Auditable {

	@Column(length = 120)
	private String name;

	@Column(length = 50)
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(length = 40)
	private String city;

	@Column(length = 40)
	private String state;

	@Column(length = 40)
	private String country;

	@Column(name = "linkedin_url", length = 150)
	private String linkedinUrl;

	@Column(name = "github_url", length = 150)
	private String githubUrl;

	@Column(name = "portfolio_url", length = 150)
	private String portfolioUrl;

}
