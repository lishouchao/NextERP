# NextERP 项目结构规划

## 一、总体布局

### 1.1 仓库策略

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        仓库架构选择                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  方案 A: 单体仓库 (Monorepo) - 推荐                                     │
│  ┌────────────────────────────────────────────────────────────────────┐│
│  │  Advantages:                                                        ││
│  │  - 统一代码审查和版本管理                                            ││
│  │  - 简化跨模块重构                                                    ││
│  │  - 统一 CI/CD 流程                                                   ││
│  │  - 共享配置和工具                                                    ││
│  │  - 适合模块化单体架构                                                ││
│  │                                                                     ││
│  │  Disadvantages:                                                     ││
│  │  - 仓库体积较大                                                      ││
│  │  - 需要良好的构建工具支持                                            ││
│  └────────────────────────────────────────────────────────────────────┘│
│                                                                         │
│  方案 B: 多仓库 (Multi-Repo)                                            │
│  ┌────────────────────────────────────────────────────────────────────┐│
│  │  Advantages:                                                        ││
│  │  - 仓库体积小                                                        ││
│  │  - 独立版本管理                                                      ││
│  │                                                                     ││
│  │  Disadvantages:                                                     ││
│  │  - 跨模块依赖管理复杂                                                ││
│  │  - 代码审查分散                                                      ││
│  │  - CI/CD 流程复杂                                                    ││
│  └────────────────────────────────────────────────────────────────────┘│
│                                                                         │
│  决策: 采用 Monorepo + 多模块构建                                       │
│  - 后端: Maven/Gradle 多模块                                            │
│  - 前端: pnpm Workspace + Turborepo                                    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 顶层目录结构

```
nexterp/                                    # 项目根目录
├── .github/                               # GitHub 配置
│   ├── workflows/                         # CI/CD 工作流
│   ├── ISSUE_TEMPLATE/                    # Issue 模板
│   └── PULL_REQUEST_TEMPLATE.md           # PR 模板
│
├── docs/                                  # 项目文档
│   ├── architecture/                      # 架构文档
│   ├── api/                               # API 文档
│   ├── user-guide/                        # 用户指南
│   └── development/                       # 开发文档
│
├── backend/                               # 后端项目 (Java/Spring Boot)
│   ├── .mvn/                              # Maven Wrapper
│   ├── pom.xml                            # 根 POM 文件
│   ├── nexterp-platform/                  # 平台层模块
│   ├── nexterp-business/                  # 业务层模块
│   ├── nexterp-shared/                    # 共享层模块
│   ├── nexterp-api/                       # API 网关模块
│   └── nexterp-assembly/                  # 部署装配模块
│
├── frontend/                              # 前端项目 (Next.js)
│   ├── package.json                       # 根 package.json
│   ├── pnpm-workspace.yaml                # pnpm workspace 配置
│   ├── turbo.json                         # Turborepo 配置
│   ├── apps/                              # 应用
│   │   ├── web/                           # Web 应用 (Next.js)
│   │   └── admin/                         # 管理后台 (Next.js)
│   ├── packages/                          # 共享包
│   │   ├── ui-components/                 # UI 组件库
│   │   ├── shared-types/                  # 共享类型
│   │   ├── shared-utils/                  # 共享工具
│   │   └── api-client/                    # API 客户端
│   └── config/                           # 共享配置
│
├── mobile/                                # 移动端项目 (Flutter)
│   ├── lib/                               # 源代码
│   └── pubspec.yaml                       # Flutter 配置
│
├── deploy/                                # 部署配置
│   ├── k8s/                               # Kubernetes 配置
│   ├── docker/                            # Docker 配置
│   ├── terraform/                         # Terraform 配置
│   └── scripts/                           # 部署脚本
│
├── tools/                                 # 开发工具
│   ├── code-generator/                    # 代码生成器
│   ├── db-migration/                      # 数据库迁移工具
│   └── api-doc-generator/                 # API 文档生成器
│
├── .gitignore                             # Git 忽略文件
├── .gitpod.yml                            # Gitpod 配置
├── LICENSE                                # 许可证
├── README.md                              # 项目说明
└── docker-compose.yml                     # 本地开发环境
```

---

## 二、后端项目结构 (Java Spring Boot)

### 2.1 Maven 多模块结构

