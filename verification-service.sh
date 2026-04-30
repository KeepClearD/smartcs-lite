# 健康检查
curl http://localhost:8080/actuator/health

# 创建知识库
curl -X POST "http://localhost:8080/api/v1/knowledge-bases?name=产品FAQ&description=常见问题"

# 添加 FAQ
curl -X POST http://localhost:8080/api/v1/knowledge-bases/1/faqs \
  -H "Content-Type: application/json" \
  -d '{
    "question": "如何退款？",
    "answer": "请在订单详情页点击退款按钮，填写退款原因后提交。一般3-5个工作日内到账。",
    "category": "退款"
  }'

# 查看统计
curl http://localhost:8080/api/v1/analytics/overview
