from datetime import datetime
from typing import List
from sqlalchemy import func
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
from sqlalchemy.ext.asyncio import AsyncAttrs


class Base(AsyncAttrs, DeclarativeBase):
    pass


class Note(Base):
    __tablename__ = "notes"

    id: Mapped[int] = mapped_column(primary_key=True)
    author: Mapped[int] = mapped_column(nullable=False)
    guild: Mapped[int] = mapped_column(nullable=False)
    name: Mapped[str] = mapped_column(nullable=False)
    aliases: Mapped[List[str]] = mapped_column(nullable=False)
    content: Mapped[str] = mapped_column(nullable=False)
    original_author: Mapped[int] = mapped_column(nullable=True)
    time_created: Mapped[datetime] = mapped_column(
        nullable=False, server_default=func.now()
    )
