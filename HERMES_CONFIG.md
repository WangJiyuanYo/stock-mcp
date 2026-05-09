# Stock MCP Server — Hermes 配置说明

## SSE 端点

```
http://127.0.0.1:18080/sse
```

## 启动命令

```bash
mkdir -p /root/stock_dir/db
java -jar /root/stock_dir/mcp/stock-mcp-0.0.1-SNAPSHOT.jar
```

## Hermes 配置 JSON

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

## 可用工具（8个）

| 工具名 | 输入 | 描述 |
|--------|------|------|
| `addOrUpdateStock` | `{stockType, stockCode, holdingQuantity, holdingPrice}` | 添加或更新持仓（支持恢复已删除记录） |
| `batchSaveStocks` | `{stocks: [...]}` | 批量保存股票 |
| `listStocks` | `{}` 或留空 | 列出全部持仓 |
| `getStock` | `{stockCode}` | 按代码查询持仓详情 |
| `deleteStock` | `{stockCode}` | 逻辑删除（可恢复） |
| `getMarketData` | `{stockCode}` | 单只实时行情 + 盈亏计算 |
| `getAllMarketData` | `{}` 或留空 | 全部实时行情 + 盈亏计算 |
| `getProfitSummary` | `{}` 或留空 | 盈亏汇总（总盈亏/总市值/总成本/今日盈亏） |

## 典型使用流程

### 1. 添加持仓

调用 `addOrUpdateStock`：

```json
{
  "stockType": "A股",
  "stockCode": "600519",
  "holdingQuantity": 100,
  "holdingPrice": 1680.00
}
```

### 2. 批量添加

调用 `batchSaveStocks`：

```json
{
  "stocks": [
    { "stockType": "A股", "stockCode": "600519", "holdingQuantity": 100, "holdingPrice": 1680.00 },
    { "stockType": "港股", "stockCode": "00700", "holdingQuantity": 500, "holdingPrice": 380.00 },
    { "stockType": "美股", "stockCode": "AAPL", "holdingQuantity": 200, "holdingPrice": 175.50 }
  ]
}
```

### 3. 查询单只股票

调用 `getStock`：

```json
{ "stockCode": "600519" }
```

### 4. 实时行情

调用 `getMarketData`：

```json
{ "stockCode": "600519" }
```

### 5. 删除股票

调用 `deleteStock`：

```json
{ "stockCode": "600519" }
```

### 6. 查看全部实时行情

调用 `getAllMarketData`（传 `{}` 即可），获取全部持仓的实时行情、持仓成本、浮动盈亏、今日盈亏。

### 7. 盈亏汇总

调用 `getProfitSummary`（传入任意字符串），获取总盈亏、总市值、总成本、今日总盈亏。

## 股票类型说明

| 类型 | 说明 | 代码示例 |
|------|------|----------|
| A股 | 中国大陆A股市场 | 600519（6位数字） |
| 港股 | 香港股票市场 | 00700（5位数字） |
| 美股 | 美国股票市场 | AAPL |
| 英股 | 英国股票市场 | BP |
| 贵金属 | 黄金、白银等 | AU |

## 数据库

- 路径：`/root/stock_dir/db/stock.db`（SQLite，自动建表）
- 部署前确保目录存在：`mkdir -p /root/stock_dir/db`
