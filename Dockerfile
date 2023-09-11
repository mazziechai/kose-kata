FROM python:3.11-slim AS builder
RUN python -m pip install pdm
RUN pdm config python.use_venv false

COPY pyproject.toml pdm.lock /project/
WORKDIR /project
RUN pdm install --prod --no-lock --no-editable

FROM python:3.11-slim as bot
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends postgresql-15 libuv1 build-essential && \
    apt-get autoclean -y && \
    apt-get autoremove -y

ENV PYTHONPATH=/project/pkgs
COPY --from=builder /project/__pypackages__/3.11/lib /project/pkgs
COPY src/ /project/pkgs/
ENTRYPOINT ["python", "-m", "kose_kata"]
