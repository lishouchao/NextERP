# NextERP 工作流组件技术栈详解

## 一、技术栈概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           工作流技术栈架构                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         表现层 (API)                                  │   │
│  │  WorkflowController / ProcessDefinitionController / TaskController  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       服务层 (Service)                                │   │
│  │  WorkflowService / ProcessMonitorService / TaskAssignmentService    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    工作流引擎                                         │   │
│  │              Flowable 7.0.0 (BPMN 2.0)                              │   │
│  │  ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐                │   │
│  │  │Repository│ │ Runtime  │ │  Task    │ │ History  │                │   │
│  │  │ Service │ │ Service  │ │ Service  │ │ Service  │                │   │
│  │  └─────────┘ └──────────┘ └──────────┘ └──────────┘                │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      表达式引擎                                        │   │
│  │         Spring Expression Language (SpEL)                            │   │
│  │    WorkflowExpressionParser (自定义扩展)                             │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       持久化层                                         │   │
│  │              PostgreSQL + Flowable Tables                            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 二、核心组件详解

### 2.1 工作流引擎：Flowable 7.0.0

| 特性 | 说明 |
|------|------|
| **版本** | 7.0.0 |
| **规范** | BPMN 2.0 (Business Process Model and Notation) |
| **依赖** | `flowable-spring-boot-starter` |
| **数据库** | 自动创建 60+ 张表支持流程持久化 |

#### 核心服务

```java
// Flowable 六大核心服务
RepositoryService      // 流程定义管理
RuntimeService         // 流程实例运行时管理
TaskService            // 任务管理
HistoryService         // 历史数据管理
IdentityService        // 用户/组管理
FormService            // 表单管理
```

#### 数据库表结构

```
Flowable 自动创建的表分类:

ACT_R_*   - Repository (流程定义)
ACT_RU_*  - Runtime (运行时)
ACT_HI_*  - History (历史)
ACT_ID_*  - Identity (身份)
ACT_GE_*  - General (通用)
ACT_CO_*  - Content (内容)
```

---

### 2.2 表达式引擎：SpEL + 自定义扩展

#### 支持的表达式类型

| 类型 | 语法 | 示例 | 说明 |
|------|------|------|------|
| **变量替换** | `${...}` | `${initiator.deptId}` | 发起人部门ID |
| **SpEL表达式** | `#{...}` | `#{T(java.lang.Math).random()}` | Java函数调用 |
| **用户指定** | `#{user.id}` | `#{user.123}` | 指定用户ID |
| **角色指定** | `#{role.id}` | `#{role.admin}` | 指定角色ID |
| **部门指定** | `#{dept.id}` | `#{dept.100}` | 指定部门ID |

#### 自定义表达式函数

```java
// WorkflowExpressionParser.ExpressionFunctions

currentDate()        // 当前日期
currentTime()        // 当前时间
currentDateTime()    // 当前日期时间
daysBetween(start, end)  // 日期差计算
between(value, min, max)  // 数值范围判断
contains(collection, value)  // 包含判断
formatNumber(value, pattern)  // 数字格式化
formatDate(date, pattern)     // 日期格式化
```

---

### 2.3 领域模型

#### ProcessDefinition (流程定义)

```java
@Entity
@Table(name = "wf_process_definition")
public class ProcessDefinition {
    private Long id;
    private Long tenantId;           // 租户ID
    private String processKey;       // 流程Key
    private String processName;      // 流程名称
    private Integer version;         // 版本号
    private String bpmnXml;          // BPMN XML定义
    private String category;         // 流程分类
    private Integer status;          // 状态 (0-草稿 1-发布 2-归档)
    private Boolean enabled;         // 是否启用
}
```

#### TaskAssignment (任务分配规则)

```java
@Entity
@Table(name = "wf_task_assignment")
public class TaskAssignment {
    private Long id;
    private String processKey;       // 流程定义Key
    private String taskKey;          // 任务定义Key
    private String assignmentType;   // 分配类型
    private String assignmentValue;  // 分配值
    private Integer priority;        // 优先级
}
```

---

### 2.4 核心服务接口

#### WorkflowService 核心方法

| 方法 | 说明 |
|------|------|
| `deployProcess()` | 部署流程定义 |
| `startProcess()` | 启动流程实例 |
| `completeTask()` | 完成任务 |
| `getUserTasks()` | 获取用户待办任务 |
| `getUserCandidateTasks()` | 获取用户候选任务 |
| `suspendProcessInstance()` | 挂起流程实例 |
| `activateProcessInstance()` | 激活流程实例 |
| `deleteProcessInstance()` | 删除流程实例 |

---

## 三、BPMN 2.0 流程设计

### 3.1 支持的节点类型

| 节点类型 | 说明 | 图标 |
|----------|------|------|
| **Start Event** | 开始事件 | ⭕ |
| **End Event** | 结束事件 | ⭕⚫ |
| **User Task** | 用户任务 | 👤 |
| **Service Task** | 服务任务 | ⚙️ |
| **Exclusive Gateway** | 排他网关 | ◇ |
| **Parallel Gateway** | 并行网关 | ⧫ |
| **Inclusive Gateway** | 包容网关 | ◇⚫ |

