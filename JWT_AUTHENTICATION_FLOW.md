# JWT Authentication Flow for CourseController APIs

## Overview
The authentication system uses **JWT (JSON Web Tokens)** with Spring Security to protect the CourseController APIs. Here's how it works:

---

## 🔄 Complete Authentication Flow

### Step 1: User Login/Signup
```
Client → POST /api/auth/login
         {
           "email": "user@example.com",
           "password": "password123"
         }

AuthService → Validates credentials
            → Generates JWT token using JwtUtil
            → Returns: { "token": "eyJhbGc...", "user": {...} }
```

### Step 2: Client Stores Token
```
Client stores JWT token (typically in localStorage or sessionStorage)
```

### Step 3: Client Makes Authenticated Request
```
Client → GET /api/courses/mine
         Headers:
           Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Step 4: JWT Filter Intercepts Request
**File:** `JwtAuthenticationFilter.java`

```java
1. Extracts token from Authorization header:
   - Looks for "Authorization: Bearer <token>"
   - Removes "Bearer " prefix

2. Validates token:
   - Calls jwtUtil.validateToken(jwt)
   - Verifies signature, expiration, format

3. Extracts user info:
   - email = jwtUtil.getEmailFromToken(jwt)
   - id = jwtUtil.getIdFromToken(jwt)

4. Sets authentication in SecurityContext:
   - Creates UsernamePasswordAuthenticationToken
   - Stores email as principal
   - Sets authentication in SecurityContextHolder
```

### Step 5: Spring Security Checks Authorization
**File:** `SecurityConfig.java`

```java
SecurityFilterChain checks:
- Is endpoint protected? (/api/courses/** → YES)
- Is user authenticated? (Check SecurityContext)
- If authenticated → Allow request
- If not authenticated → Return 401 Unauthorized
```

### Step 6: Controller Executes
**File:** `CourseController.java`

```java
@GetMapping("/mine")
public ResponseEntity<List<CourseDTO>> getMyCourses() {
    // Gets current user ID from SecurityContext
    Long userId = authService.getCurrentUserId();
    
    // Uses userId to fetch courses
    List<Course> courses = courseService.getAllByUserId(userId);
    ...
}
```

### Step 7: AuthService Retrieves User
**File:** `AuthServiceImpl.java`

```java
public Long getCurrentUserId() {
    // Gets authentication from SecurityContext
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
    // Extracts email (set by JwtAuthenticationFilter)
    String email = auth.getName();
    
    // Looks up user in database
    User user = userService.findByEmail(email);
    
    return user.getId();
}
```

---

## 🔐 Key Components

### 1. JwtUtil (`edtech-db/src/main/java/com/task/edtech/db/security/JwtUtil.java`)
- **generateToken(email, id)**: Creates JWT with email as subject and id as claim
- **validateToken(token)**: Verifies token signature and expiration
- **getEmailFromToken(token)**: Extracts email from token
- **getIdFromToken(token)**: Extracts user ID from token

### 2. JwtAuthenticationFilter (`edtech-application/src/main/java/com/task/edtech/services/security/JwtAuthenticationFilter.java`)
- Extends `OncePerRequestFilter` (runs once per request)
- Extracts JWT from `Authorization: Bearer <token>` header
- Validates token and sets authentication in SecurityContext
- Runs **before** Spring Security's authentication filter

### 3. SecurityConfig (`edtech-application/src/main/java/com/task/edtech/services/security/SecurityConfig.java`)
- Configures which endpoints require authentication
- `/api/courses/**` → **Requires authentication**
- `/api/auth/signup`, `/api/auth/login` → **Public**
- Adds `JwtAuthenticationFilter` to filter chain

### 4. AuthService (`edtech-db/src/main/java/com/task/edtech/db/service/impl/AuthServiceImpl.java`)
- **getCurrentUserId()**: Retrieves authenticated user's ID from SecurityContext
- Uses `SecurityContextHolder.getContext().getAuthentication()`
- Extracts email and looks up user in database

---

## 📋 Request Flow Diagram

```
┌─────────┐
│ Client  │
└────┬────┘
     │
     │ 1. Request with JWT token
     │    Authorization: Bearer <token>
     ▼
┌─────────────────────────┐
│ JwtAuthenticationFilter │
│  - Extract token        │
│  - Validate token       │
│  - Set SecurityContext  │
└────┬────────────────────┘
     │
     │ 2. Check authorization
     ▼
┌─────────────────────────┐
│   SecurityConfig        │
│  - Is endpoint protected?│
│  - Is user authenticated?│
└────┬────────────────────┘
     │
     │ 3. If authenticated
     ▼
┌─────────────────────────┐
│   CourseController      │
│  - authService.getCurrentUserId()│
│  - Process request      │
└────┬────────────────────┘
     │
     │ 4. Return response
     ▼
┌─────────┐
│ Client  │
└─────────┘
```

---

## 🛡️ Security Features

1. **Stateless Authentication**: No server-side sessions (JWT contains all info)
2. **Token Expiration**: Tokens expire after 24 hours (configurable)
3. **Signature Verification**: Tokens are signed with HMAC-SHA256
4. **Protected Endpoints**: All `/api/courses/**` endpoints require authentication
5. **Public Endpoints**: Signup, login, and search are publicly accessible

---

## 🔧 Configuration

### JWT Secret Key
Set in `application.properties`:
```properties
app.jwt.secret=your-secret-key-minimum-256-bits-for-hmac-sha-algorithms-change-this-in-production
app.jwt.expiration=86400000  # 24 hours in milliseconds
```

### CORS Configuration
Allows requests from:
- `http://localhost:3000`
- `http://localhost:5173`

---

## 📝 Example API Calls

### 1. Login (Get Token)
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": { ... }
}
```

### 2. Create Course (Authenticated)
```bash
POST /api/courses
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "Java Basics",
  "description": "Learn Java",
  ...
}
```

### 3. Get My Courses (Authenticated)
```bash
GET /api/courses/mine
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## ⚠️ Important Notes

1. **Token Storage**: Client must store JWT token securely (localStorage/sessionStorage)
2. **Token Expiration**: Client should handle token expiration and refresh
3. **HTTPS in Production**: Always use HTTPS to protect tokens in transit
4. **Secret Key**: Change default JWT secret in production!

