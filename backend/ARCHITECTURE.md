# School Management System - Architecture & Design

## 📐 System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│                  (Web/Mobile Application)                        │
└────────────────────┬────────────────────────────────────────────┘
                     │ HTTP/REST
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API GATEWAY LAYER                             │
│              (Spring Boot Application)                          │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Security Filter Chain                       │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  CORS Filter → JWT Filter → Authentication Filter  │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                    REST CONTROLLERS                             │
│  ┌─────────────┬──────────┬─────────┬──────────┬──────┐        │
│  │   Auth      │  Staff   │ Student │ Library  │ ...  │        │
│  │ Controller  │Controller│Controller│Controller│      │        │
│  └─────────────┴──────────┴─────────┴──────────┴──────┘        │
├─────────────────────────────────────────────────────────────────┤
│                      SERVICE LAYER                              │
│  ┌──────────────┬──────────────┬───────────┬─────────────┐    │
│  │Authentication│Staff Service │Student Srv│LibraryService│   │
│  │  Service     │              │           │             │    │
│  ├──────────────┼──────────────┼───────────┼─────────────┤    │
│  │Attendance    │Grade Service │Fee Service│  Utils      │    │
│  │  Service     │              │           │             │    │
│  └──────────────┴──────────────┴───────────┴─────────────┘    │
├─────────────────────────────────────────────────────────────────┤
│                    REPOSITORY LAYER                             │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐      │
│  │User Repo │Staff Repo│Student R │Class Repo│Fee Repo  │      │
│  ├──────────┼──────────┼──────────┼──────────┼──────────┤      │
│  │Attendance│Grade Repo│Library B │Book Trans│Permission│      │
│  │   Repo   │          │  Repo    │   Repo   │  Repo    │      │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘      │
├─────────────────────────────────────────────────────────────────┤
│                   ORM LAYER (JPA/Hibernate)                     │
├─────────────────────────────────────────────────────────────────┤
│                   DATABASE LAYER (MySQL)                        │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Request Flow Diagram

```
User Request
    ↓
HTTP Request (with JWT Token)
    ↓
Spring Boot Application
    ↓
Security Filters
    ├─ CORS Filter
    ├─ JWT Authentication Filter
    └─ Authorization Filter
    ↓
Request Dispatcher (DispatcherServlet)
    ↓
Route to Appropriate Controller
    ↓
Controller Method Execution
    ├─ Request Validation
    ├─ Call Service Layer
    └─ Return DTO Response
    ↓
Service Layer Execution
    ├─ Business Logic Processing
    ├─ Data Validation
    ├─ Permission Checking
    └─ Call Repository Layer
    ↓
Repository Layer
    ├─ Query Database
    └─ Return Entity Objects
    ↓
Service Layer Processing
    ├─ Data Transformation
    ├─ Additional Processing
    └─ Return to Controller
    ↓
Controller Response Handling
    ├─ DTO Conversion
    ├─ HTTP Status Code
    └─ JSON Serialization
    ↓
HTTP Response
    ↓
Client
```

## 🏗️ Module Architecture

### Authentication Module
```
AuthController
    ↓
AuthenticationService
    ├─ UserRepository
    ├─ PasswordEncoder
    ├─ AuthenticationManager
    └─ JwtTokenProvider
```

### Staff Management Module
```
StaffController
    ↓
StaffService
    ├─ StaffRepository
    ├─ UserRepository
    └─ Business Logic
```

### Student Management Module
```
StudentController
    ↓
StudentService
    ├─ StudentRepository
    ├─ Business Logic
    └─ Data Validation
```

### Library Management Module
```
LibraryController
    ↓
LibraryService
    ├─ LibraryBookRepository
    ├─ BookTransactionRepository
    └─ Business Logic
        ├─ Book Management
        ├─ Borrow/Return
        └─ Fine Calculation
```

### Attendance Module
```
AttendanceController
    ↓
AttendanceService
    ├─ AttendanceRepository
    ├─ StudentRepository
    └─ Calculation Logic
        ├─ Percentage Calculation
        ├─ Batch Processing
        └─ Reporting
```

### Grade Management Module
```
GradeController
    ↓
GradeService
    ├─ GradeRepository
    ├─ StudentRepository
    └─ Calculation Logic
        ├─ Grade Calculation
        ├─ Percentage Calculation
        └─ Average Calculation
```

### Fee Management Module
```
FeeController
    ↓
FeeService
    ├─ FeeRepository
    ├─ StudentRepository
    └─ Business Logic
        ├─ Payment Processing
        ├─ Fine Calculation
        └─ Status Management
```

## 📊 Database Schema

