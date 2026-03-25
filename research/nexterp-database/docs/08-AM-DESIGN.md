# AM 模块数据库设计

**模块**: Asset Management (资产管理/固定资产)
**对标**: SAP ECC AA (ANEP/ANLC/ANKT)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 资产主数据 | 资产卡片管理 | AS01/AS02/AS03 |
| 资产业务 | 购置、折旧、报废 | AB01/AB08/ABAVN |
| 折旧 | 折旧计算与过账 | AFAB/AFBP |
| 资产分类 | 资产类别管理 | OAOA |
| 资产浏览 | 资产报表 | AW01N |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     AM Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    资产主数据                            │    │
│  │                     am_asset                             │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │ 资产编码 | 描述 | 分类 | 公司 | 成本中心       │    │    │
│  │  │ 原值 | 净值 | 累计折旧 | 使用年限 | 残值       │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│        ┌─────────────────────┼─────────────────────┐            │
│        │                     │                     │            │
│        ▼                     ▼                     ▼            │
│  ┌──────────┐          ┌──────────┐          ┌──────────┐      │
│  │ 资产分类 │          │ 折旧范围 │          │ 资产业务 │      │
│  │am_class  │          │am_depr_  │          │am_trans  │      │
│  │          │          │area      │          │          │      │
│  └──────────┘          └──────────┘          └──────────┘      │
│        │                     │                     │            │
│        └─────────────────────┴─────────────────────┘            │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    折旧计算                              │    │
│  │                  am_depreciation                         │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 资产分类

### 2.1 资产分类 (am_asset_class)

对标 SAP ANKT

```sql
CREATE TABLE am_asset_class (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 分类信息
    class_code      VARCHAR(8) NOT NULL,       -- 分类代码
    class_name      VARCHAR(100) NOT NULL,     -- 分类名称
    description     TEXT,

    -- 分类类型
    class_type      VARCHAR(2),                -- 分类类型
    -- 01:固定资产 02:低值易耗品 03:无形资产 04:在建工程

    -- 账户确定
    account_determination VARCHAR(4),          -- 科目确定

    -- 默认值
    default_useful_life INTEGER,               -- 默认使用年限 (年)
    default_depr_method VARCHAR(2),            -- 默认折旧方法

    -- 号码范围
    number_range    VARCHAR(4),                -- 资产号码范围

    -- 层级
    parent_id       UUID REFERENCES am_asset_class(id),
    level           INTEGER DEFAULT 1,

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, class_code)
);
```

**常用资产分类**:

| class_code | class_name | class_type | default_useful_life |
|------------|------------|------------|---------------------|
| 1000 | 房屋建筑物 | 01 | 20 |
| 2000 | 机器设备 | 01 | 10 |
| 3000 | 运输工具 | 01 | 5 |
| 4000 | 电子设备 | 01 | 3 |
| 5000 | 办公设备 | 01 | 5 |
| 6000 | 无形资产 | 03 | 10 |

---

## 3. 资产主数据

### 3.1 资产主表 (am_asset)

对标 SAP ANLA

