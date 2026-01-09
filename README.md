# EdTech Course Platform - Architecture & Build Guide

## 📋 Project Overview

A web application that connects course providers (people/organizations running courses) with learners (users searching for courses). Providers can create and manage courses, while learners can search for courses by PIN code and browse results.

---

## 🏗️ Architecture Overview

### Technology Stack

**Backend:**
- Java 21
- Spring Boot 4.0.1
- Spring Data JPA
- PostgreSQL Database
- Spring Security (for authentication)
- JWT (JSON Web Tokens) for authentication
- Maven (build tool - multi-module project)
- Lombok (for reducing boilerplate code)

**Frontend:**
- React (with Vite or Create React App)
- React Router (for navigation)
- Axios or Fetch API (for HTTP requests)
- Modern CSS framework (Tailwind CSS or Material-UI)

### Project Structure (Multi-Module Maven)

```
edtech-parent/
├── edtech-db/          # Database layer (entities, repositories, services, DTOs, converters)
├── edtech-api/         # REST API layer (controllers)
└── edtech-application/ # Application layer (main class, security config, application properties)
```

**Module Responsibilities:**
- **edtech-db**: Contains all database-related code (entities, repositories, services, DTOs, converters, security utilities)
- **edtech-api**: Contains REST controllers for API endpoints
- **edtech-application**: Contains the main Spring Boot application class, security configuration, and application properties

### System Architecture

```
┌─────────────────┐         ┌─────────────────┐
│   React Frontend │ ◄─────► │  Spring Boot    │
│   (Port 3000)    │  HTTP   │  REST API       │
│                  │         │  (Port 8080)    │
└─────────────────┘         └────────┬────────┘
                                     │
                                     ▼
                              ┌──────────────┐
                              │  PostgreSQL  │
                              │  Database    │
                              └──────────────┘
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

#### **Course Management Endpoints** (Protected - Requires JWT)

**Note:** All course management endpoints require authentication and only users with `UserType.PROVIDER` can create courses.

4. **POST /api/courses**
   - Headers: `Authorization: Bearer <token>`
   - Request Body: CourseDTO object (see below)
   - Response: Created course object (CourseDTO)
   - **Validation:** Only users with `userType: "PROVIDER"` can create courses

5. **GET /api/courses/mine**
   - Headers: `Authorization: Bearer <token>`
   - Response: Array of user's courses (CourseDTO[])

6. **GET /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>`
   - Response: Course detail object (CourseDTO)
   - **Security:** Only returns course if user owns it

7. **PUT /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>`
   - Request Body: CourseDTO object
   - Response: Updated course object (CourseDTO)
   - **Security:** Only allows update if user owns the course

8. **DELETE /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>`
   - Response: 204 No Content
   - **Security:** Only allows deletion if user owns the course

9. **POST /api/courses/{id}/publish**
   - Headers: `Authorization: Bearer <token>`
   - Response: Updated course with `isPublished: true` (CourseDTO)
   - **Security:** Only allows publish if user owns the course

10. **POST /api/courses/{id}/unpublish**
    - Headers: `Authorization: Bearer <token>`
    - Response: Updated course with `isPublished: false` (CourseDTO)
    - **Security:** Only allows unpublish if user owns the course

#### **Learner Search Endpoints** (Public - No Authentication Required)

