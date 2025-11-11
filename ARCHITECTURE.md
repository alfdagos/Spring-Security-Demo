# ARCHITECTURE

Questo documento descrive l'architettura a livelli usata nel progetto.


Consigli di sicurezza

# ARCHITECTURE

Questo documento spiega l'architettura dell'applicazione, le scelte principali e il
flusso di autenticazione. È pensato per dare contesto a chi vuole estendere o
manutenere il progetto.

## Panoramica a livelli

L'app è organizzata secondo un'architettura a livelli classica:

- Controller (web layer): espone gli endpoint REST e si occupa di validazione base
	degli input (Bean Validation). Non contiene logica di business complessa.
- Service (business layer): ospita la logica applicativa, transazioni (`@Transactional`)
	e orchestrazione di repository e componenti esterni (cache, messaging, ecc.).
- Repository (data access): interfaccia con JPA/Hibernate e il database PostgreSQL.
- Domain (model): entità JPA (`User`, `Role`, `RefreshToken`) e DTO per le API.

MapStruct viene usato per mappare in modo dichiarativo DTO <-> Entity; questo
riduce boilerplate e mantiene chiara la separazione tra rappresentazione API e
modello di persistenza.

## Flusso di autenticazione (JWT + Refresh)

1. Registrazione (`/api/auth/register`): l'utente viene creato con `ROLE_USER`.
	 Lo script Flyway iniziale inserisce i ruoli di base nella tabella `role`.
2. Login (`/api/auth/login`): se le credenziali sono valide, il server rilascia:
	 - Un access token JWT a breve durata (es. 15 minuti) contenente claim `sub` e `roles`.
	 - Un refresh token (stringa UUID) memorizzato in tabella `refresh_token` con expiry.
3. Accesso a risorse protette: il client invia l'access token via header
	 `Authorization: Bearer <token>`. Un filtro (`JwtAuthenticationFilter`) valida
	 il token e popola il `SecurityContext` con l'utente/ruoli.
4. Scambio refresh (`/api/auth/refresh`): se il refresh token è valido e non revocato,
	 viene emesso un nuovo access token; il refresh token è persistente fino alla sua
	 scadenza o revoca.
5. Logout (`/api/auth/logout`): il refresh token viene marcato come revocato nel DB
	 così non può essere riutilizzato.

Questa strategia mantiene le API stateless (autenticazione via JWT) ma consente
di revocare sessioni lato server usando i refresh token persistenti.

## Schema DB (high level)

- `role` — lista dei ruoli (ROLE_USER, ROLE_ADMIN, ...)
- `user` — utente con username, email, password (BCrypt) e relazione many-to-many con `role`
- `refresh_token` — token di refresh con riferimento a `user`, expiry e flag `revoked`

Le migrazioni sono gestite con Flyway (cartella `src/main/resources/db/migration`).

## Caching

Il livello di servizio usa Caffeine per cache in-memory (configurato nel `application.yml`).
Per carichi distribuiti si può sostituire con Redis o altri store condivisi.

## Scelte di sicurezza e trade-offs

- Token breve durata vs. refresh persistente: diminuisce la finestra d'attacco per
	access token compromessi ma richiede storage e gestione dei refresh token.
- MapStruct + DTO: evita leakage di campi sensibili ma richiede attenzione nelle mappature
	(test e revisione delle mappature).
- BCrypt per password hashing: buona scelta out-of-the-box per la maggior parte dei casi.

## Test strategy

- Unit tests per servizi e componenti (Mockito)
- Integration tests E2E con Testcontainers per verificare il flusso autentico (DB + Flyway)
- CI: eseguire `mvn clean verify`; i job che eseguono IT devono avere Docker disponibile.

## Operazioni di deployment e note operative

- Assicurarsi che `JWT_SECRET` sia gestito in modo sicuro (secret manager) e abbia
	lunghezza adeguata.
- Monitorare rotte e metriche via Actuator e integrare alerting per rate-limit /
	tentativi di login sospetti.

## Ulteriori riferimenti

Per implementazioni avanzate (rotazione chiavi, OAuth2/OIDC, Single Sign-On) usare
provider dedicati e integrare con un Identity Provider esterno.