```sql
CREATE TABLE am_asset (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 资产标识
    asset_number    VARCHAR(12) NOT NULL,      -- 资产编号
    sub_number      VARCHAR(4) DEFAULT '0000', -- 子编号
    asset_code      VARCHAR(20) GENERATED ALWAYS AS (
        asset_number || '-' || sub_number
    ) STORED,                                   -- 完整资产号

    -- 描述
    description     VARCHAR(100) NOT NULL,     -- 资产描述
    description_2   VARCHAR(100),              -- 描述2

    -- 分类
    asset_class_id  UUID NOT NULL REFERENCES am_asset_class(id),
    asset_class_code VARCHAR(8),

    -- 公司
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    company_code    VARCHAR(4),

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),
    internal_order  VARCHAR(12),

    -- 位置
    plant_id        UUID REFERENCES sys_plant(id),
    location        VARCHAR(30),               -- 资产位置
    room            VARCHAR(10),               -- 房间号

    -- 负责人
    responsible_person UUID REFERENCES hr_employee(id),

    -- 供应商
    vendor_id       UUID REFERENCES bp_business_partner(id),

    -- 日期
    capitalization_date DATE,                  -- 资本化日期
    acquisition_date DATE,                     -- 购置日期
    first_acquisition_date DATE,               -- 首次购置日期
    deactivation_date DATE,                    -- 停用日期

    -- 数量
    quantity        DECIMAL(13,3) DEFAULT 1,
    unit            VARCHAR(3) DEFAULT 'EA',

    -- 原值 (本位币)
    acquisition_value DECIMAL(15,2) DEFAULT 0, -- 购置原值
    revaluation_value DECIMAL(15,2) DEFAULT 0, -- 重估价值
    gross_value     DECIMAL(15,2) DEFAULT 0,   -- 总值

    -- 折旧
    accum_depreciation DECIMAL(15,2) DEFAULT 0, -- 累计折旧
    net_book_value  DECIMAL(15,2) DEFAULT 0,   -- 净值
    depreciation_to_date DECIMAL(15,2) DEFAULT 0, -- 已提折旧

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),

    -- 评估
    useful_life_years INTEGER,                 -- 使用年限 (年)
    useful_life_periods INTEGER,               -- 使用年限 (期间)
    salvage_value   DECIMAL(15,2) DEFAULT 0,   -- 残值
    salvage_percent DECIMAL(5,2) DEFAULT 5,    -- 残值率%

    -- 折旧范围标识
    depreciation_key VARCHAR(4),               -- 折旧码

    -- 状态
    asset_status    VARCHAR(2) DEFAULT '01',   -- 01:在用 02:闲置 03:报废 04:处置
    is_active       BOOLEAN DEFAULT TRUE,
    is_fully_depreciated BOOLEAN DEFAULT FALSE,

    -- 关联资产
    parent_asset_id UUID REFERENCES am_asset(id), -- 父资产

    -- 文本
    notes           TEXT,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, asset_number, sub_number)
);
```

### 3.2 资产折旧范围 (am_depreciation_area)

对标 SAP ANLC

```sql
CREATE TABLE am_depreciation_area (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    asset_id        UUID NOT NULL REFERENCES am_asset(id) ON DELETE CASCADE,

    -- 折旧范围
    depr_area       VARCHAR(2) NOT NULL,       -- 折旧范围
    -- 01:账面折旧 02:税法折旧 03:集团折旧

    -- 折旧方法
    depr_method     VARCHAR(2) NOT NULL,       -- 折旧方法
    -- SL:直线法 DR:双倍余额递减法 SY:年数总和法

    -- 使用年限
    useful_life_years INTEGER,                 -- 使用年限
    useful_life_periods INTEGER,               -- 期间数

    -- 原值
    acquisition_value DECIMAL(15,2) DEFAULT 0, -- 购置价值

    -- 累计折旧
    accum_depreciation DECIMAL(15,2) DEFAULT 0, -- 累计折旧
    depr_this_year  DECIMAL(15,2) DEFAULT 0,   -- 本年折旧

    -- 净值
    net_book_value  DECIMAL(15,2) DEFAULT 0,   -- 账面净值

    -- 计划折旧 (按年度)
    planned_depr_01 DECIMAL(15,2),             -- 第1年计划折旧
    planned_depr_02 DECIMAL(15,2),             -- 第2年计划折旧
    -- ... 可根据需要扩展

    -- 残值
    salvage_value   DECIMAL(15,2),             -- 残值

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 已折旧期间
    periods_depreciated INTEGER DEFAULT 0,     -- 已折旧期间数

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (asset_id, depr_area)
);
```

---

## 4. 资产业务

### 4.1 资产业务凭证 (am_transaction)

对标 SAP ANEP

