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
- Maven (build tool)

**Frontend:**
- React (with Vite or Create React App)
- React Router (for navigation)
- Axios or Fetch API (for HTTP requests)
- Modern CSS framework (Tailwind CSS or Material-UI)

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
Provider (1) ────< (Many) Course
```

### Database Tables

#### 1. **providers** table
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `email` (VARCHAR, UNIQUE, NOT NULL)
- `password_hash` (VARCHAR, NOT NULL) - encrypted password
- `name` (VARCHAR, NOT NULL) - provider's name or organization name
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

#### 2. **courses** table
- `id` (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- `provider_id` (BIGINT, FOREIGN KEY → providers.id)
- `title` (VARCHAR, NOT NULL)
- `description` (TEXT)
- `category` (VARCHAR, NOT NULL) - e.g., "Yoga", "Coding", "Music"
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
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### Database Indexes
- Index on `courses.pin_code` for faster PIN code searches
- Index on `courses.start_date` for date filtering
- Index on `courses.is_published` for filtering published courses
- Index on `courses.category` for category filtering
- Index on `courses.mode` for mode filtering

---

## 🔌 API Design

### Base URL
`http://localhost:8080/api`

### Authentication
- JWT-based authentication
- Token sent in `Authorization: Bearer <token>` header
- Token expiration: 24 hours (configurable)

### API Endpoints

#### **Authentication Endpoints** (Public)

1. **POST /api/auth/signup**
   - Request Body: `{ "email": "string", "password": "string", "name": "string" }`
   - Response: `{ "token": "string", "provider": { "id": number, "email": "string", "name": "string" } }`

2. **POST /api/auth/login**
   - Request Body: `{ "email": "string", "password": "string" }`
   - Response: `{ "token": "string", "provider": { "id": number, "email": "string", "name": "string" } }`

3. **GET /api/auth/me** (Protected)
   - Headers: `Authorization: Bearer <token>`
   - Response: `{ "id": number, "email": "string", "name": "string" }`

#### **Provider Course Management Endpoints** (Protected - Requires JWT)

4. **POST /api/courses**
   - Headers: `Authorization: Bearer <token>`
   - Request Body: Course creation object (see below)
   - Response: Created course object with ID

5. **GET /api/courses/mine**
   - Headers: `Authorization: Bearer <token>`
   - Response: Array of provider's courses

6. **GET /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>` (only for provider's own courses)
   - Response: Course detail object

7. **PUT /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>`
   - Request Body: Course update object
   - Response: Updated course object

8. **DELETE /api/courses/{id}**
   - Headers: `Authorization: Bearer <token>`
   - Response: Success message

9. **POST /api/courses/{id}/publish**
   - Headers: `Authorization: Bearer <token>`
   - Response: Updated course with `is_published: true`

10. **POST /api/courses/{id}/unpublish**
    - Headers: `Authorization: Bearer <token>`
    - Response: Updated course with `is_published: false`

#### **Learner Search Endpoints** (Public)

11. **GET /api/courses/search**
    - Query Parameters:
      - `pin` (required): PIN code to search
      - `category` (optional): Filter by category
      - `mode` (optional): "ONLINE" or "IN_PERSON"
      - `free` (optional): "true" or "false"
      - `startFrom` (optional): Start date filter (YYYY-MM-DD)
      - `startTo` (optional): End date filter (YYYY-MM-DD)
      - `page` (optional): Page number (default: 0)
      - `size` (optional): Page size (default: 20)
      - `sort` (optional): Sort field (default: "startDate")
    - Response: Paginated list of published courses

12. **GET /api/courses/{id}/public**
    - Response: Course detail (public view, only published courses)

### Request/Response Examples

