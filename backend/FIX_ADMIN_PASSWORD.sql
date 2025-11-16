-- Fix admin user password
-- This updates the admin user with the correct BCrypt hash for "Test@123"

-- First, check current admin user
SELECT id, username, email, password, enabled, role
FROM users
WHERE username = 'admin';

-- Update admin user with correct password hash for "Test@123"
-- BCrypt hash: $2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm
UPDATE users
SET password = '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm',
    enabled = 1
WHERE username = 'admin';

-- Verify the update
SELECT id, username, email, password, enabled, role
FROM users
WHERE username = 'admin';

-- Result should show:
-- password: $2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm
-- enabled: 1
-- role: ADMIN

