# Development Guide - School Management System Backend

## 🛠️ Development Setup

### Prerequisites Installation

#### 1. Install Java 17
```bash
# Windows - Download from oracle.com or use Chocolatey
choco install openjdk17

# Verify installation
java -version
javac -version
```

#### 2. Install Maven
```bash
# Windows - Download from maven.apache.org or use Chocolatey
choco install maven

# Verify installation
mvn -version
```

#### 3. Install MySQL 8.0
```bash
# Windows - Download from mysql.com or use Chocolatey
choco install mysql

# Start MySQL service
net start MySQL80

# Connect to MySQL
mysql -u root -p
```

#### 4. Install Git
```bash
# Windows - Download from git-scm.com or use Chocolatey
choco install git

# Verify installation
git --version
```

---

## 📁 Project Setup

### 1. Clone Repository
```bash
git clone <repository-url>
cd backend
```

### 2. Create Database
```bash
mysql -u root -p < schema.sql
# or manually:
mysql -u root -p
mysql> CREATE DATABASE school_management CHARACTER SET utf8mb4;
```

### 3. Configure IDE

#### IntelliJ IDEA
1. Open `File → Open`
2. Select the `backend` folder
3. Click `Open as Project`
4. Wait for Maven indexing
5. Mark `src/main/java` as Sources Root
6. Mark `src/test/java` as Test Sources Root

#### Eclipse
1. Open `File → Import`
2. Select `Maven → Existing Maven Projects`
3. Browse to `backend` folder
4. Click `Finish`

#### VS Code
1. Install `Extension Pack for Java`
2. Open the `backend` folder
3. Extensions will auto-configure Maven

### 4. Build Project
```bash
cd backend
mvn clean install
```

---

## 🏃 Running the Application

### Option 1: Maven
```bash
mvn spring-boot:run
```

### Option 2: Direct Java Execution
```bash
mvn clean package
java -jar target/school-management-system-1.0.0.jar
```

