package br.com.bbts.gpe.gpe_backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.junit.jupiter.api.Test;

@SpringBootTest
@Import(GpeBackendApplicationTests.TestJwtConfiguration.class)
class GpeBackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class TestJwtConfiguration {
		@Bean
		JwtDecoder jwtDecoder() {
			return token -> Jwt.withTokenValue(token)
				.header("alg", "none")
				.claim("preferred_username", "test-user")
				.build();
		}
	}

}
