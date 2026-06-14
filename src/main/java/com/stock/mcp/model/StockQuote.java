package com.stock.mcp.model;

import java.math.BigDecimal;

/**
 * 通用股票行情 DTO（精简版，不含持仓/盈亏字段）
 * 跨市场（A股 / 美股 / 港股 ...）统一返回结构
 */
public class StockQuote {

    private String symbol;          // 用户输入的原始代码（去前缀），如 "600000" / "SPCX"
    private String formattedCode;   // 接口侧带前缀代码，如 "sh600000" / "gb_spcx"
    private String name;            // 股票名称
    private String market;          // 市场名（A股 / 美股 / 港股）

    private BigDecimal price;          // 当前价 / 最新成交价（盘后即收盘价）
    private BigDecimal previousClose;  // 昨收
    private BigDecimal changeAmount;   // 涨跌额
    private BigDecimal changePercent;  // 涨跌幅 (%)

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;

    private String date;
    private String time;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getFormattedCode() { return formattedCode; }
    public void setFormattedCode(String formattedCode) { this.formattedCode = formattedCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }

    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }

    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }

    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }

    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }

    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
