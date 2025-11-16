-- FIX: Delete invalid permissions for student1 user and insert valid ones

-- First, check what permissions exist for student1
SELECT up.id, up.user_id, up.permission, u.username
FROM user_permissions up
JOIN users u ON up.user_id = u.id
WHERE u.username = 'student1';

-- Delete ALL permissions for student1 user (they have invalid data)
DELETE FROM user_permissions
WHERE user_id = (SELECT id FROM users WHERE username = 'student1');

-- Insert valid permissions for student1 user
-- student1 should have ALL permissions
INSERT INTO user_permissions (user_id, permission, granted_at)
SELECT u.id, 'CREATE_STAFF', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'READ_STAFF', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'UPDATE_STAFF', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'DELETE_STAFF', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'CREATE_STUDENT', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'READ_STUDENT', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'UPDATE_STUDENT', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'DELETE_STUDENT', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'CREATE_BOOK', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'READ_BOOK', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'UPDATE_BOOK', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'DELETE_BOOK', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'CREATE_CLASS', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'READ_CLASS', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'MANAGE_USERS', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'MANAGE_ROLES', NOW() FROM users u WHERE u.username = 'student1' UNION ALL
SELECT u.id, 'SYSTEM_CONFIG', NOW() FROM users u WHERE u.username = 'student1';

-- Verify the fix
SELECT up.id, up.user_id, up.permission, u.username
FROM user_permissions up
JOIN users u ON up.user_id = u.id
WHERE u.username = 'student1';