```
backend/
├── pom.xml                                # 父 POM
│
├── nexterp-bom/                           # BOM (Bill of Materials)
│   └── pom.xml                            # 统一依赖版本管理
│
├── nexterp-platform/                      # 平台层 (技术底座)
│   ├── pom.xml                            # 平台层父 POM
│   │
│   ├── nexterp-platform-auth/             # 认证授权模块
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/nexterp/platform/auth/
│   │       │   │   ├── api/              # 对外 API
│   │       │   │   ├── domain/           # 领域模型
│   │       │   │   ├── application/      # 应用服务
│   │       │   │   ├── infrastructure/    # 基础设施
│   │       │   │   └── controller/       # REST 控制器
│   │       │   └── resources/
│   │       │       ├── application.yml   # 模块配置
│   │       │       └── db/migration/     # 数据库迁移
│   │       └── test/
│   │
│   ├── nexterp-platform-tenant/           # 多租户模块
│   │   └── ...                            # 类似结构
│   │
│   ├── nexterp-platform-workflow/         # 工作流模块
│   │   └── ...
│   │
│   ├── nexterp-platform-report/           # 报表模块
│   │   └── ...
│   │
│   └── nexterp-platform-notification/     # 通知模块
│       └── ...
│
├── nexterp-business/                      # 业务层
│   ├── pom.xml                            # 业务层父 POM
│   │
│   ├── nexterp-business-finance/          # 财务模块
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/nexterp/business/finance/
│   │       │   │   ├── api/              # 模块对外 API
│   │       │   │   │   ├── facade/       # 外观服务
│   │       │   │   │   ├── dto/          # 数据传输对象
│   │       │   │   │   └── vo/           # 视图对象
│   │       │   │   │
│   │       │   │   ├── domain/           # 领域层
│   │       │   │   │   ├── model/        # 聚合根
│   │       │   │   │   ├── repository/   # 仓储接口
│   │       │   │   │   └── service/      # 领域服务
│   │       │   │   │
│   │       │   │   ├── application/      # 应用层
│   │       │   │   │   ├── service/      # 应用服务
│   │       │   │   │   └── event/        # 领域事件
│   │       │   │   │
│   │       │   │   ├── infrastructure/    # 基础设施层
│   │       │   │   │   ├── repository/   # 仓储实现
│   │       │   │   │   ├── mapper/       # 数据映射
│   │       │   │   │   └── persistence/  # 持久化
│   │       │   │   │
│   │       │   │   └── integration/      # 集成接口
│   │       │   │       ├── event/        # 事件发布/订阅
│   │       │   │       └── remote/       # 远程调用
│   │       │   │
│   │       │   └── resources/
│   │       │           ├── application.yml
│   │       │           ├── db/migration/
│   │       │           └── i18n/          # 国际化
│   │       └── test/
│   │           ├── java/
│   │           │   ├── unit/             # 单元测试
│   │           │   └── integration/      # 集成测试
│   │           └── resources/
│   │
│   ├── nexterp-business-supply/           # 供应链模块
│   │   └── ...                            # 类似结构
│   │
│   ├── nexterp-business-sales/            # 销售模块
│   │   └── ...
│   │
│   ├── nexterp-business-production/       # 生产模块
│   │   └── ...
│   │
│   └── nexterp-business-hrm/              # 人力资源模块
│       └── ...
│
├── nexterp-shared/                        # 共享层
│   ├── pom.xml                            # 共享层父 POM
│   │
│   ├── nexterp-shared-core/               # 核心工具
│   │   ├── pom.xml
│   │   └── src/
│   │       └── main/java/com/nexterp/shared/core/
│   │           ├── util/                  # 工具类
│   │           ├── constant/              # 常量定义
│   │           ├── exception/             # 异常定义
│   │           ├── result/                # 统一返回结果
│   │           └── config/                # 通用配置
│   │
│   ├── nexterp-shared-data/               # 数据访问
│   │   ├── pom.xml
│   │   └── src/
│   │       └── main/java/com/nexterp/shared/data/
│   │           ├── audit/                 # 审计功能
│   │           ├── tenant/                # 租户支持
│   │           ├── soft-delete/           # 软删除
│   │           └── specification/         # 动态查询
│   │
│   └── nexterp-shared-security/           # 安全组件
│       ├── pom.xml
│       └── src/
│           └── main/java/com/nexterp/shared/security/
│               ├── annotation/            # 安全注解
│               ├── handler/               # 处理器
│               └── utils/                 # 工具类
│
├── nexterp-api/                           # API 网关
│   ├── pom.xml
│   │
│   └── nexterp-api-gateway/               # 网关实现
│       └── src/
│           ├── main/
│           │   ├── java/com/nexterp/api/gateway/
│           │   │   ├── config/            # 网关配置
│           │   │   ├── filter/            # 网关过滤器
│           │   │   └── controller/        # 网关控制器
│           │   └── resources/
│           │       └── application.yml
│           └── test/
│
├── nexterp-assembly/                      # 部署装配
│   ├── pom.xml
│   │
│   ├── nexterp-assembly-platform/         # 平台独立部署
│   │   ├── pom.xml
│   │   └── src/main/assembly/
│   │       └── assembly.xml
│   │
│   └── nexterp-assembly-full/             # 完整应用部署
│       ├── pom.xml
│       └── src/main/assembly/
│           ├── assembly.xml
│           └── scripts/                   # 启动脚本
│               ├── bin/start.sh
│               └── bin/start.bat
│
├── nexterp-starters/                      # Spring Boot Starters
│   ├── pom.xml
│   │
│   ├── nexterp-starter-auth/              # 认证 Starter
│   ├── nexterp-starter-tenant/            # 多租户 Starter
│   ├── nexterp-starter-data/              # 数据访问 Starter
│   └── nexterp-starter-web/               # Web Starter
│
└── code-quality/                          # 代码质量检查
    ├── checkstyle/                        # Checkstyle 配置
    ├── pmd/                               # PMD 配置
    ├── spotbugs/                          # SpotBugs 配置
    └── architecture/                      # 架构测试
        └── src/test/java/
            └── ArchitectureTest.java      # ArchUnit 测试
```

### 2.2 模块间依赖关系

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        模块依赖层次                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  nexterp-api-gateway                                                   │
│      │                                                                  │
│      ├──► nexterp-platform-*                                           │
│      │                                                                  │
│      └──► nexterp-business-*                                           │
│                                                                         │
│  nexterp-business-*                                                    │
│      │                                                                  │
│      ├──► nexterp-platform-*      (平台接口依赖)                         │
│      │                                                                  │
│      └──► nexterp-shared-*        (共享组件依赖)                         │
│                                                                         │
│  nexterp-platform-*                                                   │
│      │                                                                  │
│      └──► nexterp-shared-*        (共享组件依赖)                         │
│                                                                         │
│  nexterp-shared-*                                                      │
│      │                                                                  │
│      └──► (无业务依赖,仅依赖框架)                                         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

