from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
import os
from dotenv import load_dotenv

load_dotenv()

# Build database URL from environment or default to local SQLite
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
default_url = f"sqlite:///{os.path.join(BASE_DIR, 'spliteasy.db')}"

SQLALCHEMY_DATABASE_URL = os.environ.get("DATABASE_URL", default_url)

# Setup connect_args conditionally (sqlite needs check_same_thread)
connect_args = {"check_same_thread": False} if SQLALCHEMY_DATABASE_URL.startswith("sqlite") else {}

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args=connect_args
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
