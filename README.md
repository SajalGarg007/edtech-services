# EdTech Course Platform - Architecture & Build Guide

## 📋 Project Overview

EdTech Services is a Spring Boot REST API backend for an EdTech course platform. It provides APIs for course providers to create and manage courses, and for learners to search for courses by PIN code. The system uses JWT-based authentication and supports role-based access control (PROVIDER and LEARNER roles).

---

## 📁 Project Structure

### Multi-Module Maven Project

```
edtech.services/
├── pom.xml                          # Parent POM
├── README.md                        # This file
├── JWT_AUTHENTICATION_FLOW.md      # JWT authentication documentation
│
├── edtech-db/                       # Database Layer Module
│   ├── pom.xml
│   └── src/main/java/com/task/edtech/db/
│       ├── converter/               # Entity-DTO converters
│       │   ├── CourseConverter.java
│       │   └── UserConverter.java
│       ├── dto/                     # Data Transfer Objects
│       │   ├── AuthResponse.java
│       │   ├── CourseDTO.java
│       │   ├── LoginRequest.java
│       │   ├── SearchFilters.java
│       │   ├── SignupRequest.java
│       │   └── UserDTO.java
│       ├── entity/                  # JPA Entities
│       │   ├── BaseEntity.java
│       │   ├── Course.java
│       │   └── User.java
│       ├── enums/                   # Enumerations
│       │   ├── CourseCategory.java
│       │   ├── CourseMode.java
│       │   └── UserType.java
│       ├── exception/               # Custom Exceptions
│       │   └── EntityNotFoundException.java
│       ├── repository/              # Spring Data JPA Repositories
│       │   ├── CourseRepository.java
│       │   └── UserRepository.java
│       ├── security/                # Security Utilities
│       │   └── JwtUtil.java
│       └── service/                 # Business Logic Services
│           ├── AuthService.java
│           ├── CourseService.java
│           ├── UserService.java
│           └── impl/
│               ├── AuthServiceImpl.java
│               ├── CourseServiceImpl.java
│               └── UserServiceImpl.java
│
├── edtech-api/                      # REST API Layer Module
│   ├── pom.xml
│   └── src/main/java/com/task/edtech/api/
│       └── controller/              # REST Controllers
│           ├── AuthController.java
│           └── CourseController.java
│
└── edtech-application/              # Application Layer Module
    ├── pom.xml
    └── src/main/
        ├── java/com/task/edtech/services/
        │   ├── Application.java     # Main Spring Boot Application
        │   ├── config/              # Configuration Classes
        │   │   └── JpaConfig.java
        │   └── security/            # Security Configuration
        │       ├── JwtAuthenticationFilter.java
        │       └── SecurityConfig.java
        └── resources/
            ├── application.properties
            └── application-local.properties  # Local configuration (gitignored)
```

### Module Responsibilities

- **edtech-db**: Database layer containing entities, repositories, services, DTOs, converters, and security utilities
- **edtech-api**: REST API layer containing all REST controllers
- **edtech-application**: Application layer containing the main Spring Boot application class, security configuration, and application properties

### Package Structure Details

**edtech-db Module:**
- `converter/` - Converts between entities and DTOs
- `dto/` - Data Transfer Objects for API requests/responses
- `entity/` - JPA entities representing database tables
- `enums/` - Enumeration types (UserType, CourseMode, CourseCategory)
- `exception/` - Custom exception classes
- `repository/` - Spring Data JPA repository interfaces
- `security/` - JWT utility classes
- `service/` - Service interfaces and implementations

**edtech-api Module:**
- `controller/` - REST controllers handling HTTP requests

**edtech-application Module:**
- `Application.java` - Main Spring Boot application entry point
- `config/` - Configuration classes (JPA, etc.)
- `security/` - Security configuration and filters

---

## 🚀 Quick Start Guide

### Prerequisites

Before you begin, ensure you have the following installed:

1. **Java 21** or higher
   ```bash
   java -version
   ```
   Should show version 21 or higher

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **PostgreSQL 12+**
   ```bash
   psql --version
   ```