**Course Creation Request:**
```json
{
  "title": "Yoga for Beginners",
  "description": "Learn basic yoga poses",
  "category": "Yoga",
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
      "id": 1,
      "title": "Yoga for Beginners",
      "description": "Learn basic yoga poses",
      "category": "Yoga",
      "mode": "IN_PERSON",
      "address": "123 Main St",
      "pinCode": "110001",
      "startDate": "2025-02-01",
      "endDate": "2025-02-28",
      "scheduleInfo": "Mon-Fri 6-7pm",
      "priceAmount": 5000.00,
      "isFree": false,
      "capacity": 20,
      "providerName": "Yoga Studio"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

---

## 🔍 Search Behavior Logic

### PIN Code Matching Strategy
- **Exact Match**: Courses with the exact PIN code
- **Nearby Match**: Courses where the first 3 digits of the PIN code match (simple approach)
  - Example: PIN "110001" matches "110001", "110002", "110003", etc.
- Only return courses where `start_date >= current_date` (future courses only)
- Only return courses where `is_published = true`

### Filtering & Sorting
- Default sort: `start_date` ascending (earliest first)
- Filters are applied in combination (AND logic)
- Date range filter: `startFrom` and `startTo` filter courses by `start_date`

---

## 🛠️ Step-by-Step Backend Build Instructions

### Phase 1: Project Setup & Dependencies

#### Step 1.1: Verify Prerequisites
- Ensure Java 21 is installed (`java -version`)
- Ensure Maven is installed (`mvn -version`)
- Ensure PostgreSQL is installed and running
- Create a database named `edtech_db` in PostgreSQL

#### Step 1.2: Update Maven Dependencies
- Open `pom.xml`
- Add Spring Security dependency
- Add JWT library dependency (e.g., `io.jsonwebtoken:jjwt`)
- Add BCrypt password encoder dependency (usually included with Spring Security)
- Add validation dependencies (Jakarta Bean Validation)
- Remove H2 database dependency (we're using PostgreSQL only)
- Run `mvn clean install` to download dependencies

#### Step 1.3: Configure Application Properties
- Update `application.properties` with:
  - Server port (8080)
  - JPA/Hibernate settings
  - Database connection settings (use environment variables)
  - JWT secret key (use environment variable)
  - JWT expiration time
- Ensure `application-local.properties` is in `.gitignore` and contains actual database credentials

### Phase 2: Database Layer

#### Step 2.1: Create Entity Classes
- Create `Provider` entity class in `entity` package
  - Add fields: id, email, passwordHash, name, createdAt, updatedAt
  - Add JPA annotations (@Entity, @Table, @Id, @GeneratedValue, etc.)
  - Add validation annotations (@NotNull, @Email, etc.)
  - Add relationship: OneToMany with Course

- Create `Course` entity class in `entity` package
  - Add all fields as per schema design
  - Add JPA annotations
  - Add validation annotations
  - Add relationship: ManyToOne with Provider
  - Add enum for CourseMode (ONLINE, IN_PERSON)
  - Add enum for CourseCategory (or use String)

#### Step 2.2: Create Repository Interfaces
- Create `ProviderRepository` interface extending `JpaRepository<Provider, Long>`
  - Add method: `findByEmail(String email)`

- Create `CourseRepository` interface extending `JpaRepository<Course, Long>`
  - Add method: `findByProviderId(Long providerId)`
  - Add method: `findByPinCodeStartingWithAndIsPublishedTrueAndStartDateGreaterThanEqual(String pinPrefix, LocalDate date)`
  - Add method: `findByPinCodeAndIsPublishedTrueAndStartDateGreaterThanEqual(String pinCode, LocalDate date)`
  - Add custom query methods for search with filters

#### Step 2.3: Configure Database
- Ensure `spring.jpa.hibernate.ddl-auto=update` in properties
- Run application to create tables automatically
- Verify tables are created in PostgreSQL

### Phase 3: Security & Authentication

#### Step 3.1: Create Security Configuration
- Create `SecurityConfig` class
  - Configure password encoder (BCrypt)
  - Configure JWT authentication filter
  - Configure public endpoints (signup, login, search)
  - Configure protected endpoints (all /api/courses/* except search)
  - Disable CSRF for API
  - Configure CORS (allow frontend origin)

#### Step 3.2: Create JWT Utility Class
- Create `JwtUtil` class
  - Method: `generateToken(String email, Long id)`
  - Method: `validateToken(String token)`
  - Method: `getEmailFromToken(String token)`
  - Method: `getIdFromToken(String token)`

#### Step 3.3: Create JWT Authentication Filter
- Create `JwtAuthenticationFilter` class extending `OncePerRequestFilter`
  - Extract token from Authorization header
  - Validate token
  - Set authentication in SecurityContext

#### Step 3.4: Create Authentication Service
- Create `AuthService` class
  - Method: `signup(SignupRequest)` - create provider, hash password, return JWT
  - Method: `login(LoginRequest)` - verify credentials, return JWT
  - Method: `getCurrentProvider()` - get provider from SecurityContext

### Phase 4: DTOs & Request/Response Models

#### Step 4.1: Create Request DTOs
- Create `SignupRequest` class with email, password, name
- Create `LoginRequest` class with email, password
- Create `CourseCreateRequest` class with all course fields
- Create `CourseUpdateRequest` class (similar to create, but fields optional)

#### Step 4.2: Create Response DTOs
- Create `AuthResponse` class with token and provider info
- Create `CourseResponse` class with all course fields
- Create `CourseSummaryResponse` class (simplified for list views)
- Create `ProviderResponse` class

#### Step 4.3: Create Mapper Classes (Optional)
- Create mapper methods to convert Entity → DTO and DTO → Entity
- Or use manual mapping in service layer

### Phase 5: Service Layer

#### Step 5.1: Create Course Service
- Create `CourseService` class
  - Method: `createCourse(CourseCreateRequest, Long providerId)` - create course, link to provider
  - Method: `getMyCourses(Long providerId)` - get all courses for provider
  - Method: `getCourseById(Long courseId, Long providerId)` - get course (verify ownership)
  - Method: `updateCourse(Long courseId, CourseUpdateRequest, Long providerId)` - update course (verify ownership)
  - Method: `deleteCourse(Long courseId, Long providerId)` - delete course (verify ownership)
  - Method: `publishCourse(Long courseId, Long providerId)` - set is_published = true
  - Method: `unpublishCourse(Long courseId, Long providerId)` - set is_published = false

#### Step 5.2: Create Search Service
- Create `SearchService` class
  - Method: `searchCourses(String pinCode, SearchFilters, Pageable)` - search with filters
  - Implement PIN code matching logic (exact + nearby)
  - Filter by category, mode, price, date range
  - Only return published courses with future start dates
  - Return paginated results

### Phase 6: Controller Layer

#### Step 6.1: Create Authentication Controller
- Create `AuthController` class
  - Endpoint: `POST /api/auth/signup` - call AuthService.signup
  - Endpoint: `POST /api/auth/login` - call AuthService.login
  - Endpoint: `GET /api/auth/me` - call AuthService.getCurrentProvider

#### Step 6.2: Create Course Controller (Provider)
- Create `CourseController` class
  - Endpoint: `POST /api/courses` - create course
  - Endpoint: `GET /api/courses/mine` - get provider's courses
  - Endpoint: `GET /api/courses/{id}` - get course detail
  - Endpoint: `PUT /api/courses/{id}` - update course
  - Endpoint: `DELETE /api/courses/{id}` - delete course
  - Endpoint: `POST /api/courses/{id}/publish` - publish course
  - Endpoint: `POST /api/courses/{id}/unpublish` - unpublish course
  - All endpoints require authentication (use `@PreAuthorize` or get from SecurityContext)

#### Step 6.3: Create Search Controller (Public)
- Create `SearchController` class
  - Endpoint: `GET /api/courses/search` - search courses with filters
  - Endpoint: `GET /api/courses/{id}/public` - get public course detail
  - These endpoints are public (no authentication required)

### Phase 7: Exception Handling

#### Step 7.1: Create Custom Exceptions
- Create `ResourceNotFoundException` class
- Create `UnauthorizedException` class
- Create `BadRequestException` class

#### Step 7.2: Create Global Exception Handler
- Create `GlobalExceptionHandler` class with `@ControllerAdvice`
  - Handle ResourceNotFoundException → 404
  - Handle UnauthorizedException → 401
  - Handle BadRequestException → 400
  - Handle validation errors → 400
  - Handle generic exceptions → 500

### Phase 8: Testing & Validation

#### Step 8.1: Test Database Connection
- Run application
- Verify database tables are created
- Check application logs for any errors

#### Step 8.2: Test Authentication Endpoints
- Use Postman or curl to test:
  - Signup endpoint
  - Login endpoint
  - Get current user endpoint (with token)

#### Step 8.3: Test Course Management Endpoints
- Test creating a course (with authentication)
- Test getting provider's courses
- Test updating a course
- Test deleting a course
- Test publish/unpublish

#### Step 8.4: Test Search Endpoints
- Test search by PIN code
- Test search with filters
- Test pagination
- Verify only published and future courses are returned

#### Step 8.5: Validate Business Logic
- Verify PIN code matching (exact + nearby)
- Verify date filtering
- Verify ownership checks (providers can only modify their own courses)

### Phase 9: Final Configuration

#### Step 9.1: Configure CORS
- Update SecurityConfig to allow frontend origin (http://localhost:3000)
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

## 🎨 Step-by-Step Frontend Build Instructions

### Phase 1: Project Setup

#### Step 1.1: Create React Project
- Navigate to project root directory
- Run command to create React app (using Vite or Create React App)
- Install dependencies

#### Step 1.2: Install Additional Dependencies
- Install React Router for navigation
- Install Axios for HTTP requests
- Install a CSS framework (Tailwind CSS or Material-UI)
- Install form handling library (React Hook Form)
- Install date handling library (date-fns or moment.js)

#### Step 1.3: Configure Environment Variables
- Create `.env` file
- Add `REACT_APP_API_BASE_URL=http://localhost:8080/api`
- Add `.env` to `.gitignore`

