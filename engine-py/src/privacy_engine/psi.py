"""PSI（安全求交）计算后端。

- ``MockPsiEngine``：纯 Python 集合求交，用于本地样例 / 测试 / CI（不依赖 SecretFlow）。
- 真实后端 ``SecretFlowPsiEngine``：基于隐语 SecretFlow 的 PSI/OT 协议，元素不出域。
  手动安装（``uv pip install --prerelease=allow secretflow``，不纳入锁定），此处留接入点。

通过环境变量 ``PSI_BACKEND``（默认 ``mock``）选择后端。
"""

from __future__ import annotations

import os
from typing import Protocol

from .models import PsiRunRequest, PsiRunResult, PsiStatus
from .tenant import assert_psi_authorized


class InvalidPsiRequest(Exception):
    """请求不满足 PSI 约束（如参与方数量不为 2）。"""


class PsiEngine(Protocol):
    name: str

    def run(self, request: PsiRunRequest) -> PsiRunResult: ...


class MockPsiEngine:
    """明文集合求交的 mock 实现。

    仅用于本地样例与测试：直接计算两方元素的交集。**不是**真实隐私计算，
    元素在引擎内可见。真实部署须切换到 SecretFlow 后端。
    """

    name = "mock"

    def run(self, request: PsiRunRequest) -> PsiRunResult:
        if len(request.parties) != 2:
            raise InvalidPsiRequest("PSI requires exactly 2 parties")

        # 跨租户授权守卫（默认不串）。
        assert_psi_authorized(request)

        left, right = request.parties
        right_set = set(right.elements)
        # 保持发起方（left）的元素顺序，去重。
        seen: set[str] = set()
        intersection = [
            e for e in left.elements
            if e in right_set and not (e in seen or seen.add(e))
        ]
        return PsiRunResult(
            job_id=request.job_id,
            status=PsiStatus.SUCCEEDED,
            intersection_size=len(intersection),
            intersection=intersection,
            message="ok",
        )


class SecretFlowPsiEngine:
    """真实 SecretFlow PSI 后端（接入点占位）。

    TODO: 接入隐语 SecretFlow 的 PSI（如 ECDH-PSI / KKRT）。
    需手动安装 secretflow（不纳入锁定）并以 SPU/PYU 设备编排多方节点。
    """

    name = "secretflow"

    def run(self, request: PsiRunRequest) -> PsiRunResult:  # pragma: no cover
        raise NotImplementedError(
            "SecretFlow PSI backend not wired yet; install extra 'secretflow' and implement"
        )


def get_engine(backend: str | None = None) -> PsiEngine:
    backend = backend or os.getenv("PSI_BACKEND", "mock")
    if backend == "mock":
        return MockPsiEngine()
    if backend == "secretflow":
        return SecretFlowPsiEngine()
    raise InvalidPsiRequest(f"unknown PSI backend: {backend}")
