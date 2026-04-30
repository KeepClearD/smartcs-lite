# 配置环境变量（或在 .env 文件中配置）
export OPENAI_API_KEY=sk-your-key-here

# 编译运行
mvn clean package -DskipTests
java -jar target/smartcs-lite-0.1.0-SNAPSHOT.jar

# 或者开发模式
mvn spring-boot:run
