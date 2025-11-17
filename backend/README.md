# School Management System - Backend API

A comprehensive REST API for school management built with Spring Boot, Spring Security, and PostgreSQL.

## 🚀 Quick Start

```bash
# Start the backend
mvn spring-boot:run

# Access Swagger UI
http://localhost:8080/api/swagger-ui.html
```

## 🔧 Technology Stack

- **Java 17** - Programming language
- **Spring Boot 3.1.5** - Framework
- **Spring Security 6** - Authentication & authorization
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Database (Aiven Cloud)
- **JWT** - Token-based authentication
- **Lombok** - Boilerplate code reduction
- **Swagger/OpenAPI 3.0** - API documentation

## 📋 Features

- 🔐 **JWT Authentication** - Secure token-based auth
- 👥 **User Management** - Multi-role user system
- 🏛️ **Staff Management** - Employee records and roles
- 🎓 **Student Management** - Student profiles and academics
- 📚 **Library Management** - Books and borrowing system
- 📊 **Attendance System** - Daily attendance tracking
- 🎯 **Grade Management** - Student assessments
- 💰 **Fee Management** - Payment and billing
- 📈 **Reports** - Academic and administrative reports

## 🗄️ Database

**Current:** PostgreSQL (Aiven Cloud)
- Host: school-clinicbooking.c.aivencloud.com
- Port: 14143
- Database: defaultdb

**Configuration:** `src/main/resources/application.yml`

## 📊 Test Data

Import sample data:
```bash
psql -U avnadmin -h school-clinicbooking.c.aivencloud.com -p 14143 -d defaultdb -f TEST_DATA_CORRECTED.sql
```

**Test Credentials:**
- Admin: `admin` / `Test@123`
- Principal: `principal` / `Test@123`
- Teacher: `teacher1` / `Test@123`
- Student: `student1` / `Test@123`

## 🛡️ Security

### Roles & Permissions
- **ADMIN** - Full system access
- **PRINCIPAL** - Management oversight
- **TEACHER** - Grade and attendance management
- **STUDENT** - Limited read access
- **LIBRARIAN** - Book management
- **ACCOUNTANT** - Fee management

### Authentication Flow
1. **Login** → `POST /api/v1/auth/login`
2. **Get JWT Token** → Include in Authorization header
3. **Access Protected APIs** → `Bearer <token>`

## 🌐 API Endpoints

| Endpoint | Description | Auth Required |
|----------|-------------|---------------|
| `POST /api/v1/auth/login` | User login | No |
| `GET /api/v1/users` | List users | Yes |
| `GET /api/v1/staff` | List staff | Yes |
| `GET /api/v1/students` | List students | Yes |
| `GET /api/v1/classes` | List classes | Yes |
| `GET /api/v1/library/books` | List books | Yes |

## 📖 Documentation

- **API Docs:** http://localhost:8080/api/swagger-ui.html
- **Database Setup:** [DATABASE_SETUP_INDEX.md](./DATABASE_SETUP_INDEX.md)
- **API Testing:** [API_TESTING_GUIDE.md](./API_TESTING_GUIDE.md)
- **Architecture:** [ARCHITECTURE.md](./ARCHITECTURE.md)
- **Development:** [DEVELOPMENT_GUIDE.md](./DEVELOPMENT_GUIDE.md)

## 🔧 Development

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL access
- IDE (IntelliJ IDEA recommended)

### Setup
1. **Clone repository**
2. **Configure database** in application.yml
3. **Install dependencies:** `mvn clean install`
4. **Run application:** `mvn spring-boot:run`
5. **Import test data** (optional)
6. **Access Swagger UI** for testing

### Build
```bash
# Development
mvn spring-boot:run

# Production build
mvn clean package
java -jar target/school-management-system-1.0.0.jar
```

## 📦 Dependencies

Key dependencies managed by Maven:
- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Boot Starter Data JPA
- PostgreSQL JDBC Driver
- JWT Support (jjwt)
- Lombok
- SpringDoc OpenAPI (Swagger)

## 🚦 Status

✅ **Authentication:** JWT-based security working  
✅ **Database:** PostgreSQL configured and connected  
✅ **APIs:** All endpoints functional  
✅ **Documentation:** Swagger UI available  
✅ **Test Data:** Sample data ready for import

---

**Port:** 8080  
**Context Path:** /api  
**Version:** 1.0.0  
**Last Updated:** November 17, 2025

## Features

- **Authentication & Authorization**: JWT-based authentication with Spring Security
- **Staff Management**: Manage teachers, administrators, and other staff members
- **Student Management**: Complete student profile management
- **Library Management**: Book catalog and borrowing system
- **Attendance Tracking**: Mark and track student attendance
- **Grade Management**: Record and manage student grades
- **Fee Management**: Manage student fees and payments
- **Role-Based Access Control**: Admin, Principal, Teacher, Student, Librarian, Accountant roles

## Technology Stack

- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Security 6**
- **Spring Data JPA**
- **MySQL 8.0**
- **JWT (JSON Web Tokens)**
- **Lombok**
- **Swagger/OpenAPI 3.0**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd backend
```

### 2. Create MySQL Database

```sql
CREATE DATABASE school_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure Application Properties

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/school_management?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## API Documentation

