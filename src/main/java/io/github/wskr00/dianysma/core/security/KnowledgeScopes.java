package io.github.wskr00.dianysma.core.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component("knowledgeScopes")
@ConfigurationProperties(prefix = "dianysma.security.scopes")
@Getter
@Setter
public final class KnowledgeScopes {

	public static final String READ_AUTHORITY = "KNOWLEDGE_READ";
	public static final String WRITE_AUTHORITY = "KNOWLEDGE_WRITE";

	private String knowledgeRead;

	private String knowledgeWrite;

}
