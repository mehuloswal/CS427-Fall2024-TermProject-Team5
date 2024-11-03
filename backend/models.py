# models.py
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class User(db.Model):
    """
    Database model representing a user in the system.

    Attributes:
        id (int): Primary key for the user.
        username (str): Unique username, maximum 50 characters.
        email (str): Unique email address, maximum 120 characters.
        theme (bool): User's theme preference, defaults to True (light theme).
        extended_theme (str): Additional theme settings, nullable, maximum 100 characters.
        cities (relationship): One-to-many relationship with UserCity model.

    Relationships:
        cities: Relationship to UserCity model, allowing access to user's associated cities.

    Note:
        The theme attribute is used to toggle between light (True) and dark (False) modes.
        The extended_theme attribute can store additional theme-related preferences.
    """
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    theme = db.Column(db.Boolean, default=True)
    extended_theme = db.Column(db.String(100), nullable=True)

    cities = db.relationship('UserCity', back_populates='user')


class City(db.Model):
    """
    Database model representing a city in the system.

    Attributes:
        id (int): Primary key for the city.
        city_name (str): Unique name of the city, maximum 50 characters.
        latitude (float): Geographic latitude of the city.
        longitude (float): Geographic longitude of the city.
        users (relationship): One-to-many relationship with UserCity model.

    Relationships:
        users: Relationship to UserCity model, allowing access to users associated with the city.

    Note:
        The latitude and longitude attributes store the geographic coordinates
        of the city for potential use in mapping or distance calculations.
    """
    __tablename__ = 'cities'
    id = db.Column(db.Integer, primary_key=True)
    city_name = db.Column(db.String(50), unique=True, nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)

    users = db.relationship('UserCity', back_populates='city')


class UserCity(db.Model):
    """
    Database model representing the many-to-many relationship between users and cities.

    Attributes:
        id (int): Primary key for the user-city relationship.
        user_id (int): Foreign key referencing the users table.
        city_id (int): Foreign key referencing the cities table.
        user (relationship): Relationship to the User model.
        city (relationship): Relationship to the City model.

    Relationships:
        user: Back reference to the User model.
        city: Back reference to the City model.

    Note:
        This is a junction table that enables many-to-many relationships between
        users and cities, allowing each user to have multiple cities and each
        city to be associated with multiple users.
    """
    __tablename__ = 'user_city'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    city_id = db.Column(db.Integer, db.ForeignKey('cities.id'), nullable=False)

    user = db.relationship('User', back_populates='cities')
    city = db.relationship('City', back_populates='users')
