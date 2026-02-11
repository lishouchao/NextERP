# NextERP 快速启动指南

## 前置要求

| 软件 | 版本要求 | 检查命令 |
|------|----------|----------|
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Node.js | 20+ | `node -v` |
| pnpm | 8+ | `pnpm -v` |
| Docker | 20+ | `docker -v` |
| Docker Compose | 2+ | `docker-compose -v` |

## 一、环境准备

### 1. 安装 JDK 21

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# macOS
brew install openjdk@21

# 验证安装
java -version
```

### 2. 安装 pnpm

```bash
npm install -g pnpm@8

# 验证安装
pnpm -v
```

### 3. 安装 Docker 和 Docker Compose

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install docker.io docker-compose

# macOS
brew install docker docker-compose

# 验证安装
docker -v
docker-compose -v
```

## 二、启动数据库服务

```bash
cd deploy/docker
docker-compose up -d postgres redis rabbitmq

# 检查服务状态
docker-compose ps

# 查看日志
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f rabbitmq
```

## 三、启动后端服务

### 1. 编译项目

```bash
cd backend

# 首次构建，下载依赖
./mvnw clean install -DskipTests

# 或者使用 Maven Wrapper (Windows)
mvnw.cmd clean install -DskipTests
```

### 2. 启动应用

```bash
cd backend/nexterp-assembly/nexterp-assembly-full

# 启动完整应用
../../mvnw spring-boot:run

# 或指定 profile
../../mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 验证后端服务

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# API 文档
open http://localhost:8080/swagger-ui.html
```

## 四、启动前端服务

### 1. 安装依赖

```bash
cd frontend
pnpm install
```

### 2. 启动开发服务器

```bash
# 启动所有应用
pnpm dev

# 或只启动 web 应用
cd apps/web
pnpm dev
```

### 3. 访问应用

```bash
# Web 应用 (http://localhost:3000)
open http://localhost:3000

# 管理后台 (http://localhost:3001)
open http://localhost:3001
```

## 五、IDE 配置

### IntelliJ IDEA

1. **导入项目**
   - File → Open → 选择 `backend/pom.xml`
   - 选择 "Open as Project"

2. **配置 JDK**
   - File → Project Structure → Project
   - SDK: 选择 JDK 21

3. **启用注解处理**
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - 勾选 "Enable annotation processing"

4. **配置 Lombok 插件**
   - File → Settings → Plugins
   - 搜索 "Lombok" 并安装

### VS Code

1. **安装扩展**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - ESLint
   - Prettier
   - Tailwind CSS IntelliSense

2. **配置 Java**
   - 按 `Ctrl+Shift+P` 打开命令面板
   - 输入 "Java: Configure Java Runtime"
   - 设置 JDK 21 路径

## 六、数据库初始化

### Liquibase 自动初始化

应用启动时会自动执行 Liquibase 迁移脚本：

```bash
# 迁移脚本位置
backend/nexterp-business/nexterp-business-finance/src/main/resources/db/migration/
```

### 手动初始化

```bash
# 连接到 PostgreSQL
docker exec -it nexterp-postgres psql -U nexterp -d nexterp

# 查看表
\dt

# 退出
\q
```

## 七、常用命令

### 后端

```bash
# 清理构建
./mvnw clean

# 编译
./mvnw compile

# 运行测试
./mvnw test

# 打包
./mvnw package

# 跳过测试打包
./mvnw package -DskipTests

# 代码格式检查
./mvnw spotless:check

# 代码格式化
./mvnw spotless:apply

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

# 代码修复
pnpm lint --fix

# 类型检查
pnpm type-check

# 格式化代码
pnpm format
```

### Docker

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 查看日志
docker-compose logs -f

# 重启服务
docker-compose restart backend

# 查看服务状态
docker-compose ps
```

## 八、故障排除

### 问题 1: 端口被占用

```bash
# 查看端口占用
sudo lsof -i :8080  # 后端
sudo lsof -i :3000  # 前端

# 杀死进程
kill -9 <PID>
```

### 问题 2: 数据库连接失败

```bash
# 检查 PostgreSQL 状态
docker-compose ps postgres

# 重启 PostgreSQL
docker-compose restart postgres

# 查看日志
docker-compose logs postgres
```

### 问题 3: 前端构建失败

```bash
# 清理缓存
rm -rf node_modules
rm -rf .next
rm pnpm-lock.yaml

# 重新安装
pnpm install
```

### 问题 4: Maven 依赖下载失败

```bash
# 清理本地仓库缓存
rm -rf ~/.m2/repository

# 使用国内镜像
# 在 ~/.m2/settings.xml 中配置阿里云镜像
```

## 九、开发工作流

### 1. 创建功能分支

```bash
git checkout -b feature/your-feature-name
```

### 2. 开发和测试

```bash
# 后端
cd backend
./mvnw spring-boot:run

# 前端 (新终端)
cd frontend
pnpm dev
```

### 3. 提交代码

```bash
git add .
git commit -m "feat: 添加新功能"
git push origin feature/your-feature-name
```

### 4. 代码审查

```bash
# 后端代码检查
./mvnw checkstyle:check pmd:check

# 前端代码检查
pnpm lint
pnpm type-check
```

## 十、默认账号

| 服务 | 用户名 | 密码 |
|------|--------|------|
| PostgreSQL | nexterp | nexterp |
| Redis | - | nexterp |
| RabbitMQ | nexterp | nexterp |
| RabbitMQ Management | nexterp | nexterp |

## 十一、访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| Web 应用 | http://localhost:3000 | 终端用户界面 |
| 管理后台 | http://localhost:3001 | 管理员界面 |
| API 服务 | http://localhost:8080 | REST API |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 文档 |
| Actuator | http://localhost:8080/actuator | 健康检查 |
| RabbitMQ Management | http://localhost:15672 | 消息队列管理 |

## 十二、下一步

- 阅读 [技术架构文档](03-technical-architecture.md)
- 查看 [组件设计文档](05-component-design.md)
- 了解 [开发规范](development/coding-standards.md)
