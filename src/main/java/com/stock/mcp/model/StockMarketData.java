package com.stock.mcp.model;

import java.math.BigDecimal;

/**
 * 股票行情数据实体
 * 用于存储从 API 获取的实时行情数据
 */
public class StockMarketData {

    private String stockCode;
    private String name;
    private BigDecimal todayOpen;
    private BigDecimal yesterdayClose;
    private BigDecimal currentPrice;
    private BigDecimal todayHigh;
    private BigDecimal todayLow;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private Long volume;
    private BigDecimal turnover;

    private Long bidQty1;
    private BigDecimal bidPrice1;
    private Long bidQty2;
    private BigDecimal bidPrice2;
    private Long bidQty3;
    private BigDecimal bidPrice3;
    private Long bidQty4;
    private BigDecimal bidPrice4;
    private Long bidQty5;
    private BigDecimal bidPrice5;

    private Long askQty1;
    private BigDecimal askPrice1;
    private Long askQty2;
    private BigDecimal askPrice2;
    private Long askQty3;
    private BigDecimal askPrice3;
    private Long askQty4;
    private BigDecimal askPrice4;
    private Long askQty5;
    private BigDecimal askPrice5;

    private String date;
    private String time;

    private Long holdingQuantity;
    private BigDecimal holdingCost;
    private BigDecimal holdingPrice;

    public BigDecimal getChangePercent() {
        if (currentPrice == null || yesterdayClose == null || yesterdayClose.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal change = currentPrice.subtract(yesterdayClose);
        return change.multiply(new BigDecimal("100")).divide(yesterdayClose, 2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getChangeAmount() {
        if (currentPrice == null || yesterdayClose == null) return null;
        return currentPrice.subtract(yesterdayClose);
    }

    public BigDecimal getMarketValue() {
        if (currentPrice == null || holdingQuantity == null) return null;
        return currentPrice.multiply(new BigDecimal(holdingQuantity));
    }

    public BigDecimal getProfitLoss() {
        if (currentPrice == null || holdingPrice == null || holdingQuantity == null) return null;
        BigDecimal priceDiff = currentPrice.subtract(holdingPrice);
        return priceDiff.multiply(new BigDecimal(holdingQuantity));
    }

    public BigDecimal getProfitLossPercent() {
        if (holdingPrice == null || holdingPrice.compareTo(BigDecimal.ZERO) == 0) return null;
        BigDecimal priceDiff = currentPrice.subtract(holdingPrice);
        return priceDiff.multiply(new BigDecimal("100")).divide(holdingPrice, 2, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal getTodayProfitLoss() {
        if (currentPrice == null || yesterdayClose == null || holdingQuantity == null) return null;
        BigDecimal priceDiff = currentPrice.subtract(yesterdayClose);
        return priceDiff.multiply(new BigDecimal(holdingQuantity));
    }

    // Getters and Setters

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTodayOpen() { return todayOpen; }
    public void setTodayOpen(BigDecimal todayOpen) { this.todayOpen = todayOpen; }

    public BigDecimal getYesterdayClose() { return yesterdayClose; }
    public void setYesterdayClose(BigDecimal yesterdayClose) { this.yesterdayClose = yesterdayClose; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getTodayHigh() { return todayHigh; }
    public void setTodayHigh(BigDecimal todayHigh) { this.todayHigh = todayHigh; }

    public BigDecimal getTodayLow() { return todayLow; }
    public void setTodayLow(BigDecimal todayLow) { this.todayLow = todayLow; }

    public BigDecimal getBidPrice() { return bidPrice; }
    public void setBidPrice(BigDecimal bidPrice) { this.bidPrice = bidPrice; }

    public BigDecimal getAskPrice() { return askPrice; }
    public void setAskPrice(BigDecimal askPrice) { this.askPrice = askPrice; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public BigDecimal getTurnover() { return turnover; }
    public void setTurnover(BigDecimal turnover) { this.turnover = turnover; }

    public Long getBidQty1() { return bidQty1; }
    public void setBidQty1(Long bidQty1) { this.bidQty1 = bidQty1; }
    public BigDecimal getBidPrice1() { return bidPrice1; }
    public void setBidPrice1(BigDecimal bidPrice1) { this.bidPrice1 = bidPrice1; }
    public Long getBidQty2() { return bidQty2; }
    public void setBidQty2(Long bidQty2) { this.bidQty2 = bidQty2; }
    public BigDecimal getBidPrice2() { return bidPrice2; }
    public void setBidPrice2(BigDecimal bidPrice2) { this.bidPrice2 = bidPrice2; }
    public Long getBidQty3() { return bidQty3; }
    public void setBidQty3(Long bidQty3) { this.bidQty3 = bidQty3; }
    public BigDecimal getBidPrice3() { return bidPrice3; }
    public void setBidPrice3(BigDecimal bidPrice3) { this.bidPrice3 = bidPrice3; }
    public Long getBidQty4() { return bidQty4; }
    public void setBidQty4(Long bidQty4) { this.bidQty4 = bidQty4; }
    public BigDecimal getBidPrice4() { return bidPrice4; }
    public void setBidPrice4(BigDecimal bidPrice4) { this.bidPrice4 = bidPrice4; }
    public Long getBidQty5() { return bidQty5; }
    public void setBidQty5(Long bidQty5) { this.bidQty5 = bidQty5; }
    public BigDecimal getBidPrice5() { return bidPrice5; }
    public void setBidPrice5(BigDecimal bidPrice5) { this.bidPrice5 = bidPrice5; }

    public Long getAskQty1() { return askQty1; }
    public void setAskQty1(Long askQty1) { this.askQty1 = askQty1; }
    public BigDecimal getAskPrice1() { return askPrice1; }
    public void setAskPrice1(BigDecimal askPrice1) { this.askPrice1 = askPrice1; }
    public Long getAskQty2() { return askQty2; }
    public void setAskQty2(Long askQty2) { this.askQty2 = askQty2; }
    public BigDecimal getAskPrice2() { return askPrice2; }
    public void setAskPrice2(BigDecimal askPrice2) { this.askPrice2 = askPrice2; }
    public Long getAskQty3() { return askQty3; }
    public void setAskQty3(Long askQty3) { this.askQty3 = askQty3; }
    public BigDecimal getAskPrice3() { return askPrice3; }
    public void setAskPrice3(BigDecimal askPrice3) { this.askPrice3 = askPrice3; }
    public Long getAskQty4() { return askQty4; }
    public void setAskQty4(Long askQty4) { this.askQty4 = askQty4; }
    public BigDecimal getAskPrice4() { return askPrice4; }
    public void setAskPrice4(BigDecimal askPrice4) { this.askPrice4 = askPrice4; }
    public Long getAskQty5() { return askQty5; }
    public void setAskQty5(Long askQty5) { this.askQty5 = askQty5; }
    public BigDecimal getAskPrice5() { return askPrice5; }
    public void setAskPrice5(BigDecimal askPrice5) { this.askPrice5 = askPrice5; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Long getHoldingQuantity() { return holdingQuantity; }
    public void setHoldingQuantity(Long holdingQuantity) { this.holdingQuantity = holdingQuantity; }

    public BigDecimal getHoldingCost() { return holdingCost; }
    public void setHoldingCost(BigDecimal holdingCost) { this.holdingCost = holdingCost; }

    public BigDecimal getHoldingPrice() { return holdingPrice; }
    public void setHoldingPrice(BigDecimal holdingPrice) { this.holdingPrice = holdingPrice; }
}
