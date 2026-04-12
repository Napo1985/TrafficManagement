# Tech Stack Definition

| Concern | Choice | Notes |
|---------|--------|--------|
| Language / runtime | **Java 21 (LTS)** | Matches “latest LTS” spirit; pin the exact JDK in `pom.xml` / `build.gradle` when implementing. |
| Framework | **Spring Boot 3.x** | Web MVC by default (or WebFlux later if you want reactive-only; MVC is simpler for redirects + JPA). |
| API | **Spring Web** | REST controllers; `RedirectView` / `ResponseEntity` for **302** on `GET /{slug}`. |
| Persistence | **Spring Data JPA** + **PostgreSQL** | Entities + repositories. |
| Migrations | **Flyway** (recommended) | Versioned SQL or Java migrations; common Spring pairing. |
| Cache | **Spring Data Redis** | Cache abstraction or `RedisTemplate` for slug → `targetUrl` on the hot path. |
| Validation | **`jakarta.validation` (Bean Validation)** + DTOs | Validate request bodies for link creation. |
| Testing | **JUnit 5**, **Spring Boot Test**, **Testcontainers** (PostgreSQL + Redis) | Use `@SpringBootTest` / `@WebMvcTest` / sliced tests as appropriate. |
| Logging | **Logback** (Spring Boot default) or **Log4j2** | Optional starter switch; default Logback is fine. |
| Build | **Maven** (default here) | **Gradle** is equally valid; pick one when implementing and stick to it. |

Optional later (not required for the first implementation): **Spring Security** + JWT when you add auth; **Micrometer** for metrics.
