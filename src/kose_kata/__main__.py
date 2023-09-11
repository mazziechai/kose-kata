import logging
import os

import lightbulb

TOKEN = os.environ["TOKEN"]
LOG_LEVEL = os.environ["LOG_LEVEL"].upper()
TEST_SERVER = int(os.environ["TEST_SERVER"])
POSTGRES_USER = os.environ["POSTGRES_USER"]
POSTGRES_PASSWORD = os.environ["POSTGRES_PASSWORD"]
POSTGRES_DB = os.environ["POSTGRES_DB"]

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


def main() -> None:
    init_logger()

    bot = lightbulb.BotApp(token=TOKEN, default_enabled_guilds=TEST_SERVER, logs="INFO")

    bot.run()


if __name__ == "__main__":
    if os.name != "nt":
        import uvloop

        uvloop.install()

    main()
