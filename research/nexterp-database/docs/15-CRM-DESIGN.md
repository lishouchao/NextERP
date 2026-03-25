# CRM 模块数据库设计

**模块**: Customer Relationship Management (客户关系管理)
**对标**: SAP CRM / Salesforce
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | 功能 |
|--------|------|------|
| 线索管理 | 销售线索 | 线索获取、评分、转化 |
| 商机管理 | 销售机会 | 商机阶段、预测、赢单 |
| 客户管理 | 客户360° | 客户视图、交互记录 |
| 活动管理 | 销售活动 | 任务、日程、跟进 |
| 销售管道 | 管道分析 | 阶段转化、预测 |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     CRM Module Architecture                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    销售管道                              │    │
│  │                                                          │    │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │    │
│  │  │  线索   │─►│  商机   │─►│  报价   │─►│  订单   │    │    │
│  │  │crm_lead │  │crm_     │  │crm_quote│  │ sd_so   │    │    │
│  │  │         │  │opportunity│ │         │  │         │    │    │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘    │    │
│  │       │            │            │            │          │    │
│  │       └────────────┴────────────┴────────────┘          │    │
│  │                          │                               │    │
│  └──────────────────────────┼───────────────────────────────┘    │
│                              │                                   │
│        ┌─────────────────────┼─────────────────────┐            │
│        ▼                     ▼                     ▼            │
│  ┌──────────┐          ┌──────────┐          ┌──────────┐      │
│  │ 客户360° │          │ 活动管理 │          │ 销售分析 │      │
│  │crm_360   │          │crm_      │          │crm_      │      │
│  │          │          │activity  │          │analytics │      │
│  └──────────┘          └──────────┘          └──────────┘      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 CRM 与 ERP 集成