4. **Git** (for cloning the repository)
   ```bash
   git --version
   ```

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd edtech.services
```

### Step 2: Database Setup

1. **Start PostgreSQL service**
   ```bash
   # Windows
   net start postgresql-x64-XX
   
   # Linux/Mac
   sudo systemctl start postgresql
   # or
   brew services start postgresql
   ```

2. **Create the database**
   ```bash
   psql -U postgres
   ```
   ```sql
   CREATE DATABASE edtech_db;
   ```

3. **Verify database creation**
   ```bash
   psql -U postgres -d edtech_db -c "\dt"
   ```

### Step 3: Configure Application Properties

1. **Create local properties file** (if not exists)
   ```bash
   cd edtech-application/src/main/resources
   ```

2. **Edit `application-local.properties`** with your database credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/edtech_db
   spring.datasource.username=postgres
   spring.datasource.password=your_postgres_password
   app.jwt.secret=your_jwt_secret_key_minimum_256_bits_for_hmac_sha_algorithms
   app.jwt.expiration=86400000
   ```

   **Important:** 
   - Replace `your_postgres_password` with your actual PostgreSQL password
   - Generate a secure JWT secret (at least 256 bits/32 characters)
   - Ensure `application-local.properties` is in `.gitignore`

3. **Update `application.properties`** if needed:
   ```properties
   spring.profiles.active=local
   server.port=8080
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
   ```

### Step 4: Build the Project

1. **Navigate to project root**
   ```bash
   cd edtech.services
   ```

2. **Build all modules**
   ```bash
   mvn clean install
   ```

   This will:
   - Download all dependencies
   - Compile all modules
   - Run tests (if any)
   - Package the application

### Step 5: Run the Application

1. **Run from Maven** (recommended for development)
   ```bash
   cd edtech-application
   mvn spring-boot:run
   ```

   Or from the root directory:
   ```bash
   mvn spring-boot:run -pl edtech-application
   ```

2. **Run from JAR** (after building)
   ```bash
   java -jar edtech-application/target/edtech-application-0.0.1-SNAPSHOT.jar
   ```

3. **Verify the application is running**
   - Check console logs for: `Started Application in X.XXX seconds`
   - Open browser: `http://localhost:8080`
   - Check health: `http://localhost:8080/api/auth/signup` (should return 400 Bad Request, not 404)


### Step 6: Test the API

1. **Test Signup Endpoint**
   ```bash
   curl -X POST http://localhost:8080/api/auth/signup \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Test Provider",
       "email": "provider@test.com",
       "password": "password123",
       "userType": "PROVIDER"
     }'
   ```

2. **Test Login Endpoint**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "email": "provider@test.com",
       "password": "password123"
     }'
   ```

3. **Test Search Endpoint** (public, no auth required)
   ```bash
   curl http://localhost:8080/api/courses/search?pinCode=560066
   ```

---

## 🏗️ Architecture Overview

### Technology Stack

- **Java 21** - Programming language
- **Spring Boot 4.0.1** - Application framework
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Relational database
- **Spring Security** - Authentication and authorization
- **JWT (JSON Web Tokens)** - Stateless authentication
- **Maven** - Build tool and dependency management
- **Lombok** - Boilerplate code reduction

### System Architecture

```
┌─────────────────────┐
│   REST API Clients  │
│  (Postman, curl,    │
│   Frontend Apps)    │
└──────────┬──────────┘
           │ HTTP/REST
           ▼
┌─────────────────────┐
│   Spring Boot       │
│   REST API          │
│   (Port 8080)       │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   PostgreSQL        │
│   Database          │
│   (Port 5432)       │
└─────────────────────┘
```

---

## 📊 Data Schema Design

### Entity Relationship Overview

```
User (1) ────< (Many) Course
```

**Note:** The system uses a unified `User` entity with a `UserType` enum to distinguish between `PROVIDER` and `LEARNER` roles. Only users with `UserType.PROVIDER` can create courses.

### Database Tables

#### 1. **users** table
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `internal_id` (UUID, UNIQUE, NOT NULL) - Public identifier
- `email` (VARCHAR, UNIQUE, NOT NULL)
- `password_hash` (VARCHAR, NOT NULL) - encrypted password
- `name` (VARCHAR, NOT NULL) - user's name or organization name
- `user_type` (VARCHAR, NOT NULL) - "PROVIDER" or "LEARNER"
- `created_at` (TIMESTAMP, NOT NULL)
- `updated_at` (TIMESTAMP)

**Indexes:**
- Unique index on `internal_id`
- Unique index on `email`
- Index on `internal_id` for faster lookups
- Index on `email` for faster lookups

#### 2. **courses** table
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `internal_id` (UUID, UNIQUE, NOT NULL) - Public identifier
- `user_id` (BIGINT, FOREIGN KEY → users.id, NOT NULL)
- `title` (VARCHAR, NOT NULL)
- `description` (TEXT)
- `category` (VARCHAR, NOT NULL) - Enum: CourseCategory values
- `mode` (VARCHAR, NOT NULL) - "ONLINE" or "IN_PERSON"
- `address` (VARCHAR) - required if mode is IN_PERSON
- `pin_code` (VARCHAR) - required if mode is IN_PERSON
- `start_date` (DATE, NOT NULL)
- `end_date` (DATE) - nullable for single-session courses
- `schedule_info` (VARCHAR) - e.g., "Mon-Fri 6-7pm"
- `price_amount` (DECIMAL) - nullable if free
- `is_free` (BOOLEAN, DEFAULT false)
- `capacity` (INTEGER) - nullable, optional
- `is_published` (BOOLEAN, DEFAULT false)
- `created_at` (TIMESTAMP, NOT NULL)
- `updated_at` (TIMESTAMP)

**Indexes:**
- Unique index on `internal_id`
- Unique index on `(user_id, title)` - prevents duplicate course titles per user
- Index on `internal_id` for faster lookups
- Index on `(user_id, title)` for faster queries

### Enums

#### UserType
- `PROVIDER` - Users who can create and manage courses
- `LEARNER` - Users who can search and view courses

#### CourseMode
- `ONLINE` - Online courses
- `IN_PERSON` - In-person courses (requires address and PIN code)

#### CourseCategory
- Defined in `CourseCategory` enum (e.g., YOGA, CODING, MUSIC, etc.)

---

## 🔌 API Design

### Base URL
`http://localhost:8080/api`

