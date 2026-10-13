package io.github.wskr00.dianysma.knowledge.controller.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.wskr00.dianysma.TestcontainersConfiguration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.cloud.aws.region.static=us-east-1",
		"spring.ai.google.genai.embedding.api-key=test-key-never-used"
})
class McpServerIntegrationTest {

	@LocalServerPort
	private int serverPort;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private int requestId;

	@BeforeEach
	void setUp() {
		this.requestId = 1;
		this.jdbcTemplate.update("delete from profile");
		this.jdbcTemplate.update("delete from skill");
	}

	@Test
	void anonymousPingReturnsValidJsonRpcResponse() throws Exception {
		var response = post(request("ping", Map.of()));

		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.body().path("jsonrpc").asString()).isEqualTo("2.0");
		assertThat(response.body().path("id").asInt()).isEqualTo(1);
		assertThat(response.body().path("result").isObject()).isTrue();
		assertThat(response.body().path("result").isEmpty()).isTrue();
	}

	@Test
	void healthEndpointIsPublicAndReportsTheDatabaseAsReady() throws Exception {
		var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:%d/actuator/health".formatted(this.serverPort)))
			.header("Accept", "application/json")
			.GET()
			.build();
		var response = this.httpClient.send(request, BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(this.objectMapper.readTree(response.body()).path("status").asString()).isEqualTo("UP");
	}

	@Test
	void initializeAndToolsListWorkWithoutApplicationAuthentication() throws Exception {
		var initialized = post(initializeRequest());
		assertThat(initialized.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(initialized.body().path("result").path("protocolVersion").asString()).isEqualTo("2025-11-25");
		post(notification("notifications/initialized"));

		JsonNode tools = post(request("tools/list", Map.of())).body();
		assertThat(tools.path("result").path("tools").size()).isEqualTo(41);
		assertThat(findTool(tools, "create_profile").path("name").asString()).isEqualTo("create_profile");
		assertThat(findTool(tools, "get_profile").path("name").asString()).isEqualTo("get_profile");
	}

	@Test
	void toolsCanBeCalledWithoutApplicationAuthentication() throws Exception {
		initialize();
		JsonNode result = callTool("create_profile", Map.of("name", "Lucas"));
		assertThat(result.path("isError").asBoolean()).isFalse();
		assertThat(this.jdbcTemplate.queryForObject("select count(*) from profile", Integer.class)).isOne();
	}

	@Test
	void invalidProfileEmailIsReportedAsInputErrorAndDoesNotPersist() throws Exception {
		initialize();

		JsonNode result = callTool("create_profile",
				Map.of("name", "Lucas", "email", "not-an-email"));

		assertThat(result.path("isError").asBoolean()).isTrue();
		assertThat(result.path("content").get(0).path("text").asString()).contains("e-mail válido");
		assertThat(this.jdbcTemplate.queryForObject("select count(*) from profile", Integer.class)).isZero();
	}

	@Test
	void profileToolsReturnNullableFieldsAndSkillToolsUseCanonicalCaseInsensitiveRecords() throws Exception {
		initialize();

		JsonNode tools = post(request("tools/list", Map.of())).body();
		JsonNode getProfileTool = findTool(tools, "get_profile");
		JsonNode profileProperties = getProfileTool.path("outputSchema").path("properties");
		for (String field : List.of("name", "email", "phone", "city", "state", "country",
				"linkedinUrl", "githubUrl", "portfolioUrl")) {
			assertThat(schemaAllowsNull(profileProperties.path(field)))
				.as("MCP output schema must allow null for optional profile field %s", field)
				.isTrue();
		}

		JsonNode createdProfile = callTool("create_profile", Map.of("name", "Lucas"));
		assertThat(createdProfile.path("isError").asBoolean()).isFalse();
		JsonNode profileData = createdProfile.path("structuredContent");
		assertThat(profileData.path("name").asString()).isEqualTo("Lucas");
		assertThat(profileData.path("email").isNull()).isTrue();
		assertThat(profileData.path("city").isNull()).isTrue();
		UUID profileId = UUID.fromString(profileData.path("id").asString());
		assertThat(profileId.version()).isEqualTo(7);
		assertThat(profileData.path("createdAt").isString()).isTrue();
		assertThat(profileData.path("updatedAt").isString()).isTrue();
		for (String field : List.of("email", "phone", "city", "state", "country",
				"linkedinUrl", "githubUrl", "portfolioUrl")) {
			assertThat(profileData.path(field).isNull()).as("missing profile field %s should be returned as null", field).isTrue();
		}

		JsonNode duplicateProfile = callTool("create_profile", Map.of("name", "Segundo perfil"));
		assertThat(duplicateProfile.path("isError").asBoolean()).isTrue();
		assertThat(this.jdbcTemplate.queryForObject("select count(*) from profile", Integer.class)).isOne();

		JsonNode loadedProfile = callTool("get_profile", Map.of()).path("structuredContent");
		assertThat(loadedProfile.path("id").asString()).isEqualTo(profileData.path("id").asString());
		assertThat(loadedProfile.path("email").isNull()).isTrue();

		JsonNode createdSkill = callTool("create_skill",
				Map.of("name", " Java ", "aliases", List.of("JVM", "java"))).path("structuredContent");
		JsonNode duplicateSkill = callTool("create_skill",
				Map.of("name", "java", "aliases", List.of("HotSpot"))).path("structuredContent");
		assertThat(duplicateSkill.path("id").asString()).isEqualTo(createdSkill.path("id").asString());
		assertThat(duplicateSkill.path("aliases").toString()).isEqualTo("[\"java\",\"JVM\"]");

		JsonNode search = callTool("search_skills", Map.of("query", "jvm"));
		assertThat(search.path("structuredContent").path("skills").get(0).path("name").asString())
			.isEqualTo("Java");
	}

	@Test
	void postgresConstraintsProtectSingletonAndKnowledgeSourceInvariants() {
		var firstProfileId = UUID.randomUUID();
		var secondProfileId = UUID.randomUUID();
		insertProfile(firstProfileId, "Lucas");
		assertThatThrownBy(() -> insertProfile(secondProfileId, "Outro perfil"))
			.isInstanceOf(DataIntegrityViolationException.class);

		this.jdbcTemplate.update("insert into skill (id, created_at, updated_at, name) values (?, now(), now(), ?)",
				UUID.randomUUID(), "Java");
		assertThatThrownBy(() -> this.jdbcTemplate.update(
				"insert into skill (id, created_at, updated_at, name) values (?, now(), now(), ?)",
				UUID.randomUUID(), "java"))
			.isInstanceOf(DataIntegrityViolationException.class);

		assertThatThrownBy(() -> this.jdbcTemplate.update(
				"insert into participation (id, created_at, updated_at) values (?, now(), now())",
				UUID.randomUUID()))
			.isInstanceOf(DataIntegrityViolationException.class);

		var vectorId = UUID.randomUUID();
		this.jdbcTemplate.update("insert into vector_store (id) values (?)", vectorId);
		assertThatThrownBy(() -> this.jdbcTemplate.update(
				"insert into knowledge_chunk (id, indexed_at, model, hash) values (?, now(), ?, ?)",
				vectorId, "test-model", "test-hash"))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	private void initialize() throws Exception {
		var response = post(initializeRequest());
		assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.body().path("result").path("protocolVersion").asString()).isEqualTo("2025-11-25");
		post(notification("notifications/initialized"));
	}

	private JsonNode callTool(String name, Map<String, ?> arguments) throws Exception {
		JsonNode response = post(request("tools/call", Map.of("name", name, "arguments", arguments))).body();
		return response.path("result");
	}

	private JsonNode findTool(JsonNode response, String name) {
		for (JsonNode tool : response.path("result").path("tools")) {
			if (tool.path("name").asString().equals(name)) {
				return tool;
			}
		}
		throw new AssertionError("Tool MCP não encontrada: " + name);
	}

	private HttpResponse post(Map<String, ?> body) throws Exception {
		String jsonBody = this.objectMapper.writeValueAsString(body);
		var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:%d/mcp".formatted(this.serverPort)))
			.header("Content-Type", "application/json")
			.header("Accept", "application/json, text/event-stream")
			.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
		var response = this.httpClient.send(request.build(), BodyHandlers.ofString());
		JsonNode json = response.body() == null || response.body().isBlank()
				? this.objectMapper.nullNode()
				: this.objectMapper.readTree(response.body());
		return new HttpResponse(response.statusCode(), json);
	}

	private Map<String, ?> initializeRequest() {
		return Map.of(
				"jsonrpc", "2.0",
				"id", this.requestId++,
				"method", "initialize",
				"params", Map.of(
						"protocolVersion", "2025-11-25",
						"capabilities", Map.of(),
						"clientInfo", Map.of("name", "dianysma-tests", "version", "1.0")));
	}

	private Map<String, ?> request(String method, Map<String, ?> params) {
		return Map.of("jsonrpc", "2.0", "id", this.requestId++, "method", method, "params", params);
	}

	private Map<String, ?> notification(String method) {
		return Map.of("jsonrpc", "2.0", "method", method);
	}

	private static boolean schemaAllowsNull(JsonNode schema) {
		JsonNode type = schema.path("type");
		if (type.isArray()) {
			for (JsonNode candidate : type) {
				if (candidate.asString().equals("null")) {
					return true;
				}
			}
		}
		if (type.isString() && type.asString().equals("null")) {
			return true;
		}
		if (schema.path("nullable").asBoolean(false)) {
			return true;
		}
		for (String composition : List.of("anyOf", "oneOf")) {
			JsonNode alternatives = schema.path(composition);
			for (JsonNode alternative : alternatives) {
				if (schemaAllowsNull(alternative)) {
					return true;
				}
			}
		}
		return false;
	}

	private void insertProfile(UUID id, String name) {
		this.jdbcTemplate.update(
				"insert into profile (id, created_at, updated_at, name) values (?, now(), now(), ?)", id, name);
	}

	private record HttpResponse(int statusCode, JsonNode body) {
	}

}