### Phase 2: Project Structure

#### Step 2.1: Create Folder Structure
- Create `src/components` folder
- Create `src/pages` folder
- Create `src/services` folder (for API calls)
- Create `src/utils` folder (for utilities)
- Create `src/context` folder (for React Context if needed)
- Create `src/hooks` folder (for custom hooks)

#### Step 2.2: Create API Service Layer
- Create `api.js` or `api.ts` file
  - Configure Axios instance with base URL
  - Add request interceptor to attach JWT token
  - Add response interceptor for error handling
  - Create API methods for all endpoints

### Phase 3: Authentication Pages

#### Step 3.1: Create Login Page
- Create `Login.jsx` component
  - Form with email and password fields
  - Submit handler calls login API
  - Store JWT token in localStorage
  - Redirect to provider dashboard on success

#### Step 3.2: Create Signup Page
- Create `Signup.jsx` component
  - Form with email, password, and name fields
  - Submit handler calls signup API
  - Store JWT token in localStorage
  - Redirect to provider dashboard on success

#### Step 3.3: Create Auth Context (Optional)
- Create `AuthContext.jsx` to manage authentication state
- Provide auth state and methods to all components

### Phase 4: Provider Dashboard

#### Step 4.1: Create Provider Dashboard Page
- Create `ProviderDashboard.jsx` component
  - Display list of provider's courses
  - Add "Create New Course" button
  - Show course status (published/unpublished)
  - Add edit/delete buttons for each course

