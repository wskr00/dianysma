package io.github.wskr00.dianysma.core.nativeimage;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.internal.exception.FlywaySqlException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlNoDriversForInteractiveAuthException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlNoIntegratedAuthException;
import org.flywaydb.core.internal.exception.sqlExceptions.FlywaySqlServerUntrustedCertificateSqlException;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public final class FlywayNativeHints implements RuntimeHintsRegistrar {

	private static final List<Class<? extends FlywaySqlException>> SQL_EXCEPTIONS = List.of(
			FlywaySqlServerUntrustedCertificateSqlException.class,
			FlywaySqlNoIntegratedAuthException.class,
			FlywaySqlNoDriversForInteractiveAuthException.class);

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		for (Class<? extends FlywaySqlException> exceptionType : SQL_EXCEPTIONS) {
			hints.reflection().registerType(exceptionType, typeHint -> typeHint
					.withMethod("isFlywaySpecificVersionOf", List.of(TypeReference.of(SQLException.class)),
							ExecutableMode.INVOKE)
					.withConstructor(List.of(TypeReference.of(SQLException.class), TypeReference.of(DataSource.class)),
							ExecutableMode.INVOKE));
		}
	}

}