依赖规则:
1. 业务模块 (business) 可以依赖平台模块 (platform) 的接口
2. 业务模块不能直接依赖其他业务模块的实现
3. 业务模块间通过事件或接口进行集成
4. 平台模块间可以相互依赖,但需保持最小化
5. 共享模块 (shared) 不依赖任何业务模块
```

### 2.3 Java 包结构约定

```java
// ========== 领域模型包结构 ==========
/**
 * 财务模块包结构
 * 遵循 DDD (Domain-Driven Design) 分层
 */
package com.nexterp.business.finance;

/**
 * API 层 - 对外接口
 * 包含: Facade, DTO, VO
 */
package com.nexterp.business.finance.api;

/**
 * Facade - 外观服务
 * 模块对外提供的统一接口
 */
public interface FiVoucherFacade {
    FiVoucherVO getById(Long id);
    Long create(FiVoucherCreateDTO dto);
    void update(FiVoucherUpdateDTO dto);
    void delete(Long id);
    PageResult<FiVoucherVO> page(FiVoucherQueryDTO query);
}

/**
 * DTO - 数据传输对象
 * 用于接收外部请求数据
 */
public record FiVoucherCreateDTO(
    @NotNull String voucherDate,
    @NotNull String voucherType,
    @NotEmpty List<FiVoucherDetailCreateDTO> details
) {}

/**
 * VO - 视图对象
 * 用于返回数据给外部
 */
public record FiVoucherVO(
    Long id,
    String voucherNo,
    String voucherDate,
    String voucherType,
    BigDecimal debitAmount,
    BigDecimal creditAmount,
    String voucherStatus,
    List<FiVoucherDetailVO> details
) {}

/**
 * 领域层 - 核心业务逻辑
 * 包含: Model, Repository, Domain Service
 */
package com.nexterp.business.finance.domain;

/**
 * Aggregate Root - 聚合根
 * 领域模型的根实体
 */
@Entity
@Table(name = "fi_voucher")
public class FiVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String voucherNo;
    private LocalDate voucherDate;
    private String voucherType;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String voucherStatus;

    @Embedded
    private TenantInfo tenant;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "voucher")
    private List<FiVoucherDetail> details;

    /**
     * 领域行为
     */
    public void post() {
        validate();
        this.voucherStatus = "POSTED";
        // 发布领域事件
        DomainEventPublisher.publish(new FiVoucherPostedEvent(this.id));
    }

    public void unpost() {
        if (!"POSTED".equals(this.voucherStatus)) {
            throw new VoucherNotPostedException();
        }
        this.voucherStatus = "DRAFT";
    }

    private void validate() {
        if (debitAmount.compareTo(creditAmount) != 0) {
            throw new VoucherBalanceException();
        }
    }
}

/**
 * Repository Interface - 仓储接口
 * 领域层定义,基础设施层实现
 */
public interface FiVoucherRepository {
    FiVoucher save(FiVoucher voucher);
    Optional<FiVoucher> findById(Long id);
    Optional<FiVoucher> findByVoucherNo(String voucherNo);
    Page<FiVoucher> query(FiVoucherQuery query);
    void delete(Long id);
}

/**
 * Domain Service - 领域服务
 * 处理跨聚合根的业务逻辑
 */
@Service
@RequiredArgsConstructor
public class FiVoucherDomainService {
    private final FiVoucherRepository voucherRepository;
    private final FiAccountRepository accountRepository;

    public String generateVoucherNo(String voucherType, LocalDate voucherDate) {
        // 凭证号生成逻辑
        String prefix = voucherType + DateUtil.format(voucherDate, "yyyyMM");
        Long seq = voucherRepository.getNextSeq(prefix);
        return prefix + String.format("%06d", seq);
    }

    public void validateAccounts(List<FiVoucherDetail> details) {
        // 科目校验逻辑
    }
}

/**
 * 应用层 - 应用服务
 * 包含: Application Service, Event
 */
package com.nexterp.business.finance.application;

/**
 * Application Service - 应用服务
 * 编排领域对象完成业务用例
 */
@Service
@RequiredArgsConstructor
public class FiVoucherApplicationService {

    private final FiVoucherRepository voucherRepository;
    private final FiVoucherDomainService voucherDomainService;

    @Transactional
    public Long createVoucher(FiVoucherCreateDTO dto) {
        // 1. 构建领域对象
        FiVoucher voucher = buildVoucher(dto);

        // 2. 调用领域服务
        String voucherNo = voucherDomainService.generateVoucherNo(
            dto.voucherType(),
            dto.voucherDate()
        );
        voucher.setVoucherNo(voucherNo);

        // 3. 保存
        return voucherRepository.save(voucher).getId();
    }

    @Transactional
    public void postVoucher(Long voucherId) {
        FiVoucher voucher = voucherRepository.findById(voucherId)
            .orElseThrow(() -> new NotFoundException());

        voucher.post();  // 领域行为
        voucherRepository.save(voucher);
    }
}

/**
 * 领域事件
 */
public record FiVoucherCreatedEvent(
    Long voucherId,
    String voucherNo,
    LocalDate voucherDate,
    BigDecimal amount
) implements DomainEvent {}

/**
 * 基础设施层 - 技术实现
 * 包含: Repository Impl, Mapper, Persistence
 */
