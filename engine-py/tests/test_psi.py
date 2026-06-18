"""MockPsiEngine 与跨租户授权守卫的单测。脱敏占位 acme / tenant-demo / example.com。"""

from __future__ import annotations

import pytest

from privacy_engine.models import Party, PsiRunRequest, PsiStatus
from privacy_engine.psi import InvalidPsiRequest, MockPsiEngine
from privacy_engine.tenant import CrossTenantNotAuthorized, is_cross_tenant


def _req(left_tenant: str, right_tenant: str, *, authorized: bool = False) -> PsiRunRequest:
    return PsiRunRequest(
        job_id="job-0001",
        initiator_tenant=left_tenant,
        parties=[
            Party(party_id="party-a", tenant=left_tenant,
                  elements=["alice@example.com", "bob@example.com", "bob@example.com"]),
            Party(party_id="party-b", tenant=right_tenant,
                  elements=["bob@example.com", "carol@example.com"]),
        ],
        cross_tenant_authorized=authorized,
    )


def test_same_tenant_intersection():
    result = MockPsiEngine().run(_req("tenant-demo", "tenant-demo"))
    assert result.status is PsiStatus.SUCCEEDED
    # 去重 + 保持发起方顺序
    assert result.intersection == ["bob@example.com"]
    assert result.intersection_size == 1


def test_empty_intersection():
    req = PsiRunRequest(
        job_id="job-empty",
        initiator_tenant="acme",
        parties=[
            Party(party_id="party-a", tenant="acme", elements=["x@example.com"]),
            Party(party_id="party-b", tenant="acme", elements=["y@example.com"]),
        ],
    )
    result = MockPsiEngine().run(req)
    assert result.intersection_size == 0
    assert result.intersection == []


def test_cross_tenant_detection():
    assert is_cross_tenant(_req("acme", "tenant-demo")) is True
    assert is_cross_tenant(_req("acme", "acme")) is False


def test_cross_tenant_blocked_by_default():
    with pytest.raises(CrossTenantNotAuthorized):
        MockPsiEngine().run(_req("acme", "tenant-demo", authorized=False))


def test_cross_tenant_allowed_when_authorized():
    result = MockPsiEngine().run(_req("acme", "tenant-demo", authorized=True))
    assert result.status is PsiStatus.SUCCEEDED
    assert result.intersection == ["bob@example.com"]


def test_invalid_party_count():
    # pydantic 限制 2 方；直接构造非法请求以校验引擎兜底
    req = _req("acme", "acme")
    req.parties = req.parties[:1]
    with pytest.raises(InvalidPsiRequest):
        MockPsiEngine().run(req)
