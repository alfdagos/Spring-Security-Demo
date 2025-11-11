# Spring Security Demo

Repository demo che offre una base completa per servizi REST protetti con Spring Boot,
Spring Security e JWT. Il progetto contiene implementazioni e convenzioni utilizzabili
in un ambiente reale: autenticazione stateless con JWT, refresh token persistente,
ruoli, Flyway, caching (Caffeine), OpenAPI e test automatizzati.

Caratteristiche principali
- Java 21, Spring Boot 3.5.x
- Autenticazione JWT (access tokens) + refresh tokens persistenti
- Spring Data JPA + PostgreSQL + Flyway migrations
- MapStruct per mapping DTO <-> Entity
- Caffeine cache per layer di servizio
- Actuator e OpenAPI (Swagger UI)
- Dockerfile + docker-compose per sviluppo locale
- Test: JUnit 5, Mockito, Testcontainers (Postgres) per integrazione

Prerequisiti
- JDK 21
- Maven 3.8+
- Docker (consigliato per test d'integrazione e sviluppo)

Quick start (sviluppo locale)

1) Build locale (senza creare immagine Docker):

```pwsh
cd 'C:/Git/PROGETTI_ALF/VARI/Spring-Security-Demo'
mvn -DskipTests=false clean package
```

2) Avviare stack di sviluppo con Docker Compose (Postgres):

```pwsh
docker compose up -d --build postgres
# Attendere che Postgres sia pronto, poi avviare l'app (se si vuole via Dockerfile o eseguire localmente)
```

3) Eseguire l'app localmente (usando profilo dev):

```pwsh
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

Endpoints principali

- POST /api/auth/register — registrazione utente
- POST /api/auth/login — autenticazione e rilascio access+refresh token
- POST /api/auth/refresh — scambio refresh -> nuovo access token
- POST /api/auth/logout — revoca refresh token
- GET /api/users/me — informazioni utente autenticato

Esempi (curl)

Registrazione:

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

Swagger UI / OpenAPI è esposto (in dev) su `/swagger-ui.html` o `/swagger-ui/index.html`.

Configurazione e segreti

- `JWT_SECRET`: la chiave HMAC per i token; NON commitare nel repo. Deve essere >= 32 byte
   per l'algoritmo HS256.
- Database credentials e URL: impostare tramite variabili d'ambiente o usare il
   `docker-compose.yml` fornito.

Testing

- Eseguire tutti i test locali (unit): `mvn -DskipTests=false test`
- Eseguire la verifica completa (unit + integrazione se abilitati): `mvn -DskipTests=false clean verify`
- Per i dettagli sui test e Testcontainers, vedere `TESTING.md`.

Note produzione

- Cambiare `jwt.secret` con una chiave sicura e gestita (secret manager / KeyVault)
- Abilitare HTTPS e limitare CORS in produzione
- Monitorare le rotte sensibili tramite Actuator/metrics

Contribuire

Aprire issue o pull request. Seguire le regole del progetto per le modifiche
al database (aggiungere migration Flyway) e per i test automatici.
