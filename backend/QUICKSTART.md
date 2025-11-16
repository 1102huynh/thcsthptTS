# Quick Start Guide - School Management System Backend

## 📋 Prerequisites

Before starting, ensure you have:
- Java 17 or higher installed
- Maven 3.6 or higher installed
- MySQL 8.0 or higher running
- Git (optional)

## 🔧 Installation Steps

### Step 1: Create MySQL Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE school_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### Step 2: Configure Application

1. Navigate to `backend/src/main/resources/`
2. Edit `application.yml`
3. Update database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/school_management?useSSL=false&serverTimezone=UTC
    username: root
    password: your_mysql_password
```

### Step 3: Build the Project

```bash
cd backend
mvn clean install
```

### Step 4: Run the Application

Option A - Using Maven:
```bash
mvn spring-boot:run
```

Option B - Using Java directly:
```bash
java -jar target/school-management-system-1.0.0.jar
```

### Step 5: Verify Installation

1. Open browser and go to: `http://localhost:8080/api/swagger-ui.html`
2. Check if Swagger UI loads successfully
3. Database tables should be auto-created

## 🔐 First-Time Setup

### Create Default Admin User

Use Swagger UI or cURL to register:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@school.com",
    "password": "Admin@123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

### Login to Get Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'
```

Response will include `accessToken` - use this in Authorization header.

## 📚 API Usage Examples

### 1. Authentication
```bash
# Register
POST /api/v1/auth/register

# Login
POST /api/v1/auth/login

# Refresh Token
POST /api/v1/auth/refresh-token
```

### 2. Staff Management
```bash
# Create Staff
POST /api/v1/staff
Headers: Authorization: Bearer <token>
Body: {staff details}

# Get All Staff
GET /api/v1/staff
Headers: Authorization: Bearer <token>

# Get Staff by ID
GET /api/v1/staff/{id}
Headers: Authorization: Bearer <token>
```

### 3. Student Management
```bash
# Create Student
POST /api/v1/students
Headers: Authorization: Bearer <token>

# Get All Students
GET /api/v1/students
Headers: Authorization: Bearer <token>
```

### 4. Library Management
```bash
# Add Book
POST /api/v1/library/books
Headers: Authorization: Bearer <token>

# Get All Books
GET /api/v1/library/books

# Borrow Book
POST /api/v1/library/books/{bookId}/borrow
Headers: Authorization: Bearer <token>
```

### 5. Attendance
```bash
# Mark Attendance
POST /api/v1/attendance
Headers: Authorization: Bearer <token>

# Get Student Attendance
GET /api/v1/attendance/student/{studentId}
Headers: Authorization: Bearer <token>
```

### 6. Grades
```bash
# Create Grade
POST /api/v1/grades
Headers: Authorization: Bearer <token>

# Get Student Grades
GET /api/v1/grades/student/{studentId}
Headers: Authorization: Bearer <token>
```

### 7. Fees
```bash
# Create Fee
POST /api/v1/fees
Headers: Authorization: Bearer <token>

# Get Student Fees
GET /api/v1/fees/student/{studentId}
Headers: Authorization: Bearer <token>

# Process Payment
POST /api/v1/fees/{feeId}/payment?amount=100&paymentMethod=ONLINE
Headers: Authorization: Bearer <token>
```

## 🌐 API Endpoints Summary

| Module | Endpoints |
|--------|-----------|
| **Auth** | `/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/auth/refresh-token` |
| **Staff** | `/api/v1/staff/*` |
| **Students** | `/api/v1/students/*` |
| **Library** | `/api/v1/library/books/*` |
| **Attendance** | `/api/v1/attendance/*` |
| **Grades** | `/api/v1/grades/*` |
| **Fees** | `/api/v1/fees/*` |

## 🔑 Default Roles & Permissions

### Roles Available:
- **ADMIN** - Full system access
- **PRINCIPAL** - School administration
- **TEACHER** - Class management, attendance, grades
- **STUDENT** - Student portal
- **LIBRARIAN** - Library management
- **ACCOUNTANT** - Financial management
- **PARENT** - Parent portal (future)

## 📝 Important Configuration

### JWT Secret (Change for Production!)
File: `src/main/resources/application.yml`

```yaml
app:
  jwt:
    secret: "your-secret-key-change-this-in-production-make-it-at-least-256-bits-long"
    expiration: 86400000  # 24 hours
```

### Database Connection
Ensure MySQL is running before starting the application.

### CORS (if needed)
Add to SecurityConfig for frontend integration:
```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
        }
    };
}
```

## 🆘 Troubleshooting

### Database Connection Error
```
ERROR - Failed to get a connection...
```
**Solution**: Ensure MySQL is running and credentials in application.yml are correct.

### Port Already in Use
```
java.net.BindException: Address already in use
```
**Solution**: Change port in application.yml:
```yaml
server:
  port: 8081  # Change to different port
```

### JWT Expiration
**Solution**: Get new token using refresh token endpoint or login again.

### Permission Denied
**Solution**: Ensure user has appropriate role. Check @PreAuthorize annotations.

## 📊 Database Schema

The following tables are auto-created:
- users
- user_permissions
- staff
- students
- classes
- attendance
- grades
- fees
- library_books
- book_transactions

## 🚀 Deployment

### Docker Deployment
```dockerfile
FROM openjdk:17
COPY target/school-management-system-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Build Docker Image
```bash
docker build -t school-management:1.0 .
docker run -p 8080:8080 school-management:1.0
```

## 📚 Project Structure Reference

```
backend/
├── pom.xml                    # Maven dependencies
├── README.md                  # Full documentation
├── IMPLEMENTATION_SUMMARY.md  # Complete summary
├── QUICKSTART.md             # This file
└── src/
    └── main/
        ├── java/com/schoolmanagement/
        │   ├── controller/   # REST endpoints
        │   ├── service/      # Business logic
        │   ├── entity/       # Database models
        │   ├── repository/   # Data access
        │   ├── security/     # JWT & Security
        │   ├── exception/    # Error handling
        │   ├── dto/          # Data transfer
        │   └── config/       # Configuration
        └── resources/
            └── application.yml  # Settings
```

## ✅ Verification Checklist

- [ ] Java 17 installed (`java -version`)
- [ ] Maven installed (`mvn -version`)
- [ ] MySQL running and school_management database created
- [ ] application.yml configured with correct database credentials
- [ ] Project built successfully (`mvn clean install`)
- [ ] Application starts without errors
- [ ] Swagger UI accessible at http://localhost:8080/api/swagger-ui.html
- [ ] Can register a new user
- [ ] Can login and receive JWT token
- [ ] Can access protected endpoints with token

## 🎓 Next Steps

1. **Integrate with Frontend**: Update CORS settings and connect to your frontend
2. **Add More Features**: Extend functionality with additional endpoints
3. **Implement Notifications**: Add email/SMS notifications
4. **Setup CI/CD**: Configure GitHub Actions or Jenkins
5. **Performance Optimization**: Add caching, pagination limits
6. **Advanced Security**: Implement OAuth2, Two-Factor Auth

## 📞 Support & Documentation

- Full Documentation: See `README.md`
- Implementation Details: See `IMPLEMENTATION_SUMMARY.md`
- API Docs: Visit Swagger UI at runtime

## 📄 License

This project is licensed under MIT License.

---

**Status**: ✅ Ready to Use
**Last Updated**: November 16, 2025