```sql
CREATE TABLE am_transaction (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 凭证信息
    transaction_number VARCHAR(12) NOT NULL,   -- 交易号
    asset_id        UUID NOT NULL REFERENCES am_asset(id),
    asset_number    VARCHAR(12),

    -- 交易类型
    transaction_type VARCHAR(3) NOT NULL,      -- 交易类型
    -- 100:购置 110:发票校验 200:转账 300:报废 310:出售
    -- 400:折旧 500:重估 600:在建工程转固

    -- 日期
    document_date   DATE NOT NULL,
    posting_date    DATE NOT NULL,
    value_date      DATE,                      -- 价值日期

    -- 金额
    amount          DECIMAL(15,2) NOT NULL,    -- 交易金额
    currency_id     UUID REFERENCES core_currency(id),

    -- 原值变动
    acquisition_change DECIMAL(15,2) DEFAULT 0, -- 原值变动

    -- 折旧变动
    depreciation_change DECIMAL(15,2) DEFAULT 0, -- 折旧变动
    revaluation_change DECIMAL(15,2) DEFAULT 0, -- 重估变动

    -- 净值变动
    nbv_change      DECIMAL(15,2) DEFAULT 0,   -- 净值变动

    -- 折旧范围
    depr_area       VARCHAR(2),

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    -- FI:财务发票 MM:采购订单 MA:手工
    source_document VARCHAR(20),               -- 来源单据
    fi_document_id  UUID,                      -- FI凭证ID

    -- 文本
    description     VARCHAR(100),
    long_text       TEXT,

    -- 冲销
    is_reversed     BOOLEAN DEFAULT FALSE,
    reversed_transaction_id UUID,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, transaction_number)
);
```

**交易类型枚举**:

| 代码 | 类型 | 借/贷方向 |
|------|------|----------|
| 100 | 外部购置 | 借:资产 / 贷:应付 |
| 110 | 发票校验 | 借:资产 / 贷:应付 |
| 120 | 自建资产 | 借:资产 / 贷:在建工程 |
| 200 | 资产转移 | 借/贷:资产 |
| 300 | 报废 | 借:累计折旧 / 贷:资产 |
| 310 | 出售 | 借:累计折旧/现金 / 贷:资产 |
| 400 | 折旧 | 借:费用 / 贷:累计折旧 |
| 500 | 重估 | 借/贷:资产 / 贷/借:重估储备 |

---

## 5. 折旧计算

### 5.1 折旧方法配置 (am_depreciation_method)

```sql
CREATE TABLE am_depreciation_method (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 方法标识
    method_code     VARCHAR(2) NOT NULL,       -- 方法代码
    method_name     VARCHAR(100) NOT NULL,     -- 方法名称

    -- 算法类型
    algorithm_type  VARCHAR(2) NOT NULL,       -- 算法类型
    -- SL:直线法 DR:双倍余额递减 SY:年数总和法
    -- DI:定率递减 PR:生产量法

    -- 参数
    factor          DECIMAL(5,2) DEFAULT 1,    -- 系数 (如双倍为2)
    base_method     VARCHAR(2),                -- 基础方法

    -- 控制参数
    switch_to_sl    BOOLEAN DEFAULT FALSE,     -- 转换为直线法
    half_year_convention BOOLEAN DEFAULT FALSE, -- 半年惯例

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, method_code)
);
```

**折旧方法说明**:

| 方法 | 代码 | 公式 | 说明 |
|------|------|------|------|
| 直线法 | SL | (原值-残值)/年限 | 每年折旧额相等 |
| 双倍余额递减 | DR | 2/年限 * 净值 | 前期折旧多 |
| 年数总和法 | SY | (原值-残值)*剩余年限/年数总和 | 递减 |
| 定率递减 | DI | 固定比率 * 净值 | 指定折旧率 |

### 5.2 折旧运行 (am_depreciation_run)

