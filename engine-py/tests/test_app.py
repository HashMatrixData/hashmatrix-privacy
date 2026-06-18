"""REST 端点测试（契约 privacy-psi-v1.yaml）。"""

from __future__ import annotations

from fastapi.testclient import TestClient

from privacy_engine.app import app

client = TestClient(app)


def test_healthz():
    resp = client.get("/healthz")
    assert resp.status_code == 200
    assert resp.json() == {"status": "UP", "engine": "mock"}


def _payload(left_tenant: str, right_tenant: str, *, authorized: bool = False) -> dict:
    return {
        "jobId": "job-0001",
        "initiatorTenant": left_tenant,
        "parties": [
            {"partyId": "party-a", "tenant": left_tenant,
             "elements": ["alice@example.com", "bob@example.com"]},
            {"partyId": "party-b", "tenant": right_tenant,
             "elements": ["bob@example.com", "carol@example.com"]},
        ],
        "crossTenantAuthorized": authorized,
    }


def test_run_psi_same_tenant():
    resp = client.post("/v1/psi/run", json=_payload("tenant-demo", "tenant-demo"))
    assert resp.status_code == 200
    body = resp.json()
    assert body["status"] == "SUCCEEDED"
    assert body["intersection"] == ["bob@example.com"]
    assert body["intersectionSize"] == 1


def test_run_psi_cross_tenant_forbidden():
    resp = client.post("/v1/psi/run", json=_payload("acme", "tenant-demo"))
    assert resp.status_code == 403
    assert resp.json()["code"] == "CROSS_TENANT_NOT_AUTHORIZED"


def test_run_psi_cross_tenant_authorized():
    resp = client.post("/v1/psi/run", json=_payload("acme", "tenant-demo", authorized=True))
    assert resp.status_code == 200
    assert resp.json()["intersectionSize"] == 1


def test_run_psi_invalid_party_count():
    payload = _payload("acme", "acme")
    payload["parties"] = payload["parties"][:1]
    resp = client.post("/v1/psi/run", json=payload)
    assert resp.status_code == 422
    assert resp.json()["code"] == "INVALID_REQUEST"
