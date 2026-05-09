# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Boot MCP (Model Context Protocol) server for stock data. Exposes tools for Hermes (MCP client) to query stock prices, search symbols, and retrieve market data.

## Tech Stack

- Java 17
- Spring Boot 3.4.4
- Spring AI 1.0.0 (MCP Server WebMVC starter — SSE transport via Tomcat)
- Maven

## Build & Run

```bash
# Build
mvn package

# Run
mvn spring-boot:run

# Run a single test
mvn test -Dtest=<TestClass>
```

## Architecture

```
com.stock.mcp
├── StockMcpApplication.java    -- @SpringBootApplication entry point
├── config/
│   └── McpServerConfig.java    -- MCP server configuration beans
└── tool/
    └── StockDataTool.java      -- @Component with @Bean methods returning FunctionToolCallback
```

### MCP Tool Model

Tools are defined as Spring `@Bean` methods that return `FunctionToolCallback<I, O>`.
Each callback is built with a tool name, a lambda/function, a description, and input type.
The MCP server auto-configuration discovers all `ToolCallback` beans and registers them
as MCP tools.

```java
@Bean
public FunctionToolCallback<String, Map<String, Object>> myTool() {
    return FunctionToolCallback
            .builder("toolName", (String input) -> { ... })
            .description("What this tool does")
            .inputType(String.class)
            .build();
}
```

SSE transport endpoints are auto-configured by `spring-ai-starter-mcp-server-webmvc`.
No manual controller setup is needed.

### Adding a New Tool

1. Add a `@Bean` method to `StockDataTool` (or a new `@Component` in `tool/`).
2. Use `FunctionToolCallback.builder("name", lambda)` to construct the callback.
3. Set `.description(...)` and `.inputType(...)`.
4. Build and return.

### Configuration

`src/main/resources/application.yml`:
- `spring.ai.mcp.server.name` — server name reported to MCP clients
- `server.port` — HTTP port (default 8080)
- All MCP properties support `${ENV_VAR:default}` overrides

### Dependencies

Spring AI artifacts are in the Spring Milestones repository, configured in `pom.xml`.
The `spring-ai-bom` manages all Spring AI dependency versions.
