# System Architecture

## Package layout (Spring Boot)

Typical layout under `src/main/java/...`:

```
.../TrafficApplication.java          # @SpringBootApplication entry point
.../api/ or .../web/                # REST controllers (thin)
.../service/                        # Business logic (slug generation, redirect orchestration, async click handling)
.../repository/                     # Spring Data JPA interfaces
.../domain/ or .../model/           # JPA entities (Link, Click)
.../config/                         # Redis, async (@EnableAsync), CORS, rate limiting beans
.../web/ (filters, advice)          # Bot UA: OncePerRequestFilter; errors: @ControllerAdvice (or a dedicated security package)
```

Spring idioms replace a separate “middlewares” folder: **filters** for cross-cutting request logic, **`@ControllerAdvice`** (or RFC 7807 `ProblemDetail`) for consistent API errors, and **services** for orchestration.

## Redirection logic (the hot path)

1. Request hits **`GET /{slug}`** (Spring Web MVC handler).
2. A **filter** (e.g. `OncePerRequestFilter`) detects bot user agents before heavy work.
3. **Service** resolves the slug: check **Redis** first (Spring Data Redis / cache abstraction).
4. On cache miss, load from **PostgreSQL** via a **repository**, then **prime Redis**.
5. Enqueue or **@Async** persist **click** analytics so the redirect is not blocked.
6. Return **302** immediately (`RedirectView` / `ResponseEntity` with `HttpStatus.FOUND`).

## Persistence model (JPA entities)

- **Link:** `id` (UUID), `slug` (unique), `targetUrl`, `source`, `createdAt`.
- **Click:** `id` (UUID), `link` / `linkId` (FK to Link), `timestamp`, `ip`, `userAgent`, **`referrer`** (aligns with analytics in [requirements.md](requirements.md)), `isBot`.

Flyway migrations own the physical schema; entities stay in sync with those scripts.
