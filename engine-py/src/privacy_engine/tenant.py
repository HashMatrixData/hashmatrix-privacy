"""多租户边界守卫。

红线：隐私计算按租户隔离；**跨租户联合计算须显式授权，默认不串**。
当一次 PSI 的参与方分属不同租户时，必须 `cross_tenant_authorized=True`，否则拒绝。

注：这是引擎侧的纵深防御；编排层（orchestrator-java 的 starter-tenant TenantContext）
是第一道闸。两侧都校验，避免单点遗漏。
"""

from __future__ import annotations

from .models import PsiRunRequest


class CrossTenantNotAuthorized(Exception):
    """跨租户联合计算未显式授权。"""


def party_tenants(request: PsiRunRequest) -> set[str]:
    return {p.tenant for p in request.parties}


def is_cross_tenant(request: PsiRunRequest) -> bool:
    return len(party_tenants(request)) > 1


def assert_psi_authorized(request: PsiRunRequest) -> None:
    """跨租户但未显式授权时抛出 ``CrossTenantNotAuthorized``。"""
    if is_cross_tenant(request) and not request.cross_tenant_authorized:
        tenants = ", ".join(sorted(party_tenants(request)))
        raise CrossTenantNotAuthorized(
            f"cross-tenant PSI across [{tenants}] requires explicit authorization"
        )
