# MyRecipes Project Improvement Report

This report documents a thorough review of the project, with prioritized recommendations. No code changes were made at the time of review; this document aggregates findings and suggestions across architecture, domain model, persistence/migrations, services, web layer, security/JWT, exception handling, configuration/build, Docker, testing, and performance/DX.

Last updated: 2025-09-18

## Quick Wins (Low effort, high impact)
- Fix RecipeRepository search to handle nullable filters (dynamic query)
- Add totalPages and totalElements to PagedResponse
- Store Recipe.status as STRING (EnumType.STRING) and align DB column type
- Populate authorities in UserPrincipal and include proper username claim in JWT
- Add indexes: recipes(user_entity_id), ratings(recipe_id), optional lower(cuisine)
- Tighten validation and NOT NULL constraints in schema

## Domain Model and DTOs
### RecipeEntity
- Use @Enumerated(EnumType.STRING) for Status to avoid ordinal pitfalls. Update schema to VARCHAR accordingly. Currently DB uses SMALLINT (V4), so ordinals are stored; reordering enum breaks data.
- Column sizes: description, ingredients, directions, cuisine are VARCHAR(255). For real content, 255 is insufficient. Consider TEXT for description/ingredients/directions; keep cuisine shorter and possibly constrain.
- Naming: ratingEntity -> ratings for clarity.
- Average rating calculation now loads all ratings into memory; for large lists, consider denormalized average and count on RecipeEntity or use aggregate JPQL.

### RatingEntity
- Rating enum stored as STRING (V9 migration). Keep @Enumerated(EnumType.STRING).
- No link to the rater user; users could rate multiple times and you can’t show "my rating." Consider adding raterUserId (FK to users) with unique (recipe_id, rater_user_id). Update API to upsert rating per user.
- Consider removing Rating.NONE from persisted values and represent absence by missing row.

### DTOs
- RecipeResponse only has average rating. Consider adding ratingCount and userRating (if you add per-user ratings) to enrich UX.
- RecipeReq allows clients to set status. If you want workflow control, default to DRAFT on create and expose publish endpoint.

## Repositories and Queries
- RecipeRepository.findRecipeEntitiesByCuisineIgnoreCaseAndStatus(cuisine: String?, status: Status?, pageable): With null params, Spring Data derives comparisons to null, returning no rows. Your controller treats them as optional. Replace with dynamic criteria or custom @Query using (:param IS NULL OR ...) pattern, e.g.:
  @Query("SELECT r FROM RecipeEntity r WHERE (:cuisine IS NULL OR LOWER(r.cuisine)=LOWER(:cuisine)) AND (:status IS NULL OR r.status = :status)")
- Add DB indexes:
  - recipes(user_entity_id)
  - ratings(recipe_id)
  - Optionally, function index on lower(cuisine) or store normalized cuisine and index.

## Services and Business Logic
- Transactions: Keep @Transactional; use readOnly = true for read methods where possible.
- rateRecipe: no check to prevent duplicate ratings per user or prevent rating own recipe (if desired). If you add raterUserId, enforce uniqueness and adjust logic to update existing rating.
- getRecipesBy currently relies on the repository method that mishandles null filters; fix repository first.

## Web/Controllers
- Validation on RecipeReq is good. Consider adding @Size constraints (e.g., title length) and domain rules for cuisine.
- POST /api/v1/recipes: return 201 Created with a Location header pointing to the new resource.
- GET /api/v1/recipes/search: Decide and document behavior when both cuisine and status are null. Likely return all SHARED recipes. Ensure repository supports this.
- Pagination: PagedResponse lacks totalPages and totalElements; add them for better client UX.

## Security and JWT
- JWTProcessor sets claim USER_NAME_KEY to userId string; likely bug. Should carry the actual username when issuing tokens. Consider also including roles to avoid DB lookup per request.
- UserPrincipal.getAuthorities() returns emptyList(); you lose roles. If you include roles in JWT, populate authorities in JWTAuthenticationFilter from claims. Alternatively, load roles from DB in a custom filter (costly).
- Add issuer/audience claims and verify them. Consider leeway for clock skew.
- CORS is globally disabled in SecurityConfig; for front-end clients, enable CORS properly.

## Exception Handling
- GlobalExceptionHandler is solid. Add handlers for IllegalArgumentException and EntityNotFoundException with 400/404 respectively (e.g., rateRecipe not found should become 404, not 500).
- Add ConstraintViolationException handling for validation on query/path params.

