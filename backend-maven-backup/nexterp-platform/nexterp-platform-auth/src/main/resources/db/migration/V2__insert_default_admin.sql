-- ==============================================================
-- 插入默认管理员用户
-- 创建时间: 2025-01-15
-- 说明: 默认管理员账号: admin/admin123
-- ==============================================================

-- 插入默认管理员用户 (密码为 admin123 的BCrypt加密结果)
INSERT INTO sys_user (
    tenant_id,
    username,
    password,
    real_name,
    email,
    phone,
    status,
    user_type,
    created_by,
    created_at
) VALUES (
    0,
    'admin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    '系统管理员',
    'admin@nexterp.com',
    '13800138000',
    1,
    0,
    'system',
    CURRENT_TIMESTAMP
);

-- 为管理员分配角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
CROSS JOIN sys_role r
WHERE u.username = 'admin'
  AND r.role_code = 'ROLE_ADMIN'
  AND u.is_deleted = FALSE
  AND r.is_deleted = FALSE;
