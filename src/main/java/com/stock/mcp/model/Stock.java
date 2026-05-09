package com.stock.mcp.model;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("stocks")
public class Stock {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("stock_type")
    private String stockType;

    @TableField("stock_code")
    private String stockCode;

    @TableField("holding_quantity")
    private Long holdingQuantity;

    @TableField("holding_price")
    private BigDecimal holdingPrice;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField(exist = false)
    private BigDecimal holdingCost;

    public Stock() {
    }

    public Stock(String stockType, String stockCode, Long holdingQuantity, BigDecimal holdingPrice) {
        this.stockType = stockType;
        this.stockCode = stockCode;
        this.holdingQuantity = holdingQuantity;
        this.holdingPrice = holdingPrice;
    }

    public BigDecimal getHoldingCost() {
        if (holdingPrice == null || holdingQuantity == null) {
            return null;
        }
        return holdingPrice.multiply(new BigDecimal(holdingQuantity));
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStockType() { return stockType; }
    public void setStockType(String stockType) { this.stockType = stockType; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public Long getHoldingQuantity() { return holdingQuantity; }
    public void setHoldingQuantity(Long holdingQuantity) { this.holdingQuantity = holdingQuantity; }

    public BigDecimal getHoldingPrice() { return holdingPrice; }
    public void setHoldingPrice(BigDecimal holdingPrice) { this.holdingPrice = holdingPrice; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public void setHoldingCost(BigDecimal holdingCost) { this.holdingCost = holdingCost; }
}
