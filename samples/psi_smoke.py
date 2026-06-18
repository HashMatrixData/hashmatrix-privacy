"""最小 PSI 样例（mock 双方）—— 端到端跑通：节点互联 mock → 编排层 → 计算引擎。

流程：
  1. 从 node-mock 取远端参与方元素（模拟节点互联）；
  2. 与本地参与方组装一次 PSI 求交请求；
  3. 经编排层 orchestrator-java（带 X-Tenant-Id）下发，编排层调用 engine-py 计算；
  4. 断言交集结果符合预期。

纯标准库实现。脱敏占位 tenant-demo / example.com。
"""

from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request

ORCH_URL = os.getenv("ORCH_URL", "http://localhost:8080")
NODE_URL = os.getenv("NODE_URL", "http://localhost:9000")
TENANT = os.getenv("TENANT", "tenant-demo")

# 本地参与方（脱敏）。与 node-mock 的 [bob, carol, dave] 交集应为 [bob, carol]。
LOCAL_PARTY = {
    "partyId": "party-local",
    "tenant": TENANT,
    "elements": ["alice@example.com", "bob@example.com", "carol@example.com"],
}
EXPECTED_INTERSECTION = {"bob@example.com", "carol@example.com"}


def _get_json(url: str) -> dict:
    with urllib.request.urlopen(url, timeout=10) as resp:
        return json.load(resp)


def _post_json(url: str, body: dict, headers: dict) -> dict:
    data = json.dumps(body).encode()
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    for k, v in headers.items():
        req.add_header(k, v)
    with urllib.request.urlopen(req, timeout=10) as resp:
        return json.load(resp)


def main() -> int:
    remote_party = _get_json(f"{NODE_URL}/v1/party")
    print(f"[1] node-mock remote party: {remote_party['partyId']} ({len(remote_party['elements'])} elements)")

    request = {
        "jobId": "job-smoke-0001",
        "parties": [LOCAL_PARTY, remote_party],
        "crossTenantAuthorized": False,
    }
    result = _post_json(
        f"{ORCH_URL}/api/v1/psi/intersect",
        request,
        headers={"X-Tenant-Id": TENANT},
    )
    print(f"[2] orchestrator → engine PSI result: {json.dumps(result, ensure_ascii=False)}")

    intersection = set(result.get("intersection", []))
    if result.get("status") != "SUCCEEDED" or intersection != EXPECTED_INTERSECTION:
        print(f"[FAIL] expected {sorted(EXPECTED_INTERSECTION)}, got {sorted(intersection)}", file=sys.stderr)
        return 1
    print(f"[OK] PSI intersection = {sorted(intersection)} (size={result['intersectionSize']})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
