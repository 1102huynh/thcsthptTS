# API Testing Guide - School Management System

## 🧪 Testing Tools & Approaches

### 1. Postman Collection

#### Import Collection
1. Open Postman
2. Click `Import`
3. Select `Link` tab
4. Paste collection URL or import JSON file

#### API Requests Template

##### Authentication
```
POST {{baseUrl}}/v1/auth/register
Content-Type: application/json

{
    "username": "student1",
    "email": "student1@school.com",
    "password": "Password@123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "STUDENT"
}

---

POST {{baseUrl}}/v1/auth/login
Content-Type: application/json

{
    "username": "student1",
    "password": "Password@123"
}

Response: Copy accessToken from response
```

##### Staff Management
```
POST {{baseUrl}}/v1/staff
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
    "employeeId": "EMP001",
    "position": "TEACHER",
    "department": "English",
    "dateOfBirth": "1990-01-15",
    "dateOfJoining": "2020-01-15",
    "qualification": "B.Ed",
    "subjectSpecialization": "English Literature",
    "salary": 50000.00,
    "status": "ACTIVE",
    "user": {
        "id": 1
    }
}

---

GET {{baseUrl}}/v1/staff
Authorization: Bearer {{accessToken}}

---

GET {{baseUrl}}/v1/staff/{{staffId}}
Authorization: Bearer {{accessToken}}
```

##### Student Management
```
POST {{baseUrl}}/v1/students
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
    "rollNumber": "STU001",
    "admissionNumber": "ADM001",
    "dateOfBirth": "2005-06-15",
    "gender": "Male",
    "bloodGroup": "O+",
    "className": "10",
    "section": "A",
    "dateOfAdmission": "2020-01-15",
    "status": "ACTIVE",
    "fatherName": "Mr. Doe",
    "fatherPhone": "9876543210",
    "motherName": "Mrs. Doe",
    "motherPhone": "9876543211",
    "user": {
        "id": 2
    }
}

---

GET {{baseUrl}}/v1/students
Authorization: Bearer {{accessToken}}

---

GET {{baseUrl}}/v1/students/class/10?section=A
Authorization: Bearer {{accessToken}}
```

##### Library Management
```
POST {{baseUrl}}/v1/library/books
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
    "isbn": "978-0-123456-78-9",
    "title": "Advanced Java Programming",
    "author": "John Smith",
    "publisher": "Tech Books",
    "publicationYear": 2020,
    "edition": "3rd",
    "category": "ACADEMIC",
    "totalCopies": 5,
    "availableCopies": 5,
    "description": "A comprehensive guide to Java programming",
    "locationRack": "A-1",
    "callNumber": "005.133",
    "status": "AVAILABLE",
    "price": 1500.00
}

---

GET {{baseUrl}}/v1/library/books
Authorization: Bearer {{accessToken}}

---

GET {{baseUrl}}/v1/library/books/search?title=Java
Authorization: Bearer {{accessToken}}

---

POST {{baseUrl}}/v1/library/books/{{bookId}}/borrow
Authorization: Bearer {{accessToken}}
```

##### Attendance Management
```
POST {{baseUrl}}/v1/attendance
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
    "student": {
        "id": 1
    },
    "attendanceDate": "2023-11-16",
    "status": "PRESENT"
}

---

GET {{baseUrl}}/v1/attendance/student/{{studentId}}
Authorization: Bearer {{accessToken}}

---

GET {{baseUrl}}/v1/attendance/student/{{studentId}}/percentage?startDate=2023-11-01&endDate=2023-11-30
Authorization: Bearer {{accessToken}}
```

##### Grade Management
```
POST {{baseUrl}}/v1/grades
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
    "student": {
        "id": 1
    },
    "subject": "Mathematics",
    "examType": "Midterm",
    "marksObtained": 85,
    "totalMarks": 100,
    "academicYear": "2023-2024"
}

---

GET {{baseUrl}}/v1/grades/student/{{studentId}}
Authorization: Bearer {{accessToken}}

---

GET {{baseUrl}}/v1/grades/student/{{studentId}}/average
Authorization: Bearer {{accessToken}}
```

##### Fee Management
```
POST {{baseUrl}}/v1/fees
Authorization: Bearer {{accessToken}}
Content-Type: application/json

{
    "student": {
        "id": 1
    },
    "academicYear": "2023-2024",
    "feeType": "Tuition",
    "amount": 50000,
    "dueDate": "2023-12-31"
}

---

GET {{baseUrl}}/v1/fees/student/{{studentId}}
Authorization: Bearer {{accessToken}}

---

POST {{baseUrl}}/v1/fees/{{feeId}}/payment?amount=10000&paymentMethod=ONLINE
Authorization: Bearer {{accessToken}}
```

### 2. cURL Testing

#### Basic Authentication
```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password@123",
    "firstName": "Test",
    "lastName": "User",
    "role": "STUDENT"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password@123"
  }'
```

