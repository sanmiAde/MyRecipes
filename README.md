# MyRecipes

MyRecipes is a Kotlin + Spring Boot REST API for managing user profiles and cooking recipes, featuring JWT-based authentication, pagination, and Flyway-powered database migrations. It provides endpoints to register/login, manage user accounts, create and browse recipes, and rate them.

## Table of contents
- Features
- Tech stack
- Getting started
  - Prerequisites
  - Running with Docker (PostgreSQL)
  - Running locally (Gradle)
  - Configuration
- API overview
  - Authentication
  - Profile
  - Recipes
- Database & migrations
- Testing
- Project structure
- Troubleshooting

## Features
- JWT authentication with access and refresh tokens
- Secure endpoints (stateless sessions)
- User profile retrieval and update
- Recipe CRUD subset (create, filter by cuisine/status, list by user), and ratings
- Pagination support for listings
- Database migrations with Flyway
- Container-friendly (Docker, docker-compose)

## Tech stack
- Kotlin, Spring Boot
- Spring Web, Spring Security
- Flyway (db/migration)
- PostgreSQL (via docker-compose for local dev)
- Gradle Kotlin DSL
- Testing with Spring WebMvcTest, MockMvc, MockK

## Getting started

### Prerequisites
- JDK 17+
- Docker (for easy database setup) optional
- Gradle Wrapper is included

### Running with Docker (PostgreSQL)
1. Start PostgreSQL:
   docker compose up -d
   This launches a postgres instance with credentials defined in docker-compose.yml.

2. Configure Spring profile (optional):
   By default, application.properties is used. You can enable the dev profile if needed:
   SPRING_PROFILES_ACTIVE=dev

3. Run the app:
   ./gradlew bootRun

### Running locally (Gradle)
If you already have a local PostgreSQL or want to configure your own DB URL, set the relevant properties (see Configuration) and then run:
./gradlew bootRun

To build a runnable JAR:
./gradlew clean build

The application will start by default on http://localhost:8080

### Configuration
Application configuration lives under src/main/resources:
- application.properties (default)
- application-dev.properties (development overrides)

Useful properties you may need to set (either via properties files or environment variables):
- spring.datasource.url=jdbc:postgresql://localhost:5432/MyRecipes
- spring.datasource.username=MyRecipes
- spring.datasource.password=MyRecipes
- spring.jpa.hibernate.ddl-auto=validate (recommended for Flyway)
- spring.flyway.enabled=true

JWT-related properties are configured via code in utils/security (JWTProperties/JWTProcessor). If your project expects properties, set:
- jwt.secret (example; update based on your implementation)
- jwt.access-token-ttl
- jwt.refresh-token-ttl

Note: The security configuration permits unauthenticated access to /api/v1/auth/** and requires authentication for all other endpoints.

## API overview

Base path: /api/v1

The following is a quick overview. Field names and payloads are based on the current source code.

### Authentication
- POST /api/v1/auth/login
  Request: { "username": "john", "password": "secret" }
  Response: 200 OK -> { "username": "john", "accessToken": "...", "refreshToken": "..." }

- POST /api/v1/auth/register
  Request: { "username": "john", "password": "secret", "confirmPassword": "secret", "email": "john@example.com" }
  Response: 201 Created -> AuthenticationResponse (same shape as login)

- POST /api/v1/auth/logout
  Request: { "refreshToken": "..." }
  Response: 200 OK

- POST /api/v1/auth/refresh
  Request: { "refreshToken": "..." }
  Response: 200 OK -> { "accessToken": "...", "refreshToken": "..." }

Errors are returned in a common format (ApiError) with proper HTTP status codes (see GlobalExceptionHandler).

### Profile
Requires Authorization: Bearer <accessToken>

- GET /api/v1/account
  Response: 200 OK -> UserResponse { "username": "...", "email": "..." }

- PUT /api/v1/account
  Request: ProfileRequest { "username": "newname", "email": "new@example.com" }
  Response: 200 OK -> updated UserResponse

### Recipes
Requires Authorization: Bearer <accessToken>

- POST /api/v1/recipes
  Request: RecipeReq { title, description, ingredients, directions, cuisine, status }
  Response: 200 OK -> RecipeResponse

- GET /api/v1/recipes
  Query params (optional): cuisine=<str>, status=<DRAFT|PUBLISHED|ARCHIVED>, page, size
  Response: 200 OK -> PagedResponse<RecipeResponse>

- GET /api/v1/recipes/user/{userId}
  Query params: status=<DRAFT|PUBLISHED|ARCHIVED>, page, size
  Response: 200 OK -> PagedResponse<RecipeResponse>

- POST /api/v1/recipes/{id}/ratings
  Request: RatingReq { rating: Int }
  Response: 200 OK -> RecipeResponse (with updated ratings)

Note: Endpoints are inferred from available controllers/services; consult RecipeController for the exact mapping if changed.

## Database & migrations
- Flyway migration scripts live in src/main/resources/db/migration (V1__*.sql ...)
- On application startup, Flyway applies pending migrations to keep schema up-to-date.
- Default docker-compose Postgres credentials are:
  - user: MyRecipes
  - password: MyRecipes
  - database: MyRecipes

## Testing
Run the test suite:
./gradlew test

There are WebMvc tests covering AuthenticationController and ProfileController, plus service tests. Tests use MockMvc and MockK for isolation.

## Project structure
- src/main/kotlin/com/sanmiade/myrecipes
  - features/authentication: controllers, service interfaces, DTOs, refresh token repo
  - features/profile: account endpoints and services
  - features/recipes: recipe models, controller, service, repositories
  - utils: error model, exception handler, pagination helpers
  - utils/security: JWT filter, processor, properties, Spring Security config
- src/main/resources: application properties and Flyway migrations
- docs: improvement notes
- Dockerfile, docker-compose.yml: containerization support

## Troubleshooting
- 401 Unauthorized on protected endpoints: ensure you set the Authorization header with a valid access token.
- 400 Bad Request on /logout or /refresh: provide refreshToken in the JSON payload.
- Database connection issues: verify docker compose is running and spring.datasource.* properties match your environment.
- Migration errors: ensure spring.jpa.hibernate.ddl-auto is not creating/dropping schemas that conflict with Flyway; use validate or none.

---
If you notice any inconsistencies between this README and the code, prefer the code and open a PR to update the documentation.