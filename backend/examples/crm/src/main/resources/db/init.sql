-- xarch-crm schema
-- Run once: mysql -u root -p < src/main/resources/db/init.sql

CREATE DATABASE IF NOT EXISTS `xarch_crm`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `xarch_crm`;

-- ---------------------------------------------------------------------------
-- Customer
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_customer`;
CREATE TABLE `crm_customer` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `name`            VARCHAR(200) NOT NULL COMMENT 'Customer name',
    `type`            VARCHAR(20)  NOT NULL DEFAULT 'LEAD' COMMENT 'LEAD / PROSPECT / CUSTOMER / LOST',
    `industry`        VARCHAR(100)          DEFAULT NULL COMMENT 'Industry',
    `scale`           VARCHAR(20)           DEFAULT NULL COMMENT 'SMALL / MEDIUM / LARGE / ENTERPRISE',
    `contact_name`    VARCHAR(100)          DEFAULT NULL COMMENT 'Primary contact name',
    `contact_phone`   VARCHAR(50)           DEFAULT NULL COMMENT 'Primary contact phone',
    `contact_email`   VARCHAR(200)          DEFAULT NULL COMMENT 'Primary contact email',
    `address`         VARCHAR(500)          DEFAULT NULL COMMENT 'Address',
    `website`         VARCHAR(200)          DEFAULT NULL COMMENT 'Website',
    `owner_id`        BIGINT                DEFAULT NULL COMMENT 'Sales owner id',
    `source`          VARCHAR(50)           DEFAULT NULL COMMENT 'Acquisition source',
    `level`           VARCHAR(10)           DEFAULT NULL COMMENT 'A / B / C / D - higher = hotter',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / INACTIVE',
    `tags`            VARCHAR(1000)         DEFAULT NULL COMMENT 'Comma-separated tags',
    `last_contact_time` BIGINT              DEFAULT NULL COMMENT 'Last contact timestamp (epoch millis)',
    `create_time`     BIGINT                DEFAULT NULL COMMENT 'Created at (epoch millis)',
    `update_time`     BIGINT                DEFAULT NULL COMMENT 'Updated at (epoch millis)',
    `is_deleted`      INT          NOT NULL DEFAULT 0 COMMENT 'Soft delete flag',
    PRIMARY KEY (`id`),
    KEY `idx_customer_name` (`name`),
    KEY `idx_customer_type` (`type`),
    KEY `idx_customer_level` (`level`),
    KEY `idx_customer_owner` (`owner_id`),
    KEY `idx_customer_deleted` (`is_deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CRM customer';

-- ---------------------------------------------------------------------------
-- Contact
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_contact`;
CREATE TABLE `crm_contact` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `customer_id` BIGINT       NOT NULL COMMENT 'FK -> crm_customer.id',
    `name`        VARCHAR(100) NOT NULL COMMENT 'Contact name',
    `position`    VARCHAR(100)          DEFAULT NULL COMMENT 'Job title',
    `phone`       VARCHAR(50)           DEFAULT NULL COMMENT 'Phone',
    `email`       VARCHAR(200)          DEFAULT NULL COMMENT 'Email',
    `is_primary`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Primary contact flag',
    `create_time` BIGINT                DEFAULT NULL COMMENT 'Created at (epoch millis)',
    `update_time` BIGINT                DEFAULT NULL COMMENT 'Updated at (epoch millis)',
    `is_deleted`  INT          NOT NULL DEFAULT 0 COMMENT 'Soft delete flag',
    PRIMARY KEY (`id`),
    KEY `idx_contact_customer` (`customer_id`),
    KEY `idx_contact_primary` (`customer_id`, `is_primary`),
    KEY `idx_contact_deleted` (`is_deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CRM contact';

