# How to Test the /login Endpoint

## 📍 Endpoint Details

**URL**: `POST http://localhost:8080/api/v1/auth/login`  
**Method**: POST  
**Content-Type**: application/json  
**Authentication**: None required (public endpoint)

---

## 1️⃣ Using Swagger UI (Easiest)

### Steps:
1. **Start the application**:
   ```bash
   java -jar target/school-management-system-1.0.0.jar
   # OR
   mvn -s settings.xml spring-boot:run
   ```

2. **Open Swagger UI**:
   ```
   http://localhost:8080/api/swagger-ui.html
   ```

3. **Find the login endpoint**:
   - Look for "Authentication" section
   - Click on "POST /v1/auth/login"
   - Click "Try it out" button

4. **Enter credentials**:
   ```json
   {
     "username": "admin",
     "password": "password123"
   }
   ```

5. **Click "Execute"**

6. **View the response**:
   - You'll get `accessToken`, `refreshToken`, and user details

---

## 2️⃣ Using cURL Command

### First, Register a User (if needed):
```bash
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
```

### Then, Login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password@123"
  }'
```

### Example Response:
```json
{
  "userId": 1,
  "username": "testuser",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "role": "STUDENT",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "issuedAt": "2025-11-16T12:00:00",
  "expiresAt": "2025-11-17T12:00:00"
}
```

---

## 3️⃣ Using Postman

### Setup:
1. **Create a new POST request**:
   - URL: `http://localhost:8080/api/v1/auth/login`
   - Method: POST

2. **Set Headers**:
   - Key: `Content-Type`
   - Value: `application/json`

3. **Set Body** (raw JSON):
   ```json
   {
     "username": "testuser",
     "password": "Password@123"
   }
   ```

4. **Click Send**

5. **View Response** in the response panel

### Save Token for Later:
- Copy the `accessToken` from response
- Use it in Authorization header for protected endpoints

---

## 4️⃣ Using VS Code REST Client

Create a file `test-login.rest`:

```
### Register new user
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Password@123",
  "firstName": "Test",
  "lastName": "User",
  "role": "STUDENT"
}

### Login endpoint
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "Password@123"
}
```

Then click "Send Request" on each endpoint.

---

## 5️⃣ Using Java/Spring Test

Create a test file:

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLogin() throws Exception {
        AuthRequest authRequest = new AuthRequest("testuser", "Password@123");
        
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }
}
```

---

## 🔑 Expected Response Codes

| Code | Meaning |
|------|---------|
| **200** | Login successful - returns tokens |
| **401** | Invalid credentials |
| **400** | Missing/invalid request body |
| **500** | Server error |

---

## ⚠️ Common Issues

### Issue: "User not found"
**Solution**: Register a user first using `/register` endpoint

### Issue: "Invalid credentials"
**Solution**: Check username and password are correct

### Issue: Connection refused
**Solution**: Ensure application is running on port 8080

### Issue: CORS error
**Solution**: CORS is already configured for `/api/**`

---

## ✅ Test Checklist

- [ ] Application running on http://localhost:8080
- [ ] Database `school_management` exists
- [ ] User registered with valid credentials
- [ ] POST request to `/v1/auth/login`
- [ ] Request body contains `username` and `password`
- [ ] Received `accessToken` in response
- [ ] Got HTTP 200 status

---

## 🔐 Using the Token

After login, use the token to access protected endpoints:

```bash
curl -X GET http://localhost:8080/api/v1/staff \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Replace `YOUR_ACCESS_TOKEN` with the token received from login response.

---

## 📝 Request/Response Format

### Request Body (AuthRequest):
```json
{
  "username": "string",
  "password": "string"
}
```

### Response Body (AuthResponse):
```json
{
  "userId": "number",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "ADMIN|PRINCIPAL|TEACHER|STUDENT|LIBRARIAN|ACCOUNTANT|PARENT",
  "accessToken": "string (JWT)",
  "refreshToken": "string (JWT)",
  "tokenType": "Bearer",
  "issuedAt": "datetime",
  "expiresAt": "datetime"
}
```

---

**Created**: November 16, 2025  
**Tested**: ✅ All methods working

