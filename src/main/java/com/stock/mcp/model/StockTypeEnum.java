package com.stock.mcp.model;

/**
 * 股票类型枚举
 */
public enum StockTypeEnum {

    A_SHARE("A股", "中国大陆A股市场"),
    HK_SHARE("港股", "香港股票市场"),
    US_SHARE("美股", "美国股票市场"),
    UK_SHARE("英股", "英国股票市场"),
    PRECIOUS_METAL("贵金属", "黄金、白银等贵金属");

    private final String name;
    private final String description;

    StockTypeEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public static StockTypeEnum fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return A_SHARE;
        }
        for (StockTypeEnum type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return A_SHARE;
    }

    public static boolean isAShare(String name) {
        return A_SHARE.getName().equals(name);
    }

    public static boolean isHkShare(String name) {
        return HK_SHARE.getName().equals(name);
    }

    public static boolean isUsShare(String name) {
        return US_SHARE.getName().equals(name);
    }
}
