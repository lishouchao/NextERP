# NextERP 项目初始化文档

## 项目创建时间

2025-01-15

## 项目概述

NextERP 是基于模块化单体架构的国产 ERP 系统，采用以下技术栈：

| 技术栈 | 版本 | 说明 |
|--------|------|------|
| 后端语言 | Java 21 | LTS 版本 |
| 后端框架 | Spring Boot 3.2.0 | 最新稳定版 |
| 前端框架 | Next.js 14.0.4 | App Router |
| 前端语言 | TypeScript 5.3.3 | 类型安全 |
| 构建工具 | Maven 3.9+ / pnpm 8+ | 后端/前端 |
| 数据库 | PostgreSQL 16 | 生产环境 |
| 缓存 | Redis 7 | 缓存与会话 |
| 消息队列 | RabbitMQ 3 | 异步处理 |

## 目录结构

```
nexterp/                                    # 项目根目录
├── backend/                               # 后端项目 (Maven 多模块)
│   ├── pom.xml                            # 父 POM
│   │
│   ├── nexterp-bom/                       # BOM 模块
│   │   └── pom.xml
│   │
│   ├── nexterp-platform/                  # 平台层模块
│   │   ├── pom.xml
│   │   ├── nexterp-platform-auth/         # 认证授权
│   │   │   ├── pom.xml
│   │   │   └── src/{main,test}/{java,resources}
│   │   │       └── java/com/nexterp/platform/auth/
│   │   │           ├── api/
│   │   │           ├── domain/
│   │   │           ├── application/
│   │   │           ├── infrastructure/
│   │   │           └── controller/
│   │   │
│   │   ├── nexterp-platform-tenant/       # 多租户
│   │   │   └── ...
│   │   ├── nexterp-platform-workflow/     # 工作流
│   │   │   └── ...
│   │   ├── nexterp-platform-report/       # 报表
│   │   │   └── ...
│   │   └── nexterp-platform-notification/ # 通知
│   │       └── ...
│   │
│   ├── nexterp-business/                  # 业务层模块
│   │   ├── pom.xml
│   │   ├── nexterp-business-finance/      # 财务模块
│   │   │   ├── pom.xml
│   │   │   └── src/{main,test}/{java,resources}
│   │   │       └── java/com/nexterp/business/finance/
│   │   │           ├── api/              # Facade, DTO, VO
│   │   │           │   ├── facade/
│   │   │           │   ├── dto/
│   │   │           │   └── vo/
│   │   │           ├── domain/           # 领域层
│   │   │           │   ├── model/        # 聚合根
│   │   │           │   ├── repository/   # 仓储接口
│   │   │           │   └── service/      # 领域服务
│   │   │           ├── application/      # 应用层
│   │   │           │   ├── service/
│   │   │           │   └── event/        # 领域事件
│   │   │           ├── infrastructure/    # 基础设施层
│   │   │           │   ├── repository/
│   │   │           │   ├── mapper/
│   │   │           │   └── persistence/
│   │   │           └── integration/      # 集成层
│   │   │               ├── event/
│   │   │               └── remote/
│   │   │
│   │   ├── nexterp-business-supply/       # 供应链模块
│   │   │   └── ...
│   │   ├── nexterp-business-sales/        # 销售模块
│   │   │   └── ...
│   │   ├── nexterp-business-production/   # 生产模块
│   │   │   └── ...
│   │   └── nexterp-business-hrm/          # 人力资源模块
│   │       └── ...
│   │
│   ├── nexterp-shared/                    # 共享层模块
│   │   ├── pom.xml
│   │   ├── nexterp-shared-core/           # 核心工具
│   │   │   ├── pom.xml
│   │   │   └── src/{main,test}/{java,resources}
│   │   │       └── java/com/nexterp/shared/core/
│   │   │           ├── util/
│   │   │           ├── constant/
│   │   │           ├── exception/
│   │   │           ├── result/
│   │   │           └── config/
│   │   │
│   │   ├── nexterp-shared-data/           # 数据访问
│   │   │   └── ...
│   │   └── nexterp-shared-security/       # 安全组件
│   │       └── ...
│   │
│   ├── nexterp-api/                       # API 网关
│   │   ├── pom.xml
│   │   └── nexterp-api-gateway/
│   │       └── src/{main,test}/{java,resources}
│   │
│   ├── nexterp-assembly/                  # 部署装配
│   │   ├── pom.xml
│   │   ├── nexterp-assembly-platform/     # 平台独立部署
│   │   │   ├── pom.xml
│   │   │   └── src/main/assembly/assembly.xml
│   │   └── nexterp-assembly-full/         # 完整应用部署
│   │       ├── pom.xml
│   │       ├── src/main/assembly/assembly.xml
│   │       └── src/main/scripts/bin/
│   │           ├── start.sh
│   │           └── start.bat
│   │
│   ├── nexterp-starters/                  # Spring Boot Starters
│   │   ├── pom.xml
│   │   ├── nexterp-starter-auth/
│   │   ├── nexterp-starter-tenant/
│   │   ├── nexterp-starter-data/
│   │   └── nexterp-starter-web/
│   │
│   └── code-quality/                      # 代码质量检查
│       ├── checkstyle/checkstyle.xml
│       ├── pmd/pmd.xml
│       ├── spotbugs/
│       └── architecture/src/test/java/
│           └── ArchitectureTest.java     # ArchUnit 架构测试
│
├── frontend/                              # 前端项目 (Next.js)
│   ├── package.json                       # 根配置
│   ├── pnpm-workspace.yaml                # Workspace 配置
│   ├── turbo.json                         # Turborepo 配置
│   ├── tsconfig.json                      # TypeScript 配置
│   ├── .eslintrc.js                       # ESLint 配置
│   ├── .prettierrc                        # Prettier 配置
│   │
│   ├── apps/                              # 应用目录
│   │   ├── web/                           # Web 应用 (终端用户)
│   │   │   ├── package.json
│   │   │   ├── next.config.js
│   │   │   ├── tsconfig.json
│   │   │   ├── tailwind.config.js
│   │   │   └── src/
│   │   │       ├── app/                   # App Router
│   │   │       │   ├── layout.tsx
│   │   │       │   ├── page.tsx
│   │   │       │   ├── globals.css
│   │   │       │   ├── (auth)/           # 认证路由组
│   │   │       │   │   ├── login/
│   │   │       │   │   └── layout.tsx
│   │   │       │   └── (main)/           # 主应用路由组
│   │   │       │       ├── dashboard/
│   │   │       │       ├── finance/      # 财务模块
│   │   │       │       │   ├── vouchers/
│   │   │       │       │   └── accounts/
│   │   │       │       ├── supply/       # 供应链模块
│   │   │       │       └── sales/        # 销售模块
│   │   │       ├── components/           # 页面组件
│   │   │       ├── hooks/                # 自定义 Hooks
│   │   │       │   ├── api/              # API Hooks
│   │   │       │   └── use-table.ts
│   │   │       ├── lib/                  # 工具库
│   │   │       │   ├── api/              # API 客户端
│   │   │       │   ├── fetcher.ts        # SWR fetcher
│   │   │       │   └── format.ts
│   │   │       ├── stores/               # 状态管理 (Zustand)
│   │   │       ├── types/                # TypeScript 类型
│   │   │       └── styles/               # 样式文件
│   │   │
│   │   └── admin/                         # 管理后台 (管理员)
│   │       └── ...                        # 类似结构
│   │
│   └── packages/                          # 共享包
│       ├── ui-components/                 # UI 组件库
│       │   ├── package.json
│       │   └── src/
│       │       ├── components/            # 组件
│       │       │   ├── button/
│       │       │   ├── table/
│       │       │   ├── form/
│       │       │   └── modal/
│       │       └── index.ts
│       │
│       ├── shared-types/                  # 共享类型
│       │   ├── package.json
│       │   └── src/
│       │       ├── api/
│       │       ├── models/
│       │       └── index.ts
│       │
│       ├── shared-utils/                  # 共享工具
│       │   ├── package.json
│       │   └── src/
│       │       ├── date.ts
│       │       ├── number.ts
│       │       ├── string.ts
│       │       └── index.ts
│       │
│       └── api-client/                    # API 客户端
│           ├── package.json
│           └── src/
│               ├── client.ts
│               ├── request.ts
│               ├── response.ts
│               └── index.ts
│
├── deploy/                                # 部署配置
│   ├── k8s/                               # Kubernetes 配置
│   │   ├── base/                          # 基础配置
│   │   │   ├── namespace.yaml
│   │   │   ├── configmap/app-config.yaml
│   │   │   └── secret/db-secret.yaml
│   │   │
│   │   └── overlays/                      # 环境配置
│   │       ├── dev/                       # 开发环境
│   │       │   ├── kustomization.yaml
│   │       │   ├── deployment/
│   │       │   │   ├── backend-deployment.yaml
│   │       │   │   └── frontend-deployment.yaml
│   │       │   └── service/
│   │       │       ├── backend-service.yaml
│   │       │       └── frontend-service.yaml
│   │       ├── staging/                   # 测试环境
│   │       └── prod/                      # 生产环境
│   │
│   └── docker/                            # Docker 配置
│       ├── backend/
│       │   ├── Dockerfile
│       │   └── .dockerignore
│       ├── frontend/
│       │   ├── Dockerfile
│       │   └── .dockerignore
│       └── docker-compose.yml             # 本地开发环境
│
├── tools/                                 # 开发工具
│   ├── code-generator/                    # 代码生成器
│   │   ├── templates/                     # 模板
│   │   │   ├── backend/
│   │   │   └── frontend/
│   │   ├── config/
│   │   └── src/
│   │
│   ├── db-migration/                      # 数据库迁移工具
│   │   ├── migrations/                    # 迁移脚本
│   │   │   ├── V1__init_schema.sql
│   │   │   ├── V2__create_fi_tables.sql
│   │   │   └── V3__create_sd_tables.sql
│   │   └── seeds/                         # 种子数据
│   │       ├── base_data.sql
│   │       └── demo_data.sql
│   │
│   └── api-doc-generator/                 # API 文档生成器
│       ├── config/
│       └── templates/
│
├── docs/                                  # 项目文档
│   ├── 01-quick-start.md                  # 快速启动指南
│   ├── 02-project-initialization.md       # 项目初始化文档
│   ├── 03-development-plan.md             # 开发计划
│   ├── 04-sprint-plan.md                  # Sprint 计划
│   ├── 05-technical-architecture.md       # 技术架构 v2.0
│   ├── 06-architecture-decision.md        # 架构决策
│   ├── 07-component-design.md             # 组件设计
│   ├── 08-project-structure.md            # 项目结构
│   ├── 09-sap-s4hana-introduction.md      # SAP 参考文档
│   └── 10-sap-netweaver-isolation.md      # SAP NetWeaver 分析
│
├── .gitignore                             # Git 忽略文件
├── .gitpod.yml                            # Gitpod 配置
├── LICENSE                                # 许可证
├── README.md                              # 项目说明
└── docker-compose.yml                     # 本地开发环境
```

