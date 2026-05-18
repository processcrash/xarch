# ===============================================
# xarch Kubernetes 部署配置文件
# ===============================================
# 本目录包含 xarch 框架所有服务的 Kubernetes 部署清单
#
# 目录结构:
#   base/           - 基础资源配置（所有环境通用）
#   overlays/dev/   - 开发环境配置覆盖
#   overlays/prod/  - 生产环境配置覆盖
#
# 支持的服务:
#   - xarch-example         : 业务应用 (8080)
#   - xarch-mcp-database    : 数据库 MCP Server (9090)
#   - xarch-mcp-knowledge   : 知识库 MCP Server (9091)
#   - xarch-mcp-filesystem  : 文件系统 MCP Server (9092)
#   - xarch-cloud-gateway   : API Gateway (8080)
#   - nacos                 : Nacos 注册中心 (8848)
#   - mysql                 : MySQL 数据库 (3306)
#   - redis                 : Redis 缓存 (6379)
#
# 使用方法:
#   kubectl apply -k base/                    # 部署基础配置
#   kubectl apply -k overlays/dev/            # 部署开发环境
#   kubectl apply -k overlays/prod/           # 部署生产环境
# ===============================================