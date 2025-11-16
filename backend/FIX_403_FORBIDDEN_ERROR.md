# ✅ FIX: 403 Forbidden Error on Swagger UI

## 🎯 THE ISSUE

```
curl http://localhost:8080/api/swagger-ui.html
Error: (403) Forbidden
```

The backend is running but blocking access to Swagger UI.

---

## ✅ ROOT CAUSE

The backend's Spring Security configuration is blocking unauthenticated access to `/api/swagger-ui.html`

**Why?** Because all `/api/**` endpoints require authentication by default.

---

## ✅ SOLUTION: Fix Security Configuration

The Swagger UI endpoint needs to be in the **public/permitAll** list.

### Step 1: Locate Security Configuration
**File**: `D:\learn\thcsthptTS\backend\src\main\java\com\schoolmanagement\config\SecurityConfig.java`

### Step 2: Update Security Config
Add Swagger endpoints to the public endpoints list.

Replace this section:
```java
.requestMatchers("/api/v1/auth/**").permitAll()
```

With this:
```java
.requestMatchers(
    "/api/v1/auth/**",
    "/api/swagger-ui.html",
    "/api/swagger-ui/**",
    "/api/v3/api-docs/**",
    "/api/v3/api-docs.yaml"
).permitAll()
```

### Step 3: Rebuild Backend
```bash
cd D:\learn\thcsthptTS\backend
mvn -s settings.xml clean install
```

### Step 4: Restart Backend
```bash
java -jar target/school-management-system-1.0.0.jar
```

### Step 5: Test
```bash
# Try accessing Swagger UI (without curl in PowerShell, use browser)
http://localhost:8080/api/swagger-ui.html
```

---

## 🌐 ALTERNATIVE: Use Browser Instead of cURL

### In PowerShell, cURL is an alias for Invoke-WebRequest
To use real cURL, either:

**Option 1: Use WSL (Windows Subsystem for Linux)**
```bash
wsl curl http://localhost:8080/api/swagger-ui.html
```

**Option 2: Use Invoke-WebRequest (PowerShell curl)**
```powershell
Invoke-WebRequest http://localhost:8080/api/swagger-ui.html
```

**Option 3: Use Git Bash**
```bash
# Open Git Bash and run:
curl http://localhost:8080/api/swagger-ui.html
```

**Option 4: Just Use Browser**
```
Open: http://localhost:8080/api/swagger-ui.html
```

---

## 🔧 QUICKEST FIX: Use Browser

Just open in your browser:
```
http://localhost:8080/api/swagger-ui.html
```

If still getting 403, then you need to update SecurityConfig.

---

## 📝 FULL SecurityConfig FIX

**File**: `src/main/java/com/schoolmanagement/config/SecurityConfig.java`

Find and update the security configuration:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeHttpRequests(auth -> auth
            // Public endpoints
            .requestMatchers(
                "/api/v1/auth/**",
                "/api/swagger-ui.html",
                "/api/swagger-ui/**",
                "/api/v3/api-docs/**",
                "/api/v3/api-docs.yaml",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml"
            ).permitAll()
            // All other endpoints require authentication
            .anyRequest().authenticated()
        )
        .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

---

## ✅ AFTER FIX

You should be able to:
1. ✅ Access `http://localhost:8080/api/swagger-ui.html` without error
2. ✅ See Swagger UI with all API endpoints
3. ✅ Login with admin/Test@123
4. ✅ Test endpoints

---

## 📞 IF STILL NOT WORKING

### Check Backend Started Correctly
```bash
# Look for these lines in backend console:
# - "Tomcat started on port(s): 8080"
# - "Started SchoolManagementApplication"
```

### Check No Security Errors
```bash
# Look for errors like:
# - SecurityException
# - AccessDeniedException
# - AuthenticationException
```

### Verify Port 8080 Open
```bash
netstat -ano | findstr :8080
```

Should show Java process running.

---

## 🎯 SUMMARY

**The 403 error means:**
- ✅ Backend is running
- ✅ Port 8080 is accessible
- ❌ Security is blocking unauthenticated access to Swagger UI

**Solutions:**
1. Update SecurityConfig to allow public access to Swagger
2. Or just use Browser instead of cURL
3. Or use Git Bash instead of PowerShell for curl

---

**Try Option 1 (Update SecurityConfig) for permanent fix!**

