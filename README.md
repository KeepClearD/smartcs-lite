<p align="center">
  <h1 align="center">SmartCS-Lite</h1>
  <p align="center">
    <strong>AI 驱动的开源智能客服系统 · 轻量 MVP 版</strong>
  </p>
  <p align="center">
    基于 Spring Boot + Spring AI + PostgreSQL + PGvector 构建<br/>
  </p>
</p>
<p align="center">
  <img src="https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?style=flat-square&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20AI-1.0-6DB33F?style=flat-square&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-17-336791?style=flat-square&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/PGvector-0.8-336791?style=flat-square&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square" />
</p>

---

## 目录

- [项目简介](#项目简介)
- [核心特性](#核心特性)
- [系统架构](#系统架构)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
  - [环境要求](#环境要求)
  - [克隆项目](#克隆项目)
  - [启动基础设施](#启动基础设施)
  - [配置应用](#配置应用)
  - [编译运行](#编译运行)
  - [验证服务](#验证服务)
- [功能使用指南](#功能使用指南)
  - [聊天 Widget](#聊天-widget)
  - [管理后台](#管理后台)
  - [WebSocket 接口](#websocket-接口)
  - [REST API](#rest-api)
- [知识库配置](#知识库配置)
- [AI 对话引擎](#ai-对话引擎)
- [数据库设计](#数据库设计)
- [部署指南](#部署指南)
- [开发计划](#开发计划)
- [演进路线](#演进路线)
- [常见问题](#常见问题)
- [参与贡献](#参与贡献)
- [开源协议](#开源协议)

---

## 项目简介

SmartCS-Lite 是一个 **AI-First、Human-in-the-Loop** 的智能客服系统轻量版（MVP），旨在用最短的时间和最低的复杂度交付一个**可上线运行**的智能客服方案。

**设计原则：**

- **能跑 > 完整性** — 先跑通核心链路，再逐步完善
- **简单优先** — 单体应用，去掉一切非必要组件
- **可演进** — 架构保留清晰的扩展点，后续可平滑升级到微服务

### 适用场景

| 场景 | 说明 |
|------|------|
| 创业团队快速验证 | 4 周内上线 AI 客服，验证商业模式 |
| 企业内部客服 | 替代传统 FAQ 页面，提供智能问答 |
| 开源学习 | 完整的 AI + RAG + WebSocket 项目参考 |
| 产品原型 | 快速搭建 Demo，展示给客户或投资人 |

---

## 核心特性

### AI 能力

- **RAG 知识问答** — 基于 PGvector 向量检索 + LLM 生成，回答有据可依
- **文档自动处理** — 上传 PDF/Word/TXT/MD，自动解析、分块、向量化
- **FAQ 精准匹配** — 高频问题配置标准问答对，命中即答
- **对话记忆** — 跨轮次上下文记忆，支持连续追问
- **LLM 缓存** — 相同问题缓存命中，降低成本
- **流式输出** — SSE 逐字回复，体验流畅

### 客服能力

- **机器人自动接待** — 7x24 小时 AI 自动回复
- **无缝转人工** — 机器人无法解答时自动/手动转接座席
- **座席工作台** — 实时聊天、会话管理、客户信息查看
- **会话生命周期** — 创建 → 机器人接待 → 排队 → 人工接待 → 关闭
- **满意度评价** — 会话结束后客户评分

### 管理能力

- **知识库管理** — FAQ 增删改查、文档上传管理
- **座席管理** — 添加座席、上下线状态切换
- **数据看板** — 今日会话数、消息量、在线座席、机器人解决率
- **多租户（轻量）** — 按租户隔离数据，支持多企业使用

### 技术亮点

- **Java 21 虚拟线程** — WebSocket 高并发场景天然适配
- **Spring AI 统一抽象** — 一套代码兼容 OpenAI / 通义千问 / Ollama 等
- **PGvector 内嵌** — 向量数据与业务数据同库，零外部向量库依赖
- **RustFS / MinIO 对象存储** — S3 兼容，文档文件统一存储
- **Docker Compose 一键部署** — 一条命令启动全部服务

---

## 系统架构
```text
┌─────────────────────────────────────────────────────┐
│                   客户端                               │
│  ┌───────────────────────────────────────────────┐  │
│  │  Web Chat Widget (React / 纯 JS)              │  │
│  └──────────────────────┬────────────────────────┘  │
└─────────────────────────┼───────────────────────────┘
                          │
                    WebSocket + REST
                          │
┌─────────────────────────▼───────────────────────────┐
│            SmartCS-Lite 单体应用                      │
│            (Spring Boot 3.4+ / Java 21)              │
│                                                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ WebSocket │  │ REST API │  │ Admin API        │  │
│  │ Handler   │  │ (会话/   │  │ (知识库/座席/     │  │
│  │ (IM通信)  │  │  消息)   │  │  配置管理)       │  │
│  └─────┬────┘  └────┬─────┘  └────────┬─────────┘  │
│        │            │                  │             │
│  ┌─────▼────────────▼──────────────────▼─────────┐  │
│  │              Service Layer (业务逻辑)           │  │
│  │                                                │  │
│  │  ┌──────────────┐  ┌────────────────────────┐ │  │
│  │  │ MessageService│  │ ConversationService   │ │  │
│  │  └──────────────┘  └────────────────────────┘ │  │
│  │  ┌──────────────┐  ┌────────────────────────┐ │  │
│  │  │ AiService    │  │ KnowledgeService      │ │  │
│  │  │ (RAG + LLM)  │  │ (知识库管理)           │ │  │
│  │  └──────────────┘  └────────────────────────┘ │  │
│  │  ┌──────────────┐  ┌────────────────────────┐ │  │
│  │  │ AgentService │  │ TenantService         │ │  │
│  │  │ (座席管理)    │  │ (租户管理)             │ │  │
│  │  └──────────────┘  └────────────────────────┘ │  │
│  └────────────────────────────────────────────────┘  │
│                         │                             │
│  ┌──────────────────────▼──────────────────────────┐ │
│  │              Data Layer                          │ │
│  │                                                  │ │
│  │  PostgreSQL 17 + PGvector  │  Redis 7.x         │ │
│  │  (业务数据 + 向量存储)       │  (缓存/在线状态)    │ │
│  └──────────────────────────────────────────────────┘ │
│                         │                             │
│  ┌──────────────────────▼──────────────────────────┐ │
│  │  RustFS (S3兼容) — 文档/文件存储                 │ │
│  └──────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
```

**核心链路：**


用户发消息 → WebSocket → MessageService → AiService → RAG检索 → LLM生成 → 返回用户



---
## 技术栈
```
| 模块 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **后端框架** | Spring Boot | 3.4+ | 单体应用 |
| **Java** | OpenJDK | 21 | 虚拟线程支持 |
| **AI 框架** | Spring AI | 1.0+ | 统一 ChatModel + VectorStore |
| **数据库** | PostgreSQL + PGvector | 17 | 业务数据 + 向量存储一体化 |
| **缓存** | Redis | 7.x | LLM 缓存 + 在线状态 + 限流 |
| **对象存储** | RustFS / MinIO | — | S3 兼容，文档文件存储 |
| **文档解析** | Apache Tika | 2.9+ | PDF/Word/TXT/MD 统一解析 |
| **实时通信** | Spring WebSocket | — | 用户与座席实时消息 |
| **前端** | React 18 + TypeScript | — | 管理后台 + 座席工作台 |
| **UI 组件** | Ant Design | 5.x | 企业级 UI 库 |
| **聊天组件** | 原生 HTML/JS | — | 可嵌入任意网页的 Web Component |
| **容器化** | Docker + Docker Compose | — | 一键部署 |
```
---
## 项目结构
```text
smartcs-lite/
├── pom.xml # Maven 父 POM
├── .env.example # 环境变量示例
├── .gitignore
├── docker-compose.yml # Docker 一键部署
├── schema.sql # 数据库初始化脚本
├── README.md
│
├── src/main/java/com/smartcs/lite/
│ ├── SmartCsLiteApplication.java # 启动类
│ │
│ ├── common/ # 通用模块
│ │ ├── Result.java # 统一响应
│ │ ├── ErrorCode.java # 错误码枚举
│ │ ├── BusinessException.java # 业务异常
│ │ └── GlobalExceptionHandler.java # 全局异常处理
│ │
│ ├── config/ # 配置类
│ │ ├── WebConfig.java # CORS + 拦截器
│ │ ├── WebSocketConfig.java # WebSocket 端点
│ │ ├── AiConfig.java # Spring AI ChatClient
│ │ ├── RedisConfig.java # Redis
│ │ ├── AsyncConfig.java # 异步线程池
│ │ └── StorageConfig.java # RustFS/MinIO
│ │
│ ├── interceptor/ # 拦截器
│ │ ├── TenantContext.java # 租户上下文 (ThreadLocal)
│ │ ├── TenantInterceptor.java # 租户识别
│ │ └── RateLimitInterceptor.java # 接口限流
│ │
│ ├── model/
│ │ ├── enums/ # 枚举定义
│ │ ├── entity/ # JPA 实体
│ │ └── dto/ # 数据传输对象
│ │
│ ├── repository/ # Spring Data JPA Repository
│ │
│ ├── service/ # 业务逻辑层
│ │ ├── AiService.java # AI 对话核心
│ │ ├── RagService.java # RAG 向量检索
│ │ ├── CacheService.java # LLM 缓存
│ │ ├── ChatMemoryService.java # 对话记忆
│ │ ├── ConversationService.java # 会话管理
│ │ ├── MessageService.java # 消息管理
│ │ ├── KnowledgeService.java # 知识库管理
│ │ ├── DocumentProcessService.java # 文档处理流水线
│ │ ├── AgentService.java # 座席管理
│ │ ├── AnalyticsService.java # 数据统计
│ │ └── StorageService.java # 对象存储
│ │
│ ├── websocket/ # WebSocket 处理
│ │ ├── SessionManager.java # 连接管理
│ │ ├── WsMessageWrapper.java # 消息包装
│ │ ├── ChatWebSocketHandler.java # 客户端聊天
│ │ └── AgentWebSocketHandler.java # 座席端
│ │
│ └── controller/ # REST API
│ ├── ConversationController.java
│ ├── KnowledgeController.java
│ ├── AgentController.java
│ ├── AnalyticsController.java
│ └── FileController.java
│
├── src/main/resources/
│ └── application.yml # 应用配置
│
└── frontend/
├── admin-app/ # 管理后台 (React + Ant Design)
│ └── src/
│ ├── App.tsx
│ ├── layouts/MainLayout.tsx
│ ├── pages/
│ │ ├── Dashboard.tsx # 仪表盘
│ │ ├── Conversations.tsx # 会话管理
│ │ ├── KnowledgeBase.tsx # 知识库管理
│ │ └── Agents.tsx # 座席管理
│ └── utils/api.ts
│
└── chat-widget/ # 嵌入式聊天组件
└── index.html
```
---

## 快速开始

### 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 21+ | 推荐 Eclipse Temurin |
| Maven | 3.9+ | 或使用项目自带的 mvnw |
| Docker | 24+ | 运行基础设施 |
| Docker Compose | 2.20+ | Docker Desktop 自带 |
| Node.js | 18+ | 前端构建（可选） |

```bash
# 验证环境
java -version      # openjdk version "21.x"
mvn -version       # Apache Maven 3.9.x
docker -version    # Docker version 24.x
node -version      # v18.x / v20.x
```

### 克隆项目
```bash
git clone https://github.com/smartcs/smartcs-lite.git
cd smartcs-lite
```

### 启动基础设施
```bash
# 启动 PostgreSQL (含 PGvector) + Redis + MinIO
docker compose up -d

# 查看服务状态，确认全部 healthy
docker compose ps
```

预期输出：
```text
NAME                STATUS          PORTS
smartcs-postgres    Up (healthy)    0.0.0.0:5432->5432/tcp
smartcs-redis       Up              0.0.0.0:6379->6379/tcp
smartcs-minio       Up              0.0.0.0:9000->9000/tcp, 0.0.0.0:9001->9001/tcp
```

> 数据库会自动执行 `schema.sql` 完成建表和初始化数据。

### 配置应用
```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env，填入你的 AI 模型 API Key
```

`.env` 关键配置：
```env
# ====== 必须配置 ======
OPENAI_API_KEY=sk-your-api-key-here

# ====== 可选配置（使用其他模型时修改）======
OPENAI_BASE_URL=https://api.openai.com
OPENAI_CHAT_MODEL=gpt-4o-mini
OPENAI_EMBEDDING_MODEL=text-embedding-3-small

# ====== 本地模型（可选）======
# OPENAI_BASE_URL=http://localhost:11434/v1
# OPENAI_CHAT_MODEL=qwen2.5:7b
# OPENAI_EMBEDDING_MODEL=nomic-embed-text
```

**支持的模型提供商：**

| 提供商 | OPENAI_BASE_URL | 说明  |
| --- | --- | --- |
| OpenAI | `https://api.openai.com` | 官方 API |
| 通义千问 | `https://dashscope.aliyuncs.com/compatible-mode/v1` | 阿里云 |
| DeepSeek | `https://api.deepseek.com/v1` | DeepSeek |
| Ollama | `http://localhost:11434/v1` | 本地部署，零成本 |
| 其他兼容 | —   | 任何 OpenAI 兼容接口 |

### 编译运行
```bash
# 编译打包
mvn clean package -DskipTests

# 启动应用
java -jar target/smartcs-lite-0.1.0-SNAPSHOT.jar

# 或者开发模式（热重载）
mvn spring-boot:run
```

### 验证服务

```bash

# 1. 健康检查
curl http://localhost:8080/actuator/health
# 预期: {"status":"UP"}

# 2. 创建知识库
curl -X POST "http://localhost:8080/api/v1/knowledge-bases?name=产品FAQ&description=常见问题"

# 3. 添加 FAQ
curl -X POST http://localhost:8080/api/v1/knowledge-bases/1/faqs \
  -H "Content-Type: application/json" \
  -d '{
    "question": "如何退款？",
    "answer": "请在订单详情页点击退款按钮，填写退款原因后提交，一般 3-5 个工作日内到账。",
    "category": "退款"
  }'

# 4. 查看统计
curl http://localhost:8080/api/v1/analytics/overview
```

---

## 功能使用指南

### 聊天 Widget

最简单的体验方式，直接在浏览器中打开聊天测试页面：

```bash
# 用浏览器打开
open frontend/chat-widget/index.html
# 或
start frontend/chat-widget/index.html
```

点击右下角聊天按钮，即可开始与 AI 对话。

**嵌入到你的网站：**

将 `index.html` 中的聊天组件代码复制到任意网页中，修改 `SERVER` 地址即可：


```html
<script>
  const SERVER = 'ws://your-domain.com/ws/chat';
  // ... 其余代码
</script>
```

### 管理后台

```bash
cd frontend/admin-app

# 安装依赖
npm install

# 启动开发服务
npm run dev
```

访问 `http://localhost:5173`，包含以下页面：

| 页面  | 功能  |
| --- | --- |
| **仪表盘** | 今日会话数、活跃会话、在线座席、机器人解决率 |
| **会话管理** | 会话列表、筛选状态、接手会话、关闭会话 |
| **知识库** | 创建知识库、管理 FAQ、上传文档 |
| **座席管理** | 添加座席、上下线切换 |

### WebSocket 接口

**客户端聊天连接：**

```text
ws://localhost:8080/ws/chat?customerId={客户ID}&tenantId={租户ID}
```

**座席工作台连接：**

```text
ws://localhost:8080/ws/agent?agentId={座席ID}
```

**消息协议：**

```jsonc
// 发送消息
{
  "type": "chat",           // 消息类型: chat | transfer | close | satisfaction
  "content": "你好",         // 消息内容
  "conversationId": 123      // 会话 ID（可选）
}

// 接收消息
{
  "type": "chat",           // chat | typing | status | error | connected
  "content": "您好！有什么可以帮您的吗？",
  "conversationId": 123
}
```

**消息类型说明：**

| type (客户端→服务端) | 说明  |
| --- | --- |
| `chat` | 发送聊天消息 |
| `transfer` | 请求转人工 |
| `close` | 关闭会话 |
| `satisfaction` | 提交满意度评分 (metadata.score: 1-5) |

| type (服务端→客户端) | 说明  |
| --- | --- |
| `connected` | 连接建立成功 |
| `chat` | 收到聊天消息（AI/座席/System） |
| `typing` | 对方正在输入 |
| `status` | 会话状态变更 |
| `error` | 错误通知 |
| `transfer_request` | 新的转人工请求（座席端） |

**测试 WebSocket：**


```bash
# 安装 wscat
npm install -g wscat

# 连接
wscat -c "ws://localhost:8080/ws/chat?customerId=test-001&tenantId=1"

# 发送消息
> {"type":"chat","content":"你好，我想退款"}

# 请求转人工
> {"type":"transfer","content":""}
```

### REST API

**基础路径：**`/api/v1`

**通用请求头：**

| Header | 说明  | 示例  |
| --- | --- | --- |
| `X-Tenant-Id` | 租户 ID | `1` |
| `Content-Type` | 内容类型 | `application/json` |

**会话管理：**

```text
GET    /api/v1/conversations?status=BOT&page=0&size=20   # 会话列表
GET    /api/v1/conversations/{id}                         # 会话详情
GET    /api/v1/conversations/{id}/messages?page=0&size=50 # 历史消息
PUT    /api/v1/conversations/{id}/assign?agentId=1        # 座席接手
PUT    /api/v1/conversations/{id}/close                   # 关闭会话
```

**知识库管理：**

```text
GET    /api/v1/knowledge-bases                             # 知识库列表
POST   /api/v1/knowledge-bases?name=xxx&description=xxx    # 创建知识库
GET    /api/v1/knowledge-bases/{id}/faqs                   # FAQ 列表
POST   /api/v1/knowledge-bases/{id}/faqs                   # 添加 FAQ
DELETE /api/v1/knowledge-bases/{kbId}/faqs/{faqId}         # 删除 FAQ
GET    /api/v1/knowledge-bases/{id}/documents              # 文档列表
POST   /api/v1/knowledge-bases/{id}/documents              # 上传文档 (multipart)
```

**座席管理：**

```text
GET    /api/v1/agents                                      # 座席列表
POST   /api/v1/agents                                      # 添加座席
PUT    /api/v1/agents/{id}/status?status=ONLINE            # 更新状态
```

**数据分析：**

```text
GET    /api/v1/analytics/overview                          # 概览统计
```

**文件上传：**


```text
POST   /api/v1/files/upload                                # 通用文件上传 (multipart)
```

**统一响应格式：**

```json
{
  "code": 0,
  "message": "success",
  "data": { 
    
  }
}
```

**错误响应：**

```json
{
  "code": 50001,
  "message": "AI 服务异常",
  "data": null
}
```

---

## 知识库配置

### FAQ 知识库

FAQ 适合**高频标准问题**，命中后直接返回答案，无需调用 LLM，响应快且成本为零。

```bash
# 创建知识库
curl -X POST "http://localhost:8080/api/v1/knowledge-bases?name=售后FAQ"

# 批量添加 FAQ
curl -X POST http://localhost:8080/api/v1/knowledge-bases/1/faqs \
  -H "Content-Type: application/json" \
  -d '{"question":"发货时间","answer":"下单后 48 小时内发货，偏远地区 3-5 天。","category":"物流"}'

curl -X POST http://localhost:8080/api/v1/knowledge-bases/1/faqs \
  -H "Content-Type: application/json" \
  -d '{"question":"如何开发票","answer":"请在订单详情页点击申请开票，支持电子发票和纸质发票。","category":"发票"}'
```

添加 FAQ 后，系统会自动将「问题 + 答案」进行向量化并存储到 PGvector，后续用户提问时通过语义匹配召回。

### 文档知识库

文档适合**长文本知识**（产品手册、政策文档等），系统会自动完成以下处理：

```text
上传文档 → 存储到 RustFS/MinIO → Apache Tika 解析 → 文本分块 → Embedding 向量化 → 存入 PGvector
```

```bash
# 上传文档
curl -X POST http://localhost:8080/api/v1/knowledge-bases/1/documents \
  -F "file=@./产品手册.pdf" \
  -F "title=产品使用手册"

# 查看文档处理状态
curl http://localhost:8080/api/v1/knowledge-bases/1/documents
# status: PENDING → PROCESSING → READY (或 FAILED)
```

支持的文件格式：PDF、DOCX、TXT、Markdown、HTML。

---

## AI 对话引擎

### RAG 流程

```text
用户提问
    ↓
Embedding 向量化 (Spring AI EmbeddingModel)
    ↓
PGvector 向量检索 (topK=3, cosine similarity)
    ↓
拼接知识上下文到 System Prompt
    ↓
ChatModel 生成回答 (Spring AI ChatClient)
    ↓
保存对话记忆 + 缓存结果
    ↓
返回用户
```

### 切换模型

只需修改 `.env` 即可切换不同的 AI 模型，**无需改代码**（得益于 Spring AI 的统一抽象）：

```env
# 使用 OpenAI
OPENAI_BASE_URL=https://api.openai.com
OPENAI_CHAT_MODEL=gpt-4o-mini
OPENAI_EMBEDDING_MODEL=text-embedding-3-small

# 使用通义千问
OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
OPENAI_CHAT_MODEL=qwen-plus
OPENAI_EMBEDDING_MODEL=text-embedding-v3

# 使用本地 Ollama
OPENAI_BASE_URL=http://localhost:11434/v1
OPENAI_CHAT_MODEL=qwen2.5:7b
OPENAI_EMBEDDING_MODEL=nomic-embed-text
```

### 转人工机制

AI 在以下情况会触发转人工：

1. **用户主动要求** — 用户说"转人工"、"找客服"等，AI 回复中包含 `[TRANSFER]` 标记
2. **知识库无匹配** — 向量检索结果为空或相似度过低时，AI 建议转人工
3. **前端手动触发** — 用户点击"转人工"按钮

---

## 数据库设计

### 核心表

| 表名  | 说明  | 关键字段 |
| --- | --- | --- |
| `tenant` | 租户  | id, name, config |
| `agent` | 座席  | id, tenant_id, name, status, role |
| `conversation` | 会话  | id, tenant_id, customer_id, agent_id, status |
| `message` | 消息  | id, conversation_id, sender_type, content |
| `knowledge_base` | 知识库 | id, tenant_id, name |
| `faq` | FAQ | id, kb_id, question, answer |
| `document` | 文档  | id, kb_id, file_key, status |
| `document_chunk` | 知识分块 | id, doc_id, content, embedding(vector) |
| `chat_memory` | 对话记忆 | id, conversation_id, role, content |

### 向量索引

```sql
-- PGvector HNSW 索引（余弦相似度）
CREATE INDEX idx_chunk_embedding ON document_chunk
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);
```

### 会话状态流转

```text
BOT (机器人接待)
  → PENDING (排队等待人工)  → AGENT (人工接待中)  → CLOSED (已关闭)
  → CLOSED (机器人直接关闭)
```

---

## 部署指南

### Docker Compose 一键部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/smartcs/smartcs-lite.git
cd smartcs-lite

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env，填入 OPENAI_API_KEY

# 3. 启动基础设施
docker compose up -d

# 4. 编译并启动应用
mvn clean package -DskipTests
java -jar target/smartcs-lite-0.1.0-SNAPSHOT.jar
```

### 生产环境部署

```bash
# 构建应用镜像
docker build -t smartcs-lite:latest .

# 使用生产配置启动
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### 最小服务器配置

| 资源  | 配置  | 预估成本 |
| --- | --- | --- |
| CPU | 2 核 | —   |
| 内存  | 4 GB | —   |
| 磁盘  | 40 GB SSD | —   |
| 操作系统 | Ubuntu 22.04 / Debian 12 | —   |
| 云服务器 | —   | 约 ¥50~100/月 |

### 访问地址

| 服务  | 地址  | 说明  |
| --- | --- | --- |
| 后端 API | `http://localhost:8080` | REST API + WebSocket |
| 管理后台 | `http://localhost:5173` | React 前端 |
| MinIO 控制台 | `http://localhost:9001` | 对象存储管理 |
| 健康检查 | `http://localhost:8080/actuator/health` | 服务状态 |

---

## 开发计划

### Phase 1 — MVP（当前，4 周）

- [x] 项目脚手架搭建
- [x] 数据库设计与初始化
- [x] WebSocket 实时通信
- [x] Spring AI 集成（ChatModel + PGvector VectorStore）
- [x] RAG 知识问答
- [x] FAQ 管理
- [x] 文档上传与自动处理
- [x] 机器人 → 人工转接
- [x] 座席工作台
- [x] 管理后台（仪表盘/会话/知识库/座席）
- [x] 聊天嵌入组件
- [x] LLM 缓存
- [x] 接口限流
- [x] Docker Compose 部署

### Phase 2 — 核心增强

- [ ] 完整用户体系（注册/登录/SSO）
- [ ] 工单系统
- [ ] 多渠道接入（企微/微信）
- [ ] 多租户 RLS 行级安全
- [ ] AI 辅助座席（建议回复/知识推荐）
- [ ] 会话满意度统计
- [ ] 多模型路由

### Phase 3 — 企业级

- [ ] 微服务拆分
- [ ] Kafka 消息队列
- [ ] 工作流可视化编排
- [ ] 质检系统
- [ ] K8s Helm Charts
- [ ] 复杂权限（RBAC）
- [ ] 数据分析看板增强

### Phase 4 — 生态

- [ ] 电话/语音渠道
- [ ] 知识图谱
- [ ] 插件市场
- [ ] 多语言支持
- [ ] 开源社区建设

---

## 演进路线

SmartCS-Lite 的设计目标是**可平滑演进**，当前的简化方案在后续升级时改动最小：

| 升级项 | 当前方案 | 升级方案 | 改动范围 |
| --- | --- | --- | --- |
| 消息队列 | 无 (进程内调用) | Kafka | 新增 EventListener，核心逻辑不变 |
| 服务拆分 | 单体应用 | 微服务 | Service 类独立为应用，业务逻辑不变 |
| 模型切换 | 固定一个模型 | 多模型路由 | 修改配置，零代码改动 (Spring AI) |
| 向量库 | PGvector 内嵌 | Milvus | 替换 VectorStore Bean |
| 渠道扩展 | 仅 Web | 多渠道 | 新增 ChannelAdapter，核心逻辑复用 |
| 权限  | 简单角色 | RBAC | 新增权限模块 |
| 部署  | Docker Compose | K8s | 新增 Helm Charts |

---

## 常见问题

### Q: 向量检索返回空结果？

**A:** 检查以下几点：

1. 1.知识库中是否有已添加的 FAQ 或已处理完成的文档（status = READY）
2. 2.`OPENAI_EMBEDDING_MODEL` 配置是否正确
3. 3.向量维度是否与模型匹配（默认 1536）
4. 4.PGvector 扩展是否安装成功：`SELECT * FROM pg_extension WHERE extname = 'vector';`

### Q: AI 回复很慢？

**A:** 可能的原因和优化方案：

1. 1.**使用远端 API** — 网络延迟导致，可切换到本地 Ollama 模型
2. 2.**开启缓存** — 已默认开启，相同问题会直接返回缓存结果
3. 3.**流式输出** — 使用 SSE 接口，逐字返回，体感更快
4. 4.**减少 topK** — 修改 `smartcs.ai.rag.top-k` 从 3 降到 2

### Q: 文档上传后状态一直是 PENDING？

**A:** 文档处理是异步的，检查应用日志中是否有错误信息。常见原因：

1. 1.RustFS/MinIO 未启动
2. 2.文件格式不支持
3. 3.Embedding 模型调用失败（API Key 错误或额度用尽）

### Q: 如何使用本地模型（完全离线）？

**A:** 安装 Ollama，然后修改 `.env`：

```env
OPENAI_BASE_URL=http://localhost:11434/v1
OPENAI_CHAT_MODEL=qwen2.5:7b
OPENAI_EMBEDDING_MODEL=nomic-embed-text
```

### Q: 如何支持多租户？

**A:** 当前版本通过请求头 `X-Tenant-Id` 区分租户，数据层面通过 `tenant_id` 字段过滤。后续版本将引入 PostgreSQL RLS 行级安全策略。

---

## 参与贡献

我们欢迎任何形式的贡献！

### 贡献方式

- **提交 Issue** — 报告 Bug、提出功能建议
- **提交 PR** — 修复 Bug、实现新功能、完善文档
- **完善文档** — 补充使用指南、翻译文档
- **分享使用案例** — 告诉我们你在用 SmartCS-Lite 做什么

### 开发流程

```bash
# 1. Fork 本仓库

# 2. 创建功能分支
git checkout -b feature/my-feature

# 3. 提交代码
git commit -m "feat: add my feature"

# 4. 推送到远程
git push origin feature/my-feature

# 5. 创建 Pull Request
```

### 代码规范

- Java 代码遵循 Google Java Style
- 使用 Lombok 减少样板代码
- 新增功能需附带单元测试
- Commit Message 遵循 Conventional Commits

---

## 开源协议

本项目基于 Apache License 2.0 开源。

## 联系我们

- GitHub Issues: 提交问题或建议
- 讨论区: GitHub Discussions

---

如果这个项目对你有帮助，请给我们一个 Star！  
**Star 是对开源项目最大的鼓励**