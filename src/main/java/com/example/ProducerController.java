package com.example;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {
	private final KafkaTemplate<String, String> kafkaTemplate;

	private final String topic;

	public ProducerController(KafkaTemplate<String, String> kafkaTemplate, @Value("${demo.topic}") String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@PostMapping(path = "/")
	public CompletableFuture<String> hello(@RequestBody String message) {
		CompletableFuture<SendResult<String, String>> result = this.kafkaTemplate.send(this.topic, message);
		return result.thenApply(r -> r.getProducerRecord().toString());
	}
}
