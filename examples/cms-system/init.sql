-- CMS 内容管理系统数据库初始化脚本
-- 基于 xarch 框架的 sys_* 表结构扩展

-- 文章表
CREATE TABLE IF NOT EXISTS cms_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '文章标题',
    short_title VARCHAR(100) COMMENT '短标题',
    slug VARCHAR(100) COMMENT 'URL别名',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    author_id BIGINT COMMENT '作者ID',
    source VARCHAR(50) COMMENT '来源',
    summary TEXT COMMENT '摘要',
    content LONGTEXT COMMENT '正文内容',
    thumbnail VARCHAR(255) COMMENT '缩略图',
    view_count INT DEFAULT 0 COMMENT '阅读数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    status INT DEFAULT 0 COMMENT '0-草稿/1-已发布/2-已下线',
    published_time DATETIME COMMENT '发布时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_category (category_id),
    INDEX idx_author (author_id),
    INDEX idx_status (status),
    INDEX idx_published (published_time),
    FULLTEXT idx_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 分类表
CREATE TABLE IF NOT EXISTS cms_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    slug VARCHAR(100) COMMENT 'URL别名',
    path VARCHAR(255) COMMENT '路径',
    level INT DEFAULT 1 COMMENT '层级',
    sort INT DEFAULT 0 COMMENT '排序',
    icon VARCHAR(255) COMMENT '图标',
    description VARCHAR(255) COMMENT '分类描述',
    status INT DEFAULT 1 COMMENT '1-正常/0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_parent (parent_id),
    INDEX idx_path (path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 标签表
CREATE TABLE IF NOT EXISTS cms_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '标签名',
    slug VARCHAR(50) COMMENT 'URL别名',
    color VARCHAR(20) COMMENT '标签颜色',
    article_count INT DEFAULT 0 COMMENT '文章数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    UNIQUE INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 文章标签关联表
CREATE TABLE IF NOT EXISTS cms_article_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL COMMENT '文章ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_article (article_id),
    INDEX idx_tag (tag_id),
    UNIQUE INDEX idx_article_tag (article_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- 媒体表
CREATE TABLE IF NOT EXISTS cms_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    media_type VARCHAR(20) NOT NULL COMMENT 'image/video/audio/document',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(255) NOT NULL COMMENT '存储路径',
    file_url VARCHAR(500) COMMENT '访问URL',
    file_size BIGINT COMMENT '文件大小(字节)',
    mime_type VARCHAR(100) COMMENT 'MIME类型',
    width INT COMMENT '宽度',
    height INT COMMENT '高度',
    duration INT COMMENT '时长(秒)',
    uploader_id BIGINT COMMENT '上传者ID',
    group_id BIGINT COMMENT '分组ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_type (media_type),
    INDEX idx_uploader (uploader_id),
    INDEX idx_group (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体表';

-- 媒体分组表
CREATE TABLE IF NOT EXISTS cms_media_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '分组名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分组ID',
    type VARCHAR(20) COMMENT '分组类型',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体分组表';

-- 模板表
CREATE TABLE IF NOT EXISTS cms_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(30) NOT NULL COMMENT '模板类型',
    template_code TEXT COMMENT '模板代码',
    template_vars TEXT COMMENT '变量定义JSON',
    is_default INT DEFAULT 0 COMMENT '是否默认',
    status INT DEFAULT 1 COMMENT '1-启用/0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_type (template_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板表';

-- 页面表
CREATE TABLE IF NOT EXISTS cms_page (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '页面标题',
    slug VARCHAR(100) COMMENT 'URL别名',
    page_type VARCHAR(30) COMMENT '页面类型',
    template_id BIGINT COMMENT '使用的模板ID',
    content TEXT COMMENT '页面内容',
    seo_title VARCHAR(255) COMMENT 'SEO标题',
    seo_keywords VARCHAR(255) COMMENT 'SEO关键词',
    seo_description VARCHAR(500) COMMENT 'SEO描述',
    status INT DEFAULT 1 COMMENT '1-发布/0-草稿',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INT DEFAULT 0,
    INDEX idx_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面表';

-- 插入测试数据
INSERT INTO cms_category (name, parent_id, slug, path, level, sort) VALUES
('技术文档', 0, 'tech-docs', '/tech-docs', 1, 1),
('运维文档', 0, 'ops-docs', '/ops-docs', 1, 2),
('产品设计', 0, 'product', '/product', 1, 3),
('新闻资讯', 0, 'news', '/news', 1, 4),
('Spring Boot', 1, 'spring-boot', '/tech-docs/spring-boot', 2, 1),
('Kubernetes', 2, 'kubernetes', '/ops-docs/kubernetes', 2, 1);

INSERT INTO cms_article (title, short_title, slug, category_id, source, summary, content, status, published_time) VALUES
('Spring Boot 3.0 正式发布', 'SB3发布', 'spring-boot-3-release', 5, '官方博客', 'Spring Boot 3.0 正式发布，带来多项重大更新', '## 主要特性\n\n1. 支持 JDK 17+\n2. GraalVM 原生支持\n3. 新的自动配置机制', 1, '2024-01-15 10:00:00'),
('Kubernetes 1.28 发布', 'K8s 1.28', 'kubernetes-1-28', 6, '社区', 'Kubernetes 1.28 正式发布', '## 新特性\n\n1. 非终止 Pod 驱逐\n2. 替代 kubectl delete', 1, '2024-01-10 09:00:00');

INSERT INTO cms_tag (name, slug, color) VALUES
('Spring Boot', 'spring-boot', '#6db33f'),
('Kubernetes', 'kubernetes', '#326ce5'),
('Java', 'java', '#007396'),
('DevOps', 'devops', '#ff6b6b');

INSERT INTO cms_media (media_type, file_name, file_path, file_url, file_size, mime_type, width, height) VALUES
('image', 'spring-boot-logo.png', '/uploads/2024/01/', 'https://example.com/spring-boot-logo.png', 51200, 'image/png', 200, 100),
('image', 'kubernetes-logo.png', '/uploads/2024/01/', 'https://example.com/kubernetes-logo.png', 48128, 'image/png', 200, 117);

INSERT INTO cms_template (name, template_type, is_default) VALUES
('文章详情页', 'article_detail', 1),
('文章列表页', 'article_list', 1),
('首页', 'index', 1),
('单页面', 'page', 0);