```sql
CREATE TABLE am_depreciation_run (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 运行信息
    run_number      VARCHAR(10) NOT NULL,      -- 运行号
    run_type        VARCHAR(2) NOT NULL,       -- 运行类型
    -- 01:计划折旧 02:实际折旧 03:重复运行

    -- 范围
    company_id      UUID REFERENCES sys_company(id),
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,          -- 期间

    -- 运行时间
    run_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,

    -- 统计
    assets_processed INTEGER DEFAULT 0,        -- 处理资产数
    total_depreciation DECIMAL(15,2) DEFAULT 0, -- 折旧总额

    -- 过账
    is_posted       BOOLEAN DEFAULT FALSE,
    posted_at       TIMESTAMP,
    posted_by       UUID,
    fi_document_id  UUID,                      -- FI凭证ID

    -- 状态
    run_status      VARCHAR(2) DEFAULT '01',   -- 01:运行中 02:完成 03:已过账 04:错误

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, run_number)
);
```

### 5.3 折旧明细 (am_depreciation_detail)

```sql
CREATE TABLE am_depreciation_detail (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 运行
    run_id          UUID REFERENCES am_depreciation_run(id),

    -- 资产
    asset_id        UUID NOT NULL REFERENCES am_asset(id),
    asset_number    VARCHAR(12),

    -- 期间
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,

    -- 折旧范围
    depr_area       VARCHAR(2) NOT NULL,

    -- 折旧计算
    gross_value     DECIMAL(15,2),             -- 期初原值
    net_book_value  DECIMAL(15,2),             -- 期初净值
    depreciation_amount DECIMAL(15,2) NOT NULL, -- 本期折旧

    -- 累计
    accum_depreciation DECIMAL(15,2),          -- 累计折旧
    nbv_after       DECIMAL(15,2),             -- 期末净值

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 交易
    transaction_id  UUID REFERENCES am_transaction(id),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (run_id, asset_id, depr_area)
);
```

---

## 6. 存储过程

### 6.1 计算折旧

```sql
CREATE OR REPLACE FUNCTION am_calculate_depreciation(
    p_asset_id UUID,
    p_depr_area VARCHAR,
    p_fiscal_year INTEGER,
    p_period INTEGER
) RETURNS DECIMAL AS $$
DECLARE
    v_asset RECORD;
    v_depr_area RECORD;
    v_method RECORD;
    v_depreciation DECIMAL(15,2);
    v_remaining_life INTEGER;
    v_depreciable_base DECIMAL(15,2);
BEGIN
    -- 获取资产信息
    SELECT * INTO v_asset FROM am_asset WHERE id = p_asset_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION '资产不存在';
    END IF;

    -- 获取折旧范围
    SELECT * INTO v_depr_area
    FROM am_depreciation_area
    WHERE asset_id = p_asset_id AND depr_area = p_depr_area;
    IF NOT FOUND THEN
        RAISE EXCEPTION '折旧范围不存在';
    END IF;

    -- 获取折旧方法
    SELECT * INTO v_method
    FROM am_depreciation_method
    WHERE method_code = v_depr_area.depr_method OR method_code = 'SL';

    -- 计算折旧基数
    v_depreciable_base := v_depr_area.acquisition_value - COALESCE(v_depr_area.salvage_value, 0);

    -- 根据方法计算折旧
    CASE v_method.algorithm_type
        WHEN 'SL' THEN
            -- 直线法: (原值-残值)/使用年限/12
            v_depreciation := v_depreciable_base / v_depr_area.useful_life_years / 12;

        WHEN 'DR' THEN
            -- 双倍余额递减法: 2/年限/12 * 净值
            v_depreciation := (v_method.factor / v_depr_area.useful_life_years / 12) * v_depr_area.net_book_value;

        WHEN 'SY' THEN
            -- 年数总和法
            v_remaining_life := v_depr_area.useful_life_years - (v_depr_area.periods_depreciated / 12);
            v_depreciation := v_depreciable_base * v_remaining_life / (v_depr_area.useful_life_years * (v_depr_area.useful_life_years + 1) / 2) / 12;

        ELSE
            -- 默认直线法
            v_depreciation := v_depreciable_base / v_depr_area.useful_life_years / 12;
    END CASE;

    -- 四舍五入到2位小数
    v_depreciation := ROUND(v_depreciation, 2);

    -- 不超过净值
    IF v_depreciation > v_depr_area.net_book_value THEN
        v_depreciation := v_depr_area.net_book_value;
    END IF;

    RETURN v_depreciation;
END;
$$ LANGUAGE plpgsql;
```