```
┌─────────────────┐
│     Users       │
├─────────────────┤
│ id (PK)         │
│ username        │
│ email           │
│ password        │
│ firstName       │
│ lastName        │
│ phoneNumber     │
│ role (ENUM)     │
│ enabled         │
│ createdAt       │
│ updatedAt       │
│ lastLogin       │
└────────┬────────┘
         │
         ├──────────────────┬──────────────────┬──────────────┐
         ▼                  ▼                  ▼              ▼
    ┌────────────┐  ┌────────────┐  ┌──────────────┐  ┌────────────┐
    │   Staff    │  │  Students  │  │BookTransaction│  │Permissions │
    ├────────────┤  ├────────────┤  ├──────────────┤  ├────────────┤
    │id (PK)     │  │id (PK)     │  │id (PK)       │  │id (PK)     │
    │employeeId  │  │rollNumber  │  │user_id (FK)  │  │user_id (FK)│
    │user_id(FK) │  │admission#  │  │book_id (FK)  │  │permission  │
    │position    │  │user_id(FK) │  │transactionType
    │department  │  │dateOfBirth │  │borrowDate    │  │grantedAt   │
    │salary      │  │className   │  │dueDate       │  │grantedBy   │
    │status      │  │status      │  │returnDate    │  └────────────┘
    │dateOfJoin  │  │...details..│  │fineAmount    │
    │...details..│  └────────────┘  │finePaid      │
    └────────────┘                  └──────────────┘

Other Related Tables:
┌───────────┐  ┌─────────────┐  ┌──────────┐  ┌──────────┐
│  Classes  │  │ Attendance  │  │ Grades   │  │  Fees    │
├───────────┤  ├─────────────┤  ├──────────┤  ├──────────┤
│id (PK)    │  │id (PK)      │  │id (PK)   │  │id (PK)   │
│className  │  │student_id FK│  │student_id│  │student_id│
│section    │  │attendDate   │  │subject   │  │academicYr│
│teacher_id │  │status       │  │marksObtai│  │feeType   │
│academicYr │  │markedBy(FK) │  │percentage│  │amount    │
│...details.│  │...details...│  │grade     │  │status    │
└───────────┘  └─────────────┘  │...det... │  │...details│
                                 └──────────┘  └──────────┘

┌──────────────┐
│LibraryBooks  │
├──────────────┤
│id (PK)       │
│isbn          │
│title         │
│author        │
│publisher     │
│category      │
│totalCopies   │
│availableCopies
│status        │
│...details... │
└──────────────┘
```

## 🔐 Security Architecture

```
┌─────────────────────────────────────────────────┐
│         Spring Security Filter Chain            │
├─────────────────────────────────────────────────┤
│                                                 │
│ 1. CORS Filter                                  │
│    └─ Allow cross-origin requests               │
│                                                 │
│ 2. JWT Authentication Filter                    │
│    ├─ Extract JWT from Authorization header     │
│    ├─ Validate JWT signature                    │
│    ├─ Check token expiration                    │
│    ├─ Load user details                         │
│    └─ Set authentication context                │
│                                                 │
│ 3. Authorization Filter                         │
│    ├─ Check user roles                          │
│    ├─ Verify permissions                        │
│    └─ Allow/Deny access                         │
│                                                 │
└─────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────┐
│     Role-Based Access Control (RBAC)            │
├─────────────────────────────────────────────────┤
│                                                 │
│ Roles → Permissions Mapping                     │
│                                                 │
│ ADMIN         → All Permissions                 │
│ PRINCIPAL     → School Admin Permissions        │
│ TEACHER       → Class Management Permissions    │
│ STUDENT       → Student Portal Permissions      │
│ LIBRARIAN     → Library Management Permissions  │
│ ACCOUNTANT    → Financial Permissions           │
│ PARENT        → Parent Portal Permissions       │
│                                                 │
└─────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────┐
│    Method-Level Security (@PreAuthorize)        │
├─────────────────────────────────────────────────┤
│                                                 │
│ @PreAuthorize("hasRole('ADMIN')")               │
│ @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL')") │
│ @PreAuthorize("hasPermission(...)")             │
│                                                 │
└─────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────┐
│      Password Encryption (BCrypt)               │
├─────────────────────────────────────────────────┤
│                                                 │
│ Plain Password → BCrypt Hashing → Database      │
│                                                 │
└─────────────────────────────────────────────────┘
```

## 🔄 Data Flow Examples

### Example 1: Student Login Flow
```
1. User submits login form
   ├─ Username: "student1"
   └─ Password: "password123"
   
2. AuthController.login() receives request
   
3. AuthenticationService.login()
   ├─ Calls AuthenticationManager
   ├─ Validates credentials
   └─ Throws BadCredentialsException if invalid
   
4. Generate JWT Token
   ├─ Extract user details
   ├─ Create JWT payload
   └─ Sign with secret key
   
5. Return AuthResponse
   ├─ Access Token
   ├─ Refresh Token
   └─ User details
   
6. Client stores token
   └─ Include in future requests
```

