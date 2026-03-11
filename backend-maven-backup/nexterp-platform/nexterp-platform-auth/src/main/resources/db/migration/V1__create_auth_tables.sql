-- ==============================================================
-- 认证授权模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: 用户、角色、权限、菜单相关表结构
-- ==============================================================

-- 1. 创建用户表
-- ==============================================================
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    gender INT,
    avatar_url VARCHAR(500),
    status INT NOT NULL DEFAULT 1,
    user_type INT NOT NULL DEFAULT 1,
    dept_id BIGINT,
    remark VARCHAR(500),
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(50),
    pwd_update_time TIMESTAMP,
    expire_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.password IS '密码(BCrypt加密)';
COMMENT ON COLUMN sys_user.real_name IS '真实姓名';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.phone IS '手机号';
COMMENT ON COLUMN sys_user.gender IS '性别 (0-女 1-男 2-未知)';
COMMENT ON COLUMN sys_user.avatar_url IS '头像URL';
COMMENT ON COLUMN sys_user.status IS '用户状态 (0-禁用 1-正常)';
COMMENT ON COLUMN sys_user.user_type IS '用户类型 (0-系统用户 1-租户用户)';
COMMENT ON COLUMN sys_user.dept_id IS '部门ID';
COMMENT ON COLUMN sys_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN sys_user.last_login_ip IS '最后登录IP';
COMMENT ON COLUMN sys_user.pwd_update_time IS '密码最后修改时间';
COMMENT ON COLUMN sys_user.expire_time IS '账号过期时间';

