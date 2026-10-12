package io.github.wskr00.dianysma.knowledge.repository;

import io.github.wskr00.dianysma.knowledge.domain.Experience;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, UUID> {

	@EntityGraph(attributePaths = { "employer", "client" })
	Optional<Experience> findDetailedById(UUID id);

	List<Experience> findAllByOrderByUpdatedAtDesc(Limit limit);

	boolean existsByEmployer_Id(UUID employerId);

	boolean existsByClient_Id(UUID clientId);
}
