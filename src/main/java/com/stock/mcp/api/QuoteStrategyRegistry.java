package com.stock.mcp.api;

import com.stock.mcp.api.strategy.MarketQuoteStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 行情策略注册表
 * Spring 自动注入所有 MarketQuoteStrategy 实现，按 supports(code) 选择
 */
@Component
public class QuoteStrategyRegistry {

    private final List<MarketQuoteStrategy> strategies;

    public QuoteStrategyRegistry(List<MarketQuoteStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 根据用户输入的代码挑选策略
     * @throws IllegalArgumentException 没有任何策略匹配
     */
    public MarketQuoteStrategy pick(String code) {
        return strategies.stream()
                .filter(s -> s.supports(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "不支持的股票代码格式: " + code + "（已支持：A股 6 位数字 / 美股字母代码）"));
    }

    /** 按市场返回（如需指定市场跳过嗅探） */
    public MarketQuoteStrategy byMarket(String marketName) {
        return strategies.stream()
                .filter(s -> s.market().getName().equals(marketName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未注册的市场: " + marketName));
    }
}
