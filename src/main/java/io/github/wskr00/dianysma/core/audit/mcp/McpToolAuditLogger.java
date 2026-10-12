package io.github.wskr00.dianysma.core.audit.mcp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpToolAuditLogger {

	public static void succeeded(String toolName, long durationNanos) {
		logToolCall(toolName, "success", durationNanos, null, null);
	}

	public static void failed(String toolName, long durationNanos, Class<? extends Throwable> errorType) {
		logToolCall(toolName, "failure", durationNanos, errorType, null);
	}

	public static void failedUnexpectedly(String toolName, long durationNanos, RuntimeException failure) {
		logToolCall(toolName, "failure", durationNanos, failure.getClass(), failure);
	}

	private static void logToolCall(String toolName, String outcome, long durationNanos,
			Class<? extends Throwable> errorType, RuntimeException failure) {
		var event = failure == null ? log.atInfo() : log.atError();
		event.addKeyValue("event.action", "mcp_tool_call")
			.addKeyValue("event.outcome", outcome)
			.addKeyValue("event.duration", durationNanos)
			.addKeyValue("mcp.tool.name", toolName);
		if (errorType != null) {
			event.addKeyValue("error.type", errorType.getSimpleName());
		}
		if (failure != null) {
			event.setCause(failure);
		}
		event.log(failure == null ? "Execução da ferramenta MCP concluída" : "Falha inesperada na ferramenta MCP");
	}

}
