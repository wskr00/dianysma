package io.github.wskr00.dianysma.knowledge.domain;

import io.github.wskr00.dianysma.core.audit.Auditable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "claim", indexes = {
		@Index(name = "idx_claim_participation_id", columnList = "participation_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Claim extends Auditable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "participation_id", foreignKey = @ForeignKey(name = "fk_claim_participation"))
	private Participation participation;

	@Column(nullable = false, columnDefinition = "text")
	private String content;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "claim_skill",
			joinColumns = @JoinColumn(name = "claim_id", nullable = false,
					foreignKey = @ForeignKey(name = "fk_claim_skill_claim")),
			inverseJoinColumns = @JoinColumn(name = "skill_id", nullable = false,
					foreignKey = @ForeignKey(name = "fk_claim_skill_skill")),
			indexes = @Index(name = "idx_claim_skill_skill_id", columnList = "skill_id"))
	private Set<Skill> skills = new HashSet<>();

}
