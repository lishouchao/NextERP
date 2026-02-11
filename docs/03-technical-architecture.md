# NextERP 国产ERP技术架构方案 v2.0

> **架构决策**：采用模块化单体 (Modular Monolith) 架构，预留微服务演进路径

## 项目概述

**NextERP** 是一款面向中国企业的云原生ERP系统，采用SaaS多租户模式，基于模块化单体架构构建，旨在提供与国际一流ERP系统相媲美的功能，同时更好地适配中国企业业务场景和合规要求。

### 核心定位

- **国产自主可控**：全栈开源技术，无外部依赖风险
- **云原生架构**：基于Kubernetes，支持弹性伸缩和多云部署
- **SaaS多租户**：一套服务支撑多企业隔离
- **模块化单体**：单一部署单元，严格的模块边界，预留演进空间
- **现代化体验**：响应式Web界面 + 移动端支持

### 架构演进策略

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      NextERP 架构演进路线                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Phase 1: 模块化单体 (当前阶段)                                        │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  目标: 快速交付 MVP，降低开发和运维成本                             │  │
│  │  特点:                                                             │  │
│  │  - 单一部署单元                                                   │  │
│  │  - 严格的模块边界 (接口隔离)                                      │  │
│  │  - 本地事务 ACID (强一致性)                                       │  │
│  │  - 领域事件驱动解耦                                                 │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  Phase 2: 垂直拆分 (成长阶段)                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  触发条件: 团队 > 20人，特定模块性能瓶颈                           │  │
│  │  拆分模块: 报表服务、通知服务、文件服务                             │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  Phase 3: 混合架构 (成熟阶段)                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  核心业务 (单体) + 边缘服务 (微服务)                               │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 技术架构总览

### 整体架构图 - 模块化单体

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              访问层 (Access Layer)                       │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │  Web Browser │  │  Mobile App  │  │  Third-party │  │   Open API   ││
│  │  (Next.js)   │  │  (Flutter)   │  │   Systems    │  │  (REST/GraphQL)││
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
│                      NextERP 模块化单体应用                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      平台层 (Platform)                             │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │  │
│  │  │ 认证授权  │ │ 多租户   │ │ 工作流   │ │ 报表引擎 │ │ 消息通知 │ │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      业务层 (Business)                             │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │  │
│  │  │ 财务管理  │ │ 供应链   │ │ 销售管理 │ │ 生产管理 │ │ 人力资源 │ │  │
│  │  │ (Module)  │ │ (Module)  │ │ (Module)  │ │ (Module)  │ │ (Module)  │ │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘ │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐                             │  │
│  │  │ 项目管理  │ │ 资产管理 │ │ 质量管理 │                             │  │
│  │  └──────────┘ └──────────┘ └──────────┘                             │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      共享层 (Shared)                                │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │  统一数据访问 (单数据库连接池 + 本地事务)                    │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │  领域事件总线 (进程内事件，模块解耦)                          │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │  共享组件 (缓存、消息队列、任务调度)                         │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        中间件与数据层 (Middleware & Data)                   │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐│
│  │  缓存 (Redis) │  │ 消息队列    │  │ 任务调度    │  │ 对象存储    ││
│  │             │  │ (RocketMQ)  │  │ (XXL-Job)   │  │ (MinIO)     ││
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘│
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                   主数据库 (MySQL/PostgreSQL)                      │  │
│  │                   租户隔离: 行级 tenant_id 字段                     │  │
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
│  │  │  容器编排    │  │ 服务网格(可选)│  │  可观测性    │          │  │
│  │  │ (K8s/Docker) │  │  (后期演进)  │  │(Prometheus/Grafana)│    │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 架构关键特性

| 特性 | 实现方式 | 优势 |
|------|---------|------|
| **单一部署单元** | 单一 JAR/WAR | 部署简单，DevOps 友好 |
| **模块边界清晰** | 包级隔离 + ArchUnit 测试 | 代码内聚，便于维护 |
| **本地事务** | Spring @Transactional | ACID 强一致性 |
| **模块解耦** | 领域事件 (ApplicationEventPublisher) | 松耦合，易扩展 |
| **租户隔离** | 数据库行级隔离 + Redis Key 前缀 | 多租户支持 |
| **接口标准** | 模块 API 层 (spi/api 包) | 预留拆分空间 |

---

## 技术栈选型

### 后端技术栈 (简化版)