package com.nexterp.business.finance.infrastructure;

/**
 * Repository Implementation - 仓储实现
 */
@Repository
@RequiredArgsConstructor
public class FiVoucherRepositoryImpl implements FiVoucherRepository {

    private final FiVoucherJpaRepository jpaRepository;
    private final FiVoucherMapper mapper;

    @Override
    public FiVoucher save(FiVoucher voucher) {
        FiVoucherEntity entity = mapper.toEntity(voucher);
        entity = jpaRepository.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<FiVoucher> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
}

/**
 * JPA Entity - 持久化实体
 */
@Entity
@Table(name = "fi_voucher")
public class FiVoucherEntity {
    @Id
    private Long id;
    private String voucherNo;
    private LocalDate voucherDate;
    // ... 字段映射
}

/**
 * Mapper - 领域对象与持久化对象映射
 */
@Component
public class FiVoucherMapper {
    public FiVoucher toDomain(FiVoucherEntity entity) {
        // 映射逻辑
    }

    public FiVoucherEntity toEntity(FiVoucher domain) {
        // 映射逻辑
    }
}

/**
 * 集成层 - 模块集成
 * 包含: Event Publisher, Event Listener, Remote Client
 */
package com.nexterp.business.finance.integration;

/**
 * Event Listener - 事件监听器
 * 监听其他模块发布的事件
 */
@Component
@RequiredArgsConstructor
public class SalesOrderFinanceListener {

