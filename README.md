# Spring Security Demo

A demo repository providing a solid starting point for REST services secured with
Spring Boot, Spring Security and JWT. The project includes production-oriented
conventions and implementations: stateless JWT authentication, persistent refresh
tokens, roles, Flyway migrations, in-memory caching (Caffeine), OpenAPI and automated tests.

Main features
- Java 21, Spring Boot 3.5.x
- JWT authentication (short-lived access tokens) + persistent refresh tokens
- Spring Data JPA + PostgreSQL + Flyway migrations
- MapStruct for DTO <-> Entity mapping
- Caffeine in-memory cache for service layer
- Actuator and OpenAPI (Swagger UI)
- Dockerfile + docker-compose for local development
- Tests: JUnit 5, Mockito, Testcontainers (Postgres) for integration tests

Prerequisites
- JDK 21
- Maven 3.8+
- Docker (recommended for integration tests and local development)

Quick start (local development)

1) Build locally (without creating a Docker image):

```pwsh
cd 'C:/Git/PROGETTI_ALF/VARI/Spring-Security-Demo'
mvn -DskipTests=false clean package
```

2) Start the development stack with Docker Compose (Postgres):

```pwsh
docker compose up -d --build postgres
# Wait for Postgres to be ready, then start the app (via Dockerfile or run locally)
```

3) Run the app locally (use the `dev` profile):

```pwsh
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

Main endpoints

- POST /api/auth/register — register a new user
- POST /api/auth/login — authenticate and receive access + refresh tokens
- POST /api/auth/refresh — exchange a refresh token for a new access token
- POST /api/auth/logout — revoke a refresh token
- GET /api/users/me — current authenticated user information

Examples (curl)

Register:

```pwsh
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" `
 -d '{"username":"user1","email":"u@example.com","password":"P@ssw0rd"}'
```

Login:

```pwsh
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" `
 -d '{"username":"user1","password":"P@ssw0rd"}'
```

API docs

Swagger UI / OpenAPI is exposed (in `dev`) at `/swagger-ui.html` or `/swagger-ui/index.html`.

Configuration and secrets

- `JWT_SECRET`: HMAC signing key for JWTs; DO NOT commit to the repository. It must be at least 32 bytes for HS256.
- Database credentials and URL: provide via environment variables or use the provided `docker-compose.yml`.

Testing

- Run unit tests locally: `mvn -DskipTests=false test`
- Run full verification (unit + integration if enabled): `mvn -DskipTests=false clean verify`
- For details about tests and Testcontainers, see `TESTING.md`.

Production notes

- Replace `jwt.secret` with a securely managed key (secret manager / KeyVault)
- Enable HTTPS and restrict CORS in production
- Monitor sensitive endpoints using Actuator/metrics

Contributing

Open issues or pull requests. Follow project rules for database changes (add Flyway migrations) and automated tests.
