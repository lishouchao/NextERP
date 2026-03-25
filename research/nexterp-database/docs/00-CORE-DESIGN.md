# CORE 模块数据库设计

**模块**: Core Foundation (核心基础)
**说明**: 枚举类型、基础表、通用函数、触发器
**版本**: 1.0

---

## 1. 模块概述

### 1.1 设计目的

CORE 模块是 NextERP 的基础设施层，提供：

1. **枚举类型** - 系统级枚举定义
2. **基础数据表** - 国家、地区、城市、货币、语言等
3. **通用函数** - 编号生成、审计触发器、验证函数
4. **系统配置** - 全局参数、用户偏好

### 1.2 依赖关系

```
                    ┌─────────────────────┐
                    │    00-core.sql      │
                    │    (基础设施层)      │
                    └─────────────────────┘
                              │
        ┌─────────────┬───────┼───────┬─────────────┐
        ▼             ▼       ▼       ▼             ▼
   ┌─────────┐  ┌─────────┐  ···  ┌─────────┐  ┌─────────┐
   │01-tenant│  │ 02-bp   │       │ 03-fico │  │ 04-mm   │
   └─────────┘  └─────────┘       └─────────┘  └─────────┘
        │             │                │             │
        └─────────────┴────────────────┴─────────────┘
                              │
                    所有业务模块依赖 core
```

---

## 2. 枚举类型

### 2.1 通用状态枚举

```sql
-- 通用状态
CREATE TYPE general_status AS ENUM (
    'ACTIVE',      -- 激活
    'INACTIVE',    -- 未激活
    'DELETED'      -- 已删除
);

-- 审批状态
CREATE TYPE approval_status AS ENUM (
    'DRAFT',       -- 草稿
    'PENDING',     -- 待审批
    'APPROVED',    -- 已审批
    'REJECTED',    -- 已拒绝
    'CANCELLED'    -- 已取消
);

-- 文档状态
CREATE TYPE document_status AS ENUM (
    'DRAFT',       -- 草稿
    'PENDING',     -- 待处理
    'POSTED',      -- 已过账
    'REVERSED',    -- 已冲销
    'ARCHIVED'     -- 已归档
);

-- 性别
CREATE TYPE gender AS ENUM (
    'M',           -- 男
    'F',           -- 女
    'O'            -- 其他
);

-- 是/否/未知
CREATE TYPE yes_no_unknown AS ENUM (
    'Y',           -- 是
    'N',           -- 否
    'U'            -- 未知
);
```

### 2.2 业务枚举

```sql
-- 借贷标识
CREATE TYPE debit_credit AS ENUM (
    'D',           -- 借方
    'C'            -- 贷方
);

-- 货币类型
CREATE TYPE currency_type AS ENUM (
    'FI',          -- 财务货币
    'CO',          -- 控制货币
    'TR',          -- 交易货币
    'LO'           -- 本地货币
);

-- 时间单位
CREATE TYPE time_unit AS ENUM (
    'DAY',         -- 天
    'WEEK',        -- 周
    'MON',         -- 月
    'QTR',         -- 季度
    'YER'          -- 年
);

-- 金额符号
CREATE TYPE amount_sign AS ENUM (
    'POSITIVE',    -- 正数
    'NEGATIVE',    -- 负数
    'ZERO'         -- 零
);
```

---

## 3. 基础数据表

### 3.1 国家 (core_country)