    @EventListener
    @Async("financeEventExecutor")
    public void handleSalesOrderCreated(SalesOrderCreatedEvent event) {
        // 处理销售订单创建事件
        // 生成应收账款记录等
    }
}
```

---

## 三、前端项目结构 (Next.js)

### 3.1 Monorepo 结构 (pnpm workspace)

```
frontend/
├── package.json                           # 根 package.json
├── pnpm-workspace.yaml                    # Workspace 配置
├── turbo.json                             # Turborepo 配置
├── next.config.js                         # Next.js 配置
├── tsconfig.json                          # TypeScript 配置
├── .eslintrc.js                           # ESLint 配置
├── .prettierrc                            # Prettier 配置
├── .npmrc                                 # npm 配置
│
├── apps/                                  # 应用目录
│   ├── web/                               # Web 应用 (面向终端用户)
│   │   ├── package.json
│   │   ├── next.config.js
│   │   ├── tailwind.config.js
│   │   ├── tsconfig.json
│   │   │
│   │   ├── src/
│   │   │   ├── app/                       # Next.js App Router
│   │   │   │   ├── layout.tsx             # 根布局
│   │   │   │   ├── page.tsx               # 首页
│   │   │   │   ├── globals.css            # 全局样式
│   │   │   │   │
│   │   │   │   ├── (auth)/                # 认证路由组
│   │   │   │   │   ├── login/
│   │   │   │   │   │   └── page.tsx
│   │   │   │   │   └── layout.tsx
│   │   │   │   │
│   │   │   │   ├── (main)/                # 主应用路由组
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   └── page.tsx
│   │   │   │   │   │
│   │   │   │   │   ├── finance/           # 财务模块
│   │   │   │   │   │   ├── vouchers/
│   │   │   │   │   │   │   ├── page.tsx   # 凭证列表
│   │   │   │   │   │   │   ├── [id]/
│   │   │   │   │   │   │   │   └── page.tsx  # 凭证详情
│   │   │   │   │   │   │   └── layout.tsx
│   │   │   │   │   │   └── accounts/
│   │   │   │   │   │       └── page.tsx
│   │   │   │   │   │
│   │   │   │   │   ├── supply/            # 供应链模块
│   │   │   │   │   │   ├── purchase/
│   │   │   │   │   │   └── inventory/
│   │   │   │   │   │
│   │   │   │   │   ├── sales/             # 销售模块
│   │   │   │   │   │   ├── orders/
│   │   │   │   │   │   └── delivery/
│   │   │   │   │   │
│   │   │   │   │   └── layout.tsx         # 主应用布局
│   │   │   │   │
│   │   │   │   └── api/                   # API Routes (可选)
│   │   │   │       └── proxy/             # 代理后端 API
│   │   │   │
│   │   │   ├── components/                # 页面组件
│   │   │   │   ├── finance/               # 财务组件
│   │   │   │   │   ├── voucher-form.tsx
│   │   │   │   │   ├── voucher-table.tsx
│   │   │   │   │   └── account-selector.tsx
│   │   │   │   │
│   │   │   │   ├── shared/                # 共享组件
│   │   │   │   │   ├── page-header.tsx
│   │   │   │   │   ├── data-table.tsx
│   │   │   │   │   └── search-form.tsx
│   │   │   │   │
│   │   │   │   └── layout/                # 布局组件
│   │   │   │       ├── sidebar.tsx
│   │   │   │       ├── header.tsx
│   │   │   │       └── footer.tsx
│   │   │   │
│   │   │   ├── hooks/                     # 自定义 Hooks
│   │   │   │   ├── api/                   # API Hooks
│   │   │   │   │   ├── finance/
│   │   │   │   │   │   ├── voucher.ts
│   │   │   │   │   │   └── account.ts
│   │   │   │   │   └── supply/
│   │   │   │   │
│   │   │   │   ├── use-table.ts           # 表格 Hook
│   │   │   │   ├── use-form.ts            # 表单 Hook
│   │   │   │   └── use-modal.ts           # 弹窗 Hook
│   │   │   │
│   │   │   ├── lib/                       # 工具库
│   │   │   │   ├── api/                   # API 客户端
│   │   │   │   │   ├── client.ts         # 基础客户端
│   │   │   │   │   └── finance/
│   │   │   │   │       └── voucher.ts
│   │   │   │   │
│   │   │   │   ├── fetcher.ts             # SWR fetcher
│   │   │   │   └── format.ts              # 格式化工具
│   │   │   │
│   │   │   ├── stores/                    # 状态管理 (Zustand)
│   │   │   │   ├── auth.ts
│   │   │   │   ├── finance/
│   │   │   │   │   └── voucher.ts
│   │   │   │   └── ui.ts
│   │   │   │
│   │   │   ├── types/                     # TypeScript 类型
│   │   │   │   ├── finance/
│   │   │   │   │   ├── voucher.ts
│   │   │   │   │   └── account.ts
│   │   │   │   ├── supply/
│   │   │   │   │   ├── purchase.ts
│   │   │   │   │   └── inventory.ts
│   │   │   │   └── common.ts
│   │   │   │
│   │   │   └── styles/                    # 样式文件
│   │   │       └── globals.css
│   │   │
│   │   ├── public/                        # 静态资源
│   │   │   ├── icons/
│   │   │   ├── images/
│   │   │   └── locales/                   # 国际化文件
│   │   │       ├── zh-CN.json
│   │   │       └── en-US.json
│   │   │
│   │   └── tests/                         # 测试文件
│   │       ├── unit/
│   │       └── e2e/
│   │
│   └── admin/                             # 管理后台 (面向管理员)
│       └── ...                            # 类似结构
│
├── packages/                              # 共享包
│   │
│   ├── ui-components/                     # UI 组件库
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   ├── tailwind.config.js
│   │   │
│   │   └── src/
│   │       ├── components/                # 组件
│   │       │   ├── button/
│   │       │   │   ├── Button.tsx
│   │       │   │   ├── Button.test.tsx
│   │       │   │   └── index.ts
│   │       │   │
│   │       │   ├── table/
│   │       │   ├── form/
│   │       │   ├── modal/
│   │       │   └── ...
│   │       │
│   │       ├── styles/                    # 样式
│   │       │   └── index.css
│   │       │
│   │       └── index.ts                   # 导出入口
│   │
│   ├── shared-types/                      # 共享类型
│   │   ├── package.json
│   │   └── src/
│   │       ├── api/                       # API 类型
│   │       │   ├── request.ts
│   │       │   └── response.ts
│   │       ├── models/                    # 数据模型
│   │       │   ├── user.ts
│   │       │   └── tenant.ts
│   │       └── index.ts
│   │
│   ├── shared-utils/                      # 共享工具
│   │   ├── package.json
│   │   └── src/
│   │       ├── date.ts                    # 日期工具
│   │       ├── number.ts                  # 数字工具
│   │       ├── string.ts                  # 字符串工具
│   │       ├── validate.ts                # 验证工具
│   │       └── index.ts
│   │
│   └── api-client/                        # API 客户端
│       ├── package.json
│       └── src/
│           ├── client.ts                  # 基础客户端
│           ├── request.ts                 # 请求封装
│           ├── response.ts                # 响应处理
│           ├── error.ts                   # 错误处理
│           └── index.ts
│
└── config/                                # 共享配置
    ├── tailwind/
    │   └── base.config.js                 # 基础 Tailwind 配置
    ├── tsconfig/
    │   └── base.json                      # 基础 TypeScript 配置
    └── nextjs/
        └── base.config.js                 # 基础 Next.js 配置
```

### 3.2 前端目录约定

```typescript
// ========== 目录命名约定 ==========
/**
 * 1. 目录名使用小写字母和连字符 (kebab-case)
 *    示例: voucher-form, data-table
 *
 * 2. 组件文件名使用 PascalCase
 *    示例: VoucherForm.tsx, DataTable.tsx
 *
 * 3. Hook 文件名使用 use 前缀
 *    示例: useVoucher.ts, useTable.ts
 *
 * 4. 工具函数文件名使用小写
 *    示例: format.ts, validate.ts
 *
 * 5. 类型文件名与功能同名
 *    示例: voucher.ts (types/finance/voucher.ts)
 *
 * 6. 测试文件与源文件同名
 *    示例: VoucherForm.test.tsx
 */

// ========== 文件组织示例 ==========
/**
 * 财务凭证模块文件组织
 */

// types/finance/voucher.ts - 类型定义
export interface Voucher {
  id: number
  voucherNo: string
  voucherDate: string
  voucherType: VoucherType
  status: VoucherStatus
  debitAmount: number
  creditAmount: number
  details: VoucherDetail[]
  createdAt: string
  createdBy: string
}

export enum VoucherType {
  RECEIPT = 'RECEIPT',
  PAYMENT = 'PAYMENT',
  TRANSFER = 'TRANSFER'
}

export enum VoucherStatus {
  DRAFT = 'DRAFT',
  REVIEW = 'REVIEW',
  POSTED = 'POSTED'
}

export interface VoucherQueryParams extends PageParams {
  voucherNo?: string
  voucherType?: VoucherType
  status?: VoucherStatus
  startDate?: string
  endDate?: string
}

// hooks/api/finance/voucher.ts - API Hooks
import useSWR from 'swr'
import useSWRMutation from 'swr/mutation'
import { voucherApi } from '@/lib/api/finance/voucher'

export function useVouchers(params: VoucherQueryParams) {
  return useSWR(
    ['vouchers', params],
    () => voucherApi.page(params)
  )
}

export function useVoucher(id: number) {
  return useSWR(
    ['voucher', id],
    () => voucherApi.getById(id)
  )
}

export function useCreateVoucher() {
  return useSWRMutation(
    'voucher-create',
    (_, { arg }: { arg: VoucherCreateForm }) => voucherApi.create(arg)
  )
}

export function usePostVoucher() {
  return useSWRMutation(
    'voucher-post',
    (_, { arg }: { arg: number }) => voucherApi.post(arg)
  )
}

// lib/api/finance/voucher.ts - API 调用
import { fetcher } from '@/lib/fetcher'
import type {
  Voucher,
  VoucherQueryParams,
  VoucherCreateForm,
  PageResult
} from '@/types/finance/voucher'

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

// stores/finance/voucher.ts - 状态管理
import { create } from 'zustand'

interface VoucherStore {
  selectedIds: number[]
  setSelectedIds: (ids: number[]) => void
  toggleSelection: (id: number) => void
  clearSelection: () => void
}

export const useVoucherStore = create<VoucherStore>((set) => ({
  selectedIds: [],
  setSelectedIds: (ids) => set({ selectedIds: ids }),
  toggleSelection: (id) => set((state) => ({
    selectedIds: state.selectedIds.includes(id)
      ? state.selectedIds.filter(i => i !== id)
      : [...state.selectedIds, id]
  })),
  clearSelection: () => set({ selectedIds: [] })
}))
```

### 3.3 工作空间配置

```yaml
# pnpm-workspace.yaml
packages:
  - 'apps/*'
  - 'packages/*'
```

```json
// turbo.json
{
  "$schema": "https://turbo.build/schema.json",
  "globalDependencies": ["**/.env.*local"],
  "pipeline": {
    "build": {
      "dependsOn": ["^build"],
      "outputs": [".next/**", "!.next/cache/**", "dist/**"]
    },
    "dev": {
      "cache": false,
      "persistent": true
    },
    "lint": {
      "dependsOn": ["^lint"]
    },
    "test": {
      "dependsOn": ["^build"],
      "outputs": ["coverage/**"]
    }
  }
}
```

---

## 四、移动端项目结构 (Flutter)

```
mobile/
├── lib/                                   # 源代码
│   ├── main.dart                          # 应用入口
│   │
│   ├── core/                              # 核心层
│   │   ├── constants/                     # 常量定义
│   │   │   ├── app_constants.dart
│   │   │   └── api_constants.dart
│   │   ├── theme/                         # 主题配置
│   │   │   ├── app_theme.dart
│   │   │   ├── light_theme.dart
│   │   │   └── dark_theme.dart
│   │   ├── router/                        # 路由配置
│   │   │   ├── app_router.dart
│   │   │   └── routes.dart
│   │   ├── network/                       # 网络层
│   │   │   ├── api_client.dart
│   │   │   ├── api_interceptor.dart
│   │   │   └── api_response.dart
│   │   ├── storage/                       # 本地存储
│   │   │   ├── storage_service.dart
│   │   │   └── secure_storage.dart
│   │   └── utils/                         # 工具类
│   │       ├── date_utils.dart
│   │       ├── number_utils.dart
│   │       └── validators.dart
│   │
│   ├── data/                              # 数据层
│   │   ├── models/                        # 数据模型
│   │   │   ├── user.dart
│   │   │   ├── voucher.dart
│   │   │   └── response.dart
│   │   ├── repositories/                  # 仓储实现
│   │   │   ├── auth_repository.dart
│   │   │   ├── voucher_repository.dart
│   │   │   └── api_repository.dart
│   │   └── datasources/                   # 数据源
│   │       ├── remote/                    # 远程数据源
│   │       │   ├── auth_api.dart
│   │       │   └── voucher_api.dart
│   │       └── local/                     # 本地数据源
│   │           ├── auth_dao.dart
│   │           └── app_database.dart
│   │
│   ├── domain/                            # 领域层
│   │   ├── entities/                      # 领域实体
│   │   │   ├── user_entity.dart
│   │   │   └── voucher_entity.dart
│   │   ├── repositories/                  # 仓储接口
│   │   │   ├── auth_repository.dart
│   │   │   └── voucher_repository.dart
│   │   └── usecases/                      # 用例
│   │       ├── auth/
│   │       │   ├── login_usecase.dart
│   │       │   └── logout_usecase.dart
│   │       └── voucher/
│   │           ├── get_vouchers_usecase.dart
│   │           └── create_voucher_usecase.dart
│   │
│   ├── presentation/                      # 表现层
│   │   ├── pages/                         # 页面
│   │   │   ├── auth/
│   │   │   │   ├── login_page.dart
│   │   │   │   └── splash_page.dart
│   │   │   ├── home/
│   │   │   │   └── home_page.dart
│   │   │   └── finance/
│   │   │       ├── voucher_list_page.dart
│   │   │       └── voucher_detail_page.dart
│   │   │
│   │   ├── widgets/                       # 通用组件
│   │   │   ├── app_button.dart
│   │   │   ├── app_input.dart
│   │   │   └── loading_widget.dart
│   │   │
│   │   ├── providers/                     # 状态管理 (Provider/Riverpod)
│   │   │   ├── auth_provider.dart
│   │   │   └── voucher_provider.dart
│   │   │
│   │   └── dialogs/                       # 对话框
│   │       ├── confirm_dialog.dart
│   │       └── error_dialog.dart
│   │
│   └── l10n/                              # 国际化
│       ├── app_en.arb
│       └── app_zh.arb
│
├── android/                               # Android 项目
├── ios/                                   # iOS 项目
├── test/                                  # 测试文件
├── pubspec.yaml                           # 依赖配置
└── README.md
```

---

## 五、部署项目结构

### 5.1 Kubernetes 配置

```
deploy/k8s/
├── base/                                  # 基础配置
│   ├── namespace.yaml
│   ├── configmap/
│   │   ├── app-config.yaml
│   │   └── log-config.yaml
│   ├── secret/
│   │   ├── db-secret.yaml
│   │   └── api-secret.yaml
│   └── pvc/
│       ├── data-pvc.yaml
│       └── log-pvc.yaml
│
├── overlays/                              # 环境配置
│   ├── dev/                               # 开发环境
│   │   ├── kustomization.yaml
│   │   ├── deployment/
│   │   │   ├── backend-deployment.yaml
│   │   │   ├── frontend-deployment.yaml
│   │   │   └── ingress.yaml
│   │   └── service/
│   │       ├── backend-service.yaml
│   │       └── frontend-service.yaml
│   │
│   ├── staging/                           # 测试环境
│   │   └── ...
│   │
│   └── prod/                              # 生产环境
│       └── ...
│
├── helm/                                  # Helm Charts
│   └── nexterp/
│       ├── Chart.yaml
│       ├── values.yaml
│       ├── values-dev.yaml
│       ├── values-staging.yaml
│       ├── values-prod.yaml
│       └── templates/
│           ├── deployment.yaml
│           ├── service.yaml
│           ├── ingress.yaml
│           ├── configmap.yaml
│           └── secret.yaml
│
└── scripts/                               # 部署脚本
    ├── deploy.sh
    ├── rollback.sh
    └── backup.sh
```

### 5.2 Docker 配置

```
deploy/docker/
├── backend/                               # 后端镜像
│   ├── Dockerfile
│   ├── Dockerfile.jdk17
│   └── .dockerignore
│
├── frontend/                              # 前端镜像
│   ├── Dockerfile
│   ├── Dockerfile.standalone
│   └── .dockerignore
│
└── docker-compose.yml                     # 本地开发环境
```

### 5.3 Terraform 配置

```
deploy/terraform/
├── modules/                               # 模块
│   ├── vpc/                               # 网络配置
│   ├── rds/                               # 数据库
│   ├── redis/                             # 缓存
│   └── eks/                               # Kubernetes 集群
│
├── environments/                          # 环境配置
│   ├── dev/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   ├── staging/
│   └── prod/
│
└── scripts/                               # 脚本
    ├── init.sh
    └── apply.sh
```

---

## 六、开发工具结构

```
tools/
├── code-generator/                        # 代码生成器
│   ├── templates/                         # 模板
│   │   ├── backend/
│   │   │   ├── module-template.java
│   │   │   ├── facade-template.java
│   │   │   └── service-template.java
│   │   └── frontend/
│   │       ├── page-template.tsx
│   │       └── component-template.tsx
│   ├── config/                            # 配置
│   │   └── generator-config.json
│   └── src/                               # 生成器源码
│
├── db-migration/                          # 数据库迁移工具
│   ├── migrations/                        # 迁移脚本
│   │   ├── V1__init_schema.sql
│   │   ├── V2__create_fi_tables.sql
│   │   └── V3__create_sd_tables.sql
│   └── seeds/                             # 种子数据
│       ├── base_data.sql
│       └── demo_data.sql
│
├── api-doc-generator/                    # API 文档生成器
│   ├── config/
│   │   └── swagger-config.json
│   └── templates/
│       └── api-doc-template.md
│
└── performance-testing/                   # 性能测试
    ├── jmeter/                            # JMeter 脚本
    │   ├── scenarios/
    │   │   ├── voucher_test.jmx
    │   │   └── order_test.jmx
    │   └── config/
    │       └── test-plan.properties
    └── k6/                                # K6 脚本
        ├── scenarios/
        │   └── load-test.js
        └── config/
            └── k6.config.js
```

---

## 七、配置文件规范

### 7.1 Git 配置

```bash
# .gitignore
# Java
*.class
*.jar
*.war
target/
.mvn/

# IDE
.idea/
*.iml
.vscode/
*.swp
*.swo

# Node
node_modules/
.next/
out/
dist/
.turbo/

# Environment
.env
.env.*.local

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db
```

### 7.2 Maven 配置

```xml
<!-- pom.xml 根 POM -->
<project>
    <groupId>com.nexterp</groupId>
    <artifactId>nexterp-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <!-- BOM -->
        <module>nexterp-bom</module>

