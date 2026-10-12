package io.github.wskr00.dianysma.core.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KnowledgeJwtAuthenticationConverterTest {

	private final KnowledgeScopes scopes = scopes("custom/knowledge.read", "custom/knowledge.write");
	private final KnowledgeJwtAuthenticationConverter converter = new KnowledgeJwtAuthenticationConverter(this.scopes);

	@Test
	void mapsConfiguredCognitoScopesToKnowledgeAuthorities() {
		var authentication = this.converter.convert(jwt("custom/knowledge.read custom/knowledge.write profile"));

		assertThat(authentication).isNotNull();
		assertThat(authentication.getAuthorities())
			.extracting(GrantedAuthority::getAuthority)
			.contains(
					"SCOPE_custom/knowledge.read",
					"SCOPE_custom/knowledge.write",
					KnowledgeScopes.READ_AUTHORITY,
					KnowledgeScopes.WRITE_AUTHORITY);
	}

	@Test
	void doesNotGrantKnowledgeAuthorityForUnrelatedScopes() {
		var authentication = this.converter.convert(jwt("profile email"));

		assertThat(authentication).isNotNull();
		assertThat(authentication.getAuthorities())
			.extracting(GrantedAuthority::getAuthority)
			.contains("SCOPE_profile", "SCOPE_email")
			.doesNotContain(KnowledgeScopes.READ_AUTHORITY, KnowledgeScopes.WRITE_AUTHORITY);
	}

	@Test
	void doesNotMapScopeThatOnlySharesTheConfiguredScopePrefix() {
		var authentication = this.converter.convert(jwt("custom/knowledge.read.untrusted"));

		assertThat(authentication).isNotNull();
		assertThat(authentication.getAuthorities())
			.extracting(GrantedAuthority::getAuthority)
			.doesNotContain(KnowledgeScopes.READ_AUTHORITY, KnowledgeScopes.WRITE_AUTHORITY);
	}

	private static KnowledgeScopes scopes(String read, String write) {
		var scopes = new KnowledgeScopes();
		scopes.setKnowledgeRead(read);
		scopes.setKnowledgeWrite(write);
		return scopes;
	}

	private static Jwt jwt(String scopeClaim) {
		return Jwt.withTokenValue("test-token")
			.header("alg", "none")
			.claim("scope", scopeClaim)
			.build();
	}

}
