-- ============================================
-- 股票表创建脚本 (SQLite)
-- ============================================

CREATE TABLE IF NOT EXISTS stocks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_type VARCHAR(20) NOT NULL,
    stock_code VARCHAR(20) NOT NULL UNIQUE,
    holding_quantity INTEGER DEFAULT 0,
    holding_price DECIMAL(10, 2) DEFAULT 0.00,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_stock_code ON stocks(stock_code);
CREATE INDEX IF NOT EXISTS idx_stock_type ON stocks(stock_type);
CREATE INDEX IF NOT EXISTS idx_deleted ON stocks(deleted);
CREATE INDEX IF NOT EXISTS idx_create_time ON stocks(create_time);