#### Step 4.2: Create Course Form Component
- Create `CourseForm.jsx` component
  - Form with all course fields
  - Handle both create and edit modes
  - Validation for required fields
  - Conditional fields (address/pin code only for in-person)
  - Submit handler calls create/update API

#### Step 4.3: Create Course List Component
- Create `CourseList.jsx` component
  - Display courses in cards or table
  - Show course details (title, category, dates, status)
  - Add publish/unpublish toggle
  - Add edit and delete actions

### Phase 5: Learner Search Pages

#### Step 5.1: Create Search Page
- Create `SearchPage.jsx` component
  - PIN code search input
  - Filter sidebar/bar with:
    - Category dropdown
    - Mode toggle (Online/In-Person)
    - Price filter (Free/Paid)
    - Date range picker
  - Display search results
  - Add pagination

#### Step 5.2: Create Course Card Component
- Create `CourseCard.jsx` component
  - Display course summary (title, category, dates, price, location)
  - Clickable to view details
  - Show "Free" badge if course is free

#### Step 5.3: Create Course Detail Page
- Create `CourseDetailPage.jsx` component
  - Display full course information
  - Show provider name
  - Display schedule, address (if in-person), price
  - Show capacity if available

### Phase 6: Routing & Navigation

#### Step 6.1: Set Up React Router
- Create `App.jsx` with routes:
  - `/` - Home/Search page
  - `/login` - Login page
  - `/signup` - Signup page
  - `/provider/dashboard` - Provider dashboard (protected)
  - `/provider/courses/new` - Create course (protected)
  - `/provider/courses/:id/edit` - Edit course (protected)
  - `/courses/:id` - Course detail (public)