## 模块说明

### 后端模块

#### 平台层 (Platform Layer)

| 模块 | 说明 | 主要功能 |
|------|------|----------|
| `nexterp-platform-auth` | 认证授权 | JWT 认证、权限管理、用户管理 |
| `nexterp-platform-tenant` | 多租户 | 租户隔离、租户配置 |
| `nexterp-platform-workflow` | 工作流 | 流程定义、流程执行 |
| `nexterp-platform-report` | 报表 | 报表定义、报表生成 |
| `nexterp-platform-notification` | 通知 | 消息通知、推送服务 |

#### 业务层 (Business Layer)

| 模块 | 说明 | 主要功能 |
|------|------|----------|
| `nexterp-business-finance` | 财务模块 | 凭证、科目、期间、报表 |
| `nexterp-business-supply` | 供应链模块 | 采购、库存、供应商 |
| `nexterp-business-sales` | 销售模块 | 订单、发货、客户 |
| `nexterp-business-production` | 生产模块 | BOM、生产订单、工艺路线 |
| `nexterp-business-hrm` | 人力资源模块 | 员工、考勤、薪资 |

#### 共享层 (Shared Layer)

| 模块 | 说明 | 主要功能 |
|------|------|----------|
| `nexterp-shared-core` | 核心工具 | 通用工具类、常量、异常、结果封装 |
| `nexterp-shared-data` | 数据访问 | 审计、租户支持、软删除、动态查询 |
| `nexterp-shared-security` | 安全组件 | 安全注解、处理器、工具类 |