Once the application is running, access the Swagger UI at:
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api/v3/api-docs`

## API Endpoints

### Authentication Endpoints (`/v1/auth`)
- `POST /register` - Register a new user
- `POST /login` - Login and get JWT token
- `POST /refresh-token` - Refresh access token

### Staff Management (`/v1/staff`)
- `POST /` - Create staff member (Admin/Principal only)
- `GET /` - Get all staff members
- `GET /{id}` - Get staff by ID
- `GET /employee/{employeeId}` - Get staff by employee ID
- `GET /position/{position}` - Get staff by position
- `GET /department/{department}` - Get staff by department
- `GET /active` - Get active staff members
- `PUT /{id}` - Update staff (Admin/Principal only)
- `DELETE /{id}` - Delete staff (Admin/Principal only)

### Student Management (`/v1/students`)
- `POST /` - Create student (Admin/Principal only)
- `GET /` - Get all students
- `GET /{id}` - Get student by ID
- `GET /roll/{rollNumber}` - Get student by roll number
- `GET /class/{className}` - Get students by class
- `GET /class/{className}/section/{section}` - Get students by class and section
- `GET /active` - Get active students
- `PUT /{id}` - Update student (Admin/Principal only)
- `DELETE /{id}` - Delete student (Admin/Principal only)

### Library Management (`/v1/library`)
- `POST /books` - Add book (Admin/Librarian only)
- `GET /books` - Get all books
- `GET /books/{id}` - Get book by ID
- `GET /books/search?title=...` - Search books by title
- `GET /books/category/{category}` - Get books by category
- `GET /books/author/{author}` - Get books by author
- `GET /books/available` - Get available books
- `POST /books/{bookId}/borrow` - Borrow a book
- `POST /books/{bookId}/return` - Return a borrowed book
- `PUT /books/{id}` - Update book (Admin/Librarian only)
- `DELETE /books/{id}` - Delete book (Admin/Librarian only)

### Attendance Management (`/v1/attendance`)
- `POST /` - Mark attendance for student
- `POST /class` - Mark attendance for entire class
- `GET /{id}` - Get attendance record
- `GET /student/{studentId}` - Get student attendance records
- `GET /student/{studentId}/between` - Get attendance between dates
- `GET /date/{date}` - Get attendance by date
- `GET /student/{studentId}/percentage` - Get attendance percentage
- `PUT /{id}` - Update attendance record
- `DELETE /{id}` - Delete attendance record

### Grade Management (`/v1/grades`)
- `POST /` - Create grade record
- `GET /{id}` - Get grade record
- `GET /student/{studentId}` - Get all grades for student
- `GET /student/{studentId}/year/{academicYear}` - Get grades by year
- `GET /student/{studentId}/subject/{subject}` - Get grades by subject
- `GET /student/{studentId}/average` - Get average percentage
- `PUT /{id}` - Update grade record
- `DELETE /{id}` - Delete grade record

### Fee Management (`/v1/fees`)
- `POST /` - Create fee record
- `GET /{id}` - Get fee record
- `GET /student/{studentId}` - Get student fees
- `GET /student/{studentId}/year/{academicYear}` - Get fees by year
- `GET /student/{studentId}/pending` - Get pending fees
- `GET /status/{status}` - Get fees by status
- `POST /{feeId}/payment` - Process fee payment
- `PUT /{id}` - Update fee record
- `DELETE /{id}` - Delete fee record

## Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

Get a token by logging in:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

## Default Roles

- **ADMIN**: Full system access
- **PRINCIPAL**: School administration
- **TEACHER**: Teaching and classroom management
- **STUDENT**: Student access
- **LIBRARIAN**: Library management
- **ACCOUNTANT**: Finance management
- **PARENT**: Parent portal access

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/schoolmanagement/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── exception/       # Custom Exceptions
│   │   │   ├── repository/      # JPA Repositories
│   │   │   ├── security/        # Security configuration
│   │   │   ├── service/         # Business Logic
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       └── application.yml  # Application configuration
│   └── test/
├── pom.xml
└── README.md
```

## Security Features

- JWT token-based authentication
- Role-based access control (RBAC)
- Password encryption using BCrypt
- Method-level security annotations
- CSRF protection disabled (for stateless API)
- CORS support

## Database Schema

The application automatically creates all tables through Hibernate JPA using the `ddl-auto: update` configuration. Tables include:

- users
- user_permissions
- staff
- students
- library_books
- book_transactions
- attendance
- grades
- fees
- classes

## Error Handling

The API provides consistent error responses with status codes and messages:

```json
{
  "status": "NOT_FOUND",
  "message": "Student not found with id: 123",
  "code": 404,
  "path": "/api/v1/students/123",
  "timestamp": "2023-11-16T10:30:00"
}
```

## Performance Considerations

- Lazy loading for relationships
- Pagination support for list endpoints
- Query optimization with proper indexes
- Connection pooling with HikariCP

## Future Enhancements

- [ ] Advanced reporting features
- [ ] Bulk import/export functionality
- [ ] Message notification system
- [ ] Timetable management
- [ ] Exam management system
- [ ] Parent-Teacher communication module
- [ ] Mobile app API endpoints
- [ ] Advanced analytics and dashboards

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, email support@schoolmanagement.com or create an issue in the repository.

## Contact

- **Author**: School Management Team
- **Email**: team@schoolmanagement.com
- **Website**: https://www.schoolmanagement.com

