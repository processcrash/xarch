-- xarch PostgreSQL Database Schema
-- Migrated from MySQL (RuoYi-Vue)
-- PostgreSQL compatible syntax

-- Create database (run as superuser)
-- CREATE DATABASE xarch WITH ENCODING = 'UTF8' OWNER = postgres;

-- ======================
-- System Module Tables
-- ======================

-- Department table (sys_dept)
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    ancestors VARCHAR(500) DEFAULT '',
    dept_name VARCHAR(50) DEFAULT '',
    order_num INT DEFAULT 0,
    leader VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    status CHAR(1) DEFAULT '0',
    del_flag CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL
);

COMMENT ON TABLE sys_dept IS 'Department table';
COMMENT ON COLUMN sys_dept.dept_id IS 'Department ID';
COMMENT ON COLUMN sys_dept.parent_id IS 'Parent department ID';
COMMENT ON COLUMN sys_dept.ancestors IS 'Ancestors';
COMMENT ON COLUMN sys_dept.dept_name IS 'Department name';
COMMENT ON COLUMN sys_dept.order_num IS 'Display order';
COMMENT ON COLUMN sys_dept.leader IS 'Leader name';
COMMENT ON COLUMN sys_dept.phone IS 'Contact phone';
COMMENT ON COLUMN sys_dept.email IS 'Email';
COMMENT ON COLUMN sys_dept.status IS 'Status: 0=normal, 1=disabled';
COMMENT ON COLUMN sys_dept.del_flag IS 'Delete flag: 0=normal, 1=deleted';

-- User table (sys_user)
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dept_id BIGINT DEFAULT NULL,
    user_name VARCHAR(50) NOT NULL,
    nick_name VARCHAR(50) DEFAULT '',
    user_type VARCHAR(20) DEFAULT '1',
    email VARCHAR(100) DEFAULT '',
    phonenumber VARCHAR(20) DEFAULT '',
    sex CHAR(1) DEFAULT '0',
    avatar VARCHAR(255) DEFAULT '',
    password VARCHAR(200) DEFAULT '',
    status CHAR(1) DEFAULT '0',
    del_flag CHAR(1) DEFAULT '0',
    login_ip VARCHAR(50) DEFAULT '',
    login_date TIMESTAMP DEFAULT NULL,
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL
);

CREATE INDEX idx_sys_user_dept_id ON sys_user(dept_id);
COMMENT ON TABLE sys_user IS 'User table';
COMMENT ON COLUMN sys_user.user_name IS 'Username';

-- Role table (sys_role)
CREATE TABLE IF NOT EXISTS sys_role (
    role_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_key VARCHAR(50) NOT NULL,
    role_sort INT NOT NULL,
    data_scope CHAR(1) DEFAULT '1',
    menu_check_strictly SMALLINT DEFAULT 1,
    dept_check_strictly SMALLINT DEFAULT 1,
    status CHAR(1) NOT NULL,
    del_flag CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL
);

COMMENT ON TABLE sys_role IS 'Role table';

-- Menu/Permission table (sys_menu)
CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    menu_name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    order_num INT DEFAULT 0,
    path VARCHAR(200) DEFAULT '',
    component VARCHAR(255) DEFAULT NULL,
    query VARCHAR(255) DEFAULT NULL,
    is_frame INT DEFAULT 1,
    is_cache INT DEFAULT 0,
    menu_type CHAR(1) DEFAULT '',
    visible CHAR(1) DEFAULT '0',
    status CHAR(1) DEFAULT '0',
    perms VARCHAR(100) DEFAULT NULL,
    icon VARCHAR(100) DEFAULT '#',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT ''
);

COMMENT ON TABLE sys_menu IS 'Menu/Permission table';

-- Post table (sys_post)
CREATE TABLE IF NOT EXISTS sys_post (
    post_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_code VARCHAR(50) NOT NULL,
    post_name VARCHAR(50) NOT NULL,
    post_sort INT NOT NULL,
    status CHAR(1) NOT NULL,
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    UNIQUE (post_code)
);

