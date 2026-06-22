# CLAUDE.md — hashmatrix-privacy 协作与合规指引

本文件为 Claude Code 及所有协作者在本仓库工作的**强制约束**。违反「信息红线」的内容一律不得提交。

## 🔴 信息红线（强制 · 不可协商）

本仓库为**公开开源仓库**。所有内容（代码、注释、文档、配置样例、提交信息、Issue/PR、分支与标签名）必须满足：

1. **禁止出现任何甲方/客户可识别信息**，包括但不限于：真实单位名称/简称/品牌、人员姓名或账号、招标/合同/立项编号、内部项目代号、甲方专有业务术语、真实数据、具体部署地点、客户网络或系统拓扑。
2. **禁止透漏任何项目机密**：商务/合同条款、里程碑与报价、验收细节、甲方环境参数、真实业务数据样本。
3. **仅允许记录可面向大众公开的内容**：通用技术方案、代码实现、系统架构与产品决策、开源组件选型、通用工程最佳实践。
4. **示例/测试数据一律虚构脱敏**，使用通用占位（如 `example.com`、`acme`、`tenant-demo`），严禁使用任何真实甲方数据。
5. **敏感原始资料一律置于 `.gitignore`、不得入库**（仅本地留存）。

> 判定标准：把本仓任意文件公开到互联网，不会泄露任何客户身份或项目机密。不确定时一律按「不写入」处理。

## 提交前自检（每次 commit / PR 必过）

- [ ] 无甲方名称 / 编号 / 代号 / 人员 / 地点等可识别信息
- [ ] 无商务 / 合同 / 验收 / 报价等项目机密
- [ ] 示例数据均为虚构 / 脱敏
- [ ] 敏感原始资料未入库（已在 `.gitignore`）
- [ ] 提交信息与分支/标签名同样不含上述敏感信息

## 🧭 北极星：产品形态与多租户模式（开发者时刻谨记）

本平台**双模交付**，所有设计与代码都须按此模式思考：

| | 公网 SaaS | 私有化部署 |
|--|--|--|
| 运营 / 品牌 | 我们运营 · **我们公司统一品牌** | 客户环境 · **客户品牌（部署级）** |
| 租户 = | 企业客户 | 客户的部门 |

- **品牌是部署级**（部署期配置注入），**不按租户在运行期动态换肤**。
- **多租户隔离（C 分层桥接）**：控制平面共享 + 数据平面按租户隔离。身份 = Keycloak **Organizations 单 realm**（org=租户，JWT 带 tenant 声明）；数据 = **schema/db-per-tenant**；计算 = **namespace-per-tenant**；由 `control-plane` 编排开通。

**本仓视角（privacy）**：MPC / PSI / 匿踪等隐私计算**按租户隔离**；跨租户联合计算须**显式授权**，默认不串租户数据。Python+Java 子系统同样遵循租户上下文透传。

> 全局定义见主仓 `docs/00-主仓初始化-spec.md` 与 `docs/architecture/05-多租户与控制平面.md`。

## 仓库定位

隐私计算子模块：MPC、安全求交(PSI)、匿踪查询、节点互联、可视编排（Python+Java）。

技术栈与具体选型**待独立讨论后逐步丰富**，当前为初始脚手架。

## 📜 契约声明（ICD）

> **单一事实源 = 主仓 `contracts/`**。本仓**不 vendor 副本**（已删除旧 `contracts/` 目录），按需直读
> `../../contracts`；改契约先动主仓、再落实现，避免漂移。查契约/同步本块用 toolkit `/contracts` skill。

**Producer（本仓提供）**
- `privacy-orchestrator-v1`：编排层对外 REST 契约（客户端/网关 → orchestrator-java）
  （主仓 `contracts/openapi/privacy-orchestrator-v1.yaml`；`POST /api/v1/psi/intersect`，
  发起方租户取自 `X-Tenant-Id` 头、不在体内；经网关 `/api/privacy/*` 暴露）。
- `privacy-psi-v1`：编排层 ↔ 引擎的内部计算契约（orchestrator-java → engine-py）
  （主仓 `contracts/openapi/privacy-psi-v1.yaml`；`POST /v1/psi/run`、`/healthz`）。
  前瞻流式 PSI 见 `contracts/proto/privacy/v1/psi.proto`（gRPC，后置）。

**Consumer（本仓依赖）**
- `tenant-context-headers-icd`（主仓 `contracts/icd/`）：消费方按 **`X-Tenant-Id`** 做数据/计算隔离路由；
  `X-Tenant-Org` 仅信息展示、`X-Tenant-Subject` 预留须容忍其存在（tolerant reader）；
  取不到租户上下文却访问租户隔离资源即编程/配置错误（不静默放行）。`starter-tenant` 默认 `required=false`
  （信任边缘已强制：服务仅在网关之后可达）。

**M1 工程约定**
- 端口（env 可覆盖，默认对齐基线）：orch **8086**/管理 **9086**、engine **8087**、node-mock **8088**。
- 镜像命名：`ghcr.io/hashmatrixdata/privacy-{orchestrator-java,engine}:<tag>`；
  **子仓交付 image，主仓 `deploy/` owns charts**（D5）。
  `privacy-node`（node-mock）**仅本地 compose 联调用，不经 CI 发布、不入集群 chart**。
