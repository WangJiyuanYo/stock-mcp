package com.stock.mcp.tool;

import com.stock.mcp.api.StockApiService;
import com.stock.mcp.model.Stock;
import com.stock.mcp.model.StockMarketData;
import com.stock.mcp.model.StockQuote;
import com.stock.mcp.service.StockService;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class StockDataTool {

    private final StockService stockService;
    private final StockApiService stockApiService;

    public StockDataTool(StockService stockService, StockApiService stockApiService) {
        this.stockService = stockService;
        this.stockApiService = stockApiService;
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> addOrUpdateStock() {
        return FunctionToolCallback
                .builder("addOrUpdateStock", (Map<String, Object> input) -> {
                    String stockType = (String) input.get("stockType");
                    String stockCode = (String) input.get("stockCode");
                    Long holdingQuantity = toLong(input.get("holdingQuantity"));
                    BigDecimal holdingPrice = toBigDecimal(input.get("holdingPrice"));

                    Stock stock = new Stock(stockType, stockCode, holdingQuantity, holdingPrice);
                    String message = stockService.saveOrUpdateStock(stock);

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("success", true);
                    result.put("message", message);
                    result.put("symbol", stock.getStockCode());
                    result.put("stock", stockToMap(stock));
                    return result;
                })
                .description("添加或更新股票持仓。输入: {stockType(可选), stockCode(必填), holdingQuantity, holdingPrice}")
                .inputType(Map.class)
                .build();
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> batchSaveStocks() {
        return FunctionToolCallback
                .builder("batchSaveStocks", (Map<String, Object> input) -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> stockList = (List<Map<String, Object>>) input.get("stocks");
                    if (stockList == null || stockList.isEmpty()) {
                        Map<String, Object> err = new LinkedHashMap<>();
                        err.put("success", false);
                        err.put("message", "股票列表不能为空");
                        return err;
                    }

                    List<Stock> stocks = new ArrayList<>();
                    for (Map<String, Object> map : stockList) {
                        Stock s = new Stock(
                                (String) map.get("stockType"),
                                (String) map.get("stockCode"),
                                toLong(map.get("holdingQuantity")),
                                toBigDecimal(map.get("holdingPrice")));
                        stocks.add(s);
                    }

                    String message = stockService.saveBatchStocks(stocks);

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("success", true);
                    result.put("message", message);
                    result.put("total", stocks.size());
                    return result;
                })
                .description("批量添加或更新股票。输入: {stocks: [{stockType, stockCode, holdingQuantity, holdingPrice}]}")
                .inputType(Map.class)
                .build();
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> listStocks() {
        return FunctionToolCallback
                .builder("listStocks", (Map<String, Object> _unused) -> {
                    List<Stock> stocks = stockService.getAllStocks();
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (Stock s : stocks) {
                        list.add(stockToMap(s));
                    }

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("success", true);
                    result.put("count", stocks.size());
                    result.put("stocks", list);
                    return result;
                })
                .description("获取所有股票持仓列表")
                .inputType(Map.class)
                .build();
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> getStock() {
        return FunctionToolCallback
                .builder("getStock", (Map<String, Object> input) -> {
                    String stockCode = (String) input.get("stockCode");
                    Stock stock = stockService.findByStockCode(stockCode);

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("symbol", stockCode.toUpperCase());
                    if (stock != null) {
                        result.put("found", true);
                        result.put("stock", stockToMap(stock));
                    } else {
                        result.put("found", false);
                        result.put("message", "股票不存在: " + stockCode.toUpperCase());
                    }
                    return result;
                })
                .description("根据股票代码查询持仓信息。输入: {stockCode}")
                .inputType(Map.class)
                .build();
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> deleteStock() {
        return FunctionToolCallback
                .builder("deleteStock", (Map<String, Object> input) -> {
                    String stockCode = (String) input.get("stockCode");
                    try {
                        String message = stockService.deleteStock(stockCode);
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("success", true);
                        result.put("message", message);
                        result.put("symbol", stockCode.toUpperCase());
                        return result;
                    } catch (IllegalArgumentException e) {
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("success", false);
                        result.put("message", e.getMessage());
                        return result;
                    }
                })
                .description("删除指定的股票持仓（逻辑删除）。输入: {stockCode}")
                .inputType(Map.class)
                .build();
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> getMarketData() {
        return FunctionToolCallback
                .builder("getMarketData", (Map<String, Object> input) -> {
                    String stockCode = (String) input.get("stockCode");
                    Map<String, Object> result = new LinkedHashMap<>();
                    try {
                        StockMarketData data = stockApiService.fetchStockMarketDataWithProfit(stockCode);
                        if (data == null) {
                            result.put("success", false);
                            result.put("message", "未获取到行情数据: " + stockCode);
                        } else {
                            result.put("success", true);
                            result.put("marketData", marketDataToMap(data));
                        }
                    } catch (Exception e) {
                        result.put("success", false);
                        result.put("message", "获取行情失败: " + e.getMessage());
                    }
                    return result;
                })
                .description("获取单只股票的实时行情数据（含盈亏计算）。输入: {stockCode}")
                .inputType(Map.class)
                .build();
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> getAllMarketData() {
        return FunctionToolCallback
                .builder("getAllMarketData", (Map<String, Object> _unused) -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    try {
                        List<StockMarketData> dataList = stockApiService.fetchAllStockMarketDataWithProfit();
                        List<Map<String, Object>> list = new ArrayList<>();
                        for (StockMarketData d : dataList) {
                            list.add(marketDataToMap(d));
                        }
                        result.put("success", true);
                        result.put("count", dataList.size());
                        result.put("stocks", list);
                    } catch (Exception e) {
                        result.put("success", false);
                        result.put("message", "获取行情数据失败: " + e.getMessage());
                    }
                    return result;
                })
                .description("获取所有股票的实时行情数据（含盈亏计算）")
                .inputType(Map.class)
                .build();
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> getProfitSummary() {
        return FunctionToolCallback
                .builder("getProfitSummary", (Map<String, Object> _unused) -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    try {
                        List<StockMarketData> dataList = stockApiService.fetchAllStockMarketDataWithProfit();

                        BigDecimal totalProfitLoss = BigDecimal.ZERO;
                        BigDecimal totalMarketValue = BigDecimal.ZERO;
                        BigDecimal totalCost = BigDecimal.ZERO;
                        BigDecimal totalTodayProfitLoss = BigDecimal.ZERO;

                        List<Map<String, Object>> list = new ArrayList<>();
                        for (StockMarketData data : dataList) {
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("name", data.getName());
                            item.put("stockCode", data.getStockCode());
                            item.put("currentPrice", data.getCurrentPrice());
                            item.put("holdingPrice", data.getHoldingPrice());
                            item.put("holdingQuantity", data.getHoldingQuantity());
                            item.put("holdingCost", data.getHoldingCost());
                            item.put("marketValue", data.getMarketValue());
                            item.put("profitLoss", data.getProfitLoss());
                            item.put("profitLossPercent", data.getProfitLossPercent());
                            item.put("todayProfitLoss", data.getTodayProfitLoss());
                            item.put("changePercent", data.getChangePercent());
                            list.add(item);

                            if (data.getProfitLoss() != null) {
                                totalProfitLoss = totalProfitLoss.add(data.getProfitLoss());
                            }
                            if (data.getMarketValue() != null) {
                                totalMarketValue = totalMarketValue.add(data.getMarketValue());
                            }
                            if (data.getHoldingCost() != null) {
                                totalCost = totalCost.add(data.getHoldingCost());
                            }
                            if (data.getTodayProfitLoss() != null) {
                                totalTodayProfitLoss = totalTodayProfitLoss.add(data.getTodayProfitLoss());
                            }
                        }

                        Map<String, Object> summary = new LinkedHashMap<>();
                        summary.put("totalProfitLoss", totalProfitLoss);
                        summary.put("totalMarketValue", totalMarketValue);
                        summary.put("totalCost", totalCost);
                        summary.put("totalTodayProfitLoss", totalTodayProfitLoss);
                        summary.put("profitLossPercent", totalCost.compareTo(BigDecimal.ZERO) > 0 ?
                                totalProfitLoss.multiply(new BigDecimal("100")).divide(totalCost, 2, java.math.RoundingMode.HALF_UP) : null);

                        result.put("success", true);
                        result.put("stocks", list);
                        result.put("summary", summary);
                    } catch (Exception e) {
                        result.put("success", false);
                        result.put("message", "获取盈亏数据失败: " + e.getMessage());
                    }
                    return result;
                })
                .description("获取所有股票的盈亏汇总（含总盈亏、总市值、总成本、今日盈亏）")
                .inputType(Map.class)
                .build();
    }

    @Bean
    public FunctionToolCallback<Map<String, Object>, Map<String, Object>> getQuote() {
        return FunctionToolCallback
                .builder("getQuote", (Map<String, Object> input) -> {
                    String stockCode = (String) input.get("stockCode");
                    Map<String, Object> result = new LinkedHashMap<>();
                    if (stockCode == null || stockCode.trim().isEmpty()) {
                        result.put("success", false);
                        result.put("message", "stockCode 不能为空");
                        return result;
                    }
                    try {
                        StockQuote quote = stockApiService.fetchQuote(stockCode.trim());
                        if (quote == null) {
                            result.put("success", false);
                            result.put("message", "未获取到行情数据: " + stockCode);
                        } else {
                            result.put("success", true);
                            result.put("quote", quoteToMap(quote));
                        }
                    } catch (IllegalArgumentException e) {
                        result.put("success", false);
                        result.put("message", e.getMessage());
                    } catch (Exception e) {
                        result.put("success", false);
                        result.put("message", "获取行情失败: " + e.getMessage());
                    }
                    return result;
                })
                .description("获取股票最新行情（价格 + 涨跌幅），自动识别 A股 / 美股。"
                        + "输入: {stockCode} 例: \"600000\"（A股）或 \"SPCX\"（美股）")
                .inputType(Map.class)
                .build();
    }

    // ---- helper methods ----

    private Map<String, Object> quoteToMap(StockQuote q) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("symbol", q.getSymbol());
        map.put("formattedCode", q.getFormattedCode());
        map.put("name", q.getName());
        map.put("market", q.getMarket());
        map.put("price", q.getPrice());
        map.put("previousClose", q.getPreviousClose());
        map.put("changeAmount", q.getChangeAmount());
        map.put("changePercent", q.getChangePercent());
        map.put("open", q.getOpen());
        map.put("high", q.getHigh());
        map.put("low", q.getLow());
        map.put("date", q.getDate());
        map.put("time", q.getTime());
        return map;
    }


    private Map<String, Object> stockToMap(Stock s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("stockType", s.getStockType());
        map.put("stockCode", s.getStockCode());
        map.put("holdingQuantity", s.getHoldingQuantity());
        map.put("holdingPrice", s.getHoldingPrice());
        map.put("holdingCost", s.getHoldingCost());
        map.put("createTime", s.getCreateTime() != null ? s.getCreateTime().toString() : null);
        map.put("updateTime", s.getUpdateTime() != null ? s.getUpdateTime().toString() : null);
        return map;
    }

    private Map<String, Object> marketDataToMap(StockMarketData data) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", data.getName());
        map.put("stockCode", data.getStockCode());
        map.put("currentPrice", data.getCurrentPrice());
        map.put("todayOpen", data.getTodayOpen());
        map.put("yesterdayClose", data.getYesterdayClose());
        map.put("todayHigh", data.getTodayHigh());
        map.put("todayLow", data.getTodayLow());
        map.put("changePercent", data.getChangePercent());
        map.put("changeAmount", data.getChangeAmount());
        map.put("volume", data.getVolume());
        map.put("turnover", data.getTurnover());
        map.put("holdingQuantity", data.getHoldingQuantity());
        map.put("holdingPrice", data.getHoldingPrice());
        map.put("holdingCost", data.getHoldingCost());
        map.put("marketValue", data.getMarketValue());
        map.put("profitLoss", data.getProfitLoss());
        map.put("profitLossPercent", data.getProfitLossPercent());
        map.put("todayProfitLoss", data.getTodayProfitLoss());
        map.put("date", data.getDate());
        map.put("time", data.getTime());
        return map;
    }

    private Long toLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String && !((String) value).isEmpty()) return Long.parseLong((String) value);
        return 0L;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        if (value instanceof String && !((String) value).isEmpty()) return new BigDecimal((String) value);
        return BigDecimal.ZERO;
    }
}
