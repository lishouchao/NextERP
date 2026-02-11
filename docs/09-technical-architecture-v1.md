# NextERP 国产ERP技术架构方案

## 项目概述

**NextERP** 是一款面向中国企业的云原生ERP系统，采用SaaS多租户模式，基于现代化技术栈构建，旨在提供与国际一流ERP系统相媲美的功能，同时更好地适配中国企业业务场景和合规要求。

### 核心定位

- **国产自主可控**：全栈开源技术，无外部依赖风险
- **云原生架构**：基于Kubernetes，支持弹性伸缩和多云部署
- **SaaS多租户**：一套服务支撑多企业隔离
- **现代化体验**：响应式Web界面 + 移动端支持

---

## 技术架构总览

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              访问层 (Access Layer)                       │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │  Web Browser │  │  Mobile App  │  │  Third-party │  │   Open API   ││
│  │  (Vue3/React)│  │  (Flutter)   │  │   Systems    │  │  (REST/GraphQL)│
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
└─────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                              API网关层 (API Gateway)                     │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │   路由转发   │  │   鉴权认证   │  │   限流熔断   │  │   协议转换   ││
│  │   (Nginx)    │  │  (OAuth2/JWT)│  │  (Sentinel)  │  │ (gRPC/REST)  ││
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘
└─────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           微服务层 (Microservices)                       │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │
│  │  财务服务   │ │  供应链服务 │ │  销售服务   │ │  人力资源   │       │
│  │  Finance    │ │   Supply    │ │    Sales    │ │    HRM      │       │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │
│  │  生产服务   │ │  资产服务   │ │  项目服务   │ │  质量服务   │       │
│  │ Production  │ │    Asset    │ │   Project   │ │   Quality   │       │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                     │
│  │  工作流服务 │ │  报表服务   │ │  租户服务   │                     │
│  │  Workflow  │ │  Reporting  │ │   Tenant    │                     │
│  └─────────────┘ └─────────────┘ └─────────────┘                     │
└─────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           中间件层 (Middleware)                         │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │ 服务注册中心 │  │ 配置中心     │  │ 消息队列     │  │  分布式缓存  ││
│  │  (Nacos)    │  │ (Nacos/Apollo)│  │ (RocketMQ)   │  │  (Redis)     ││
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘│
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │  分布式事务 │  │ 任务调度     │  │ 搜索引擎     │  │  对象存储    ││
│  │ (Seata)     │  │ (XXL-Job)    │  │ (OpenSearch) │  │ (MinIO/OSS)  ││
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘│
└─────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           数据层 (Data Layer)                           │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    主数据库 (Primary Database)                    │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │  │
│  │  │ 业务数据库   │  │ 租户隔离库   │  │    系统配置库         │  │  │
│  │  │ (MySQL/PgSQL)│  │ (按租户分库) │  │   (Config/Permission) │  │  │
│  │  └──────────────┘  └──────────────┘  └──────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                   数据仓库 (Data Warehouse)                      │  │
│  │                   用于 BI 分析和报表生成                         │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                   时序数据库 (Optional)                          │  │
│  │                   用于 IoT 和监控数据存储                        │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        基础设施层 (Infrastructure)                     │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                    Kubernetes 集群                              │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │  │
│  │  │  容器编排    │  │  服务网格    │  │  可观测性    │          │  │
│  │  │ (K8s/Docker) │  │ (Istio/Cilium)│  │(Prometheus/Grafana)│    │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 技术栈选型

### 后端技术栈

