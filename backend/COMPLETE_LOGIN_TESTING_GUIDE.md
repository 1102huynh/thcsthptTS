# 🧪 Complete Guide to Test /login Endpoint

## 📍 Endpoint Information

**Endpoint**: `POST /api/v1/auth/login`  
**Full URL**: `http://localhost:8080/api/v1/auth/login`  
**Method**: POST  
**Content-Type**: application/json  
**Authentication**: None required (public endpoint)

---

## 🚀 Method 1: Swagger UI (Easiest - Visual)

### Steps:
1. **Start your application**:
   ```bash
   java -jar target/school-management-system-1.0.0.jar
   # OR
   mvn -s settings.xml spring-boot:run
   ```

2. **Open Swagger UI**:
   ```
   http://localhost:8080/api/swagger-ui.html
   ```

3. **Find the endpoint**:
   - Look for "Authentication" or "Auth" section
   - Find "POST /v1/auth/login"
   - Click on it to expand

4. **Click "Try it out"** button

5. **Enter test credentials**:
   ```json
   {
     "username": "admin",
     "password": "Test@123"
   }
   ```

6. **Click "Execute"**

7. **View Response**:
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
     "issuedAt": "2025-11-16T...",
     "expiresAt": "2025-11-17T..."
   }
   ```

---

## 🔧 Method 2: cURL Command (Command Line)

### Test Login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Test@123"
  }'
```

### Save Response to File:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Test@123"
  }' > response.json
```

### Test with Different Users:
```bash
# Teacher login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "teacher1", "password": "Test@123"}'

# Student login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "student1", "password": "Test@123"}'

# Librarian login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "librarian", "password": "Test@123"}'
```

---

## 📮 Method 3: Postman (API Testing Tool)

### Setup:
1. **Download & Open** Postman
2. **Create New Request**:
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/auth/login`

3. **Set Headers**:
   - Key: `Content-Type`
   - Value: `application/json`

4. **Set Body** (Raw JSON):
   ```json
   {
     "username": "admin",
     "password": "Test@123"
   }
   ```

5. **Click Send**

6. **View Response** in response panel

### Save Token for Future Requests:
- Copy `accessToken` from response
- Use in future requests as:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN
  ```

---

## 💻 Method 4: VS Code REST Client

### Install Extension:
1. Open VS Code
2. Go to Extensions (Ctrl+Shift+X)
3. Search "REST Client"
4. Install by Huachao Mao

### Create `test-login.rest` File:

```rest
### Login as Admin
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Test@123"
}

### Login as Teacher
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "teacher1",
  "password": "Test@123"
}

### Login as Student
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "student1",
  "password": "Test@123"
}

### Login as Librarian
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "librarian",
  "password": "Test@123"
}

### Login Invalid Credentials
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "wrongpassword"
}
```

### Run:
- Click "Send Request" above each endpoint
- View response in side panel

---

## 🧪 Method 5: Java Test Code

### Create Test File:

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginSuccess() throws Exception {
        // Test successful login
        String authRequest = "{\"username\":\"admin\",\"password\":\"Test@123\"}";
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        // Test invalid credentials
        String authRequest = "{\"username\":\"admin\",\"password\":\"wrongpassword\"}";
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequest))
                .andExpect(status().isUnauthorized());
    }
}
```

### Run:
```bash
mvn -s settings.xml test
```

---

## 📝 Method 6: JavaScript/Fetch API

### Browser Console:

```javascript
// Test login
fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'Test@123'
  })
})
.then(response => response.json())
.then(data => console.log('Login Response:', data))
.catch(error => console.error('Error:', error));
```

### Save and Use Token:

```javascript
let token = null;

// Login and save token
fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'admin', password: 'Test@123' })
})
.then(r => r.json())
.then(data => {
  token = data.accessToken;
  console.log('Token saved:', token);
  
  // Use token for protected endpoint
  return fetch('http://localhost:8080/api/v1/staff', {
    headers: { 'Authorization': 'Bearer ' + token }
  });
})
.then(r => r.json())
.then(data => console.log('Staff data:', data));
```

---

## 🔑 All Test Credentials

**All passwords**: `Test@123`

| Username | Role | Purpose |
|----------|------|---------|
| admin | ADMIN | Full access |
| principal | PRINCIPAL | School management |
| teacher1 | TEACHER | Class management |
| teacher2 | TEACHER | Class management |
| teacher3 | TEACHER | Class management |
| librarian | LIBRARIAN | Library management |
| accountant | ACCOUNTANT | Finance management |
| student1 | STUDENT | Student access |
| student2 | STUDENT | Student access |
| student3 | STUDENT | Student access |
| student4 | STUDENT | Student access |
| student5 | STUDENT | Student access |
| student6 | STUDENT | Student access |

---

## ✅ Expected Response Codes

| Code | Status | Meaning |
|------|--------|---------|
| **200** | OK | Login successful |
| **400** | Bad Request | Missing/invalid request body |
| **401** | Unauthorized | Invalid credentials |
| **500** | Server Error | Server issue |

---

## 📋 Request/Response Format

### Request Body (AuthRequest):
```json
{
  "username": "string",
  "password": "string"
}
```

### Success Response (200 OK):
```json
{
  "userId": "number",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "ADMIN|PRINCIPAL|TEACHER|STUDENT|LIBRARIAN|ACCOUNTANT|PARENT",
  "accessToken": "JWT token string",
  "refreshToken": "JWT token string",
  "tokenType": "Bearer",
  "issuedAt": "2025-11-16T13:20:00",
  "expiresAt": "2025-11-17T13:20:00"
}
```

### Error Response (401 Unauthorized):
```json
{
  "status": "UNAUTHORIZED",
  "message": "Invalid username or password",
  "code": 401,
  "timestamp": "2025-11-16T13:20:00"
}
```

---

## 🔐 Using the Token

After login, use the `accessToken` to access protected endpoints:

```bash
# Example: Get all staff
curl -X GET http://localhost:8080/api/v1/staff \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Example: Get students
curl -X GET http://localhost:8080/api/v1/students \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Example: Get library books
curl -X GET http://localhost:8080/api/v1/library/books \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 🎯 Test Checklist

- [ ] Application running on http://localhost:8080
- [ ] Database `school_management` exists with test data
- [ ] Can access Swagger UI at http://localhost:8080/api/swagger-ui.html
- [ ] Login endpoint returns 200 status
- [ ] Response contains `accessToken`
- [ ] Can use token for protected endpoints
- [ ] Invalid credentials return 401 status
- [ ] Different user roles return correct role in response

---

**Date**: November 16, 2025  
**Status**: ✅ Complete guide ready

