FROM python:3.12-slim AS builder
RUN python -m pip install pdm
RUN pdm config python.use_venv false

COPY pyproject.toml pdm.lock /project/
WORKDIR /project
RUN pdm install --prod --no-lock --no-editable

FROM python:3.12-slim as bot
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends postgresql-15 libuv1 build-essential && \
    apt-get autoclean -y && \
    apt-get autoremove -y

ENV PYTHONPATH=/project/pkgs
COPY --from=builder /project/__pypackages__/3.12/lib /project/pkgs
COPY ./ /project/pkgs/
ENTRYPOINT ["python", "-O", "-m", "kose_kata"]
