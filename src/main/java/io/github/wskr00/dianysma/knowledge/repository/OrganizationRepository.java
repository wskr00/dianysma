package io.github.wskr00.dianysma.knowledge.repository;

import io.github.wskr00.dianysma.knowledge.domain.Organization;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

	List<Organization> findAllByNameContainingIgnoreCaseOrderByName(String name, Limit limit);
}