### Example 2: Mark Attendance Flow
```
1. Teacher submits attendance form
   ├─ Class: "10-A"
   ├─ Date: "2023-11-16"
   └─ Present students: [1, 2, 3, 5, 7]
   
2. AttendanceController receives request
   ├─ Validates authentication (JWT)
   └─ Checks authorization (Teacher role)
   
3. AttendanceService.markAttendanceForClass()
   ├─ Get all students in class
   ├─ For each student:
   │  ├─ Check if in present list
   │  ├─ Create Attendance record
   │  │  ├─ Status: PRESENT or ABSENT
   │  │  ├─ Date: given date
   │  │  └─ MarkedBy: current user
   │  └─ Save to database
   └─ Return success response
   
4. Attendance records created
   └─ Available for reports and queries
```

### Example 3: Process Fee Payment Flow
```
1. Student/Accountant submits payment
   ├─ Fee ID: 123
   ├─ Amount: 500
   └─ Payment Method: "ONLINE"
   
2. FeeController receives request
   ├─ Validates authentication
   └─ Checks authorization
   
3. FeeService.processPayment()
   ├─ Get Fee record
   ├─ Update paid amount
   ├─ Calculate remaining
   ├─ Update status
   │  ├─ If fully paid: PAID
   │  ├─ If partially: PARTIAL_PAID
   │  └─ If overdue: OVERDUE
   ├─ Store payment details
   └─ Return updated Fee
   
4. Transaction recorded
   └─ Available for financial reports
```

## 📈 Performance Considerations

```
Database Optimization:
├─ Lazy loading for relationships
├─ Eager loading where needed
├─ Proper indexing on FK columns
└─ Query optimization

Caching Strategy:
├─ User authentication cache
├─ Reference data cache
└─ Frequently accessed queries

Connection Pooling:
├─ HikariCP connection pool
├─ Connection timeout: 30 seconds
├─ Maximum pool size: 10
└─ Idle timeout: 5 minutes

Response Optimization:
├─ DTO compression
├─ Pagination for large datasets
├─ Partial response selection
└─ Gzip compression
```

## 🚀 Deployment Architecture

```
┌─────────────────────────────────────────┐
│     Development Environment             │
├─────────────────────────────────────────┤
│ Local Database (MySQL)                  │
│ Local IDE (IntelliJ/Eclipse/VS Code)    │
│ Local Testing                           │
└─────────────────────────────────────────┘
           ↓ Git Push
┌─────────────────────────────────────────┐
│     Version Control (GitHub/GitLab)     │
├─────────────────────────────────────────┤
│ Source Code Repository                  │
│ Issue Tracking                          │
│ Code Review                             │
└─────────────────────────────────────────┘
           ↓ CI/CD Pipeline
┌─────────────────────────────────────────┐
│     Build & Testing (Jenkins/GitHub)    │
├─────────────────────────────────────────┤
│ Maven Build                             │
│ Unit Tests                              │
│ Integration Tests                       │
│ Code Quality Analysis (SonarQube)       │
└─────────────────────────────────────────┘
           ↓ Docker Build
┌─────────────────────────────────────────┐
│     Docker Registry                     │
├─────────────────────────────────────────┤
│ Docker Image Storage                    │
│ Image Versioning                        │
└─────────────────────────────────────────┘
           ↓ Deploy
┌─────────────────────────────────────────┐
│     Production Environment              │
├─────────────────────────────────────────┤
│ Docker Container                        │
│ Load Balancer (Nginx)                   │
│ Production Database (MySQL)             │
│ Monitoring & Logging (ELK Stack)        │
└─────────────────────────────────────────┘
```

## 🎯 Key Design Patterns Used

1. **MVC Pattern**: Model-View-Controller separation
2. **Repository Pattern**: Data access abstraction
3. **Service Pattern**: Business logic encapsulation
4. **DTO Pattern**: Data transfer objects
5. **Singleton Pattern**: Spring beans
6. **Factory Pattern**: Spring bean creation
7. **Decorator Pattern**: Spring AOP
8. **Strategy Pattern**: Different authentication strategies

## 🔌 Integration Points

```
External Integrations:
├─ Email Notification Service (future)
├─ SMS Gateway (future)
├─ Payment Gateway (future)
├─ File Storage (future)
└─ Reporting Engine (future)

Frontend Integration:
├─ React/Angular/Vue.js
├─ REST API consumption
├─ JWT token handling
└─ Error handling

Third-party Services:
├─ Google Drive (file storage)
├─ SendGrid (email)
├─ Twilio (SMS)
├─ Stripe (payments)
└─ AWS S3 (storage)
```

---

**Architecture Designed**: November 16, 2025
**Status**: ✅ Complete and Production-Ready

