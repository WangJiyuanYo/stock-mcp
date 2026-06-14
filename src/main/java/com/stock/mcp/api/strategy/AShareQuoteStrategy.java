package com.stock.mcp.api.strategy;

import com.stock.mcp.model.StockQuote;
import com.stock.mcp.model.StockTypeEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A 股行情策略：6 位纯数字代码 → sh / sz 前缀；新浪返回 32 字段格式
 *
 * 字段顺序参考：name, todayOpen, yesterdayClose, currentPrice, todayHigh, todayLow,
 *              ... (买卖盘 24 字段) ..., date, time
 */
@Component
public class AShareQuoteStrategy implements MarketQuoteStrategy {

    @Override
    public StockTypeEnum market() {
        return StockTypeEnum.A_SHARE;
    }

    @Override
    public boolean supports(String code) {
        if (code == null) return false;
        String c = code.trim().toLowerCase();
        if (c.startsWith("sh") || c.startsWith("sz")) return true;
        return c.matches("\\d{6}");
    }

    @Override
    public String formatSymbol(String code) {
        String c = code.trim().toLowerCase();
        if (c.startsWith("sh") || c.startsWith("sz")) return c;
        char first = c.charAt(0);
        return (first == '6' || first == '5' || first == '9' ? "sh" : "sz") + c;
    }

    @Override
    public StockQuote parse(String stockCodeWithPrefix, String[] fields) {
        if (fields.length < 32) return null;

        StockQuote q = new StockQuote();
        q.setFormattedCode(stockCodeWithPrefix);
        q.setSymbol(stockCodeWithPrefix.length() > 2
                ? stockCodeWithPrefix.substring(2)
                : stockCodeWithPrefix);
        q.setMarket(market().getName());
        q.setName(fields[0]);
        q.setOpen(parseBigDecimal(fields[1]));
        q.setPreviousClose(parseBigDecimal(fields[2]));
        q.setPrice(parseBigDecimal(fields[3]));
        q.setHigh(parseBigDecimal(fields[4]));
        q.setLow(parseBigDecimal(fields[5]));
        q.setDate(fields[30]);
        q.setTime(fields[31]);

        // 涨跌额 / 涨跌幅由 price + previousClose 计算
        if (q.getPrice() != null
                && q.getPreviousClose() != null
                && q.getPreviousClose().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = q.getPrice().subtract(q.getPreviousClose());
            q.setChangeAmount(diff);
            q.setChangePercent(diff.multiply(new BigDecimal("100"))
                    .divide(q.getPreviousClose(), 2, RoundingMode.HALF_UP));
        }
        return q;
    }
}
