-- CRM 客户管理系统数据库初始化脚本
-- 基于 xarch 框架的 sys_* 表结构扩展

-- 客户表
CREATE TABLE IF NOT EXISTS crm_customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL COMMENT '客户名称',
    customer_type VARCHAR(20) COMMENT 'enterprise-企业/individual-个人',
    industry VARCHAR(50) COMMENT '所属行业',
    level VARCHAR(10) DEFAULT 'C' COMMENT '客户级别 A/B/C',
    source VARCHAR(50) COMMENT '客户来源',
    website VARCHAR(255) COMMENT '公司网站',
    employee_count INT COMMENT '员工规模',
    annual_revenue DECIMAL(15,2) COMMENT '年营业额',
    owner_id BIGINT COMMENT '负责人ID',
    owner_name VARCHAR(50) COMMENT '负责人姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(255) COMMENT '地址',
    status INT DEFAULT 1 COMMENT '1-正常/0-流失/2-未合作',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_level (level),
    INDEX idx_industry (industry),
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 联系人表
CREATE TABLE IF NOT EXISTS crm_contact (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    position VARCHAR(50) COMMENT '职位',
    phone VARCHAR(20) COMMENT '电话',
    mobile VARCHAR(20) COMMENT '手机',
    email VARCHAR(100) COMMENT '邮箱',
    is_primary INT DEFAULT 0 COMMENT '是否主联系人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联系人表';

-- 销售机会表
CREATE TABLE IF NOT EXISTS crm_opportunity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    opportunity_name VARCHAR(200) NOT NULL COMMENT '机会名称',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    contact_id BIGINT COMMENT '联系人ID',
    amount DECIMAL(15,2) COMMENT '预计金额',
    stage VARCHAR(30) DEFAULT 'qualification' COMMENT '阶段',
    probability INT DEFAULT 0 COMMENT '赢单概率',
    expected_date DATE COMMENT '预计成交日期',
    description VARCHAR(500) COMMENT '描述',
    status INT DEFAULT 1 COMMENT '1-进行中/0-已关闭/2-已输单',
    closed_reason VARCHAR(255) COMMENT '关闭原因',
    owner_id BIGINT COMMENT '负责人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_customer (customer_id),
    INDEX idx_stage (stage),
    INDEX idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售机会表';

-- 合同表
CREATE TABLE IF NOT EXISTS crm_contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_no VARCHAR(50) NOT NULL COMMENT '合同编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    opportunity_id BIGINT COMMENT '关联机会ID',
    amount DECIMAL(15,2) NOT NULL COMMENT '合同金额',
    sign_date DATE COMMENT '签订日期',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    payment_status VARCHAR(20) DEFAULT 'unpaid' COMMENT '付款状态',
    status INT DEFAULT 1 COMMENT '1-执行中/0-已终止/2-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_customer (customer_id),
    INDEX idx_contract_no (contract_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- 跟进记录表
CREATE TABLE IF NOT EXISTS crm_follow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT COMMENT '客户ID',
    opportunity_id BIGINT COMMENT '机会ID',
    follow_type VARCHAR(20) NOT NULL COMMENT '跟进方式',
    content VARCHAR(500) COMMENT '跟进内容',
    next_time DATETIME COMMENT '下次跟进时间',
    next_content VARCHAR(255) COMMENT '下次跟进内容',
    follow_time DATETIME COMMENT '跟进时间',
    follow_by BIGINT COMMENT '跟进人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_customer (customer_id),
    INDEX idx_opportunity (opportunity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进记录表';

-- 回款记录表
CREATE TABLE IF NOT EXISTS crm_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    amount DECIMAL(15,2) NOT NULL COMMENT '回款金额',
    payment_date DATE NOT NULL COMMENT '回款日期',
    payment_method VARCHAR(20) COMMENT '付款方式',
    payment_type VARCHAR(20) COMMENT 'prepaid-预付款/arrears-欠款/final-尾款',
    invoice_status INT DEFAULT 0 COMMENT '开票状态 0-未开票/1-已开票',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_contract (contract_id),
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回款记录表';

-- 插入测试数据
INSERT INTO crm_customer (customer_name, customer_type, industry, level, source, employee_count, annual_revenue, owner_id, owner_name, phone, email) VALUES
('上海科技有限公司', 'enterprise', 'IT', 'A', '网络推广', 100, 10000000, 1, '张三', '021-12345678', 'contact@shtech.com'),
('成都实业有限公司', 'enterprise', '制造业', 'B', '展会', 500, 50000000, 1, '张三', '028-87654321', 'contact@cdind.com'),
('深圳网络科技', 'enterprise', '互联网', 'A', '客户推荐', 50, 8000000, 2, '李四', '0755-11112222', 'contact@sznet.com');

INSERT INTO crm_contact (customer_id, name, position, phone, email, is_primary) VALUES
(1, '王经理', '技术总监', '13800001111', 'wang@shtech.com', 1),
(1, '刘助理', '行政助理', '13800001112', 'liu@shtech.com', 0),
(2, '陈总', '总经理', '13900001111', 'chen@cdind.com', 1),
(3, '赵总', 'CEO', '13700001111', 'zhao@sznet.com', 1);

INSERT INTO crm_opportunity (opportunity_name, customer_id, amount, stage, probability, expected_date, owner_id) VALUES
('上海科技CRM采购项目', 1, 500000, 'proposal', 50, '2024-03-01', 1),
('成都实业ERP项目', 2, 800000, 'negotiation', 70, '2024-02-15', 1),
('深圳网络官网建设', 3, 200000, 'qualification', 30, '2024-04-01', 2);

INSERT INTO crm_contract (contract_no, customer_id, opportunity_id, amount, sign_date, start_date, end_date, payment_status) VALUES
('HT202401001', 1, 1, 500000, '2024-01-15', '2024-01-15', '2025-01-14', 'prepaid'),
('HT202312001', 2, 2, 800000, '2023-12-01', '2023-12-01', '2024-11-30', 'arrears');