| 技术分类 | 技术选型 | 版本 | 说明 |
|---------|---------|------|------|
| **开发语言** | Java | 21+ | 最新 LTS 版本 |
| **开发框架** | Spring Boot | 3.2+ | 现代化框架 |
| **数据访问** | Spring Data JPA + MyBatis-Plus | 3.x | JPA 简化 CRUD，MyBatis 复杂查询 |
| **API 文档** | SpringDoc (OpenAPI 3.0) | 2.x | 自动生成 API 文档 |
| **缓存** | Spring Cache + Redis | 7.x | 统一缓存抽象 |
| **消息队列** | Spring Kafka/RocketMQ | 3.x/5.x | 事件驱动 |
| **任务调度** | Spring Scheduling + XXL-Job | 2.x | 本地 + 分布式任务 |
| **参数验证** | Spring Validation + Jakarta | 3.x | 统一参数校验 |
| **安全框架** | Spring Security + JWT | 6.x | 认证授权 |
| **监控** | Spring Boot Actuator + Micrometer | 3.x | 应用监控 |
| **日志** | SLF4J + Logback | 2.x | 日志记录 |

### 前端技术栈

| 技术分类 | 技术选型 | 版本 | 说明 |
|---------|---------|------|------|
| **开发框架** | Next.js | 15.x | React 服务端渲染框架 |
| **UI 库** | React | 19.x | 用户界面库 |
| **状态管理** | Zustand / React Context | 5.x / 19.x | 轻量级状态管理 |
| **UI 框架** | Ant Design | 5.x | 企业级组件库 |
| **表单处理** | React Hook Form | 7.x | 高性能表单管理 |
| **数据请求** | SWR / TanStack Query | 2.x / 5.x | 数据获取与缓存 |
| **移动端** | Flutter | 3.x | 跨平台移动应用 |
| **图表库** | ECharts / Recharts | 5.x / 2.x | 数据可视化 |
| **表格增强** | Ant Design Table | 5.x | 高性能表格 |
| **TypeScript** | TypeScript | 5.x | 类型安全 |
| **样式方案** | Tailwind CSS | 3.x | 原子化 CSS |

### 数据库技术栈

| 技术分类 | 技术选型 | 版本 | 说明 |
|---------|---------|------|------|
| **主数据库** | PostgreSQL / MySQL | 16+ / 8.0+ | 推荐PostgreSQL |
| **缓存数据库** | Redis | 7.x | 分布式缓存 |
| **全文搜索** | PostgreSQL 全文搜索 / OpenSearch | - | 可选 |

### DevOps 工具链

| 技术分类 | 技术选型 | 说明 |
|---------|---------|------|
| **容器编排** | Kubernetes | 容器编排 |
| **容器运行时** | Containerd | 生产级容器运行时 |
| **镜像仓库** | Harbor | 企业级 Docker 镜像仓库 |
| **CI/CD** | GitLab CI | 持续集成与部署 |
| **代码质量** | SonarQube | 代码静态分析 |
| **日志收集** | Loki | 轻量级日志方案 |
| **监控告警** | Prometheus + Grafana | 监控与可视化 |

---

## 模块化单体设计

### 模块化设计原则

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      模块化单体设计原则                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. 模块边界清晰 (Explicit Module Boundaries)                            │
│     ┌───────────────────────────────────────────────────────────────┐  │
│     │  - 包级隔离: com.nexterp.{module}.*                            │  │
│     │  - API 层: com.nexterp.{module}.api                            │  │
│     │  - SPI 层: com.nexterp.{module}.spi (可扩展接口)               │  │
│     │  - 实现: com.nexterp.{module}.internal                         │  │
│     └───────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  2. 接口隔离 (Interface Segregation)                                    │
│     ┌───────────────────────────────────────────────────────────────┐  │
│     │  模块通过 API 包暴露的接口进行通信                              │  │
│     │  严禁直接依赖模块内部实现类                                     │  │
│     │  使用 ArchUnit 进行架构测试                                     │  │
│     └───────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  3. 领域事件驱动 (Domain Event Driven)                                  │
│     ┌───────────────────────────────────────────────────────────────┐  │
│     │  - 使用 Spring ApplicationEventPublisher                       │  │
│     │  - 事件包: com.nexterp.{module}.event                          │  │
│     │  - 监听器包: com.nexterp.{module}.listener                     │  │
│     │  - 异步处理: @EventListener @Async                            │  │
│     └───────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  4. 数据库隔离 (Database Isolation)                                     │
│     ┌───────────────────────────────────────────────────────────────┐  │
│     │  - 每个模块有独立的表前缀: t_{module}_{table}                    │  │
│     │  - 租户隔离: 所有表包含 tenant_id 字段                          │  │
│     │  - 模块可演进为独立 Schema 或 Database                          │  │
│     └───────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  5. 共享基础设施 (Shared Infrastructure)                                │
│     ┌───────────────────────────────────────────────────────────────┐  │
│     │  - 统一的数据访问层 (Repository 模式)                           │  │
│     │  - 统一的缓存抽象 (Spring Cache)                                │  │
│     │  - 统一的消息发送 (Message Publisher)                          │  │
│     │  - 统一的异常处理 (Global Exception Handler)                    │  │
│     └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 模块结构规范