COMMENT ON TABLE sys_post IS 'Post/Position table';

-- ======================
-- Relation Tables
-- ======================

-- User-Post relation (sys_user_post)
CREATE TABLE IF NOT EXISTS sys_user_post (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, post_id)
);

-- User-Role relation (sys_user_role)
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- Role-Menu permission (sys_role_menu)
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- Role-Department relation (sys_role_dept)
CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, dept_id)
);

-- ======================
-- System Config Tables
-- ======================

-- Dictionary type (sys_dict_type)
CREATE TABLE IF NOT EXISTS sys_dict_type (
    dict_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dict_name VARCHAR(100) DEFAULT '',
    dict_type VARCHAR(100) DEFAULT '',
    status CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    UNIQUE (dict_type)
);

-- Dictionary data (sys_dict_data)
CREATE TABLE IF NOT EXISTS sys_dict_data (
    dict_code BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dict_sort INT DEFAULT 0,
    dict_label VARCHAR(100) DEFAULT '',
    dict_value VARCHAR(100) DEFAULT '',
    dict_type VARCHAR(100) DEFAULT '',
    css_class VARCHAR(100) DEFAULT NULL,
    list_class VARCHAR(100) DEFAULT NULL,
    is_default CHAR(1) DEFAULT 'N',
    status CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL
);

CREATE INDEX idx_sys_dict_data_type ON sys_dict_data(dict_type);

-- System config (sys_config)
CREATE TABLE IF NOT EXISTS sys_config (
    config_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    config_name VARCHAR(100) DEFAULT '',
    config_key VARCHAR(100) DEFAULT '',
    config_value VARCHAR(500) DEFAULT '',
    config_type CHAR(1) DEFAULT 'N',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    UNIQUE (config_key)
);

-- ======================
-- Storage Module Tables
-- ======================

-- Resource file table (sys_resource)
CREATE TABLE IF NOT EXISTS sys_resource (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    resource_name VARCHAR(255) DEFAULT '',
    object_key VARCHAR(500) DEFAULT '',
    access_url VARCHAR(500) DEFAULT '',
    scene_code VARCHAR(100) DEFAULT '',
    file_size BIGINT DEFAULT 0,
    file_type VARCHAR(100) DEFAULT '',
    storage_type VARCHAR(20) DEFAULT 'local',
    biz_key VARCHAR(100) DEFAULT '',
    create_user_id BIGINT DEFAULT NULL,
    create_user_name VARCHAR(100) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_time TIMESTAMP DEFAULT NULL,
    del_flag INTEGER DEFAULT 0
);

COMMENT ON TABLE sys_resource IS 'File resource table';
COMMENT ON COLUMN sys_resource.resource_name IS 'Resource name (original filename)';
COMMENT ON COLUMN sys_resource.object_key IS 'Storage object key';
COMMENT ON COLUMN sys_resource.access_url IS 'Access URL';
COMMENT ON COLUMN sys_resource.scene_code IS 'Scene code for categorization';
COMMENT ON COLUMN sys_resource.file_size IS 'File size in bytes';
COMMENT ON COLUMN sys_resource.file_type IS 'MIME type';
COMMENT ON COLUMN sys_resource.storage_type IS 'Storage type: local, minio, aliyun_oss';
COMMENT ON COLUMN sys_resource.biz_key IS 'Business key for grouping';

