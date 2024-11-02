# api/cities.py

from flask import Blueprint, request, jsonify
from models import db, City

cities_bp = Blueprint('cities', __name__)

@cities_bp.route('/cities', methods=['POST'])
def create_city():
    """Create a new city in the database.

    Expects a JSON payload with the following structure:
    {
        "city_name": str,
        "latitude": float,
        "longitude": float
    }

    Returns:
        tuple: JSON response with success message and 201 status code
        Example: ({"message": "City created successfully"}, 201)
    """
    data = request.json
    new_city = City(
        city_name=data['city_name'],
        latitude=data['latitude'],
        longitude=data['longitude']
    )
    db.session.add(new_city)
    db.session.commit()
    return jsonify({"message": "City created successfully"}), 201

@cities_bp.route('/cities/<int:id>', methods=['GET'])
def get_city(id):
    """Retrieve a specific city by its ID.

    Args:
        id (int): The unique identifier of the city

    Returns:
        tuple: JSON response with city data and status code
        Success example: ({"city_name": "Paris", "latitude": 48.8566, "longitude": 2.3522}, 200)
        Error example: ({"error": "City not found"}, 404)
    """
    city = City.query.get(id)
    if not city:
        return jsonify({"error": "City not found"}), 404
    return jsonify({
        "city_name": city.city_name,
        "latitude": city.latitude,
        "longitude": city.longitude
    })

# New route to get all cities
@cities_bp.route('/cities', methods=['GET'])
def get_all_cities():
    """Retrieve a list of all city names in the database.

    Returns:
        list: JSON response containing a list of city names
        Example: ["Paris", "London", "New York"]
    """
    cities = City.query.all()
    city_names = [city.city_name for city in cities]
    return jsonify(city_names)
