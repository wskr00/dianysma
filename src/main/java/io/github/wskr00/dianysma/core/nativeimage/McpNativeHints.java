package io.github.wskr00.dianysma.core.nativeimage;

import java.util.List;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.CreateClaimCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.ListClaimsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.claim.UpdateClaimCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.common.ListKnowledgeRecordsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.education.EducationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.experience.ExperienceCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.knowledge.SearchKnowledgeCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.language.LanguageCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.CreateOrganizationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.SearchOrganizationsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.organization.UpdateOrganizationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.participation.ParticipationCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.profile.ProfileCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.project.ProjectCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.skill.CreateSkillCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.command.skill.SearchSkillsCommand;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.claim.ClaimDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.claim.ClaimList;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.common.DeleteResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.education.EducationDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.education.EducationList;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.experience.ExperienceDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.experience.ExperienceList;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.knowledge.KnowledgeSearchHit;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.knowledge.KnowledgeSearchResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.language.LanguageDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.language.LanguageList;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.organization.OrganizationDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.organization.OrganizationSearchResult;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.participation.ParticipationDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.participation.ParticipationList;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.profile.ProfileDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.project.ProjectDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.project.ProjectList;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.skill.SkillDetails;
import io.github.wskr00.dianysma.knowledge.controller.dto.result.skill.SkillSearchResult;

public final class McpNativeHints implements RuntimeHintsRegistrar {

	private static final List<Class<?>> COMMAND_TYPES = List.of(
			CreateClaimCommand.class,
			ListClaimsCommand.class,
			UpdateClaimCommand.class,
			ListKnowledgeRecordsCommand.class,
			EducationCommand.class,
			ExperienceCommand.class,
			SearchKnowledgeCommand.class,
			LanguageCommand.class,
			CreateOrganizationCommand.class,
			SearchOrganizationsCommand.class,
			UpdateOrganizationCommand.class,
			ParticipationCommand.class,
			ProfileCommand.class,
			ProjectCommand.class,
			CreateSkillCommand.class,
			SearchSkillsCommand.class);

	private static final List<Class<?>> RESULT_TYPES = List.of(
			ClaimDetails.class,
			ClaimList.class,
			DeleteResult.class,
			EducationDetails.class,
			EducationList.class,
			ExperienceDetails.class,
			ExperienceList.class,
			KnowledgeSearchHit.class,
			KnowledgeSearchResult.class,
			LanguageDetails.class,
			LanguageList.class,
			OrganizationDetails.class,
			OrganizationSearchResult.class,
			ParticipationDetails.class,
			ParticipationList.class,
			ProfileDetails.class,
			ProjectDetails.class,
			ProjectList.class,
			SkillDetails.class,
			SkillSearchResult.class);

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		RESULT_TYPES.forEach(type -> hints.reflection().registerType(type, MemberCategory.INVOKE_PUBLIC_METHODS));
		COMMAND_TYPES.forEach(type -> hints.reflection().registerType(type,
				MemberCategory.INVOKE_PUBLIC_METHODS,
				MemberCategory.ACCESS_DECLARED_FIELDS));
	}

}
