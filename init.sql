-- Initialize xarch database

CREATE DATABASE IF NOT EXISTS xarch DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xarch;

-- User table
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    password VARCHAR(100) NOT NULL COMMENT 'Password',
    email VARCHAR(100) COMMENT 'Email',
    mobile VARCHAR(20) COMMENT 'Mobile',
    status TINYINT DEFAULT 1 COMMENT 'Status: 0=disabled, 1=normal',
    create_time BIGINT NOT NULL COMMENT 'Create timestamp',
    update_time BIGINT NOT NULL COMMENT 'Update timestamp',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System user table';

-- Insert sample data
INSERT INTO sys_user (username, password, email, mobile, status, create_time, update_time) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@xarch.com', '13800138000', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user1@xarch.com', '13800138001', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);