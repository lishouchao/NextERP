-- ==============================================================
-- 通知模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: 通知、通知模板相关表结构
-- ==============================================================

-- 1. 创建通知表
-- ==============================================================
CREATE TABLE sys_notification (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    sender_id BIGINT,
    sender_name VARCHAR(50),
    receiver_id BIGINT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_time TIMESTAMP,
    link_url VARCHAR(500),
    extra_data TEXT,
    priority INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_notification IS '通知表';
COMMENT ON COLUMN sys_notification.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_notification.notification_type IS '通知类型 (system-系统通知 email-邮件 message-站内信 sms-短信 push-推送)';
COMMENT ON COLUMN sys_notification.title IS '通知标题';
COMMENT ON COLUMN sys_notification.content IS '通知内容';
COMMENT ON COLUMN sys_notification.sender_id IS '发送者ID';
COMMENT ON COLUMN sys_notification.sender_name IS '发送者名称';
COMMENT ON COLUMN sys_notification.receiver_id IS '接收者ID';
COMMENT ON COLUMN sys_notification.receiver_name IS '接收者名称';
COMMENT ON COLUMN sys_notification.is_read IS '是否已读';
COMMENT ON COLUMN sys_notification.read_time IS '阅读时间';
COMMENT ON COLUMN sys_notification.link_url IS '跳转链接';
COMMENT ON COLUMN sys_notification.extra_data IS '扩展数据(JSON格式)';
COMMENT ON COLUMN sys_notification.priority IS '优先级 (0-普通 1-重要 2-紧急)';
COMMENT ON COLUMN sys_notification.status IS '状态 (0-禁用 1-正常)';

-- 创建索引
CREATE INDEX idx_notification_receiver ON sys_notification(receiver_id, is_read, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_notification_type ON sys_notification(notification_type, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_notification_time ON sys_notification(created_at DESC, tenant_id) WHERE is_deleted = FALSE;

-- 2. 创建通知模板表
-- ==============================================================
CREATE TABLE sys_notification_template (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    title_template VARCHAR(200),
    content_template TEXT NOT NULL,
    variables TEXT,
    category VARCHAR(50),
    description VARCHAR(500),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    status INT NOT NULL DEFAULT 1,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_notification_template IS '通知模板表';
COMMENT ON COLUMN sys_notification_template.template_code IS '模板编码';
COMMENT ON COLUMN sys_notification_template.template_name IS '模板名称';
COMMENT ON COLUMN sys_notification_template.notification_type IS '通知类型 (system-系统通知 email-邮件 message-站内信 sms-短信)';
COMMENT ON COLUMN sys_notification_template.title_template IS '标题模板（支持变量）';
COMMENT ON COLUMN sys_notification_template.content_template IS '内容模板（支持变量，支持HTML）';
COMMENT ON COLUMN sys_notification_template.variables IS '变量定义(JSON格式)';
COMMENT ON COLUMN sys_notification_template.category IS '模板分类';
COMMENT ON COLUMN sys_notification_template.is_system IS '是否系统模板';

-- 创建索引
CREATE UNIQUE INDEX uk_template_code ON sys_notification_template(template_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_template_type ON sys_notification_template(notification_type, tenant_id) WHERE is_deleted = FALSE;

-- 3. 创建通知配置表
-- ==============================================================
CREATE TABLE sys_notification_config (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    config_type VARCHAR(50) NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP
);

COMMENT ON TABLE sys_notification_config IS '通知配置表';
COMMENT ON COLUMN sys_notification_config.config_type IS '配置类型 (email-邮件 sms-短信 push-推送)';
COMMENT ON COLUMN sys_notification_config.config_key IS '配置键';
COMMENT ON COLUMN sys_notification_config.config_value IS '配置值';
COMMENT ON COLUMN sys_notification_config.is_encrypted IS '是否加密';

-- 创建索引
CREATE UNIQUE INDEX uk_config ON sys_notification_config(config_type, config_key, tenant_id);

-- 4. 创建通知发送历史表
-- ==============================================================
CREATE TABLE sys_notification_send_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    notification_type VARCHAR(20) NOT NULL,
    template_id BIGINT,
    receiver_id BIGINT NOT NULL,
    receiver_address VARCHAR(200) NOT NULL,
    title VARCHAR(200),
    content TEXT,
    send_status VARCHAR(20) NOT NULL,
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    send_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_notification_send_log IS '通知发送历史表';
COMMENT ON COLUMN sys_notification_send_log.notification_type IS '通知类型';
COMMENT ON COLUMN sys_notification_send_log.template_id IS '模板ID';
COMMENT ON COLUMN sys_notification_send_log.receiver_id IS '接收者ID';
COMMENT ON COLUMN sys_notification_send_log.receiver_address IS '接收地址（邮箱/手机号等）';
COMMENT ON COLUMN sys_notification_send_log.send_status IS '发送状态 (pending-待发送 sending-发送中 success-成功 failed-失败)';
COMMENT ON COLUMN sys_notification_send_log.error_message IS '错误信息';
COMMENT ON COLUMN sys_notification_send_log.retry_count IS '重试次数';
COMMENT ON COLUMN sys_notification_send_log.send_time IS '发送时间';

-- 创建索引
CREATE INDEX idx_send_log_receiver ON sys_notification_send_log(receiver_id);
CREATE INDEX idx_send_log_status ON sys_notification_send_log(send_status);
CREATE INDEX idx_send_log_time ON sys_notification_send_log(created_at);

-- 5. 插入系统默认通知模板
-- ==============================================================
INSERT INTO sys_notification_template (tenant_id, template_code, template_name, notification_type, title_template, content_template, variables, category, is_system, status)
VALUES
(0, 'WELCOME', '欢迎通知', 'system', '欢迎使用${systemName}', '欢迎您注册${systemName}！您的账号已创建成功。', '{"systemName":"系统名称"}', '系统', true, 1),
(0, 'PASSWORD_RESET', '密码重置', 'email', '密码重置通知', '<p>您好，</p><p>您的密码已重置为：<strong>${newPassword}</strong></p><p>请尽快登录系统修改密码。</p>', '{"newPassword":"新密码"}', '安全', true, 1),
(0, 'TASK_ASSIGNED', '任务分配', 'message', '新任务通知', '您有一个新的任务：<strong>${taskName}</strong><br>截止时间：${dueDate}', '{"taskName":"任务名称","dueDate":"截止日期"}', '工作流', true, 1);
