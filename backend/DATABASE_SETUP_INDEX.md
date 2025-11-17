# SQL Database Setup - Quick Reference

## 📌 Current Status
✅ **Cleanup Complete** - All unnecessary SQL files removed, only standard test data retained.

---

## 🗂️ Files Available

### Main Test Data File (USE THIS)
📄 **TEST_DATA_CORRECTED.sql**
- Complete, accurate test data
- PostgreSQL compatible
- Includes: Users, Staff, Students, Classes, Books, Attendance
- Ready for production testing

### Documentation
📖 **TEST_DATA_SETUP_GUIDE.md** - How to import and use test data
📋 **SQL_FILES_CLEANUP_REPORT.md** - Detailed cleanup analysis

---

## 🚀 Quick Start

### 1. Import Test Data
```bash
# Option A: Using psql
psql -U avnadmin -h school-clinicbooking.c.aivencloud.com -p 14143 -d defaultdb -f TEST_DATA_CORRECTED.sql

# Option B: Using pgAdmin - Query Tool - Open File - Execute
```

### 2. Verify Import
```sql
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_students FROM students;
SELECT COUNT(*) as total_staff FROM staff;
```

### 3. Login to System
```
Username: admin
Password: Test@123
Role: ADMIN
```

### 4. Test API
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test@123"}'
```

---

## 📊 Data Summary

| Entity | Count | Details |
|--------|-------|---------|
| **Users** | 13 | 1 Admin + 1 Principal + 3 Teachers + 1 Librarian + 1 Accountant + 6 Students |
| **Staff** | 6 | All staff roles with complete profiles |
| **Students** | 6 | With parent/guardian & emergency contact info |
| **Classes** | 4 | Classes 10A, 10B, 9A, 9B |
| **Books** | 8 | Academic + Fiction + Reference materials |
| **Attendance** | Sample | Ready for testing |

---

## ✅ Cleanup Summary

### Deleted (8 Files)
- ❌ TEST_DATA.sql
- ❌ TEST_DATA_SAFE.sql
- ❌ CHECK_ADMIN_USER.sql
- ❌ FIX_PERMISSIONS.sql
- ❌ FIX_PASSWORD_NOW.sql
- ❌ FIX_ADMIN_PASSWORD.sql
- ❌ USE_FRESH_HASH.sql
- ❌ QUICK_FIX_DELETE_PERMISSIONS.sql

### Kept (1 File)
- ✅ TEST_DATA_CORRECTED.sql (Most accurate & standard)

---

## 🔒 Security Notes

⚠️ **Development Only**
- Credentials shown are for development/testing only
- Change all passwords before production
- Enable HTTPS in production environment
- Use strong JWT secret key (at least 256 bits)

✅ **Password Security**
- All passwords use BCrypt hashing
- Hash: `$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm`
- Hash represents: `Test@123`

---

## 📚 Additional Resources

For detailed information, see:
- `TEST_DATA_SETUP_GUIDE.md` - Complete import instructions
- `SQL_FILES_CLEANUP_REPORT.md` - Detailed file analysis
- Project documentation in main directory

---

**Last Updated:** November 17, 2025
**Status:** ✅ Ready for Testing

