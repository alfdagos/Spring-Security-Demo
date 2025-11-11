# TESTING

Questo documento descrive la strategia di testing del progetto, come eseguire i test
in locale (unit e integrazione), come abilitare i test d'integrazione basati su
Testcontainers/Postgres, e come risolvere i problemi più comuni.

## Indice
- Scopo e contesto
- Tipi di test presenti
- Eseguire i test unitari (veloce)
- Eseguire i test d'integrazione (Testcontainers)
- Eseguire i test usando Docker Compose (DB esterno)
- Variabili d'ambiente importanti per i test
- Troubleshooting: problemi comuni e soluzioni

## Scopo e contesto

Questo progetto è un demo applicativo Spring Boot che mostra:

- Autenticazione con JWT (access token) e refresh token persistenti
- Ruoli e autorizzazioni (ROLE_USER, ROLE_ADMIN, ROLE_MANAGER)
- Persistenza su PostgreSQL con Flyway per le migrazioni
- Caching (Caffeine)
- Documentazione OpenAPI, Actuator, Logback con MDC

I test sono organizzati in:

- Unit tests: veloci, senza dipendenze esterne (mocking con Mockito)
- Integration tests (E2E): verificano il flusso HTTP completo usando Testcontainers
  (avviano un container PostgreSQL e applicano Flyway)

## Tipi di test presenti

- `it.alf.springsecurity.*Test` — Unit test per servizi, utilità e filtri
- `it.alf.springsecurity.integration.*` — Test d'integrazione end-to-end (Testcontainers)

## Eseguire i test unitari (veloce)

Per eseguire solo i unit test (rapido, non richiede Docker):

```pwsh
cd 'C:/Git/PROGETTI_ALF/VARI/Spring-Security-Demo'
mvn -DskipTests=false -Dtest="**/*Test" test
```

Nel progetto alcuni test di integrazione placeholder sono disabilitati (annotazione
`@Disabled`) per default: prima di abilitare gli IT, leggere la sezione seguente.

## Eseguire i test d'integrazione (Testcontainers)

I test d'integrazione presenti utilizzano Testcontainers per avviare un Postgres
temporaneo e permettere l'esecuzione delle migrazioni Flyway. Requisiti:

- Docker in esecuzione sulla macchina di sviluppo
- Almeno 2GB liberi per i container temporanei

Per abilitare e lanciare gli IT:

1. Rimuovere o commentare `@Disabled` dal file `src/test/java/it/alf/springsecurity/integration/AuthIntegrationTest.java`
2. Eseguire i test d'integrazione con Maven:

```pwsh
cd 'C:/Git/PROGETTI_ALF/VARI/Spring-Security-Demo'
mvn -DskipTests=false -Dgroups=integration test
# oppure solo tutti i test (unit + integration)
mvn -DskipTests=false clean verify
```

Note:

- I Testcontainers imposteranno automaticamente le proprietà `spring.datasource.*`
  tramite `DynamicPropertySource`, quindi non è necessaria configurazione aggiuntiva.
- Se si preferisce usare una DB esterno (docker-compose), vedere la sezione successiva.

## Eseguire i test usando Docker Compose (DB esterno)

In alternativa ai Testcontainers, è possibile avviare un Postgres locale tramite
`docker-compose.yml` fornito nel progetto e puntare l'applicazione a quel DB.

1. Avviare i servizi richiesti:

```pwsh
cd 'C:/Git/PROGETTI_ALF/VARI/Spring-Security-Demo'
docker compose up -d postgres
```

2. Modificare il profilo di test o impostare le seguenti variabili d'ambiente
   prima di eseguire i test (esempio PowerShell):

```pwsh
$env:SPRING_PROFILES_ACTIVE='test'
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/testdb'
$env:SPRING_DATASOURCE_USERNAME='postgres'
$env:SPRING_DATASOURCE_PASSWORD='postgres'
mvn -DskipTests=false clean verify
```

## Variabili d'ambiente importanti per i test

- `JWT_SECRET` / `jwt.secret`: la chiave HMAC usata nei test deve essere lunga almeno
  32 byte (per HS256). Nel `application.yml` è presente un default lungo per i test
  locali. In produzione fornire la chiave tramite variabile d'ambiente o secret manager.
- `SPRING_PROFILES_ACTIVE`: attiva il profilo (`dev`, `test`, `prod`)
- DB URL/credentials (se non si usa Testcontainers)

## Troubleshooting: problemi comuni e soluzioni

- WeakKeyException (JJWT): se l'app fallisce all'avvio con una eccezione del tipo
  "The specified key byte array is 128 bits which is not secure enough...", configurare
  una chiave JWT più lunga (>32 byte) attraverso `JWT_SECRET` o nel `application.yml`.
- Flyway/DB connection refused: i test d'integrazione e Flyway richiedono accesso
  al DB. Se si esegue senza Docker/Testcontainers, assicurarsi che Postgres sia
  in ascolto sulla porta corretta (es. 5433 nel profilo di test) oppure usare
  `docker compose up -d postgres`.
- Testcontainers non parte: verificare che Docker Desktop sia in esecuzione e che
  la versione del driver (Testcontainers) sia compatibile con la versione di Docker.
- Test che cercano `ROLE_USER`: alcuni metodi di registration si aspettano che il ruolo
  `ROLE_USER` sia presente in DB (lo script Flyway iniziale inserisce i ruoli).

## Note per CI (GitHub Actions)

La pipeline CI configurata nel repository usa Maven per eseguire `clean verify` e può
essere estesa per eseguire i test d'integrazione con Testcontainers (richiede runner
che supporti Docker). In alternativa usare servizi Postgres ospitati per i job CI.

## Contatti e riferimento

Per problemi specifici relativi ai test o all'ambiente locale, includere nei report
di failure i log Maven (opzione `-e` o `-X`) e l'output del container Docker (se
usato). Il file `ARCHITECTURE.md` fornisce ulteriori dettagli sul flusso di autenticazione
e sulle scelte progettuali utili per il debug.
# TESTING

Il progetto integra unit test (JUnit 5 + Mockito) e integration tests con Testcontainers.

Comandi utili:

- Eseguire tutti i test: `mvn test`
- Verifica completa: `mvn -DskipTests=false clean verify`

Testcontainers
- I test d'integrazione avviano un container PostgreSQL e usano il profilo `test`.

Coverage
- JaCoCo è configurato; il job CI fallisce se la coverage scende sotto soglia (impostare nella pipeline).
