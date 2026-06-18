# engine-py · 隐私计算引擎（SecretFlow）

PSI/安全求交等隐私计算原语的计算服务，向编排层（`orchestrator-java`）暴露 REST API。
契约见 [`../contracts/openapi/privacy-psi-v1.yaml`](../contracts/openapi/privacy-psi-v1.yaml)。

> **只进本目录即可装依赖、跑测试、起服务**，无需 clone 主仓或其他子目录。

## 依赖管理：uv

```bash
cd engine-py
uv sync                 # 安装依赖（含 dev：pytest/httpx）
uv run pytest           # 跑单测
uv run privacy-engine   # 起 REST 服务（默认 :8000）
```

## PSI 后端

| 后端 | 说明 | 启用 |
|---|---|---|
| `mock`（默认） | 纯 Python 集合求交，用于样例/测试/CI | 开箱即用 |
| `secretflow` | 隐语 SecretFlow 真实 PSI（元素不出域） | `uv pip install --prerelease=allow secretflow` + 实现 `SecretFlowPsiEngine`（TODO） |

> SecretFlow 仅发布 pre-release 且对 `spu` 版本钉死，故**不纳入** `pyproject` 锁定（否则拖垮 mock 链路解析）；真实接入时按上表手动安装。

经环境变量 `PSI_BACKEND` 选择（默认 `mock`）。

## 多租户红线

跨租户联合计算（参与方分属不同租户）须显式 `crossTenantAuthorized=true`，否则引擎返回
`403 CROSS_TENANT_NOT_AUTHORIZED`（默认不串）。编排层 `TenantContext` 为第一道闸，引擎为纵深防御。

## 最小 PSI 样例

```bash
curl -s localhost:8000/v1/psi/run -H 'content-type: application/json' -d '{
  "jobId": "job-0001",
  "initiatorTenant": "tenant-demo",
  "parties": [
    {"partyId": "party-a", "tenant": "tenant-demo", "elements": ["alice@example.com","bob@example.com"]},
    {"partyId": "party-b", "tenant": "tenant-demo", "elements": ["bob@example.com","carol@example.com"]}
  ]
}'
# → {"jobId":"job-0001","status":"SUCCEEDED","intersectionSize":1,"intersection":["bob@example.com"],"message":"ok"}
```
