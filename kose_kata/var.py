import os

from dotenv import load_dotenv

load_dotenv(override=False)

TOKEN = os.environ["TOKEN"]
LOG_LEVEL = os.environ["LOG_LEVEL"].upper()
TEST_SERVER = int(os.environ["TEST_SERVER"])
POSTGRES_USER = os.environ["POSTGRES_USER"]
POSTGRES_PASSWORD = os.environ["POSTGRES_PASSWORD"]
POSTGRES_DB = os.environ["POSTGRES_DB"]
DEVELOPER = int(os.environ["DEVELOPER"])
DEBUG = bool(os.environ["DEBUG"])
