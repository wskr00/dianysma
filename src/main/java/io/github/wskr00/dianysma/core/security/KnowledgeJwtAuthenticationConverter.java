package io.github.wskr00.dianysma.core.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public final class KnowledgeJwtAuthenticationConverter extends JwtAuthenticationConverter {

	private static final GrantedAuthority KNOWLEDGE_READ_AUTHORITY =
			new SimpleGrantedAuthority(KnowledgeScopes.READ_AUTHORITY);
	private static final GrantedAuthority KNOWLEDGE_WRITE_AUTHORITY =
			new SimpleGrantedAuthority(KnowledgeScopes.WRITE_AUTHORITY);

	private final Converter<Jwt, Collection<GrantedAuthority>> scopeAuthoritiesConverter =
			new JwtGrantedAuthoritiesConverter();

	private final GrantedAuthority readScopeAuthority;
	private final GrantedAuthority writeScopeAuthority;

	public KnowledgeJwtAuthenticationConverter(KnowledgeScopes knowledgeScopes) {
		this.readScopeAuthority = new SimpleGrantedAuthority("SCOPE_" + knowledgeScopes.getKnowledgeRead());
		this.writeScopeAuthority = new SimpleGrantedAuthority("SCOPE_" + knowledgeScopes.getKnowledgeWrite());
		setJwtGrantedAuthoritiesConverter(this::convertAuthorities);
	}

	private Collection<GrantedAuthority> convertAuthorities(Jwt jwt) {
		Collection<GrantedAuthority> scopeAuthorities = scopeAuthoritiesConverter.convert(jwt);
		var authorities = new ArrayList<GrantedAuthority>(scopeAuthorities.size() + 2);
		authorities.addAll(scopeAuthorities);

		if (scopeAuthorities.contains(readScopeAuthority)) {
			authorities.add(KNOWLEDGE_READ_AUTHORITY);
		}
		if (scopeAuthorities.contains(writeScopeAuthority)) {
			authorities.add(KNOWLEDGE_WRITE_AUTHORITY);
		}

		return authorities;
	}

}
