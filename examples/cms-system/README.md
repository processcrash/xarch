# CMS 内容管理系统案例

基于 xarch 框架构建的网站内容管理系统。

## 功能模块

### 内容管理
- [x] 文章管理 - 创建、编辑、发布文章
- [x] 分类管理 - 栏目分类、标签管理
- [x] 页面管理 - 静态页面、自定义页面
- [x] 回收站 - 内容回收与恢复

### 媒体管理
- [x] 图片管理 - 图片上传、裁剪、压缩
- [x] 视频管理 - 视频上传、转码、播放
- [x] 文件管理 - 文档上传、下载管理
- [x] 图库管理 - 图片分组、相册管理

### 模板管理
- [x] 模板列表 - 站点模板、频道模板
- [x] 模板编辑 - 可视化编辑、代码编辑
- [x] 变量管理 - 模板变量、配置变量
- [x] 碎片管理 - 页面碎片、灵活复用

### 网站配置
- [x] 站点设置 - 基本信息、SEO设置
- [x] 频道管理 - 频道配置、权限设置
- [x] 用户权限 - 管理员、编辑、作者
- [x] 操作日志 - 审计跟踪

## 技术架构

```
backend/
├── xarch-example/
│   ├── controller/
│   │   ├── cms/              # CMS 相关控制器
│   │   │   ├── ArticleController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── MediaController.java
│   │   │   └── TemplateController.java
│   │   └── ...
│   └── entity/
│       └── cms/
└── ...

frontend/
├── vue3-admin/
│   └── src/views/cms/
```

## 数据库模型

### 文章表 (cms_article)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| title | varchar | 文章标题 |
| short_title | varchar | 短标题 |
| slug | varchar | URL别名 |
| category_id | bigint | 分类ID |
| author_id | bigint | 作者ID |
| source | varchar | 来源 |
| summary | text | 摘要 |
| content | longtext | 正文内容 |
| thumbnail | varchar | 缩略图 |
| view_count | int | 阅读数 |
| status | int | 状态(草稿/已发布) |
| published_time | datetime | 发布时间 |
| created_time | datetime | 创建时间 |

### 分类表 (cms_category)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar | 分类名称 |
| parent_id | bigint | 父分类ID |
| slug | varchar | URL别名 |
| path | varchar | 路径 |
| level | int | 层级 |
| sort | int | 排序 |

### 媒体表 (cms_media)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| media_type | varchar | 媒体类型 |
| file_name | varchar | 文件名 |
| file_path | varchar | 存储路径 |
| file_url | varchar | 访问URL |
| file_size | bigint | 文件大小 |
| mime_type | varchar | MIME类型 |
| width | int | 宽度(图片/视频) |
| height | int | 高度(图片/视频) |
| duration | int | 时长(视频)秒 |
| uploader_id | bigint | 上传者 |
| created_time | datetime | 上传时间 |

### 模板表 (cms_template)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar | 模板名称 |
| template_type | varchar | 模板类型 |
| template_code | text | 模板代码 |
| variables | text | 变量JSON |
| is_default | int | 是否默认 |
| created_time | datetime | 创建时间 |

## API 示例

### 文章管理

```bash
# 创建文章
POST /api/cms/article
{
  "title": "Spring Boot 3.0 正式发布",
  "shortTitle": "Spring Boot 3.0发布",
  "slug": "spring-boot-3-release",
  "categoryId": 1,
  "source": "官方博客",
  "summary": "Spring Boot 3.0 正式发布，带来多项重大更新",
  "content": "## 主要特性\n\n1. 支持 JDK 17+\n2. 新的自动配置机制...",
  "thumbnail": "/uploads/2024/01/spring-boot.png",
  "status": 1,
  "publishedTime": "2024-01-15 10:00:00"
}

# 分页查询文章
GET /api/cms/article/page?categoryId=1&status=1&pageNum=1&pageSize=20

# 文章上下架
PUT /api/cms/article/1/status
{
  "status": 0,
  "reason": "内容需要修订"
}

# 获取相关文章
GET /api/cms/article/1/related?limit=5
```

### 分类管理

