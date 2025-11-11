# ARCHITECTURE

This document describes the layered architecture used in the project and the
main design choices related to authentication and persistence.

## Overview

The application follows a classic layered architecture:

- Controller (web layer): exposes REST endpoints and performs basic input
  validation (Bean Validation). It contains minimal business logic.
- Service (business layer): contains application logic, transaction boundaries
  (`@Transactional`) and orchestration of repositories and external components
  (caches, messaging, etc.).
- Repository (data access): interfaces with JPA/Hibernate and the PostgreSQL database.
- Domain (model): JPA entities (`User`, `Role`, `RefreshToken`) and DTOs used by the API.

MapStruct is used for declarative DTO <-> Entity mapping to reduce boilerplate and
keep the API representation separate from the persistence model.

## Authentication flow (JWT + Refresh)

1. Registration (`/api/auth/register`): a new user is created with the `ROLE_USER` role.
   The initial Flyway migration inserts the base roles into the `role` table.
2. Login (`/api/auth/login`): if credentials are valid, the server issues:
   - A short-lived JWT access token (e.g. 15 minutes) containing `sub` and `roles` claims.
   - A refresh token (UUID) stored in the `refresh_token` table with an expiry date.
3. Accessing protected resources: the client sends the access token in the
   `Authorization: Bearer <token>` header. The `JwtAuthenticationFilter` validates
   the token and populates the `SecurityContext` with the user and roles.
4. Refresh exchange (`/api/auth/refresh`): if the refresh token is valid and not
   revoked, a new access token is issued. The refresh token remains persistent until
   expiry or explicit revocation.
5. Logout (`/api/auth/logout`): the refresh token is marked as revoked in the DB so
   it cannot be reused.

This approach keeps the API stateless (authentication via JWT) while allowing server-side
session revocation using persistent refresh tokens.

## Database schema (high level)

- `role` — list of roles (ROLE_USER, ROLE_ADMIN, ...)
- `user` — user entity with username, email, password (BCrypt) and many-to-many
  relationship with `role`
- `refresh_token` — refresh tokens referencing `user`, with expiry and a `revoked` flag

Migrations are managed by Flyway (folder: `src/main/resources/db/migration`).

## Caching

The service layer uses Caffeine for in-memory caching (configured in `application.yml`).
For distributed workloads, replace it with Redis or another shared store.

## Security choices and trade-offs

- Short-lived access tokens + persistent refresh tokens: reduces the risk window
  for compromised access tokens but requires storage and management of refresh tokens.
- MapStruct + DTOs: prevents leaking sensitive fields but requires careful mapping
  tests and reviews.
- BCrypt for password hashing: a sensible default for most use cases.

## Test strategy

- Unit tests for services and components (Mockito)
- End-to-end integration tests with Testcontainers to validate the full flow (DB + Flyway)
- CI: run `mvn clean verify`; jobs that execute integration tests must have Docker available.

## Deployment and operational notes

- Ensure that `JWT_SECRET` is managed securely (secret manager) and has sufficient length.
- Monitor routes and metrics via Actuator and add alerts for suspicious login activity or rate limits.

## Further reading

For advanced scenarios (key rotation, OAuth2/OIDC, Single Sign-On) integrate a
dedicated identity provider or external Identity Provider.
