package io.github.wskr00.dianysma.knowledge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.CreateClaimCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.UpdateClaimCommand;
import io.github.wskr00.dianysma.knowledge.domain.Claim;
import io.github.wskr00.dianysma.knowledge.domain.Skill;
import io.github.wskr00.dianysma.knowledge.repository.ClaimRepository;
import io.github.wskr00.dianysma.knowledge.repository.ParticipationRepository;
import io.github.wskr00.dianysma.knowledge.repository.SkillRepository;
import io.github.wskr00.dianysma.knowledge.service.exception.InvalidKnowledgeInputException;
import io.github.wskr00.dianysma.knowledge.service.exception.KnowledgeNotFoundException;
import io.github.wskr00.dianysma.knowledge.service.vector.KnowledgeVectorIndexer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

	@Mock
	private ClaimRepository claimRepository;

	@Mock
	private ParticipationRepository participationRepository;

	@Mock
	private SkillRepository skillRepository;

	@Mock
	private KnowledgeVectorIndexer knowledgeVectorIndexer;

	@InjectMocks
	private ClaimService claimService;

	@Test
	void updateCanClearParticipationAndSkillsExplicitly() {
		UUID claimId = UUID.randomUUID();
		var existingSkill = new Skill("Java", new String[] { "JVM" });
		var claim = new Claim(null, "Experiência anterior", new HashSet<>(Set.of(existingSkill)));
		when(this.claimRepository.findDetailedById(claimId)).thenReturn(Optional.of(claim));
		when(this.claimRepository.saveAndFlush(claim)).thenReturn(claim);

		var result = this.claimService.update(new UpdateClaimCommand(claimId, "Experiência revisada", null, true, List.of()));

		assertThat(result.content()).isEqualTo("Experiência revisada");
		assertThat(result.participationId()).isNull();
		assertThat(result.skills()).isEmpty();
		verify(this.knowledgeVectorIndexer).index(claim);
	}

	@Test
	void rejectsConflictingParticipationInstructionsBeforeLoadingClaim() {
		UUID claimId = UUID.randomUUID();

		assertThatThrownBy(() -> this.claimService.update(
				new UpdateClaimCommand(claimId, "Texto válido", UUID.randomUUID(), true, null)))
			.isInstanceOf(InvalidKnowledgeInputException.class)
			.hasMessageContaining("não informe ambos");

		verify(this.claimRepository, never()).findDetailedById(any());
	}

	@Test
	void refusesToCreateClaimWhenAnyReferencedSkillDoesNotExist() {
		UUID existingSkillId = UUID.randomUUID();
		UUID missingSkillId = UUID.randomUUID();
		var existingSkill = new Skill("Java", new String[0]);
		when(this.skillRepository.findAllById(Set.of(existingSkillId, missingSkillId)))
			.thenReturn(List.of(existingSkill));

		assertThatThrownBy(() -> this.claimService.create(
				new CreateClaimCommand("Implementei uma API", null, List.of(existingSkillId, missingSkillId))))
			.isInstanceOf(KnowledgeNotFoundException.class)
			.hasMessageContaining("IDs de competência");

		verify(this.claimRepository, never()).saveAndFlush(any(Claim.class));
		verify(this.knowledgeVectorIndexer, never()).index(any(Claim.class));
	}

}
