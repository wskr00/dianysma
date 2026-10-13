package io.github.wskr00.dianysma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

import io.github.wskr00.dianysma.core.nativeimage.FlywayNativeHints;
import io.github.wskr00.dianysma.core.nativeimage.HibernateNativeHints;
import io.github.wskr00.dianysma.core.nativeimage.McpNativeHints;

@SpringBootApplication
@ImportRuntimeHints({ FlywayNativeHints.class, HibernateNativeHints.class, McpNativeHints.class })
public class DianysmaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DianysmaApplication.class, args);
	}

}