        <!-- Platform -->
        <module>nexterp-platform/nexterp-platform-auth</module>
        <module>nexterp-platform/nexterp-platform-tenant</module>
        <module>nexterp-platform/nexterp-platform-workflow</module>
        <module>nexterp-platform/nexterp-platform-report</module>
        <module>nexterp-platform/nexterp-platform-notification</module>

        <!-- Business -->
        <module>nexterp-business/nexterp-business-finance</module>
        <module>nexterp-business/nexterp-business-supply</module>
        <module>nexterp-business/nexterp-business-sales</module>
        <module>nexterp-business/nexterp-business-production</module>
        <module>nexterp-business/nexterp-business-hrm</module>

        <!-- Shared -->
        <module>nexterp-shared/nexterp-shared-core</module>
        <module>nexterp-shared/nexterp-shared-data</module>
        <module>nexterp-shared/nexterp-shared-security</module>

        <!-- API -->
        <module>nexterp-api/nexterp-api-gateway</module>

        <!-- Assembly -->
        <module>nexterp-assembly/nexterp-assembly-full</module>

        <!-- Starters -->
        <module>nexterp-starters/nexterp-starter-auth</module>
        <module>nexterp-starters/nexterp-starter-tenant</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <!-- Spring Boot -->
        <spring-boot.version>3.2.0</spring-boot.version>

