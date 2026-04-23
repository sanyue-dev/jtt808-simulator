---
name: lookup-doc
description: 查阅外部库/框架文档，替代 context7。自动判断用 zread（GitHub 仓库）还是 web-reader（任意 URL）。
user-invocable: true
---

# 查阅文档

用 `$ARGUMENTS` 中指定的关键词查阅外部文档。

## 流程

### 1. 判断来源

根据查询关键词判断目标库：

| 关键词前缀 | GitHub 仓库 | 文档入口路径 |
|---|---|---|
| Spring Boot | spring-projects/spring-boot | README.md → docs/ |
| Spring Framework | spring-projects/spring-framework | README.md |
| MyBatis | mybatis/mybatis-3 | src/site/ |
| Netty | netty/netty | README.md |
| JTT808 Protocol | yezh246/jtt808-protocol | README.md |
| FreeMarker | apache/freemarker | README.md |
| Leaflet | Leaflet/Leaflet | README.md |

如果关键词不匹配上表，先用 `web-search-prime` 搜索相关文档 URL。

### 2. 执行查询

- **GitHub 仓库可识别** → 用 `zread` 的 `search_doc` 搜索仓库文档和 issues，再用 `read_file` 读取具体文件。
- **非 GitHub 或需搜索** → 用 `mcp__web-search-prime__web_search_prime` 搜索，再用 `mcp__web-reader__webReader` 或 `mcp__web_reader__webReader` 抓取页面内容。

### 3. 输出

用中文总结关键要点，附带原始文档链接。