```sql
CREATE TABLE core_country (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 国家编码
    country_code    VARCHAR(3) NOT NULL UNIQUE, -- ISO 3166-1 alpha-3
    country_code_2  VARCHAR(2),                 -- ISO 3166-1 alpha-2

    -- 名称
    country_name    VARCHAR(100) NOT NULL,      -- 国家名称
    country_name_en VARCHAR(100),               -- 英文名称

    -- 属性
    is_eu           BOOLEAN DEFAULT FALSE,      -- 是否欧盟
    is_active       BOOLEAN DEFAULT TRUE,

    -- 格式
    phone_code      VARCHAR(5),                 -- 电话区号
    currency_code   VARCHAR(3),                 -- 默认货币

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**示例数据**:

| country_code | country_code_2 | country_name | phone_code | currency_code |
|--------------|----------------|--------------|------------|---------------|
| CHN | CN | 中国 | 86 | CNY |
| USA | US | 美国 | 1 | USD |
| DEU | DE | 德国 | 49 | EUR |
| JPN | JP | 日本 | 81 | JPY |

### 3.2 地区/省份 (core_region)

```sql
CREATE TABLE core_region (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id      UUID NOT NULL REFERENCES core_country(id),

    -- 地区编码
    region_code     VARCHAR(10) NOT NULL,       -- 地区编码
    region_type     VARCHAR(2),                 -- 地区类型
    -- 01:省/州 02:市 03:区/县

    -- 名称
    region_name     VARCHAR(100) NOT NULL,      -- 地区名称
    region_name_en  VARCHAR(100),               -- 英文名称

    -- 属性
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (country_id, region_code)
);
```

### 3.3 城市 (core_city)

```sql
CREATE TABLE core_city (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country_id      UUID NOT NULL REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),

    -- 城市编码
    city_code       VARCHAR(10) NOT NULL,       -- 城市编码

    -- 名称
    city_name       VARCHAR(100) NOT NULL,      -- 城市名称
    city_name_en    VARCHAR(100),               -- 英文名称

    -- 属性
    city_type       VARCHAR(2),                 -- 城市类型
    -- 01:直辖市 02:地级市 03:县级市 04:县
    is_active       BOOLEAN DEFAULT TRUE,

    -- 地理位置
    latitude        DECIMAL(10,7),
    longitude       DECIMAL(10,7),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (country_id, city_code)
);
```

### 3.4 货币 (core_currency)

```sql
CREATE TABLE core_currency (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 货币编码
    currency_code   VARCHAR(3) NOT NULL UNIQUE, -- ISO 4217

    -- 名称
    currency_name   VARCHAR(100) NOT NULL,      -- 货币名称
    currency_symbol VARCHAR(10),                -- 货币符号

    -- 小数位
    decimals        INTEGER DEFAULT 2,          -- 小数位数

    -- 属性
    is_active       BOOLEAN DEFAULT TRUE,
    is_crypto       BOOLEAN DEFAULT FALSE,      -- 是否加密货币

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**示例数据**:

| currency_code | currency_name | currency_symbol | decimals |
|---------------|---------------|-----------------|----------|
| CNY | 人民币 | ¥ | 2 |
| USD | 美元 | $ | 2 |
| EUR | 欧元 | € | 2 |
| JPY | 日元 | ¥ | 0 |
| GBP | 英镑 | £ | 2 |

### 3.5 语言 (core_language)

```sql
CREATE TABLE core_language (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 语言编码
    language_code   VARCHAR(5) NOT NULL UNIQUE, -- ISO 639-1/639-2

    -- 名称
    language_name   VARCHAR(100) NOT NULL,      -- 语言名称
    language_name_en VARCHAR(100),              -- 英文名称

    -- 区域设置
    locale          VARCHAR(10),                -- 区域设置 (zh_CN, en_US)

    -- 属性
    is_active       BOOLEAN DEFAULT TRUE,
    is_default      BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.6 时区 (core_timezone)

```sql
CREATE TABLE core_timezone (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 时区标识
    timezone_code   VARCHAR(40) NOT NULL UNIQUE, -- IANA 时区标识

    -- 名称
    timezone_name   VARCHAR(100) NOT NULL,       -- 时区名称

    -- 偏移
    utc_offset      VARCHAR(6),                  -- UTC 偏移 (+08:00)
    dst_offset      VARCHAR(6),                  -- 夏令时偏移

    -- 属性
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.7 计量单位 (core_unit_of_measure)

```sql
CREATE TABLE core_unit_of_measure (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 单位编码
    uom_code        VARCHAR(3) NOT NULL UNIQUE, -- ISO 单位编码

    -- 名称
    uom_name        VARCHAR(50) NOT NULL,       -- 单位名称
    uom_name_en     VARCHAR(50),                -- 英文名称

    -- 类型
    uom_type        VARCHAR(2) NOT NULL,        -- 单位类型
    -- 01:长度 02:重量 03:体积 04:面积 05:时间 06:数量

    -- 换算
    base_uom_id     UUID REFERENCES core_unit_of_measure(id),
    conversion_factor DECIMAL(15,6),            -- 换算系数

    -- 小数位
    decimals        INTEGER DEFAULT 3,

    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**示例数据**:

| uom_code | uom_name | uom_type | decimals |
|----------|----------|----------|----------|
| EA | 件/个 | 06 | 0 |
| KG | 千克 | 02 | 3 |
| M | 米 | 01 | 3 |
| L | 升 | 03 | 3 |
| SET | 套 | 06 | 0 |

---

## 4. 序列号管理

### 4.1 编号范围 (core_number_range)

```sql
CREATE TABLE core_number_range (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 范围标识
    object_type     VARCHAR(10) NOT NULL,       -- 对象类型
    -- BP:业务伙伴 JE:凭证 PO:采购订单 SO:销售订单
    range_code      VARCHAR(4) NOT NULL,        -- 范围代码

    -- 范围值
    from_number     VARCHAR(20) NOT NULL,       -- 起始号
    to_number       VARCHAR(20) NOT NULL,       -- 结束号
    current_number  VARCHAR(20),                -- 当前号
    prefix          VARCHAR(5),                 -- 前缀
    suffix          VARCHAR(5),                 -- 后缀

    -- 格式
    number_length   INTEGER DEFAULT 10,         -- 编号长度
    padding_char    CHAR(1) DEFAULT '0',        -- 填充字符

    -- 状态
    is_external     BOOLEAN DEFAULT FALSE,      -- 是否外部编号
    is_year_dependent BOOLEAN DEFAULT FALSE,    -- 是否按年度
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, object_type, range_code)
);
```

### 4.2 编号生成函数

```sql
CREATE OR REPLACE FUNCTION generate_business_code(
    p_tenant_id UUID,
    p_object_type VARCHAR,
    p_range_code VARCHAR DEFAULT NULL,
    p_fiscal_year INTEGER DEFAULT NULL
) RETURNS VARCHAR AS $$
DECLARE
    v_range RECORD;
    v_new_number VARCHAR(20);
    v_current_num BIGINT;
BEGIN
    -- 获取编号范围
    SELECT * INTO v_range
    FROM core_number_range
    WHERE tenant_id = p_tenant_id
      AND object_type = p_object_type
      AND (range_code = p_range_code OR p_range_code IS NULL)
      AND status = 'ACTIVE';

    IF NOT FOUND THEN
        RAISE EXCEPTION '未找到编号范围: %/%', p_object_type, p_range_code;
    END IF;

    -- 计算下一个编号
    IF v_range.current_number IS NULL THEN
        v_current_num := v_range.from_number::BIGINT;
    ELSE
        v_current_num := v_range.current_number::BIGINT + 1;
    END IF;

    -- 检查是否超出范围
    IF v_current_num > v_range.to_number::BIGINT THEN
        RAISE EXCEPTION '编号范围已用尽: %', v_range.range_code;
    END IF;

    -- 生成编号
    v_new_number := v_range.prefix ||
                    LPAD(v_current_num::TEXT, v_range.number_length, v_range.padding_char) ||
                    v_range.suffix;

    -- 更新当前编号
    UPDATE core_number_range
    SET current_number = v_current_num::TEXT,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = v_range.id;

    RETURN v_new_number;
END;
$$ LANGUAGE plpgsql;
```

---

## 5. 审计功能

### 5.1 更新时间戳函数

```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### 5.2 统一审计触发器函数

```sql
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    -- 更新时间戳
    NEW.updated_at = CURRENT_TIMESTAMP;

    -- INSERT 处理
    IF TG_OP = 'INSERT' THEN
        NEW.created_at = CURRENT_TIMESTAMP;
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = TG_TABLE_NAME
            AND column_name = 'version'
        ) THEN
            NEW.version = 0;
        END IF;
    END IF;

    -- UPDATE 处理
    IF TG_OP = 'UPDATE' THEN
        -- 乐观锁检查
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = TG_TABLE_NAME
            AND column_name = 'version'
        ) THEN
            IF OLD.version IS DISTINCT FROM NEW.version THEN
                RAISE EXCEPTION '乐观锁冲突: 表%, ID%', TG_TABLE_NAME, OLD.id;
            END IF;
            NEW.version = OLD.version + 1;
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### 5.3 添加审计触发器

```sql
CREATE OR REPLACE FUNCTION add_audit_trigger(p_table_name VARCHAR)
RETURNS VOID AS $$
BEGIN
    EXECUTE format('
        DROP TRIGGER IF EXISTS trigger_%s_audit ON %I;
        CREATE TRIGGER trigger_%s_audit
            BEFORE INSERT OR UPDATE ON %I
            FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();
    ', p_table_name, p_table_name, p_table_name, p_table_name);
END;
$$ LANGUAGE plpgsql;
```

### 5.4 审计日志表

```sql
CREATE TABLE core_audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID,

    -- 操作信息
    table_name      VARCHAR(100) NOT NULL,
    operation       VARCHAR(10) NOT NULL,       -- INSERT/UPDATE/DELETE
    record_id       UUID,

    -- 变更数据
    old_data        JSONB,
    new_data        JSONB,
    changed_fields  TEXT[],                    -- 变更的字段列表

    -- 操作者
    user_id         UUID,
    session_id      UUID,
    ip_address      VARCHAR(45),

    -- 时间
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (created_at);

-- 按月分区
CREATE TABLE core_audit_log_202603
    PARTITION OF core_audit_log
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
```

### 5.5 审计日志触发器

```sql
CREATE OR REPLACE FUNCTION audit_log_trigger_func()
RETURNS TRIGGER AS $$
DECLARE
    v_changed_fields TEXT[];
    v_old_data JSONB;
    v_new_data JSONB;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_old_data := to_jsonb(OLD);
        v_new_data := NULL;
    ELSIF TG_OP = 'INSERT' THEN
        v_old_data := NULL;
        v_new_data := to_jsonb(NEW);
    ELSIF TG_OP = 'UPDATE' THEN
        v_old_data := to_jsonb(OLD);
        v_new_data := to_jsonb(NEW);

        -- 计算变更字段
        SELECT array_agg(key) INTO v_changed_fields
        FROM jsonb_each(v_old_data)
        WHERE v_old_data->key IS DISTINCT FROM v_new_data->key;
    END IF;

    INSERT INTO core_audit_log (
        tenant_id,
        table_name,
        operation,
        record_id,
        old_data,
        new_data,
        changed_fields,
        user_id
    ) VALUES (
        COALESCE(NEW.tenant_id, OLD.tenant_id),
        TG_TABLE_NAME,
        TG_OP,
        COALESCE(NEW.id, OLD.id),
        v_old_data,
        v_new_data,
        v_changed_fields,
        current_setting('app.current_user_id', TRUE)::UUID
    );

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;
```

---

## 6. 附件管理

### 6.1 附件表 (core_attachment)

```sql
CREATE TABLE core_attachment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联对象
    object_type     VARCHAR(20) NOT NULL,       -- 对象类型
    object_id       UUID NOT NULL,              -- 对象ID

    -- 文件信息
    file_name       VARCHAR(255) NOT NULL,      -- 原始文件名
    file_type       VARCHAR(100),               -- MIME类型
    file_size       BIGINT,                     -- 文件大小(字节)
    file_hash       VARCHAR(64),                -- SHA256哈希

    -- 存储
    storage_type    VARCHAR(10) DEFAULT 'LOCAL', -- LOCAL/S3/OSS
    storage_path    VARCHAR(500),               -- 存储路径

    -- 分类
    category        VARCHAR(20),                -- 分类
    description     TEXT,                       -- 描述

    -- 状态
    is_deleted      BOOLEAN DEFAULT FALSE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    -- 索引
    created_at_idx  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attachment_object ON core_attachment (object_type, object_id);
CREATE INDEX idx_attachment_tenant ON core_attachment (tenant_id);
```

---

## 7. 系统参数

### 7.1 系统参数表 (core_system_parameter)

```sql
CREATE TABLE core_system_parameter (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID,                       -- NULL 表示全局参数

    -- 参数标识
    param_category  VARCHAR(20) NOT NULL,       -- 参数分类
    param_code      VARCHAR(50) NOT NULL,       -- 参数代码
    param_name      VARCHAR(100) NOT NULL,      -- 参数名称

    -- 参数值
    param_value     TEXT,                       -- 参数值
    param_type      VARCHAR(20) DEFAULT 'STRING', -- 类型
    -- STRING/INTEGER/DECIMAL/BOOLEAN/JSON/DATE

    -- 默认值
    default_value   TEXT,

    -- 描述
    description     TEXT,

    -- 状态
    is_readonly     BOOLEAN DEFAULT FALSE,
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, param_category, param_code)
);
```

**示例参数**:

| param_category | param_code | param_value | param_type |
|----------------|------------|-------------|------------|
| FI | FISCAL_YEAR_VARIANT | K4 | STRING |
| FI | MAX_DOC_AMOUNT | 999999999.99 | DECIMAL |
| MM | DEFAULT_PLANT | 1001 | STRING |
| HR | ANNUAL_LEAVE_DAYS | 15 | INTEGER |
| SYSTEM | DATE_FORMAT | YYYY-MM-DD | STRING |

### 7.2 参数获取函数

```sql
CREATE OR REPLACE FUNCTION get_system_param(
    p_tenant_id UUID,
    p_category VARCHAR,
    p_code VARCHAR,
    p_default TEXT DEFAULT NULL
) RETURNS TEXT AS $$
DECLARE
    v_value TEXT;
BEGIN
    -- 优先获取租户级参数
    SELECT param_value INTO v_value
    FROM core_system_parameter
    WHERE tenant_id = p_tenant_id
      AND param_category = p_category
      AND param_code = p_code
      AND status = 'ACTIVE';

    IF FOUND THEN
        RETURN v_value;
    END IF;

    -- 获取全局参数
    SELECT param_value INTO v_value
    FROM core_system_parameter
    WHERE tenant_id IS NULL
      AND param_category = p_category
      AND param_code = p_code
      AND status = 'ACTIVE';

    IF FOUND THEN
        RETURN v_value;
    END IF;

    -- 返回默认值
    RETURN p_default;
END;
$$ LANGUAGE plpgsql;
```

---

## 8. 用户与权限

### 8.0 设计说明

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     用户-员工架构 (对标 SAP BASIS + HR)                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  设计原则 (参考 SAP USR02 + PA0105):                                        │
│                                                                             │
│  1. auth_user - 纯认证层 (对标 SAP USR02)                                   │
│     • 只负责身份认证，不包含业务信息                                        │
│     • 全局唯一，跨租户                                                       │
│     • 不引用任何业务表                                                       │
│                                                                             │
│  2. sys_user - 业务用户层                                                   │
│     • 租户内的业务用户                                                       │
│     • 关联 auth_user 进行认证                                               │
│     • 通过 employee_number 逻辑关联员工 (无物理外键)                        │
│                                                                             │
│  3. hr_employee - 员工主数据 (HR模块)                                       │
│     • 独立的业务实体，不直接引用 auth_user                                  │
│     • 通过 employee_number 与 sys_user 逻辑关联                             │
│                                                                             │
│  关联方式:                                                                   │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ auth_user   │◄────│ sys_user    │     │ hr_employee │                   │
│  │ (认证)      │ 1:1 │ (业务用户)  │     │ (员工)      │                   │
│  └─────────────┘     └──────┬──────┘     └──────┬──────┘                   │
│                             │                    │                          │
│                             │ employee_number    │ employee_number          │
│                             │ (逻辑关联)         │ (值相等)                 │
│                             └────────────────────┘                          │
│                                                                             │
│  优点:                                                                      │
│  ✅ 无循环依赖 - auth_user 不引用任何业务表                                 │
│  ✅ 模块独立 - sys_user 在 CORE，其他模块可用                               │
│  ✅ HR 可选 - 不启用 HR 模块也能有用户系统                                  │
│  ✅ 灵活关联 - 一个员工可以有多个账号 (多租户/代理)                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 8.1 认证用户表 (auth_user)

对标 SAP USR02，纯身份认证，不包含业务信息。

```sql
CREATE TABLE auth_user (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 登录凭证 (全局唯一)
    username        VARCHAR(50) NOT NULL UNIQUE,    -- 登录名
    password_hash   VARCHAR(255),                    -- 密码哈希 (SSO用户可为空)

    -- 联系方式 (用于登录/验证/找回密码)
    email           VARCHAR(100) UNIQUE,             -- 邮箱 (可用于登录)
    phone           VARCHAR(50),                     -- 手机 (可用于登录)

    -- 认证方式
    auth_type       VARCHAR(2) DEFAULT 'LO',         -- LO:本地 LD:LDAP OA:OAuth SI:SAML
    external_id     VARCHAR(100),                    -- 外部系统ID (SSO用)

    -- 多因素认证
    mfa_enabled     BOOLEAN DEFAULT FALSE,           -- 是否启用MFA
    mfa_secret      VARCHAR(100),                    -- MFA密钥 (加密存储)

    -- 登录状态
    is_active       BOOLEAN DEFAULT TRUE,            -- 是否激活
    is_locked       BOOLEAN DEFAULT FALSE,           -- 是否锁定
    is_system       BOOLEAN DEFAULT FALSE,           -- 是否系统用户 (不能登录UI)
    must_change_pwd BOOLEAN DEFAULT FALSE,           -- 必须修改密码

    -- 登录统计
    last_login_at   TIMESTAMP,                       -- 最后登录时间
    last_login_ip   VARCHAR(45),                     -- 最后登录IP
    failed_attempts INTEGER DEFAULT 0,               -- 连续失败次数
    locked_until    TIMESTAMP,                       -- 锁定至

    -- 密码历史 (防止重复使用)
    password_changed_at TIMESTAMP,                   -- 密码修改时间
    password_history JSONB DEFAULT '[]',             -- 密码历史哈希

    -- 审计 (不引用任何业务表)
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_auth_user_email ON auth_user (email);
CREATE INDEX idx_auth_user_phone ON auth_user (phone);
CREATE INDEX idx_auth_user_type ON auth_user (auth_type);
```

**认证类型说明**:

| auth_type | 说明 | 密码 |
|-----------|------|------|
| LO | 本地认证 | 必填 |
| LD | LDAP/AD | 可空 |
| OA | OAuth2.0 | 可空 |
| SI | SAML | 可空 |

### 8.2 系统用户表 (sys_user)

业务用户，关联认证用户，管理租户内的权限和偏好。

```sql
CREATE TABLE sys_user (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    -- 认证用户关联 (必填)
    auth_user_id    UUID NOT NULL REFERENCES auth_user(id) ON DELETE CASCADE,

    -- 业务信息
    display_name    VARCHAR(80) NOT NULL,            -- 显示名称
    avatar_url      VARCHAR(500),                    -- 头像URL

    -- 用户类型
    user_type       VARCHAR(2) DEFAULT 'EM',         -- EM:内部员工 EX:外部 SY:系统
    user_category   VARCHAR(4),                      -- 用户分类

    -- ★★★ 员工关联 - 逻辑关联，无物理外键 ★★★
    -- 对应 hr_employee.employee_number，通过值匹配建立关联
    employee_number VARCHAR(8),                      -- 员工编号 (可为空:外部用户)

    -- 权限标识
    is_admin        BOOLEAN DEFAULT FALSE,           -- 租户管理员
    is_super_admin  BOOLEAN DEFAULT FALSE,           -- 超级管理员 (跨租户)

    -- 偏好设置
    language_id     UUID REFERENCES core_language(id),
    timezone_id     UUID REFERENCES core_timezone(id),
    preferences     JSONB DEFAULT '{}',              -- 用户偏好

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,            -- 业务激活状态

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID REFERENCES sys_user(id),    -- 自引用允许
    updated_by      UUID REFERENCES sys_user(id),
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, auth_user_id)
);

-- 索引
CREATE INDEX idx_sys_user_tenant ON sys_user (tenant_id);
CREATE INDEX idx_sys_user_auth ON sys_user (auth_user_id);
CREATE INDEX idx_sys_user_employee ON sys_user (tenant_id, employee_number);
CREATE INDEX idx_sys_user_type ON sys_user (tenant_id, user_type);
```

**用户类型说明**:

| user_type | 说明 | employee_number |
|-----------|------|-----------------|
| EM | 内部员工 | 必填 |
| EX | 外部用户 | 可空 |
| SY | 系统用户 | 可空 |

**员工关联说明**:

```sql
-- 逻辑关联查询示例
SELECT
    su.id AS sys_user_id,
    su.display_name,
    su.employee_number,
    emp.id AS employee_id,
    emp.full_name AS employee_name
FROM sys_user su
LEFT JOIN hr_employee emp
    ON emp.tenant_id = su.tenant_id
    AND emp.employee_number = su.employee_number
WHERE su.tenant_id = :tenant_id;
```

### 8.2 角色表 (sys_role)

```sql
CREATE TABLE sys_role (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    -- 角色信息
    role_code       VARCHAR(20) NOT NULL,       -- 角色代码
    role_name       VARCHAR(100) NOT NULL,      -- 角色名称
    description     TEXT,

    -- 类型
    role_type       VARCHAR(2) DEFAULT 'CU',    -- SY:系统 CU:自定义

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, role_code)
);
```

### 8.3 用户角色关联 (sys_user_role)

```sql
CREATE TABLE sys_user_role (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    user_id         UUID NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    role_id         UUID NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (user_id, role_id)
);
```

### 8.4 权限表 (sys_permission)

```sql
CREATE TABLE sys_permission (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID,

    -- 权限标识
    permission_code VARCHAR(50) NOT NULL,       -- 权限代码
    permission_name VARCHAR(100) NOT NULL,      -- 权限名称

    -- 资源
    resource_type   VARCHAR(20),                -- 资源类型
    -- MENU/BUTTON/API/DATA
    resource_code   VARCHAR(100),               -- 资源代码

    -- 操作
    action          VARCHAR(20),                -- 操作
    -- CREATE/READ/UPDATE/DELETE/EXECUTE

    -- 父权限
    parent_id       UUID REFERENCES sys_permission(id),

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, permission_code)
);
```

### 8.5 角色权限关联 (sys_role_permission)

```sql
CREATE TABLE sys_role_permission (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    role_id         UUID NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    permission_id   UUID NOT NULL REFERENCES sys_permission(id) ON DELETE CASCADE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (role_id, permission_id)
);
```

---

## 9. 索引策略

```sql
-- 国家/地区/城市
CREATE INDEX idx_core_country_code ON core_country (country_code);
CREATE INDEX idx_core_region_country ON core_region (country_id);
CREATE INDEX idx_core_city_region ON core_city (region_id);