```bash
# 创建分类
POST /api/cms/category
{
  "name": "技术文档",
  "parentId": 0,
  "slug": "tech-docs",
  "sort": 1
}

# 获取分类树
GET /api/cms/category/tree

# 查询分类及其子分类下的文章
GET /api/cms/article/page?categoryId=1&includeChildren=true
```

### 媒体管理

```bash
# 上传图片
POST /api/cms/media/upload
Content-Type: multipart/form-data
file: [图片文件]
sceneCode: article_thumbnail

# 图片裁剪
POST /api/cms/media/1/crop
{
  "x": 0,
  "y": 0,
  "width": 800,
  "height": 600
}

# 生成缩略图
POST /api/cms/media/1/thumbnail
{
  "width": 300,
  "height": 200,
  "mode": "scale"
}
```

### 模板管理

```bash
# 创建模板
POST /api/cms/template
{
  "name": "文章详情页",
  "templateType": "article_detail",
  "templateCode": "<!DOCTYPE html>\n<html>\n<head>...</head>\n<body>{{article.content}}</body>\n</html>",
  "isDefault": 1
}

# 更新模板
PUT /api/cms/template/1
{
  "templateCode": "<!DOCTYPE html>..."
}

# 预览模板
GET /api/cms/template/1/preview?articleId=1
```

## 界面预览

```
┌─────────────────────────────────────────────────────────────────┐
│  CMS内容管理系统                                                   │
├──────────┬──────────────────────────────────────────────────────┤
│ 内容管理  │ ┌──────────────────────────────────────────────────┐ │
│  ├文章   │ │  文章列表              [新建] [批量发布] [导出]    │ │
│  ├分类   │ ├──────────────────────────────────────────────────┤ │
│  └页面   │ │  ☑ 全选    标题        分类      作者    状态    │ │
│          │ │  ☑        Spring...   技术文档   张三    [已发布] │ │
│ 媒体管理  │ │  ☑        Vue3指南    技术文档   李四    [已发布] │ │
│  ├图片   │ │  ☑        Docker...   运维文档   王五    [草稿]   │ │
│  ├视频   │ │  ☑        K8s实战    运维文档   赵六    [已发布] │ │
│  └文件   │ └──────────────────────────────────────────────────┘ │
│          │                                                      │
│ 模板管理  │ ┌──────────────────────────────────────────────────┐ │
│  ├模板   │ │  媒体库                         [上传] [新建文件夹] │ │
│  └变量   │ ├──────────────────────────────────────────────────┤ │
│          │ │  📁 图片                         📁 视频   📁 文档 │ │
│ 网站配置  │ │  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐       │ │
│  ├站点   │ │  │img1│ │img2│ │img3│ │img4│ │img5│ │img6│       │ │
│  ├频道   │ │  └────┘ └────┘ └────┘ └────┘ └────┘ └────┘       │ │
│  └用户   │ └──────────────────────────────────────────────────┘ │
└──────────┴────────────────────────────────────────────────────┘
```

## 扩展 xarch

在 xarch 基础上扩展 CMS 功能：

```java
@RestController
@RequestMapping("/api/cms/article")
public class ArticleController extends BaseController<Article> {

    @Autowired
    private ArticleService articleService;

    // 创建文章
    @PostMapping
    @XarchLog(value = "创建文章", type = "CREATE")
    public ApiResult<Long> create(@RequestBody Article article) {
        Long id = articleService.createArticle(article);
        return ApiResult.ok(id);
    }

    // 发布文章
    @PutMapping("/{id}/publish")
    @XarchLog(value = "发布文章", type = "UPDATE")
    public ApiResult<Void> publish(@PathVariable Long id) {
        articleService.publishArticle(id);
        return ApiResult.ok();
    }

    // 文章上下架
    @PutMapping("/{id}/status")
    @XarchLog(value = "更新文章状态", type = "UPDATE")
    public ApiResult<Void> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        articleService.updateStatus(id, request.getStatus());
        return ApiResult.ok();
    }

    // 获取相关文章
    @GetMapping("/{id}/related")
    public ApiResult<List<ArticleVO>> getRelatedArticles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        return ApiResult.ok(articleService.getRelatedArticles(id, limit));
    }
}
```