-- Storage configuration table (sys_storage_config)
CREATE TABLE IF NOT EXISTS sys_storage_config (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    storage_type VARCHAR(20) DEFAULT 'local',
    config_name VARCHAR(100) NOT NULL,
    is_default INTEGER DEFAULT 0,
    endpoint VARCHAR(255) DEFAULT '',
    access_key VARCHAR(255) DEFAULT '',
    secret_key VARCHAR(255) DEFAULT '',
    bucket_name VARCHAR(100) DEFAULT '',
    region VARCHAR(100) DEFAULT '',
    base_path VARCHAR(255) DEFAULT '',
    domain VARCHAR(255) DEFAULT '',
    status INTEGER DEFAULT 1,
    description VARCHAR(500) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_time TIMESTAMP DEFAULT NULL,
    del_flag INTEGER DEFAULT 0
);

COMMENT ON TABLE sys_storage_config IS 'Storage configuration table';
COMMENT ON COLUMN sys_storage_config.storage_type IS 'Storage type: local, minio, aliyun_oss';
COMMENT ON COLUMN sys_storage_config.config_name IS 'Configuration name';
COMMENT ON COLUMN sys_storage_config.is_default IS 'Is default: 0=no, 1=yes';
COMMENT ON COLUMN sys_storage_config.endpoint IS 'Endpoint URL';
COMMENT ON COLUMN sys_storage_config.access_key IS 'Access key / Access ID';
COMMENT ON COLUMN sys_storage_config.secret_key IS 'Secret key';
COMMENT ON COLUMN sys_storage_config.bucket_name IS 'Bucket name';
COMMENT ON COLUMN sys_storage_config.region IS 'Region';
COMMENT ON COLUMN sys_storage_config.base_path IS 'Base path for files';
COMMENT ON COLUMN sys_storage_config.domain IS 'Domain/CDN for accessing files';
COMMENT ON COLUMN sys_storage_config.status IS 'Status: 0-disabled, 1-enabled';

-- ======================
-- Notice Table
-- ======================

-- Notice/Announcement (sys_notice)
CREATE TABLE IF NOT EXISTS sys_notice (
    notice_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    notice_title VARCHAR(50) NOT NULL,
    notice_type CHAR(1) NOT NULL,
    notice_content TEXT DEFAULT NULL,
    status CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL
);

-- ======================
-- Logging Tables
-- ======================

-- Operation log (sys_oper_log)
CREATE TABLE IF NOT EXISTS sys_oper_log (
    oper_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(50) DEFAULT '',
    business_type INT DEFAULT 0,
    method VARCHAR(100) DEFAULT '',
    request_method VARCHAR(10) DEFAULT '',
    operator_type INT DEFAULT 0,
    oper_name VARCHAR(50) DEFAULT '',
    dept_name VARCHAR(50) DEFAULT '',
    oper_url VARCHAR(255) DEFAULT '',
    oper_ip VARCHAR(50) DEFAULT '',
    oper_location VARCHAR(255) DEFAULT '',
    oper_param VARCHAR(2000) DEFAULT '',
    json_result VARCHAR(2000) DEFAULT '',
    status INT DEFAULT 0,
    error_msg VARCHAR(2000) DEFAULT '',
    oper_time TIMESTAMP DEFAULT NULL
);

CREATE INDEX idx_sys_oper_log_time ON sys_oper_log(oper_time);
CREATE INDEX idx_sys_oper_log_status ON sys_oper_log(status);

-- Login log (sys_logininfor)
CREATE TABLE IF NOT EXISTS sys_logininfor (
    info_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_name VARCHAR(50) DEFAULT '',
    ipaddr VARCHAR(50) DEFAULT '',
    login_location VARCHAR(255) DEFAULT '',
    browser VARCHAR(50) DEFAULT '',
    os VARCHAR(50) DEFAULT '',
    status CHAR(1) DEFAULT '0',
    msg VARCHAR(255) DEFAULT '',
    login_time TIMESTAMP DEFAULT NULL
);

CREATE INDEX idx_sys_logininfor_ipaddr ON sys_logininfor(ipaddr);
CREATE INDEX idx_sys_logininfor_user_name ON sys_logininfor(user_name);
CREATE INDEX idx_sys_logininfor_status ON sys_logininfor(status);
CREATE INDEX idx_sys_logininfor_login_time ON sys_logininfor(login_time);