### 6.2 执行折旧过账

```sql
CREATE OR REPLACE FUNCTION am_post_depreciation(
    p_tenant_id UUID,
    p_company_id UUID,
    p_fiscal_year INTEGER,
    p_period INTEGER,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_run_id UUID;
    v_run_number VARCHAR(10);
    v_asset RECORD;
    v_depreciation DECIMAL(15,2);
    v_transaction_id UUID;
    v_total_depreciation DECIMAL(15,2) := 0;
    v_assets_processed INTEGER := 0;
BEGIN
    -- 生成运行号
    v_run_number := generate_business_code(p_tenant_id, 'DP', NULL, NULL);

    -- 创建折旧运行记录
    INSERT INTO am_depreciation_run (
        tenant_id, run_number, run_type,
        company_id, fiscal_year, period,
        created_by
    ) VALUES (
        p_tenant_id, v_run_number, '02',
        p_company_id, p_fiscal_year, p_period,
        p_user_id
    ) RETURNING id INTO v_run_id;

    -- 遍历所有需要折旧的资产
    FOR v_asset IN
        SELECT a.* FROM am_asset a
        JOIN am_depreciation_area da ON da.asset_id = a.id
        WHERE a.tenant_id = p_tenant_id
          AND a.company_id = p_company_id
          AND a.is_active = TRUE
          AND a.asset_status = '01'
          AND a.is_fully_depreciated = FALSE
          AND da.depr_area = '01'
    LOOP
        -- 计算折旧
        v_depreciation := am_calculate_depreciation(
            v_asset.id, '01', p_fiscal_year, p_period
        );

        IF v_depreciation > 0 THEN
            -- 创建折旧交易
            INSERT INTO am_transaction (
                tenant_id, transaction_number, asset_id, asset_number,
                transaction_type, document_date, posting_date,
                amount, currency_id,
                depreciation_change, depr_area,
                description, created_by
            ) VALUES (
                p_tenant_id,
                generate_business_code(p_tenant_id, 'AT', NULL, NULL),
                v_asset.id, v_asset.asset_number,
                '400', CURRENT_DATE, CURRENT_DATE,
                v_depreciation, v_asset.currency_id,
                v_depreciation, '01',
                '折旧 - ' || p_fiscal_year || '/' || p_period, p_user_id
            ) RETURNING id INTO v_transaction_id;

            -- 更新资产折旧
            UPDATE am_asset SET
                accum_depreciation = accum_depreciation + v_depreciation,
                net_book_value = net_book_value - v_depreciation,
                depreciation_to_date = depreciation_to_date + v_depreciation,
                is_fully_depreciated = (net_book_value - v_depreciation <= salvage_value),
                updated_at = CURRENT_TIMESTAMP
            WHERE id = v_asset.id;

            -- 更新折旧范围
            UPDATE am_depreciation_area SET
                accum_depreciation = accum_depreciation + v_depreciation,
                depr_this_year = depr_this_year + v_depreciation,
                net_book_value = net_book_value - v_depreciation,
                periods_depreciated = periods_depreciated + 1,
                updated_at = CURRENT_TIMESTAMP
            WHERE asset_id = v_asset.id AND depr_area = '01';

            -- 创建折旧明细
            INSERT INTO am_depreciation_detail (
                tenant_id, run_id, asset_id, asset_number,
                fiscal_year, period, depr_area,
                depreciation_amount, currency_id, transaction_id
            ) VALUES (
                p_tenant_id, v_run_id, v_asset.id, v_asset.asset_number,
                p_fiscal_year, p_period, '01',
                v_depreciation, v_asset.currency_id, v_transaction_id
            );

            v_total_depreciation := v_total_depreciation + v_depreciation;
            v_assets_processed := v_assets_processed + 1;
        END IF;
    END LOOP;

    -- 更新运行统计
    UPDATE am_depreciation_run
    SET assets_processed = v_assets_processed,
        total_depreciation = v_total_depreciation,
        run_status = '02',
        end_time = CURRENT_TIMESTAMP
    WHERE id = v_run_id;

    RETURN v_run_id;
END;
$$ LANGUAGE plpgsql;
```