## Persistence and Migrations
- Ensure migration history consistency; V7 changed recipe id to identity, V8/V9 normalized ratings and changed ratings.data to VARCHAR.
- If switching Recipe.status to STRING, add a migration to convert SMALLINT ordinals to names and change column type.
- Add NOT NULL and sensible defaults: title NOT NULL, status default 'DRAFT', etc.
- Consider ON DELETE CASCADE for ratings.recipe_id to match orphanRemoval=true.

## Configuration and Build
- build.gradle.kts includes both data-jdbc and data-jpa; remove data-jdbc if unused to avoid autoconfig overlap.
- Align Kotlin plugin versions (noarg 1.9.10 vs Kotlin 1.9.25). Consider applying kotlin("plugin.allopen") if needed (already configured via allOpen block).
- Logging: good for dev; ensure prod profile reduces to INFO and externalizes secrets.

## Docker
- docker-compose.yml improvements:
  - Use explicit image tag (e.g., postgres:16-alpine)
  - Add named volume for persistence
  - Add healthcheck
  - Optionally add app service for full dockerized dev

## Testing
- Expand tests substantially:
  - AuthenticationServiceTests: register, login success/failure, refresh, logout
  - RecipeServiceTests: createRecipe, getRecipesByUserId pagination, getRecipesBy with null filters, rateRecipe constraints and averaging
  - Controller tests (MockMvc): validation, security, success paths
  - JWTProcessor tests: issue/verify, expiry, malformed tokens
  - Repository tests: dynamic query behavior

## Performance and Developer Experience
- Avoid N+1 rating issue: batch fetch or denormalize rating aggregates
- Enable response compression and consider ETags for lists
- Add OpenAPI/Swagger via springdoc-openapi for API discoverability

## Longer-Term Improvements
- Add user reference to RatingEntity and enforce unique rating per user per recipe
- Denormalize rating average/count on RecipeEntity
- Introduce publish/unpublish workflow with visibility rules
- Add comprehensive integration tests and API documentation

---

If you want, I can implement specific items from this report. Suggested starting sequence for implementation:
1) Fix repository search and add pagination metadata.
2) Correct JWT username claim and authorities handling.
3) Switch Recipe.status to STRING with a migration.
4) Add indexing and validation constraints.
5) Expand tests.



## Why for each improvement

Quick Wins (Low effort, high impact)
- Fix RecipeRepository search to handle nullable filters (dynamic query)
  - Why: Current derived method compares parameters to NULL, returning no rows when filters are omitted. Dynamic conditions ensure predictable behavior and align with controller’s optional params.
- Add totalPages and totalElements to PagedResponse
  - Why: Clients need full pagination context to render UIs (e.g., disable next/prev, show totals) without extra requests.
- Store Recipe.status as STRING (EnumType.STRING) and align DB column type
  - Why: Ordinal storage breaks when enum order changes; string mapping is resilient and self-describing for DB admins and migrations.
- Populate authorities in UserPrincipal and include proper username claim in JWT
  - Why: Without authorities you can’t authorize by role. Incorrect username claim prevents consistent identity display and auditing.
- Add indexes: recipes(user_entity_id), ratings(recipe_id), optional lower(cuisine)
  - Why: Speeds up common lookups (by owner, by recipe) and case-insensitive cuisine filtering at scale.
- Tighten validation and NOT NULL constraints in schema
  - Why: Prevents bad data at the boundary, reduces runtime errors, and improves data integrity.

Domain Model and DTOs
- RecipeEntity: Use @Enumerated(EnumType.STRING) for Status
  - Why: Avoids data corruption on enum reordering; improves readability in DB.
- RecipeEntity: Increase column sizes/use TEXT where appropriate
  - Why: Real recipes exceed 255 chars; prevents truncation errors and validation friction.
- RecipeEntity: Rename ratingEntity -> ratings
  - Why: Improves code clarity and aligns with Kotlin naming conventions for collections.
- Recipe average rating strategy
  - Why: Loading all ratings per recipe does not scale and causes N+1 issues. Aggregation/denormalization reduces query load and latency.
- RatingEntity: Keep EnumType.STRING
  - Why: Same enum-safety reasons as above; matches V9 migration.
- RatingEntity: Add raterUserId unique per recipe (future)
  - Why: Enforces one rating per user, enables “my rating,” and prevents spam/duplicate ratings.