-- ---------------------------------------------------------------------------
-- Opportunity
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_opportunity`;
CREATE TABLE `crm_opportunity` (
    `id`                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `customer_id`         BIGINT         NOT NULL COMMENT 'FK -> crm_customer.id',
    `name`                VARCHAR(200)   NOT NULL COMMENT 'Opportunity name',
    `amount`              DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT 'Amount',
    `currency`            VARCHAR(10)    NOT NULL DEFAULT 'CNY' COMMENT 'Currency',
    `stage`               VARCHAR(30)    NOT NULL COMMENT 'Funnel stage',
    `probability`         INT            NOT NULL DEFAULT 10 COMMENT 'Win probability 0-100',
    `expected_close_date` BIGINT                  DEFAULT NULL COMMENT 'Expected close (epoch millis)',
    `owner_id`            BIGINT                  DEFAULT NULL COMMENT 'Sales owner id',
    `description`         VARCHAR(2000)           DEFAULT NULL COMMENT 'Description',
    `status`              VARCHAR(20)    NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN / WON / LOST',
    `create_time`         BIGINT                  DEFAULT NULL COMMENT 'Created at (epoch millis)',
    `update_time`         BIGINT                  DEFAULT NULL COMMENT 'Updated at (epoch millis)',
    `is_deleted`          INT            NOT NULL DEFAULT 0 COMMENT 'Soft delete flag',
    PRIMARY KEY (`id`),
    KEY `idx_opp_customer` (`customer_id`),
    KEY `idx_opp_stage` (`stage`),
    KEY `idx_opp_owner` (`owner_id`),
    KEY `idx_opp_status` (`status`),
    KEY `idx_opp_deleted` (`is_deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CRM opportunity (sales pipeline)';

-- ---------------------------------------------------------------------------
-- Follow-up
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_follow_up`;
CREATE TABLE `crm_follow_up` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `customer_id`        BIGINT       NOT NULL COMMENT 'FK -> crm_customer.id',
    `contact_id`         BIGINT                DEFAULT NULL COMMENT 'FK -> crm_contact.id',
    `opportunity_id`     BIGINT                DEFAULT NULL COMMENT 'FK -> crm_opportunity.id',
    `type`               VARCHAR(20)  NOT NULL COMMENT 'PHONE / EMAIL / MEETING / VISIT / OTHER',
    `content`            VARCHAR(2000)         DEFAULT NULL COMMENT 'Content',
    `result`             VARCHAR(1000)         DEFAULT NULL COMMENT 'Result',
    `next_follow_up_date` BIGINT               DEFAULT NULL COMMENT 'Next follow-up (epoch millis)',
    `attachments`        VARCHAR(1000)         DEFAULT NULL COMMENT 'Comma-separated attachment ids',
    `user_id`            BIGINT                DEFAULT NULL COMMENT 'Rep id',
    `create_time`        BIGINT                DEFAULT NULL COMMENT 'Created at (epoch millis)',
    `is_deleted`         INT          NOT NULL DEFAULT 0 COMMENT 'Soft delete flag',
    PRIMARY KEY (`id`),
    KEY `idx_follow_customer` (`customer_id`),
    KEY `idx_follow_opp` (`opportunity_id`),
    KEY `idx_follow_next` (`next_follow_up_date`),
    KEY `idx_follow_deleted` (`is_deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CRM follow-up log';

-- ---------------------------------------------------------------------------
-- Contract
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_contract`;
CREATE TABLE `crm_contract` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `customer_id`   BIGINT         NOT NULL COMMENT 'FK -> crm_customer.id',
    `opportunity_id` BIGINT                 DEFAULT NULL COMMENT 'FK -> crm_opportunity.id',
    `contract_no`   VARCHAR(100)   NOT NULL COMMENT 'Contract number (unique)',
    `amount`        DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT 'Amount',
    `start_date`    BIGINT                  DEFAULT NULL COMMENT 'Start (epoch millis)',
    `end_date`      BIGINT                  DEFAULT NULL COMMENT 'End (epoch millis)',
    `payment_terms` VARCHAR(500)            DEFAULT NULL COMMENT 'Payment terms',
    `status`        VARCHAR(20)    NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / ACTIVE / EXPIRED / TERMINATED',
    `signed_date`   BIGINT                  DEFAULT NULL COMMENT 'Signed at (epoch millis)',
    `create_time`   BIGINT                  DEFAULT NULL COMMENT 'Created at (epoch millis)',
    `update_time`   BIGINT                  DEFAULT NULL COMMENT 'Updated at (epoch millis)',
    `is_deleted`    INT            NOT NULL DEFAULT 0 COMMENT 'Soft delete flag',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_contract_no` (`contract_no`),
    KEY `idx_contract_customer` (`customer_id`),
    KEY `idx_contract_status` (`status`),
    KEY `idx_contract_deleted` (`is_deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CRM contract';