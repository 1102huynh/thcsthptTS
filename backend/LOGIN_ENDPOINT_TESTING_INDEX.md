# 📚 LOGIN ENDPOINT TESTING - Complete Documentation Index

## 🎯 Quick Navigation

### For Impatient Users (30 seconds)
👉 **Read**: `QUICK_LOGIN_TEST_REFERENCE.md`

### For Visual Learners  
👉 **Open**: Swagger UI at http://localhost:8080/api/swagger-ui.html

### For Command Line Users
👉 **Use**: cURL commands from `LOGIN_TEST_EXAMPLES.md`

### For Detailed Information
👉 **Read**: `COMPLETE_LOGIN_TESTING_GUIDE.md`

### For Ready-to-Use Code
👉 **Copy from**: `LOGIN_TEST_EXAMPLES.md`

---

## 📁 All Documentation Files

### 1. COMPLETE_LOGIN_TESTING_GUIDE.md
**Content**: Comprehensive guide with 6 testing methods
- Swagger UI (step-by-step)
- cURL commands
- Postman setup
- VS Code REST Client
- JavaScript/Fetch API
- Java test code
- All credentials
- Request/response formats
- Error handling

**Best for**: Complete understanding

---

### 2. QUICK_LOGIN_TEST_REFERENCE.md
**Content**: Quick reference card
- Fastest way (30 sec)
- Swagger UI quick steps
- cURL one-liner
- Postman quick setup
- Test credentials
- Expected responses
- Error responses

**Best for**: Quick lookup

---

### 3. LOGIN_TEST_EXAMPLES.md
**Content**: Ready-to-use code examples
- cURL commands (copy-paste)
- Postman collection JSON
- REST Client file (.rest)
- JavaScript examples
- Python examples
- PowerShell examples
- All with credentials

**Best for**: Copy and run

---

### 4. LOGIN_ENDPOINT_TESTING_SUMMARY.md
**Content**: Overview and reference
- Endpoint details
- 6 testing methods
- Request/response format
- Success/error responses
- Test credentials table
- Test scenarios
- Token usage
- Checklist

**Best for**: Overall reference

---

### 5. LOGIN_TESTING_COMPLETE.md
**Content**: Visual summary (this display)
- Quick overview
- 30-second test
- All methods at a glance
- Credentials list
- Expected response
- File index

**Best for**: Quick visual overview

---

## 🚀 Getting Started

### Option 1: Visual (Swagger UI)
1. Start app: `java -jar target/school-management-system-1.0.0.jar`
2. Open: http://localhost:8080/api/swagger-ui.html
3. Find: POST /v1/auth/login
4. Click: Try it out
5. Enter credentials
6. Click: Execute

⏱️ **Time**: 30 seconds

---

### Option 2: Command Line (cURL)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test@123"}'
```

⏱️ **Time**: 10 seconds

---

### Option 3: Postman
1. Create POST request
2. URL: http://localhost:8080/api/v1/auth/login
3. Body: {"username":"admin","password":"Test@123"}
4. Send

⏱️ **Time**: 1 minute

---

### Option 4: VS Code REST Client
1. Install REST Client extension
2. Create `test.rest` file
3. Add: See `LOGIN_TEST_EXAMPLES.md`
4. Click: Send Request

⏱️ **Time**: 2 minutes

---

### Option 5: JavaScript (Browser Console)
```javascript
fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({username: 'admin', password: 'Test@123'})
}).then(r => r.json()).then(console.log);
```

⏱️ **Time**: 1 minute

---

### Option 6: Automated Tests
- See `COMPLETE_LOGIN_TESTING_GUIDE.md` for Java test code
- Run: `mvn -s settings.xml test`

⏱️ **Time**: 3-5 minutes

---

## 🔑 Test Credentials

**Password for all users**: `Test@123`

### Admin
```
Username: admin
Password: Test@123
Role: ADMIN
```

### Principal
```
Username: principal
Password: Test@123
Role: PRINCIPAL
```

### Teachers
```
Usernames: teacher1, teacher2, teacher3
Password: Test@123
Role: TEACHER
```

### Librarian
```
Username: librarian
Password: Test@123
Role: LIBRARIAN
```

### Accountant
```
Username: accountant
Password: Test@123
Role: ACCOUNTANT
```

### Students
```
Usernames: student1, student2, student3, student4, student5, student6
Password: Test@123
Role: STUDENT
```

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

## 🔗 Using the Token

After successful login, use the token for protected endpoints:

```bash
# Example: Get all staff
curl -X GET http://localhost:8080/api/v1/staff \
  -H "Authorization: Bearer {accessToken}"

# Example: Get students
curl -X GET http://localhost:8080/api/v1/students \
  -H "Authorization: Bearer {accessToken}"

# Example: Get grades
curl -X GET http://localhost:8080/api/v1/grades/student/1 \
  -H "Authorization: Bearer {accessToken}"
```

---

## ✅ Testing Checklist

- [ ] Application running on port 8080
- [ ] Database imported with test data
- [ ] Can access http://localhost:8080/api/swagger-ui.html
- [ ] Login endpoint returns 200 status
- [ ] Response contains accessToken
- [ ] Token format is valid JWT
- [ ] Can use token for protected endpoints
- [ ] Invalid credentials return 401
- [ ] Different roles return correct role

---

## 📞 Troubleshooting

| Issue | Solution |
|-------|----------|
| Connection refused | Ensure app running on port 8080 |
| Invalid credentials | Check username/password (case-sensitive) |
| Database error | Import test data first |
| 404 Not found | Check URL spelling |
| CORS error | Should not happen (configured) |
| No response | Check network connection |

---

## 📝 File Locations

All files in: `D:\learn\thcsthptTS\backend\`

```
├── COMPLETE_LOGIN_TESTING_GUIDE.md
├── QUICK_LOGIN_TEST_REFERENCE.md
├── LOGIN_TEST_EXAMPLES.md
├── LOGIN_ENDPOINT_TESTING_SUMMARY.md
├── LOGIN_TESTING_COMPLETE.md
└── LOGIN_ENDPOINT_TESTING_INDEX.md (this file)
```

---

## 🎯 Next Steps

1. ✅ Choose your testing method
2. ✅ Start the application
3. ✅ Run the first test
4. ✅ Verify you get tokens
5. ✅ Use token for protected endpoints
6. ✅ Test different user roles

---

**Created**: November 16, 2025  
**Last Updated**: November 16, 2025  
**Status**: ✅ Complete and ready to use

---

🎉 **You're all set! Choose a method and start testing!** 🎉

