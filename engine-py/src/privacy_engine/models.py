"""请求/响应模型 —— 与 `contracts/openapi/privacy-psi-v1.yaml` 一一对应。

对外 JSON 使用 camelCase（与契约一致），Python 内部用 snake_case。
"""

from __future__ import annotations

from enum import Enum

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class _CamelModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


class Party(_CamelModel):
    party_id: str
    tenant: str
    # mock 后端为明文元素；真实 PSI 中元素不出域，此字段仅用于本地样例。
    elements: list[str]


class PsiStatus(str, Enum):
    SUCCEEDED = "SUCCEEDED"
    REJECTED = "REJECTED"
    FAILED = "FAILED"


class PsiRunRequest(_CamelModel):
    job_id: str
    initiator_tenant: str
    parties: list[Party] = Field(min_length=2, max_length=2)
    # 跨租户联合计算的显式授权开关；默认 False（默认不串）。
    cross_tenant_authorized: bool = False


class PsiRunResult(_CamelModel):
    job_id: str
    status: PsiStatus
    intersection_size: int
    intersection: list[str] = Field(default_factory=list)
    message: str = "ok"


class ErrorCode(str, Enum):
    CROSS_TENANT_NOT_AUTHORIZED = "CROSS_TENANT_NOT_AUTHORIZED"
    INVALID_REQUEST = "INVALID_REQUEST"
    ENGINE_ERROR = "ENGINE_ERROR"


class PsiError(_CamelModel):
    code: ErrorCode
    message: str


class Health(_CamelModel):
    status: str = "UP"
    engine: str = "mock"
