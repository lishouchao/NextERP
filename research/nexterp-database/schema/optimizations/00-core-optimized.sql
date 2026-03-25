-- ============================================================================
-- NextERP 优化版 Core Schema
-- 优化点：统一审计触发器、软删除、乐观锁
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 通用审计触发器
-- ----------------------------------------------------------------------------

-- 统一审计函数
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    -- 更新时间
    NEW.updated_at = CURRENT_TIMESTAMP;

    -- 更新人（从会话变量获取）
    BEGIN
        NEW.updated_by = current_setting('app.current_user_id', TRUE)::UUID;
    EXCEPTION WHEN OTHERS THEN
        NEW.updated_by = COALESCE(NEW.updated_by, NEW.created_by);
    END;

    -- 新记录设置创建信息
    IF TG_OP = 'INSERT' THEN
        NEW.created_at = CURRENT_TIMESTAMP;
        NEW.created_by = NEW.updated_by;
        NEW.version = 0;
    END IF;

    -- 乐观锁版本递增
    IF TG_OP = 'UPDATE' THEN
        -- 检查版本
        IF OLD.version IS DISTINCT FROM NEW.version THEN
            RAISE EXCEPTION '乐观锁冲突：记录已被其他用户修改';
        END IF;
        NEW.version = OLD.version + 1;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 软删除函数
CREATE OR REPLACE FUNCTION soft_delete_func()
RETURNS TRIGGER AS $$
BEGIN
    -- 不实际删除，标记为已删除
    NEW.is_deleted = TRUE;
    NEW.deleted_at = CURRENT_TIMESTAMP;

    BEGIN
        NEW.deleted_by = current_setting('app.current_user_id', TRUE)::UUID;
    EXCEPTION WHEN OTHERS THEN
        NEW.deleted_by = NULL;
    END;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 2. 通用查询函数
-- ----------------------------------------------------------------------------

-- 获取当前有效记录
CREATE OR REPLACE FUNCTION get_current_record(
    p_table_name TEXT,
    p_key_column TEXT,
    p_key_value UUID,
    p_check_date DATE DEFAULT CURRENT_DATE
) RETURNS UUID AS $$
DECLARE
    v_sql TEXT;
    v_id UUID;