-- 货币/语言
CREATE INDEX idx_core_currency_code ON core_currency (currency_code);
CREATE INDEX idx_core_language_code ON core_language (language_code);

-- 编号范围
CREATE INDEX idx_core_nr_object ON core_number_range (tenant_id, object_type);

-- 审计日志
CREATE INDEX idx_core_audit_table ON core_audit_log (table_name, record_id);
CREATE INDEX idx_core_audit_time ON core_audit_log (created_at);

-- 用户
CREATE INDEX idx_sys_user_username ON sys_user (tenant_id, username);
CREATE INDEX idx_sys_user_role_user ON sys_user_role (user_id);
CREATE INDEX idx_sys_role_perm_role ON sys_role_permission (role_id);
```

---

## 10. 初始化数据

### 10.1 必要的初始化数据

```sql
-- 国家
INSERT INTO core_country (country_code, country_code_2, country_name, phone_code, currency_code) VALUES
('CHN', 'CN', '中国', '86', 'CNY'),
('USA', 'US', '美国', '1', 'USD'),
('DEU', 'DE', '德国', '49', 'EUR'),
('JPN', 'JP', '日本', '81', 'JPY'),
('GBR', 'GB', '英国', '44', 'GBP');

-- 货币
INSERT INTO core_currency (currency_code, currency_name, currency_symbol, decimals) VALUES
('CNY', '人民币', '¥', 2),
('USD', '美元', '$', 2),
('EUR', '欧元', '€', 2),
('JPY', '日元', '¥', 0),
('GBP', '英镑', '£', 2);

-- 语言
INSERT INTO core_language (language_code, language_name, locale, is_default) VALUES
('zh_CN', '简体中文', 'zh_CN', TRUE),
('en_US', 'English', 'en_US', FALSE);

-- 计量单位
INSERT INTO core_unit_of_measure (uom_code, uom_name, uom_type, decimals) VALUES
('EA', '件', '06', 0),
('KG', '千克', '02', 3),
('M', '米', '01', 3),
('L', '升', '03', 3),
('SET', '套', '06', 0),
('PC', '台', '06', 0);
```

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