```
nexterp/
├── nexterp-assembly/                    # 部署模块
│   └── pom.xml
│
├── nexterp-platform/                    # 平台层
│   ├── nexterp-platform-auth/           # 认证授权
│   │   ├── api/                         # 对外API
│   │   ├── spi/                         # 可扩展接口
│   │   ├── internal/                   # 内部实现
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   └── domain/
│   │   └── resources/                  # 资源文件
│   │
│   ├── nexterp-platform-tenant/         # 多租户
│   ├── nexterp-platform-workflow/       # 工作流
│   ├── nexterp-platform-report/         # 报表引擎 ← 可优先微服务化
│   └── nexterp-platform-notification/   # 消息通知 ← 可优先微服务化
│
├── nexterp-business/                    # 业务层
│   ├── nexterp-business-finance/        # 财务模块
│   │   ├── finance-api/                 # 对外接口
│   │   │   ├── dto/                    # 数据传输对象
│   │   │   ├── vo/                     # 视图对象
│   │   │   └── facade/                 # 门面接口
│   │   │
│   │   ├── finance-spi/                 # 扩展接口
│   │   │   └── FinanceServiceExtension.java
│   │   │
│   │   ├── finance-domain/              # 领域模型
│   │   │   ├── model/                  # 实体
│   │   │   ├── event/                  # 领域事件
│   │   │   ├── service/                # 领域服务
│   │   │   └── repository/             # 仓储接口
│   │   │
│   │   ├── finance-application/         # 应用服务
│   │   │   ├── service/                 # 应用服务实现
│   │   │   └── listener/                # 事件监听器
│   │   │
│   │   └── finance-infrastructure/      # 基础设施
│   │       ├── repository/jpa/         # JPA 仓储实现
│   │       ├── mapper/                 # MyBatis 映射
│   │       └── config/                 # 模块配置
│   │
│   ├── nexterp-business-supply/         # 供应链
│   ├── nexterp-business-sales/          # 销售
│   ├── nexterp-business-production/     # 生产
│   └── nexterp-business-hrm/            # 人力资源
│
├── nexterp-shared/                      # 共享层
│   ├── nexterp-shared-core/             # 核心工具
│   │   ├── util/                        # 工具类
│   │   ├── constant/                    # 常量定义
│   │   ├── exception/                   # 异常定义
│   │   └── enums/                       # 枚举定义
│   │
│   ├── nexterp-shared-data/             # 数据访问
│   │   ├── BaseRepository.java         # 基础仓储
│   │   ├── BaseEntity.java             # 基础实体
│   │   ├── TenantSupport.java          # 租户支持
│   │   └── pagination/                  # 分页组件
│   │
│   ├── nexterp-shared-web/              # Web组件
│   │   ├── BaseController.java         # 基础控制器
│   │   ├── Result.java                 # 统一响应
│   │   └── validation/                  # 参数校验
│   │
│   ├── nexterp-shared-security/         # 安全组件
│   │   ├── UserDetails.java            # 用户详情
│   │   ├── TenantContext.java          # 租户上下文
│   │   └── permission/                  # 权限注解
│   │
│   └── nexterp-shared-cache/            # 缓存组件
│       ├── CacheConfig.java            # 缓存配置
│       └── CacheKeyGenerator.java     # 缓存键生成
│
└── nexterp-api/                         # API 网关 (独立部署)
    └── pom.xml
```

### 模块间通信规范

