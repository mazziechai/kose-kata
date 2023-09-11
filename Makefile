init:
	pdm install
	pdm run pre-commit install

build:
	docker compose build

up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs

# TODO: Add db related commands