| 技术分类 | 技术选型 | 版本 | 说明 |
|---------|---------|------|------|
| **开发语言** | Java | 17+ | LTS 版本，长期支持 |
| **开发框架** | Spring Boot | 3.x | 现代化微服务框架 |
| **微服务治理** | Spring Cloud Alibaba | 2022.x | 国内生态成熟 |
| **服务注册** | Nacos | 2.x | 服务发现与配置中心 |
| **API 网关** | Spring Cloud Gateway | 4.x | 响应式网关 |
| **负载均衡** | Nginx + OpenResty | latest | 高性能反向代理 |
| **分布式事务** | Seata | 1.x | AT/TCC/SAGA 模式 |
| **消息队列** | RocketMQ | 5.x | 高吞吐、事务消息 |
| **分布式缓存** | Redis | 7.x | 缓存 + 分布式锁 |
| **任务调度** | XXL-Job | 2.x | 分布式任务调度 |
| **搜索引擎** | OpenSearch | 2.x | 全文检索与日志分析 |
| **对象存储** | MinIO | latest | 私有云对象存储 |
| **ORM框架** | MyBatis-Plus | 3.x | 持久层增强 |
| **文档引擎** | EasyExcel | 3.x | Excel 导入导出 |

### 前端技术栈

| 技术分类 | 技术选型 | 版本 | 说明 |
|---------|---------|------|------|
| **开发框架** | Vue.js | 3.x | 组合式 API |
| **构建工具** | Vite | 5.x | 快速构建 |
| **状态管理** | Pinia | 2.x | Vue 官方状态管理 |
| **UI 框架** | Element Plus | 2.x | 企业级组件库 |
| **移动端** | Flutter | 3.x | 跨平台移动应用 |
| **图表库** | ECharts | 5.x | 数据可视化 |
| **表格增强** | VXE-Table | 4.x | 高性能表格 |

### 数据库技术栈

| 技术分类 | 技术选型 | 版本 | 说明 |
|---------|---------|------|------|
| **关系型数据库** | MySQL / PostgreSQL | 8.0 / 15+ | 主数据库 |
| **时序数据库** | TDengine / InfluxDB | 3.x | IoT 数据 (可选) |
| **搜索引擎** | OpenSearch | 2.x | 全文检索 |
| **缓存数据库** | Redis | 7.x | 分布式缓存 |

### DevOps 工具链

| 技术分类 | 技术选型 | 说明 |
|---------|---------|------|
| **容器编排** | Kubernetes | 容器编排 |
| **容器运行时** | Containerd | 生产级容器运行时 |
| **镜像仓库** | Harbor | 企业级 Docker 镜像仓库 |
| **CI/CD** | GitLab CI / Jenkins | 持续集成与部署 |
| **代码质量** | SonarQube | 代码静态分析 |
| **日志收集** | PLG (Promtail/Loki/Grafana) | 轻量级日志方案 |
| **监控告警** | Prometheus + Grafana | 监控与可视化 |
| **链路追踪** | SkyWalking | APM 性能监控 |
| **配置管理** | Helm + Kustomize | K8s 应用管理 |

---

## 多租户架构设计

### 租户隔离策略

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         多租户隔离架构                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  策略选择: 以数据库隔离为主，Schema/表隔离为辅                           │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       租户路由层                                  │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │  TenantIdentifier (租户识别器)                              │  │  │
│  │  │  - 域名识别 (tenant1.erp.company.com)                      │  │  │
│  │  │  - Header识别 (X-Tenant-ID: tenant1)                       │  │  │
│  │  │  - Token识别 (JWT claims)                                  │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                   数据源动态路由 (DataSource)                      │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐             │  │
│  │  │ VIP 企业    │  │ 标准企业    │  │ 小微企业    │             │  │
│  │  │ 独立数据库   │  │ 共享数据库  │  │ 共享数据库  │             │  │
│  │  │ + Schema隔离 │  │ + 表级隔离  │  │ + 行级隔离  │             │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘             │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                   缓存隔离 (Redis)                                 │  │
│  │  Key设计: tenant:{tenantId}:{module}:{key}                        │  │
│  │  示例: tenant:10001:finance:account:12345                         │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                   消息队列隔离 (RocketMQ)                          │  │
│  │  Topic设计: {tenantId}_{module}_{event}                           │  │
│  │  示例: 10001_sales_order_created                                  │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                   对象存储隔离 (MinIO)                             │  │
│  │  Bucket设计: erp-{tenantId}/{module}/{year}/{month}/{filename}     │  │
│  │  示例: erp-10001/finance/invoice/2024/01/INV001.pdf              │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 租户套餐等级

