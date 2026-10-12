package io.github.wskr00.dianysma.knowledge.repository;

import io.github.wskr00.dianysma.knowledge.domain.Skill;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SkillRepository extends JpaRepository<Skill, UUID> {

	Optional<Skill> findFirstByNameIgnoreCase(String name);

	@Query(value = """
			SELECT s.*
			FROM skill s
			WHERE s.name ILIKE CONCAT('%', :query, '%')
			   OR EXISTS (
					SELECT 1
					FROM unnest(COALESCE(s.aliases, ARRAY[]::text[])) AS aliases(skill_alias)
					WHERE aliases.skill_alias ILIKE CONCAT('%', :query, '%')
			   )
			ORDER BY s.name
			""", nativeQuery = true)
	List<Skill> searchByNameOrAlias(@Param("query") String query, Limit limit);
}
