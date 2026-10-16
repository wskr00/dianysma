package io.github.wskr00.dianysma.core.audit.mcp;

import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class McpToolAuditLogger {

	static final String REQUEST_ID_MDC_KEY = "http.request.id";
	private static final String TOOL_NAME_ATTRIBUTE = McpToolAuditLogger.class.getName() + ".toolName";
	private static final String TOOL_FAILURE_ATTRIBUTE = McpToolAuditLogger.class.getName() + ".toolFailure";

	private record ToolFailure(Class<? extends Throwable> type, Throwable cause) {
	}

	public static void requested(String toolName) {
		var request = currentRequest();
		if (request == null) {
			return;
		}

		request.setAttribute(TOOL_NAME_ATTRIBUTE, toolName);
		var event = log.atInfo()
			.addKeyValue("event.action", "mcp_tool_call")
			.addKeyValue("event.phase", "start")
			.addKeyValue("mcp.tool.name", toolName);
		addRequestId(event);
		event.log("tool: {}", toolName);
	}

	public static void failed(String toolName, Class<? extends Throwable> errorType) {
		var request = currentToolRequest(toolName);
		if (request == null) {
			return;
		}

		request.setAttribute(TOOL_FAILURE_ATTRIBUTE, new ToolFailure(errorType, null));
	}

	public static void failedUnexpectedly(String toolName, RuntimeException failure) {
		var request = currentToolRequest(toolName);
		if (request == null) {
			return;
		}

		request.setAttribute(TOOL_FAILURE_ATTRIBUTE, new ToolFailure(failure.getClass(), failure));
	}

	static void completed(HttpServletRequest request, HttpServletResponse response, long startedAt,
			Throwable transportFailure) {
		Object requestedTool = request.getAttribute(TOOL_NAME_ATTRIBUTE);
		long durationNanos = System.nanoTime() - startedAt;
		long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
		int status = response.getStatus();
		if (transportFailure != null && !response.isCommitted() && status < HttpServletResponse.SC_BAD_REQUEST) {
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}
		if (!(requestedTool instanceof String toolName)) {
			if (transportFailure != null || status >= HttpServletResponse.SC_BAD_REQUEST) {
				logHttpFailure(request, status, durationNanos, durationMillis, transportFailure);
			}
			return;
		}

		ToolFailure toolFailure = request.getAttribute(TOOL_FAILURE_ATTRIBUTE) instanceof ToolFailure recordedFailure
				? recordedFailure
				: null;
		Throwable failure = toolFailure != null && toolFailure.cause() != null
				? toolFailure.cause()
				: transportFailure;
		String errorType = toolFailure == null ? null : toolFailure.type().getSimpleName();
		if (errorType == null && failure != null) {
			errorType = failure.getClass().getSimpleName();
		}
		boolean failed = errorType != null || status >= HttpServletResponse.SC_BAD_REQUEST;

		var event = failure != null || status >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR
				? log.atError()
				: log.atInfo();
		event.addKeyValue("event.action", "mcp_tool_call")
			.addKeyValue("event.phase", "end")
			.addKeyValue("event.outcome", failed ? "failure" : "success")
			.addKeyValue("event.duration", durationNanos)
			.addKeyValue("mcp.tool.name", toolName)
			.addKeyValue("http.response.status_code", status);
		addRequestId(event);
		if (errorType != null) {
			event.addKeyValue("error.type", errorType);
		}
		if (failure != null) {
			event.setCause(failure);
		}
		event.log("tool: {}, status: {}, duration: {}ms", toolName, status, durationMillis);
	}

	private static void logHttpFailure(HttpServletRequest request, int status, long durationNanos, long durationMillis,
			Throwable failure) {
		var event = failure != null || status >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR
				? log.atError()
				: log.atWarn();
		event.addKeyValue("event.action", "mcp_http_request")
			.addKeyValue("event.phase", "end")
			.addKeyValue("event.outcome", "failure")
			.addKeyValue("event.duration", durationNanos)
			.addKeyValue("http.request.method", request.getMethod())
			.addKeyValue("url.path", request.getServletPath())
			.addKeyValue("http.response.status_code", status);
		addRequestId(event);
		if (failure != null) {
			event.addKeyValue("error.type", failure.getClass().getSimpleName()).setCause(failure);
		}
		event.log("Falha na requisição MCP HTTP: {} {}, HTTP {}, duração: {} ms", request.getMethod(),
				request.getServletPath(), status, durationMillis);
	}

	private static void addRequestId(LoggingEventBuilder event) {
		String requestId = MDC.get(REQUEST_ID_MDC_KEY);
		if (requestId != null) {
			event.addKeyValue(REQUEST_ID_MDC_KEY, requestId);
		}
	}

	private static HttpServletRequest currentRequest() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		return attributes instanceof ServletRequestAttributes servletAttributes
				? servletAttributes.getRequest()
				: null;
	}

	private static HttpServletRequest currentToolRequest(String toolName) {
		var request = currentRequest();
		return request != null && toolName.equals(request.getAttribute(TOOL_NAME_ATTRIBUTE)) ? request : null;
	}

}