-- ======================
-- Quartz Scheduler Tables
-- ======================

-- Scheduled task (sys_job)
CREATE TABLE IF NOT EXISTS sys_job (
    job_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_name VARCHAR(50) NOT NULL,
    job_group VARCHAR(50) DEFAULT 'DEFAULT',
    invoke_target VARCHAR(500) NOT NULL,
    cron_expression VARCHAR(255) DEFAULT '',
    misfire_policy VARCHAR(20) DEFAULT '0',
    concurrent VARCHAR(20) DEFAULT '1',
    status CHAR(1) DEFAULT '0',
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT NULL,
    update_by VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    UNIQUE (job_id, job_group)
);

-- Task execution log (sys_job_log)
CREATE TABLE IF NOT EXISTS sys_job_log (
    job_log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_name VARCHAR(50) NOT NULL,
    job_group VARCHAR(50) DEFAULT 'DEFAULT',
    invoke_target VARCHAR(500) DEFAULT '',
    job_message VARCHAR(500) DEFAULT NULL,
    status CHAR(1) DEFAULT '0',
    exception_info VARCHAR(2000) DEFAULT '',
    start_time TIMESTAMP DEFAULT NULL,
    end_time TIMESTAMP DEFAULT NULL
);

-- ======================
-- Sample Data
-- ======================

-- Insert sample department
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, create_by, create_time) VALUES
(100, 0, '0', 'Headquarters', 0, 'Admin', '15888888888', 'admin@xarch.com', '0', 'admin', CURRENT_TIMESTAMP),
(101, 100, '0,100', 'Technology Department', 1, 'Tech Lead', '15888888889', 'tech@xarch.com', '0', 'admin', CURRENT_TIMESTAMP),
(102, 100, '0,100', 'Sales Department', 2, 'Sales Lead', '15888888890', 'sales@xarch.com', '0', 'admin', CURRENT_TIMESTAMP);

-- Insert sample post
INSERT INTO sys_post (post_code, post_name, post_sort, status, create_by, create_time) VALUES
('CEO', 'Chief Executive Officer', 1, '0', 'admin', CURRENT_TIMESTAMP),
('CTO', 'Chief Technology Officer', 2, '0', 'admin', CURRENT_TIMESTAMP),
('DEV', 'Developer', 3, '0', 'admin', CURRENT_TIMESTAMP),
('TEST', 'Tester', 4, '0', 'admin', CURRENT_TIMESTAMP),
('HR', 'Human Resources', 5, '0', 'admin', CURRENT_TIMESTAMP);

-- Insert sample user (password: admin123, bcrypt hash)
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, create_by, create_time) VALUES
(100, 'admin', 'Administrator', '1', 'admin@xarch.com', '15888888888', '0', '$2a$10$7JB720yub1V7G2v0pZeV3u1iZ1fYk9Z3v8n6VXqZQZQZQZQZQZQZQ', '0', 'admin', CURRENT_TIMESTAMP),
(101, 'user01', 'User One', '1', 'user01@xarch.com', '15888888889', '0', '$2a$10$7JB720yub1V7G2v0pZeV3u1iZ1fYk9Z3v8n6VXqZQZQZQZQZQZQZQ', '0', 'admin', CURRENT_TIMESTAMP);

-- Insert sample role
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, status, create_by, create_time, remark) VALUES
('Super Admin', 'admin', 1, '1', '0', 'admin', CURRENT_TIMESTAMP, 'Super administrator'),
('Common User', 'common', 2, '2', '0', 'admin', CURRENT_TIMESTAMP, 'Common role');

