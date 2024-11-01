# config.py
import os

class Config:
    SQLALCHEMY_DATABASE_URI = 'sqlite:///local.db'  # Local SQLite DB
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'your_secret_key'