```java
// ==================== 模块A：财务模块 ====================
package com.nexterp.business.finance.api;

// 模块A对外接口
public interface FiVoucherService {
    FiVoucherVO getById(Long id);
    Long create(FiVoucherCreateDTO dto);
    void post(Long id);
}

// ==================== 模块B：销售模块 ====================
package com.nexterp.business.sales;

// 销售模块调用财务模块接口
@Service
@RequiredArgsConstructor
public class SalesOrderService {

    // 依赖注入模块A的API接口（不是实现类）
    private final FiVoucherService fiVoucherService;

    @Transactional
    public void createOrder(SalesOrderCreateDTO dto) {
        // 1. 创建销售订单
        SalesOrder order = buildOrder(dto);
        orderRepository.save(order);

        // 2. 通过模块API调用财务模块
        // 财务模块生成应收账款凭证
        fiVoucherService.create(FiVoucherCreateDTO.builder()
            .accountingPeriod(DateUtil.format(order.getOrderDate(), "yyyy-MM"))
            .amount(order.getAmount())
            .build());
    }
}

// ==================== 模块解耦：领域事件 ====================
package com.nexterp.business.sales.event;

// 销售订单创建事件
public record SalesOrderCreatedEvent(
    Long orderId,
    String orderNo,
    BigDecimal amount,
    LocalDateTime createTime
) {}

// 发布事件
@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final ApplicationEventPublisher eventPublisher;

    public void createOrder(SalesOrderCreateDTO dto) {
        // 创建订单
        SalesOrder order = buildOrder(dto);
        orderRepository.save(order);

        // 发布领域事件（模块解耦）
        eventPublisher.publishEvent(new SalesOrderCreatedEvent(
            order.getId(),
            order.getOrderNo(),
            order.getAmount(),
            LocalDateTime.now()
        ));
    }
}

// ==================== 财务模块监听事件 ====================
package com.nexterp.business.finance.application.listener;

@Component
@RequiredArgsConstructor
public class FinanceOrderListener {

    private final FiReceivableService receivableService;

    @EventListener
    @Async("financeEventExecutor")
    public void handleOrderCreated(SalesOrderCreatedEvent event) {
        // 监听销售订单创建事件，生成应收账款
        receivableService.createReceivable(event);
    }
}
```

### ArchUnit 架构测试

```java
@AnalyzeClasses(packages = "com.nexterp")
public class ModularityArchitectureTest {

    // 财务模块只被平台层依赖，不被其他业务模块直接依赖
    @ArchTest
    static final ArchRule finance_module_isolation =
        classes().that().resideInAPackage("..finance..")
            .should().onlyBeAccessed().byAnyPackage(
                "..finance..",     // 模块自身
                "..platform..",    // 平台层
                "..shared.."       // 共享层
            );

    // 业务模块不依赖其他业务模块的实现
    @ArchTest
    static final ArchRule no_module_to_module_dependency =
        noClasses()
            .that().resideInAPackage("..business..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..business..", "java..", "org.springframework..");

    // API 包的类是公共接口
    @ArchTest
    static final ArchRule api_classes_are_public_interfaces =
        classes().that().resideInAPackage("..api..")
            .should().beInterfaces()
            .orShould().beAnnotatedWith(PublicAPI.class);

    // Internal 包的类不被外部依赖
    @ArchTest
    static final ArchRule internal_classes_not_accessible =
        classes().that().resideInAPackage("..internal..")
            .should().onlyBeAccessed().byAnyPackage("..module..", "..shared..");
}
```

---

## 多租户架构设计

### 租户隔离策略 (行级隔离)

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         多租户隔离架构 (行级)                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  策略选择: 行级隔离 (Row-Level Isolation)                                │
│                                                                         │
│  优势:                                                                   │
│  - 单一数据库，运维简单                                               │
│  - 支持跨租户联合查询                                                 │
│  - 备份恢复统一管理                                                   │
│  - 适合中小规模租户                                                   │
│                                                                         │
│  数据库设计:                                                             │
│  所有表包含 tenant_id 字段作为租户标识                                 │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       租户上下文管理                                │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │  TenantContextHolder (ThreadLocal存储)                       │  │  │
│  │  │  - 存储当前请求的租户ID                                     │  │  │
│  │  │  - 自动传递给数据访问层                                     │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       数据访问层自动过滤                             │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │  @Mapper注解自动添加 tenant_id 条件                         │  │  │
│  │  │  SELECT * FROM t_fi_voucher WHERE tenant_id = ? AND ...     │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       缓存隔离 (Redis)                                 │  │
│  │  Key设计: tenant:{tenantId}:{module}:{key}                        │  │
│  │  示例: tenant:10001:finance:account:12345                         │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       消息队列隔离 (RocketMQ)                          │  │
│  │  Topic设计: {tenantId}_{module}_{event}                           │  │
│  │  示例: 10001_sales_order_created                                  │  │
│  │  消费者使用 Tag 过滤: tenantId = 10001                          │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                       对象存储隔离 (MinIO)                             │  │
│  │  Bucket设计: erp/{module}/{tenantId}/{year}/{month}/{filename}  │  │
│  │  示例: erp/finance/10001/2024/01/INV001.pdf                      │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 租户套餐等级

| 套餐等级 | 数据隔离 | 存储限制 | 并发用户 | 功能模块 | 价格/月 |
|---------|---------|---------|---------|---------|--------|
| **旗舰版** | 行级隔离 | 1TB+ | 1000+ | 全部模块 | ¥50,000+ |
| **企业版** | 行级隔离 | 500GB | 100-500 | 核心模块+扩展 | ¥10,000 |
| **标准版** | 行级隔离 | 100GB | 20-100 | 核心模块 | ¥3,000 |
| **基础版** | 行级隔离 | 10GB | 5-20 | 基础模块 | ¥500 |

