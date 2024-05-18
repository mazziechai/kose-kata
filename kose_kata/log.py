import logging

from . import var


def init_logger() -> None:
    root_logger = logging.getLogger()
    root_logger.setLevel(var.LOG_LEVEL)

    datefmt = "%Y-%m-%d %H:%M:%S"
    fmt = logging.Formatter(
        "[{asctime}] [{levelname}] {name}: {message}", datefmt, style="{"
    )

    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(fmt)
    root_logger.addHandler(stream_handler)
