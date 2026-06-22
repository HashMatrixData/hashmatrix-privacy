"""FastAPI 应用 —— 实现契约 privacy-psi-v1（主仓 contracts/openapi/privacy-psi-v1.yaml，单一事实源）。"""

from __future__ import annotations

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from .models import ErrorCode, Health, PsiError, PsiRunRequest, PsiRunResult
from .psi import InvalidPsiRequest, get_engine
from .tenant import CrossTenantNotAuthorized

app = FastAPI(title="Privacy PSI Engine API", version="1.0.0")
engine = get_engine()


def _error(status_code: int, code: ErrorCode, message: str) -> JSONResponse:
    body = PsiError(code=code, message=message)
    return JSONResponse(status_code=status_code, content=body.model_dump(by_alias=True))


@app.exception_handler(CrossTenantNotAuthorized)
async def _cross_tenant_handler(_: Request, exc: CrossTenantNotAuthorized) -> JSONResponse:
    return _error(403, ErrorCode.CROSS_TENANT_NOT_AUTHORIZED, str(exc))


@app.exception_handler(InvalidPsiRequest)
async def _invalid_handler(_: Request, exc: InvalidPsiRequest) -> JSONResponse:
    return _error(422, ErrorCode.INVALID_REQUEST, str(exc))


@app.exception_handler(RequestValidationError)
async def _validation_handler(_: Request, exc: RequestValidationError) -> JSONResponse:
    return _error(422, ErrorCode.INVALID_REQUEST, str(exc))


@app.get("/healthz", response_model=Health)
def health() -> Health:
    return Health(status="UP", engine=engine.name)


@app.post("/v1/psi/run", response_model=PsiRunResult)
def run_psi(request: PsiRunRequest) -> PsiRunResult:
    return engine.run(request)