-- 创建索引
CREATE UNIQUE INDEX uk_username ON sys_user(username, tenantId) WHERE is_deleted = FALSE;
CREATE INDEX idx_email ON sys_user(email, tenantId) WHERE is_deleted = FALSE;
CREATE INDEX idx_status ON sys_user(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_tenant_id ON sys_user(tenantId) WHERE is_deleted = FALSE;
CREATE INDEX idx_dept_id ON sys_user(deptId) WHERE is_deleted = FALSE;

-- 2. 创建角色表
-- ==============================================================
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    role_sort INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_role.role_code IS '角色编码';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_sort IS '排序';
COMMENT ON COLUMN sys_role.status IS '状态 (0-禁用 1-正常)';

-- 创建索引
CREATE UNIQUE INDEX uk_role_code ON sys_role(role_code, tenantId) WHERE is_deleted = FALSE;
CREATE INDEX idx_status ON sys_role(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_tenant_id ON sys_role(tenantId) WHERE is_deleted = FALSE;

-- 3. 创建权限表
-- ==============================================================
CREATE TABLE sys_permission (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    permission_code VARCHAR(100) NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    permission_type VARCHAR(20) NOT NULL DEFAULT 'button',
    parent_id BIGINT,
    path VARCHAR(255),
    component VARCHAR(255),
    icon VARCHAR(100),
    sort_order INT NOT NULL DEFAULT 0,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_permission IS '权限表';
COMMENT ON COLUMN sys_permission.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_permission.permission_code IS '权限编码 (格式: module:action:resource)';
COMMENT ON COLUMN sys_permission.permission_type IS '权限类型 (menu-菜单权限 button-按钮权限 data-数据权限)';
COMMENT ON COLUMN sys_permission.parent_id IS '父权限ID';
COMMENT ON COLUMN sys_permission.path IS '路由路径';
COMMENT ON COLUMN sys_permission.component IS '组件路径';
COMMENT ON COLUMN sys_permission.icon IS '图标';
COMMENT ON COLUMN sys_permission.visible IS '是否可见';
COMMENT ON COLUMN sys_permission.status IS '状态 (0-禁用 1-正常)';

-- 创建索引
CREATE UNIQUE INDEX uk_permission_code ON sys_permission(permission_code, tenantId) WHERE is_deleted = FALSE;
CREATE INDEX idx_status ON sys_permission(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_tenant_id ON sys_permission(tenantId) WHERE is_deleted = FALSE;
CREATE INDEX idx_parent_id ON sys_permission(parentId, tenantId) WHERE is_deleted = FALSE;

-- 4. 创建菜单表
-- ==============================================================
CREATE TABLE sys_menu (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    parent_id BIGINT,
    menu_name VARCHAR(100) NOT NULL,
    menu_type CHAR(1) NOT NULL,
    order_num INT NOT NULL DEFAULT 0,
    path VARCHAR(255),
    component VARCHAR(255),
    query TEXT,
    is_frame BOOLEAN DEFAULT FALSE,
    is_cache BOOLEAN DEFAULT FALSE,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    icon VARCHAR(100),
    permission VARCHAR(100),
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN sys_menu.menu_type IS '菜单类型 (M-目录 C-菜单 F-按钮)';
COMMENT ON COLUMN sys_menu.order_num IS '显示顺序';
COMMENT ON COLUMN sys_menu.path IS '路由地址';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.query IS '路由参数';
COMMENT ON COLUMN sys_menu.is_frame IS '是否为外链';
COMMENT ON COLUMN sys_menu.is_cache IS '是否缓存';
COMMENT ON COLUMN sys_menu.visible IS '是否可见';
COMMENT ON COLUMN sys_menu.icon IS '图标';
COMMENT ON COLUMN sys_menu.permission IS '权限标识';

-- 创建索引
CREATE INDEX idx_parent_id ON sys_menu(parentId, tenantId) WHERE is_deleted = FALSE;
CREATE INDEX idx_status ON sys_menu(visible, tenantId) WHERE is_deleted = FALSE;
CREATE INDEX idx_tenant_id ON sys_menu(tenantId) WHERE is_deleted = FALSE;

-- 5. 创建用户角色关联表
-- ==============================================================
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- 创建索引
CREATE INDEX idx_user_id ON sys_user_role(user_id);
CREATE INDEX idx_role_id ON sys_user_role(role_id);

-- 6. 创建角色权限关联表
-- ==============================================================
CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_role_permission IS '角色权限关联表';

-- 创建索引
CREATE INDEX idx_role_id ON sys_role_permission(role_id);
CREATE INDEX idx_permission_id ON sys_role_permission(permission_id);

-- 7. 插入系统初始化数据
-- ==============================================================
-- 插入系统管理员角色
INSERT INTO sys_role (tenant_id, role_code, role_name, role_sort, status, created_by)
VALUES (0, 'ROLE_ADMIN', '系统管理员', 1, 1, 'system');

-- 插入系统权限 (示例权限)
INSERT INTO sys_permission (tenant_id, permission_code, permission_name, permission_type, status, created_by)
VALUES
    (0, 'system:user:view', '查看用户', 'button', 1, 'system'),
    (0, 'system:user:add', '添加用户', 'button', 1, 'system'),
    (0, 'system:user:edit', '编辑用户', 'button', 1, 'system'),
    (0, 'system:user:delete', '删除用户', 'button', 1, 'system'),
    (0, 'system:role:view', '查看角色', 'button', 1, 'system'),
    (0, 'system:role:add', '添加角色', 'button', 1, 'system'),
    (0, 'system:role:edit', '编辑角色', 'button', 1, 'system'),
    (0, 'system:role:delete', '删除角色', 'button', 1, 'system');

-- 为系统管理员角色分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE tenant_id = 0;

-- 插入系统菜单 (示例菜单结构)
INSERT INTO sys_menu (tenant_id, parent_id, menu_name, menu_type, order_num, path, icon, permission, status, created_by)
VALUES
    (0, NULL, '系统管理', 'M', 1, '/system', 'setting', NULL, 1, 'system'),
    (0, 1, '用户管理', 'C', 1, '/system/user', 'user', 'system:user:view', 1, 'system'),
    (0, 1, '角色管理', 'C', 2, '/system/role', 'team', 'system:role:view', 1, 'system'),
    (0, 1, '权限管理', 'C', 3, '/system/permission', 'lock', 'system:permission:view', 1, 'system');