### 前端应用

| 应用 | 说明 | 端口 |
|------|------|------|
| `@nexterp/web` | Web 应用 (终端用户) | 3000 |
| `@nexterp/admin` | 管理后台 (管理员) | 3001 |

### 前端共享包

| 包 | 说明 |
|------|------|
| `@nexterp/ui-components` | UI 组件库 |
| `@nexterp/shared-types` | 共享类型定义 |
| `@nexterp/shared-utils` | 共享工具函数 |
| `@nexterp/api-client` | API 客户端封装 |

## 依赖关系

```
┌─────────────────────────────────────────────────────────────────────┐
│                        模块依赖关系                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  nexterp-api-gateway                                                │
│      ├──► nexterp-platform-*                                       │
│      └──► nexterp-business-*                                       │
│                                                                     │
│  nexterp-business-*                                                 │
│      ├──► nexterp-platform-*      (仅接口依赖)                      │
│      └──► nexterp-shared-*                                        │
│                                                                     │
│  nexterp-platform-*                                                 │
│      └──► nexterp-shared-*                                        │
│                                                                     │
│  nexterp-shared-*                                                   │
│      └──► (无业务依赖)                                              │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

## Maven 依赖版本

### 核心依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Cloud | 2023.0.0 | 微服务组件 |
| PostgreSQL | 42.7.1 | 数据库驱动 |
| H2 | 2.2.224 | 内存数据库 (测试) |
| Liquibase | 4.25.0 | 数据库迁移 |
| Redisson | 3.25.0 | Redis 客户端 |
| MapStruct | 1.5.5.Final | 对象映射 |
| Lombok | 1.18.30 | 代码生成 |
| SpringDoc | 2.3.0 | API 文档 |
| JWT | 0.12.3 | JWT 认证 |
| JUnit | 5.10.1 | 单元测试 |
| Testcontainers | 1.19.3 | 集成测试 |
| ArchUnit | 1.2.1 | 架构测试 |

### 前端依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| Next.js | 14.0.4 | React 框架 |
| React | 18.2.0 | UI 库 |
| Ant Design | 5.12.8 | UI 组件库 |
| SWR | 2.2.4 | 数据获取 |
| Zustand | 4.4.7 | 状态管理 |
| TailwindCSS | - | CSS 框架 |
| TypeScript | 5.3.3 | 类型系统 |

## 环境配置

### 开发环境

```yaml
# .env.dev
SPRING_PROFILES_ACTIVE: dev
SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/nexterp
SPRING_DATASOURCE_USERNAME: nexterp
SPRING_DATASOURCE_PASSWORD: nexterp
SPRING_REDIS_HOST: localhost
SPRING_REDIS_PORT: 6379
SPRING_RABBITMQ_HOST: localhost
SPRING_RABBITMQ_PORT: 5672
```

### 生产环境

```yaml
# .env.prod
SPRING_PROFILES_ACTIVE: prod
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-service:5432/nexterp
SPRING_REDIS_HOST: redis-service
SPRING_RABBITMQ_HOST: rabbitmq-service
JAVA_OPTS: -Xmx1g -Xms512m
```

## 构建命令

### 后端

```bash
# 清理构建
./mvnw clean

