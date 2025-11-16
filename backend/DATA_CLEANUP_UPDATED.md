# ✅ TEST_DATA.sql Updated - Delete Before Insert

## 🔄 What Changed

I've updated your `TEST_DATA.sql` file to **automatically delete all existing data** before inserting new test data.

### Added Cleanup Section

The file now starts with this cleanup code:

```sql
-- Disable foreign key constraints temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Delete data from all tables
DELETE FROM user_permissions;
DELETE FROM book_transactions;
DELETE FROM fees;
DELETE FROM grades;
DELETE FROM attendance;
DELETE FROM library_books;
DELETE FROM classes;
DELETE FROM students;
DELETE FROM staff;
DELETE FROM users;

-- Reset auto-increment counters
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE staff AUTO_INCREMENT = 1;
... (all tables)

-- Re-enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;
```

---

## ✅ What This Does

1. ✅ **Disables foreign key checks** - Allows deletion without constraint errors
2. ✅ **Deletes from all tables** - Removes old data in correct order
3. ✅ **Resets ID counters** - Auto-increment starts from 1 again
4. ✅ **Re-enables foreign keys** - Restores data integrity
5. ✅ **Inserts fresh test data** - Adds all new test records

---

## 🚀 How to Use

Simply run the updated file:

```bash
mysql -u root -p school_management < TEST_DATA.sql
```

Or in MySQL directly:

```sql
USE school_management;
SOURCE D:\learn\thcsthptTS\backend\TEST_DATA.sql;
```

---

## ✨ Benefits

- ✅ No duplicate data issues
- ✅ Clean state for testing
- ✅ Auto-increment IDs reset properly
- ✅ All old data removed
- ✅ Fresh test data inserted

---

## 📊 Before & After

**Before**: Old data + new data (mix of old and new)  
**After**: Only new test data (clean slate)

---

## 🧪 Verify It Works

After import:

```sql
SELECT COUNT(*) FROM users;        -- Should be 13
SELECT COUNT(*) FROM students;     -- Should be 6
SELECT COUNT(*) FROM staff;        -- Should be 6
```

---

**File Updated**: ✅ YES  
**Status**: Ready to use  
**Location**: `D:\learn\thcsthptTS\backend\TEST_DATA.sql`

