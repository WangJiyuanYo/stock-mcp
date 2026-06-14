package com.stock.mcp.api.strategy;

import com.stock.mcp.model.StockQuote;
import com.stock.mcp.model.StockTypeEnum;

import java.math.BigDecimal;

/**
 * 行情解析策略：每个市场（A股/美股/港股...）一个实现
 *
 * 新增市场只需：
 *   1. 实现本接口
 *   2. 标注 @Component
 * 主流程会自动识别并派发，无需改动 StockApiService / StockDataTool。
 */
public interface MarketQuoteStrategy {

    /** 该策略所属市场 */
    StockTypeEnum market();

    /** 嗅探：能否处理这个用户输入的代码（如 "600000" / "SPCX" / "gb_aapl"） */
    boolean supports(String code);

    /** 把用户输入转换成新浪接口期望的带前缀格式（如 SPCX → gb_spcx） */
    String formatSymbol(String code);

    /**
     * 解析新浪返回的一行的字段数组到 StockQuote
     * @param stockCodeWithPrefix  接口返回的带前缀代码（如 "gb_spcx" / "sh600000"），来自 var hq_str_xxx
     * @param fields               引号内字符串按 "," 切分后的字段数组
     * @return 解析结果，字段数不足或格式异常时返回 null
     */
    StockQuote parse(String stockCodeWithPrefix, String[] fields);

    /** 字段解析公共工具：空字符串 / 字面量 "0" / 解析失败均返回 null */
    default BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "0".equals(value.trim())) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