# 编译
./mvnw compile

# 运行测试
./mvnw test

# 打包
./mvnw package -DskipTests

# 安装到本地仓库
./mvnw install

# 运行应用
./mvnw spring-boot:run

# 代码检查
./mvnw checkstyle:check
./mvnw pmd:check
./mvnw spotbugs:check

# 架构测试
./mvnw test -Dtest=ArchitectureTest
```

### 前端

```bash
# 安装依赖
pnpm install

# 开发模式
pnpm dev

# 构建
pnpm build

# 代码检查
pnpm lint

# 类型检查
pnpm type-check

# 格式化代码
pnpm format
```

### Docker

```bash
# 构建后端镜像
docker build -f deploy/docker/backend/Dockerfile -t nexterp/backend:latest .

# 构建前端镜像
docker build -f deploy/docker/frontend/Dockerfile -t nexterp/frontend:latest .

# 启动开发环境
docker-compose -f deploy/docker/docker-compose.yml up -d

# 启动所有服务
docker-compose up -d postgres redis rabbitmq backend frontend
```

### Kubernetes

```bash
# 应用开发环境配置
kubectl apply -k deploy/k8s/overlays/dev/

# 查看状态
kubectl get pods -n nexterp

# 查看日志
kubectl logs -f deployment/nexterp-backend -n nexterp

# 端口转发
kubectl port-forward -n nexterp svc/nexterp-backend 8080:8080
kubectl port-forward -n nexterp svc/nexterp-frontend 3000:80
```

## 开发规范

### Git 提交规范

```
<type>(<scope>): <subject>

feat: 新功能
fix: 缺陷修复
docs: 文档更新
style: 代码格式
refactor: 重构
perf: 性能优化
test: 测试相关
chore: 构建/工具
```

### Java 包命名规范

```
com.nexterp.{layer}.{module}.{layer}

# 示例
com.nexterp.platform.auth.api
com.nexterp.business.finance.domain.model
com.nexterp.business.finance.application.service
com.nexterp.business.finance.infrastructure.repository
com.nexterp.shared.core.util
```

### TypeScript 文件命名规范

```
# 组件文件
PascalCase.tsx (如: VoucherForm.tsx)

# Hook 文件
use*.ts (如: useVoucher.ts)

# 类型文件
*.ts (如: voucher.ts)

# 工具文件
*.ts (如: format.ts)
```

## 常见问题

### 1. Maven 构建失败

```bash
# 清理并重新构建
./mvnw clean install -U

# 跳过测试
./mvnw install -DskipTests
```

### 2. 前端依赖安装失败

```bash
# 清理缓存
pnpm store prune

# 重新安装
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### 3. Docker 容器无法启动

```bash
# 查看日志
docker-compose logs backend

# 重新构建
docker-compose build --no-cache backend

# 清理并重启
docker-compose down -v
docker-compose up -d
```

## 相关文档

- [快速启动指南](01-quick-start.md)
- [技术架构 v2.0](03-technical-architecture.md)
- [架构决策: 微服务 vs 模块化单体](04-architecture-decision.md)
- [组件设计](05-component-design.md)
- [项目结构](06-project-structure.md)

## 更新日志

| 日期 | 版本 | 说明 |
|------|------|------|
| 2025-01-15 | 1.0.0 | 初始化项目结构 |