### Option 3: IDE Run
- IntelliJ: Right-click `SchoolManagementApplication.java` → Run
- Eclipse: Right-click Project → Run As → Spring Boot App
- VS Code: Click `Run` button on `main()` method

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=AuthenticationServiceTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=AuthenticationServiceTest#testLogin
```

### Generate Test Coverage Report
```bash
mvn clean test jacoco:report
# Report: target/site/jacoco/index.html
```

---

## 📝 Code Organization

### Package Structure
```
com.schoolmanagement
├── config/          # Configuration classes
├── controller/      # REST Controllers
├── service/         # Business Logic
├── repository/      # Data Access
├── entity/          # JPA Entities
├── dto/             # Data Transfer Objects
├── security/        # Security Configuration
├── exception/       # Custom Exceptions
└── util/            # Utility Classes
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `StudentService.java`)
- **Methods**: camelCase (e.g., `getStudentById()`)
- **Variables**: camelCase (e.g., `studentId`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase (e.g., `com.schoolmanagement.service`)

---

## 🔄 Git Workflow

### Creating a New Feature
```bash
# Create feature branch
git checkout -b feature/student-management

# Make changes and commit
git add .
git commit -m "Add student management functionality"

# Push to remote
git push origin feature/student-management

# Create Pull Request
```

### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

Types: feat, fix, docs, style, refactor, test, chore
Example: `feat(student): add student creation endpoint`

---

## 🐛 Debugging

### Debug Mode in IDE

#### IntelliJ IDEA
1. Set breakpoints (click line number)
2. Right-click project → Debug 'SchoolManagementApplication'
3. Use Debug panel to step through code

#### Eclipse
1. Set breakpoints (double-click line number)
2. Right-click project → Debug As → Spring Boot App
3. Use Debug perspective to inspect variables

#### VS Code
1. Install `Debugger for Java`
2. Set breakpoints
3. Press `F5` to start debugging

### Logging
```java
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);

logger.debug("Debug message");
logger.info("Info message");
logger.warn("Warning message");
logger.error("Error message");
```

### View Application Logs
```bash
# Tail logs
tail -f logs/application.log

# Filter logs
grep "ERROR" logs/application.log
```

---

## 📚 API Development

### Creating a New Endpoint

#### 1. Create Entity (if needed)
```java
@Entity
@Table(name = "my_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // fields...
}
```

#### 2. Create Repository
```java
@Repository
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    // custom methods...
}
```

#### 3. Create Service
```java
@Service
@AllArgsConstructor
@Transactional
public class MyService {
    private MyRepository myRepository;
    
    public MyEntity create(MyEntity entity) {
        return myRepository.save(entity);
    }
}
```

#### 4. Create Controller
```java
@RestController
@RequestMapping("/v1/my-resource")
@AllArgsConstructor
public class MyController {
    private MyService myService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MyEntity> create(@RequestBody MyEntity entity) {
        MyEntity created = myService.create(entity);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
```

### Testing Endpoints with cURL
```bash
# GET request
curl -X GET http://localhost:8080/api/v1/my-resource/1 \
  -H "Authorization: Bearer <token>"

# POST request
curl -X POST http://localhost:8080/api/v1/my-resource \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"value"}'

# PUT request
curl -X PUT http://localhost:8080/api/v1/my-resource/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"updated-value"}'

# DELETE request
curl -X DELETE http://localhost:8080/api/v1/my-resource/1 \
  -H "Authorization: Bearer <token>"
```

---

## 🗄️ Database Development

### Creating Migrations (Manual)

1. Create SQL script in `src/main/resources/db/migration/`
```sql
-- V1__Initial_schema.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
```

### Database Console
Access H2 Console for testing:
```
http://localhost:8080/api/h2-console
```

### Query Examples
```sql
-- View all users
SELECT * FROM users;

-- Find user by email
SELECT * FROM users WHERE email = 'user@example.com';

-- Count students per class
SELECT class_name, COUNT(*) FROM students GROUP BY class_name;

-- Get pending fees
SELECT * FROM fees WHERE status = 'PENDING';
```

---

## 📦 Dependency Management

### Add New Dependency
```bash
# Search for dependency
mvn dependency:tree

# Add to pom.xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>my-lib</artifactId>
    <version>1.0.0</version>
</dependency>

# Update Maven
mvn clean install
```

### Common Dependencies
```xml
<!-- Lombok for reducing boilerplate -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>

<!-- JWT tokens -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>

<!-- Email sending -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- PDF generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
</dependency>
```

---

## 🔐 Security Best Practices

### Environment Variables
```bash
# Set JWT secret
export JWT_SECRET="your-secure-secret-key-here"

# Set database password
export DB_PASSWORD="your-db-password"

# Start application
mvn spring-boot:run
```

### .env File (Development only)
```
JWT_SECRET=dev-secret-key-only-for-dev
DB_PASSWORD=dev-password
```

### Never Commit Secrets
```
# .gitignore
.env
*.pem
*.key
application-prod.yml
```

---

## 📊 Performance Optimization

### Enable SQL Logging
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

### Query Analysis
```java
// Bad - N+1 query problem
List<Student> students = studentRepository.findAll();
for (Student student : students) {
    System.out.println(student.getUser().getUsername()); // Extra query per student
}

// Good - Fetch relationships
List<Student> students = studentRepository.findAllWithUser();

// Use @NamedEntityGraph or @EntityGraph
@NamedEntityGraph(name = "Student.user", attributeNodes = @NamedAttributeNode("user"))
public class Student { }
```

### Connection Pool Configuration
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 🚀 Build & Deployment

### Build JAR File
```bash
mvn clean package
```

### Create Docker Image
```dockerfile
FROM openjdk:17-slim
COPY target/school-management-system-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Build Docker Image
```bash
docker build -t school-management:1.0 .
```

### Run Docker Container
```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/school_management \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  school-management:1.0
```

---

## 📚 Useful Resources

### Spring Boot Documentation
- https://spring.io/projects/spring-boot
- https://spring.io/guides/gs/spring-boot/

### Spring Security
- https://spring.io/projects/spring-security
- https://docs.spring.io/spring-security/reference/

### JWT Tokens
- https://jwt.io/
- https://github.com/jwtk/jjwt

### MySQL
- https://dev.mysql.com/doc/
- https://mysql.com/

### Maven
- https://maven.apache.org/
- https://maven.apache.org/guides/

---

## 🆘 Troubleshooting

### Issue: Cannot Connect to Database
```
Error: com.mysql.cj.jdbc.exceptions.CommunicationsException
```
**Solution**:
- Ensure MySQL is running: `net start MySQL80`
- Check connection string in `application.yml`
- Verify database exists: `CREATE DATABASE school_management;`
- Check username and password

### Issue: Port Already in Use
```
java.net.BindException: Address already in use: bind
```
**Solution**:
- Change port in `application.yml`: `server.port: 8081`
- Or kill process: `netstat -ano | findstr :8080` then `taskkill /PID <PID> /F`

### Issue: JWT Token Expired
**Solution**:
- Get new token by logging in again
- Or use refresh token endpoint

### Issue: Access Denied
```
403 Forbidden
```
**Solution**:
- Check user role with required permission
- Verify @PreAuthorize annotation
- Check JWT token is valid

### Issue: Maven Build Fails
**Solution**:
```bash
# Clear Maven cache
mvn clean
rm -rf ~/.m2/repository

# Rebuild
mvn clean install
```

---

## 📋 Development Checklist

- [ ] Java 17 installed
- [ ] Maven installed
- [ ] MySQL running
- [ ] Database created
- [ ] application.yml configured
- [ ] Project builds successfully
- [ ] Application starts without errors
- [ ] Swagger UI accessible
- [ ] Can login and get token
- [ ] Can access protected endpoints

---

## 🎯 Next Development Steps

1. **Add Unit Tests**: Write tests for services
2. **Add Integration Tests**: Test full workflow
3. **Add Validation**: Implement request validation
4. **Add Logging**: Add comprehensive logging
5. **Add Caching**: Implement caching layer
6. **Add Monitoring**: Add application monitoring
7. **Add Documentation**: Generate OpenAPI docs
8. **Add CI/CD**: Setup GitHub Actions

---

**Last Updated**: November 16, 2025
**Status**: ✅ Complete

