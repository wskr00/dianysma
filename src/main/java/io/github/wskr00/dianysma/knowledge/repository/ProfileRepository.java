package io.github.wskr00.dianysma.knowledge.repository;

import io.github.wskr00.dianysma.knowledge.domain.Profile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

	Optional<Profile> findFirstByOrderByCreatedAtAsc();
}
