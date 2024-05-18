from datetime import datetime
import logging
from typing import List, Optional
from sqlalchemy import ForeignKey, func
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship
from sqlalchemy.ext.asyncio import (
    AsyncAttrs,
    AsyncEngine,
    async_sessionmaker,
)


LOG = logging.getLogger("kose_kata.db")


class Base(AsyncAttrs, DeclarativeBase):
    pass


class Alias(Base):
    __tablename__ = "aliases"

    id: Mapped[int] = mapped_column(primary_key=True)
    note_id: Mapped[int] = mapped_column(ForeignKey("notes.id"))
    alias: Mapped[str] = mapped_column()

    note: Mapped["Note"] = relationship(back_populates="aliases")


class Note(Base):
    __tablename__ = "notes"

    id: Mapped[int] = mapped_column(primary_key=True)
    author: Mapped[int] = mapped_column()
    guild: Mapped[int] = mapped_column()
    name: Mapped[str] = mapped_column()
    aliases: Mapped[List["Alias"]] = relationship(back_populates="note")
    content: Mapped[str] = mapped_column()
    original_author: Mapped[Optional[int]] = mapped_column()
    time_created: Mapped[datetime] = mapped_column(server_default=func.now())


class Database:
    engine: AsyncEngine
    s: async_sessionmaker

    def __init__(self, engine: AsyncEngine):
        self.engine = engine
        self.s = async_sessionmaker(engine, expire_on_commit=False)
