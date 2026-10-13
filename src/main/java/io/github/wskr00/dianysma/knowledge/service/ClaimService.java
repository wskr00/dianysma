package io.github.wskr00.dianysma.knowledge.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.CreateClaimCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.ListClaimsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.UpdateClaimCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.claim.ClaimDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.skill.SkillDetails;
import io.github.wskr00.dianysma.knowledge.domain.Claim;
import io.github.wskr00.dianysma.knowledge.domain.Participation;
import io.github.wskr00.dianysma.knowledge.domain.Skill;
import io.github.wskr00.dianysma.knowledge.repository.ClaimRepository;
import io.github.wskr00.dianysma.knowledge.repository.ParticipationRepository;
import io.github.wskr00.dianysma.knowledge.repository.SkillRepository;
import io.github.wskr00.dianysma.knowledge.service.exception.InvalidKnowledgeInputException;
import io.github.wskr00.dianysma.knowledge.service.exception.KnowledgeNotFoundException;
import io.github.wskr00.dianysma.knowledge.service.vector.KnowledgeVectorIndexer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;

@Service
@Validated
@RequiredArgsConstructor
public class ClaimService {

	private final ClaimRepository claimRepository;
	private final ParticipationRepository participationRepository;
	private final SkillRepository skillRepository;
	private final KnowledgeVectorIndexer knowledgeVectorIndexer;

	@Transactional(readOnly = true)
	public ClaimDetails get(@NotNull UUID claimId) {
		return this.claimRepository.findDetailedById(claimId)
				.map(ClaimService::toDetails)
				.orElseThrow(() -> new KnowledgeNotFoundException("Fato profissional não encontrado."));
	}

	@Transactional(readOnly = true)
	public List<ClaimDetails> list(@NotNull @Valid ListClaimsCommand command) {
		int requestedLimit = command.limit() == null ? 20 : command.limit();
		List<UUID> claimIds = this.claimRepository.findIds(command.participationId(), Limit.of(requestedLimit));
		if (claimIds.isEmpty()) {
			return List.of();
		}
		Map<UUID, Claim> claimsById = this.claimRepository.findDetailedAllByIdIn(claimIds)
				.stream()
				.collect(Collectors.toMap(Claim::getId, Function.identity()));
		return claimIds.stream()
				.map(claimsById::get)
				.map(ClaimService::toDetails)
				.toList();
	}

	@Transactional
	public ClaimDetails create(@NotNull @Valid CreateClaimCommand command) {
		Claim claim = new Claim(
				resolveParticipation(command.participationId()),
				command.content(),
				resolveSkills(command.skillIds()));
		claim = this.claimRepository.saveAndFlush(claim);
		this.knowledgeVectorIndexer.index(claim);
		return toDetails(claim);
	}

	@Transactional
	public ClaimDetails update(@NotNull @Valid UpdateClaimCommand command) {
		if (Boolean.TRUE.equals(command.clearParticipation()) && command.participationId() != null) {
			throw new InvalidKnowledgeInputException(
					"Informe participationId para associar uma participação ou clearParticipation=true para desassociá-la; não informe ambos.");
		}

		Claim claim = this.claimRepository.findDetailedById(command.claimId())
				.orElseThrow(() -> new KnowledgeNotFoundException("Fato profissional não encontrado."));

		claim.setContent(command.content());
		if (command.participationId() != null) {
			claim.setParticipation(resolveParticipation(command.participationId()));
		} else if (Boolean.TRUE.equals(command.clearParticipation())) {
			claim.setParticipation(null);
		}
		if (command.skillIds() != null) {
			Set<Skill> skills = resolveSkills(command.skillIds());
			claim.getSkills().clear();
			claim.getSkills().addAll(skills);
		}

		claim = this.claimRepository.saveAndFlush(claim);
		this.knowledgeVectorIndexer.index(claim);
		return toDetails(claim);
	}

	@Transactional
	public DeleteResult delete(@NotNull UUID claimId) {
		Claim claim = this.claimRepository.findById(claimId).orElse(null);
		if (claim == null) {
			return new DeleteResult(claimId, false);
		}

		this.knowledgeVectorIndexer.delete(claimId);
		this.claimRepository.delete(claim);
		return new DeleteResult(claimId, true);
	}

	private Participation resolveParticipation(UUID participationId) {
		if (participationId == null) {
			return null;
		}
		return this.participationRepository.findById(participationId)
				.orElseThrow(() -> new KnowledgeNotFoundException("Participação não encontrada."));
	}

	private Set<Skill> resolveSkills(List<UUID> skillIds) {
		if (skillIds == null || skillIds.isEmpty()) {
			return new HashSet<>();
		}
		Set<UUID> uniqueIds = new HashSet<>(skillIds);
		List<Skill> skills = this.skillRepository.findAllById(uniqueIds);
		if (skills.size() != uniqueIds.size()) {
			throw new KnowledgeNotFoundException("Um ou mais IDs de competência informados não existem.");
		}
		return new HashSet<>(skills);
	}

	private static ClaimDetails toDetails(Claim claim) {
		List<SkillDetails> skills = claim.getSkills().stream()
				.map(SkillDetailsMapper::toDetails)
				.sorted((left, right) -> String.CASE_INSENSITIVE_ORDER.compare(left.name(), right.name()))
				.toList();
		UUID participationId = claim.getParticipation() == null ? null : claim.getParticipation().getId();
		return new ClaimDetails(claim.getId(), claim.getContent(), participationId, skills,
				claim.getCreatedAt(), claim.getUpdatedAt());
	}
}