---

## 7. 视图定义

### 7.1 资产清单视图

```sql
CREATE VIEW v_am_asset_list AS
SELECT
    a.asset_code,
    a.description,
    c.class_code,
    c.class_name AS asset_class,
    a.company_code,
    a.acquisition_value,
    a.accum_depreciation,
    a.net_book_value,
    a.salvage_value,
    a.useful_life_years,
    a.capitalization_date,
    a.asset_status,
    cc.cost_center_code,
    cc.cost_center_name,
    p.plant_code,
    p.plant_name,
    e.full_name AS responsible_person_name

FROM am_asset a
LEFT JOIN am_asset_class c ON c.id = a.asset_class_id
LEFT JOIN sys_cost_center cc ON cc.id = a.cost_center_id
LEFT JOIN sys_plant p ON p.id = a.plant_id
LEFT JOIN hr_employee e ON e.id = a.responsible_person
WHERE a.is_active = TRUE;
```

### 7.2 资产折旧汇总视图

```sql
CREATE VIEW v_am_depreciation_summary AS
SELECT
    a.asset_class_code,
    c.class_name,
    COUNT(*) AS asset_count,
    SUM(a.acquisition_value) AS total_acquisition,
    SUM(a.accum_depreciation) AS total_depreciation,
    SUM(a.net_book_value) AS total_nbv,
    SUM(a.salvage_value) AS total_salvage,
    ROUND(SUM(a.accum_depreciation) / NULLIF(SUM(a.acquisition_value), 0) * 100, 2) AS depreciation_pct

FROM am_asset a
JOIN am_asset_class c ON c.id = a.asset_class_id
WHERE a.is_active = TRUE
GROUP BY a.asset_class_code, c.class_name
ORDER BY a.asset_class_code;
```

---

## 8. 索引策略

```sql
-- 资产主数据
CREATE INDEX idx_am_asset_number ON am_asset (tenant_id, asset_number);
CREATE INDEX idx_am_asset_class ON am_asset (asset_class_id);
CREATE INDEX idx_am_asset_company ON am_asset (company_id);
CREATE INDEX idx_am_asset_cost_center ON am_asset (cost_center_id);
CREATE INDEX idx_am_asset_status ON am_asset (asset_status);
CREATE INDEX idx_am_asset_cap_date ON am_asset (capitalization_date);

-- 资产业务
CREATE INDEX idx_am_trans_asset ON am_transaction (asset_id);
CREATE INDEX idx_am_trans_type ON am_transaction (transaction_type);
CREATE INDEX idx_am_trans_date ON am_transaction (posting_date);

-- 折旧范围
CREATE INDEX idx_am_depr_area_asset ON am_depreciation_area (asset_id);

-- 折旧运行
CREATE INDEX idx_am_depr_run_company ON am_depreciation_run (company_id, fiscal_year, period);
CREATE INDEX idx_am_depr_detail_run ON am_depreciation_detail (run_id);
CREATE INDEX idx_am_depr_detail_asset ON am_depreciation_detail (asset_id, fiscal_year, period);
```

---

## 9. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
