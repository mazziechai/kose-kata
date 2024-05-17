import asyncio
import logging
import os

from sqlalchemy.ext.asyncio import create_async_engine

TOKEN = os.environ["TOKEN"]
LOG_LEVEL = os.environ["LOG_LEVEL"].upper()
TEST_SERVER = int(os.environ["TEST_SERVER"])
POSTGRES_USER = os.environ["POSTGRES_USER"]
POSTGRES_PASSWORD = os.environ["POSTGRES_PASSWORD"]
POSTGRES_DB = os.environ["POSTGRES_DB"]
DEVELOPER = int(os.environ["DEVELOPER"])

LOG = logging.getLogger()


def init_logger() -> None:
    LOG.setLevel(LOG_LEVEL)

    datefmt = "%Y-%m-%d %H:%M:%S"
    fmt = logging.Formatter(
        "[{asctime}] [{levelname}] {name}: {message}", datefmt, style="{"
    )

    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(fmt)
    LOG.addHandler(stream_handler)


async def main() -> None:
    init_logger()

    engine = create_async_engine(
        f"postgresql+asyncpg://{POSTGRES_USER}:{POSTGRES_PASSWORD}@localhost/{POSTGRES_DB}",
        echo=True,
    )

    await engine.dispose()


if __name__ == "__main__":
    if os.name != "nt":
        import uvloop

        uvloop.install()

    asyncio.run(main())
