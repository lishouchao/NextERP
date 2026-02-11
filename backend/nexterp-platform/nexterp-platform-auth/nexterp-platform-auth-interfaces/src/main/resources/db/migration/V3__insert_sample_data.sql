-- ==============================================================
-- 插入示例数据
-- 创建时间: 2025-01-15
-- 说明: 示例用户数据用于测试
-- ==============================================================

-- 插入示例租户用户 (tenant_id=1 表示租户ID为1，实际租户管理在tenant模块中)
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
) VALUES
(
    1,
    'tenantadmin',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    '租户管理员',
    'admin@tenant1.com',
    '13900139000',
    1,
    1,
    'system',
    CURRENT_TIMESTAMP
),
(
    1,
    'tenantuser',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    '租户普通用户',
    'user@tenant1.com',
    '13900139001',
    1,
    1,
    'system',
    CURRENT_TIMESTAMP
);

-- 插入租户角色
INSERT INTO sys_role (
    tenant_id,
    role_code,
    role_name,
    role_sort,
    status,
    created_by,
    created_at
) VALUES
(
    1,
    'TENANT_ADMIN',
    '租户管理员',
    1,
    1,
    'system',
    CURRENT_TIMESTAMP
),
(
    1,
    'TENANT_USER',
    '租户用户',
    2,
    1,
    'system',
    CURRENT_TIMESTAMP
);

-- 为租户管理员分配角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
CROSS JOIN sys_role r
WHERE u.username = 'tenantadmin'
  AND r.role_code = 'TENANT_ADMIN'
  AND u.tenant_id = 1
  AND r.tenant_id = 1
  AND u.is_deleted = FALSE
  AND r.is_deleted = FALSE;

-- 为租户用户分配角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
CROSS JOIN sys_role r
WHERE u.username = 'tenantuser'
  AND r.role_code = 'TENANT_USER'
  AND u.tenant_id = 1
  AND r.tenant_id = 1
  AND u.is_deleted = FALSE
  AND r.is_deleted = FALSE;