BEGIN
    v_sql := format('
        SELECT id FROM %I
        WHERE %I = $1
          AND valid_from <= $2
          AND valid_to >= $2
          AND (is_deleted IS NULL OR is_deleted = FALSE)
        LIMIT 1
    ', p_table_name, p_key_column);

    EXECUTE v_sql INTO v_id USING p_key_value, p_check_date;
    RETURN v_id;
END;
$$ LANGUAGE plpgsql;

-- 时间有效性检查
CREATE OR REPLACE FUNCTION check_time_overlap(
    p_table_name TEXT,
    p_key_column TEXT,
    p_key_value UUID,
    p_valid_from DATE,
    p_valid_to DATE,
    p_exclude_id UUID DEFAULT NULL
) RETURNS BOOLEAN AS $$
DECLARE
    v_count INTEGER;
    v_sql TEXT;
BEGIN
    v_sql := format('
        SELECT COUNT(*) FROM %I
        WHERE %I = $1
          AND ($5 IS NULL OR id != $5)
          AND valid_from <= $3
          AND valid_to >= $2
          AND (is_deleted IS NULL OR is_deleted = FALSE)
    ', p_table_name, p_key_column);

    EXECUTE v_sql INTO v_count USING p_key_value, p_valid_from, p_valid_to, p_exclude_id;
    RETURN v_count > 0;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 3. 批量添加触发器的工具函数
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION add_audit_trigger(p_table_name TEXT)
RETURNS VOID AS $$
BEGIN
    EXECUTE format('
        DROP TRIGGER IF EXISTS trg_%s_audit ON %I;
        CREATE TRIGGER trg_%s_audit
            BEFORE INSERT OR UPDATE ON %I
            FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();
    ', p_table_name, p_table_name, p_table_name, p_table_name);
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 4. 行级安全策略 (RLS) 支持多租户
-- ----------------------------------------------------------------------------

-- 启用 RLS 的通用函数
CREATE OR REPLACE FUNCTION enable_tenant_rls(p_table_name TEXT)
RETURNS VOID AS $$
BEGIN
    -- 启用 RLS
    EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', p_table_name);

    -- 创建租户隔离策略
    EXECUTE format('
        CREATE POLICY tenant_isolation ON %I
            USING (tenant_id = current_setting(''app.current_tenant_id'', TRUE)::UUID)
    ', p_table_name);
END;
$$ LANGUAGE plpgsql;

-- 租户 ID 自动设置触发器
CREATE OR REPLACE FUNCTION auto_tenant_id()
RETURNS TRIGGER AS $$
BEGIN
    BEGIN
        NEW.tenant_id := current_setting('app.current_tenant_id', TRUE)::UUID;
    EXCEPTION WHEN OTHERS THEN
        -- 如果没有设置，保持原值
        NULL;
    END;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 5. 业务编码生成优化
-- ----------------------------------------------------------------------------

-- 高效编码生成函数（使用序列缓存）
CREATE OR REPLACE FUNCTION generate_business_code(
    p_tenant_id UUID,
    p_code_type VARCHAR(20),
    p_prefix VARCHAR(10),
    p_include_date BOOLEAN DEFAULT TRUE
) RETURNS VARCHAR AS $$
DECLARE
    v_sequence_name TEXT;
    v_current_value BIGINT;
    v_code VARCHAR;
    v_date_part VARCHAR;
BEGIN
    -- 构建序列名
    v_sequence_name := format('seq_%s_%s', p_code_type, replace(p_tenant_id::TEXT, '-', '_'));

    -- 检查序列是否存在，不存在则创建
    IF NOT EXISTS (
        SELECT 1 FROM pg_sequences
        WHERE schemaname = 'public' AND sequencename = v_sequence_name
    ) THEN
        EXECUTE format('CREATE SEQUENCE %I START 1 INCREMENT 1 CACHE 100', v_sequence_name);
    END IF;

    -- 获取序列值
    EXECUTE format('SELECT nextval(%L)', v_sequence_name) INTO v_current_value;

    -- 日期部分
    IF p_include_date THEN
        v_date_part := TO_CHAR(CURRENT_DATE, 'YYYYMM');
    ELSE
        v_date_part := '';
    END IF;

    -- 生成编码
    v_code := p_prefix || v_date_part || LPAD(v_current_value::TEXT, 6, '0');

    RETURN v_code;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 6. 审计日志表（统一）
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID,

    -- 操作信息
    table_name      VARCHAR(100) NOT NULL,
    operation       VARCHAR(10) NOT NULL,  -- INSERT/UPDATE/DELETE
    record_id       UUID NOT NULL,

    -- 变更数据
    old_data        JSONB,
    new_data        JSONB,
    changed_fields  TEXT[],

    -- 操作上下文
    operated_by     UUID,
    operated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_id      VARCHAR(100),
    ip_address      VARCHAR(45),
    user_agent      TEXT
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_table ON sys_audit_log (table_name, record_id);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_time ON sys_audit_log (operated_at);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_tenant ON sys_audit_log (tenant_id, operated_at);

-- 审计日志触发器
CREATE OR REPLACE FUNCTION audit_log_trigger_func()
RETURNS TRIGGER AS $$
DECLARE
    v_old_data JSONB;
    v_new_data JSONB;
    v_changed_fields TEXT[];
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_old_data := to_jsonb(OLD);
        v_new_data := NULL;
    ELSIF TG_OP = 'UPDATE' THEN
        v_old_data := to_jsonb(OLD);
        v_new_data := to_jsonb(NEW);
        -- 计算变更字段
        SELECT array_agg(key) INTO v_changed_fields
        FROM jsonb_object_keys(v_old_data) AS key
        WHERE v_old_data->key IS DISTINCT FROM v_new_data->key;
    ELSIF TG_OP = 'INSERT' THEN
        v_old_data := NULL;
        v_new_data := to_jsonb(NEW);
    END IF;

    INSERT INTO sys_audit_log (
        tenant_id,
        table_name,
        operation,
        record_id,
        old_data,
        new_data,
        changed_fields,
        operated_by,
        session_id,
        ip_address
    ) VALUES (
        COALESCE(NEW.tenant_id, OLD.tenant_id),
        TG_TABLE_NAME,
        TG_OP,
        COALESCE(NEW.id, OLD.id),
        v_old_data,
        v_new_data,
        v_changed_fields,
        COALESCE(
            current_setting('app.current_user_id', TRUE)::UUID,
            NEW.updated_by, OLD.updated_by
        ),
        current_setting('app.session_id', TRUE),
        current_setting('app.client_ip', TRUE)
    );

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 为需要审计的表添加触发器的函数
CREATE OR REPLACE FUNCTION add_audit_log_trigger(p_table_name TEXT)
RETURNS VOID AS $$
BEGIN
    EXECUTE format('
        DROP TRIGGER IF EXISTS trg_%s_audit_log ON %I;
        CREATE TRIGGER trg_%s_audit_log
            AFTER INSERT OR UPDATE OR DELETE ON %I
            FOR EACH ROW EXECUTE FUNCTION audit_log_trigger_func();
    ', p_table_name, p_table_name, p_table_name, p_table_name);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION audit_trigger_func IS '统一审计触发器函数';
COMMENT ON FUNCTION soft_delete_func IS '软删除触发器函数';
COMMENT ON FUNCTION generate_business_code IS '业务编码生成函数';
COMMENT ON TABLE sys_audit_log IS '统一审计日志表';
