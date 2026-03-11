-- ==============================================================
-- 人力资源模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: 员工、部门、岗位、考勤相关表结构
-- ==============================================================

-- 1. 创建部门表
-- ==============================================================
CREATE TABLE hrm_department (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    dept_code VARCHAR(50) NOT NULL,
    dept_name VARCHAR(100) NOT NULL,
    parent_id BIGINT,
    dept_level INT NOT NULL,
    dept_type INT NOT NULL DEFAULT 1,
    manager_id BIGINT,
    manager_name VARCHAR(50),
    dept_leader_id BIGINT,
    dept_leader_name VARCHAR(50),
    company_id BIGINT,
    company_name VARCHAR(100),
    cost_center_code VARCHAR(50),
    cost_center_name VARCHAR(100),
    address VARCHAR(200),
    phone VARCHAR(20),
    fax VARCHAR(20),
    status INT NOT NULL DEFAULT 1,
    sort_order INT,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE hrm_department IS '部门表';
COMMENT ON COLUMN hrm_department.dept_code IS '部门编码';
COMMENT ON COLUMN hrm_department.dept_name IS '部门名称';
COMMENT ON COLUMN hrm_department.parent_id IS '父部门ID';
COMMENT ON COLUMN hrm_department.dept_level IS '部门层级';
COMMENT ON COLUMN hrm_department.dept_type IS '部门类型 (1-公司 2-部门 3-小组)';
COMMENT ON COLUMN hrm_department.manager_id IS '部门经理ID';
COMMENT ON COLUMN hrm_department.manager_name IS '部门经理姓名';
COMMENT ON COLUMN hrm_department.status IS '状态 (0-禁用 1-启用)';

CREATE UNIQUE INDEX uk_dept_code ON hrm_department(dept_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_dept_parent ON hrm_department(parent_id, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_dept_type ON hrm_department(dept_type, tenant_id) WHERE is_deleted = FALSE;

-- 2. 创建岗位表
-- ==============================================================
CREATE TABLE hrm_position (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    position_code VARCHAR(50) NOT NULL,
    position_name VARCHAR(100) NOT NULL,
    position_level INT,
    position_category VARCHAR(50),
    job_series VARCHAR(50),
    department_id BIGINT,
    department_name VARCHAR(100),
    is_manager_position BOOLEAN NOT NULL DEFAULT FALSE,
    status INT NOT NULL DEFAULT 1,
    sort_order INT,
    description TEXT,
    requirements TEXT,
    responsibilities TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE hrm_position IS '岗位表';
COMMENT ON COLUMN hrm_position.position_code IS '岗位编码';
COMMENT ON COLUMN hrm_position.position_name IS '岗位名称';
COMMENT ON COLUMN hrm_position.position_level IS '岗位级别';
COMMENT ON COLUMN hrm_position.position_category IS '岗位类别';
COMMENT ON COLUMN hrm_position.job_series IS '职位序列';
COMMENT ON COLUMN hrm_position.department_id IS '所属部门ID';
COMMENT ON COLUMN hrm_position.is_manager_position IS '是否管理岗位';
COMMENT ON COLUMN hrm_position.status IS '状态 (0-禁用 1-启用)';

CREATE UNIQUE INDEX uk_position_code ON hrm_position(position_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_position_dept ON hrm_position(department_id, tenant_id) WHERE is_deleted = FALSE;

-- 3. 创建员工表
-- ==============================================================
CREATE TABLE hrm_employee (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_no VARCHAR(50) NOT NULL,
    employee_name VARCHAR(50) NOT NULL,
    english_name VARCHAR(100),
    gender INT NOT NULL,
    birth_date DATE,
    nation VARCHAR(20),
    id_card VARCHAR(50),
    native_place VARCHAR(100),
    political_status VARCHAR(20),
    marital_status INT,
    education INT,
    graduate_school VARCHAR(100),
    major VARCHAR(50),
    hire_date DATE,
    regular_date DATE,
    resign_date DATE,
    job_no VARCHAR(50),
    department_id BIGINT,
    department_name VARCHAR(100),
    position_id BIGINT,
    position_name VARCHAR(100),
    rank_id BIGINT,
    rank_name VARCHAR(50),
    supervisor_id BIGINT,
    supervisor_name VARCHAR(50),
    work_location VARCHAR(100),
    email VARCHAR(100),
    mobile VARCHAR(20),
    emergency_contact VARCHAR(50),
    emergency_phone VARCHAR(20),
    home_address VARCHAR(200),
    registered_address VARCHAR(200),
    bank_account VARCHAR(50),
    bank_name VARCHAR(100),
    base_salary DECIMAL(19,2),
    position_salary DECIMAL(19,2),
    performance_salary DECIMAL(19,2),
    allowance DECIMAL(19,2),
    social_security_personal DECIMAL(19,2),
    housing_fund_personal DECIMAL(19,2),
    work_status INT NOT NULL DEFAULT 1,
    status INT NOT NULL DEFAULT 1,
    photo VARCHAR(500),
    attachments TEXT,
    remark VARCHAR(500),
    custom_field1 VARCHAR(100),
    custom_field2 VARCHAR(100),
    custom_field3 VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE hrm_employee IS '员工表';
COMMENT ON COLUMN hrm_employee.employee_no IS '员工编号';
COMMENT ON COLUMN hrm_employee.employee_name IS '员工姓名';
COMMENT ON COLUMN hrm_employee.gender IS '性别 (1-男 2-女)';
COMMENT ON COLUMN hrm_employee.birth_date IS '出生日期';
COMMENT ON COLUMN hrm_employee.education IS '学历 (1-高中及以下 2-大专 3-本科 4-硕士 5-博士)';
COMMENT ON COLUMN hrm_employee.hire_date IS '入职日期';
COMMENT ON COLUMN hrm_employee.regular_date IS '转正日期';
COMMENT ON COLUMN hrm_employee.resign_date IS '离职日期';
COMMENT ON COLUMN hrm_employee.department_id IS '部门ID';
COMMENT ON COLUMN hrm_employee.position_id IS '岗位ID';
COMMENT ON COLUMN hrm_employee.work_status IS '工作状态 (1-在职 2-试用 3-离职 4-停薪留职)';
COMMENT ON COLUMN hrm_employee.status IS '状态 (0-禁用 1-启用)';

CREATE UNIQUE INDEX uk_employee_no ON hrm_employee(employee_no, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_employee_dept ON hrm_employee(department_id, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_employee_position ON hrm_employee(position_id, tenant_id) WHERE is_deleted = FALSE;

-- 4. 创建考勤记录表
-- ==============================================================
CREATE TABLE hrm_attendance_record (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    employee_no VARCHAR(50),
    employee_name VARCHAR(50),
    department_id BIGINT,
    department_name VARCHAR(100),
    attendance_date DATE NOT NULL,
    shift_type VARCHAR(50),
    shift_name VARCHAR(100),
    work_time DECIMAL(10,2),
    overtime_time DECIMAL(10,2),
    late_time DECIMAL(10,2),
    early_time DECIMAL(10,2),
    absence_time DECIMAL(10,2),
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE hrm_attendance_record IS '考勤记录表';
COMMENT ON COLUMN hrm_attendance_record.employee_id IS '员工ID';
COMMENT ON COLUMN hrm_attendance_record.employee_no IS '员工编号';
COMMENT ON COLUMN hrm_attendance_record.attendance_date IS '考勤日期';
COMMENT ON COLUMN hrm_attendance_record.shift_type IS '班次类型';
COMMENT ON COLUMN hrm_attendance_record.shift_name IS '班次名称';
COMMENT ON COLUMN hrm_attendance_record.work_time IS '工作时间(小时)';
COMMENT ON COLUMN hrm_attendance_record.overtime_time IS '加班时间(小时)';
COMMENT ON COLUMN hrm_attendance_record.late_time IS '迟到时间(分钟)';
COMMENT ON COLUMN hrm_attendance_record.early_time IS '早退时间(分钟)';
COMMENT ON COLUMN hrm_attendance_record.absence_time IS '缺勤时间(小时)';
COMMENT ON COLUMN hrm_attendance_record.status IS '状态 (1-正常 2-迟到 3-早退 4-缺勤 5-请假)';

CREATE INDEX idx_attendance_employee ON hrm_attendance_record(employee_id, attendance_date);
CREATE INDEX idx_attendance_date ON hrm_attendance_record(attendance_date, tenant_id) WHERE is_deleted = FALSE;

-- 5. 创建请假记录表
-- ==============================================================
CREATE TABLE hrm_leave_request (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    leave_no VARCHAR(50) NOT NULL,
    employee_id BIGINT NOT NULL,
    employee_no VARCHAR(50),
    employee_name VARCHAR(50),
    department_id BIGINT,
    department_name VARCHAR(100),
    position_id BIGINT,
    position_name VARCHAR(100),
    leave_type INT NOT NULL,
    leave_reason VARCHAR(500),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_days DECIMAL(5,2) NOT NULL,
    start_time VARCHAR(10),
    end_time VARCHAR(10),
    handover_person_id BIGINT,
    handover_person_name VARCHAR(50),
    approver_id BIGINT,
    approver_name VARCHAR(50),
    approval_status INT NOT NULL DEFAULT 0,
    approval_time TIMESTAMP,
    approval_comment VARCHAR(500),
    attachments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE hrm_leave_request IS '请假记录表';
COMMENT ON COLUMN hrm_leave_request.leave_no IS '请假单号';
COMMENT ON COLUMN hrm_leave_request.employee_id IS '员工ID';
COMMENT ON COLUMN hrm_leave_request.leave_type IS '请假类型 (1-事假 2-病假 3-年假 4-婚假 5-产假 6-丧假 7-调休)';
COMMENT ON COLUMN hrm_leave_request.start_date IS '开始日期';
COMMENT ON COLUMN hrm_leave_request.end_date IS '结束日期';
COMMENT ON COLUMN hrm_leave_request.leave_days IS '请假天数';
COMMENT ON COLUMN hrm_leave_request.approval_status IS '审批状态 (0-待提交 1-待审批 2-已批准 3-已拒绝 4-已撤销)';

CREATE UNIQUE INDEX uk_leave_no ON hrm_leave_request(leave_no, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_leave_employee ON hrm_leave_request(employee_id, start_date);
CREATE INDEX idx_leave_status ON hrm_leave_request(approval_status, tenant_id) WHERE is_deleted = FALSE;

-- 6. 插入示例数据
-- ==============================================================
INSERT INTO hrm_department (tenant_id, dept_code, dept_name, parent_id, dept_level, dept_type, status)
VALUES
(0, 'DEPT001', '示例公司', NULL, 1, 1, 1),
(0, 'DEPT002', '研发部', 1, 2, 2, 1),
(0, 'DEPT003', '销售部', 1, 2, 2, 1);

INSERT INTO hrm_position (tenant_id, position_code, position_name, position_level, department_id, department_name, is_manager_position, status)
VALUES
(0, 'POS001', '总经理', 1, 1, '示例公司', true, 1),
(0, 'POS002', '研发经理', 2, 2, '研发部', true, 1),
(0, 'POS003', '开发工程师', 3, 2, '研发部', false, 1),
(0, 'POS004', '销售经理', 2, 3, '销售部', true, 1);

INSERT INTO hrm_employee (tenant_id, employee_no, employee_name, gender, hire_date, department_id, department_name, position_id, position_name, work_status, status)
VALUES
(0, 'EMP001', '张三', 1, CURRENT_DATE, 2, '研发部', 3, '开发工程师', 1, 1),
(0, 'EMP002', '李四', 1, CURRENT_DATE, 3, '销售部', 4, '销售经理', 1, 1);
