package com.stock.mcp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mcp.model.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    @Select("SELECT * FROM stocks WHERE stock_code = #{stockCode} AND deleted = 0")
    Stock selectByStockCode(@Param("stockCode") String stockCode);

    @Select("SELECT * FROM stocks WHERE stock_type = #{stockType} AND deleted = 0")
    List<Stock> selectByStockType(@Param("stockType") String stockType);

    @Select("SELECT * FROM stocks WHERE holding_quantity > #{minQuantity} AND deleted = 0")
    List<Stock> selectStocksWithMinQuantity(@Param("minQuantity") Long minQuantity);

    @Select("SELECT COUNT(*) > 0 FROM stocks WHERE stock_code = #{stockCode} AND deleted = 0")
    boolean existsByStockCode(@Param("stockCode") String stockCode);

    @Select("SELECT * FROM stocks WHERE stock_code = #{stockCode}")
    Stock selectByStockCodeAny(@Param("stockCode") String stockCode);
}