| 套餐等级 | 数据隔离 | 存储限制 | 并发用户 | 功能模块 |
|---------|---------|---------|---------|---------|
| **旗舰版** | 独立数据库 | 1TB+ | 1000+ | 全部模块 |
| **企业版** | Schema隔离 | 500GB | 100-500 | 核心模块+扩展 |
| **标准版** | 表级隔离 | 100GB | 20-100 | 核心模块 |
| **基础版** | 行级隔离 | 10GB | 5-20 | 基础模块 |

---

## 核心模块设计

### 模块划分

```
NextERP
├── nexterp-platform (平台层)
│   ├── nexterp-auth (认证授权)
│   ├── nexterp-tenant (多租户管理)
│   ├── nexterp-workflow (工作流引擎)
│   ├── nexterp-report (报表引擎)
│   ├── nexterp-notification (消息通知)
│   └── nexterp-file (文件管理)
│
├── nexterp-business (业务层)
│   ├── nexterp-finance (财务管理)
│   ├── nexterp-supply (供应链管理)
│   ├── nexterp-sales (销售管理)
│   ├── nexterp-production (生产管理)
│   ├── nexterp-hrm (人力资源)
│   ├── nexterp-project (项目管理)
│   ├── nexterp-asset (资产管理)
│   └── nexterp-quality (质量管理)
│
├── nexterp-gateway (网关层)
│   └── nexterp-gateway-service
│
├── nexterp-common (公共层)
│   ├── nexterp-common-core (核心工具)
│   ├── nexterp-common-web (Web组件)
│   ├── nexterp-common-data (数据组件)
│   ├── nexterp-common-security (安全组件)
│   └── nexterp-common-log (日志组件)
│
└── nexterp-adapter (适配层)
    ├── nexterp-adapter-sap (SAP适配器)
    ├── nexterp-adapter-wechat (微信适配器)
    └── nexterp-adapter-dingtalk (钉钉适配器)
```

### 财务模块详细设计

```
nexterp-finance (财务管理)
│
├── nexterp-finance-api (API 定义)
│
├── nexterp-finance-business (业务逻辑)
│   ├── gl (总账管理)
│   │   ├── account (科目管理)
│   │   ├── voucher (凭证管理)
│   │   ├── balance (余额管理)
│   │   └── closing (期末结账)
│   │
│   ├── ar (应收管理)
│   │   ├── customer (客户管理)
│   │   ├── invoice (发票管理)
│   │   ├── receipt (收款管理)
│   │   └── aging (账龄分析)
│   │
│   ├── ap (应付管理)
│   │   ├── supplier (供应商管理)
│   │   ├── bill (账单管理)
│   │   ├── payment (付款管理)
│   │   └── reconciliation (对账)
│   │
│   ├── ca (资金管理)
│   │   ├── account (账户管理)
│   │   ├── payment (支付管理)
│   │   ├── receipt (收款管理)
│   │   └── reconciliation (银企对账)
│   │
│   ├── fa (固定资产)
│   │   ├── asset (资产台账)
│   │   ├── category (资产分类)
│   │   ├── depreciation (折旧计算)
│   │   └── change (资产变动)
│   │
│   └── cost (成本管理)
│       ├── element (成本要素)
│       ├── center (成本中心)
│       ├── allocation (成本分摊)
│       └── analysis (成本分析)
│
└── nexterp-finance-infrastructure (基础设施)
    ├── repository (数据访问)
    ├── mapper (MyBatis映射)
    └── entity (实体模型)
```

---

## 数据库设计

### 数据库命名规范

