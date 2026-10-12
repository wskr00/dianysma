package io.github.wskr00.dianysma.knowledge.repository;

import io.github.wskr00.dianysma.knowledge.domain.Participation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipationRepository extends JpaRepository<Participation, UUID> {

	@EntityGraph(attributePaths = { "experience", "project" })
	Optional<Participation> findDetailedById(UUID id);

	@EntityGraph(attributePaths = { "experience.employer", "project.organization" })
	List<Participation> findAllDetailedByExperience_Id(UUID experienceId);

	@EntityGraph(attributePaths = { "experience.employer", "project.organization" })
	List<Participation> findAllDetailedByProject_Id(UUID projectId);

	@Query("select participation.id from Participation participation order by participation.id")
	List<UUID> findAllIdsForVectorReindexing();

	@Query("""
			select participation
			from Participation participation
			left join fetch participation.experience experience
			left join fetch experience.employer employer
			left join fetch participation.project project
			left join fetch project.organization projectOrganization
			where employer.id = :organizationId
				or projectOrganization.id = :organizationId
			""")
	List<Participation> findAllDetailedByOrganizationId(@Param("organizationId") UUID organizationId);

	List<Participation> findAllByOrderByUpdatedAtDesc(Limit limit);

	boolean existsByExperience_Id(UUID experienceId);

	boolean existsByProject_Id(UUID projectId);

	boolean existsByExperience_IdAndProject_Id(UUID experienceId, UUID projectId);

	boolean existsByExperience_IdAndProject_IdAndIdNot(UUID experienceId, UUID projectId, UUID id);
}