#### Step 6.2: Create Protected Route Component
- Create `ProtectedRoute.jsx` component
  - Check if user is authenticated
  - Redirect to login if not authenticated
  - Render children if authenticated

#### Step 6.3: Create Navigation Component
- Create `Navbar.jsx` component
  - Show "Search Courses" link (public)
  - Show "Provider Login" link (if not logged in)
  - Show "Dashboard" and "Logout" links (if logged in)

### Phase 7: Styling & UI

#### Step 7.1: Apply CSS Framework
- Configure Tailwind CSS or Material-UI theme
- Create consistent color scheme
- Define typography styles

#### Step 7.2: Style Components
- Style all pages and components
- Ensure responsive design (mobile-friendly)
- Add loading states
- Add error messages

#### Step 7.3: Add Icons
- Install icon library (React Icons or Material Icons)
- Add icons to buttons and navigation

### Phase 8: State Management

#### Step 8.1: Implement State Management
- Use React Context for global state (auth, user)
- Use local state for component-specific data
- Use React Query or SWR for server state (optional)

#### Step 8.2: Handle Loading & Error States
- Add loading spinners
- Display error messages
- Handle network errors gracefully

### Phase 9: Testing & Integration

#### Step 9.1: Test Authentication Flow
- Test signup flow
- Test login flow
- Test logout flow
- Test token persistence

#### Step 9.2: Test Provider Flow
- Test creating a course
- Test viewing course list
- Test editing a course
- Test deleting a course
- Test publish/unpublish

#### Step 9.3: Test Learner Flow
- Test searching by PIN code
- Test filtering results
- Test viewing course details
- Test pagination

#### Step 9.4: Test Integration
- Ensure frontend connects to backend API
- Test CORS configuration
- Test error handling
- Test edge cases

### Phase 10: Final Polish

#### Step 10.1: Add Form Validation
- Validate all forms on frontend
- Show helpful error messages
- Prevent invalid submissions

#### Step 10.2: Optimize Performance
- Add loading states
- Implement pagination properly
- Optimize re-renders
- Add debouncing for search

#### Step 10.3: Add User Feedback
- Add success messages (toasts/notifications)
- Add confirmation dialogs for delete actions
- Improve error messages

---

## 🔐 Security Considerations

### Backend Security
- Use BCrypt for password hashing (never store plain passwords)
- Validate all inputs (use Jakarta Bean Validation)
- Sanitize user inputs to prevent SQL injection (JPA handles this)
- Use HTTPS in production
- Implement rate limiting for authentication endpoints
- Validate JWT tokens properly
- Check ownership before allowing course modifications

### Frontend Security
- Store JWT token securely (localStorage or httpOnly cookies)
- Never expose sensitive data in client-side code
- Validate inputs on frontend (but always validate on backend too)
- Handle tokens expiration gracefully
- Implement proper logout (clear tokens)

---

## 📝 Environment Variables

### Backend (`application-local.properties`)
```
spring.datasource.url=jdbc:postgresql://localhost:5432/edtech_db
spring.datasource.username=postgres
spring.datasource.password=your_password
app.jwt.secret=your_jwt_secret_key_minimum_256_bits
app.jwt.expiration=86400000
```

### Frontend (`.env`)
```
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

---

## 🚀 Deployment Considerations

### Backend Deployment
- Build JAR file: `mvn clean package`
- Run JAR: `java -jar target/edtech.services-0.0.1-SNAPSHOT.jar`
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
5. Set up CI/CD pipeline (optional)
6. Prepare for frontend integration

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

### Frontend Issues
- **API calls failing**: Check CORS configuration and API base URL
- **Token not persisting**: Check localStorage implementation
- **Routing not working**: Verify React Router setup
- **Build errors**: Check Node version and dependencies

---

## 📖 Additional Resources

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- React Documentation: https://react.dev
- PostgreSQL Documentation: https://www.postgresql.org/docs
- JWT Documentation: https://jwt.io

---

**Note**: This is a step-by-step guide. Follow each phase sequentially. Do not skip steps as later phases depend on earlier ones. Test thoroughly after each phase before moving to the next.