---

## 数据库设计

### 数据库设计原则

1. **所有表包含租户字段**：`tenant_id BIGINT NOT NULL`
2. **所有表包含审计字段**：`created_by`, `created_time`, `updated_by`, `updated_time`
3. **所有表包含逻辑删除**：`is_deleted TINYINT NOT NULL DEFAULT 0`
4. **表命名规范**：`t_{module}_{function}`

### 核心表设计

#### 1. 租户表 (t_platform_tenant)

```sql
CREATE TABLE t_platform_tenant (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '租户ID',
    tenant_code     VARCHAR(50) NOT NULL COMMENT '租户编码',
    tenant_name     VARCHAR(200) NOT NULL COMMENT '租户名称',
    package_level   TINYINT NOT NULL DEFAULT 1 COMMENT '套餐等级 1-基础 2-标准 3-企业 4-旗舰',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    expire_time     DATETIME COMMENT '过期时间',
    contact_name    VARCHAR(100) COMMENT '联系人',
    contact_phone   VARCHAR(20) COMMENT '联系电话',
    contact_email   VARCHAR(100) COMMENT '联系邮箱',
    max_users       INT DEFAULT 10 COMMENT '最大用户数',
    max_storage     BIGINT DEFAULT 10737418240 COMMENT '最大存储(10GB)',
    config_json     JSON COMMENT '租户配置',
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
    password        VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    real_name       VARCHAR(100) NOT NULL COMMENT '真实姓名',
    email           VARCHAR(100) COMMENT '邮箱',
    phone           VARCHAR(20) COMMENT '手机号',
    avatar          VARCHAR(500) COMMENT '头像URL',
    department_id   BIGINT COMMENT '部门ID',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    is_admin        TINYINT NOT NULL DEFAULT 0 COMMENT '是否租户管理员',
    last_login_time DATETIME COMMENT '最后登录时间',
    login_count     INT DEFAULT 0 COMMENT '登录次数',
    created_by      BIGINT COMMENT '创建人',
    created_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT COMMENT '更新人',
    updated_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_tenant_username (tenant_id, username),
    KEY idx_tenant_id (tenant_id),
    KEY idx_department_id (department_id)
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
    auxiliary_type VARCHAR(100) COMMENT '辅助核算类型 JSON',
    description     VARCHAR(500) COMMENT '科目描述',
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
    attachment_ids  JSON COMMENT '附件ID列表',
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
    account_name    VARCHAR(200) COMMENT '科目名称',
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

## 开发规范

### 后端开发规范

#### Controller 层规范

```java
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

    @GetMapping
    @Operation(summary = "分页查询凭证列表")
    public Result<PageResult<FiVoucherVO>> page(
        @Valid FiVoucherQueryDTO query,
        @PageableDefault(sort = "created_time", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return Result.success(voucherService.page(query, pageable));
    }

    @PostMapping
    @Operation(summary = "创建凭证")
    public Result<Long> create(@Valid @RequestBody FiVoucherCreateDTO dto) {
        return Result.success(voucherService.create(dto));
    }

    @PostMapping("/{id}/post")
    @Operation(summary = "过账凭证")
    public Result<Void> post(@PathVariable Long id) {
        voucherService.post(id);
        return Result.success();
    }
}
```

#### Service 层规范

```java
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class FiVoucherService {

    private final FiVoucherRepository voucherRepository;
    private final FiVoucherDetailRepository detailRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Long create(FiVoucherCreateDTO dto) {
        // 1. 构建凭证主表
        FiVoucher voucher = FiVoucher.builder()
            .tenantId(TenantContextHolder.getTenantId())
            .voucherNo(generateVoucherNo(dto.getVoucherDate()))
            .voucherDate(dto.getVoucherDate())
            .accountingPeriod(DateUtil.format(dto.getVoucherDate(), "yyyy-MM"))
            .voucherType(dto.getVoucherType())
            .voucherStatus(VoucherStatus.DRAFT)
            .build();
        voucherRepository.save(voucher);

        // 2. 构建凭证明细
        List<FiVoucherDetail> details = dto.getDetails().stream()
            .map((dtoItem, index) -> FiVoucherDetail.builder()
                .tenantId(voucher.getTenantId())
                .voucherId(voucher.getId())
                .lineNo(index + 1)
                .accountId(dtoItem.getAccountId())
                .debitAmount(dtoItem.getDebitAmount())
                .creditAmount(dtoItem.getCreditAmount())
                .description(dtoItem.getDescription())
                .build())
            .toList();
        detailRepository.saveAll(details);

        // 3. 校验借贷平衡
        BigDecimal totalDebit = details.stream()
            .map(FiVoucherDetail::getDebitAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = details.stream()
            .map(FiVoucherDetail::getCreditAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException("借贷不平衡");
        }

        // 4. 发布凭证创建事件
        eventPublisher.publishEvent(new FiVoucherCreatedEvent(voucher.getId()));

        return voucher.getId();
    }

    @Transactional
    public void post(Long id) {
        FiVoucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("凭证不存在"));

        if (!VoucherStatus.DRAFT.equals(voucher.getVoucherStatus())) {
            throw new BusinessException("只有草稿状态的凭证可以过账");
        }

        voucher.setVoucherStatus(VoucherStatus.POSTED);
        voucher.setPostTime(LocalDateTime.now());
        voucher.setPosterId(UserContextHolder.getUserId());
        voucherRepository.save(voucher);

        // 发布凭证过账事件
        eventPublisher.publishEvent(new FiVoucherPostedEvent(id));
    }
}
```

#### Repository 层规范

```java
@Repository
@RequiredArgsConstructor
public class FiVoucherRepositoryImpl implements FiVoucherRepository {

