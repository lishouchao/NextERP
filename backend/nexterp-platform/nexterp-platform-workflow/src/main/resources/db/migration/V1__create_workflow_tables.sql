-- ==============================================================
-- 工作流模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: 工作流定义、任务分配规则相关表结构
-- ==============================================================

-- 1. 创建流程定义表
-- ==============================================================
CREATE TABLE sys_process_definition (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    process_key VARCHAR(50) NOT NULL,
    process_name VARCHAR(100) NOT NULL,
    process_version INT NOT NULL DEFAULT 1,
    category VARCHAR(50),
    bpmn_xml TEXT NOT NULL,
    description TEXT,
    form_config TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status INT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_process_definition IS '流程定义表';
COMMENT ON COLUMN sys_process_definition.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_process_definition.process_key IS '流程标识（唯一）';
COMMENT ON COLUMN sys_process_definition.process_name IS '流程名称';
COMMENT ON COLUMN sys_process_definition.process_version IS '版本号';
COMMENT ON COLUMN sys_process_definition.category IS '流程分类';
COMMENT ON COLUMN sys_process_definition.bpmn_xml IS 'BPMN XML内容';
COMMENT ON COLUMN sys_process_definition.description IS '流程描述';
COMMENT ON COLUMN sys_process_definition.form_config IS '表单配置(JSON格式)';
COMMENT ON COLUMN sys_process_definition.is_enabled IS '是否启用';
COMMENT ON COLUMN sys_process_definition.status IS '状态 (0-禁用 1-正常)';

-- 创建索引
CREATE UNIQUE INDEX uk_process_key ON sys_process_definition(process_key, process_version, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_process_category ON sys_process_definition(category, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_process_status ON sys_process_definition(status, is_enabled, tenant_id) WHERE is_deleted = FALSE;

-- 2. 创建任务分配规则表
-- ==============================================================
CREATE TABLE sys_task_assignment (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    process_key VARCHAR(50) NOT NULL,
    task_key VARCHAR(50) NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    assignment_type VARCHAR(20) NOT NULL,
    assignee_expression VARCHAR(500),
    candidate_users TEXT,
    candidate_groups TEXT,
    priority INT NOT NULL DEFAULT 0,
    due_date_expression VARCHAR(200),
    description VARCHAR(500),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sys_task_assignment IS '任务分配规则表';
COMMENT ON COLUMN sys_task_assignment.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_task_assignment.process_key IS '流程标识';
COMMENT ON COLUMN sys_task_assignment.task_key IS '任务标识';
COMMENT ON COLUMN sys_task_assignment.task_name IS '任务名称';
COMMENT ON COLUMN sys_task_assignment.assignment_type IS '分配类型 (user-用户 role-角色 dept-部门 expression-表达式)';
COMMENT ON COLUMN sys_task_assignment.assignee_expression IS '处理人表达式';
COMMENT ON COLUMN sys_task_assignment.candidate_users IS '候选用户列表（逗号分隔）';
COMMENT ON COLUMN sys_task_assignment.candidate_groups IS '候选组列表（逗号分隔）';
COMMENT ON COLUMN sys_task_assignment.priority IS '优先级';
COMMENT ON COLUMN sys_task_assignment.due_date_expression IS '到期时间表达式';
COMMENT ON COLUMN sys_task_assignment.is_enabled IS '是否启用';

-- 创建索引
CREATE UNIQUE INDEX uk_task_assignment ON sys_task_assignment(process_key, task_key, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_assignment_process ON sys_task_assignment(process_key, tenant_id) WHERE is_deleted = FALSE;

-- 3. 创建流程实例表
-- ==============================================================
CREATE TABLE sys_process_instance (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    process_key VARCHAR(50) NOT NULL,
    process_name VARCHAR(100),
    business_key VARCHAR(100) NOT NULL,
    business_type VARCHAR(50),
    instance_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    initiator_id BIGINT,
    initiator_name VARCHAR(50),
    current_task VARCHAR(100),
    variables TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE sys_process_instance IS '流程实例表';
COMMENT ON COLUMN sys_process_instance.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_process_instance.process_key IS '流程标识';
COMMENT ON COLUMN sys_process_instance.process_name IS '流程名称';
COMMENT ON COLUMN sys_process_instance.business_key IS '业务键';
COMMENT ON COLUMN sys_process_instance.business_type IS '业务类型';
COMMENT ON COLUMN sys_process_instance.instance_id IS 'Flowable实例ID';
COMMENT ON COLUMN sys_process_instance.status IS '状态 (running-运行中 completed-已完成 suspended-已挂起 terminated-已终止 error-错误)';
COMMENT ON COLUMN sys_process_instance.initiator_id IS '发起人ID';
COMMENT ON COLUMN sys_process_instance.initiator_name IS '发起人名称';
COMMENT ON COLUMN sys_process_instance.current_task IS '当前任务';
COMMENT ON COLUMN sys_process_instance.variables IS '流程变量(JSON格式)';
COMMENT ON COLUMN sys_process_instance.start_time IS '开始时间';
COMMENT ON COLUMN sys_process_instance.end_time IS '结束时间';
COMMENT ON COLUMN sys_process_instance.error_message IS '错误信息';

-- 创建索引
CREATE UNIQUE INDEX uk_instance_id ON sys_process_instance(instance_id);
CREATE UNIQUE INDEX uk_business_key ON sys_process_instance(business_key, tenant_id);
CREATE INDEX idx_instance_process ON sys_process_instance(process_key, tenant_id);
CREATE INDEX idx_instance_status ON sys_process_instance(status, tenant_id);
CREATE INDEX idx_instance_time ON sys_process_instance(start_time DESC);

-- 4. 创建流程任务表
-- ==============================================================
CREATE TABLE sys_process_task (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    instance_id VARCHAR(100) NOT NULL,
    task_id VARCHAR(100) NOT NULL,
    task_key VARCHAR(50) NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    assignee_id BIGINT,
    assignee_name VARCHAR(50),
    candidate_users TEXT,
    candidate_groups TEXT,
    status VARCHAR(20) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    due_date TIMESTAMP,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

COMMENT ON TABLE sys_process_task IS '流程任务表';
COMMENT ON COLUMN sys_process_task.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_process_task.instance_id IS '流程实例ID';
COMMENT ON COLUMN sys_process_task.task_id IS 'Flowable任务ID';
COMMENT ON COLUMN sys_process_task.task_key IS '任务标识';
COMMENT ON COLUMN sys_process_task.task_name IS '任务名称';
COMMENT ON COLUMN sys_process_task.assignee_id IS '处理人ID';
COMMENT ON COLUMN sys_process_task.assignee_name IS '处理人名称';
COMMENT ON COLUMN sys_process_task.candidate_users IS '候选用户';
COMMENT ON COLUMN sys_process_task.candidate_groups IS '候选组';
COMMENT ON COLUMN sys_process_task.status IS '状态 (pending-待处理 assigned-已分配 completed-已完成 cancelled-已取消)';
COMMENT ON COLUMN sys_process_task.priority IS '优先级';
COMMENT ON COLUMN sys_process_task.due_date IS '到期时间';
COMMENT ON COLUMN sys_process_task.start_time IS '开始时间';
COMMENT ON COLUMN sys_process_task.end_time IS '结束时间';
COMMENT ON COLUMN sys_process_task.comment IS '处理意见';

-- 创建索引
CREATE UNIQUE INDEX uk_task_id ON sys_process_task(task_id);
CREATE INDEX idx_task_instance ON sys_process_task(instance_id);
CREATE INDEX idx_task_assignee ON sys_process_task(assignee_id, status);
CREATE INDEX idx_task_status ON sys_process_task(status, tenant_id);

-- 5. 创建流程审批历史表
-- ==============================================================
CREATE TABLE sys_process_approval (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    instance_id VARCHAR(100) NOT NULL,
    task_id VARCHAR(100) NOT NULL,
    task_key VARCHAR(50) NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    approver_id BIGINT NOT NULL,
    approver_name VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    comment TEXT,
    attachments TEXT,
    approval_time TIMESTAMP NOT NULL,
    next_approver_id BIGINT,
    next_approver_name VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_process_approval IS '流程审批历史表';
COMMENT ON COLUMN sys_process_approval.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_process_approval.instance_id IS '流程实例ID';
COMMENT ON COLUMN sys_process_approval.task_id IS '任务ID';
COMMENT ON COLUMN sys_process_approval.task_key IS '任务标识';
COMMENT ON COLUMN sys_process_approval.task_name IS '任务名称';
COMMENT ON COLUMN sys_process_approval.approver_id IS '审批人ID';
COMMENT ON COLUMN sys_process_approval.approver_name IS '审批人名称';
COMMENT ON COLUMN sys_process_approval.action IS '审批动作 (approve-同意 reject-拒绝 transfer-转办 delegate-委派)';
COMMENT ON COLUMN sys_process_approval.comment IS '审批意见';
COMMENT ON COLUMN sys_process_approval.attachments IS '附件(JSON格式)';
COMMENT ON COLUMN sys_process_approval.approval_time IS '审批时间';
COMMENT ON COLUMN sys_process_approval.next_approver_id IS '下一审批人ID';
COMMENT ON COLUMN sys_process_approval.next_approver_name IS '下一审批人名称';

-- 创建索引
CREATE INDEX idx_approval_instance ON sys_process_approval(instance_id);
CREATE INDEX idx_approval_task ON sys_process_approval(task_id);
CREATE INDEX idx_approval_approver ON sys_process_approval(approver_id);
CREATE INDEX idx_approval_time ON sys_process_approval(approval_time DESC);
