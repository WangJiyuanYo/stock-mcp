package com.stock.mcp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.stock.mcp.mapper")
public class StockMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockMcpApplication.class, args);
    }
}