    private final FiVoucherMapper mapper;

    @Override
    public FiVoucher save(FiVoucher voucher) {
        if (voucher.getId() == null) {
            mapper.insert(voucher);
        } else {
            mapper.updateById(voucher);
        }
        return voucher;
    }

    @Override
    public Optional<FiVoucher> findById(Long id) {
        return Optional.ofNullable(
            mapper.selectById(id, TenantContextHolder.getTenantId())
        );
    }

    @Override
    public Page<FiVoucher> page(FiVoucherQuery query, Pageable pageable) {
        Page<FiVoucherEntity> page = mapper.selectPage(
            query,
            TenantContextHolder.getTenantId(),
            pageable
        );
        return page.map(this::toDomain);
    }

    private FiVoucher toDomain(FiVoucherEntity entity) {
        // 转换逻辑
    }
}
```

### 前端开发规范

#### TypeScript 类型定义

```typescript
// types/finance/voucher.ts
export interface Voucher {
  id: number
  voucherNo: string
  voucherDate: string
  accountingPeriod: string
  debitAmount: number
  creditAmount: number
  voucherStatus: string
  createdTime: string
}

export interface VoucherQueryParams extends PageParams {
  voucherNo?: string
  voucherDateStart?: string
  voucherDateEnd?: string
  voucherStatus?: string
}

export interface VoucherCreateForm {
  voucherDate: string
  voucherType: string
  details: VoucherDetailItem[]
}
```

#### API 封装 (使用 SWR)

```typescript
// lib/api/finance/voucher.ts
import { fetcher } from '@/lib/fetcher'

export const voucherApi = {
  page: (params: VoucherQueryParams) => {
    return fetcher<PageResult<Voucher>>('/api/v1/finance/vouchers', { params })
  },
  getById: (id: number) => {
    return fetcher<Voucher>(`/api/v1/finance/vouchers/${id}`)
  },
  create: (data: VoucherCreateForm) => {
    return fetcher<number>('/api/v1/finance/vouchers', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },
  post: (id: number) => {
    return fetcher(`/api/v1/finance/vouchers/${id}/post`, {
      method: 'POST'
    })
  }
}

// SWR Hook
import useSWR from 'swr'
import useSWRMutation from 'swr/mutation'

export function useVouchers(params: VoucherQueryParams) {
  return useSWR(
    ['/api/v1/finance/vouchers', params],
    () => voucherApi.page(params)
  )
}

export function usePostVoucher() {
  return useSWRMutation(
    '/api/v1/finance/vouchers/post',
    (url, { arg }: { arg: number }) => voucherApi.post(arg)
  )
}
```

#### React 组件示例

```typescript
// app/finance/vouchers/page.tsx
'use client'

import { Table, Button, message, Space } from 'antd'
import { useVouchers, usePostVoucher } from '@/hooks/api/finance/voucher'
import { ColumnsType } from 'antd/es/table'
import type { Voucher } from '@/types/finance/voucher'

export default function VoucherListPage() {
  const { data, isLoading, error, mutate } = useVouchers({
    page: 1,
    size: 20
  })
  const { trigger: postVoucher, isMutating } = usePostVoucher()

  const handlePost = async (id: number) => {
    try {
      await postVoucher(id)
      message.success('凭证过账成功')
      mutate() // 刷新列表
    } catch (error) {
      message.error('凭证过账失败')
    }
  }

  const columns: ColumnsType<Voucher> = [
    {
      title: '凭证号',
      dataIndex: 'voucherNo',
      key: 'voucherNo'
    },
    {
      title: '凭证日期',
      dataIndex: 'voucherDate',
      key: 'voucherDate'
    },
    {
      title: '借方金额',
      dataIndex: 'debitAmount',
      key: 'debitAmount',
      align: 'right'
    },
    {
      title: '贷方金额',
      dataIndex: 'creditAmount',
      key: 'creditAmount',
      align: 'right'
    },
    {
      title: '状态',
      dataIndex: 'voucherStatus',
      key: 'voucherStatus',
      render: (status: string) => {
        const statusMap: Record<string, { text: string; color: string }> = {
          DRAFT: { text: '草稿', color: 'default' },
          REVIEW: { text: '审核中', color: 'processing' },
          POSTED: { text: '已过账', color: 'success' }
        }
        const { text, color } = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={color}>{text}</Tag>
      }
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="small">
          {record.voucherStatus === 'DRAFT' && (
            <Button
              type="link"
              size="small"
              onClick={() => handlePost(record.id)}
              loading={isMutating}
            >
              过账
            </Button>
          )}
          <Button type="link" size="small">
            查看
          </Button>
        </Space>
      )
    }
  ]

  if (error) {
    return <div>加载失败</div>
  }

  return (
    <div className="p-6">
      <Table
        loading={isLoading}
        columns={columns}
        dataSource={data?.records || []}
        rowKey="id"
        pagination={{
          current: data?.page || 1,
          pageSize: data?.size || 20,
          total: data?.total || 0
        }}
      />
    </div>
  )
}
```

#### 表单处理示例 (React Hook Form + Zod)

```typescript
// components/finance/voucher-form.tsx
'use client'

