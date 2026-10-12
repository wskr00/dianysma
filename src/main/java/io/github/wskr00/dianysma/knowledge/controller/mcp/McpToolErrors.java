package io.github.wskr00.dianysma.knowledge.controller.mcp;

import io.github.wskr00.dianysma.knowledge.service.exception.InvalidKnowledgeInputException;
import io.github.wskr00.dianysma.knowledge.service.exception.KnowledgeNotFoundException;
import io.modelcontextprotocol.spec.McpError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
final class McpToolErrors {

	private McpToolErrors() {
	}

	static <T> T call(String operation, Supplier<T> action) {
		try {
			return action.get();
		}
		catch (InvalidKnowledgeInputException | KnowledgeNotFoundException exception) {
			throw exception;
		}
		catch (ConstraintViolationException exception) {
			String message = exception.getConstraintViolations().stream()
					.map(ConstraintViolation::getMessage)
					.distinct()
					.collect(Collectors.joining(" "));
			throw new InvalidKnowledgeInputException(message.isBlank() ? "A validação dos dados informados falhou." : message);
		}
		catch (AccessDeniedException exception) {
			throw new AccessDeniedException("O token de acesso não contém o escopo de conhecimento necessário.");
		}
		catch (McpError exception) {
			throw exception;
		}
		catch (RuntimeException exception) {
			log.error("A ferramenta MCP '{}' falhou inesperadamente.", operation, exception);
			throw new IllegalStateException(
					"Não foi possível concluir a operação devido a uma falha interna. Tente novamente mais tarde. Se o problema persistir, entre em contato com quem administra o servidor.");
		}
	}
}