-- Insert sample menu
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('System', 0, 1, 'system', NULL, 'M', '0', '0', '', 'el-icon-system', 'admin', CURRENT_TIMESTAMP),
('User Management', 1, 1, 'user', 'system/user/index', 'C', '0', '0', 'system:user:list', 'el-icon-user', 'admin', CURRENT_TIMESTAMP),
('Role Management', 1, 2, 'role', 'system/role/index', 'C', '0', '0', 'system:role:list', 'el-icon-role', 'admin', CURRENT_TIMESTAMP),
('Menu Management', 1, 3, 'menu', 'system/menu/index', 'C', '0', '0', 'system:menu:list', 'el-icon-menu', 'admin', CURRENT_TIMESTAMP),
('Department Management', 1, 4, 'dept', 'system/dept/index', 'C', '0', '0', 'system:dept:list', 'el-icon-dept', 'admin', CURRENT_TIMESTAMP),
('Post Management', 1, 5, 'post', 'system/post/index', 'C', '0', '0', 'system:post:list', 'el-icon-post', 'admin', CURRENT_TIMESTAMP),
('System Monitoring', 0, 2, 'monitor', NULL, 'M', '0', '0', '', 'el-icon-monitor', 'admin', CURRENT_TIMESTAMP),
('Online Users', 7, 1, 'online', 'monitor/online/index', 'C', '0', '0', 'monitor:online:list', 'el-icon-online', 'admin', CURRENT_TIMESTAMP),
('Operation Log', 7, 2, 'operlog', 'monitor/operlog/index', 'C', '0', '0', 'monitor:operlog:list', 'el-icon-log', 'admin', CURRENT_TIMESTAMP),
('Login Log', 7, 3, 'logininfor', 'monitor/logininfor/index', 'C', '0', '0', 'monitor:logininfor:list', 'el-icon-login', 'admin', CURRENT_TIMESTAMP),
('System Tools', 0, 3, 'tool', NULL, 'M', '0', '0', '', 'el-icon-tool', 'admin', CURRENT_TIMESTAMP);

-- User-Role relation
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Role-Menu relation
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11);

-- Role-Department relation
INSERT INTO sys_role_dept (role_id, dept_id) VALUES (1, 100), (1, 101), (1, 102);

-- Insert dictionary type
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('Gender', 'sys_user_sex', '0', 'admin', CURRENT_TIMESTAMP, 'Gender dictionary'),
('Menu Status', 'sys_show_hide', '0', 'admin', CURRENT_TIMESTAMP, 'Menu status dictionary'),
('System Status', 'sys_normal_disable', '0', 'admin', CURRENT_TIMESTAMP, 'System status dictionary');

-- Insert dictionary data
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'Male', '0', 'sys_user_sex', '', 'default', 'Y', '0', 'admin', CURRENT_TIMESTAMP),
(2, 'Female', '1', 'sys_user_sex', '', 'danger', 'N', '0', 'admin', CURRENT_TIMESTAMP),
(3, 'Unknown', '2', 'sys_user_sex', '', 'info', 'N', '0', 'admin', CURRENT_TIMESTAMP),
(1, 'Show', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', CURRENT_TIMESTAMP),
(2, 'Hide', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', CURRENT_TIMESTAMP),
(1, 'Normal', '0', 'sys_normal_disable', '', 'success', 'Y', '0', 'admin', CURRENT_TIMESTAMP),
(2, 'Disabled', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', CURRENT_TIMESTAMP);

-- Insert system config
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('Main framework page - Default skin', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', CURRENT_TIMESTAMP, 'Skin name'),
('User management - Initial password', 'sys.user.initPassword', 'admin123', 'Y', 'admin', CURRENT_TIMESTAMP, 'Initial password'),
('Main framework page - Sidebar skin', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', CURRENT_TIMESTAMP, 'Sidebar skin'),
('Captcha switch', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', CURRENT_TIMESTAMP, 'Captcha enabled'),
('Username retrieval', 'sys.account.userNameEnabled', 'false', 'Y', 'admin', CURRENT_TIMESTAMP, 'Username retrieval enabled');