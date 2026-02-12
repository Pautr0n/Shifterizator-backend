# 🚀 Shifterizator - Automatic Multi-Business Schedule Generator

📄 **Description - App Statement**

Shifterizator is a comprehensive backend system for automatic multi-business schedule generation and workforce management. This Spring Boot application provides a RESTful API for managing companies, employees, shifts, availability, and schedules across multiple business locations. The system supports role-based access control (RBAC) with different permission levels for super administrators, company administrators, shift managers, and employees.

Key features include:
- Multi-tenant architecture supporting multiple companies
- Automatic shift generation based on templates and employee availability
- Employee availability and preference management
- Blackout days and special opening hours configuration
- Comprehensive role-based access control
- JWT-based authentication and authorization
- RESTful API with pagination and filtering capabilities

---

💻 **Technologies Used**

- **Java 21** - Programming language
- **Spring Boot 4.0.2** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **MySQL 8.0** - Production database
- **H2 Database** - In-memory database for unit tests
- **JWT (JJWT 0.11.5)** - JSON Web Token for authentication
- **Lombok** - Code generation and boilerplate reduction
- **Maven** - Build and dependency management
- **Testcontainers** - Integration testing with Docker containers
- **JUnit 5** - Testing framework
- **AssertJ** - Fluent assertions for testing
- **Jackson** - JSON serialization/deserialization
- **Docker & Docker Compose** - Containerization and local development
- **SpringDoc OpenAPI 3** - API documentation (Swagger UI)
- **Spring Boot Actuator** - Application monitoring and health checks

---

📋 **Requirements**

- **Java 21** or higher
- **Maven 3.6+** (or use included Maven Wrapper)
- **MySQL 8.0** (for production and development)
- **Docker** (optional, for running MySQL via Docker Compose)
- **Git** (for cloning the repository)

---

🛠️ **Installation**

1. **Clone this repository:**
   ```bash
   git clone https://github.com/username/shifterizator-backend.git
   ```

2. **Navigate to the project directory:**
   ```bash
   cd shifterizator-backend
   ```

3. **Set up environment variables:**
   
   Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
   
   Edit `.env` and configure:
   - `JWT_SECRET` - Generate a secure secret: `openssl rand -base64 64`
   - Database credentials (if using Docker Compose, defaults are provided)
   - `ENABLE_DEMO_DATA=true` - Enable demo data seeding for presentations

4. **Set up MySQL database:**
   
   **Option A: Using Docker Compose (Recommended)**
   ```bash
   docker-compose up -d --build
   ```
   This will start both MySQL and the application containers:
   - MySQL on port 3307 (configurable via `MYSQL_PORT` in `.env`)
   - Application on port 8080 (configurable via `APP_PORT` in `.env`)
   - Database credentials configured via `.env` file
   - See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed deployment guide

   **Option B: Using local MySQL**
   - Create a MySQL database (default: `shifterizator_dev`)
   - Update connection details in `src/main/resources/application-dev.yml` if needed
   - Run the application: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

5. **Install dependencies (if not using Docker):**
   ```bash
   ./mvnw clean install
   ```
   Or if you have Maven installed:
   ```bash
   mvn clean install
   ```

---

▶️ **Execution**

1. **Development mode:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   Or:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   The application will start on `http://localhost:8080`
   
   **Access Swagger UI (Development only):**
   - Navigate to `http://localhost:8080/swagger-ui.html`
   - Swagger is automatically disabled in production profile

2. **Using Maven Wrapper (Windows):**
   ```bash
   mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Running tests:**
   ```bash
   # Run all tests (unit + integration)
   ./mvnw verify
   
   # Run only unit tests
   ./mvnw test
   
   # Run only integration tests
   ./mvnw verify -DskipTests
   ```

4. **Demo Data (Presentation Mode):**
   
   When `ENABLE_DEMO_DATA=true` is set in `.env`, the application automatically seeds:
   - 1 SuperAdmin user (`superadmin` / `SuperAdmin1!`)
   - 2 Companies with full demo data
   - Multiple users with different roles
   - Employees, positions, locations, and sample shifts
   
   **Note:** Demo data only seeds if the database is empty (no users exist).

---

🌐 **Deployment**

### Docker Compose Deployment (Recommended)

The easiest way to deploy locally or in production is using Docker Compose:

1. **Configure environment variables:**
   - Copy `.env.example` to `.env`
   - Set `JWT_SECRET` (generate with `openssl rand -base64 64`)
   - Configure database credentials
   - Set `DDL_AUTO=validate` for production
   - Set `ENABLE_DEMO_DATA=false` for production

2. **Deploy:**
   ```bash
   docker-compose up -d --build
   ```

3. **Verify deployment:**
   ```bash
   docker-compose ps
   curl http://localhost:8080/api/health
   ```

For detailed deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md).

### Traditional Deployment

1. **Prepare the production environment:**
   - Set up a MySQL 8.0 database server
   - Configure environment variables (see `.env.example`)

2. **Build the application:**
   ```bash
   ./mvnw clean package -DskipTests
   ```

3. **Run the application:**
   ```bash
   java -jar target/shifterizator-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

**Important Production Settings:**
- Swagger/OpenAPI is automatically disabled in `prod` profile
- Actuator endpoints are restricted to `/health` and `/info`
- CORS must be configured via `CORS_ALLOWED_ORIGINS` environment variable
- JWT secret must be provided via `JWT_SECRET` environment variable

---

🤝 **Contributions**

Contributions are welcome! Please follow these steps to contribute:

1. **Fork the repository**

2. **Create a new branch:**
   ```bash
   git checkout -b feature/NewFeature
   ```

3. **Make your changes and commit them:**
   ```bash
   git add .
   git commit -m 'Add New Feature'
   ```

4. **Push changes to your branch:**
   ```bash
   git push origin feature/NewFeature
   ```

5. **Create a pull request**

**Guidelines:**
- Follow the existing code style and conventions
- Write unit tests for new features
- Write integration tests for API endpoints
- Update documentation as needed
- Ensure all tests pass (`mvn verify`)
- Follow the role-based access control patterns established in the codebase

---

## 📚 Additional Resources

- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Comprehensive Docker deployment guide
- **[private-doc/](private-doc/)** - Internal documentation (requirements, planning, integration guides)

### API Documentation

- **Swagger UI**: Available at `http://localhost:8080/swagger-ui.html` (development only)
- **OpenAPI JSON**: Available at `http://localhost:8080/v3/api-docs` (development only)
- **Note**: API documentation is disabled in production profile for security

---

## 🔐 Security

This application uses JWT-based authentication. Access tokens expire after 15 minutes, and refresh tokens expire after 7 days. All endpoints are protected by role-based access control (RBAC) with the following hierarchy:

- **SUPERADMIN** → Full system access
- **COMPANYADMIN** → Company-level access
- **SHIFTMANAGER** → Location-level access
- **READONLYMANAGER** → Read-only location access
- **EMPLOYEE** → Self-service access

---

## 📝 License

See the [LICENSE](LICENSE) file for details.
