# ⚡ QUICK FIX: 403 Forbidden Error

## 🎯 WHAT'S WRONG

Backend blocking Swagger UI with 403 Forbidden error

## ✅ WHAT'S FIXED

Updated SecurityConfig.java to allow public access to Swagger endpoints

## 🚀 DO THIS NOW

### Copy & Paste These Commands:

```bash
# Terminal 1: Stop backend (Ctrl+C if running)

# Then run this:
cd D:\learn\thcsthptTS\backend
mvn -s settings.xml clean install -DskipTests
java -jar target/school-management-system-1.0.0.jar
```

Wait for: `Started SchoolManagementApplication`

### Then Open Browser:
```
http://localhost:8080/api/swagger-ui.html
```

---

## ✅ EXPECTED

- ✅ Swagger UI loads without 403 error
- ✅ Can see all API endpoints
- ✅ Ready to test

---

**That's it! Just rebuild backend** 🎉

