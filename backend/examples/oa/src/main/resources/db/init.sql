-- xarch OA example - database schema
-- Target: MySQL 8.x (BIGINT epoch-ms timestamps, InnoDB indexes)

CREATE DATABASE IF NOT EXISTS `xarch_oa` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xarch_oa`;

-- ---------------------------------------------------------------------------
-- Leave requests
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `oa_leave_request`;
CREATE TABLE `oa_leave_request` (
    `id`                   BIGINT         NOT NULL AUTO_INCREMENT,
    `user_id`              BIGINT         NOT NULL,
    `type`                 VARCHAR(32)    NOT NULL,
    `start_date`           BIGINT         NOT NULL,
    `end_date`             BIGINT         NOT NULL,
    `days`                 DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    `reason`               VARCHAR(1024)           DEFAULT NULL,
    `status`               VARCHAR(16)    NOT NULL DEFAULT 'DRAFT',
    `current_approver_id`  BIGINT                  DEFAULT NULL,
    `attachments`          TEXT                    DEFAULT NULL,
    `create_time`          BIGINT         NOT NULL DEFAULT 0,
    `update_time`          BIGINT         NOT NULL DEFAULT 0,
    `is_deleted`           TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_leave_user` (`user_id`),
    KEY `idx_leave_status` (`status`),
    KEY `idx_leave_approver` (`current_approver_id`),
    KEY `idx_leave_deleted` (`is_deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Leave requests';

-- ---------------------------------------------------------------------------
-- Expense reports
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `oa_expense_report`;
CREATE TABLE `oa_expense_report` (
    `id`                  BIGINT         NOT NULL AUTO_INCREMENT,
    `user_id`             BIGINT         NOT NULL,
    `category`            VARCHAR(32)    NOT NULL,
    `amount`              DECIMAL(12, 2) NOT NULL DEFAULT 0,
    `currency`            VARCHAR(8)     NOT NULL DEFAULT 'CNY',
    `description`         VARCHAR(1024)           DEFAULT NULL,
    `items`               TEXT                    DEFAULT NULL,
    `status`              VARCHAR(16)    NOT NULL DEFAULT 'DRAFT',
    `approver_id`         BIGINT                  DEFAULT NULL,
    `reimbursement_date`  BIGINT                  DEFAULT NULL,
    `create_time`         BIGINT         NOT NULL DEFAULT 0,
    `update_time`         BIGINT         NOT NULL DEFAULT 0,
    `is_deleted`          TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_expense_user` (`user_id`),
    KEY `idx_expense_status` (`status`),
    KEY `idx_expense_approver` (`approver_id`),
    KEY `idx_expense_deleted` (`is_deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Expense reports';

-- ---------------------------------------------------------------------------
-- Approval records (audit log)
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `oa_approval_record`;
CREATE TABLE `oa_approval_record` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT,
    `business_type`  VARCHAR(16)   NOT NULL,
    `business_id`    BIGINT        NOT NULL,
    `approver_id`    BIGINT        NOT NULL,
    `approver_name`  VARCHAR(128)           DEFAULT NULL,
    `action`         VARCHAR(16)   NOT NULL,
    `comment`        VARCHAR(1024)          DEFAULT NULL,
    `create_time`    BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_record_business` (`business_type`, `business_id`),
    KEY `idx_record_approver` (`approver_id`),
    KEY `idx_record_create` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Approval audit records';

-- ---------------------------------------------------------------------------
-- Workflow definitions
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `oa_workflow`;
CREATE TABLE `oa_workflow` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `name`           VARCHAR(128) NOT NULL,
    `business_type`  VARCHAR(16)  NOT NULL,
    `definition`     TEXT         NOT NULL,
    `current_node`   INT                   DEFAULT NULL,
    `status`         VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    `created_at`     BIGINT       NOT NULL DEFAULT 0,
    `updated_at`     BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_type` (`business_type`),
    KEY `idx_workflow_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Workflow definitions';

-- ---------------------------------------------------------------------------
-- Seed workflows
-- ---------------------------------------------------------------------------
INSERT INTO `oa_workflow` (`name`, `business_type`, `definition`, `current_node`, `status`)
VALUES
    ('Standard leave', 'LEAVE', JSON_OBJECT(
        'startNode', 0,
        'nodes', JSON_ARRAY(
            JSON_OBJECT('id', 0, 'name', 'manager', 'role', 'MANAGER', 'approvers', JSON_ARRAY(101)),
            JSON_OBJECT('id', 1, 'name', 'hr',      'role', 'HR',      'approvers', JSON_ARRAY(201))
        ),
        'edges', JSON_ARRAY(
            JSON_OBJECT('from', 0, 'to', 1,  'on', 'APPROVE'),
            JSON_OBJECT('from', 0, 'to', -1, 'on', 'REJECT'),
            JSON_OBJECT('from', 1, 'to', -2, 'on', 'APPROVE'),
            JSON_OBJECT('from', 1, 'to', -1, 'on', 'REJECT')
        )
    ), 0, 'ACTIVE'),
    ('Standard expense', 'EXPENSE', JSON_OBJECT(
        'startNode', 0,
        'nodes', JSON_ARRAY(
            JSON_OBJECT('id', 0, 'name', 'manager', 'role', 'MANAGER', 'approvers', JSON_ARRAY(101)),
            JSON_OBJECT('id', 1, 'name', 'finance', 'role', 'FINANCE', 'approvers', JSON_ARRAY(301))
        ),
        'edges', JSON_ARRAY(
            JSON_OBJECT('from', 0, 'to', 1,  'on', 'APPROVE'),
            JSON_OBJECT('from', 0, 'to', -1, 'on', 'REJECT'),
            JSON_OBJECT('from', 1, 'to', -2, 'on', 'APPROVE'),
            JSON_OBJECT('from', 1, 'to', -1, 'on', 'REJECT')
        )
    ), 0, 'ACTIVE');
