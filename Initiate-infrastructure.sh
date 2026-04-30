# 1. 启动 PostgreSQL + Redis + RustFS(MinIO)
docker compose up -d

# 2. 等待 PostgreSQL 就绪（约 10 秒）
docker compose logs -f postgres
# 看到 "database system is ready to accept connections" 后 Ctrl+C
