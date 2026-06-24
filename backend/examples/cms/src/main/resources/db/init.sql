-- xarch CMS example - database schema
-- Target: MySQL 8.x (uses BIGINT epoch-ms timestamps and InnoDB indexes)

CREATE DATABASE IF NOT EXISTS `xarch_cms` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `xarch_cms`;

-- ---------------------------------------------------------------------------
-- Categories form a tree via parent_id
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `cms_category`;
CREATE TABLE `cms_category` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `name`         VARCHAR(128) NOT NULL,
    `slug`         VARCHAR(128) NOT NULL,
    `parent_id`    BIGINT       NOT NULL DEFAULT 0,
    `sort_order`   INT          NOT NULL DEFAULT 0,
    `description`  VARCHAR(512)          DEFAULT NULL,
    `create_time`  BIGINT       NOT NULL DEFAULT 0,
    `update_time`  BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cms_category_slug` (`slug`),
    KEY `idx_cms_category_parent` (`parent_id`),
    KEY `idx_cms_category_sort` (`sort_order`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CMS categories';

-- ---------------------------------------------------------------------------
-- Tags are flat
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `cms_tag`;
CREATE TABLE `cms_tag` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `name`         VARCHAR(64)  NOT NULL,
    `slug`         VARCHAR(64)  NOT NULL,
    `description`  VARCHAR(512)          DEFAULT NULL,
    `create_time`  BIGINT       NOT NULL DEFAULT 0,
    `update_time`  BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cms_tag_slug` (`slug`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CMS tags';

-- ---------------------------------------------------------------------------
-- Articles go through a DRAFT -> PUBLISHED -> ARCHIVED lifecycle
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `cms_article`;
CREATE TABLE `cms_article` (
    `id`             BIGINT         NOT NULL AUTO_INCREMENT,
    `title`          VARCHAR(256)   NOT NULL,
    `content`        MEDIUMTEXT     NOT NULL,
    `summary`        VARCHAR(1024)           DEFAULT NULL,
    `category_id`    BIGINT                  DEFAULT NULL,
    `author_id`      BIGINT         NOT NULL,
    `status`         VARCHAR(16)    NOT NULL DEFAULT 'DRAFT',
    `view_count`     BIGINT         NOT NULL DEFAULT 0,
    `like_count`     BIGINT         NOT NULL DEFAULT 0,
    `create_time`    BIGINT         NOT NULL DEFAULT 0,
    `update_time`    BIGINT         NOT NULL DEFAULT 0,
    `published_time` BIGINT                  DEFAULT NULL,
    `is_deleted`     TINYINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_cms_article_status` (`status`),
    KEY `idx_cms_article_category` (`category_id`),
    KEY `idx_cms_article_author` (`author_id`),
    KEY `idx_cms_article_create` (`create_time`),
    KEY `idx_cms_article_deleted` (`is_deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CMS articles';

-- ---------------------------------------------------------------------------
-- Article <-> Tag join
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `cms_article_tag`;
CREATE TABLE `cms_article_tag` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `tag_id`     BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cms_article_tag` (`article_id`, `tag_id`),
    KEY `idx_cms_article_tag_tag` (`tag_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Article-Tag join';

-- ---------------------------------------------------------------------------
-- Comments support a single level of nesting via parent_id
-- ---------------------------------------------------------------------------
DROP TABLE IF EXISTS `cms_comment`;
CREATE TABLE `cms_comment` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `article_id`  BIGINT        NOT NULL,
    `user_id`     BIGINT        NOT NULL,
    `content`     VARCHAR(2048) NOT NULL,
    `parent_id`   BIGINT        NOT NULL DEFAULT 0,
    `status`      VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    `create_time` BIGINT        NOT NULL DEFAULT 0,
    `is_deleted`  TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_cms_comment_article` (`article_id`),
    KEY `idx_cms_comment_user` (`user_id`),
    KEY `idx_cms_comment_parent` (`parent_id`),
    KEY `idx_cms_comment_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'CMS comments';

-- ---------------------------------------------------------------------------
-- Seed data
-- ---------------------------------------------------------------------------
INSERT INTO `cms_category` (`name`, `slug`, `parent_id`, `sort_order`, `description`)
VALUES
    ('Engineering', 'engineering', 0, 1, 'Engineering articles'),
    ('Product',     'product',     0, 2, 'Product articles');

INSERT INTO `cms_tag` (`name`, `slug`)
VALUES
    ('Java',  'java'),
    ('Vue',   'vue'),
    ('DevOps', 'devops');
