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
项目最重要要说清楚 xarch 是 AI 时代企业级后台管理项目规范！
---
- 在此基础上，增加 spring cloud 的支持，业务可快速 spring cloud 化
- 增加 Nacos 3.2 的支持，Nacos 不仅管理微服务体系，也要实现管理 MCP 服务
- 增加数据库对接 MCP Server 服务，支持市面上绝大多数的数据库，包含但不限于Mysql、postgresql、Mongodb、Oracle、DB2、Microsoft SQL Server
- 利用在 brainstorming 这个 Skill 调研网络上、软件市场上、 GitHub 上常用的 MCP Server 实现，增加至少三个最常用的 MCP Server 实现，注意实现要支持注册成 Nacos MCP 服务，可在 Spring Cloud 体系中调用
- 在 Spring Cloud 体系中增加企业级知识库的功能模块实现，将这个功能模块做成 MCP Server 实现，并实现注册成 Nacos MCP 服务，可在 Spring Cloud 体系中调用
- 直到这些所有的功能都实现并测试好为止！
---