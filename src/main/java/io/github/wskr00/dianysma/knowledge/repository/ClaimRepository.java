package io.github.wskr00.dianysma.knowledge.repository;

import io.github.wskr00.dianysma.knowledge.domain.Claim;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaimRepository extends JpaRepository<Claim, UUID> {

	@EntityGraph(attributePaths = { "skills", "participation" })
	Optional<Claim> findDetailedById(UUID id);

	@EntityGraph(attributePaths = { "skills", "participation" })
	List<Claim> findDetailedAllByIdIn(Collection<UUID> ids);

	@Query("select claim.id from Claim claim order by claim.id")
	List<UUID> findAllIdsForVectorReindexing();

	@Query("""
			select claim.id
			from Claim claim
			where (:participationId is null or claim.participation.id = :participationId)
			order by claim.updatedAt desc
			""")
	List<UUID> findIds(@Param("participationId") UUID participationId, Limit limit);

	boolean existsByParticipation_Id(UUID participationId);
}
