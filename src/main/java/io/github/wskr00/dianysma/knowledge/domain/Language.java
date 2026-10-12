package io.github.wskr00.dianysma.knowledge.domain;

import io.github.wskr00.dianysma.core.audit.Auditable;
import io.github.wskr00.dianysma.knowledge.domain.enums.LanguageProficiency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "language", indexes = {
		@Index(name = "idx_language_profile_id", columnList = "profile_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Language extends Auditable {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "profile_id", nullable = false, foreignKey = @ForeignKey(name = "fk_language_profile"))
	private Profile profile;

	@Column(nullable = false, length = 50)
	private String name;

	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LanguageProficiency proficiency;
}
