package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.core.audit.mcp.McpToolAuditLogger;
import io.github.wskr00.dianysma.knowledge.service.exception.InvalidKnowledgeInputException;
import io.github.wskr00.dianysma.knowledge.service.exception.KnowledgeNotFoundException;
import io.modelcontextprotocol.spec.McpError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class McpToolErrors {

	static <T> T call(String operation, Supplier<T> action) {
		long startedAt = System.nanoTime();
		try {
			T result = action.get();
			McpToolAuditLogger.succeeded(operation, System.nanoTime() - startedAt);
			return result;
		}
		catch (InvalidKnowledgeInputException | KnowledgeNotFoundException exception) {
			McpToolAuditLogger.failed(operation, System.nanoTime() - startedAt, exception.getClass());
			throw exception;
		}
		catch (ConstraintViolationException exception) {
			String message = exception.getConstraintViolations().stream()
					.map(ConstraintViolation::getMessage)
					.distinct()
					.collect(Collectors.joining(" "));
			var translatedException = new InvalidKnowledgeInputException(
					message.isBlank() ? "A validação dos dados informados falhou." : message);
			McpToolAuditLogger.failed(operation, System.nanoTime() - startedAt, translatedException.getClass());
			throw translatedException;
		}
		catch (McpError exception) {
			McpToolAuditLogger.failed(operation, System.nanoTime() - startedAt, exception.getClass());
			throw exception;
		}
		catch (RuntimeException exception) {
			McpToolAuditLogger.failedUnexpectedly(operation, System.nanoTime() - startedAt, exception);
			throw new IllegalStateException(
					"Não foi possível concluir a operação devido a uma falha interna. Tente novamente mais tarde. Se o problema persistir, entre em contato com quem administra o servidor.");
		}
	}
}
