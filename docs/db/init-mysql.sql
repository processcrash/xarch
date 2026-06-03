-- xarch Database Schema
-- Based on RuoYi-Vue

CREATE DATABASE IF NOT EXISTS xarch DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xarch;

-- ======================
-- System Module Tables
-- ======================

-- Department table (sys_dept)
CREATE TABLE IF NOT EXISTS sys_dept (
    dept_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Department ID',
    parent_id BIGINT DEFAULT 0 COMMENT 'Parent department ID',
    ancestors VARCHAR(500) DEFAULT '' COMMENT 'Ancestors',
    dept_name VARCHAR(50) DEFAULT '' COMMENT 'Department name',
    order_num INT DEFAULT 0 COMMENT 'Display order',
    leader VARCHAR(50) DEFAULT NULL COMMENT 'Leader name',
    phone VARCHAR(20) DEFAULT NULL COMMENT 'Contact phone',
    email VARCHAR(100) DEFAULT NULL COMMENT 'Email',
    status CHAR(1) DEFAULT '0' COMMENT 'Status: 0=normal, 1=disabled',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag: 0=normal, 1=deleted',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    PRIMARY KEY (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Department table';

-- User table (sys_user)
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'User ID',
    dept_id BIGINT DEFAULT NULL COMMENT 'Department ID',
    user_name VARCHAR(50) NOT NULL COMMENT 'Username',
    nick_name VARCHAR(50) DEFAULT '' COMMENT 'Nick name',
    user_type VARCHAR(20) DEFAULT '1' COMMENT 'User type: 1=ordinary, 2=system',
    email VARCHAR(100) DEFAULT '' COMMENT 'Email',
    phonenumber VARCHAR(20) DEFAULT '' COMMENT 'Phone number',
    sex CHAR(1) DEFAULT '0' COMMENT 'Gender: 0=male, 1=female, 2=unknown',
    avatar VARCHAR(255) DEFAULT '' COMMENT 'Avatar',
    password VARCHAR(100) DEFAULT '' COMMENT 'Password',
    status CHAR(1) DEFAULT '0' COMMENT 'Status: 0=normal, 1=disabled',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag: 0=normal, 1=deleted',
    login_ip VARCHAR(50) DEFAULT '' COMMENT 'Last login IP',
    login_date DATETIME DEFAULT NULL COMMENT 'Last login time',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (user_id),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- Role table (sys_role)
CREATE TABLE IF NOT EXISTS sys_role (
    role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Role ID',
    role_name VARCHAR(50) NOT NULL COMMENT 'Role name',
    role_key VARCHAR(50) NOT NULL COMMENT 'Role key',
    role_sort INT NOT NULL COMMENT 'Display order',
    data_scope CHAR(1) DEFAULT '1' COMMENT 'Data scope',
    menu_check_strictly TINYINT DEFAULT 1 COMMENT 'Menu tree strictly',
    dept_check_strictly TINYINT DEFAULT 1 COMMENT 'Department tree strictly',
    status CHAR(1) NOT NULL COMMENT 'Status: 0=normal, 1=disabled',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag: 0=normal, 1=deleted',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role table';

-- Menu/Permission table (sys_menu)
CREATE TABLE IF NOT EXISTS sys_menu (
    menu_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Menu ID',
    menu_name VARCHAR(50) NOT NULL COMMENT 'Menu name',
    parent_id BIGINT DEFAULT 0 COMMENT 'Parent menu ID',
    order_num INT DEFAULT 0 COMMENT 'Display order',
    path VARCHAR(200) DEFAULT '' COMMENT 'Route path',
    component VARCHAR(255) DEFAULT NULL COMMENT 'Component path',
    query VARCHAR(255) DEFAULT NULL COMMENT 'Route query string',
    is_frame INT DEFAULT 1 COMMENT 'Is external link: 0=yes, 1=no',
    is_cache INT DEFAULT 0 COMMENT 'Is cached: 0=cached, 1=not cached',
    menu_type CHAR(1) DEFAULT '' COMMENT 'Menu type: M=directory, C=menu, F=button',
    visible CHAR(1) DEFAULT '0' COMMENT 'Visible: 0=show, 1=hide',
    status CHAR(1) DEFAULT '0' COMMENT 'Status: 0=normal, 1=disabled',
    perms VARCHAR(100) DEFAULT NULL COMMENT 'Permission string',
    icon VARCHAR(100) DEFAULT '#' COMMENT 'Menu icon',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT '' COMMENT 'Remark',
    PRIMARY KEY (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Menu/Permission table';

-- Post table (sys_post)
CREATE TABLE IF NOT EXISTS sys_post (
    post_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Post ID',
    post_code VARCHAR(50) NOT NULL COMMENT 'Post code',
    post_name VARCHAR(50) NOT NULL COMMENT 'Post name',
    post_sort INT NOT NULL COMMENT 'Display order',
    status CHAR(1) NOT NULL COMMENT 'Status: 0=normal, 1=disabled',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (post_id),
    UNIQUE KEY uk_post_code (post_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Post/Position table';

-- ======================
-- Relation Tables
-- ======================

-- User-Post relation (sys_user_post)
CREATE TABLE IF NOT EXISTS sys_user_post (
    user_id BIGINT NOT NULL COMMENT 'User ID',
    post_id BIGINT NOT NULL COMMENT 'Post ID',
    PRIMARY KEY (user_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-Post relation';

-- User-Role relation (sys_user_role)
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT 'User ID',
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User-Role relation';

-- Role-Menu permission (sys_role_menu)
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    menu_id BIGINT NOT NULL COMMENT 'Menu ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role-Menu relation';

-- Role-Department relation (sys_role_dept)
CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    dept_id BIGINT NOT NULL COMMENT 'Department ID',
    PRIMARY KEY (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Role-Department relation';

-- ======================
-- System Config Tables
-- ======================

-- Dictionary type (sys_dict_type)
CREATE TABLE IF NOT EXISTS sys_dict_type (
    dict_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Dictionary ID',
    dict_name VARCHAR(100) DEFAULT '' COMMENT 'Dictionary name',
    dict_type VARCHAR(100) DEFAULT '' COMMENT 'Dictionary type',
    status CHAR(1) DEFAULT '0' COMMENT 'Status: 0=normal, 1=disabled',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (dict_id),
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dictionary type table';

-- Dictionary data (sys_dict_data)
CREATE TABLE IF NOT EXISTS sys_dict_data (
    dict_code BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Dictionary code',
    dict_sort INT DEFAULT 0 COMMENT 'Display order',
    dict_label VARCHAR(100) DEFAULT '' COMMENT 'Dictionary label',
    dict_value VARCHAR(100) DEFAULT '' COMMENT 'Dictionary value',
    dict_type VARCHAR(100) DEFAULT '' COMMENT 'Dictionary type',
    css_class VARCHAR(100) DEFAULT NULL COMMENT 'CSS class',
    list_class VARCHAR(100) DEFAULT NULL COMMENT 'List class',
    is_default CHAR(1) DEFAULT 'N' COMMENT 'Is default: Y=yes, N=no',
    status CHAR(1) DEFAULT '0' COMMENT 'Status: 0=normal, 1=disabled',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (dict_code),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Dictionary data table';

-- System config (sys_config)
CREATE TABLE IF NOT EXISTS sys_config (
    config_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Config ID',
    config_name VARCHAR(100) DEFAULT '' COMMENT 'Config name',
    config_key VARCHAR(100) DEFAULT '' COMMENT 'Config key',
    config_value VARCHAR(500) DEFAULT '' COMMENT 'Config value',
    config_type CHAR(1) DEFAULT 'N' COMMENT 'Config type: Y=system, N=normal',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System config table';

-- ======================
-- Notice Table
-- ======================

-- Notice/Announcement (sys_notice)
CREATE TABLE IF NOT EXISTS sys_notice (
    notice_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Notice ID',
    notice_title VARCHAR(50) NOT NULL COMMENT 'Notice title',
    notice_type CHAR(1) NOT NULL COMMENT 'Notice type: 1=notice, 2=announcement',
    notice_content TEXT DEFAULT NULL COMMENT 'Notice content',
    status CHAR(1) DEFAULT '0' COMMENT 'Status: 0=normal, 1=closed',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notice/Announcement table';

-- ======================
-- Logging Tables
-- ======================

-- Operation log (sys_oper_log)
CREATE TABLE IF NOT EXISTS sys_oper_log (
    oper_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
    title VARCHAR(50) DEFAULT '' COMMENT 'Module title',
    business_type INT DEFAULT 0 COMMENT 'Business type',
    method VARCHAR(100) DEFAULT '' COMMENT 'Method name',
    request_method VARCHAR(10) DEFAULT '' COMMENT 'Request method',
    operator_type INT DEFAULT 0 COMMENT 'Operator type',
    oper_name VARCHAR(50) DEFAULT '' COMMENT 'Operator name',
    dept_name VARCHAR(50) DEFAULT '' COMMENT 'Department name',
    oper_url VARCHAR(255) DEFAULT '' COMMENT 'Request URL',
    oper_ip VARCHAR(50) DEFAULT '' COMMENT 'Host address',
    oper_location VARCHAR(255) DEFAULT '' COMMENT 'Location',
    oper_param VARCHAR(2000) DEFAULT '' COMMENT 'Request parameter',
    json_result VARCHAR(2000) DEFAULT '' COMMENT 'Response result',
    status INT DEFAULT 0 COMMENT 'Status: 0=normal, 1=abnormal',
    error_msg VARCHAR(2000) DEFAULT '' COMMENT 'Error message',
    oper_time DATETIME DEFAULT NULL COMMENT 'Operation time',
    PRIMARY KEY (oper_id),
    KEY idx_oper_time (oper_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Operation log table';

-- Login log (sys_logininfor)
CREATE TABLE IF NOT EXISTS sys_logininfor (
    info_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
    user_name VARCHAR(50) DEFAULT '' COMMENT 'Username',
    ipaddr VARCHAR(50) DEFAULT '' COMMENT 'IP address',
    login_location VARCHAR(255) DEFAULT '' COMMENT 'Login location',
    browser VARCHAR(50) DEFAULT '' COMMENT 'Browser',
    os VARCHAR(50) DEFAULT '' COMMENT 'Operating system',
    status CHAR(1) DEFAULT '0' COMMENT 'Login status: 0=success, 1=failed',
    msg VARCHAR(255) DEFAULT '' COMMENT 'Prompt message',
    login_time DATETIME DEFAULT NULL COMMENT 'Login time',
    PRIMARY KEY (info_id),
    KEY idx_ipaddr (ipaddr),
    KEY idx_user_name (user_name),
    KEY idx_status (status),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Login log table';

-- ======================
-- Quartz Scheduler Tables
-- ======================

-- Scheduled task (sys_job)
CREATE TABLE IF NOT EXISTS sys_job (
    job_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Task ID',
    job_name VARCHAR(50) NOT NULL COMMENT 'Task name',
    job_group VARCHAR(50) DEFAULT 'DEFAULT' COMMENT 'Task group',
    invoke_target VARCHAR(500) NOT NULL COMMENT 'Invocation target',
    cron_expression VARCHAR(255) DEFAULT '' COMMENT 'Cron expression',
    misfire_policy VARCHAR(20) DEFAULT '0' COMMENT 'Misfire policy: 0=default, 1=fire immediately, 2=execute once, 3=do not execute',
    concurrent VARCHAR(20) DEFAULT '1' COMMENT 'Concurrent: 0=allow, 1=prevent',
    status CHAR(1) DEFAULT '0' COMMENT 'Status: 0=normal, 1=paused',
    create_by VARCHAR(64) DEFAULT '' COMMENT 'Creator',
    create_time DATETIME DEFAULT NULL COMMENT 'Create time',
    update_by VARCHAR(64) DEFAULT '' COMMENT 'Updater',
    update_time DATETIME DEFAULT NULL COMMENT 'Update time',
    remark VARCHAR(500) DEFAULT NULL COMMENT 'Remark',
    PRIMARY KEY (job_id, job_group)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Scheduled task table';

-- Task execution log (sys_job_log)
CREATE TABLE IF NOT EXISTS sys_job_log (
    job_log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
    job_name VARCHAR(50) NOT NULL COMMENT 'Task name',
    job_group VARCHAR(50) DEFAULT 'DEFAULT' COMMENT 'Task group',
    invoke_target VARCHAR(500) DEFAULT '' COMMENT 'Invocation target',
    job_message VARCHAR(500) DEFAULT NULL COMMENT 'Log message',
    status CHAR(1) DEFAULT '0' COMMENT 'Status: 0=success, 1=failed',
    exception_info VARCHAR(2000) DEFAULT '' COMMENT 'Exception info',
    start_time DATETIME DEFAULT NULL COMMENT 'Start time',
    end_time DATETIME DEFAULT NULL COMMENT 'End time',
    PRIMARY KEY (job_log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Task execution log table';

-- ======================
-- Sample Data
-- ======================

-- Insert sample department
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, create_by, create_time) VALUES
(100, 0, '0', 'Headquarters', 0, 'Admin', '15888888888', 'admin@xarch.com', '0', 'admin', NOW()),
(101, 100, '0,100', 'Technology Department', 1, 'Tech Lead', '15888888889', 'tech@xarch.com', '0', 'admin', NOW()),
(102, 100, '0,100', 'Sales Department', 2, 'Sales Lead', '15888888890', 'sales@xarch.com', '0', 'admin', NOW());

-- Insert sample post
INSERT INTO sys_post (post_id, post_code, post_name, post_sort, status, create_by, create_time) VALUES
(1, 'CEO', 'Chief Executive Officer', 1, '0', 'admin', NOW()),
(2, 'CTO', 'Chief Technology Officer', 2, '0', 'admin', NOW()),
(3, 'DEV', 'Developer', 3, '0', 'admin', NOW()),
(4, 'TEST', 'Tester', 4, '0', 'admin', NOW()),
(5, 'HR', 'Human Resources', 5, '0', 'admin', NOW());

-- Insert sample user (password: admin123)
INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, password, status, create_by, create_time) VALUES
(1, 100, 'admin', 'Administrator', '1', 'admin@xarch.com', '15888888888', '0', '$2a$10$7JB720yub1V7G2v0pZeV3u1iZ1fYk9Z3v8n6VXqZQZQZQZQZQZQZQ', '0', 'admin', NOW()),
(2, 101, 'user01', 'User One', '1', 'user01@xarch.com', '15888888889', '0', '$2a$10$7JB720yub1V7G2v0pZeV3u1iZ1fYk9Z3v8n6VXqZQZQZQZQZQZQZQ', '0', 'admin', NOW());

-- Insert sample role
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, create_by, create_time, remark) VALUES
(1, 'Super Admin', 'admin', 1, '1', '0', 'admin', NOW(), 'Super administrator'),
(2, 'Common User', 'common', 2, '2', '0', 'admin', NOW(), 'Common role');

-- Insert sample menu
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(1, 'System', 0, 1, 'system', NULL, 'M', '0', '0', '', 'el-icon-system', 'admin', NOW()),
(100, 'User Management', 1, 1, 'user', 'system/user/index', 'C', '0', '0', 'system:user:list', 'el-icon-user', 'admin', NOW()),
(101, 'Role Management', 1, 2, 'role', 'system/role/index', 'C', '0', '0', 'system:role:list', 'el-icon-role', 'admin', NOW()),
(102, 'Menu Management', 1, 3, 'menu', 'system/menu/index', 'C', '0', '0', 'system:menu:list', 'el-icon-menu', 'admin', NOW()),
(103, 'Department Management', 1, 4, 'dept', 'system/dept/index', 'C', '0', '0', 'system:dept:list', 'el-icon-dept', 'admin', NOW()),
(104, 'Post Management', 1, 5, 'post', 'system/post/index', 'C', '0', '0', 'system:post:list', 'el-icon-post', 'admin', NOW()),
(2, 'System Monitoring', 0, 2, 'monitor', NULL, 'M', '0', '0', '', 'el-icon-monitor', 'admin', NOW()),
(200, 'Online Users', 2, 1, 'online', 'monitor/online/index', 'C', '0', '0', 'monitor:online:list', 'el-icon-online', 'admin', NOW()),
(201, 'Operation Log', 2, 2, 'operlog', 'monitor/operlog/index', 'C', '0', '0', 'monitor:operlog:list', 'el-icon-log', 'admin', NOW()),
(202, 'Login Log', 2, 3, 'logininfor', 'monitor/logininfor/index', 'C', '0', '0', 'monitor:logininfor:list', 'el-icon-login', 'admin', NOW()),
(3, 'System Tools', 0, 3, 'tool', NULL, 'M', '0', '0', '', 'el-icon-tool', 'admin', NOW());

-- User-Role relation
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- Role-Menu relation
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1), (1, 100), (1, 101), (1, 102), (1, 103), (1, 104), (1, 2), (1, 200), (1, 201), (1, 202), (1, 3);

-- Role-Department relation
INSERT INTO sys_role_dept (role_id, dept_id) VALUES (1, 100), (1, 101), (1, 102);

-- Insert dictionary type
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(1, 'Gender', 'sys_user_sex', '0', 'admin', NOW(), 'Gender dictionary'),
(2, 'Menu Status', 'sys_show_hide', '0', 'admin', NOW(), 'Menu status dictionary'),
(3, 'System Status', 'sys_normal_disable', '0', 'admin', NOW(), 'System status dictionary');

-- Insert dictionary data
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time) VALUES
(1, 'Male', '0', 'sys_user_sex', '', 'default', 'Y', '0', 'admin', NOW()),
(2, 'Female', '1', 'sys_user_sex', '', 'danger', 'N', '0', 'admin', NOW()),
(3, 'Unknown', '2', 'sys_user_sex', '', 'info', 'N', '0', 'admin', NOW()),
(1, 'Show', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', NOW()),
(2, 'Hide', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', NOW()),
(1, 'Normal', '0', 'sys_normal_disable', '', 'success', 'Y', '0', 'admin', NOW()),
(2, 'Disabled', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', NOW());

-- Insert system config
INSERT INTO sys_config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
(1, 'Main framework page - Default skin', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', NOW(), 'Skin name'),
(2, 'User management - Initial password', 'sys.user.initPassword', 'admin123', 'Y', 'admin', NOW(), 'Initial password'),
(3, 'Main framework page - Sidebar skin', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', NOW(), 'Sidebar skin'),
(4, 'Captcha switch', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', NOW(), 'Captcha enabled'),
(5, 'Username retrieval', 'sys.account.userNameEnabled', 'false', 'Y', 'admin', NOW(), 'Username retrieval enabled');