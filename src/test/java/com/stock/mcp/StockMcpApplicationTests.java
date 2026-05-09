package com.stock.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.sql.init.mode=never")
class StockMcpApplicationTests {

    @Test
    void contextLoads() {
    }
}
