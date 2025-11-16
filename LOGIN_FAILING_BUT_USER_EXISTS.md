# 🔍 LOGIN FAILING BUT ADMIN USER EXISTS - TROUBLESHOOTING

## 🎯 SITUATION

- ✅ Admin user EXISTS in database
- ❌ Login returns 401: "Invalid username or password"
- ✅ Backend running
- ✅ Swagger UI accessible

---

## 🔍 POSSIBLE CAUSES

### 1. Wrong Password Hash
The password in database doesn't match "Test@123"

### 2. User Disabled
The `enabled` field is set to 0 (false)

### 3. Case Sensitivity
Username is case-sensitive

### 4. Backend Not Restarted
Backend still using old data/cache

---

## ✅ SOLUTION 1: Fix Admin Password

### Run this SQL in your database console:

```sql
-- Update admin user with correct password for "Test@123"
UPDATE users 
SET password = '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm',
    enabled = 1
WHERE username = 'admin';

-- Verify the update
SELECT id, username, email, password, enabled, role 
FROM users 
WHERE username = 'admin';
```

### Expected Result:
- `password` should start with `$2a$10$slYQmyNdGzin7o...`
- `enabled` should be `1` (or `true`)
- `role` should be `ADMIN`

---

## ✅ SOLUTION 2: Restart Backend

After updating the password:

1. **Stop backend**: Press `Ctrl+C` in backend terminal

2. **Start backend**:
   ```bash
   cd D:\learn\thcsthptTS\backend
   java -jar target/school-management-system-1.0.0.jar
   ```

3. **Wait for**: "Started SchoolManagementApplication" message

---

## ✅ SOLUTION 3: Verify User Details

Run this query to check admin user:

```sql
SELECT 
    id,
    username,
    email,
    LEFT(password, 20) as password_prefix,
    enabled,
    role,
    created_at
FROM users 
WHERE username = 'admin';
```

### Expected Values:
- `username`: `admin` (lowercase)
- `email`: `admin@school.com`
- `password_prefix`: `$2a$10$slYQmyNdGzin` (BCrypt format)
- `enabled`: `1` or `true`
- `role`: `ADMIN`

---

## 🔧 SOLUTION 4: Check Backend Logs

When you try to login, check backend console logs for:

### Look for these messages:
```
- "Authentication attempt for user: admin"
- "User loaded: admin"
- "Password matches: true/false"
```

### Common Error Messages:
- `Bad credentials` → Wrong password hash
- `User not found` → Username issue
- `User account is disabled` → enabled = false

---

## ✅ SOLUTION 5: Test with SQL Query

Verify password matches by checking the hash:

```sql
-- Check if admin user has correct password hash
SELECT 
    username,
    password = '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm' as correct_password,
    enabled,
    role
FROM users 
WHERE username = 'admin';
```

If `correct_password` is `0` (false), run the UPDATE query from Solution 1.

---

## 🎯 COMPLETE FIX WORKFLOW

```
1. Run UPDATE query to fix password ✅
   ↓
2. Verify password updated ✅
   ↓
3. Restart backend ✅
   ↓
4. Try login in Swagger UI ✅
   ↓
5. Should get 200 OK! ✅
```

---

## 📋 QUICK FIX COMMANDS

### In Database Console:
```sql
UPDATE users SET password = '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', enabled = 1 WHERE username = 'admin';
```

### In Command Prompt:
```bash
# Restart backend
cd D:\learn\thcsthptTS\backend
java -jar target/school-management-system-1.0.0.jar
```

### In Swagger UI:
```json
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "Test@123"
}
```

---

## ✅ EXPECTED RESULT

After fixing:

**Request**:
```json
{
  "username": "admin",
  "password": "Test@123"
}
```

**Response (200 OK)**:
```json
{
  "userId": 1,
  "username": "admin",
  "email": "admin@school.com",
  "firstName": "Admin",
  "lastName": "User",
  "role": "ADMIN",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

---

## 📞 IF STILL FAILING

1. Check backend logs for exact error
2. Verify MySQL connection working
3. Check if other users can login
4. Verify BCrypt encoding is being used
5. Check application.yml for correct JWT secret

---

## 🔑 PASSWORD HASH REFERENCE

**Correct BCrypt hash for "Test@123"**:
```
$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm
```

This is what should be in the `password` field for admin user.

---

**ACTION**: Run the UPDATE query in Solution 1, then restart backend!

