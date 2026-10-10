# AI Agent：健康生活指导

这是一个基于 Spring Boot、Spring AI Alibaba 和 DashScope 的健康生活指导后端，由 `yupi-agent` 的示例结构改造而来。它支持多轮对话、SSE 流式输出、结构化健康计划、本地知识库 RAG、领域工具调用和 ReAct 智能体。

> 本项目提供一般性健康生活信息，不用于疾病诊断、药物调整、治疗或急救。

## 环境要求

- JDK 17
- Maven Wrapper（仓库已包含）
- DashScope API Key
- PostgreSQL + pgvector（仅在启用 PGVector 时需要）

在运行配置中设置环境变量：

```text
AI_DASHSCOPE_API_KEY=你的 DashScope API Key
```

可选开关：

```text
# 启用基于本地 Markdown 文档的内存 RAG；启动时会调用嵌入模型
AI_AGENT_RAG_ENABLED=true

# 启用 PGVector Bean
AI_AGENT_PGVECTOR_ENABLED=true
AI_AGENT_DB_URL=jdbc:postgresql://localhost:5432/ai_agent
AI_AGENT_DB_USERNAME=postgres
AI_AGENT_DB_PASSWORD=你的数据库密码
```

## 启动与测试

Windows：

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

账户状态正常时，可显式运行真实 DashScope 集成测试（会产生模型调用）：

```powershell
.\mvnw.cmd "-Dai.integration.enabled=true" "-Dtest=ai_agent.app.AppTest" test
```

服务默认地址为 `http://localhost:8123/api`，接口文档为 `http://localhost:8123/api/swagger-ui.html`。

## 主要接口

所有业务接口以 `/ai/health` 开头：

- `POST /chat`：基础多轮对话
- `POST /chat/stream`：使用请求体的 SSE 流式对话，适合避免将健康描述放进 URL
- `GET /chat/sse`：SSE 流式对话
- `POST /report`：结构化健康生活计划
- `POST /rag`：本地健康知识库问答（需开启 RAG）
- `POST /tools`：允许调用已注册的安全领域工具
- `GET /agent`：ReAct 健康规划智能体流式执行

普通 POST 请求体示例：

```json
{
  "message": "我长期久坐、睡眠不规律，想先做一周改善计划",
  "conversationId": "demo-001"
}
```

`conversationId` 只能包含字母、数字、下划线和短横线，长度为 1–100。

## 目录说明

- `app/App.java`：健康生活助手及对话、报告、RAG、工具入口
- `agent/`：ReAct 智能体执行框架
- `tools/`：经过限制的健康领域工具
- `RAG/`：知识文档加载和向量库配置
- `resources/document/`：健康知识库，内容附权威来源链接
- `chatmemory/`：限制窗口大小、校验会话 ID 的文件会话记忆

## 页面展示
<img width="1828" height="955" alt="image" src="https://github.com/user-attachments/assets/f4890358-ca67-4a19-8938-965ec73cf65b" />

<img width="1813" height="846" alt="image" src="https://github.com/user-attachments/assets/86bd0b88-0a9a-4704-8cc3-e2a96b958a79" />



## 安全设计

- 系统提示词禁止疾病诊断、药物调整、极端饮食和危险训练。
- 检测到可能的急症描述时，要求立即升级至当地急救服务。
- 未注册任意终端执行、任意文件读写或任意 URL 下载工具。
- PGVector 和 RAG 默认关闭，避免本地启动依赖外部数据库或在启动时产生模型调用。
