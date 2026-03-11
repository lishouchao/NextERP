# NextERP Backend 开发指南

## 环境要求

- **Java**: JDK 21+
- **Maven**: 3.9+
- **Docker**: 用于运行基础服务

## 快速开始

### 1. 启动基础服务

只启动数据库、Redis、RabbitMQ 等依赖服务：

```bash
cd deploy/docker
docker compose up postgres redis rabbitmq -d
```

查看服务状态：
```bash
docker compose ps
```

停止服务：
```bash
docker compose down
```

### 2. 本地编译

```bash
cd backend-modulith

# 编译（跳过测试）
mvn compile -DskipTests

# 完整构建
mvn clean install -DskipTests
```

### 3. 运行应用

**命令行方式：**
```bash
cd backend-modulith
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**IDE 方式（推荐）：**

在 VS Code 中安装扩展：
- Extension Pack for Java
- Spring Boot Extension Pack

创建 `.vscode/launch.json`：
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "NextERP Application",
      "request": "launch",
      "mainClass": "com.nexterp.NexterpApplication",
      "projectName": "backend-modulith",
      "env": {
        "SPRING_PROFILES_ACTIVE": "dev"
      }
    }
  ]
}
```

按 `F5` 启动调试。

### 4. 访问服务

| 服务 | 地址 | 说明 |
|------|------|------|
| API | http://localhost:8080 | 主应用 |
| Actuator | http://localhost:8080/actuator/health | 健康检查 |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 文档 |

## 数据库连接

| 参数 | 值 |
|------|-----|
| Host | localhost |
| Port | 5432 |
| Database | nexterp |
| Username | nexterp |
| Password | nexterp |

连接字符串：`jdbc:postgresql://localhost:5432/nexterp`

## Redis 连接

| 参数 | 值 |
|------|-----|
| Host | localhost |
| Port | 6379 |
| Password | nexterp |

## RabbitMQ 管理界面

- URL: http://localhost:15672
- Username: nexterp
- Password: nexterp

## 常见问题

### 1. 端口冲突

如果本地端口被占用，可以修改 `docker-compose.yml` 中的端口映射：

```yaml
ports:
  - "5433:5432"  # PostgreSQL 改为 5433
  - "6380:6379"  # Redis 改为 6380
```

然后修改 `application-dev.yml` 中的连接配置。

### 2. Maven 编译失败

检查 Java 版本：
```bash
java -version  # 应该是 21+
```

清理并重新构建：
```bash
mvn clean install -U -DskipTests
```

### 3. IDE 无法识别 Lombok

确保安装了 Lombok 注解处理插件：
- VS Code: 安装 "Lombok Annotations Support for VS Code"
- IDEA: 安装 "Lombok Plugin"

### 4. 数据库连接失败

确保 PostgreSQL 容器正在运行：
```bash
docker compose ps postgres
```

检查日志：
```bash
docker compose logs postgres
```

## 项目结构

```
backend-modulith/
├── src/main/java/com/nexterp/
│   ├── NexterpApplication.java      # 主启动类
│   ├── business/                     # 业务模块
│   │   ├── finance/                  # 财务模块
│   │   ├── hrm/                      # 人力资源模块
│   │   ├── production/               # 生产模块
│   │   ├── sales/                    # 销售模块
│   │   └── supply/                   # 供应模块
│   ├── platform/                     # 平台模块
│   │   ├── auth/                     # 认证授权
│   │   ├── notification/             # 通知服务
│   │   ├── report/                   # 报表服务
│   │   ├── tenant/                   # 多租户
│   │   └── workflow/                 # 工作流
│   └── shared/                       # 共享模块
│       ├── core/                     # 核心类
│       ├── data/                     # 数据访问
│       └── security/                 # 安全相关
└── src/main/resources/
    ├── application.yml               # 主配置
    └── application-dev.yml           # 开发环境配置
```

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 基础框架 |
| Spring Modulith | 1.1.0 | 模块化架构 |
| Spring Security | 6.2+ | 安全框架 |
| Spring Data JPA | 3.2.0 | 数据访问 |
| PostgreSQL | 16 | 数据库 |
| Redis | 7 | 缓存 |
| RabbitMQ | 3 | 消息队列 |
| Flowable | 7.0.0 | 工作流引擎 |
| PDFBox | 3.0.0 | PDF 生成 |
| Lombok | 1.18.30 | 代码简化 |
