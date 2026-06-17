# hashmatrix-privacy

> hashmatrix 数据中台子模块 · 所属：数据工具分系统(隐私)
>
> 主仓：[HashMatrixData/hashmatrix](https://github.com/HashMatrixData/hashmatrix)

## 产品形态与多租户（北极星）

**双模交付**：公网 SaaS（我们运营 · 统一**我们品牌** · 租户=企业）／私有化部署（客户环境 · **客户品牌**部署级 · 租户=客户部门）。品牌**部署级**、不按租户运行期换肤。多租户走 **C 分层桥接**：控制平面共享 + 数据平面按租户隔离（Keycloak Organizations 单 realm · schema/db-per-tenant · namespace-per-tenant），由 `control-plane` 编排开通。

**本仓视角**：隐私计算按租户隔离，跨租户联合须显式授权、默认不串。

> 详见主仓 `docs/00-主仓初始化-spec.md`、`docs/architecture/05-多租户与控制平面.md`。

## 职责

隐私计算：MPC、安全求交(PSI)、匿踪查询、节点互联、可视编排。

## 技术栈

Python+Java（**具体技术选型待独立讨论，逐步丰富**）

## 说明

本仓库作为 `hashmatrix` 主仓的 git submodule，挂载于 `services/privacy`。架构背景见主仓 `docs/architecture/`。

## License

Apache-2.0
