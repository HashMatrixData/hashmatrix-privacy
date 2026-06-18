# contracts · 编排接口契约（privacy 本仓）

Java 编排层（`orchestrator-java`）↔ Python 计算引擎（`engine-py`）之间的接口契约。
**先改契约、再改实现。**

| 文件 | 通道 | 状态 |
|---|---|---|
| `openapi/privacy-psi-v1.yaml` | REST | **live** —— 当前最小骨架的运行通道 |
| `proto/privacy/v1/psi.proto` | gRPC | **forward** —— 未来流式 PSI 演进目标（待 protoc 工具链） |

## 约定

- **多租户红线**：跨租户联合计算（参与方分属不同租户）须显式 `crossTenantAuthorized=true`，否则引擎拒绝（默认不串）。
- **脱敏**：示例数据一律虚构占位（`acme` / `tenant-demo` / `example.com`），严禁真实数据。

## 与主仓 contracts/ 的关系

本契约同时登记到主仓 `contracts/`（ICD 统一维护处）。本仓副本随实现演进，
变更需保持与主仓登记一致；契约评审通过后再落实现。
