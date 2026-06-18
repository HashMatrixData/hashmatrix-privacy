"""节点互联 mock —— 模拟一个远端隐私计算参与方节点。

仅用于本地 docker-compose 联调：对外提供一个远端参与方的（脱敏）元素集合，
供编排层组装跨节点 PSI 求交样例。纯标准库实现，无第三方依赖。

脱敏占位：tenant-demo / example.com。
"""

from __future__ import annotations

import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

# 远端参与方（mock）。真实场景下元素不出域，这里仅为联调样例。
REMOTE_PARTY = {
    "partyId": "party-remote",
    "tenant": os.getenv("NODE_TENANT", "tenant-demo"),
    "elements": ["bob@example.com", "carol@example.com", "dave@example.com"],
}


class Handler(BaseHTTPRequestHandler):
    def _send(self, code: int, body: dict) -> None:
        payload = json.dumps(body).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def do_GET(self) -> None:  # noqa: N802
        if self.path == "/healthz":
            self._send(200, {"status": "UP"})
        elif self.path == "/v1/party":
            self._send(200, REMOTE_PARTY)
        else:
            self._send(404, {"code": "NOT_FOUND", "message": self.path})

    def log_message(self, *args) -> None:  # 静默默认访问日志
        pass


def main() -> None:
    host = os.getenv("NODE_HOST", "0.0.0.0")
    port = int(os.getenv("NODE_PORT", "9000"))
    ThreadingHTTPServer((host, port), Handler).serve_forever()


if __name__ == "__main__":
    main()