```
┌─────────────────────────────────────────────────────────────────┐
│                     CRM & ERP Integration                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   CRM 模块                         ERP 模块                      │
│  ┌─────────────┐                   ┌─────────────┐              │
│  │ 线索/商机    │                   │ SD 销售订单  │              │
│  │ Leads/Opp   │─────────────────►│ Sales Order │              │
│  └─────────────┘                   └─────────────┘              │
│        │                                  │                      │
│        ▼                                  ▼                      │
│  ┌─────────────┐                   ┌─────────────┐              │
│  │ 客户主数据   │◄────────────────►│ BP 业务伙伴  │              │
│  │crm_customer │      共享         │ BP_Customer │              │
│  └─────────────┘                   └─────────────┘              │
│        │                                  │                      │
│        ▼                                  ▼                      │
│  ┌─────────────┐                   ┌─────────────┐              │
│  │ 报价单      │                   │ 开票/收款   │              │
│  │ crm_quote   │─────────────────►│ FI/SD       │              │
│  └─────────────┘                   └─────────────┘              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 线索管理

### 2.1 线索主表 (crm_lead)

```sql
CREATE TABLE crm_lead (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 线索信息
    lead_number     VARCHAR(12) NOT NULL,      -- 线索号
    lead_source     VARCHAR(2),                -- 线索来源
    -- 01:网站 02:展会 03:电话 04:邮件 05:推荐 06:广告 07:社交媒体 08:合作伙伴 09:内部推荐 10:其他

    -- 联系人信息
    first_name      VARCHAR(40),
    last_name       VARCHAR(40),
    full_name       VARCHAR(80) GENERATED ALWAYS AS (
        COALESCE(first_name, '') || ' ' || COALESCE(last_name, '')
    ) STORED,
    title           VARCHAR(30),               -- 职位
    email           VARCHAR(100),
    phone           VARCHAR(50),
    mobile          VARCHAR(50),

    -- 公司信息
    company_name    VARCHAR(100),              -- 公司名称
    industry        VARCHAR(4),                -- 行业
    company_size    VARCHAR(2),                -- 公司规模
    -- 01:1-50 02:51-200 03:201-500 04:501-1000 05:1000+
    website         VARCHAR(100),

    -- 地址
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city            VARCHAR(40),

    -- 线索评分
    lead_score      INTEGER DEFAULT 0,         -- 线索评分 (0-100)
    lead_grade      VARCHAR(1),                -- 线索等级 (A/B/C/D)
    lead_status     VARCHAR(2) DEFAULT '01',   -- 线索状态
    -- 01:新建 02:已联系 03:合格 04:不合格 05:已转化 06:已关闭

    -- 产品兴趣
    product_interest VARCHAR(100),             -- 感兴趣的产品

    -- 需求描述
    requirements    TEXT,                      -- 需求说明
    budget          VARCHAR(2),                -- 预算范围
    -- 01:<10万 02:10-50万 03:50-100万 04:100-500万 05:>500万

    -- 负责人
    owner_id        UUID REFERENCES hr_employee(id), -- 负责人
    team_id         UUID REFERENCES crm_sales_team(id), -- 销售团队

    -- 转化信息
    converted       BOOLEAN DEFAULT FALSE,
    converted_date  TIMESTAMP,
    converted_to_customer_id UUID REFERENCES bp_business_partner(id),
    converted_to_opportunity_id UUID REFERENCES crm_opportunity(id),

    -- 营销活动
    campaign_id     UUID REFERENCES crm_campaign(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    last_contact_date DATE,                    -- 最后联系日期

    UNIQUE (tenant_id, lead_number)
);
```

### 2.2 线索评分规则 (crm_lead_scoring_rule)

```sql
CREATE TABLE crm_lead_scoring_rule (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 规则信息
    rule_name       VARCHAR(100) NOT NULL,
    description     TEXT,

    -- 条件
    field_name      VARCHAR(50) NOT NULL,      -- 字段名
    operator        VARCHAR(2) NOT NULL,       -- 操作符
    -- EQ:等于 NE:不等于 IN:包含 GT:大于 LT:小于
    field_value     VARCHAR(200),              -- 字段值

    -- 分值
    score           INTEGER NOT NULL,          -- 得分

    -- 优先级
    priority        INTEGER DEFAULT 10,

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 3. 商机管理

### 3.1 商机主表 (crm_opportunity)

```sql
CREATE TABLE crm_opportunity (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 商机信息
    opportunity_number VARCHAR(12) NOT NULL,   -- 商机号
    opportunity_name VARCHAR(100) NOT NULL,    -- 商机名称
    description     TEXT,

    -- 客户
    customer_id     UUID NOT NULL REFERENCES bp_business_partner(id),
    customer_name   VARCHAR(100),
    contact_id      UUID REFERENCES crm_contact(id), -- 主要联系人

    -- 商机阶段
    stage           VARCHAR(2) NOT NULL,       -- 商机阶段
    -- 01:初步接触 02:需求确认 03:方案演示 04:商务谈判 05:合同审批 06:赢单 07:输单
    probability     INTEGER DEFAULT 10,        -- 成功概率%
    is_closed       BOOLEAN DEFAULT FALSE,
    is_won          BOOLEAN,
    close_date      DATE,                      -- 预计成交日期

    -- 金额
    amount          DECIMAL(15,2) NOT NULL,    -- 商机金额
    currency_id     UUID REFERENCES core_currency(id),
    expected_revenue DECIMAL(15,2) GENERATED ALWAYS AS (
        amount * probability / 100
    ) STORED,                                   -- 加权收入

    -- 来源
    lead_id         UUID REFERENCES crm_lead(id),
    campaign_id     UUID REFERENCES crm_campaign(id),
    source_type     VARCHAR(2),

    -- 产品
    product_interest VARCHAR(200),             -- 产品兴趣

    -- 负责人
    owner_id        UUID NOT NULL REFERENCES hr_employee(id),
    team_id         UUID REFERENCES crm_sales_team(id),

    -- 组织
    company_id      UUID REFERENCES sys_company(id),
    sales_org_id    UUID REFERENCES sys_sales_org(id),

    -- 竞争对手
    competitor_id   UUID REFERENCES crm_competitor(id),
    competition_status VARCHAR(2),             -- 竞争状态

    -- 输单原因
    loss_reason     VARCHAR(4),                -- 输单原因代码
    loss_remark     TEXT,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, opportunity_number)
);
```

### 3.2 商机产品 (crm_opportunity_product)

```sql
CREATE TABLE crm_opportunity_product (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 商机
    opportunity_id  UUID NOT NULL REFERENCES crm_opportunity(id) ON DELETE CASCADE,

    -- 产品
    product_id      UUID REFERENCES mm_material(id),
    product_code    VARCHAR(18),
    product_name    VARCHAR(100),

    -- 数量
    quantity        DECIMAL(13,3) NOT NULL,
    unit            VARCHAR(3),

    -- 价格
    unit_price      DECIMAL(15,2) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    total_price     DECIMAL(15,2) NOT NULL,

    -- 描述
    description     VARCHAR(200),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (opportunity_id, product_id)
);
```

### 3.3 商机阶段历史 (crm_opportunity_stage_history)

```sql
CREATE TABLE crm_opportunity_stage_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 商机
    opportunity_id  UUID NOT NULL REFERENCES crm_opportunity(id),

    -- 阶段
    from_stage      VARCHAR(2),
    to_stage        VARCHAR(2) NOT NULL,

    -- 金额变化
    old_amount      DECIMAL(15,2),
    new_amount      DECIMAL(15,2),

    -- 概率变化
    old_probability INTEGER,
    new_probability INTEGER,

    -- 日期
    change_date     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 原因
    change_reason   TEXT,

    -- 操作人
    changed_by      UUID,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 4. 客户360°

### 4.1 联系人 (crm_contact)

```sql
CREATE TABLE crm_contact (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 基本信息
    first_name      VARCHAR(40),
    last_name       VARCHAR(40),
    full_name       VARCHAR(80) GENERATED ALWAYS AS (
        COALESCE(first_name, '') || ' ' || COALESCE(last_name, '')
    ) STORED,
    title           VARCHAR(30),
    department      VARCHAR(50),

    -- 客户
    customer_id     UUID NOT NULL REFERENCES bp_business_partner(id),

    -- 联系方式
    email           VARCHAR(100),
    email_2         VARCHAR(100),
    phone           VARCHAR(50),
    mobile          VARCHAR(50),
    fax             VARCHAR(50),

    -- 地址
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city            VARCHAR(40),
    street          VARCHAR(60),
    postal_code     VARCHAR(10),

    -- 社交媒体
    linkedin        VARCHAR(100),
    wechat          VARCHAR(50),
    weibo           VARCHAR(50),

    -- 角色
    is_primary      BOOLEAN DEFAULT FALSE,     -- 主要联系人
    is_decision_maker BOOLEAN DEFAULT FALSE,   -- 决策者
    is_influencer   BOOLEAN DEFAULT FALSE,     -- 影响者

    -- 生日
    birthday        DATE,

    -- 备注
    notes           TEXT,

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 客户交互记录 (crm_interaction)

```sql
CREATE TABLE crm_interaction (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 客户/联系人
    customer_id     UUID REFERENCES bp_business_partner(id),
    contact_id      UUID REFERENCES crm_contact(id),
    opportunity_id  UUID REFERENCES crm_opportunity(id),
    lead_id         UUID REFERENCES crm_lead(id),

    -- 交互类型
    interaction_type VARCHAR(2) NOT NULL,      -- 交互类型
    -- 01:电话 02:邮件 03:会议 04:拜访 05:微信 06:其他

    -- 方向
    direction       VARCHAR(1),                -- 方向
    -- I:入站 O:出站

    -- 内容
    subject         VARCHAR(200) NOT NULL,
    content         TEXT,

    -- 日期时间
    interaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration        INTEGER,                   -- 持续时间(分钟)

    -- 关联单据
    related_type    VARCHAR(2),                -- 关联类型
    related_document VARCHAR(20),

    -- 负责人
    owner_id        UUID REFERENCES hr_employee(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID
);
```

### 4.3 客户摘要视图 (crm_customer_summary)

```sql
CREATE TABLE crm_customer_summary (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 客户
    customer_id     UUID NOT NULL REFERENCES bp_business_partner(id),

    -- 概况
    first_contact_date DATE,                   -- 首次接触日期
    last_contact_date DATE,                    -- 最后接触日期
    total_interactions INTEGER DEFAULT 0,      -- 总交互次数

    -- 商机
    open_opportunities INTEGER DEFAULT 0,      -- 开放商机数
    won_opportunities INTEGER DEFAULT 0,       -- 赢单数
    lost_opportunities INTEGER DEFAULT 0,      -- 输单数
    total_pipeline_amount DECIMAL(15,2),       -- 总商机金额
    won_amount      DECIMAL(15,2),             -- 赢单金额

    -- 订单
    total_orders    INTEGER DEFAULT 0,         -- 总订单数
    total_revenue   DECIMAL(15,2) DEFAULT 0,   -- 总收入
    last_order_date DATE,                      -- 最后订单日期

    -- 客户等级
    customer_tier   VARCHAR(2),                -- 客户等级
    -- 01:钻石 02:白金 03:金 04:银 05:铜

    -- 客户生命周期
    lifecycle_stage VARCHAR(2),                -- 生命周期阶段
    -- 01:潜在 02:新客户 03:活跃 04:忠诚 05:流失风险 06:流失

    -- RFM分析
    recency_days    INTEGER,                   -- 最近购买距今天数
    frequency       INTEGER,                   -- 购买频次
    monetary        DECIMAL(15,2),             -- 消费金额
    rfm_score       INTEGER,                   -- RFM得分

    -- 审计
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, customer_id)
);
```

---

## 5. 活动管理

### 5.1 任务 (crm_task)

```sql
CREATE TABLE crm_task (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 任务信息
    subject         VARCHAR(200) NOT NULL,
    description     TEXT,

    -- 任务类型
    task_type       VARCHAR(2),                -- 任务类型
    -- 01:电话 02:邮件 03:会议 04:拜访 05:演示 06:跟进 07:其他

    -- 关联对象
    related_type    VARCHAR(2),                -- 关联类型
    -- LE:线索 OP:商机 CU:客户 CO:联系人
    related_id      UUID,
    related_name    VARCHAR(100),

    -- 优先级
    priority        VARCHAR(2) DEFAULT '03',   -- 01:高 02:中 03:低

    -- 日期
    due_date        DATE NOT NULL,
    completed_date  DATE,

    -- 状态
    task_status     VARCHAR(2) DEFAULT '01',   -- 01:未开始 02:进行中 03:已完成 04:已取消
    is_completed    BOOLEAN DEFAULT FALSE,

    -- 负责人
    owner_id        UUID NOT NULL REFERENCES hr_employee(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID
);
```

### 5.2 日程 (crm_calendar)

```sql
CREATE TABLE crm_calendar (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 日程信息
    subject         VARCHAR(200) NOT NULL,
    description     TEXT,
    location        VARCHAR(200),

    -- 类型
    event_type      VARCHAR(2),                -- 日程类型
    -- 01:会议 02:拜访 03:演示 04:培训 05:其他

    -- 时间
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    is_all_day      BOOLEAN DEFAULT FALSE,

    -- 重复
    recurrence_type VARCHAR(1),                -- 重复类型
    -- N:不重复 D:每日 W:每周 M:每月 Y:每年
    recurrence_end  DATE,

    -- 关联对象
    related_type    VARCHAR(2),
    related_id      UUID,

    -- 参与人
    owner_id        UUID NOT NULL REFERENCES hr_employee(id),

    -- 提醒
    reminder_minutes INTEGER,                  -- 提前提醒分钟数

    -- 状态
    event_status    VARCHAR(2) DEFAULT '01',   -- 01:计划 02:已完成 03:已取消

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID
);
```

---

## 6. 销售分析

### 6.1 销售管道 (crm_pipeline)

```sql
CREATE TABLE crm_pipeline (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 维度
    company_id      UUID REFERENCES sys_company(id),
    sales_org_id    UUID REFERENCES sys_sales_org(id),
    owner_id        UUID REFERENCES hr_employee(id),
    team_id         UUID REFERENCES crm_sales_team(id),
    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL,

    -- 阶段金额
    stage_01_amount DECIMAL(15,2) DEFAULT 0,   -- 初步接触
    stage_02_amount DECIMAL(15,2) DEFAULT 0,   -- 需求确认
    stage_03_amount DECIMAL(15,2) DEFAULT 0,   -- 方案演示
    stage_04_amount DECIMAL(15,2) DEFAULT 0,   -- 商务谈判
    stage_05_amount DECIMAL(15,2) DEFAULT 0,   -- 合同审批

    -- 加权金额
    weighted_amount DECIMAL(15,2) DEFAULT 0,

    -- 赢单
    won_amount      DECIMAL(15,2) DEFAULT 0,
    won_count       INTEGER DEFAULT 0,

    -- 输单
    lost_amount     DECIMAL(15,2) DEFAULT 0,
    lost_count      INTEGER DEFAULT 0,

    -- 商机数
    opportunity_count INTEGER DEFAULT 0,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, sales_org_id, owner_id, period_year, period_month)
);
```

### 6.2 销售配额 (crm_sales_quota)

```sql
CREATE TABLE crm_sales_quota (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 维度
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    sales_org_id    UUID REFERENCES sys_sales_org(id),
    owner_id        UUID NOT NULL REFERENCES hr_employee(id),
    team_id         UUID REFERENCES crm_sales_team(id),

    -- 期间
    quota_year      INTEGER NOT NULL,
    quota_month     INTEGER,
    quarter         INTEGER,

    -- 配额
    quota_amount    DECIMAL(15,2) NOT NULL,    -- 配额金额
    quota_type      VARCHAR(2),                -- 配额类型
    -- 01:新客户 02:现有客户 03:产品 04:总计

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    quota_status    VARCHAR(2) DEFAULT '01',   -- 01:草稿 02:已审批

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by     UUID,
    approved_at     TIMESTAMP,

    UNIQUE (company_id, owner_id, quota_year, quota_month, quota_type)
);
```

---

## 7. 营销管理

### 7.1 营销活动 (crm_campaign)

```sql
CREATE TABLE crm_campaign (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 活动信息
    campaign_number VARCHAR(10) NOT NULL,
    campaign_name   VARCHAR(100) NOT NULL,
    description     TEXT,

    -- 类型
    campaign_type   VARCHAR(2),                -- 活动类型
    -- 01:展会 02:网络营销 03:邮件营销 04:电话营销 05:社交媒体

    -- 状态
    campaign_status VARCHAR(2) DEFAULT '01',   -- 01:计划 02:进行中 03:已完成 04:取消

    -- 日期
    start_date      DATE,
    end_date        DATE,

    -- 预算
    budget          DECIMAL(15,2),
    actual_cost     DECIMAL(15,2) DEFAULT 0,
    currency_id     UUID REFERENCES core_currency(id),

    -- 目标
    target_leads    INTEGER,                   -- 目标线索数
    target_opportunities INTEGER,              -- 目标商机数
    target_revenue  DECIMAL(15,2),             -- 目标收入

    -- 实际
    actual_leads    INTEGER DEFAULT 0,         -- 实际线索数
    actual_opportunities INTEGER DEFAULT 0,    -- 实际商机数
    actual_revenue  DECIMAL(15,2) DEFAULT 0,   -- 实际收入

    -- 负责人
    owner_id        UUID REFERENCES hr_employee(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, campaign_number)
);
```

### 7.2 竞争对手 (crm_competitor)

```sql
CREATE TABLE crm_competitor (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 基本信息
    competitor_name VARCHAR(100) NOT NULL,
    website         VARCHAR(100),

    -- 实力分析
    strengths       TEXT,                      -- 优势
    weaknesses      TEXT,                      -- 劣势

    -- 市场信息
    market_share    DECIMAL(5,2),              -- 市场份额%
    revenue         DECIMAL(15,2),             -- 营收

    -- 产品对比
    product_comparison TEXT,

    -- 威胁等级
    threat_level    VARCHAR(2),                -- 威胁等级
    -- 01:高 02:中 03:低

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 8. 销售团队

### 8.1 销售团队 (crm_sales_team)

```sql
CREATE TABLE crm_sales_team (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 团队信息
    team_code       VARCHAR(10) NOT NULL,
    team_name       VARCHAR(100) NOT NULL,
    description     TEXT,

    -- 组织
    company_id      UUID REFERENCES sys_company(id),
    sales_org_id    UUID REFERENCES sys_sales_org(id),
    parent_team_id  UUID REFERENCES crm_sales_team(id),

    -- 负责人
    manager_id      UUID REFERENCES hr_employee(id),

    -- 地区
    region_id       UUID REFERENCES core_region(id),

    -- 配额
    team_quota      DECIMAL(15,2),

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, team_code)
);
```

### 8.2 销售团队成员 (crm_sales_team_member)

```sql
CREATE TABLE crm_sales_team_member (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 团队
    team_id         UUID NOT NULL REFERENCES crm_sales_team(id) ON DELETE CASCADE,

    -- 成员
    employee_id     UUID NOT NULL REFERENCES hr_employee(id),

    -- 角色
    member_role     VARCHAR(2),                -- 成员角色
    -- 01:经理 02:销售代表 03:售前 04:销售支持

    -- 加入日期
    join_date       DATE DEFAULT CURRENT_DATE,
    leave_date      DATE,

    -- 配额
    quota           DECIMAL(15,2),

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (team_id, employee_id)
);
```

---

## 9. 视图定义

### 9.1 销售管道分析视图

```sql
CREATE VIEW v_crm_pipeline_analysis AS
SELECT
    o.tenant_id,
    o.company_id,
    c.company_code,
    o.sales_org_id,
    so.sales_org_code,
    o.owner_id,
    emp.full_name AS owner_name,
    t.team_code,
    t.team_name,

    COUNT(*) AS opportunity_count,
    SUM(o.amount) AS total_amount,
    SUM(o.expected_revenue) AS weighted_amount,

    SUM(CASE WHEN o.stage = '01' THEN o.amount ELSE 0 END) AS stage_01,
    SUM(CASE WHEN o.stage = '02' THEN o.amount ELSE 0 END) AS stage_02,
    SUM(CASE WHEN o.stage = '03' THEN o.amount ELSE 0 END) AS stage_03,
    SUM(CASE WHEN o.stage = '04' THEN o.amount ELSE 0 END) AS stage_04,
    SUM(CASE WHEN o.stage = '05' THEN o.amount ELSE 0 END) AS stage_05,
    SUM(CASE WHEN o.stage = '06' THEN o.amount ELSE 0 END) AS won_amount,
    SUM(CASE WHEN o.stage = '07' THEN o.amount ELSE 0 END) AS lost_amount

FROM crm_opportunity o
LEFT JOIN sys_company c ON c.id = o.company_id
LEFT JOIN sys_sales_org so ON so.id = o.sales_org_id
LEFT JOIN hr_employee emp ON emp.id = o.owner_id
LEFT JOIN crm_sales_team t ON t.id = o.team_id
WHERE o.is_closed = FALSE
GROUP BY o.tenant_id, o.company_id, c.company_code, o.sales_org_id, so.sales_org_code,
         o.owner_id, emp.full_name, t.team_code, t.team_name;
```

### 9.2 客户360°视图

```sql
CREATE VIEW v_crm_customer_360 AS
SELECT
    bp.id AS customer_id,
    bp.bp_number,
    bp.name AS customer_name,
    bp.bp_type,

    -- 联系人
    (SELECT COUNT(*) FROM crm_contact WHERE customer_id = bp.id) AS contact_count,

    -- 商机统计
    cs.open_opportunities,
    cs.won_opportunities,
    cs.total_pipeline_amount,
    cs.won_amount,

    -- 交互统计
    cs.total_interactions,
    cs.last_contact_date,

    -- 订单统计
    cs.total_orders,
    cs.total_revenue,
    cs.last_order_date,

    -- 客户等级
    cs.customer_tier,
    cs.lifecycle_stage,

    -- 负责人
    owner.emp.full_name AS owner_name

FROM bp_business_partner bp
LEFT JOIN crm_customer_summary cs ON cs.customer_id = bp.id
LEFT JOIN hr_employee owner ON owner.id = cs.customer_id
WHERE EXISTS (
    SELECT 1 FROM bp_role WHERE bp_id = bp.id AND role_type = 'ZCUSTOMER'
);
```

---

## 10. 存储过程

### 10.1 线索转化

```sql
CREATE OR REPLACE FUNCTION crm_convert_lead_to_opportunity(
    p_lead_id UUID,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_lead RECORD;
    v_opportunity_id UUID;
    v_customer_id UUID;
    v_opportunity_number VARCHAR(12);
BEGIN
    -- 获取线索信息
    SELECT * INTO v_lead FROM crm_lead WHERE id = p_lead_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '线索不存在';
    END IF;

    IF v_lead.converted THEN
        RAISE EXCEPTION '线索已转化';
    END IF;

    -- 检查或创建客户
    SELECT id INTO v_customer_id
    FROM bp_business_partner
    WHERE name = v_lead.company_name
    LIMIT 1;

    IF v_customer_id IS NULL THEN
        -- 创建客户 (调用BP模块函数)
        v_customer_id := bp_create_business_partner(
            v_lead.tenant_id,
            'O',
            v_lead.company_name,
            v_lead.company_name,
            NULL,
            p_user_id
        );

        -- 添加客户角色
        PERFORM bp_add_customer_role(v_customer_id, ...);
    END IF;

    -- 生成商机号
    v_opportunity_number := generate_business_code(v_lead.tenant_id, 'OP', NULL, NULL);

    -- 创建商机
    INSERT INTO crm_opportunity (
        tenant_id, opportunity_number, opportunity_name,
        customer_id, customer_name,
        stage, probability,
        amount, currency_id,
        lead_id, owner_id,
        created_by
    ) VALUES (
        v_lead.tenant_id, v_opportunity_number,
        v_lead.company_name || ' - 商机',
        v_customer_id, v_lead.company_name,
        '01', 10,
        0, NULL,
        p_lead_id, v_lead.owner_id,
        p_user_id
    ) RETURNING id INTO v_opportunity_id;

    -- 更新线索状态
    UPDATE crm_lead
    SET lead_status = '05',
        converted = TRUE,
        converted_date = CURRENT_TIMESTAMP,
        converted_to_customer_id = v_customer_id,
        converted_to_opportunity_id = v_opportunity_id,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_lead_id;

    RETURN v_opportunity_id;
END;
$$ LANGUAGE plpgsql;
```

### 10.2 更新客户摘要

```sql
CREATE OR REPLACE FUNCTION crm_update_customer_summary(
    p_customer_id UUID
) RETURNS VOID AS $$
DECLARE
    v_tenant_id UUID;
BEGIN
    SELECT tenant_id INTO v_tenant_id
    FROM bp_business_partner WHERE id = p_customer_id;

    INSERT INTO crm_customer_summary (
        tenant_id, customer_id,
        first_contact_date, last_contact_date, total_interactions,
        open_opportunities, won_opportunities, lost_opportunities,
        total_pipeline_amount, won_amount,
        total_orders, total_revenue, last_order_date
    )
    SELECT
        v_tenant_id, p_customer_id,
        (SELECT MIN(interaction_date) FROM crm_interaction WHERE customer_id = p_customer_id),
        (SELECT MAX(interaction_date) FROM crm_interaction WHERE customer_id = p_customer_id),
        (SELECT COUNT(*) FROM crm_interaction WHERE customer_id = p_customer_id),
        (SELECT COUNT(*) FROM crm_opportunity WHERE customer_id = p_customer_id AND is_closed = FALSE),
        (SELECT COUNT(*) FROM crm_opportunity WHERE customer_id = p_customer_id AND is_won = TRUE),
        (SELECT COUNT(*) FROM crm_opportunity WHERE customer_id = p_customer_id AND is_closed = TRUE AND is_won = FALSE),
        (SELECT COALESCE(SUM(amount), 0) FROM crm_opportunity WHERE customer_id = p_customer_id AND is_closed = FALSE),
        (SELECT COALESCE(SUM(amount), 0) FROM crm_opportunity WHERE customer_id = p_customer_id AND is_won = TRUE),
        (SELECT COUNT(*) FROM sd_sales_order_hdr WHERE sold_to_party = p_customer_id),
        (SELECT COALESCE(SUM(net_value), 0) FROM sd_sales_order_hdr WHERE sold_to_party = p_customer_id),
        (SELECT MAX(document_date) FROM sd_sales_order_hdr WHERE sold_to_party = p_customer_id)
    ON CONFLICT (tenant_id, customer_id) DO UPDATE SET
        first_contact_date = EXCLUDED.first_contact_date,
        last_contact_date = EXCLUDED.last_contact_date,
        total_interactions = EXCLUDED.total_interactions,
        open_opportunities = EXCLUDED.open_opportunities,
        won_opportunities = EXCLUDED.won_opportunities,
        lost_opportunities = EXCLUDED.lost_opportunities,
        total_pipeline_amount = EXCLUDED.total_pipeline_amount,
        won_amount = EXCLUDED.won_amount,
        total_orders = EXCLUDED.total_orders,
        total_revenue = EXCLUDED.total_revenue,
        last_order_date = EXCLUDED.last_order_date,
        updated_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;
```

---

## 11. 索引策略

```sql
-- 线索
CREATE INDEX idx_crm_lead_number ON crm_lead (tenant_id, lead_number);
CREATE INDEX idx_crm_lead_status ON crm_lead (lead_status);
CREATE INDEX idx_crm_lead_owner ON crm_lead (owner_id);
CREATE INDEX idx_crm_lead_source ON crm_lead (lead_source);

-- 商机
CREATE INDEX idx_crm_opp_number ON crm_opportunity (tenant_id, opportunity_number);
CREATE INDEX idx_crm_opp_customer ON crm_opportunity (customer_id);
CREATE INDEX idx_crm_opp_owner ON crm_opportunity (owner_id);
CREATE INDEX idx_crm_opp_stage ON crm_opportunity (stage);
CREATE INDEX idx_crm_opp_close_date ON crm_opportunity (close_date);

-- 联系人
CREATE INDEX idx_crm_contact_customer ON crm_contact (customer_id);

-- 交互
CREATE INDEX idx_crm_interaction_customer ON crm_interaction (customer_id);
CREATE INDEX idx_crm_interaction_date ON crm_interaction (interaction_date);

-- 任务
CREATE INDEX idx_crm_task_owner ON crm_task (owner_id);
CREATE INDEX idx_crm_task_due_date ON crm_task (due_date);
CREATE INDEX idx_crm_task_related ON crm_task (related_type, related_id);

-- 客户摘要
CREATE INDEX idx_crm_summary_customer ON crm_customer_summary (customer_id);

-- 销售管道
CREATE INDEX idx_crm_pipeline_period ON crm_pipeline (period_year, period_month);
CREATE INDEX idx_crm_pipeline_owner ON crm_pipeline (owner_id);

-- 营销活动
CREATE INDEX idx_crm_campaign_number ON crm_campaign (tenant_id, campaign_number);
CREATE INDEX idx_crm_campaign_dates ON crm_campaign (start_date, end_date);
```

---

## 12. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
