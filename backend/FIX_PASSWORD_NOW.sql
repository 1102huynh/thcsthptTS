-- FIX ADMIN PASSWORD - Run this NOW in your database console
-- This sets the CORRECT BCrypt hash for password "Test@123"

UPDATE users
SET password = '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm',
    enabled = 1
WHERE username = 'admin';

-- Verify the update
SELECT
    username,
    password,
    enabled,
    role
FROM users
WHERE username = 'admin';

-- Expected result:
-- username: admin
-- password: $2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm
-- enabled: 1
-- role: ADMIN