11. **GET /api/courses/search**
    - Query Parameters:
      - `pinCode` (required): PIN code to search
      - `filterPinCode` (optional): Override search PIN code
      - `category` (optional): Filter by CourseCategory enum value
      - `mode` (optional): "ONLINE" or "IN_PERSON"
      - `isFree` (optional): true/false
      - `startFrom` (optional): Start date filter (YYYY-MM-DD format)
      - `startTo` (optional): End date filter (YYYY-MM-DD format)
      - `page` (optional): Page number (default: 0)
      - `size` (optional): Page size (default: 10)
      - `sortBy` (optional): Sort field (default: "startDate")
      - `sortDir` (optional): Sort direction - "ASC" or "DESC" (default: "ASC")
    - Response: Paginated list of published courses
      ```json
      {
        "content": [CourseDTO[]],
        "totalElements": number,
        "totalPages": number,
        "number": number,
        "size": number,
        "first": boolean,
        "last": boolean
      }
      ```
    - **Note:** Only returns published courses with `startDate >= today`

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
{
  "content": [
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
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10,
  "first": true,
  "last": true
}
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
- All filters are optional except `pinCode`

---

## 🛠️ Step-by-Step Backend Build Instructions

### Phase 1: Project Setup & Dependencies

#### Step 1.1: Verify Prerequisites
- Ensure Java 21 is installed (`java -version`)
- Ensure Maven is installed (`mvn -version`)
- Ensure PostgreSQL is installed and running
- Create a database named `edtech_db` in PostgreSQL

#### Step 1.2: Project Structure
The project uses a multi-module Maven structure:
- **edtech-parent**: Parent POM managing all modules
- **edtech-db**: Database layer module
- **edtech-api**: REST API layer module
- **edtech-application**: Application layer module

#### Step 1.3: Configure Application Properties
- Update `edtech-application/src/main/resources/application.properties` with:
  - Server port (8080)
  - JPA/Hibernate settings
  - Database connection settings (use environment variables)
  - JWT secret key (use environment variable)
  - JWT expiration time
- Ensure `application-local.properties` is in `.gitignore` and contains actual database credentials

### Phase 2: Database Layer

#### Step 2.1: Entity Classes
- **BaseEntity**: Abstract base class with `id` and `internalId` (UUID)
  - Uses `@SuperBuilder` for Lombok builder pattern
  - Auto-generates `internalId` on persist
  
- **User** entity class in `edtech-db/src/main/java/com/task/edtech/db/entity/`
  - Extends `BaseEntity`
  - Fields: email, passwordHash, name, userType, createdAt, updatedAt
  - JPA annotations (@Entity, @Table, @Id, @GeneratedValue, etc.)
  - Validation annotations (@NotNull, @Email, etc.)
  - Relationship: OneToMany with Course
  - Uses `@SuperBuilder` for inheritance support

- **Course** entity class in `edtech-db/src/main/java/com/task/edtech/db/entity/`
  - Extends `BaseEntity`
  - All fields as per schema design
  - JPA annotations
  - Validation annotations
  - Relationship: ManyToOne with User
  - Enum for CourseMode (ONLINE, IN_PERSON)
  - Enum for CourseCategory
  - Uses `@SuperBuilder` for inheritance support

#### Step 2.2: Repository Interfaces
- **UserRepository** interface extending `JpaRepository<User, Long>`
  - Method: `findByEmail(String email)`
  - Method: `findByInternalId(UUID internalId)`
  - Method: `existsByEmail(String email)`
  - All methods use `@Query` annotations

- **CourseRepository** interface extending `JpaRepository<Course, Long>`
  - Method: `getAllByUserId(Long userId)`
  - Method: `getAllByUserId(Long userId, Pageable pageable)`
  - Method: `findByUserIdAndTitle(Long userId, String title)`
  - Method: `countByUserId(Long userId)`
  - Method: `findByInternalId(UUID internalId)`
  - Method: `searchCourses(...)` - complex search with filters
  - All methods use `@Query` annotations

#### Step 2.3: Configure Database
- Ensure `spring.jpa.hibernate.ddl-auto=update` in properties
- Run application to create tables automatically
- Verify tables are created in PostgreSQL

### Phase 3: Security & Authentication

#### Step 3.1: Security Configuration
- **SecurityConfig** class in `edtech-application/src/main/java/com/task/edtech/services/security/`
  - Configure password encoder (BCrypt)
  - Configure JWT authentication filter
  - Configure public endpoints (signup, login, search)
  - Configure protected endpoints (all /api/courses/* except search and public)
  - Disable CSRF for API
  - Configure CORS (allow frontend origin)

#### Step 3.2: JWT Utility Class
- **JwtUtil** class in `edtech-db/src/main/java/com/task/edtech/db/security/`
  - Method: `generateToken(String email, Long id)`
  - Method: `validateToken(String token)`
  - Method: `getEmailFromToken(String token)`
  - Method: `getIdFromToken(String token)`

#### Step 3.3: JWT Authentication Filter
- **JwtAuthenticationFilter** class in `edtech-application/src/main/java/com/task/edtech/services/security/`
  - Extends `OncePerRequestFilter`
  - Extract token from Authorization header
  - Validate token
  - Set authentication in SecurityContext

#### Step 3.4: Authentication Service
- **AuthService** interface in `edtech-db/src/main/java/com/task/edtech/db/service/`
- **AuthServiceImpl** in `edtech-db/src/main/java/com/task/edtech/db/service/impl/`
  - Method: `signup(SignupRequest)` - create user, hash password, return JWT
  - Method: `login(LoginRequest)` - verify credentials, return JWT
  - Method: `getCurrentUser()` - get user from SecurityContext
  - Method: `getCurrentUserId()` - get current user's ID

### Phase 4: DTOs & Request/Response Models

#### Step 4.1: Request DTOs
- **SignupRequest** class with email, password, name, **userType** (required)
- **LoginRequest** class with email, password
- **CourseDTO** class with all course fields (used for both create and update)

#### Step 4.2: Response DTOs
- **AuthResponse** class with token and user info (field named `provider` but contains UserDTO)
- **UserDTO** class with user information
- **CourseDTO** class with all course fields
- **SearchFilters** class for search query parameters

#### Step 4.3: Converter Classes
- **UserConverter** class in `edtech-db/src/main/java/com/task/edtech/db/converter/`
  - Method: `toDto(User entity)` - converts User to UserDTO
  - Method: `toEntity(UserDTO dto)` - converts UserDTO to User

- **CourseConverter** class in `edtech-db/src/main/java/com/task/edtech/db/converter/`
  - Method: `toDto(Course entity)` - converts Course to CourseDTO
  - Method: `toEntity(CourseDTO dto, Long userId)` - converts CourseDTO to Course, resolves User foreign key

### Phase 5: Service Layer

#### Step 5.1: User Service
- **UserService** interface in `edtech-db/src/main/java/com/task/edtech/db/service/`
- **UserServiceImpl** in `edtech-db/src/main/java/com/task/edtech/db/service/impl/`
  - Method: `findById(Long id)`
  - Method: `findByInternalId(UUID internalId)`
  - Method: `findByEmail(String email)`
  - Method: `existsByEmail(String email)`
  - Method: `addOrUpdate(User user)`
  - Method: `delete(User user)`
  - Method: `deleteById(Long id)`

#### Step 5.2: Course Service
- **CourseService** interface in `edtech-db/src/main/java/com/task/edtech/db/service/`
- **CourseServiceImpl** in `edtech-db/src/main/java/com/task/edtech/db/service/impl/`
  - Method: `findById(Long courseId)`
  - Method: `findByInternalId(UUID courseInternalId)`
  - Method: `findByUserIdAndTitle(Long userId, String title)`
  - Method: `countByUserId(Long userId)`
  - Method: `addOrUpdate(Course course)` - handles both create and update
  - Method: `delete(Course course)`
  - Method: `publishCourse(UUID courseInternalId)`
  - Method: `unpublishCourse(UUID courseInternalId)`
  - Method: `getAllByUserId(Long userId)`
  - Method: `getAllByUserId(Long userId, Pageable pageable)`
  - Method: `searchCourses(String pinCode, SearchFilters filters, Pageable pageable)`

### Phase 6: Controller Layer

#### Step 6.1: Authentication Controller
- **AuthController** class in `edtech-api/src/main/java/com/task/edtech/api/controller/`
  - Endpoint: `POST /api/auth/signup` - call AuthService.signup
  - Endpoint: `POST /api/auth/login` - call AuthService.login
  - Endpoint: `GET /api/auth/me` - call AuthService.getCurrentUser, convert to UserDTO

#### Step 6.2: Course Controller
- **CourseController** class in `edtech-api/src/main/java/com/task/edtech/api/controller/`
  - Endpoint: `POST /api/courses` - create course (requires PROVIDER userType)
  - Endpoint: `GET /api/courses/mine` - get user's courses
  - Endpoint: `GET /api/courses/{id}` - get course detail (ownership verified)
  - Endpoint: `PUT /api/courses/{id}` - update course (ownership verified)
  - Endpoint: `DELETE /api/courses/{id}` - delete course (ownership verified)
  - Endpoint: `POST /api/courses/{id}/publish` - publish course (ownership verified)
  - Endpoint: `POST /api/courses/{id}/unpublish` - unpublish course (ownership verified)
  - Endpoint: `GET /api/courses/search` - search courses with filters (public, no auth required)
  - All protected endpoints require authentication (get userId from AuthService)
  - Uses CourseConverter for entity-DTO conversion

### Phase 7: Exception Handling

#### Step 7.1: Custom Exceptions
- **EntityNotFoundException** class in `edtech-db/src/main/java/com/task/edtech/db/exception/`
  - Extends RuntimeException
  - Used when entities are not found

#### Step 7.2: Global Exception Handler (To Be Implemented)
- Create `GlobalExceptionHandler` class with `@ControllerAdvice`
  - Handle EntityNotFoundException → 404
  - Handle validation errors → 400
  - Handle generic exceptions → 500

### Phase 8: Testing & Validation

#### Step 8.1: Test Database Connection
- Run application from `edtech-application` module
- Verify database tables are created
- Check application logs for any errors

#### Step 8.2: Test Authentication Endpoints
- Use Postman or curl to test:
  - Signup endpoint (with userType field)
  - Login endpoint
  - Get current user endpoint (with token)

#### Step 8.3: Test Course Management Endpoints
- Test creating a course (with authentication, as PROVIDER)
- Test getting user's courses
- Test updating a course
- Test deleting a course
- Test publish/unpublish

#### Step 8.4: Test Search Endpoints
- Test search by PIN code
- Test search with filters
- Test pagination
- Verify only published and future courses are returned

#### Step 8.5: Validate Business Logic
- Verify PIN code matching (prefix match)
- Verify date filtering
- Verify ownership checks (users can only modify their own courses)
- Verify only PROVIDER users can create courses

### Phase 9: Final Configuration

#### Step 9.1: Configure CORS
- Update SecurityConfig to allow frontend origin (http://localhost:3000, http://localhost:5173)
- Allow necessary HTTP methods (GET, POST, PUT, DELETE)
- Allow necessary headers (Authorization, Content-Type)

#### Step 9.2: Add Logging
- Configure logging levels
- Add logging statements in service methods
- Log authentication attempts
- Log course creation/updates

#### Step 9.3: Environment Variables
- Ensure all sensitive data (DB credentials, JWT secret) are in `application-local.properties`
- Document required environment variables in README

---

## 🔐 Security Considerations

### Backend Security
- Use BCrypt for password hashing (never store plain passwords)
- Validate all inputs (use Jakarta Bean Validation)
- Sanitize user inputs to prevent SQL injection (JPA handles this)
- Use HTTPS in production
- Implement rate limiting for authentication endpoints (to be added)
- Validate JWT tokens properly
- Check ownership before allowing course modifications
- Verify userType before allowing course creation (only PROVIDER)

### Frontend Security
- Store JWT token securely (localStorage - consider httpOnly cookies for production)
- Never expose sensitive data in client-side code
- Validate inputs on frontend (but always validate on backend too)
- Handle tokens expiration gracefully
- Implement proper logout (clear tokens)
- Protect routes based on authentication and userType

---

## 📝 Environment Variables

### Backend (`edtech-application/src/main/resources/application-local.properties`)
```
spring.datasource.url=jdbc:postgresql://localhost:5432/edtech_db
spring.datasource.username=postgres
spring.datasource.password=your_password
app.jwt.secret=your_jwt_secret_key_minimum_256_bits_for_hmac_sha_algorithms
app.jwt.expiration=86400000
```

### Frontend (`.env`)
```
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

---

## 🚀 Deployment Considerations

### Backend Deployment
- Build JAR file: `mvn clean package` (from parent directory)
- Run JAR: `java -jar edtech-application/target/edtech-application-0.0.1-SNAPSHOT.jar`
- Configure production database
- Set environment variables for production
- Use process manager (PM2, systemd)
- Configure reverse proxy (Nginx)

### Frontend Deployment
- Build production bundle: `npm run build`
- Serve static files (Nginx, Apache, or CDN)
- Configure API base URL for production
- Set up HTTPS

---

## 📚 Next Steps After Backend Completion

1. Test all API endpoints thoroughly
2. Document API using Swagger/OpenAPI (optional)
3. Add unit tests for services
4. Add integration tests for controllers
5. Implement GlobalExceptionHandler
6. Add rate limiting
7. Set up CI/CD pipeline (optional)
8. Prepare for frontend integration

---

## 📚 Next Steps After Frontend Completion

1. Test end-to-end user flows
2. Optimize performance
3. Add analytics (optional)
4. Set up error tracking (optional)
5. Deploy to production
6. Monitor and maintain

---

## 🐛 Common Issues & Solutions

### Backend Issues
- **Database connection error**: Check PostgreSQL is running and credentials are correct
- **JWT token invalid**: Verify JWT secret is consistent
- **CORS error**: Update SecurityConfig to allow frontend origin
- **Table not created**: Check `spring.jpa.hibernate.ddl-auto` setting
- **Builder compilation error**: Ensure BaseEntity uses `@SuperBuilder` and child entities also use `@SuperBuilder`

### Frontend Issues
- **API calls failing**: Check CORS configuration and API base URL
- **Token not persisting**: Check localStorage implementation
- **Routing not working**: Verify React Router setup
- **Build errors**: Check Node version and dependencies
- **401 errors**: Check if token is being sent in Authorization header

---

## 📖 Additional Resources

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- React Documentation: https://react.dev
- PostgreSQL Documentation: https://www.postgresql.org/docs
- JWT Documentation: https://jwt.io
- Lombok Documentation: https://projectlombok.org

---

**Note**: This is a step-by-step guide. Follow each phase sequentially. Do not skip steps as later phases depend on earlier ones. Test thoroughly after each phase before moving to the next.
