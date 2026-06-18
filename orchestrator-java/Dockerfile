# orchestrator-java 运行镜像（runtime-only）。
#
# 注意：本镜像不在容器内跑 mvn。因为公共依赖（io.hashmatrix:*）发布在 GitHub Packages，
# 容器内构建需注入制品仓凭据——不洁。改为「在宿主/CI 先 ./mvnw package（那里有 settings.xml
# 凭据），再把 fat-jar 拷进运行镜像」。故 docker build 前需先产出 target/*.jar。
#
#   ./mvnw package && docker build -t hashmatrix-privacy/orchestrator-java:local .
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app
ENV ENGINE_BASE_URL=http://engine-py:8000
# curl 供健康检查（jre-jammy 基础镜像默认无 curl）
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY target/privacy-orchestrator-*.jar app.jar

EXPOSE 8080

# 健康检查打到 actuator 探针
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
    CMD curl -fsS http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
