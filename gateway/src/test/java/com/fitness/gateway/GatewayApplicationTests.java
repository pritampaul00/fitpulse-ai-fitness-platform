// package com.fitness.gateway;

// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest
// class GatewayApplicationTests {

// 	@Test
// 	void contextLoads() {
// 	}

// }



package com.fitness.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://localhost:8181/realms/fitness/protocol/openid-connect/certs"
})
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