```
数据库名: nexterp_{tenant_id}_{module}
表名: t_{module}_{function}
示例:
- nexterp_10001_finance (租户10001的财务数据库)
- t_fi_account (会计科目表)
- t_fi_voucher (凭证表)
- t_fi_voucher_detail (凭证明细表)
```

### 核心表设计

#### 1. 租户表 (t_platform_tenant)

```sql
CREATE TABLE t_platform_tenant (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '租户ID',
    tenant_code     VARCHAR(50) NOT NULL COMMENT '租户编码',
    tenant_name     VARCHAR(200) NOT NULL COMMENT '租户名称',
    package_level   TINYINT NOT NULL DEFAULT 1 COMMENT '套餐等级',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    expire_time     DATETIME COMMENT '过期时间',
    db_name         VARCHAR(100) COMMENT '独立数据库名',
    contact_name    VARCHAR(100) COMMENT '联系人',
    contact_phone   VARCHAR(20) COMMENT '联系电话',
    contact_email   VARCHAR(100) COMMENT '联系邮箱',
    created_by      BIGINT COMMENT '创建人',
    created_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_tenant_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';
```

#### 2. 用户表 (t_platform_user)

```sql
CREATE TABLE t_platform_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    tenant_id       BIGINT NOT NULL COMMENT '租户ID',
    username        VARCHAR(50) NOT NULL COMMENT '用户名',
    password        VARCHAR(200) NOT NULL COMMENT '密码',
    real_name       VARCHAR(100) NOT NULL COMMENT '真实姓名',
    email           VARCHAR(100) COMMENT '邮箱',
    phone           VARCHAR(20) COMMENT '手机号',
    avatar          VARCHAR(500) COMMENT '头像URL',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_by      BIGINT COMMENT '创建人',
    created_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_tenant_username (tenant_id, username),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 3. 会计科目表 (t_fi_account)

```sql
CREATE TABLE t_fi_account (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '科目ID',
    tenant_id       BIGINT NOT NULL COMMENT '租户ID',
    account_code    VARCHAR(50) NOT NULL COMMENT '科目编码',
    account_name    VARCHAR(200) NOT NULL COMMENT '科目名称',
    account_level   TINYINT NOT NULL COMMENT '科目级次',
    parent_id       BIGINT COMMENT '父科目ID',
    account_type    VARCHAR(20) NOT NULL COMMENT '科目类型 ASSETS-资产 LIABILITIES-负债 EQUITY-权益 REVENUE-收入 EXPENSE-费用',
    direction       VARCHAR(10) NOT NULL COMMENT '余额方向 DEBIT-借 CREDIT-贷',
    is_leaf         TINYINT NOT NULL DEFAULT 1 COMMENT '是否叶子节点',
    is_enabled      TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    auxiliary_type VARCHAR(100) COMMENT '辅助核算类型 CUSTOMER-供应商 DEPARTMENT-部门 EMPLOYEE-员工 PROJECT-项目',
    created_by      BIGINT COMMENT '创建人',
    created_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_tenant_code (tenant_id, account_code),
    KEY idx_tenant_id (tenant_id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计科目表';
```

#### 4. 凭证表 (t_fi_voucher)

```sql
CREATE TABLE t_fi_voucher (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '凭证ID',
    tenant_id       BIGINT NOT NULL COMMENT '租户ID',
    voucher_no      VARCHAR(50) NOT NULL COMMENT '凭证号',
    voucher_date    DATE NOT NULL COMMENT '凭证日期',
    accounting_period VARCHAR(10) NOT NULL COMMENT '会计期间 YYYY-MM',
    voucher_type    VARCHAR(20) NOT NULL COMMENT '凭证类型',
    attachment_count INT DEFAULT 0 COMMENT '附件数量',
    debit_amount    DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '借方金额',
    credit_amount   DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '贷方金额',
    voucher_status  VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '凭证状态 DRAFT-草稿 REVIEW-审核 POSTED-过账 REVERSED-冲销',
    reviewer_id     BIGINT COMMENT '审核人',
    review_time     DATETIME COMMENT '审核时间',
    poster_id       BIGINT COMMENT '过账人',
    post_time       DATETIME COMMENT '过账时间',
    remark          VARCHAR(500) COMMENT '摘要',
    created_by      BIGINT COMMENT '创建人',
    created_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_tenant_period_no (tenant_id, accounting_period, voucher_no),
    KEY idx_tenant_id (tenant_id),
    KEY idx_voucher_date (voucher_date),
    KEY idx_voucher_status (voucher_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证表';
```

#### 5. 凭证明细表 (t_fi_voucher_detail)

```sql
CREATE TABLE t_fi_voucher_detail (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    tenant_id       BIGINT NOT NULL COMMENT '租户ID',
    voucher_id      BIGINT NOT NULL COMMENT '凭证ID',
    line_no         INT NOT NULL COMMENT '行号',
    account_id      BIGINT NOT NULL COMMENT '科目ID',
    account_code    VARCHAR(50) NOT NULL COMMENT '科目编码',
    debit_amount    DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '借方金额',
    credit_amount   DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '贷方金额',
    auxiliary_json JSON COMMENT '辅助核算JSON',
    description     VARCHAR(500) COMMENT '摘要',
    created_by      BIGINT COMMENT '创建人',
    created_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_voucher_line (voucher_id, line_no),
    KEY idx_tenant_id (tenant_id),
    KEY idx_voucher_id (voucher_id),
    KEY idx_account_id (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='凭证明细表';
```

---

## 安全架构设计

### 安全体系

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           安全防护体系                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       认证授权 (IAM)                               │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │  │
│  │  │  多因素认证  │  │  RBAC/ABAC  │  │  OAuth2/SAML │            │  │
│  │  │  MFA         │  │  权限模型    │  │  单点登录    │            │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘            │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       数据安全                                     │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │  │
│  │  │  敏感数据加密│  │  数据脱敏    │  │  备份恢复    │            │  │
│  │  │  AES-256     │  │  Masking     │  │  自动备份    │            │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘            │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       网络安全                                     │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │  │
│  │  │  WAF防火墙   │  │  DDoS防护    │  │  TLS/SSL     │            │  │
│  │  │  ModSecurity │  │  流量清洗    │  │  传输加密    │            │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘            │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       审计与合规                                   │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │  │
│  │  │  操作审计    │  │  数据日志    │  │  合规报告    │            │  │
│  │  │  全量记录    │  │  不可篡改    │  │  等保三级    │            │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘            │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### RBAC 权限模型

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           RBAC 权限模型                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│    User (用户)                                                          │
│       │                                                                 │
│       ├─── Role (角色)                                                  │
│       │      │                                                          │
│       │      ├─── Permission (权限)                                     │
│       │      │      │                                                  │
│       │      │      ├─── Resource (资源)                                │
│       │      │      │      ├── Module:Finance (模块:财务)               │
│       │      │      │      ├── Action:Create (操作:创建)                │
│       │      │      │      └── DataScope:All (数据范围:全部)            │
│       │      │                                                            │
│       │      └─── Data Permission (数据权限)                            │
│       │             ├── Organization:部门数据                           │
│       │             ├── Project:项目数据                                │
│       │             └── Custom:自定义规则                               │
│       │                                                                  │
│       └─── Group (用户组) - 便于批量授权                                │
│                                                                         │
│  预定义角色:                                                             │
│  - 系统管理员 (SYSTEM_ADMIN)                                            │
│  - 租户管理员 (TENANT_ADMIN)                                            │
│  - 财务主管 (FINANCE_MANAGER)                                           │
│  - 会计人员 (ACCOUNTANT)                                                │
│  - 出纳人员 (CASHIER)                                                   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 部署架构

### Kubernetes 部署架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Kubernetes 生产集群部署                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Namespace: nexterp-prod                                                │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                        Ingress Controller                         │  │
│  │  (Nginx Ingress + Cert-Manager 自动 HTTPS)                        │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       API Gateway (3 Pods)                         │  │
│  │  nexterp-gateway-deployment                                        │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│            ┌─────────────────────┼─────────────────────┐              │
│            ▼                     ▼                     ▼              │
│  ┌───────────────┐     ┌───────────────┐     ┌───────────────┐       │
│  │  业务服务     │     │  中间件服务   │     │  基础服务     │       │
│  │  (微服务组)   │     │  (状态fulset) │     │  (DaemonSet)  │       │
│  │               │     │               │     │               │       │
│  │ - auth (2)    │     │ - mysql (3)   │     │ - node-export │       │
│  │ - finance (3) │     │ - redis (3)   │     │ - filebeat   │       │
│  │ - supply (2)  │     │ - rocketmq(3) │     │ - log-agent  │       │
│  │ - sales (2)   │     │ - nacos (3)   │     └───────────────┘       │
│  │ - hrm (1)     │     │ - seata (1)   │                             │
│  │ - workflow(2) │     │ - minio (1)   │                             │
│  └───────────────┘     └───────────────┘                             │
│                                                                         │
│  StorageClass:                                                          │
│  - ceph-rbd (块存储) - 数据库                                          │
│  - ceph-fs (文件存储) - 共享文件                                       │
│  - local-path (本地存储) - 临时数据                                     │
└─────────────────────────────────────────────────────────────────────────┘
```

### Helm Chart 结构

```
nexterp/
├── Chart.yaml
├── values.yaml
├── values-prod.yaml
├── templates/
│   ├── _helpers.tpl
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── ingress.yaml
│   ├── hpa.yaml
│   └── pdb.yaml
└── charts/
    ├── mysql/
    ├── redis/
    ├── rocketmq/
    └── nacos/
```

---

## 开发规范

### 代码规范

#### Java 后端规范

```java
// Controller 层示例
@RestController
@RequestMapping("/api/v1/finance/vouchers")
@Tag(name = "凭证管理", description = "会计凭证CRUD接口")
@RequiredArgsConstructor
public class FiVoucherController {

    private final FiVoucherService voucherService;

    @GetMapping("/{id}")
    @Operation(summary = "查询凭证详情")
    public Result<FiVoucherVO> getById(@PathVariable Long id) {
        return Result.success(voucherService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建凭证")
    public Result<Long> create(@Valid @RequestBody FiVoucherCreateDTO dto) {
        return Result.success(voucherService.create(dto));
    }
}

// Service 层示例
@Service
@RequiredArgsConstructor
public class FiVoucherService {

    private final FiVoucherMapper voucherMapper;
    private final FiVoucherDetailMapper detailMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long create(FiVoucherCreateDTO dto) {
        // 1. 构建凭证主表
        FiVoucher voucher = BeanUtil.copyProperties(dto, FiVoucher.class);
        voucher.setVoucherNo(generateVoucherNo(dto.getVoucherDate()));
        voucher.setVoucherStatus(VoucherStatus.DRAFT);
        voucherMapper.insert(voucher);

        // 2. 构建凭证明细
        List<FiVoucherDetail> details = buildDetails(dto.getDetails(), voucher.getId());
        detailMapper.insertBatch(details);

        // 3. 校验借贷平衡
        validateBalance(voucher.getId());

        return voucher.getId();
    }
}
```

#### 前端 Vue 规范

```typescript
// API 定义
import request from '@/utils/request'

export interface Voucher {
  id?: number
  voucherNo: string
  voucherDate: string
  accountingPeriod: string
  debitAmount: number
  creditAmount: number
}

export const voucherApi = {
  // 查询凭证列表
  list: (params: VoucherQueryParams) => {
    return request.get<PaginationResult<Voucher>>('/api/v1/finance/vouchers', { params })
  },
  // 查询凭证详情
  getById: (id: number) => {
    return request.get<Voucher>(`/api/v1/finance/vouchers/${id}`)
  },
  // 创建凭证
  create: (data: VoucherCreateForm) => {
    return request.post<number>('/api/v1/finance/vouchers', data)
  }
}

// 组件示例
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { voucherApi } from '@/api/finance/voucher'

const loading = ref(false)
const vouchers = ref<Voucher[]>([])

const fetchVouchers = async () => {
  loading.value = true
  try {
    const { data } = await voucherApi.list({ page: 1, size: 20 })
    vouchers.value = data.records
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchVouchers()
})
</script>
```

### Git 分支规范

```
main (生产分支)
  ├── release/2024.01 (发布分支)
  ├── develop (开发主分支)
  │     ├── feature/fi-voucher (功能分支)
  │     ├── feature/sup-supplier (功能分支)
  │     └── feature/* (功能分支)
  │
  └── hotfix/voucher-bug (紧急修复分支)
```

---

## 与 SAP S/4HANA 对比

### 功能对标

| 模块 | SAP S/4HANA | NextERP | 差异说明 |
|------|-------------|---------|---------|
| **财务总账** | FI/GL | 财务/总账 | 核心功能一致，优化中国会计准则支持 |
| **应收应付** | FI/AR, FI/AP | 财务/应收应付 | 增强电子发票对接、税务自动申报 |
| **资金管理** | TR | 财务/资金 | 增强支付宝/微信等支付渠道 |
| **采购管理** | MM | 供应链/采购 | 增强电子采购平台对接 |
| **库存管理** | MM-IM | 供应链/库存 | 优化批次管理、效期管理 |
| **销售管理** | SD | 销售/销售 | 增强电商渠道对接 |
| **生产管理** | PP | 生产/生产 | 简化复杂制造场景 |
| **人力资源** | HCM | 人力资源 | 聚焦中小企业核心需求 |

### 技术优势

| 维度 | SAP S/4HANA | NextERP |
|------|-------------|---------|
| **部署成本** | 高 (专用硬件 + 许可费) | 低 (通用服务器 + 开源) |
| **实施周期** | 6-18个月 | 1-3个月 |
| **定制开发** | 复杂 (需SAP认证) | 简单 (全开源) |
| **云原生** | 部分支持 | 完全云原生 |
| **SaaS** | 有限 | 完全多租户 |
| **中文支持** | 国际化版本 | 原生中文设计 |

---

## 路线规划

### Phase 1: 基础平台 (3个月)

- [ ] 用户认证与授权
- [ ] 多租户管理
- [ ] 基础权限框架
- [ ] 工作流引擎
- [ ] 报表引擎
- [ ] 文件管理
- [ ] 消息通知

### Phase 2: 财务模块 (3个月)

- [ ] 总账管理
- [ ] 应收应付
- [ ] 固定资产
- [ ] 资金管理
- [ ] 成本核算
- [ ] 财务报表

### Phase 3: 供应链模块 (3个月)

- [ ] 采购管理
- [ ] 库存管理
- [ ] 仓库管理
- [ ] 供应商管理

### Phase 4: 销售模块 (2个月)

- [ ] 销售订单
- [ ] 发货管理
- [ ] 客户管理
- [ ] 价格管理

### Phase 5: 生产与扩展 (持续)

- [ ] 生产管理
- [ ] 人力资源
- [ ] 项目管理
- [ ] 移动端应用
- [ ] 开放平台

---

## 附录

### A. 参考文档

- 《企业会计准则》财政部
- 《SAP S/4HANA 技术架构文档》
- 《云原生应用架构实践》
- 《微服务架构设计模式》

### B. 开源协议

本项目采用 **MIT** 开源协议，允许商业使用。

### C. 联系方式

- 项目地址: https://github.com/nexterp/nexterp
- 文档地址: https://docs.nexterp.com
- 技术交流: https://community.nexterp.com
