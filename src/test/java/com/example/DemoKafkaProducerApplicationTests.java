package com.example;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Import({TestcontainersConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.kafka.consumer.auto-offset-reset=earliest",
		"spring.kafka.consumer.group-id=test"
})
class DemoKafkaProducerApplicationTests {
	@Autowired RestClient.Builder restClientBuildr;

	@Autowired TestListener testListener;

	@LocalServerPort int port;

	RestClient restClient;

	@BeforeEach
	void setUp() throws Exception {
		this.restClient = this.restClientBuildr
				.baseUrl("http://localhost:%d".formatted(port))
				.build();
	}


	@Test
	void contextLoads() throws Exception {
		ResponseEntity<Void> response = this.restClient.post().uri("/")
				.contentType(MediaType.TEXT_PLAIN)
				.body("Hello World!")
				.retrieve()
				.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		if (this.testListener.await(Duration.ofSeconds(5))) {
			assertThat(this.testListener.receivedData()).isEqualTo("Hello World!");
		}
		else {
			fail();
		}
	}

	@TestConfiguration
	static class TestcontainersConfiguration {
		@Bean
		public TestListener testListener() {
			return new TestListener();
		}
	}

	static class TestListener {
		private String data;

		private final CountDownLatch latch = new CountDownLatch(1);

		@KafkaListener(topics = "${demo.topic}")
		public void onMessage(String data) {
			this.data = data;
			this.latch.countDown();
		}

		public String receivedData() {
			return this.data;
		}

		public boolean await(Duration timeout) throws InterruptedException {
			return this.latch.await(timeout.getSeconds(), TimeUnit.SECONDS);
		}
	}

}
