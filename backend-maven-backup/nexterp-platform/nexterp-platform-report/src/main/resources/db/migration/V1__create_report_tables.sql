-- ==============================================================
-- 报表模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: 报表定义、报表模板相关表结构
-- ==============================================================

-- 1. 创建报表定义表
-- ==============================================================
CREATE TABLE sys_report (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    report_code VARCHAR(50) NOT NULL,
    report_name VARCHAR(100) NOT NULL,
    report_type VARCHAR(20) NOT NULL DEFAULT 'list',
    data_source VARCHAR(20) NOT NULL DEFAULT 'sql',
    sql_text TEXT,
    api_url VARCHAR(500),
    request_method VARCHAR(10) DEFAULT 'POST',
    request_headers TEXT,
    request_body_template TEXT,
    response_data_path VARCHAR(200),
    column_config TEXT NOT NULL,
    export_config TEXT,
    schedule_config TEXT,
    status INT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_report IS '报表定义表';
COMMENT ON COLUMN sys_report.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_report.report_code IS '报表编码（唯一标识）';
COMMENT ON COLUMN sys_report.report_name IS '报表名称';
COMMENT ON COLUMN sys_report.report_type IS '报表类型 (list-列表 chart-图表 cross-交叉表)';
COMMENT ON COLUMN sys_report.data_source IS '数据源类型 (sql-SQL api-API接口)';
COMMENT ON COLUMN sys_report.sql_text IS 'SQL查询语句';
COMMENT ON COLUMN sys_report.api_url IS 'API接口地址';
COMMENT ON COLUMN sys_report.request_method IS '请求方法 (GET/POST)';
COMMENT ON COLUMN sys_report.request_headers IS '请求头配置(JSON)';
COMMENT ON COLUMN sys_report.request_body_template IS '请求体模板';
COMMENT ON COLUMN sys_report.response_data_path IS '响应数据路径(JSON Path)';
COMMENT ON COLUMN sys_report.column_config IS '列配置(JSON格式)';
COMMENT ON COLUMN sys_report.export_config IS '导出配置(JSON格式)';
COMMENT ON COLUMN sys_report.schedule_config IS '定时配置(JSON格式)';
COMMENT ON COLUMN sys_report.status IS '状态 (0-禁用 1-正常)';

-- 创建索引
CREATE UNIQUE INDEX uk_report_code ON sys_report(report_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_report_type ON sys_report(report_type, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_status ON sys_report(status, tenant_id) WHERE is_deleted = FALSE;

-- 2. 创建报表模板表
-- ==============================================================
CREATE TABLE sys_report_template (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    template_type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    thumbnail_url VARCHAR(500),
    category VARCHAR(50),
    tags VARCHAR(200),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    status INT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_report_template IS '报表模板表';
COMMENT ON COLUMN sys_report_template.template_code IS '模板编码';
COMMENT ON COLUMN sys_report_template.template_name IS '模板名称';
COMMENT ON COLUMN sys_report_template.template_type IS '模板类型 (excel-Excel模板 pdf-PDF模板 html-HTML模板)';
COMMENT ON COLUMN sys_report_template.content IS '模板内容';
COMMENT ON COLUMN sys_report_template.thumbnail_url IS '缩略图URL';
COMMENT ON COLUMN sys_report_template.category IS '模板分类';
COMMENT ON COLUMN sys_report_template.tags IS '标签（逗号分隔）';
COMMENT ON COLUMN sys_report_template.is_system IS '是否系统模板';

-- 创建索引
CREATE UNIQUE INDEX uk_template_code ON sys_report_template(template_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_template_type ON sys_report_template(template_type, tenant_id) WHERE is_deleted = FALSE;

-- 3. 创建报表权限表
-- ==============================================================
CREATE TABLE sys_report_permission (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    report_id BIGINT NOT NULL,
    permission_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_report_permission IS '报表权限表';
COMMENT ON COLUMN sys_report_permission.report_id IS '报表ID';
COMMENT ON COLUMN sys_report_permission.permission_type IS '权限类型 (user-用户 role-角色 dept-部门)';
COMMENT ON COLUMN sys_report_permission.target_id IS '目标ID';

-- 创建索引
CREATE INDEX idx_report_permission_report ON sys_report_permission(report_id);
CREATE INDEX idx_report_permission_target ON sys_report_permission(permission_type, target_id);

-- 4. 创建报表访问历史表
-- ==============================================================
CREATE TABLE sys_report_access_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    report_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50),
    access_type VARCHAR(20) NOT NULL,
    parameters TEXT,
    execution_time INT,
    row_count INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_report_access_log IS '报表访问历史表';
COMMENT ON COLUMN sys_report_access_log.report_id IS '报表ID';
COMMENT ON COLUMN sys_report_access_log.user_id IS '用户ID';
COMMENT ON COLUMN sys_report_access_log.access_type IS '访问类型 (view-查看 export-导出)';
COMMENT ON COLUMN sys_report_access_log.parameters IS '查询参数';
COMMENT ON COLUMN sys_report_access_log.execution_time IS '执行时间（毫秒）';
COMMENT ON COLUMN sys_report_access_log.row_count IS '数据行数';

-- 创建索引
CREATE INDEX idx_access_log_report ON sys_report_access_log(report_id);
CREATE INDEX idx_access_log_user ON sys_report_access_log(user_id);
CREATE INDEX idx_access_log_time ON sys_report_access_log(created_at);

-- 5. 插入系统默认报表模板
-- ==============================================================
INSERT INTO sys_report_template (tenant_id, template_code, template_name, template_type, content, category, is_system, status)
VALUES
(0, 'DEFAULT_EXCEL', '默认Excel模板', 'excel', '{"header":{"backgroundColor":"#4472C4","fontColor":"#FFFFFF","fontSize":"12","bold":true},"row":{"alternate":true,"border":true},"footer":{"showPageNumber":true}}', '通用', true, 1),
(0, 'DEFAULT_PDF', '默认PDF模板', 'pdf', '{"pageSize":"A4","orientation":"portrait","margin":{"top":20,"right":20,"bottom":20,"left":20},"header":{"show":true,"text":"报表导出"},"footer":{"showPageNumber":true}}', '通用', true, 1);
