-- Check admin user details
SELECT id, username, email, password, role, enabled
FROM users
WHERE username = 'admin';

-- Check all users
SELECT id, username, email, role, enabled
FROM users;

-- Verify password hash format
-- The password hash should be BCrypt format starting with $2a$
-- For "Test@123" it should be:
-- $2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm

