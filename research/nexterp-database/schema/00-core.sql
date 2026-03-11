-- ============================================================================
-- NextERP Core Schema
-- 核心表、枚举类型、通用函数
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 枚举类型定义
-- ----------------------------------------------------------------------------

-- 通用状态
CREATE TYPE general_status AS ENUM (
    'ACTIVE',       -- 激活
    'INACTIVE',     -- 未激活
    'DRAFT',        -- 草稿
    'PENDING',      -- 待审批
    'APPROVED',     -- 已审批
    'REJECTED',     -- 已拒绝
    'COMPLETED',    -- 已完成
    'CANCELLED',    -- 已取消
    'CLOSED'        -- 已关闭
);

-- 借贷标识
CREATE TYPE debit_credit AS ENUM ('D', 'C');

-- 性别
CREATE TYPE gender AS ENUM ('M', 'F', 'O');

-- 是/否
CREATE TYPE yes_no AS ENUM ('Y', 'N');

-- 审批状态
CREATE TYPE approval_status AS ENUM (
    'DRAFT',
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED',
    'WITHDRAWN'
);

-- ----------------------------------------------------------------------------
-- 通用函数
-- ----------------------------------------------------------------------------

-- 更新时间戳函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 生成业务编码函数
CREATE OR REPLACE FUNCTION generate_code(
    p_prefix VARCHAR,
    p_tenant_id UUID,
    p_sequence_name VARCHAR
) RETURNS VARCHAR AS $$
DECLARE
    v_sequence_value BIGINT;
    v_code VARCHAR;
BEGIN
    -- 获取序列值
    SELECT nextval(p_sequence_name) INTO v_sequence_value;

    -- 生成编码: 前缀 + 年月 + 6位序号
    v_code := p_prefix ||
              TO_CHAR(CURRENT_DATE, 'YYYYMM') ||
              LPAD(v_sequence_value::TEXT, 6, '0');

    RETURN v_code;
END;
$$ LANGUAGE plpgsql;

-- 检查时间有效性函数
CREATE OR REPLACE FUNCTION is_valid_on_date(
    p_valid_from DATE,
    p_valid_to DATE,
    p_check_date DATE DEFAULT CURRENT_DATE
) RETURNS BOOLEAN AS $$
BEGIN
    RETURN p_valid_from <= p_check_date AND
           COALESCE(p_valid_to, '9999-12-31') >= p_check_date;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 核心域表
-- ----------------------------------------------------------------------------

-- 货币
CREATE TABLE core_currency (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(3) NOT NULL,          -- ISO 货币代码
    name            VARCHAR(100) NOT NULL,        -- 货币名称
    name_en         VARCHAR(100),                 -- 英文名称
    symbol          VARCHAR(10),                  -- 货币符号
    decimal_places  INTEGER DEFAULT 2,            -- 小数位数

    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (code)
);

COMMENT ON TABLE core_currency IS '货币代码表';
COMMENT ON COLUMN core_currency.code IS 'ISO 4217 货币代码';

-- 插入常用货币
INSERT INTO core_currency (code, name, name_en, symbol, decimal_places) VALUES
('CNY', '人民币', 'Chinese Yuan', '¥', 2),
('USD', '美元', 'US Dollar', '$', 2),
('EUR', '欧元', 'Euro', '€', 2),
('JPY', '日元', 'Japanese Yen', '¥', 0),
('GBP', '英镑', 'British Pound', '£', 2),
('HKD', '港币', 'Hong Kong Dollar', 'HK$', 2);

-- 计量单位
CREATE TABLE core_uom (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(3) NOT NULL,          -- 单位代码
    name            VARCHAR(50) NOT NULL,         -- 单位名称
    name_en         VARCHAR(50),                  -- 英文名称
    uom_category    VARCHAR(20),                  -- 单位类别 (长度/重量/体积等)
    decimal_places  INTEGER DEFAULT 0,            -- 小数位数
    is_base         BOOLEAN DEFAULT FALSE,        -- 是否基础单位

    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (code)
);

COMMENT ON TABLE core_uom IS '计量单位表';

-- 插入常用单位
INSERT INTO core_uom (code, name, name_en, uom_category, decimal_places, is_base) VALUES
('EA', '个', 'Each', 'QUANTITY', 0, TRUE),
('PC', '件', 'Piece', 'QUANTITY', 0, FALSE),
('KG', '千克', 'Kilogram', 'WEIGHT', 3, TRUE),
('G', '克', 'Gram', 'WEIGHT', 3, FALSE),
('M', '米', 'Meter', 'LENGTH', 3, TRUE),
('CM', '厘米', 'Centimeter', 'LENGTH', 2, FALSE),
('L', '升', 'Liter', 'VOLUME', 3, TRUE),
('ML', '毫升', 'Milliliter', 'VOLUME', 2, FALSE),
('D', '天', 'Day', 'TIME', 0, TRUE),
('H', '小时', 'Hour', 'TIME', 2, FALSE);

-- 国家/地区
CREATE TABLE core_country (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(3) NOT NULL,          -- ISO 国家代码
    name            VARCHAR(100) NOT NULL,        -- 国家名称
    name_en         VARCHAR(100),                 -- 英文名称
    region          VARCHAR(50),                  -- 所属区域 (亚洲/欧洲等)
    phone_code      VARCHAR(10),                  -- 电话区号

    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (code)
);

COMMENT ON TABLE core_country IS '国家/地区表';

-- 插入常用国家
INSERT INTO core_country (code, name, name_en, region, phone_code) VALUES
('CN', '中国', 'China', 'ASIA', '86'),
('US', '美国', 'United States', 'AMERICA', '1'),
('JP', '日本', 'Japan', 'ASIA', '81'),
('KR', '韩国', 'South Korea', 'ASIA', '82'),
('DE', '德国', 'Germany', 'EUROPE', '49'),
('GB', '英国', 'United Kingdom', 'EUROPE', '44'),
('FR', '法国', 'France', 'EUROPE', '33'),
('SG', '新加坡', 'Singapore', 'ASIA', '65'),
('HK', '香港', 'Hong Kong', 'ASIA', '852'),
('TW', '台湾', 'Taiwan', 'ASIA', '886');

-- 省份/州
CREATE TABLE core_region (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id      UUID NOT NULL REFERENCES core_country(id),
    code            VARCHAR(10) NOT NULL,         -- 省份代码
    name            VARCHAR(100) NOT NULL,        -- 省份名称
    name_en         VARCHAR(100),                 -- 英文名称

    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (country_id, code)
);

COMMENT ON TABLE core_region IS '省份/州表';

-- 插入中国省份
INSERT INTO core_region (country_id, code, name, name_en)
SELECT id, 'BJ', '北京', 'Beijing' FROM core_country WHERE code = 'CN'
UNION ALL
SELECT id, 'SH', '上海', 'Shanghai' FROM core_country WHERE code = 'CN'
UNION ALL
SELECT id, 'GD', '广东', 'Guangdong' FROM core_country WHERE code = 'CN'
UNION ALL
SELECT id, 'ZJ', '浙江', 'Zhejiang' FROM core_country WHERE code = 'CN'
UNION ALL
SELECT id, 'JS', '江苏', 'Jiangsu' FROM core_country WHERE code = 'CN';

-- 城市
CREATE TABLE core_city (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    region_id       UUID NOT NULL REFERENCES core_region(id),
    code            VARCHAR(10) NOT NULL,         -- 城市代码
    name            VARCHAR(100) NOT NULL,        -- 城市名称
    name_en         VARCHAR(100),                 -- 英文名称

    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (region_id, code)
);

COMMENT ON TABLE core_city IS '城市表';

-- 序列号管理
CREATE TABLE core_sequence (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    sequence_name   VARCHAR(50) NOT NULL,         -- 序列名称
    prefix          VARCHAR(10),                  -- 前缀
    current_value   BIGINT DEFAULT 0,             -- 当前值
    increment_by    INTEGER DEFAULT 1,            -- 步长
    max_value       BIGINT DEFAULT 999999,        -- 最大值
    cycle           BOOLEAN DEFAULT TRUE,         -- 是否循环

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, sequence_name)
);

COMMENT ON TABLE core_sequence IS '序列号管理表';

-- 附件
CREATE TABLE core_attachment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    entity_type     VARCHAR(50) NOT NULL,         -- 关联实体类型
    entity_id       UUID NOT NULL,                -- 关联实体 ID

    file_name       VARCHAR(255) NOT NULL,        -- 文件名
    file_path       VARCHAR(500) NOT NULL,        -- 存储路径
    file_size       BIGINT,                       -- 文件大小 (bytes)
    file_type       VARCHAR(100),                 -- MIME 类型
    file_hash       VARCHAR(64),                  -- 文件哈希

    description     TEXT,                         -- 描述

    uploaded_by     UUID,                         -- 上传人
    uploaded_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE core_attachment IS '附件表';

-- 备注/注释
CREATE TABLE core_note (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    entity_type     VARCHAR(50) NOT NULL,         -- 关联实体类型
    entity_id       UUID NOT NULL,                -- 关联实体 ID

    note_type       VARCHAR(20) DEFAULT 'GENERAL', -- 备注类型
    content         TEXT NOT NULL,                -- 内容

    created_by      UUID,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_core_note_entity ON core_note (entity_type, entity_id);

COMMENT ON TABLE core_note IS '备注表';

-- 更改日志 (审计)
CREATE TABLE core_change_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    entity_type     VARCHAR(50) NOT NULL,
    entity_id       UUID NOT NULL,

    action          VARCHAR(20) NOT NULL,         -- INSERT/UPDATE/DELETE
    old_values      JSONB,                        -- 旧值
    new_values      JSONB,                        -- 新值
    changed_fields  TEXT[],                       -- 变更字段列表

    changed_by      UUID,
    changed_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_id      VARCHAR(100),                 -- 会话 ID
    ip_address      VARCHAR(45)                   -- IP 地址
);

CREATE INDEX idx_core_change_log_entity ON core_change_log (entity_type, entity_id);
CREATE INDEX idx_core_change_log_time ON core_change_log (changed_at);

COMMENT ON TABLE core_change_log IS '变更日志表';

-- ----------------------------------------------------------------------------
-- 创建触发器
-- ----------------------------------------------------------------------------

-- 为核心表添加更新时间触发器
CREATE TRIGGER trigger_core_currency_updated_at
    BEFORE UPDATE ON core_currency
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_core_uom_updated_at
    BEFORE UPDATE ON core_uom
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_core_sequence_updated_at
    BEFORE UPDATE ON core_sequence
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