### Authentication
- JWT-based authentication
- Token sent in `Authorization: Bearer <token>` header
- Token expiration: 24 hours (configurable)
- Token contains user email and ID

### API Endpoints

#### **Authentication Endpoints** (Public)

1. **POST /api/auth/signup**
   - Request Body: 
     ```json
     {
       "email": "string",
       "password": "string",
       "name": "string",
       "userType": "PROVIDER" | "LEARNER"
     }
     ```
   - Response: 
     ```json
     {
       "token": "string",
       "provider": {
         "id": number,
         "internalId": "uuid",
         "email": "string",
         "name": "string",
         "userType": "PROVIDER" | "LEARNER",
         "createdAt": "timestamp",
         "updatedAt": "timestamp"
       }
     }
     ```
   - **Note:** The response field is named `provider` but contains `UserDTO` data

2. **POST /api/auth/login**
   - Request Body: 
     ```json
     {
       "email": "string",
       "password": "string"
     }
     ```
   - Response: Same as signup response

3. **GET /api/auth/me** (Protected)
   - Headers: `Authorization: Bearer <token>`
   - Response: 
     ```json
     {
       "id": number,
       "internalId": "uuid",
       "email": "string",
       "name": "string",
       "userType": "PROVIDER" | "LEARNER",
       "createdAt": "timestamp",
       "updatedAt": "timestamp"
     }
     ```

4. **POST /api/auth/logout** (Protected)
   - Headers: `Authorization: Bearer <token>`
   - Response: `200 OK`
   - **Note:** Clears server-side security context. Client should remove token from storage.

#### **Course Management Endpoints** (Protected - Requires JWT)

**Note:** All course management endpoints require authentication and only users with `UserType.PROVIDER` can create courses.

5. **POST /api/courses**
   - Headers: `Authorization: Bearer <token>`
   - Request Body: CourseDTO object (see below)
   - Response: Created course object (CourseDTO)
   - **Validation:** Only users with `userType: "PROVIDER"` can create courses

6. **GET /api/courses/mine**
   - Headers: `Authorization: Bearer <token>`
   - Response: Array of user's courses (CourseDTO[])

7. **GET /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>`
   - Response: Course detail object (CourseDTO)
   - **Security:** Only returns course if user owns it

8. **PUT /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>`
   - Request Body: CourseDTO object
   - Response: Updated course object (CourseDTO)
   - **Security:** Only allows update if user owns the course

