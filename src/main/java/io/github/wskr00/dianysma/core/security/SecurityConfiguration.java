package io.github.wskr00.dianysma.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfiguration {

	private static final String KNOWLEDGE_READ_SCOPE = "dianysma/knowledge.read";
	private static final String KNOWLEDGE_WRITE_SCOPE = "dianysma/knowledge.write";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			@Value("${spring.ai.mcp.server.streamable-http.mcp-endpoint}") String mcpEndpointPath)
			throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
				.requestMatchers(mcpEndpointPath).hasAnyAuthority(
						"SCOPE_" + KNOWLEDGE_READ_SCOPE,
						"SCOPE_" + KNOWLEDGE_WRITE_SCOPE)
				.anyRequest().denyAll())
			.oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

		return http.build();
	}

}
