# engine-py 镜像：uv 装依赖 + 跑 FastAPI 服务
FROM python:3.11-slim AS base

# uv：快速依赖管理（从官方镜像拷贝二进制）
COPY --from=ghcr.io/astral-sh/uv:0.5 /uv /uvx /bin/

ENV UV_COMPILE_BYTECODE=1 \
    UV_LINK_MODE=copy \
    ENGINE_HOST=0.0.0.0 \
    ENGINE_PORT=8087

WORKDIR /app

# 先装依赖（利用层缓存），再拷源码。带上 uv.lock 走锁定，保证镜像与本地/CI 可复现一致。
COPY pyproject.toml uv.lock ./
COPY src ./src
COPY README.md ./
RUN uv sync --no-dev --frozen

EXPOSE 8087

# 健康检查打到 /healthz
HEALTHCHECK --interval=10s --timeout=3s --start-period=15s --retries=5 \
    CMD python -c "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://localhost:8087/healthz').status==200 else 1)"

CMD ["uv", "run", "--no-dev", "privacy-engine"]
