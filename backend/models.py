# models.py
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class User(db.Model):
    """
    User model representing application users.
    
    Attributes:
        id (int): Primary key for the user
        username (str): Unique username, max length 50 characters
        email (str): Unique email address, max length 120 characters
        theme (bool): User's theme preference, defaults to True
        extended_theme (str): Additional theme settings, optional
        cities (relationship): Relationship to UserCity model
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
    City model representing geographic locations.
    
    Attributes:
        id (int): Primary key for the city
        city_name (str): Unique city name, max length 50 characters
        latitude (float): Geographic latitude of the city
        longitude (float): Geographic longitude of the city
        users (relationship): Relationship to UserCity model
    """
    __tablename__ = 'cities'
    id = db.Column(db.Integer, primary_key=True)
    city_name = db.Column(db.String(50), unique=True, nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)

    users = db.relationship('UserCity', back_populates='city')

class UserCity(db.Model):
    """
    Association model linking Users and Cities in a many-to-many relationship.
    
    Attributes:
        id (int): Primary key for the association
        user_id (int): Foreign key referencing users table
        city_id (int): Foreign key referencing cities table
        user (relationship): Relationship to User model
        city (relationship): Relationship to City model
    """
    __tablename__ = 'user_city'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    city_id = db.Column(db.Integer, db.ForeignKey('cities.id'), nullable=False)

    user = db.relationship('User', back_populates='cities')
    city = db.relationship('City', back_populates='users')
