# 节点互联 mock：纯标准库 HTTP 服务（无依赖）
FROM python:3.11-slim
WORKDIR /app
COPY server.py ./
ENV NODE_HOST=0.0.0.0 NODE_PORT=9000
EXPOSE 9000
HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=5 \
    CMD python -c "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://localhost:9000/healthz').status==200 else 1)"
CMD ["python", "server.py"]
