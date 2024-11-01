# db.py
from flask import Flask
from config import Config
from models import db

def init_db(app):
    app.config.from_object(Config)
    db.init_app(app)
    with app.app_context():
        db.create_all()  # Create tables
