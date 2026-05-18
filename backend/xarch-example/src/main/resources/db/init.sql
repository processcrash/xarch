-- Create database
CREATE DATABASE IF NOT EXISTS xarch DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xarch;

-- User table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    password VARCHAR(100) NOT NULL COMMENT 'Password',
    nickname VARCHAR(50) COMMENT 'Nickname',
    email VARCHAR(100) COMMENT 'Email',
    mobile VARCHAR(20) COMMENT 'Mobile',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    dept_id BIGINT COMMENT 'Department ID',
    user_type TINYINT DEFAULT 2 COMMENT 'User type: 1=admin, 2=normal',
    role_ids VARCHAR(200) COMMENT 'Role IDs',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT DEFAULT 0 COMMENT 'Delete flag: 0=not deleted, 1=deleted',
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System user table';

-- Insert sample users
INSERT INTO sys_user (username, password, nickname, email, mobile, status, user_type, role_ids) VALUES
('admin', 'admin123', 'Administrator', 'admin@xarch.com', '13800138000', 1, 1, '1'),
('user', 'user123', 'Normal User', 'user@xarch.com', '13800138001', 1, 2, '2');

-- Department table
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT 'Parent department ID',
    dept_name VARCHAR(50) NOT NULL COMMENT 'Department name',
    dept_code VARCHAR(50) COMMENT 'Department code',
    sort_order INT DEFAULT 0 COMMENT 'Sort order',
    leader VARCHAR(50) COMMENT 'Leader',
    phone VARCHAR(20) COMMENT 'Phone',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Department table';

INSERT INTO sys_dept (id, parent_id, dept_name, dept_code, sort_order) VALUES
(1, 0, 'Headquarters', 'HQ', 1),
(2, 1, 'Technology Department', 'TECH', 1),
(3, 1, 'Operations Department', 'OPS', 2),
(4, 2, 'Development Team', 'DEV', 1),
(5, 2, 'QA Team', 'QA', 2);

-- Role table
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT 'Role name',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Role code',
    role_type TINYINT DEFAULT 1 COMMENT 'Role type: 1=system, 2=business',
    description VARCHAR(200) COMMENT 'Description',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT DEFAULT 0,
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role table';

INSERT INTO sys_role (id, role_name, role_code, role_type, description) VALUES
(1, 'Super Admin', 'SUPER_ADMIN', 1, 'Super administrator with all permissions'),
(2, 'User', 'USER', 2, 'Normal user role');

-- Menu table
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT 'Parent menu ID',
    menu_name VARCHAR(50) NOT NULL COMMENT 'Menu name',
    menu_code VARCHAR(50) COMMENT 'Menu code',
    menu_type TINYINT DEFAULT 1 COMMENT 'Menu type: 1=menu, 2=button',
    path VARCHAR(200) COMMENT 'Route path',
    icon VARCHAR(100) COMMENT 'Icon',
    sort_order INT DEFAULT 0 COMMENT 'Sort order',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_code (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Menu table';

INSERT INTO sys_menu (id, parent_id, menu_name, menu_code, menu_type, path, icon, sort_order) VALUES
(1, 0, 'System Management', 'system', 1, '/system', 'setting', 1),
(2, 1, 'User Management', 'sys.user', 1, '/users', 'user', 1),
(3, 1, 'Role Management', 'sys.role', 1, '/roles', 'role', 2),
(4, 1, 'Menu Management', 'sys.menu', 1, '/menus', 'menu', 3),
(5, 1, 'Department Management', 'sys.dept', 1, '/depts', 'dept', 4),
(6, 1, 'Dictionary', 'sys.dict', 1, '/dicts', 'dict', 5),
(7, 1, 'System Config', 'sys.config', 1, '/configs', 'config', 6),
(8, 0, 'Logs', 'logs', 1, '/logs', 'logs', 2),
(9, 8, 'Login Logs', 'sys.loginlog', 1, '/loginlogs', 'login', 1),
(10, 8, 'Operation Logs', 'sys.oplog', 1, '/oplogs', 'op', 2);

-- Role-Menu relation table
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role-menu relation table';

INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10);

-- Login log table
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) COMMENT 'Username',
    ip VARCHAR(50) COMMENT 'IP address',
    location VARCHAR(200) COMMENT 'Location',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    login_type TINYINT DEFAULT 1 COMMENT 'Login type: 1=login, 2=logout',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=success, 0=failed',
    message VARCHAR(500) COMMENT 'Message',
    INDEX idx_username (username),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Login log table';

-- Operation log table
CREATE TABLE IF NOT EXISTS sys_op_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) COMMENT 'Username',
    operation VARCHAR(100) COMMENT 'Operation description',
    type VARCHAR(50) COMMENT 'Operation type',
    method VARCHAR(200) COMMENT 'Method name',
    ip VARCHAR(50) COMMENT 'IP address',
    location VARCHAR(200) COMMENT 'Location',
    params TEXT COMMENT 'Request parameters',
    result TEXT COMMENT 'Result',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=success, 0=failed',
    cost_time BIGINT COMMENT 'Cost time in milliseconds',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Operation log table';

-- Dictionary table
CREATE TABLE IF NOT EXISTS sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL COMMENT 'Dictionary name',
    dict_code VARCHAR(100) NOT NULL UNIQUE COMMENT 'Dictionary code',
    description VARCHAR(200) COMMENT 'Description',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT DEFAULT 0,
    INDEX idx_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dictionary table';

INSERT INTO sys_dict (dict_name, dict_code, description) VALUES
('User Status', 'user_status', 'User status dictionary'),
('Gender', 'gender', 'Gender dictionary'),
('Yes/No', 'yes_no', 'Yes/No dictionary');

-- Dictionary data table
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_id BIGINT NOT NULL COMMENT 'Dictionary ID',
    dict_label VARCHAR(100) NOT NULL COMMENT 'Label',
    dict_value VARCHAR(100) NOT NULL COMMENT 'Value',
    sort_order INT DEFAULT 0 COMMENT 'Sort order',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT DEFAULT 0,
    INDEX idx_dict_id (dict_id),
    INDEX idx_dict_value (dict_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Dictionary data table';

INSERT INTO sys_dict_data (dict_id, dict_label, dict_value, sort_order) VALUES
(1, 'Active', '1', 1),
(1, 'Disabled', '0', 2),
(2, 'Male', '1', 1),
(2, 'Female', '2', 2),
(3, 'Yes', '1', 1),
(3, 'No', '0', 2);

-- System config table
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT 'Config key',
    config_value VARCHAR(500) NOT NULL COMMENT 'Config value',
    config_type VARCHAR(50) DEFAULT 'string' COMMENT 'Config type',
    description VARCHAR(200) COMMENT 'Description',
    status TINYINT DEFAULT 1 COMMENT 'Status: 1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag TINYINT DEFAULT 0,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System config table';

INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES
('sys.index.title', 'xarch Backend Framework', 'string', 'System title'),
('sys.index.copyright', 'Copyright © 2024 xarch', 'string', 'Copyright');