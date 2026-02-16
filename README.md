# 🚀 Shifterizator - Automatic Multi-Business Schedule Generator

📄 **Description - App Statement**

Shifterizator is a comprehensive backend system for automatic multi-business schedule generation and workforce management. This Spring Boot application provides a RESTful API for managing companies, employees, shifts, availability, and schedules across multiple business locations. The system supports role-based access control (RBAC) with different permission levels for super administrators, company administrators, shift managers, read-only managers, and employees.

Key features include:
- Multi-tenant architecture supporting multiple companies
- Automatic shift generation based on templates and employee availability
- Auto-scheduling of employees to shifts with candidate tiering
- Shift assignment confirmation workflow
- In-app notifications for shift assignments
- Employee availability and preference management
- Blackout days and special opening hours configuration
- File storage (Cloudflare R2 / S3-compatible) for profile pictures and uploads
- Comprehensive role-based access control
- JWT-based authentication and authorization
- RESTful API with pagination and filtering
- OpenAPI / Swagger documentation (non-production)

---

💻 **Technologies Used**

- **Java 21** - Programming language
- **Spring Boot 4.0.2** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **MySQL 8.0** - Production database
- **H2 Database** - In-memory database for unit tests
- **JWT (JJWT 0.11.5)** - JSON Web Token for authentication
- **SpringDoc OpenAPI 3** - API documentation (Swagger UI)
- **AWS SDK for S3** - S3-compatible storage (Cloudflare R2, AWS S3)
- **Lombok** - Code generation and boilerplate reduction
- **Maven** - Build and dependency management
- **Testcontainers** - Integration testing with Docker containers
- **JUnit 5** - Testing framework
- **AssertJ** - Fluent assertions for testing
- **Jackson** - JSON serialization/deserialization
- **Docker & Docker Compose** - Containerization and local development

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

3. **Set up environment and MySQL database:**
   
   Copy `.env.example` to `.env` and set required values (especially `JWT_SECRET` for production):
   ```bash
   cp .env.example .env
   ```

   **Option A: Using Docker Compose (Recommended for development)**
   ```bash
   docker-compose up -d
   ```
   For local development with the `dev` profile, ensure `MYSQL_DATABASE=shifterizator_dev` in your `.env`. The MySQL container will run on port 3307 with defaults: `root` / `root`.

   **Option B: Using local MySQL**
   - Create a MySQL database named `shifterizator_dev`
   - Update connection details in `src/main/resources/application-dev.yml` or via environment variables if needed

4. **Install dependencies:**
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
   
   # Run only integration tests (skips unit tests)
   ./mvnw verify -DskipTests
   ```

4. **API documentation (dev/test profiles):**
   When running with `dev` or `test` profile, Swagger UI is available at:
   ```
   http://localhost:8080/swagger-ui.html
   ```
   OpenAPI docs are at `/v3/api-docs`. Swagger is disabled in production for security.

5. **Default seeded users:**
   When running with `dev` or `test` profile (not `prod`), the following users are automatically created on first run if the database is empty:
   - **SUPERADMIN**: `superadmin` / `SuperAdmin1!`
   - **COMPANYADMIN**: `admin` / `Admin123!`
   - **SHIFTMANAGER**: `manager` / `Manager123!`
   - **EMPLOYEE**: `employee` / `Employee123!`

---

🌐 **Deployment**

1. **Prepare the production environment:**
   - Set up a MySQL 8.0 database server
   - Configure environment variables for database connection:
     - `MYSQLHOST` - Database host
     - `MYSQLPORT` - Database port
     - `MYSQLDATABASE` - Database name
     - `MYSQLUSER` - Database username
     - `MYSQLPASSWORD` - Database password
     - `PORT` - Application port (default: 8080)
     - `JWT_SECRET` - JWT signing secret (required)
   - For file uploads (profile pictures, etc.), configure S3-compatible storage (e.g. Cloudflare R2):
     - `R2_ACCESS_KEY`, `R2_SECRET_KEY`, `R2_ENDPOINT`, `R2_BUCKET`, `R2_PUBLIC_BASE_URL`

2. **Build the application:**
   ```bash
   ./mvnw clean package -DskipTests
   ```
   This creates a JAR file in `target/shifterizator-backend-0.0.1-SNAPSHOT.jar`

3. **Run the application:**
   ```bash
   java -jar target/shifterizator-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

4. **Using Docker (alternative):**
   ```bash
   # Build Docker image
   docker build -t shifterizator-backend .
   
   # Run container
   docker run -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e JWT_SECRET=your-jwt-secret \
     -e MYSQLHOST=your-db-host \
     -e MYSQLPORT=3306 \
     -e MYSQLDATABASE=shifterizator_prod \
     -e MYSQLUSER=your-user \
     -e MYSQLPASSWORD=your-password \
     shifterizator-backend
   ```

5. **Health check:**
   Once deployed, verify the application is running:
   ```bash
   curl http://localhost:8080/api/health
   ```

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

- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Docker deployment guide with environment variables and troubleshooting
- **Swagger UI** - Interactive API docs at `/swagger-ui.html` when running with dev/test profile

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
