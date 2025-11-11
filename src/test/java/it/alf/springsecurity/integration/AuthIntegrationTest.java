package it.alf.springsecurity.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Integration tests require Testcontainers/Postgres; enable when running locally with Docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Test
    void contextLoads() {
        // Integration tests with Testcontainers should be added here.
        // For brevity provide the skeleton; full test performs register->login->access protected endpoint->refresh->logout.
    }
}
