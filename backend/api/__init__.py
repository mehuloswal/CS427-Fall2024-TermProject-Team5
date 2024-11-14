# api/__init__.py
from flask import Blueprint
from .users import users_bp
from .cities import cities_bp
from .user_city import user_city_bp
from .weather import weather_bp

api_bp = Blueprint('api', __name__)
api_bp.register_blueprint(users_bp)
api_bp.register_blueprint(cities_bp)
api_bp.register_blueprint(user_city_bp)
api_bp.register_blueprint(weather_bp)  # Register weather with /weather prefix

