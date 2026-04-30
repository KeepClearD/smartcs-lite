-- ==================== 扩展安装 ====================
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 尝试安装 zhparser（如果可用）
DO $$ BEGIN
    CREATE EXTENSION IF NOT EXISTS zhparser;
    CREATE TEXT SEARCH CONFIGURATION zhcfg (PARSER = zhparser);
    ALTER TEXT SEARCH CONFIGURATION zhcfg ADD MAPPING FOR n,v,a,i,e,l WITH simple;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'zhparser not available, using simple config';
END $$;

-- ==================== 租户表 ====================
CREATE TABLE IF NOT EXISTS tenant (
                                      id              BIGSERIAL PRIMARY KEY,
                                      name            VARCHAR(128) NOT NULL,
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    config          JSONB        DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

-- ==================== 座席表 ====================
CREATE TABLE IF NOT EXISTS agent (
                                     id              BIGSERIAL PRIMARY KEY,
                                     tenant_id       BIGINT       NOT NULL REFERENCES tenant(id),
    name            VARCHAR(64)  NOT NULL,
    email           VARCHAR(128),
    password_hash   VARCHAR(256),
    role            VARCHAR(16)  NOT NULL DEFAULT 'AGENT',
    status          VARCHAR(16)  NOT NULL DEFAULT 'OFFLINE',
    max_concurrent  INT          NOT NULL DEFAULT 5,
    current_load    INT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_agent_tenant ON agent(tenant_id);

-- ==================== 会话表 ====================
CREATE TABLE IF NOT EXISTS conversation (
                                            id              BIGSERIAL PRIMARY KEY,
                                            tenant_id       BIGINT       NOT NULL,
                                            customer_name   VARCHAR(64),
    customer_id     VARCHAR(128),
    channel         VARCHAR(16)  NOT NULL DEFAULT 'WEB',
    agent_id        BIGINT,
    status          VARCHAR(16)  NOT NULL DEFAULT 'BOT',
    subject         VARCHAR(256),
    satisfaction    SMALLINT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ
    );
CREATE INDEX IF NOT EXISTS idx_conv_tenant_status ON conversation(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_conv_customer ON conversation(customer_id);
CREATE INDEX IF NOT EXISTS idx_conv_agent ON conversation(agent_id) WHERE agent_id IS NOT NULL;

-- ==================== 消息表 ====================
CREATE TABLE IF NOT EXISTS message (
                                       id              BIGSERIAL PRIMARY KEY,
                                       conversation_id BIGINT       NOT NULL REFERENCES conversation(id),
    sender_type     VARCHAR(16)  NOT NULL,
    sender_id       BIGINT,
    msg_type        VARCHAR(16)  NOT NULL DEFAULT 'TEXT',
    content         TEXT         NOT NULL,
    metadata        JSONB        DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_msg_conv ON message(conversation_id, created_at DESC);

-- ==================== 知识库表 ====================
CREATE TABLE IF NOT EXISTS knowledge_base (
                                              id              BIGSERIAL PRIMARY KEY,
                                              tenant_id       BIGINT       NOT NULL,
                                              name            VARCHAR(128) NOT NULL,
    description     VARCHAR(512),
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

-- ==================== FAQ 表 ====================
CREATE TABLE IF NOT EXISTS faq (
                                   id              BIGSERIAL PRIMARY KEY,
                                   kb_id           BIGINT       NOT NULL REFERENCES knowledge_base(id),
    tenant_id       BIGINT       NOT NULL,
    question        TEXT         NOT NULL,
    answer          TEXT         NOT NULL,
    category        VARCHAR(64),
    hit_count       INT          NOT NULL DEFAULT 0,
    status          VARCHAR(16)  NOT NULL DEFAULT 'PUBLISHED',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

-- ==================== 文档表 ====================
CREATE TABLE IF NOT EXISTS document (
                                        id              BIGSERIAL PRIMARY KEY,
                                        kb_id           BIGINT       NOT NULL REFERENCES knowledge_base(id),
    tenant_id       BIGINT       NOT NULL,
    title           VARCHAR(256) NOT NULL,
    file_key        VARCHAR(512),
    file_type       VARCHAR(16),
    file_size       BIGINT,
    status          VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    chunk_count     INT          DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

-- ==================== 知识分块表（含向量）====================
CREATE TABLE IF NOT EXISTS document_chunk (
                                              id              BIGSERIAL PRIMARY KEY,
                                              doc_id          BIGINT       REFERENCES document(id),
    kb_id           BIGINT       NOT NULL,
    tenant_id       BIGINT       NOT NULL,
    content         TEXT         NOT NULL,
    metadata        JSONB        DEFAULT '{}',
    embedding       vector(1536),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

-- 向量索引
CREATE INDEX IF NOT EXISTS idx_chunk_embedding ON document_chunk
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- 租户+知识库过滤索引
CREATE INDEX IF NOT EXISTS idx_chunk_tenant_kb ON document_chunk(tenant_id, kb_id);

-- ==================== 对话记忆表 ====================
CREATE TABLE IF NOT EXISTS chat_memory (
                                           id              BIGSERIAL PRIMARY KEY,
                                           conversation_id BIGINT       NOT NULL,
                                           role            VARCHAR(16)  NOT NULL,
    content         TEXT         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_memory_conv ON chat_memory(conversation_id, created_at DESC);

-- ==================== 初始化数据 ====================
INSERT INTO tenant (name) VALUES ('默认租户') ON CONFLICT DO NOTHING;

-- 默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO agent (tenant_id, name, email, password_hash, role, status)
VALUES (1, '管理员', 'admin@smartcs.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', 'OFFLINE')
    ON CONFLICT DO NOTHING;
