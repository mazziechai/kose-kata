import logging
import os

import hikari
import arc


from . import log, var
from . import client as c

LOG = logging.getLogger("kose_kata.main")


def main() -> None:
    LOG.debug(os.path.curdir)
    log.init_logger()

    bot = hikari.GatewayBot(var.TOKEN)
    client = arc.GatewayClient(bot)

    # Hooks
    client.add_startup_hook(c.startup_hook)
    client.add_shutdown_hook(c.shutdown_hook)

    bot.run()


if __name__ == "__main__":
    if os.name != "nt":
        import uvloop

    uvloop.install()

    main()
