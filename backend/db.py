# db.py
from flask import Flask
from config import Config
from models import db

def init_db(app):
    """
    Initializes the database for the Flask application.

    This function sets up the SQLAlchemy database by:
    1. Loading configuration from Config object
    2. Initializing the database with the Flask app
    3. Creating all defined database tables if they don't exist

    Args:
        app (Flask): The Flask application instance to initialize the database for.

    Note:
        This function should be called once during application startup.
        It uses the application context to ensure database operations occur
        within the proper scope.

    Example usage:
        app = Flask(__name__)
        init_db(app)
    """
    app.config.from_object(Config)
    db.init_app(app)
    with app.app_context():
        db.create_all()  # Create tables