### 3.2 示例流程：采购审批

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             targetNamespace="Examples">
  <process id="purchaseApproval" name="采购审批流程">

    <!-- 开始事件 -->
    <startEvent id="start"/>

    <!-- 用户任务：填写采购申请 -->
    <userTask id="fillRequest" name="填写采购申请">
      <extensionElements>
        <flowable:taskListener event="create"
                               class="com.nexterp.workflow.listener.FillRequestListener"/>
      </extensionElements>
    </userTask>

    <!-- 排他网关：金额判断 -->
    <exclusiveGateway id="amountGateway"/>

    <!-- 用户任务：部门经理审批 -->
    <userTask id="managerApproval" name="部门经理审批">
      <humanPerformer>
        <resourceAssignmentExpression>
          <formalExpression>${initiator.deptManagerId}</formalExpression>
        </resourceAssignmentExpression>
      </humanPerformer>
    </userTask>

    <!-- 用户任务：财务审批 -->
    <userTask id="financeApproval" name="财务审批">
      <humanPerformer>
        <resourceAssignmentExpression>
          <formalExpression>#{role.finance_manager}</formalExpression>
        </resourceAssignmentExpression>
      </humanPerformer>
    </userTask>

    <!-- 结束事件 -->
    <endEvent id="end"/>

    <!-- 流转连线 -->
    <sequenceFlow sourceRef="start" targetRef="fillRequest"/>
    <sequenceFlow sourceRef="fillRequest" targetRef="amountGateway"/>
    <sequenceFlow sourceRef="amountGateway" targetRef="managerApproval">
      <conditionExpression>${amount <= 10000}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow sourceRef="amountGateway" targetRef="financeApproval">
      <conditionExpression>${amount > 10000}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow sourceRef="managerApproval" targetRef="end"/>
    <sequenceFlow sourceRef="financeApproval" targetRef="end"/>

  </process>
</definitions>
```

---

## 四、任务分配策略

### 4.1 分配类型

| 类型 | assignmentType | assignmentValue | 示例 |
|------|----------------|-----------------|------|
| **指定用户** | `user` | 用户ID | `123` |
| **指定角色** | `role` | 角色ID | `admin` |
| **指定部门** | `dept` | 部门ID | `100` |
| **表达式** | `expression` | SpEL表达式 | `${initiator.deptId}` |

### 4.2 分配表达式示例

```java
// 发起人部门经理
${initiator.deptManagerId}

// 发起人角色
${initiator.roleId}

// 动态查询部门负责人
#{dept.getManager(${initiator.deptId})}

// 多人分配（候选组）
[${initiator.deptManagerId}, ${initiator.financeId}]

// 条件分配
#{amount > 10000 ? 'finance_manager' : 'dept_manager'}
```

---

## 五、Spring Modulith 集成

### 5.1 模块定义

```java
// platform/workflow/package-info.java
@ApplicationModule(
    type = ApplicationModule.Type.OPEN,
    displayName = "工作流引擎",
    allowedDependencies = {"shared", "auth", "tenant"}
)
package com.nexterp.platform.workflow;
```

### 5.2 事件发布

```java
// 流程启动事件
public record ProcessStartedEvent(
    String processInstanceId,
    String processDefinitionKey,
    String businessKey,
    String initiator
) {}

// 流程完成事件
public record ProcessCompletedEvent(
    String processInstanceId,
    String businessKey,
    Map<String, Object> variables
) {}

// 任务分配事件
public record TaskAssignedEvent(
    String taskId,
    String taskName,
    String assignee,
    LocalDateTime dueDate
) {}
```

---

## 六、配置参数

### application.yml

```yaml
flowable:
  # 数据库配置
  database-schema-update: true
  db-identity-used: false

  # 流程定义缓存
  process-definition-cache-limit: 100

  # 异步执行器
  async-executor-activate: true
  async-history-enabled: true

  # 历史数据级别
  history-level: audit

  # 邮件服务器
  mail-server-host: smtp.example.com
  mail-server-port: 587
```

---

## 七、前端集成

### 工作流前端页面 (计划)

| 页面 | 路径 | 说明 |
|------|------|------|
| 流程设计器 | `/workflow/designer` | BPMN 可视化设计 |
| 我的待办 | `/workflow/my-tasks` | 用户待办任务列表 |
| 我的申请 | `/workflow/my-requests` | 用户发起的流程 |
| 流程监控 | `/workflow/monitor` | 流程实例监控 |
| 流程统计 | `/workflow/statistics` | 流程数据统计 |

---

## 八、技术栈总结

| 层次 | 技术 | 版本 |
|------|------|------|
| **工作流引擎** | Flowable | 7.0.0 |
| **流程规范** | BPMN 2.0 | - |
| **表达式引擎** | Spring SpEL | - |
| **持久化** | PostgreSQL + JPA | - |
| **模块化** | Spring Modulith | 1.1.0 |
| **缓存** | Redis (Redisson) | 3.25.0 |
