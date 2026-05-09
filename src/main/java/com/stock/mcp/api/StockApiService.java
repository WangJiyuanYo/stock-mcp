package com.stock.mcp.api;

import com.stock.mcp.model.Stock;
import com.stock.mcp.model.StockMarketData;
import com.stock.mcp.service.StockService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class StockApiService {

    private static final String API_URL = "https://hq.sinajs.cn/list=";
    private final HttpClient httpClient;
    private final StockService stockService;

    public StockApiService(StockService stockService) {
        this.stockService = stockService;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public List<StockMarketData> fetchAllStockMarketDataWithProfit() throws IOException {
        List<Stock> allStocks = stockService.getAllStocks();

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

    public StockMarketData fetchStockMarketDataWithProfit(String stockCode) throws IOException {
        StockMarketData marketData = fetchStockMarketData(stockCode);
        if (marketData != null) {
            List<Stock> allStocks = stockService.getAllStocks();
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
        return marketData;
    }

    public List<StockMarketData> fetchMarketData(List<String> stockCodes) throws IOException {
        if (stockCodes == null || stockCodes.isEmpty()) {
            return new ArrayList<>();
        }

        String codesParam = String.join(",", stockCodes);
        String fullUrl = buildStockUrl(codesParam);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Referer", "http://finance.sina.com.cn/")
                    .header("Accept", "*/*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseStockData(response.body(), stockCodes);
            } else {
                throw new IOException("API 请求失败，状态码: " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("请求被中断", e);
        }
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
            int eqIndex = line.indexOf("=");
            int quoteStart = line.indexOf("\"", eqIndex);
            int quoteEnd = line.lastIndexOf("\"");

            if (eqIndex == -1 || quoteStart == -1 || quoteEnd == -1) return null;

            String stockCodeWithPrefix = line.substring(0, eqIndex).replace("var hq_str_", "").trim();
            String dataPart = line.substring(quoteStart + 1, quoteEnd);
            String[] fields = dataPart.split(",");

            if (fields.length < 32) return null;

            StockMarketData data = new StockMarketData();
            data.setStockCode(stockCodeWithPrefix);
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
}
