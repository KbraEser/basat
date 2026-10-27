package io.basat.monitoring;

import org.springframework.boot.SpringApplication;

public class TestMonitoringServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(MonitoringServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
