-- ==============================================================
-- 多租户模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: 租户表结构
-- ==============================================================

-- 1. 创建租户表
-- ==============================================================
CREATE TABLE sys_tenant (
    id BIGSERIAL PRIMARY KEY,
    tenant_code VARCHAR(50) NOT NULL UNIQUE,
    tenant_name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    address VARCHAR(255),
    status INT NOT NULL DEFAULT 1,
    expire_time TIMESTAMP,
    max_users INT NOT NULL DEFAULT 100,
    max_storage BIGINT NOT NULL DEFAULT 10240,
    remark VARCHAR(500),
    config TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_tenant IS '租户表';
COMMENT ON COLUMN sys_tenant.tenant_code IS '租户编码';
COMMENT ON COLUMN sys_tenant.tenant_name IS '租户名称';
COMMENT ON COLUMN sys_tenant.contact_name IS '联系人';
COMMENT ON COLUMN sys_tenant.contact_phone IS '联系电话';
COMMENT ON COLUMN sys_tenant.contact_email IS '联系邮箱';
COMMENT ON COLUMN sys_tenant.address IS '租户地址';
COMMENT ON COLUMN sys_tenant.status IS '状态 (0-禁用 1-正常)';
COMMENT ON COLUMN sys_tenant.expire_time IS '过期时间';
COMMENT ON COLUMN sys_tenant.max_users IS '最大用户数';
COMMENT ON COLUMN sys_tenant.max_storage IS '最大存储空间(MB)';
COMMENT ON COLUMN sys_tenant.remark IS '备注';
COMMENT ON COLUMN sys_tenant.config IS '租户配置 (JSON格式)';

-- 创建索引
CREATE UNIQUE INDEX uk_tenant_code ON sys_tenant(tenant_code) WHERE is_deleted = FALSE;
CREATE INDEX idx_status ON sys_tenant(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_expire_time ON sys_tenant(expire_time) WHERE is_deleted = FALSE;

-- 2. 插入默认租户
-- ==============================================================
INSERT INTO sys_tenant (
    tenant_code,
    tenant_name,
    status,
    max_users,
    max_storage,
    created_by,
    created_at
) VALUES (
    'DEFAULT',
    '默认租户',
    1,
    1000,
    102400,
    'system',
    CURRENT_TIMESTAMP
);
