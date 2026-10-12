package io.github.wskr00.dianysma.core.audit.mcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
class McpHttpLoggingConfiguration {

	@Bean
	FilterRegistrationBean<McpHttpLoggingFilter> mcpHttpLoggingFilter(
			@Value("${spring.ai.mcp.server.streamable-http.mcp-endpoint:/mcp}") String mcpEndpointPath) {
		var registration = new FilterRegistrationBean<>(new McpHttpLoggingFilter());
		registration.setName("mcpHttpLoggingFilter");
		registration.addUrlPatterns(mcpEndpointPath);
		registration.setOrder(Ordered.LOWEST_PRECEDENCE);
		return registration;
	}

}
