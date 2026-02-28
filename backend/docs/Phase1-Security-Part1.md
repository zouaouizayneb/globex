# Phase 1 – Security Part 1: JWT Authentication

This document describes the JWT authentication implementation in the backend (aligned with the ERP-Ecommerce guide).

---

## Overview

- **Stateless auth:** No server-side sessions; the client sends a JWT on each request.
- **Roles:** `CLIENT`, `ADMIN` (from `Role` enum).
- **Flow:** Register or Login → receive JWT → send `Authorization: Bearer <token>` on protected APIs.

---

## Configuration

In `application.properties`:

```properties
# JWT (use a long random secret in production)
jwt.secret=your-super-secret-key-change-this-in-production-min-256-bits
jwt.expiration=86400000
```

- **jwt.secret:** Used to sign tokens (HS256). Should be at least 32 characters in production; short secrets are hashed to 256 bits internally.
- **jwt.expiration:** Token lifetime in milliseconds (e.g. 86400000 = 24 hours).

---

## Public Endpoints (no JWT)

| Method | Endpoint | Body | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | `RegisterRequest` | Create account (CLIENT only). Returns JWT + user. |
| POST | `/api/auth/login` | `LoginRequest` | Login. Returns JWT + user. |
| GET | `/api/auth/verify-email?token=...` | - | Verify email with token from email. |
| POST | `/api/auth/forgot-password` | `{ "email": "..." }` | Request password reset email. |
| POST | `/api/auth/reset-password` | `{ "token": "...", "newPassword": "..." }` | Set new password with reset token. |

---

## Protected Endpoints (JWT required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/me` | Current user info (same shape as login/register response, `token` null). |

All other APIs under `/api/*` (e.g. `/api/users/**`, `/api/orders/**`) also require a valid JWT unless documented as public.

---

## How to Use the Token

1. **Login or Register**  
   Call `POST /api/auth/login` or `POST /api/auth/register`. Response example:

   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9...",
     "type": "Bearer",
     "userId": 1,
     "username": "jane",
     "email": "jane@example.com",
     "fullname": "Jane Doe",
     "role": "CLIENT",
     "emailVerified": false
   }
   ```

2. **Call protected APIs**  
   Add header to every request:

   ```
   Authorization: Bearer <token>
   ```

3. **Get current user**  
   `GET /api/auth/me` with the same header returns the same user object (with `token: null`).

---

## Error Responses

- **401 Unauthorized**  
  - Missing or invalid `Authorization` header.  
  - Invalid or expired JWT.  
  - Body example: `{ "message": "Invalid token" }` or `{ "message": "Token has expired" }`.

- **400 Bad Request**  
  - Validation errors (e.g. blank username/password, invalid email).  
  - Duplicate username/email on register.

- **409 Conflict**  
  - e.g. Account deactivated.

---

## Components

| Component | Role |
|-----------|------|
| `JwtUtil` | Generate and parse JWT; extract username/role; validate and check expiry. |
| `JwtAuthenticationFilter` | Runs before Spring Security: reads `Authorization: Bearer <token>`, validates token, loads user, sets `SecurityContext`. On invalid/expired token returns 401 JSON. |
| `CustomUserDetailsService` | Loads `User` by username for validation and `SecurityContext`. |
| `SecurityConfig` | Permits only register/login/verify/forgot/reset; all other `/api/auth/**` and rest of `/api/**` require authenticated. |
| `AuthController` | Register, login, verify-email, forgot-password, reset-password, and GET /me. |
| `AuthService` | Business logic for auth and building auth responses. |

---

## JWT Contents

- **Subject (sub):** username.
- **Claim `role`:** e.g. `CLIENT`, `ADMIN`.
- **Issued at (iat)** and **Expiration (exp)** for lifetime.

Role-based access is enforced with `@PreAuthorize("hasRole('ADMIN')")` etc. on controllers.

---

## Testing with Postman/cURL

**Register:**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","fullname":"Test User","email":"test@example.com","password":"password123"}'
```

**Login:**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}'
```

**Get current user:**

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <paste-token-here>"
```

---

This completes **Phase 1 – Security Part 1 (JWT authentication)**. Next: Phase 2 (password reset/email verification flows) and then Phase 3 (E-Commerce core: cart, checkout, etc.).