#### GET Requests
```bash
# Get all staff
curl -X GET http://localhost:8080/api/v1/staff \
  -H "Authorization: Bearer <token>"

# Get all students
curl -X GET http://localhost:8080/api/v1/students \
  -H "Authorization: Bearer <token>"

# Get all books
curl -X GET http://localhost:8080/api/v1/library/books \
  -H "Authorization: Bearer <token>"
```

#### POST Requests
```bash
# Create staff member
curl -X POST http://localhost:8080/api/v1/staff \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "employeeId": "EMP001",
    "position": "TEACHER",
    "department": "English",
    "salary": 50000
  }'
```

### 3. Testing with JUnit & Mockito

#### Basic Service Test
```java
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    
    @Mock
    private StudentRepository studentRepository;
    
    @InjectMocks
    private StudentService studentService;
    
    @Test
    void testCreateStudent() {
        Student student = Student.builder()
            .rollNumber("STU001")
            .admissionNumber("ADM001")
            .build();
        
        when(studentRepository.save(student)).thenReturn(student);
        
        StudentDTO result = studentService.createStudent(student);
        
        assertNotNull(result);
        assertEquals("STU001", result.getRollNumber());
    }
}
```

#### Controller Test
```java
@WebMvcTest(StudentController.class)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class StudentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StudentService studentService;
    
    @Test
    void testGetAllStudents() throws Exception {
        mockMvc.perform(get("/api/v1/students")
            .header("Authorization", "Bearer token")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
```

---

## 📊 Test Scenarios

### Authentication Flow
1. Register new user
2. Login with credentials
3. Get access token
4. Use token for protected endpoints
5. Refresh token
6. Token expiration

### Staff Management Flow
1. Create staff member
2. Retrieve all staff
3. Get specific staff by ID
4. Update staff information
5. Delete staff member
6. Filter staff by position/department

### Student Management Flow
1. Create student
2. Assign to class
3. Retrieve student records
4. Update student info
5. Track student status
6. Generate student reports

### Library Management Flow
1. Add books to catalog
2. Search books
3. Check availability
4. Borrow book
5. Return book
6. Calculate fine for overdue

### Academic Flow
1. Mark attendance
2. Record grades
3. Calculate averages
4. Generate academic report
5. Track progress

### Fee Management Flow
1. Create fee record
2. View pending fees
3. Process payment
4. Track payment history
5. Generate fee report

---

## ✅ Test Checklist

### Authentication Tests
- [ ] Register new user
- [ ] Duplicate user registration fails
- [ ] Login with correct credentials
- [ ] Login with incorrect credentials
- [ ] Token generation
- [ ] Token validation
- [ ] Token expiration

### Authorization Tests
- [ ] User can access public endpoints
- [ ] Authenticated user can access protected endpoints
- [ ] Unauthenticated user cannot access protected endpoints
- [ ] Role-based access control works
- [ ] Permission-based access control works

### Staff Management Tests
- [ ] Create staff with valid data
- [ ] Reject duplicate employee ID
- [ ] Update staff information
- [ ] Delete staff member
- [ ] Retrieve staff by position
- [ ] Retrieve staff by department
- [ ] Retrieve active staff

### Student Management Tests
- [ ] Create student with valid data
- [ ] Reject duplicate roll number
- [ ] Retrieve students by class
- [ ] Retrieve students by class and section
- [ ] Update student information
- [ ] Track student status

### Library Tests
- [ ] Add book to catalog
- [ ] Search books by title
- [ ] Filter by category
- [ ] Borrow book
- [ ] Return book
- [ ] Calculate fine

### Attendance Tests
- [ ] Mark attendance for individual
- [ ] Mark attendance for class
- [ ] Calculate attendance percentage
- [ ] Generate attendance report

### Grade Tests
- [ ] Record grade
- [ ] Calculate percentage
- [ ] Assign letter grade
- [ ] Calculate average

### Fee Tests
- [ ] Create fee record
- [ ] Process payment
- [ ] Track payment status
- [ ] Calculate total dues

---

## 🐛 Common Test Issues

### Issue: Authentication Failed
**Solution**: Ensure token is valid and not expired. Get new token.

### Issue: Database Connection Failed
**Solution**: Use H2 in-memory database for testing. Configure in application-test.yml

### Issue: Permission Denied
**Solution**: Verify user role matches required permissions.

### Issue: Entity Not Found
**Solution**: Mock repository or use test data setup.

---

## 📈 Performance Testing

### Load Testing with JMeter
1. Create test plan
2. Add thread group (100 users)
3. Add HTTP requests
4. Run and analyze results

### Benchmark Testing
```bash
# Test endpoint response time
ab -n 1000 -c 10 http://localhost:8080/api/v1/students
```

---

**Last Updated**: November 16, 2025

