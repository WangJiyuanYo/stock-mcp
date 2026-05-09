# Stock MCP Server

基于 Spring Boot 3.4 + Spring AI 1.0 的股票持仓管理 MCP 服务，支持新浪财经实时行情。

## 技术栈

- Java 17, Spring Boot 3.4.4
- Spring AI 1.0.0（MCP Server WebMVC SSE 传输）
- MyBatis-Plus 3.5.9 + SQLite
- Maven

## 快速启动

```bash
# 1. 创建数据库目录
mkdir -p /root/stock_dir/db

# 2. 启动服务
java -jar stock-mcp-0.0.1-SNAPSHOT.jar
```

服务默认监听 `18080` 端口，可通过 `SERVER_PORT` 环境变量覆盖。

## Hermes 接入

SSE 端点：

```
http://127.0.0.1:18080/sse
```

```json
{
  "mcpServers": {
    "stock-mcp": {
      "type": "sse",
      "url": "http://127.0.0.1:18080/sse"
    }
  }
}
```

## MCP 工具

| 工具名 | 参数 | 描述 |
|--------|------|------|
| `addOrUpdateStock` | `{stockType, stockCode, holdingQuantity, holdingPrice}` | 添加或更新持仓 |
| `batchSaveStocks` | `{stocks: [...]}` | 批量保存股票 |
| `listStocks` | `{}` | 列出全部持仓 |
| `getStock` | `{stockCode}` | 查询持仓详情 |
| `deleteStock` | `{stockCode}` | 逻辑删除 |
| `getMarketData` | `{stockCode}` | 单只实时行情 + 盈亏 |
| `getAllMarketData` | `{}` | 全部实时行情 + 盈亏 |
| `getProfitSummary` | `{}` | 盈亏汇总 |

## 股票类型

| 类型 | 代码示例 |
|------|----------|
| A股 | 600519（6位数字） |
| 港股 | 00700（5位数字） |
| 美股 | AAPL |
| 英股 | BP |
| 贵金属 | AU |

## 配置

```yaml
# application.yml
server:
  port: ${SERVER_PORT:18080}

spring:
  datasource:
    url: jdbc:sqlite:/root/stock_dir/db/stock.db
```

数据库在首次启动时自动建表（`db/schema.sql`）。

## 开发

```bash
# 编译
mvn package

# 运行测试
mvn test

# 启动
mvn spring-boot:run
```