        <!-- Dependency Versions -->
        <springdoc.version>2.3.0</springdoc.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <liquibase.version>4.25.0</liquibase.version>
        <archunit.version>1.2.1</archunit.version>
    </properties>
</project>
```

### 7.3 前端配置

```json
// package.json 根配置
{
  "name": "nexterp-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "turbo run dev",
    "build": "turbo run build",
    "lint": "turbo run lint",
    "test": "turbo run test",
    "clean": "turbo run clean"
  },
  "devDependencies": {
    "@types/node": "^20.10.0",
    "typescript": "^5.3.0",
    "turbo": "^1.11.0",
    "prettier": "^3.1.0",
    "eslint": "^8.55.0"
  },
  "engines": {
    "node": ">=20.0.0",
    "pnpm": ">=8.0.0"
  },
  "packageManager": "pnpm@8.12.0"
}
```

---

## 八、模块边界控制

### 8.1 ArchUnit 架构测试

```java
// code-quality/architecture/src/test/java/ArchitectureTest.java
@AnalyzeClasses(packages = "com.nexterp")
public class ArchitectureTest {

    // ========== 平台层规则 ==========
    @ArchTest
    static final ArchRule platform_module_isolation =
        classes()
            .that().resideInAPackage("..platform..")
            .should().onlyBeAccessed()
            .byAnyPackage(
                "..platform..",
                "..shared..",
                "..api.."
            );

