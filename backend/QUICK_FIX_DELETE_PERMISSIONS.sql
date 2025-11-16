-- QUICK FIX: Delete invalid permissions for admin user
-- This allows login to work immediately

DELETE FROM user_permissions
WHERE user_id = (SELECT id FROM users WHERE username = 'student1');

-- Verify deletion
SELECT COUNT(*) as permission_count
FROM user_permissions up
JOIN users u ON up.user_id = u.id
WHERE u.username = 'student1';

-- Should show 0