- Rating.NONE persistence policy
  - Why: Absence is better represented by no row; simplifies averages and avoids special-case filtering.
- DTOs: Add ratingCount and userRating (future)
  - Why: Enhances UX with context (how many ratings) and personalization (my rating).
- DTOs: Default new recipes to DRAFT in create flow
  - Why: Prevents accidental publication and formalizes a safer workflow.

Repositories and Queries
- Replace derived search with @Query using (:param IS NULL OR ...)
  - Why: Correctly supports optional filters in a single index-friendly query.
- Add DB indexes on foreign keys and filter columns
  - Why: Orders-of-magnitude performance gains for common queries; standard relational best practice.

Services and Business Logic
- Use readOnly transactions for queries
  - Why: Reduces overhead, hints to JPA provider to optimize, and can avoid unnecessary dirty checks.
- rateRecipe checks (duplicate/self-rating) if adding raterUserId
  - Why: Maintains fair ratings and prevents abuse; predictable behavior for users.
- Fix getRecipesBy dependency on broken repository method
  - Why: Ensures API semantics match expectations for optional filters.

Web/Controllers
- Add @Size and domain validations
  - Why: Produces better error messages for clients and enforces business rules early.
- POST /recipes returns Location header
  - Why: REST best practice for discoverability and idempotency; clients can immediately fetch the created resource.
- GET /recipes/search behavior when filters are absent
  - Why: Predictable default (e.g., SHARED only) avoids surprising empty results and reduces client-side conditionals.
- Add totalPages/totalElements to PagedResponse
  - Why: Complete pagination metadata improves client UX and reduces network roundtrips.

Security and JWT
- Correct username claim and include roles in JWT (or hydrate authorities)
  - Why: Ensures accurate identity propagation and efficient authorization without extra DB hits per request.
- Add issuer/audience and clock skew leeway
  - Why: Hardens tokens against misuse and improves interoperability in distributed systems.
- Enable CORS properly instead of disabling
  - Why: Securely supports browser clients while avoiding blanket disable that can hide issues.

Exception Handling
- Add explicit handlers for IllegalArgumentException and EntityNotFoundException
  - Why: Maps common errors to correct HTTP codes (400/404) and improves client debuggability.
- Handle ConstraintViolationException for query/path params
  - Why: Produces consistent validation error responses beyond @Valid body fields.

Persistence and Migrations
- Migration to switch status to STRING with data conversion
  - Why: Maintains existing data while improving mapping safety; avoids downtime due to mismatched types.
- NOT NULL defaults and sensible defaults
  - Why: Data integrity at the database level, not only in app code; safer across services.
- ON DELETE CASCADE for ratings -> recipes
  - Why: Prevents orphan data and simplifies cleanup when recipes are deleted.

Configuration and Build
- Remove data-jdbc if unused (keep JPA)
  - Why: Reduces classpath noise, speeds startup, avoids confusing autoconfiguration overlaps.
- Align Kotlin plugin versions
  - Why: Eliminates potential build-time incompatibilities and subtle tooling issues.
- Tame logging in production profile
  - Why: Reduces noise and risk of sensitive info leakage; lowers operational costs.

Docker
- Explicit Postgres tag, named volume, healthcheck
  - Why: Reproducible builds, data persistence across restarts, and reliable service readiness checks.
- Optional app service in compose
  - Why: One-command dev environment, better onboarding and parity.

Testing
- Broaden unit/integration tests across auth, recipes, JWT, and repositories
  - Why: Prevents regressions, documents behavior, and increases confidence for refactors.

Performance and Developer Experience
- Avoid N+1 by batching/denormalizing
  - Why: Significant performance improvements for list endpoints and better DB efficiency.
- Response compression and ETags
  - Why: Reduced bandwidth and faster perceived performance; cache-friendly APIs.
- Add OpenAPI/Swagger
  - Why: Self-documenting APIs improve DX, client generation, and onboarding.

Longer-Term Improvements
- Per-user ratings and unique constraint
  - Why: Fairness, abuse prevention, and richer features like “your rating”.
- Denormalize rating aggregates
  - Why: O(1) retrieval of averages/counts; scales to large datasets.
- Publish/unpublish workflow
  - Why: Clear content lifecycle that separates drafting from sharing; safer collaboration.
- Comprehensive integration tests and API docs
  - Why: End-to-end confidence and easier external integration.