    // ========== 业务层规则 ==========
    @ArchTest
    static final ArchRule business_module_isolation =
        classes()
            .that().resideInAPackage("..business..")
            .should().onlyBeAccessed()
            .byAnyPackage(
                "..business..",
                "..platform..api..",
                "..shared..",
                "..api.."
            );

    // ========== 业务模块间隔离 ==========
    @ArchTest
    static final ArchRule finance_module_rule =
        classes()
            .that().resideInAPackage("..business.finance..")
            .should().onlyBeAccessed()
            .byAnyPackage(
                "..business.finance..",
                "..platform..api..",
                "..shared.."
            );

    @ArchTest
    static final ArchRule supply_module_rule =
        classes()
            .that().resideInAPackage("..business.supply..")
            .should().onlyBeAccessed()
            .byAnyPackage(
                "..business.supply..",
                "..platform..api..",
                "..shared.."
            );

    // ========== 共享层规则 ==========
    @ArchTest
    static final ArchRule shared_should_not_depend_on_business =
        noClasses()
            .that().resideInAPackage("..shared..")
            .should().dependOnClassesThat()
            .resideInAPackage("..business..");

    // ========== 控制器规则 ==========
    @ArchTest
    static final ArchRule controllers_should_only_reside_in_api_or_controller =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAnyPackage(
                "..api..controller..",
                "..platform..controller..",
                "..business..api..controller.."
            );

    // ========== 仓储规则 ==========
    @ArchTest
    static final ArchRule repositories_should_only_be_accessed_by_service =
        classes()
            .that().areAssignableTo(Repository.class)
            .should().onlyBeAccessed()
            .byAnyPackage(
                "..application..",
                "..domain..",
                "..infrastructure.."
            );

    // ========== 领域事件规则 ==========
    @ArchTest
    static final ArchRule events_should_be_in_domain_or_application =
        classes()
            .that().areAssignableTo(DomainEvent.class)
            .should().resideInAnyPackage(
                "..domain..event..",
                "..application..event.."
            );
}
```

---

## 九、开发工作流

### 9.1 分支策略

```
main (生产)
  │
  ├─ develop (开发主分支)
  │    │
  │    ├─ feature/xxx (功能分支)
  │    │
  │    ├─ bugfix/xxx (缺陷修复)
  │    │
  │    ├─ hotfix/xxx (紧急修复)
  │    │
  │    └─ release/x.x.x (发布分支)
  │
  └─ tags/v1.0.0 (版本标签)
```

### 9.2 提交规范

```
<type>(<scope>): <subject>

<body>

<footer>

# Type 类型
feat: 新功能
fix: 缺陷修复
docs: 文档更新
style: 代码格式
refactor: 重构
perf: 性能优化
test: 测试相关
chore: 构建/工具

# Scope 范围
platform: 平台层模块
finance: 财务模块
supply: 供应链模块
sales: 销售模块
auth: 认证模块
tenant: 多租户模块
ui: 前端组件
api: API 网关

# 示例
feat(finance): 添加凭证批量过账功能

- 实现批量选择功能
- 添加批量过账接口
- 添加权限检查

Closes #123
```

---

## 十、总结

本项目结构遵循以下核心原则：

1. **模块化单体架构**: 单一部署单元，严格模块边界
2. **分层架构**: Platform → Business → Shared 清晰分层
3. **DDD 领域驱动**: API → Application → Domain → Infrastructure
4. **依赖倒置**: 上层依赖下层接口，不依赖实现
5. **事件驱动**: 模块间通过领域事件解耦
6. **接口隔离**: 通过 Facade 和 SPI 实现模块隔离
7. **可测试性**: 每层独立可测
8. **可扩展性**: 预留微服务拆分空间
