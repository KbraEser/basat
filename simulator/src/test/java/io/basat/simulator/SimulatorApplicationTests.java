package io.basat.simulator;

import io.basat.simulator.reading.IngestionClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class SimulatorApplicationTests {

	// the scheduler starts with the context; the mock keeps it from sending readings to a real server
	@MockitoBean
	private IngestionClient ingestionClient;

	@Test
	void contextLoads() {
	}

}
