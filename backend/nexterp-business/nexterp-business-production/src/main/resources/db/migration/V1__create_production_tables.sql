-- ==============================================================
-- 生产模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: BOM、生产订单、工序、工艺路线相关表结构
-- ==============================================================

-- 1. 创建BOM表
-- ==============================================================
CREATE TABLE pro_bom (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    bom_code VARCHAR(50) NOT NULL,
    bom_name VARCHAR(100) NOT NULL,
    bom_type INT NOT NULL DEFAULT 1,
    version VARCHAR(20),
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(100),
    specification VARCHAR(200),
    unit VARCHAR(20),
    bom_qty DECIMAL(19,4),
    base_type INT NOT NULL DEFAULT 1,
    status INT NOT NULL DEFAULT 0,
    effective_date DATE,
    expiry_date DATE,
    remark VARCHAR(500),
    attachments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE pro_bom IS '物料清单表';
COMMENT ON COLUMN pro_bom.bom_code IS 'BOM编码';
COMMENT ON COLUMN pro_bom.bom_name IS 'BOM名称';
COMMENT ON COLUMN pro_bom.bom_type IS 'BOM类型 (1-标准BOM 2-替代BOM 3-工艺BOM)';
COMMENT ON COLUMN pro_bom.version IS '版本号';
COMMENT ON COLUMN pro_bom.product_id IS '成品物料ID';
COMMENT ON COLUMN pro_bom.product_code IS '成品物料编码';
COMMENT ON COLUMN pro_bom.product_name IS '成品物料名称';
COMMENT ON COLUMN pro_bom.specification IS '规格型号';
COMMENT ON COLUMN pro_bom.unit IS '单位';
COMMENT ON COLUMN pro_bom.bom_qty IS 'BOM数量';
COMMENT ON COLUMN pro_bom.base_type IS '基准类型 (1-离散 2-流程)';
COMMENT ON COLUMN pro_bom.status IS '状态 (0-草稿 1-启用 2-停用)';

CREATE UNIQUE INDEX uk_bom_code ON pro_bom(bom_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_bom_type ON pro_bom(bom_type, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_bom_product ON pro_bom(product_id, tenant_id) WHERE is_deleted = FALSE;

-- 2. 创建BOM明细表
-- ==============================================================
CREATE TABLE pro_bom_detail (
    id BIGSERIAL PRIMARY KEY,
    bom_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    component_type INT NOT NULL DEFAULT 1,
    component_id BIGINT NOT NULL,
    component_code VARCHAR(50),
    component_name VARCHAR(100),
    specification VARCHAR(200),
    unit VARCHAR(20),
    quantity DECIMAL(19,4) NOT NULL,
    scrap_rate DECIMAL(5,2),
    effective_start_date DATE,
    effective_end_date DATE,
    is_key_component BOOLEAN NOT NULL DEFAULT FALSE,
    is_reverse_substitute BOOLEAN NOT NULL DEFAULT FALSE,
    substitute_group VARCHAR(50),
    supply_type INT,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE pro_bom_detail IS 'BOM明细表';
COMMENT ON COLUMN pro_bom_detail.component_type IS '子件类型 (1-物料 2-虚拟件 3-替代件)';
COMMENT ON COLUMN pro_bom_detail.component_id IS '子件物料ID';
COMMENT ON COLUMN pro_bom_detail.quantity IS '用量';
COMMENT ON COLUMN pro_bom_detail.scrap_rate IS '损耗率(%)';
COMMENT ON COLUMN pro_bom_detail.is_key_component IS '是否关键件';
COMMENT ON COLUMN pro_bom_detail.is_reverse_substitute IS '是否逆向替代';
COMMENT ON COLUMN pro_bom_detail.substitute_group IS '替代组';
COMMENT ON COLUMN pro_bom_detail.supply_type IS '供应类型 (1-库存 2-生产 3-外协 4-采购)';

CREATE INDEX idx_bom_detail_bom ON pro_bom_detail(bom_id);
CREATE INDEX idx_bom_detail_component ON pro_bom_detail(component_id);

-- 3. 创建工序表
-- ==============================================================
CREATE TABLE pro_work_process (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    process_code VARCHAR(50) NOT NULL,
    process_name VARCHAR(100) NOT NULL,
    process_type INT NOT NULL DEFAULT 1,
    category_id BIGINT,
    category_name VARCHAR(50),
    department_id BIGINT,
    department_name VARCHAR(100),
    work_center_id BIGINT,
    work_center_name VARCHAR(100),
    standard_man_hours DECIMAL(19,4),
    standard_machine_hours DECIMAL(19,4),
    setup_time DECIMAL(10,2),
    wait_time DECIMAL(10,2),
    labor_rate DECIMAL(19,4),
    machine_rate DECIMAL(19,4),
    variable_overhead_rate DECIMAL(19,4),
    fixed_overhead_rate DECIMAL(19,4),
    min_batch_qty DECIMAL(19,4),
    max_batch_qty DECIMAL(19,4),
    is_bottleneck BOOLEAN NOT NULL DEFAULT FALSE,
    is_quality_check BOOLEAN NOT NULL DEFAULT FALSE,
    qc_plan_id BIGINT,
    sort_order INT,
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE pro_work_process IS '工序表';
COMMENT ON COLUMN pro_work_process.process_code IS '工序编码';
COMMENT ON COLUMN pro_work_process.process_name IS '工序名称';
COMMENT ON COLUMN pro_work_process.process_type IS '工序类型 (1-普通工序 2-外协工序 3-质检工序)';
COMMENT ON COLUMN pro_work_process.category_id IS '工序分类ID';
COMMENT ON COLUMN pro_work_process.department_id IS '负责部门ID';
COMMENT ON COLUMN pro_work_process.work_center_id IS '工作中心ID';
COMMENT ON COLUMN pro_work_process.standard_man_hours IS '标准工时(分钟)';
COMMENT ON COLUMN pro_work_process.standard_machine_hours IS '标准机时(分钟)';
COMMENT ON COLUMN pro_work_process.setup_time IS '准备时间(分钟)';
COMMENT ON COLUMN pro_work_process.wait_time IS '等待时间(分钟)';
COMMENT ON COLUMN pro_work_process.labor_rate IS '人工费率';
COMMENT ON COLUMN pro_work_process.machine_rate IS '机器费率';
COMMENT ON COLUMN pro_work_process.variable_overhead_rate IS '变动制造费率';
COMMENT ON COLUMN pro_work_process.fixed_overhead_rate IS '固定制造费率';
COMMENT ON COLUMN pro_work_process.is_bottleneck IS '是否瓶颈工序';
COMMENT ON COLUMN pro_work_process.is_quality_check IS '是否质检工序';

CREATE UNIQUE INDEX uk_process_code ON pro_work_process(process_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_process_type ON pro_work_process(process_type, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_process_work_center ON pro_work_process(work_center_id, tenant_id) WHERE is_deleted = FALSE;

-- 4. 创建工艺路线表
-- ==============================================================
CREATE TABLE pro_routing (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    routing_code VARCHAR(50) NOT NULL,
    routing_name VARCHAR(100) NOT NULL,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(100),
    specification VARCHAR(200),
    routing_type INT NOT NULL DEFAULT 1,
    version VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    status INT NOT NULL DEFAULT 0,
    effective_date DATE,
    expiry_date DATE,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE pro_routing IS '工艺路线表';
COMMENT ON COLUMN pro_routing.routing_code IS '工艺路线编码';
COMMENT ON COLUMN pro_routing.routing_name IS '工艺路线名称';
COMMENT ON COLUMN pro_routing.product_id IS '产品ID';
COMMENT ON COLUMN pro_routing.routing_type IS '工艺路线类型 (1-标准工艺 2-替代工艺)';
COMMENT ON COLUMN pro_routing.version IS '版本号';
COMMENT ON COLUMN pro_routing.is_default IS '默认标识';
COMMENT ON COLUMN pro_routing.status IS '状态 (0-草稿 1-启用 2-停用)';

CREATE UNIQUE INDEX uk_routing_code ON pro_routing(routing_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_routing_product ON pro_routing(product_id, tenant_id) WHERE is_deleted = FALSE;

-- 5. 创建工艺路线明细表
-- ==============================================================
CREATE TABLE pro_routing_detail (
    id BIGSERIAL PRIMARY KEY,
    routing_id BIGINT NOT NULL,
    sequence_no INT NOT NULL,
    process_id BIGINT NOT NULL,
    process_code VARCHAR(50),
    process_name VARCHAR(100),
    work_center_id BIGINT,
    work_center_name VARCHAR(100),
    standard_man_hours DECIMAL(19,4),
    standard_machine_hours DECIMAL(19,4),
    setup_time DECIMAL(10,2),
    wait_time DECIMAL(10,2),
    move_time DECIMAL(10,2),
    labor_rate DECIMAL(19,4),
    machine_rate DECIMAL(19,4),
    variable_overhead_rate DECIMAL(19,4),
    fixed_overhead_rate DECIMAL(19,4),
    min_batch_qty DECIMAL(19,4),
    max_batch_qty DECIMAL(19,4),
    is_parallel BOOLEAN NOT NULL DEFAULT FALSE,
    is_overlap BOOLEAN NOT NULL DEFAULT FALSE,
    parallel_group_no INT,
    next_sequence_no INT,
    alternative_process_id BIGINT,
    check_items TEXT,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE pro_routing_detail IS '工艺路线明细表';
COMMENT ON COLUMN pro_routing_detail.sequence_no IS '顺序号';
COMMENT ON COLUMN pro_routing_detail.process_id IS '工序ID';
COMMENT ON COLUMN pro_routing_detail.work_center_id IS '工作中心ID';
COMMENT ON COLUMN pro_routing_detail.is_parallel IS '是否并行工序';
COMMENT ON COLUMN pro_routing_detail.is_overlap IS '是否重叠工序';
COMMENT ON COLUMN pro_routing_detail.parallel_group_no IS '并行组号';

CREATE INDEX idx_routing_detail_routing ON pro_routing_detail(routing_id);
CREATE INDEX idx_routing_detail_process ON pro_routing_detail(process_id);

-- 6. 创建生产订单表
-- ==============================================================
CREATE TABLE pro_production_order (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(50) NOT NULL,
    order_type INT NOT NULL DEFAULT 1,
    product_id BIGINT NOT NULL,
    product_code VARCHAR(50),
    product_name VARCHAR(100),
    specification VARCHAR(200),
    unit VARCHAR(20),
    planned_qty DECIMAL(19,4) NOT NULL,
    completed_qty DECIMAL(19,4) DEFAULT 0,
    scrapped_qty DECIMAL(19,4) DEFAULT 0,
    bom_id BIGINT,
    bom_version VARCHAR(20),
    routing_id BIGINT,
    plan_start_date DATE,
    plan_end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    workshop_id BIGINT,
    workshop_name VARCHAR(100),
    production_line_id BIGINT,
    production_line_name VARCHAR(100),
    status INT NOT NULL DEFAULT 0,
    priority INT NOT NULL DEFAULT 3,
    source_type VARCHAR(50),
    source_id BIGINT,
    source_no VARCHAR(50),
    demand_user_id BIGINT,
    demand_user_name VARCHAR(50),
    created_by_id BIGINT,
    created_by_name VARCHAR(50),
    approved_by_id BIGINT,
    approved_by_name VARCHAR(50),
    approved_at TIMESTAMP,
    remark VARCHAR(500),
    attachments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE pro_production_order IS '生产订单表';
COMMENT ON COLUMN pro_production_order.order_no IS '生产订单号';
COMMENT ON COLUMN pro_production_order.order_type IS '订单类型 (1-标准订单 2-返工订单 3-拆解订单)';
COMMENT ON COLUMN pro_production_order.product_id IS '产品ID';
COMMENT ON COLUMN pro_production_order.planned_qty IS '计划数量';
COMMENT ON COLUMN pro_production_order.completed_qty IS '完工数量';
COMMENT ON COLUMN pro_production_order.bom_id IS 'BOM ID';
COMMENT ON COLUMN pro_production_order.routing_id IS '工艺路线ID';
COMMENT ON COLUMN pro_production_order.status IS '状态 (0-草稿 1-已审核 2-生产中 3-已完工 4-已关闭 5-已取消)';
COMMENT ON COLUMN pro_production_order.priority IS '优先级 (1-紧急 2-高 3-正常 4-低)';

CREATE UNIQUE INDEX uk_order_no ON pro_production_order(order_no, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_order_product ON pro_production_order(product_id, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_order_status ON pro_production_order(status, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_order_date ON pro_production_order(plan_start_date, tenant_id) WHERE is_deleted = FALSE;

-- 7. 创建生产订单明细表
-- ==============================================================
CREATE TABLE pro_production_order_detail (
    id BIGSERIAL PRIMARY KEY,
    production_order_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    detail_type INT NOT NULL DEFAULT 1,
    material_id BIGINT NOT NULL,
    material_code VARCHAR(50),
    material_name VARCHAR(100),
    specification VARCHAR(200),
    unit VARCHAR(20),
    required_qty DECIMAL(19,4) NOT NULL,
    issued_qty DECIMAL(19,4) DEFAULT 0,
    received_qty DECIMAL(19,4) DEFAULT 0,
    warehouse_id BIGINT,
    warehouse_name VARCHAR(100),
    location VARCHAR(50),
    batch_no VARCHAR(50),
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE pro_production_order_detail IS '生产订单明细表';
COMMENT ON COLUMN pro_production_order_detail.detail_type IS '明细类型 (1-子件 2-副产品 3-联产品)';
COMMENT ON COLUMN pro_production_order_detail.material_id IS '物料ID';
COMMENT ON COLUMN pro_production_order_detail.required_qty IS '需求数量';
COMMENT ON COLUMN pro_production_order_detail.issued_qty IS '领料数量';
COMMENT ON COLUMN pro_production_order_detail.received_qty IS '入库数量';

CREATE INDEX idx_order_detail_order ON pro_production_order_detail(production_order_id);
CREATE INDEX idx_order_detail_material ON pro_production_order_detail(material_id);

-- 8. 创建工序执行记录表
-- ==============================================================
CREATE TABLE pro_operation_record (
    id BIGSERIAL PRIMARY KEY,
    production_order_id BIGINT NOT NULL,
    sequence_no INT NOT NULL,
    process_id BIGINT NOT NULL,
    process_code VARCHAR(50),
    process_name VARCHAR(100),
    work_center_id BIGINT,
    work_center_name VARCHAR(100),
    planned_qty DECIMAL(19,4) NOT NULL,
    completed_qty DECIMAL(19,4) DEFAULT 0,
    qualified_qty DECIMAL(19,4) DEFAULT 0,
    scrapped_qty DECIMAL(19,4) DEFAULT 0,
    plan_start_time TIMESTAMP,
    plan_end_time TIMESTAMP,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    worker_id BIGINT,
    worker_name VARCHAR(50),
    status INT NOT NULL DEFAULT 0,
    actual_man_hours DECIMAL(10,2),
    actual_machine_hours DECIMAL(10,2),
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE pro_operation_record IS '工序执行记录表';
COMMENT ON COLUMN pro_operation_record.sequence_no IS '顺序号';
COMMENT ON COLUMN pro_operation_record.process_id IS '工序ID';
COMMENT ON COLUMN pro_operation_record.status IS '状态 (0-待开工 1-进行中 2-已完成 3-已暂停)';

CREATE INDEX idx_operation_record_order ON pro_operation_record(production_order_id);
CREATE INDEX idx_operation_record_process ON pro_operation_record(process_id);
CREATE INDEX idx_operation_record_status ON pro_operation_record(status);

-- 9. 插入示例数据
-- ==============================================================
INSERT INTO pro_work_process (tenant_id, process_code, process_name, process_type, status)
VALUES
(0, 'PROC001', '裁剪', 1, 1),
(0, 'PROC002', '缝制', 1, 1),
(0, 'PROC003', '整烫', 1, 1),
(0, 'PROC004', '质检', 3, 1);

INSERT INTO pro_bom (tenant_id, bom_code, bom_name, bom_type, product_id, product_code, product_name, unit, bom_qty, status)
VALUES
(0, 'BOM001', '示例产品BOM', 1, 1, 'P001', '示例产品', '个', 1, 1);
