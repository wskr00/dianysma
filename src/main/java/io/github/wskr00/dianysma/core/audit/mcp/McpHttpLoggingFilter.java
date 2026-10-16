package io.github.wskr00.dianysma.core.audit.mcp;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class McpHttpLoggingFilter extends OncePerRequestFilter {

	private static final String REQUEST_ID_HEADER = "X-Request-Id";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestId = UUID.randomUUID().toString();
		String previousRequestId = MDC.get(McpToolAuditLogger.REQUEST_ID_MDC_KEY);
		long startedAt = System.nanoTime();

		response.setHeader(REQUEST_ID_HEADER, requestId);
		MDC.put(McpToolAuditLogger.REQUEST_ID_MDC_KEY, requestId);

		Throwable failure = null;
		try {
			filterChain.doFilter(request, response);
		} catch (IOException | ServletException | RuntimeException | Error exception) {
			failure = exception;
			throw exception;
		} finally {
			try {
				McpToolAuditLogger.completed(request, response, startedAt, failure);
			} finally {
				if (previousRequestId == null) {
					MDC.remove(McpToolAuditLogger.REQUEST_ID_MDC_KEY);
				} else {
					MDC.put(McpToolAuditLogger.REQUEST_ID_MDC_KEY, previousRequestId);
				}
			}
		}
	}
}
