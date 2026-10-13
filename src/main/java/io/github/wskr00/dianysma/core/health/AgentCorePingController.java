package io.github.wskr00.dianysma.core.health;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentCorePingController {

	@GetMapping("/ping")
	public Map<String, String> ping() {
		return Map.of("status", "Healthy");
	}

}
