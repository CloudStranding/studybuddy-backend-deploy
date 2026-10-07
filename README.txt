StudyBuddy - Backend Server 
===========================
Group 4

Members:
  Jayson Leander (2155036)
  Wenjie Li (2090937)
  Xiangjie Dong (2072645)
  Yanhang Luo (2123061)
  Yingyao Feng (2094770)
  Yue Peng (2104431)


Project Overview
----------------
This is the REST API backend for the StudyBuddy application, built
with Spring Boot 3.5. It provides endpoints for user authentication,
meeting matching, focus session tracking, task management, and content
moderation. Data is persisted in PostgreSQL (production) or H2
(local development). Authentication uses stateless JWT tokens.


Build & Run Instructions
------------------------
Prerequisites: Java 21, Maven (or use the included Maven wrapper).

Local development (H2 in-memory database):
  ./mvnw spring-boot:run

The server starts on http://localhost:8080 by default.
H2 console is available at http://localhost:8080/h2-console during
local development.

Production deployment (Docker + PostgreSQL):
  docker compose up --build -d

The Docker setup uses PostgreSQL 16 with persistent volume storage.
Environment variables for the database connection are configured in
compose.yml. The Dockerfile activates the "docker" Spring profile
which reads from application-docker.properties.

The backend is also deployed on Railway:
  https://studybuddy-backend-deploy-production.up.railway.app/


Third-Party Java Sources
------------------------
This project does NOT contain any third-party Java source files used
unchanged. All external libraries are consumed exclusively through
Maven dependencies (declared in pom.xml) and are not included as
source files in this repository.

External libraries used (via Maven dependencies, not as source):
  - org.springframework.boot:spring-boot-starter-web      (REST API)
  - org.springframework.boot:spring-boot-starter-data-jpa  (JPA/Hibernate)
  - org.springframework.boot:spring-boot-starter-security  (Security)
  - org.springframework.boot:spring-boot-starter-validation (Bean validation)
  - io.jsonwebtoken:jjwt-api:0.12.6                        (JWT tokens)
  - io.jsonwebtoken:jjwt-impl:0.12.6
  - io.jsonwebtoken:jjwt-jackson:0.12.6
  - org.postgresql:postgresql                               (PostgreSQL driver)
  - com.h2database:h2                                       (H2 for local dev)


Test Environment
----------------
Unit tests are located in: src/test/java/

Testing frameworks used:
  - Spring Boot Starter Test (includes JUnit 5, Mockito, AssertJ)
  - Spring Security Test
  - H2 in-memory database (for integration tests)

Code coverage is measured using JaCoCo (configured in pom.xml) and
results are uploaded to SonarQube for analysis.

SonarQube reads the XML report at:
  target/jacoco-report/jacoco.xml

To run tests:
  ./mvnw test

To run tests with coverage report:
  ./mvnw test jacoco:report

Coverage report is generated at:
  target/site/jacoco/index.html

To run SonarQube analysis:
  ./mvnw verify sonar:sonar -DskipTests

SonarQube project: 2IS70 Backend Deployment


API Endpoints Overview
----------------------
All endpoints require JWT authentication except /api/auth/*.

Authentication:
  POST /api/auth/register       Create a new account
  POST /api/auth/login          Login and receive JWT token
  POST /api/auth/logout         Logout (client discards token)

User Profile:
  GET  /api/user/profile        Get current user profile
  PUT  /api/user/update         Update username and email
  PUT  /api/user/change-password Change password
  DELETE /api/user/delete        Delete account

Matching:
  POST /api/matching/join/{duration}       Join or create a meeting
  POST /api/matching/leave/{channelName}   Leave a meeting

Focus Sessions:
  POST /api/session/start          Start a new session
  PUT  /api/session/{id}/complete  Mark session completed
  PUT  /api/session/{id}/incomplete Mark session incomplete
  GET  /api/session/history        Get session history
  GET  /api/session/statistics     Get focus statistics

Tasks:
  POST   /api/tasks               Create a task
  GET    /api/tasks               Get all tasks
  PUT    /api/tasks/{id}/complete  Mark task completed
  PUT    /api/tasks/{id}/incomplete Uncheck task
  DELETE /api/tasks/{id}           Delete a task

Moderation (admin-only endpoints marked with *):
  POST /api/moderation/report                    Submit a report
  PUT  /api/moderation/action/{userId}/{action}  * Apply admin action
  PUT  /api/moderation/report/{id}/status/{s}    * Update report status
  GET  /api/moderation/reports                   * List all reports


Project Structure
-----------------
src/main/java/com/studybuddy/
  ├── account/
  │   ├── controller/    AuthController, UserController
  │   ├── service/       AuthService, UserService
  │   ├── model/         User (JPA entity)
  │   ├── repository/    UserRepository
  │   └── dto/           Request/Response DTOs
  ├── matching/
  │   ├── controller/    MatchingController
  │   ├── service/       MatchingService
  │   ├── model/         Meeting (JPA entity)
  │   ├── repository/    MeetingRepository
  │   └── dto/           JoinMeetingResponse, etc.
  ├── session/
  │   ├── controller/    FocusSessionController
  │   ├── model/         FocusSession (JPA entity)
  │   ├── repository/    FocusSessionRepository
  │   └── dto/           StartSessionRequest, etc.
  ├── task/
  │   ├── controller/    TaskController
  │   ├── model/         Task (JPA entity)
  │   ├── repository/    TaskRepository
  │   └── dto/           CreateTaskRequest, etc.
  ├── moderation/
  │   ├── controller/    ModerationController
  │   ├── model/         Report (JPA entity)
  │   ├── repository/    ReportRepository
  │   └── dto/           ReportRequest, ReportResponse
  └── security/
      ├── SecurityConfig.java   Spring Security configuration
      ├── JwtAuthFilter.java    JWT authentication filter
      └── JwtUtil.java          JWT token utilities
