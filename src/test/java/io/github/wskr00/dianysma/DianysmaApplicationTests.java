package io.github.wskr00.dianysma;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"COGNITO_ISSUER_URI=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_test",
		"MCP_RESOURCE_URI=https://dianysma.example/mcp",
		"spring.ai.google.genai.embedding.api-key=test-key"
})
class DianysmaApplicationTests {

	@Test
	void contextLoads() {
	}

}