9. **DELETE /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>`
   - Response: 204 No Content
   - **Security:** Only allows deletion if user owns the course

10. **POST /api/courses/{id}/publish**
    - Headers: `Authorization: Bearer <token>`
    - Response: Updated course with `isPublished: true` (CourseDTO)
    - **Security:** Only allows publish if user owns the course

11. **POST /api/courses/{id}/unpublish**
    - Headers: `Authorization: Bearer <token>`
    - Response: Updated course with `isPublished: false` (CourseDTO)
    - **Security:** Only allows unpublish if user owns the course

#### **Learner Search Endpoints** (Public - No Authentication Required)

12. **GET /api/courses/search**
    - Query Parameters:
      - `pinCode` (optional): PIN code to search (prefix match)
      - `filterPinCode` (optional): Override search PIN code
      - `category` (optional): Filter by CourseCategory enum value
      - `mode` (optional): "ONLINE" or "IN_PERSON"
      - `isFree` (optional): true/false
      - `startFrom` (optional): Start date filter (YYYY-MM-DD format)
      - `startTo` (optional): End date filter (YYYY-MM-DD format)
    - Response: List of published courses (not paginated)
      ```json
      [
        {
          "title": "Yoga for Beginners",
          "description": "Learn basic yoga poses",
          "category": "YOGA",
          "mode": "IN_PERSON",
          "address": "123 Main St",
          "pinCode": "110001",
          "startDate": "2025-02-01",
          "endDate": "2025-02-28",
          "scheduleInfo": "Mon-Fri 6-7pm",
          "priceAmount": 5000.00,
          "isFree": false,
          "capacity": 20
        }
      ]
      ```
    - **Note:** 
      - Only returns published courses with `startDate >= today` (or `startFrom` if provided)
      - If `pinCode` is not provided, returns all published courses matching other filters
      - Results are sorted by `startDate` ascending

### Request/Response Examples

**Signup Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "userType": "PROVIDER"
}
```

**Course Creation Request (CourseDTO):**
```json
{
  "title": "Yoga for Beginners",
  "description": "Learn basic yoga poses",
  "category": "YOGA",
  "mode": "IN_PERSON",
  "address": "123 Main St",
  "pinCode": "110001",
  "startDate": "2025-02-01",
  "endDate": "2025-02-28",
  "scheduleInfo": "Mon-Fri 6-7pm",
  "priceAmount": 5000.00,
  "isFree": false,
  "capacity": 20
}
```

**Course Search Response:**
```json
[
  {
    "title": "Yoga for Beginners",
    "description": "Learn basic yoga poses",
    "category": "YOGA",
    "mode": "IN_PERSON",
    "address": "123 Main St",
    "pinCode": "110001",
    "startDate": "2025-02-01",
    "endDate": "2025-02-28",
    "scheduleInfo": "Mon-Fri 6-7pm",
    "priceAmount": 5000.00,
    "isFree": false,
    "capacity": 20
  }
]
```

---

## 🔍 Search Behavior Logic

### PIN Code Matching Strategy
- **Prefix Match**: Courses where PIN code starts with the provided prefix
  - Example: PIN "110001" matches "110001", "110002", "110003", etc.
- Only return courses where `start_date >= current_date` (future courses only)
- Only return courses where `is_published = true`

### Filtering & Sorting
- Default sort: `start_date` ascending (earliest first)
- Filters are applied in combination (AND logic)
- Date range filter: `startFrom` and `startTo` filter courses by `start_date`
- All filters are optional (including `pinCode`)
- If `pinCode` is not provided, search returns all published courses matching other filters

---

## 💻 Development Setup

### IDE Configuration

**IntelliJ IDEA Setup:**
1. Open project: `File > Open > Select edtech.services folder`
2. Wait for Maven to import dependencies
3. Enable annotation processing: `File > Settings > Build > Compiler > Annotation Processors > Enable`
4. Configure Lombok: Install Lombok plugin if not already installed
5. Set Java version: `File > Project Structure > Project > SDK: Java 21`


## 🔐 Security Considerations

### Security Best Practices
- Use BCrypt for password hashing (never store plain passwords)
- Validate all inputs (use Jakarta Bean Validation)
- Sanitize user inputs to prevent SQL injection (JPA handles this)
- Use HTTPS in production
- Implement rate limiting for authentication endpoints (to be added)
- Validate JWT tokens properly
- Check ownership before allowing course modifications
- Verify userType before allowing course creation (only PROVIDER)
- Configure CORS appropriately for your API clients
- Never expose sensitive data in API responses
- Handle token expiration gracefully

---

## 📝 Environment Variables

### Application Properties (`edtech-application/src/main/resources/application-local.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/edtech_db
spring.datasource.username=postgres
spring.datasource.password=your_password
app.jwt.secret=your_jwt_secret_key_minimum_256_bits_for_hmac_sha_algorithms
app.jwt.expiration=86400000
```

**Important:** 
- Keep `application-local.properties` in `.gitignore`
- Use environment variables or secure vaults in production
- Generate a strong JWT secret (minimum 32 characters/256 bits)

---

## 📚 Next Steps & Improvements

1. **Error Handling**
   - Implement GlobalExceptionHandler
   - Add structured error responses
   - Implement error logging and monitoring

2. **DevOps**
   - Set up CI/CD pipeline
   - Configure automated testing
   - Set up monitoring and alerting
   - Implement logging aggregation
