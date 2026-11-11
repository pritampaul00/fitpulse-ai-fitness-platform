// package com.fitness.aiservice;

// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest
// class AiserviceApplicationTests {

// 	@Test
// 	void contextLoads() {
// 	}

// }





package com.fitness.aiservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "rabbitmq.queue.name=test-activity-queue",
    "rabbitmq.exchange.name=test-activity-exchange",
    "rabbitmq.routing.key=test-activity-routing-key",
    "groq.api.url=https://api.groq.com/openai/v1/chat/completions",
    "groq.api.key=test-api-key",
    "groq.api.model=openai/gpt-oss-20b",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "spring.rabbitmq.listener.simple.auto-startup=false",
    "spring.rabbitmq.listener.direct.auto-startup=false"
})
class AiserviceApplicationTests {

    @Test
    void contextLoads() {
    }
}
