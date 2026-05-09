package com.stock.mcp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.mcp.mapper.StockMapper;
import com.stock.mcp.model.Stock;
import com.stock.mcp.model.StockTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockService {

    private final StockMapper stockMapper;

    public StockService(StockMapper stockMapper) {
        this.stockMapper = stockMapper;
    }

    public void validateStock(Stock stock) {
        if (stock == null) {
            throw new IllegalArgumentException("股票信息不能为空");
        }
        if (stock.getStockCode() == null || stock.getStockCode().trim().isEmpty()) {
            throw new IllegalArgumentException("股票代码不能为空");
        }
        if (stock.getStockType() == null || stock.getStockType().trim().isEmpty()) {
            stock.setStockType(StockTypeEnum.A_SHARE.getName());
        } else {
            StockTypeEnum typeEnum = StockTypeEnum.fromName(stock.getStockType());
            if (typeEnum == null) {
                throw new IllegalArgumentException("无效的股票类型：" + stock.getStockType());
            }
            stock.setStockType(typeEnum.getName());
        }
    }

    public String normalizeStockCode(String stockCode) {
        if (stockCode == null) return null;
        return stockCode.replaceAll("^(sh|sz|hk|gb_)", "").toUpperCase();
    }

    public List<Stock> getAllStocks() {
        return stockMapper.selectList(null);
    }

    @Transactional
    public String saveOrUpdateStock(Stock stock) {
        validateStock(stock);
        stock.setStockCode(normalizeStockCode(stock.getStockCode()));

        Stock existingStock = stockMapper.selectByStockCode(stock.getStockCode());

        if (existingStock != null) {
            stock.setId(existingStock.getId());
            stockMapper.updateById(stock);
            return "股票信息更新成功";
        }

        Stock deletedStock = stockMapper.selectByStockCodeAny(stock.getStockCode());
        if (deletedStock != null) {
            stock.setId(deletedStock.getId());
            stock.setDeleted(0);
            stockMapper.updateById(stock);
            return "股票信息恢复并更新成功";
        }

        stockMapper.insert(stock);
        return "股票信息添加成功";
    }

    @Transactional
    public String addStock(Stock stock) {
        validateStock(stock);
        stock.setStockCode(normalizeStockCode(stock.getStockCode()));

        if (stockMapper.existsByStockCode(stock.getStockCode())) {
            throw new IllegalArgumentException("股票代码已存在：" + stock.getStockCode() + "，请使用更新接口或先删除原有记录");
        }

        Stock deletedStock = stockMapper.selectByStockCodeAny(stock.getStockCode());
        if (deletedStock != null) {
            stock.setId(deletedStock.getId());
            stock.setDeleted(0);
            stockMapper.updateById(stock);
            return "股票信息恢复并添加成功";
        }

        stockMapper.insert(stock);
        return "股票信息添加成功";
    }

    @Transactional
    public String saveBatchStocks(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            throw new IllegalArgumentException("股票列表不能为空");
        }

        for (Stock stock : stocks) {
            validateStock(stock);
            stock.setStockCode(normalizeStockCode(stock.getStockCode()));
        }

        int updatedCount = 0;
        int addedCount = 0;

        for (Stock stock : stocks) {
            Stock existingStock = stockMapper.selectByStockCode(stock.getStockCode());
            if (existingStock != null) {
                stock.setId(existingStock.getId());
                stockMapper.updateById(stock);
                updatedCount++;
            } else {
                stockMapper.insert(stock);
                addedCount++;
            }
        }

        return String.format("批量保存成功，更新 %d 只，新增 %d 只，共 %d 只股票",
                updatedCount, addedCount, updatedCount + addedCount);
    }

    public Stock findByStockCode(String stockCode) {
        return stockMapper.selectByStockCode(stockCode);
    }

    @Transactional
    public String updateStock(String stockCode, Stock stock) {
        validateStock(stock);

        Stock existingStock = stockMapper.selectByStockCode(stockCode);
        if (existingStock == null) {
            throw new IllegalArgumentException("股票不存在：" + stockCode);
        }

        stock.setId(existingStock.getId());
        stock.setStockCode(stockCode);
        stockMapper.updateById(stock);
        return "股票信息更新成功";
    }

    @Transactional
    public String deleteStock(String stockCode) {
        Stock existingStock = stockMapper.selectByStockCode(stockCode);
        if (existingStock == null) {
            throw new IllegalArgumentException("股票不存在：" + stockCode);
        }
        stockMapper.deleteById(existingStock.getId());
        return "股票信息删除成功";
    }

    public boolean exists(String stockCode) {
        return stockMapper.existsByStockCode(stockCode);
    }
}
