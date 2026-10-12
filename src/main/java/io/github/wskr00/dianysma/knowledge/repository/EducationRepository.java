package io.github.wskr00.dianysma.knowledge.repository;

import io.github.wskr00.dianysma.knowledge.domain.Education;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EducationRepository extends JpaRepository<Education, UUID> {

	@Query("select education.id from Education education order by education.id")
	List<UUID> findAllIdsForVectorReindexing();

	List<Education> findAllByOrderByUpdatedAtDesc(Limit limit);
}
