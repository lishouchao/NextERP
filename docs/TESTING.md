# NextERP 测试指南

## 目录

1. [测试环境准备](#测试环境准备)
2. [运行测试](#运行测试)
3. [测试类型](#测试类型)
4. [测试覆盖率](#测试覆盖率)
5. [编写测试](#编写测试)
6. [CI/CD集成](#cicd集成)

---

## 测试环境准备

### 1. 启动测试基础设施

使用Docker Compose启动测试所需的数据库、缓存和消息队列：

```bash
# Linux/Mac
docker-compose -f backend/docker-compose.test.yml up -d

# Windows
docker-compose -f backend\docker-compose.test.yml up -d
```

### 2. 验证环境

```bash
# 检查数据库
docker exec nexterp-postgres-test pg_isready -U nexterp_test

# 检查Redis
docker exec nexterp-redis-test redis-cli -a nexterp_test_password ping

# 检查RabbitMQ
docker exec nexterp-rabbitmq-test rabbitmq-diagnostics -q ping
```

### 3. 停止测试环境

```bash
docker-compose -f backend/docker-compose.test.yml down
```

---

## 运行测试

### 使用脚本运行

#### Linux/Mac

```bash
# 运行所有测试
./backend/scripts/test.sh

# 运行单元测试
./backend/scripts/test.sh --unit

# 运行集成测试
./backend/scripts/test.sh --integration

# 运行指定模块测试
./backend/scripts/test.sh --module nexterp-platform-auth
```

#### Windows

```cmd
REM 运行所有测试
backend\scripts\test.bat

REM 运行单元测试
backend\scripts\test.bat --unit

REM 运行集成测试
backend\scripts\test.bat --integration

REM 运行指定模块测试
backend\scripts\test.bat --module nexterp-platform-auth
```

### 使用Maven运行

```bash
# 运行所有测试
mvn test

# 运行指定模块测试
mvn test -pl nexterp-platform/nexterp-platform-auth

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 运行指定测试方法
mvn test -Dtest=UserServiceTest#createUser_Success

# 跳过测试
mvn clean install -DskipTests
```

### 使用IDE运行

在IntelliJ IDEA或Eclipse中：
1. 右键点击测试类或测试方法
2. 选择"Run"或"Debug"
3. 查看测试结果

---

## 测试类型

### 1. 单元测试

单元测试针对单个类或方法进行测试，使用Mock隔离外部依赖。

**示例**: [FinAccountServiceTest.java](../backend/nexterp-business/nexterp-business-finance/src/test/java/com/nexterp/business/finance/service/FinAccountServiceTest.java)

```java
@ExtendWith(MockitoExtension.class)
class FinAccountServiceTest {

    @Mock
    private FinAccountRepository accountRepository;

    @InjectMocks
    private FinAccountService accountService;

    @Test
    void createAccount_Success() {
        // Arrange
        when(accountRepository.save(any())).thenReturn(testAccount);

        // Act
        Long accountId = accountService.createAccount(createRequest, 0L);

        // Assert
        assertThat(accountId).isEqualTo(1L);
    }
}
```

### 2. 集成测试

集成测试验证多个组件协作是否正常，使用真实数据库和完整Spring上下文。

**示例**: [FinAccountControllerTest.java](../backend/nexterp-business/nexterp-business-finance/src/test/java/com/nexterp/business/finance/controller/FinAccountControllerTest.java)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FinAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAccount_Success() throws Exception {
        mockMvc.perform(post("/api/v1/finance/accounts")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

## 测试覆盖率

### 生成覆盖率报告

```bash
# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 查看HTML报告
open backend/target/site/jacoco/index.html
```

### 覆盖率要求

| 模块 | 最低覆盖率要求 |
|------|---------------|
| Platform层 | 80% |
| Business层 | 75% |
| Shared层 | 85% |

### 查看覆盖率

- **HTML报告**: `backend/target/site/jacoco/index.html`
- **XML报告**: `backend/target/site/jacoco/jacoco.xml`
- **CSV报告**: `backend/target/site/jacoco/jacoco.csv`

---

## 编写测试

### 测试命名规范

```
<类名>Test.java
```

### 测试方法命名规范

```java
@Test
void <方法名>_<场景>_<预期结果>() {
    // 测试代码
}

// 示例
@Test
void createUser_Success() { }

@Test
void createUser_UsernameExists() { }
```

### 测试结构 (AAA模式)

```java
@Test
void testMethodName_Scenario_ExpectedResult() {
    // Arrange (准备)
    // 设置测试数据、Mock依赖

    // Act (执行)
    // 调用被测试方法

    // Assert (断言)
    // 验证结果
}
```

### 常用断言

```java
import static org.assertj.core.api.Assertions.*;

// 相等性断言
.assertThat(actual).isEqualTo(expected);
.assertThat(actual).isNotEqualTo(other);

// 布尔断言
.assertThat(condition).isTrue();
.assertThat(condition).isFalse();

// 空值断言
.assertThat(value).isNull();
.assertThat(value).isNotNull();

// 集合断言
.assertThat(list).hasSize(3);
.assertThat(list).contains(element);
.assertThat(list).doesNotContain(element);
.assertThat(list).isEmpty();

// 异常断言
.assertThatThrownBy(() -> service.method())
    .isInstanceOf(BusinessException.class)
    .hasMessage("错误信息");

// 数字断言
.assertThat(number).isGreaterThan(0);
.assertThat(number).isLessThan(100);
.assertThat(number).isBetween(1, 10);
```

### Mock使用

```java
// Mock方法调用
when(repository.findById(1L)).thenReturn(Optional.of(entity));

// Mock抛出异常
when(service.method()).thenThrow(new BusinessException("错误"));

// 验证方法调用
verify(repository).save(any(Entity.class));
verify(repository, times(1)).save(any(Entity.class));
verify(repository, never()).delete(anyLong());
```

---

## CI/CD集成

### GitHub Actions示例

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: nexterp_test
          POSTGRES_USER: nexterp_test
          POSTGRES_PASSWORD: nexterp_test_password
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

      redis:
        image: redis:7
        ports:
          - 6379:6379
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run tests
        run: mvn clean test

      - name: Generate coverage report
        run: mvn jacoco:report

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: backend/target/site/jacoco/jacoco.xml
```

---

## 测试最佳实践

1. **测试独立性**: 每个测试应该独立运行，不依赖其他测试
2. **使用@Transactional**: 集成测试使用`@Transactional`自动回滚数据
3. ** meaningful命名**: 测试方法名应该清楚描述测试场景
4. **单一职责**: 每个测试只验证一个行为
5. **Mock外部依赖**: 单元测试中Mock数据库、HTTP调用等
6. **使用测试构建器**: 创建TestDataBuilder简化测试数据准备
7. **测试边界条件**: 测试空值、null、边界值等情况
8. **测试异常场景**: 验证错误处理和异常抛出

---

## 常见问题

### Q: 测试运行失败，提示数据库连接失败

A: 确保测试环境的Docker容器已启动：
```bash
docker-compose -f backend/docker-compose.test.yml up -d
```

### Q: 如何调试测试？

A: 在IDE中右键测试方法选择"Debug"，或使用Maven：
```bash
mvn test -Dmaven.surefire.debug
```

### Q: 如何只运行失败的测试？

A:
```bash
mvn test -DfailIfNoTests=false -Dtest='**/*Test'
```

### Q: 测试数据如何清理？

A: 集成测试使用`@Transactional`自动回滚，单元测试不操作数据库。

---

## 参考资源

- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ 文档](https://assertj.github.io/doc/)
- [Spring Boot 测试文档](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