import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Button, Form, Input, Select } from 'antd'
import { voucherApi } from '@/lib/api/finance/voucher'

const voucherSchema = z.object({
  voucherDate: z.string().min(1, '凭证日期不能为空'),
  voucherType: z.enum(['RECEIPT', 'PAYMENT', 'TRANSFER']),
  details: z.array(z.object({
    accountId: z.number(),
    debitAmount: z.number().min(0),
    creditAmount: z.number().min(0),
    description: z.string()
  })).min(2, '至少需要两条明细')
})

type VoucherFormData = z.infer<typeof voucherSchema>

export function VoucherForm() {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<VoucherFormData>({
    resolver: zodResolver(voucherSchema)
  })

  const onSubmit = async (data: VoucherFormData) => {
    try {
      const id = await voucherApi.create(data)
      message.success('凭证创建成功')
      router.push(`/finance/vouchers/${id}`)
    } catch (error) {
      message.error('凭证创建失败')
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Form.Item label="凭证日期">
        <Input type="date" {...register('voucherDate')} />
        {errors.voucherDate && (
          <span className="text-red-500">{errors.voucherDate.message}</span>
        )}
      </Form.Item>

      <Form.Item label="凭证类型">
        <Select {...register('voucherType')}>
          <Select.Option value="RECEIPT">收款</Select.Option>
          <Select.Option value="PAYMENT">付款</Select.Option>
          <Select.Option value="TRANSFER">转账</Select.Option>
        </Select>
      </Form.Item>

      <Button type="primary" htmlType="submit" loading={isSubmitting}>
        提交
      </Button>
    </form>
  )
}
```

#### 状态管理示例 (Zustand)

```typescript
// stores/finance/voucher.ts
import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'

interface VoucherStore {
  selectedVoucherIds: number[]
  setSelectedVoucherIds: (ids: number[]) => void
  addSelectedId: (id: number) => void
  removeSelectedId: (id: number) => void
  clearSelection: () => void
}

export const useVoucherStore = create<VoucherStore>()(
  devtools(
    persist(
      (set) => ({
        selectedVoucherIds: [],
        setSelectedVoucherIds: (ids) => set({ selectedVoucherIds: ids }),
        addSelectedId: (id) => set((state) => ({
          selectedVoucherIds: [...state.selectedVoucherIds, id]
        })),
        removeSelectedId: (id) => set((state) => ({
          selectedVoucherIds: state.selectedVoucherIds.filter(i => i !== id)
        })),
        clearSelection: () => set({ selectedVoucherIds: [] })
      }),
      { name: 'voucher-storage' }
    )
  )
)

// 使用示例
function BatchPostButton() {
  const { selectedVoucherIds, clearSelection } = useVoucherStore()

  const handleBatchPost = async () => {
    await Promise.all(selectedVoucherIds.map(id => voucherApi.post(id)))
    message.success(`成功过账 ${selectedVoucherIds.length} 张凭证`)
    clearSelection()
  }

  return (
    <Button
      type="primary"
      disabled={selectedVoucherIds.length === 0}
      onClick={handleBatchPost}
    >
      批量过账 ({selectedVoucherIds.length})
    </Button>
  )
}
```

---

## 部署架构

### Kubernetes 部署架构 (简化版)

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
│  │                       NextERP Application                         │  │
│  │  (模块化单体 - 3 Pods)                                           │  │
│  │  Deployment: nexterp-application                                  │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                        中间件服务 (StatefulSet)                    │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │  │
│  │  │   MySQL      │  │   Redis      │  │   MinIO      │            │  │
│  │  │   (3 replicas)│  │   (3 replicas)│  │   (1 replica) │            │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘            │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                        基础服务 (DaemonSet)                         │  │
│  │  - node-exporter (系统监控)                                       │  │
│  │  - filebeat (日志收集)                                             │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  StorageClass:                                                          │
│  - ceph-rbd (块存储) - 数据库                                          │
│  - ceph-fs (文件存储) - MinIO                                         │
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
    ├── postgresql/
    ├── redis/
    └── minio/
```

### 部署配置 (values.yaml)

```yaml
# NextERP 部署配置

# 应用配置
replicaCount: 3

image:
  repository: nexterp/application
  tag: "2.0.0"
  pullPolicy: IfNotPresent

# 资源配置
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"

# 环境变量
env:
  SPRING_PROFILES_ACTIVE: prod
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/nexterp"
  SPRING_REDIS_HOST: redis
  SPRING_REDIS_PORT: 6379

# 数据库
postgresql:
  enabled: true
  auth:
    database: nexterp
    username: nexterp
    password: changeme
  primary:
    resources:
      requests:
        memory: "512Mi"
        cpu: "250m"

# Redis
redis:
  enabled: true
  auth:
    password: changeme
  master:
    resources:
      requests:
        memory: "128Mi"
        cpu: "100m"

# MinIO
minio:
  enabled: true
  auth:
    rootUser: admin
    rootPassword: changeme
```

---

## 安全架构设计

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

## 开发分支规范

```
main (生产分支)
  ├── release/2.0.0 (发布分支)
  ├── develop (开发主分支)
  │     ├── feature/fi-voucher-create (功能分支)
  │     ├── feature/sup-supplier-manage (功能分支)
  │     ├── fix/voucher-balance-bug (修复分支)
  │     └── feature/* (功能分支)
  │
  └── hotfix/voucher-post-error (紧急修复分支)
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
| **生产管理** | PP | 生产/生产 | 聚焦中小企业核心需求 |
| **人力资源** | HCM | 人力资源 | 聚焦中小企业核心需求 |

### 技术优势

| 维度 | SAP S/4HANA | NextERP v2.0 |
|------|-------------|-------------|
| **部署成本** | 高 (专用硬件 + 许可费) | 低 (通用服务器 + 开源) |
| **实施周期** | 6-18个月 | 1-3个月 |
| **定制开发** | 复杂 (需SAP认证) | 简单 (全开源) |
| **云原生** | 部分支持 | 完全云原生 |
| **SaaS** | 有限 | 完全多租户 |
| **中文支持** | 国际化版本 | 原生中文设计 |
| **运维复杂度** | 高 | 低 (模块化单体) |
| **事务一致性** | 分布式事务 | 本地事务 ACID |

---

## 路线规划

### Phase 1: 基础平台 + 财务模块 (6个月)

- [ ] 用户认证与授权
- [ ] 多租户管理
- [ ] 基础权限框架
- [ ] 工作流引擎
- [ ] 报表引擎
- [ ] 文件管理
- [ ] 消息通知
- [ ] 总账管理
- [ ] 应收应付
- [ ] 固定资产
- [ ] 资金管理
- [ ] 成本核算
- [ ] 财务报表

### Phase 2: 供应链 + 销售模块 (4个月)

- [ ] 采购管理
- [ ] 库存管理
- [ ] 仓库管理
- [ ] 供应商管理
- [ ] 销售订单
- [ ] 发货管理
- [ ] 客户管理
- [ ] 价格管理

### Phase 3: 生产与扩展 (持续)

- [ ] 生产管理
- [ ] 人力资源
- [ ] 项目管理
- [ ] 移动端应用
- [ ] 开放平台
- [ ] 报表服务微服务化 (按需)

---

## 附录

### A. 参考文档

- 《企业会计准则》财政部
- 《SAP S/4HANA 技术架构文档》
- 《云原生应用架构实践》
- 《模块化单体架构设计模式》

### B. 开源协议

本项目采用 **MIT** 开源协议，允许商业使用。

### C. 联系方式

- 项目地址: https://github.com/nexterp/nexterp
- 文档地址: https://docs.nexterp.com
- 技术交流: https://community.nexterp.com
