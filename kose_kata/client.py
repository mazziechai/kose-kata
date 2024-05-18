import logging

import arc
from sqlalchemy.ext.asyncio import create_async_engine
from alembic.config import Config
from alembic import command

from . import db, var


LOG = logging.getLogger("kose_kata.client")


async def startup_hook(client: arc.GatewayClient) -> None:
    LOG.info("Initializing client.")

    # DB stuff
    db_url = f"postgresql+asyncpg://{var.POSTGRES_USER}:{var.POSTGRES_PASSWORD}@db/{var.POSTGRES_DB}"

    LOG.info("Running migration scripts.")
    alembic_cfg = Config("/project/pkgs/alembic.ini")
    alembic_cfg.set_main_option("script_location", "/project/pkgs/migrations")
    alembic_cfg.set_main_option("sqlalchemy.url", db_url)

    command.upgrade(alembic_cfg, "head")

    engine = create_async_engine(
        f"postgresql+asyncpg://{var.POSTGRES_USER}:{var.POSTGRES_PASSWORD}@db/{var.POSTGRES_DB}",
        echo=var.DEBUG,
    )
    database = db.Database(engine)

    client.set_type_dependency(db.Database, database)


async def shutdown_hook(client: arc.GatewayClient) -> None:
    LOG.info("Shutting down.")

    database = client.get_type_dependency(db.Database)
    await database.engine.dispose()
