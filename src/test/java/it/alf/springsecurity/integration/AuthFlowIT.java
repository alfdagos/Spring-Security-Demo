package it.alf.springsecurity.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class AuthFlowIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void fullAuthFlow() throws Exception {
        String base = "http://localhost:" + port;

        // register
        Map<String, String> regReq = Map.of("username", "itest", "email", "itest@example.com", "password", "P@ssw0rd");
        ResponseEntity<Map> regResp = restTemplate.postForEntity(base + "/api/auth/register", regReq, Map.class);
        assert regResp.getStatusCode() == HttpStatus.CREATED;
        String access = (String) regResp.getBody().get("accessToken");
        String refresh = (String) regResp.getBody().get("refreshToken");

        // access protected endpoint with access token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(access);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> protectedResp = restTemplate.exchange(base + "/api/example/protected", HttpMethod.GET, entity, String.class);
        assert protectedResp.getStatusCode() == HttpStatus.OK;

        // refresh
        ResponseEntity<Map> refreshResp = restTemplate.postForEntity(base + "/api/auth/refresh?refreshToken=" + refresh, null, Map.class);
        assert refreshResp.getStatusCode() == HttpStatus.OK;
        String newAccess = (String) refreshResp.getBody().get("accessToken");

        // logout
        ResponseEntity<Void> logoutResp = restTemplate.postForEntity(base + "/api/auth/logout?refreshToken=" + refresh, null, Void.class);
        assert logoutResp.getStatusCode() == HttpStatus.NO_CONTENT;

        // after logout, refresh should fail
        ResponseEntity<String> failed = restTemplate.postForEntity(base + "/api/auth/refresh?refreshToken=" + refresh, null, String.class);
        // expecting 4xx
        assert failed.getStatusCode().is4xxClientError();
    }
}
