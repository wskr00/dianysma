package io.github.wskr00.dianysma.core.audit.mcp;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class McpHttpLoggingFilter extends OncePerRequestFilter {

	private static final String REQUEST_ID_HEADER = "X-Request-Id";
	private static final String REQUEST_ID_MDC_KEY = "http.request.id";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestId = UUID.randomUUID().toString();
		String previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
		long startedAt = System.nanoTime();

		response.setHeader(REQUEST_ID_HEADER, requestId);
		MDC.put(REQUEST_ID_MDC_KEY, requestId);

		Throwable failure = null;
		try {
			filterChain.doFilter(request, response);
		} catch (IOException | ServletException | RuntimeException exception) {
			failure = exception;
			throw exception;
		} finally {
			try {
				logRequest(request, response, startedAt, failure);
			} finally {
				if (previousRequestId == null) {
					MDC.remove(REQUEST_ID_MDC_KEY);
				} else {
					MDC.put(REQUEST_ID_MDC_KEY, previousRequestId);
				}
			}
		}
	}

	private static void logRequest(HttpServletRequest request, HttpServletResponse response, long startedAt,
			Throwable failure) {
		int status = response.getStatus();
		String outcome = failure != null || status >= HttpServletResponse.SC_BAD_REQUEST ? "failure" : "success";

		var event = log.atInfo()
				.addKeyValue("event.action", "mcp_http_request")
				.addKeyValue("event.outcome", outcome)
				.addKeyValue("event.duration", System.nanoTime() - startedAt)
				.addKeyValue("http.request.method", request.getMethod())
				.addKeyValue("url.path", request.getServletPath())
				.addKeyValue("http.response.status_code", status);
		if (failure != null) {
			event.addKeyValue("error.type", failure.getClass().getSimpleName());
		}
		event.log("Requisição HTTP MCP concluída");
	}

}
