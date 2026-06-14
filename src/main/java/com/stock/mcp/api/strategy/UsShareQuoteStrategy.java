package com.stock.mcp.api.strategy;

import com.stock.mcp.model.StockQuote;
import com.stock.mcp.model.StockTypeEnum;
import org.springframework.stereotype.Component;

/**
 * 美股行情策略：纯字母代码（含 BRK.B 这类带点号）或 gb_ 前缀 → gb_xxx 小写
 *
 * 新浪美股字段顺序（关键位）：
 *   [0] name             公司名
 *   [1] price            最新价 / 收盘价
 *   [2] changePercent    涨跌幅(%)
 *   [3] datetime         "yyyy-MM-dd HH:mm:ss"（盘后即收盘时间）
 *   [4] changeAmount     涨跌额
 *   [5] open             开盘
 *   [6] high             今日最高
 *   [7] low              今日最低
 * 后续字段（52周高低 / 成交量 / 市值 ...）当前不取，避免依赖不稳定的尾部字段。
 * previousClose 由 price - changeAmount 计算，避免依赖固定下标。
 */
@Component
public class UsShareQuoteStrategy implements MarketQuoteStrategy {

    @Override
    public StockTypeEnum market() {
        return StockTypeEnum.US_SHARE;
    }

    @Override
    public boolean supports(String code) {
        if (code == null) return false;
        String c = code.trim();
        if (c.toLowerCase().startsWith("gb_")) return true;
        // 纯字母，可带一个点号（BRK.B、BF.A）
        return c.matches("[A-Za-z]+(\\.[A-Za-z]+)?");
    }

    @Override
    public String formatSymbol(String code) {
        String c = code.trim().toLowerCase();
        return c.startsWith("gb_") ? c : "gb_" + c;
    }

    @Override
    public StockQuote parse(String stockCodeWithPrefix, String[] fields) {
        if (fields.length < 8) return null;

        StockQuote q = new StockQuote();
        q.setFormattedCode(stockCodeWithPrefix);
        q.setSymbol(stockCodeWithPrefix.toLowerCase().startsWith("gb_")
                ? stockCodeWithPrefix.substring(3).toUpperCase()
                : stockCodeWithPrefix.toUpperCase());
        q.setMarket(market().getName());
        q.setName(fields[0]);
        q.setPrice(parseBigDecimal(fields[1]));
        q.setChangePercent(parseBigDecimal(fields[2]));

        // datetime 拆成 date + time
        String dt = fields[3];
        if (dt != null && dt.contains(" ")) {
            int sp = dt.indexOf(' ');
            q.setDate(dt.substring(0, sp));
            q.setTime(dt.substring(sp + 1));
        } else {
            q.setDate(dt);
        }

        q.setChangeAmount(parseBigDecimal(fields[4]));
        q.setOpen(parseBigDecimal(fields[5]));
        q.setHigh(parseBigDecimal(fields[6]));
        q.setLow(parseBigDecimal(fields[7]));

        // previousClose = price - changeAmount，比依赖固定下标更稳
        if (q.getPrice() != null && q.getChangeAmount() != null) {
            q.setPreviousClose(q.getPrice().subtract(q.getChangeAmount()));
        }
        return q;
    }
}
