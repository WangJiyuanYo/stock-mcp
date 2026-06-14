package com.stock.mcp.api;

import com.stock.mcp.api.strategy.MarketQuoteStrategy;
import com.stock.mcp.model.Stock;
import com.stock.mcp.model.StockMarketData;
import com.stock.mcp.model.StockQuote;
import com.stock.mcp.model.StockTypeEnum;
import com.stock.mcp.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockApiService {

    private static final Logger log = LoggerFactory.getLogger(StockApiService.class);

    private static final String API_URL = "https://hq.sinajs.cn/list=";
    /** 新浪 hq 接口的中文名是 GBK 编码（A股/港股中文名、美股中文别名都受影响） */
    private static final Charset SINA_CHARSET = Charset.forName("GBK");
    private final HttpClient httpClient;
    private final StockService stockService;
    private final QuoteStrategyRegistry strategyRegistry;

    public StockApiService(StockService stockService, QuoteStrategyRegistry strategyRegistry) {
        this.stockService = stockService;
        this.strategyRegistry = strategyRegistry;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    // ============ 通用行情查询（策略派发，跨市场） ============

    /**
     * 查询任意市场单只股票的行情（精简版，不含持仓盈亏）
     * 自动按代码格式识别 A股 / 美股 / ...
     */
    public StockQuote fetchQuote(String code) throws IOException {
        if (code == null || code.trim().isEmpty()) return null;

        MarketQuoteStrategy strategy = strategyRegistry.pick(code);
        String formatted = strategy.formatSymbol(code);
        String body = httpGet(API_URL + formatted);

        for (String line : body.split("\n")) {
            if (!line.contains("=")) continue;
            ParsedLine pl = splitLine(line);
            if (pl == null) continue;
            return strategy.parse(pl.codeWithPrefix, pl.fields);
        }
        return null;
    }

    // ============ 以下为现有 A 股 + 持仓盈亏路径，保持不动 ============

    /**
     * 获取所有 A 股持仓的行情 + 盈亏。
     * 仅纳入 stockType = "A 股" 的持仓——美股 / 港股 / 贵金属持仓不参与盈亏汇总，
     * 也避免触发新浪美股响应（28 字段）撞 A 股 32 字段解析失败的问题。
     * 美股行情请用 fetchQuote / getQuote 工具单独查询。
     */
    public List<StockMarketData> fetchAllStockMarketDataWithProfit() throws IOException {
        List<Stock> allHoldings = stockService.getAllStocks();
        List<Stock> allStocks = allHoldings.stream()
                .filter(s -> StockTypeEnum.A_SHARE.getName().equals(s.getStockType()))
                .toList();

        int filteredOut = allHoldings.size() - allStocks.size();
        if (filteredOut > 0) {
            log.info("持仓盈亏：纳入 {} 只 A 股，已排除 {} 只非 A 股持仓（美股请用 getQuote 查询）",
                    allStocks.size(), filteredOut);
        }

        if (allStocks.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> stockCodes = new ArrayList<>();
        for (Stock stock : allStocks) {
            if (stock.getStockCode() != null && !stock.getStockCode().trim().isEmpty()) {
                stockCodes.add(stock.getStockCode());
            }
        }

        List<StockMarketData> marketDataList = fetchMarketData(stockCodes);
        mergeHoldingInfo(marketDataList, allStocks);
        return marketDataList;
    }

    public StockMarketData fetchStockMarketData(String stockCode) throws IOException {
        List<String> codes = new ArrayList<>();
        codes.add(stockCode);
        List<StockMarketData> dataList = fetchMarketData(codes);
        return dataList.isEmpty() ? null : dataList.get(0);
    }

    /**
     * 单只持仓的行情 + 盈亏。仅支持 A 股持仓。
     * 传入的代码若 DB 里登记为美股 / 港股 / 等，直接返回 null —— 调用方应改用 fetchQuote/getQuote。
     * （旧逻辑会盲目走新浪美股 28 字段解析撞 32 字段下限，导致"行情解析异常"。）
     */
    public StockMarketData fetchStockMarketDataWithProfit(String stockCode) throws IOException {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return null;
        }
        // 用 DB 里登记的市场类型来卡——拼前缀的 normalizeStockCode 会把 "SPCX" 存为 "SPCX"，
        // 不改 SQL 直接按 stockCode 查 stock 即可
        Stock stock = stockService.findByStockCode(stockCode.trim().toUpperCase());
        if (stock == null) {
            log.warn("getMarketData: 未找到持仓记录 stockCode={}", stockCode);
            return null;
        }
        if (!StockTypeEnum.A_SHARE.getName().equals(stock.getStockType())) {
            log.info("getMarketData: 非 A 股持仓 stockCode={} stockType={}，建议使用 getQuote",
                    stockCode, stock.getStockType());
            return null;
        }

        StockMarketData marketData = fetchStockMarketData(stockCode);
        if (marketData != null) {
            String pureCode = removeMarketPrefix(marketData.getStockCode());
            if (pureCode.equals(stock.getStockCode())) {
                marketData.setHoldingQuantity(stock.getHoldingQuantity());
                marketData.setHoldingPrice(stock.getHoldingPrice());
                marketData.setHoldingCost(stock.getHoldingCost());
            }
        }
        return marketData;
    }

    public List<StockMarketData> fetchMarketData(List<String> stockCodes) throws IOException {
        if (stockCodes == null || stockCodes.isEmpty()) {
            return new ArrayList<>();
        }

        String codesParam = String.join(",", stockCodes);
        String fullUrl = buildStockUrl(codesParam);
        String body = httpGet(fullUrl);
        return parseStockData(body, stockCodes);
    }

    private void mergeHoldingInfo(List<StockMarketData> marketDataList, List<Stock> allStocks) {
        for (StockMarketData marketData : marketDataList) {
            String pureCode = removeMarketPrefix(marketData.getStockCode());
            for (Stock stock : allStocks) {
                if (pureCode.equals(stock.getStockCode())) {
                    marketData.setHoldingQuantity(stock.getHoldingQuantity());
                    marketData.setHoldingPrice(stock.getHoldingPrice());
                    marketData.setHoldingCost(stock.getHoldingCost());
                    break;
                }
            }
        }
    }

    private String removeMarketPrefix(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) return "";
        String code = stockCode.trim();
        if (code.startsWith("sh") || code.startsWith("sz") || code.startsWith("hk")) {
            return code.substring(2);
        }
        if (code.toLowerCase().startsWith("gb_")) {
            return code.substring(3);
        }
        return code;
    }

    private String buildStockUrl(String codesParam) {
        String[] codes = codesParam.split(",");
        StringBuilder formattedCodes = new StringBuilder();
        for (int i = 0; i < codes.length; i++) {
            String code = codes[i].trim();
            if (!code.isEmpty()) {
                if (i > 0) formattedCodes.append(",");
                formattedCodes.append(formatStockCode(code));
            }
        }
        return API_URL + formattedCodes.toString();
    }

    private String formatStockCode(String code) {
        if (code == null || code.trim().isEmpty()) return "";
        code = code.trim();

        if (code.length() == 6 && code.matches("\\d+")) {
            char firstChar = code.charAt(0);
            if (firstChar == '6' || firstChar == '5' || firstChar == '9') {
                return "sh" + code;
            } else {
                return "sz" + code;
            }
        }

        if (code.length() == 5 && code.matches("\\d+")) {
            return "hk" + code;
        }

        if (code.toLowerCase().startsWith("gb_")) {
            return code.toLowerCase();
        }

        return "gb_" + code.toLowerCase();
    }

    private List<StockMarketData> parseStockData(String responseBody, List<String> stockCodes) {
        List<StockMarketData> resultList = new ArrayList<>();
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return resultList;
        }

        String[] lines = responseBody.split("\n");
        for (String line : lines) {
            if (line.contains("=")) {
                StockMarketData data = parseSingleStock(line);
                if (data != null) {
                    resultList.add(data);
                }
            }
        }
        return resultList;
    }

    private StockMarketData parseSingleStock(String line) {
        try {
            ParsedLine pl = splitLine(line);
            if (pl == null || pl.fields.length < 32) return null;
            String[] fields = pl.fields;

            StockMarketData data = new StockMarketData();
            data.setStockCode(pl.codeWithPrefix);
            data.setName(fields[0]);
            data.setTodayOpen(parseBigDecimal(fields[1]));
            data.setYesterdayClose(parseBigDecimal(fields[2]));
            data.setCurrentPrice(parseBigDecimal(fields[3]));
            data.setTodayHigh(parseBigDecimal(fields[4]));
            data.setTodayLow(parseBigDecimal(fields[5]));
            data.setBidPrice(parseBigDecimal(fields[6]));
            data.setAskPrice(parseBigDecimal(fields[7]));
            data.setVolume(parseLong(fields[8]));
            data.setTurnover(parseBigDecimal(fields[9]));
            data.setBidQty1(parseLong(fields[10]));
            data.setBidPrice1(parseBigDecimal(fields[11]));
            data.setBidQty2(parseLong(fields[12]));
            data.setBidPrice2(parseBigDecimal(fields[13]));
            data.setBidQty3(parseLong(fields[14]));
            data.setBidPrice3(parseBigDecimal(fields[15]));
            data.setBidQty4(parseLong(fields[16]));
            data.setBidPrice4(parseBigDecimal(fields[17]));
            data.setBidQty5(parseLong(fields[18]));
            data.setBidPrice5(parseBigDecimal(fields[19]));
            data.setAskQty1(parseLong(fields[20]));
            data.setAskPrice1(parseBigDecimal(fields[21]));
            data.setAskQty2(parseLong(fields[22]));
            data.setAskPrice2(parseBigDecimal(fields[23]));
            data.setAskQty3(parseLong(fields[24]));
            data.setAskPrice3(parseBigDecimal(fields[25]));
            data.setAskQty4(parseLong(fields[26]));
            data.setAskPrice4(parseBigDecimal(fields[27]));
            data.setAskQty5(parseLong(fields[28]));
            data.setAskPrice5(parseBigDecimal(fields[29]));
            data.setDate(fields[30]);
            data.setTime(fields[31]);

            return data;
        } catch (Exception e) {
            return null;
        }
    }

    // ============ 公共底层工具 ============

    /** 把 'var hq_str_xxx="a,b,c,..";' 一行拆成 (codeWithPrefix, fields[]) */
    private static ParsedLine splitLine(String line) {
        int eqIndex = line.indexOf("=");
        int quoteStart = line.indexOf("\"", eqIndex);
        int quoteEnd = line.lastIndexOf("\"");
        if (eqIndex == -1 || quoteStart == -1 || quoteEnd == -1 || quoteEnd <= quoteStart) return null;

        String codeWithPrefix = line.substring(0, eqIndex).replace("var hq_str_", "").trim();
        String dataPart = line.substring(quoteStart + 1, quoteEnd);
        return new ParsedLine(codeWithPrefix, dataPart.split(","));
    }

    private String httpGet(String url) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Referer", "http://finance.sina.com.cn/")
                    .header("Accept", "*/*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(SINA_CHARSET));

            if (response.statusCode() != 200) {
                throw new IOException("API 请求失败，状态码: " + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("请求被中断", e);
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.trim().isEmpty() || "0".equals(value.trim())) return null;
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        try {
            if (value == null || value.trim().isEmpty()) return null;
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static final class ParsedLine {
        final String codeWithPrefix;
        final String[] fields;
        ParsedLine(String codeWithPrefix, String[] fields) {
            this.codeWithPrefix = codeWithPrefix;
            this.fields = fields;
        }
    }
}
