"""Entry point: ``python -m vector_mcp``."""

from __future__ import annotations

from vector_mcp.server import main


if __name__ == "__main__":
    import asyncio

    asyncio.run(main())
