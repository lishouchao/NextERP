# NextERP

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14.0.4-black.svg)](https://nextjs.org/)

> 下一代 ERP 系统基于模块化单体架构

## 项目简介

NextERP 是一个基于模块化单体架构的下一代 ERP 系统，采用 Spring Boot 3.2 + Java 21 后端，Next.js 14 前端技术栈。

## 技术栈

### 后端
- Java 21
- Spring Boot 3.2.0
- Spring Data JPA
- Liquibase
- PostgreSQL / H2
- Redis
- RabbitMQ
- MapStruct
- Lombok
- JUnit 5 + Testcontainers + ArchUnit

### 前端
- Next.js 14 (App Router)
- React 18
- TypeScript 5.3
- Ant Design 5
- SWR
- Zustand
- TailwindCSS
- pnpm + Turborepo

### 部署
- Docker
- Kubernetes
- Helm

## 项目结构

```
nexterp/
├── backend/           # 后端 (Maven 多模块)
│   ├── nexterp-platform/      # 平台层
│   ├── nexterp-business/      # 业务层
│   ├── nexterp-shared/        # 共享层
│   ├── nexterp-api/           # API 网关
│   └── nexterp-assembly/      # 部署装配
├── frontend/          # 前端 (pnpm workspace)
│   ├── apps/
│   │   ├── web/              # Web 应用
│   │   └── admin/            # 管理后台
│   └── packages/
│       ├── ui-components/     # UI 组件库
│       ├── shared-types/      # 共享类型
│       ├── shared-utils/      # 共享工具
│       └── api-client/        # API 客户端
├── deploy/            # 部署配置
│   ├── docker/
│   └── k8s/
└── docs/              # 项目文档
```

## 快速开始

### 环境要求

**后端**
- JDK 21+
- Maven 3.9+
- Docker & Docker Compose (可选，用于启动 PostgreSQL/Redis/RabbitMQ)

**前端**
- Node.js 20+
- pnpm 8+

### 本地开发

**开发模式（使用 H2 内存数据库，无需启动额外服务）**

1. **启动后端**
```bash
cd backend/nexterp-platform/nexterp-platform-auth/nexterp-platform-auth-interfaces
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

2. **启动前端**
```bash
cd frontend
pnpm install
pnpm dev
```

访问 http://localhost:3000

**生产模式（需要启动数据库服务）**

1. **启动数据库服务**
```bash
cd deploy/docker
docker-compose up -d postgres redis rabbitmq
```

2. **启动后端**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

3. **启动前端**
```bash
cd frontend
pnpm install
pnpm dev
```

## 开发规范

### Git 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范:

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

### 代码规范

- 后端: 遵循阿里巴巴 Java 开发手册
- 前端: 遵循 Airbnb JavaScript/TypeScript 规范

## 文档

### 快速开始
- [快速启动指南](docs/01-quick-start.md)
- [项目初始化文档](docs/02-project-initialization.md)

### 技术架构
- [技术架构 v2.0](docs/03-technical-architecture.md)
- [架构决策: 微服务 vs 单体](docs/04-architecture-decision.md)
- [组件设计](docs/05-component-design.md)
- [项目结构](docs/06-project-structure.md)

### 参考文档
- [SAP S/4HANA 介绍](docs/07-sap-s4hana-introduction.md)
- [SAP NetWeaver 隔离机制分析](docs/08-sap-netweaver-isolation.md)
- [技术架构 v1.0 (已废弃)](docs/09-technical-architecture-v1.md)

### 开发计划
- [开发计划](docs/10-development-plan.md)
- [Sprint 计划](docs/11-sprint-plan.md)

## 许可证

[MIT](LICENSE)
