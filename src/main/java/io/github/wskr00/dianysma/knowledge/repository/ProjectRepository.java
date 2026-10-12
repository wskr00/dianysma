package io.github.wskr00.dianysma.knowledge.repository;

import io.github.wskr00.dianysma.knowledge.domain.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

	@EntityGraph(attributePaths = "organization")
	Optional<Project> findDetailedById(UUID id);

	List<Project> findAllByOrderByUpdatedAtDesc(Limit limit);

	boolean existsByOrganization_Id(UUID organizationId);
}
