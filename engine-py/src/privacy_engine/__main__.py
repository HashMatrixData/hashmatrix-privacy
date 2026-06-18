"""引擎进程入口：``python -m privacy_engine`` 或 ``privacy-engine``。"""

from __future__ import annotations

import os

import uvicorn


def main() -> None:
    host = os.getenv("ENGINE_HOST", "0.0.0.0")
    port = int(os.getenv("ENGINE_PORT", "8000"))
    uvicorn.run("privacy_engine.app:app", host=host, port=port, log_level="info")


if __name__ == "__main__":
    main()
