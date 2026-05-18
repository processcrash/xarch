创建一个 Java 语言的后台开发框架，名为 xarch
- 技术栈使用 jdk 25 和 spring boot 4.0 最新版，前端使用 vue 3.0 最新版
- Java 部分可参考的代码是 D:\workspace\github.com\feiyuchuixue\sz-admin
- 前端部分可参考的代码是 D:\workspace\github.com\feiyuchuixue\sz-boot-parent
- 我之前写的代码也可以参考 D:\workspace\github.com\processcrash\crash-architecture
- 分包明确，遵循 spring boot starter 的命名方式，即 xarch-xxx-spring-boot-starter，并且遵循 spring boot starter 的使用方式引入即使用
- 包功能包含 db（支持 mysql 和 postgresql）、web、cache 等
- 做到可上线交付！
---
- 更新 README.md 文件的内容，写好推荐的内容及每个细节！
- 做好单元测试，所有功能都要实现单元测试
- 参考代码的所有功能都实现了吗？确保所有功能都实现，直到所有功能都实现为止！
---
参考这个仓库的代码，D:\workspace\gitee.com\y_project\RuoYi-Vue，把这个仓库代码的功能全部实现！
---
项目最重要要说清楚 xarch 是：
- AI 时代企业级后台管理项目规范！
- 企业级最佳实践的标准。
---
- 在此基础上，增加 spring cloud 的支持，业务可快速 spring cloud 化
- 增加 Nacos 3.2 的支持，Nacos 不仅管理微服务体系，也要实现管理 MCP 服务
- 增加数据库对接 MCP Server 服务，支持市面上绝大多数的数据库，包含但不限于Mysql、postgresql、Mongodb、Oracle、DB2、Microsoft SQL Server
- 利用在 brainstorming 这个 Skill 调研网络上、软件市场上、 GitHub 上常用的 MCP Server 实现，增加至少三个最常用的 MCP Server 实现，注意实现要支持注册成 Nacos MCP 服务，可在 Spring Cloud 体系中调用
- 在 Spring Cloud 体系中增加企业级知识库的功能模块实现，将这个功能模块做成 MCP Server 实现，并实现注册成 Nacos MCP 服务，可在 Spring Cloud 体系中调用
- 直到这些所有的功能都实现并测试好为止！
---
所有的服务都支持 K8s 部署，将部署文件写好，并且写好注释
---
根据我之前提出的需求，重新整理 REQUIREMENTS.md 文件，整理完后，根据 REQUIREMENTS.md 文件和 TODO.md 文件的内容整理需要做事，依次做完
---
- 增加企业级文件管理中心平台，可对接本地存储、Minio、阿里云 OSS 等文件存储服务，有前端管理页面，可对文件进行下载、上传、删除、预览等操作
---
- spring cloud 中增加 spring boot admin 服务，使用 spring-boot-admin 这个工具实现对所有 spring cloud 中的服务监控的能力
- 增加 alloy + loki + grafana 日志收集的 spring cloud 服务，支持收集 docker 运行的容器、docker compose 运行、k8s 运行的容器运行日志的能力
---
增加实现企业级 Linux 服务器管理 AI Agent 平台，至少包含如下功能
- 在线管理所有的 Linux 服务器
- AI Agent 不直接运行在服务器上，但可远程执行命令，管理服务器
- 所有命令都记录成历史
- 实现完后使用 brainstorming skill 调研市面上的 linux 服务器管理 AI Agent 平台，借鉴并实现完整的平台功能
---