# 🧪 Ready-to-Use Login Test Examples

## 1️⃣ cURL Commands (Copy & Paste)

### Admin Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test@123"}'
```

### Principal Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"principal","password":"Test@123"}'
```

### Teacher Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher1","password":"Test@123"}'
```

### Student Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"Test@123"}'
```

### Librarian Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"librarian","password":"Test@123"}'
```

### Accountant Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"accountant","password":"Test@123"}'
```

### Test Invalid Credentials (Should Fail)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}'
```

---

## 2️⃣ Postman Collection JSON

Save this as `login-tests.postman_collection.json`:

```json
{
  "info": {
    "name": "School Management - Login Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Admin Login",
      "request": {
        "method": "POST",
        "header": [
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"admin\",\"password\":\"Test@123\"}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "auth", "login"]
        }
      }
    },
    {
      "name": "Teacher Login",
      "request": {
        "method": "POST",
        "header": [
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"teacher1\",\"password\":\"Test@123\"}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "auth", "login"]
        }
      }
    },
    {
      "name": "Student Login",
      "request": {
        "method": "POST",
        "header": [
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"student1\",\"password\":\"Test@123\"}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "auth", "login"]
        }
      }
    },
    {
      "name": "Invalid Credentials (Should Fail)",
      "request": {
        "method": "POST",
        "header": [
          {"key": "Content-Type", "value": "application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"username\":\"admin\",\"password\":\"wrongpassword\"}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "auth", "login"]
        }
      }
    }
  ]
}
```

**To import in Postman**:
1. Click "Import" button
2. Select this JSON file
3. All tests ready to use!

---

## 3️⃣ REST Client File (VS Code)

Save as `login-tests.rest`:

```rest
### Variables
@baseUrl = http://localhost:8080
@authUrl = {{baseUrl}}/api/v1/auth

### Admin Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Test@123"
}

### Principal Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "principal",
  "password": "Test@123"
}

### Teacher1 Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "teacher1",
  "password": "Test@123"
}

### Teacher2 Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "teacher2",
  "password": "Test@123"
}

### Teacher3 Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "teacher3",
  "password": "Test@123"
}

### Librarian Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "librarian",
  "password": "Test@123"
}

### Accountant Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "accountant",
  "password": "Test@123"
}

### Student1 Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "student1",
  "password": "Test@123"
}

### Student2 Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "student2",
  "password": "Test@123"
}

### Student3 Login - Success
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "student3",
  "password": "Test@123"
}

### Invalid Credentials - Fail (Wrong Password)
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "admin",
  "password": "wrongpassword"
}

### Invalid Credentials - Fail (Non-existent User)
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "nonexistent",
  "password": "Test@123"
}

### Missing Username - Fail
POST {{authUrl}}/login
Content-Type: application/json

{
  "password": "Test@123"
}

### Missing Password - Fail
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "admin"
}

### Empty Credentials - Fail
POST {{authUrl}}/login
Content-Type: application/json

{
  "username": "",
  "password": ""
}
```

---

## 4️⃣ JavaScript Fetch Examples

### Simple Login
```javascript
fetch('http://localhost:8080/api/v1/auth/login', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    username: 'admin',
    password: 'Test@123'
  })
})
.then(r => r.json())
.then(data => console.log(data));
```

### Login and Use Token
```javascript
async function loginAndTestApi() {
  // Login
  const loginRes = await fetch('http://localhost:8080/api/v1/auth/login', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({username: 'admin', password: 'Test@123'})
  });
  
  const loginData = await loginRes.json();
  const token = loginData.accessToken;
  console.log('Token:', token);
  
  // Use token to get staff
  const staffRes = await fetch('http://localhost:8080/api/v1/staff', {
    headers: {'Authorization': `Bearer ${token}`}
  });
  
  const staff = await staffRes.json();
  console.log('Staff:', staff);
}

loginAndTestApi();
```

---

## 5️⃣ Python Examples

### Simple Login
```python
import requests
import json

url = "http://localhost:8080/api/v1/auth/login"
credentials = {
    "username": "admin",
    "password": "Test@123"
}

response = requests.post(url, json=credentials)
data = response.json()
print(json.dumps(data, indent=2))
```

### Login and Use Token
```python
import requests

# Login
login_url = "http://localhost:8080/api/v1/auth/login"
login_data = {"username": "admin", "password": "Test@123"}
login_res = requests.post(login_url, json=login_data)
token = login_res.json()['accessToken']

# Use token
staff_url = "http://localhost:8080/api/v1/staff"
headers = {"Authorization": f"Bearer {token}"}
staff_res = requests.get(staff_url, headers=headers)
print(staff_res.json())
```

---

## 6️⃣ PowerShell Examples

### Simple Login
```powershell
$url = "http://localhost:8080/api/v1/auth/login"
$body = @{
    username = "admin"
    password = "Test@123"
} | ConvertTo-Json

$response = Invoke-WebRequest -Uri $url -Method Post -ContentType "application/json" -Body $body
$response.Content | ConvertFrom-Json | ConvertTo-Json
```

### Login and Use Token
```powershell
# Login
$loginUrl = "http://localhost:8080/api/v1/auth/login"
$loginBody = @{username = "admin"; password = "Test@123"} | ConvertTo-Json
$loginRes = Invoke-WebRequest -Uri $loginUrl -Method Post -ContentType "application/json" -Body $loginBody
$token = ($loginRes.Content | ConvertFrom-Json).accessToken

# Use token
$staffUrl = "http://localhost:8080/api/v1/staff"
$headers = @{Authorization = "Bearer $token"}
$staffRes = Invoke-WebRequest -Uri $staffUrl -Headers $headers
$staffRes.Content | ConvertFrom-Json | ConvertTo-Json
```

---

## ✅ All Test Credentials

Password for ALL users: **Test@123**

```
admin
principal
teacher1, teacher2, teacher3
librarian
accountant
student1, student2, student3, student4, student5, student6
```

---

**Ready to Test!** 🎉

