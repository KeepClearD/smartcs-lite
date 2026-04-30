# 安装 wscat（如果没有）
npm install -g wscat

# 连接聊天
wscat -c "ws://localhost:8080/ws/chat?customerId=test-001&tenantId=1"

# 连接后发送消息
> {"type":"chat","content":"你好，我想问一下怎么退款？"}

# 你会收到 AI 的回复

# 请求转人工
> {"type":"transfer","content":""}

# 关闭会话
> {"type":"close","content":""}
