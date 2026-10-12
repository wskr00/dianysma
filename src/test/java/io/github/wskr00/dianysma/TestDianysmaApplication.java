package io.github.wskr00.dianysma;

import org.springframework.boot.SpringApplication;

public class TestDianysmaApplication {

	public static void main(String[] args) {
		SpringApplication.from(DianysmaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
