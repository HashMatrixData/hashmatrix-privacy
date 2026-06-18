# hashmatrix-privacy

> hashmatrix 数据中台子模块 · 所属：应用服务层 · 数据工具分系统（隐私计算）
>
> 主仓：[HashMatrixData/hashmatrix](https://github.com/HashMatrixData/hashmatrix)

## 角色与位置（一眼看懂）

- **所属**：应用服务层 · 隐私计算分系统（多语言，Python 为主 + Java 编排）。
- **一句话**：让数据"可用不可见"——MPC / 安全求交(PSI) / 匿踪查询 / 多方节点互联。
- **调用流**：多方参与节点 ↔ **privacy（隐私计算引擎）** ↔ 编排/对外 API → 结果供应用消费。

## 职责与边界

- **做**：安全多方计算（MPC）、隐私求交（PSI）、匿踪查询、多方节点互联、隐私编排。
- **不做（边界）**：通用数据采集/计算在 `data-foundation`；建模台（SecretPad）**集成纳入**；跨租户联合须显式授权。

## 骨架技术选型（首选 · 待逐仓细化）

| 维度 | 选型 |
|--|--|
| 隐私计算引擎 | **隐语 SecretFlow**（Python，备 FATE） |
| 服务编排 / 对外 | Java（Spring Boot），REST / gRPC |
| 形态 | 多方节点互联；建模台 SecretPad 集成纳入 |

> Python 子系统作独立 submodule 隔离多语言现实（见架构 04）。

## 产品形态与多租户（北极星）

**双模交付**：公网 SaaS（我们运营 · 统一**我们品牌** · 租户=企业）／私有化部署（客户环境 · **客户品牌**部署级 · 租户=客户部门）。品牌**部署级**、不按租户运行期换肤。多租户走 **C 分层桥接**：控制平面共享 + 数据平面按租户隔离（Keycloak Organizations 单 realm · schema/db-per-tenant · namespace-per-tenant），由 `control-plane` 编排开通。

**本仓视角**：隐私计算按租户隔离，跨租户联合须显式授权、默认不串。

> 详见主仓 `docs/00-主仓初始化-spec.md`、`docs/architecture/05-多租户与控制平面.md`。

## 双栈结构（各自独立可开发/编译/调试/运行）

```
privacy/
├── engine-py/            # Python · SecretFlow 计算引擎（PSI/求交）；uv + pytest + FastAPI
├── orchestrator-java/    # Java · Spring Boot 编排层；Maven；调用 engine-py
├── node-mock/            # 节点互联 mock（本地联调用，纯标准库）
├── contracts/            # 编排契约：openapi(live REST) + proto(forward gRPC)
├── samples/              # 最小 PSI 样例（mock 双方）端到端跑通
└── docker-compose.local.yml  # 一键起双栈 + 节点 mock
```

| 栈 | 目录 | 独立命令 |
|---|---|---|
| Python 引擎 | `engine-py/` | `cd engine-py && uv sync && uv run pytest` |
| Java 编排 | `orchestrator-java/` | `cd orchestrator-java && ./mvnw package` |

> 两栈各自可独立装依赖/构建/测试/运行，互不阻塞。详见各目录 README。

## 一键本地运行 + 最小 PSI 样例

```bash
# 起双栈 + 节点 mock
docker compose -f docker-compose.local.yml up --build

# 起全套并跑通最小 PSI 样例（mock 双方），样例退出码即结果
docker compose -f docker-compose.local.yml --profile sample up --build \
  --abort-on-container-exit --exit-code-from psi-sample
```

样例流程：节点互联 mock → 编排层（带 `X-Tenant-Id`）→ 计算引擎 → 返回交集。

## 多租户红线

隐私计算按租户隔离；**跨租户联合计算须显式授权，默认不串**。
编排层 `TenantContext`（来自主仓 `starter-tenant`）为第一道闸，引擎侧纵深防御；
未授权的跨租户求交返回 `403 CROSS_TENANT_NOT_AUTHORIZED`。

## Java 基座（io.hashmatrix 公共依赖）

orchestrator-java 经 **Maven 坐标**引用主仓 libs-java：`<parent>` = `io.hashmatrix:hashmatrix-platform-parent:0.1.0`、
`import` `hashmatrix-bom`、依赖 `hashmatrix-starter-tenant`（多租户上下文）+ `hashmatrix-starter-test`（统一测试栈）。
公共制品发布在 **GitHub Packages**，构建需配置制品仓访问——见
[`orchestrator-java/README.md`](./orchestrator-java/README.md#制品仓访问github-packages)。详见主仓 `docs/00-主仓初始化-spec.md` §3。

## 说明

本仓库作为 `hashmatrix` 主仓的 git submodule，挂载于 `services/privacy`。架构背景见主仓 `docs/architecture/`。

## License

Apache-2.0
