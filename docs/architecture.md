# System Architecture

## Folder Structure

```
/src
  /api          # Routes and Controllers
  /services     # Business logic (Slug generation, Analytics)
  /repositories # Database abstraction (Prisma)
  /cache        # Redis logic
  /middlewares  # Validation, Bot detection, Error handling
  /utils        # Helpers (Base62, URL validation)
  /config       # Environment variables
```

## Redirection Logic (The Hot Path)

1. Request hits `GET /:slug`.
2. Middleware checks for bots.
3. Service checks Redis for the slug.
4. If not in Redis, fetch from PostgreSQL and prime Redis.
5. Push click data to a background queue/task.
6. Return 302 Redirect immediately.

## Database Schema (Prisma)

- **Link:** `id` (UUID), `slug` (Unique), `targetUrl`, `source`, `createdAt`.
- **Click:** `id` (UUID), `linkId` (FK), `timestamp`, `ip`, `userAgent`, `isBot`.
