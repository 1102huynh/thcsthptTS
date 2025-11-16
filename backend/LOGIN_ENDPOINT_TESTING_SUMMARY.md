# 📚 LOGIN ENDPOINT TESTING - Complete Documentation

## 📍 Endpoint Details

| Property | Value |
|----------|-------|
| **Endpoint** | POST /api/v1/auth/login |
| **Full URL** | http://localhost:8080/api/v1/auth/login |
| **Method** | POST |
| **Content-Type** | application/json |
| **Authentication** | Not required (public) |
| **Response** | JWT tokens + user info |

---

## 🚀 6 Methods to Test

### ✅ Method 1: Swagger UI (Easiest - Visual)
- Open: http://localhost:8080/api/swagger-ui.html
- Find: POST /v1/auth/login
- Click: Try it out
- Enter credentials
- View response

**Time**: 30 seconds ⚡

---

### ✅ Method 2: cURL (Command Line)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test@123"}'
```

**Time**: 10 seconds ⚡⚡

---

### ✅ Method 3: Postman (Visual API Testing)
1. Create POST request
2. URL: http://localhost:8080/api/v1/auth/login
3. Body: {"username":"admin","password":"Test@123"}
4. Send

**Time**: 1 minute

---

### ✅ Method 4: VS Code REST Client
- Create `.rest` file
- Add REST requests
- Click "Send Request"
- View response

**Time**: 2 minutes

---

### ✅ Method 5: JavaScript/Fetch API
```javascript
fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({username: 'admin', password: 'Test@123'})
}).then(r => r.json()).then(d => console.log(d));
```

**Time**: 1 minute

---

### ✅ Method 6: Automated Tests (Java)
- Write JUnit test
- Run: `mvn -s settings.xml test`
- Verify results

**Time**: 3-5 minutes

---

## 📊 Request Format

```json
{
  "username": "string",
  "password": "string"
}
```

---

## ✅ Success Response (200 OK)

```json
{
  "userId": 1,
  "username": "admin",
  "email": "admin@school.com",
  "firstName": "Admin",
  "lastName": "User",
  "role": "ADMIN",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "issuedAt": "2025-11-16T13:20:00",
  "expiresAt": "2025-11-17T13:20:00"
}
```

---

## ❌ Error Response (401 Unauthorized)

```json
{
  "status": "UNAUTHORIZED",
  "message": "Invalid username or password",
  "code": 401,
  "timestamp": "2025-11-16T13:20:00"
}
```

---

## 🔑 Test Credentials

All passwords: **Test@123**

| Username | Role | Department |
|----------|------|-----------|
| admin | ADMIN | System |
| principal | PRINCIPAL | Administration |
| teacher1 | TEACHER | English |
| teacher2 | TEACHER | Mathematics |
| teacher3 | TEACHER | Science |
| librarian | LIBRARIAN | Library |
| accountant | ACCOUNTANT | Finance |
| student1 | STUDENT | Class 10-A |
| student2 | STUDENT | Class 10-A |
| student3 | STUDENT | Class 10-A |
| student4 | STUDENT | Class 10-B |
| student5 | STUDENT | Class 10-B |
| student6 | STUDENT | Class 10-B |

---

## 🧪 Test Scenarios

### ✅ Positive Tests
- Login with valid credentials → 200 OK
- Different user roles → All return 200
- Token in response → Always present
- Timestamp format → ISO 8601

### ❌ Negative Tests
- Wrong password → 401 Unauthorized
- Non-existent user → 401 Unauthorized
- Missing username → 400 Bad Request
- Missing password → 400 Bad Request
- Empty credentials → 400 Bad Request

---

## 🔐 Using the Token

After login, use token for protected endpoints:

```bash
# Get all staff
curl -X GET http://localhost:8080/api/v1/staff \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Get students
curl -X GET http://localhost:8080/api/v1/students \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Get grades
curl -X GET http://localhost:8080/api/v1/grades/student/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 📁 Documentation Files

1. **COMPLETE_LOGIN_TESTING_GUIDE.md** - Detailed guide (6 methods)
2. **QUICK_LOGIN_TEST_REFERENCE.md** - Quick reference
3. **LOGIN_TEST_EXAMPLES.md** - Ready-to-use examples
4. **LOGIN_ENDPOINT_TESTING_SUMMARY.md** - This file

---

## ✅ Checklist

- [ ] Application running
- [ ] Database has test data
- [ ] Can access Swagger UI
- [ ] Login with admin/Test@123 works
- [ ] Response contains accessToken
- [ ] Can use token for protected endpoints
- [ ] Invalid credentials return 401
- [ ] Different roles return correct role

---

## 🎯 Quick Start

### 30-Second Test:
1. Start app: `java -jar target/school-management-system-1.0.0.jar`
2. Open: http://localhost:8080/api/swagger-ui.html
3. Find: POST /v1/auth/login
4. Click: Try it out
5. Enter: {"username":"admin","password":"Test@123"}
6. Click: Execute
7. Done! ✅

---

## 📞 Support

- **Issue**: Connection refused
  - **Solution**: Ensure app is running on port 8080

- **Issue**: Invalid credentials
  - **Solution**: Check username/password (case-sensitive)

- **Issue**: Database error
  - **Solution**: Import test data first

- **Issue**: CORS error
  - **Solution**: CORS is configured, should work

---

**Created**: November 16, 2025  
**Status**: ✅ Complete and ready to test

