# api/cities.py

from flask import Blueprint, request, jsonify
from models import db, City

cities_bp = Blueprint('cities', __name__)


@cities_bp.route('/cities', methods=['POST'])
def create_city():
    """
    Adds a new city to the database for a given user.

    Parameters:
    city_name (str): The name of the city to be added.
    user_id (int): The unique identifier for the user.

    Returns:
    bool: True if the city was successfully added, False otherwise.
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
    """
    Retrieves details of a specific city from the database.

    Args:
        id (int): The unique identifier of the city to retrieve.

    Returns:
        tuple: A tuple containing:
            - JSON response with city details (city_name, latitude, longitude)
            - HTTP status code 200 (OK) if found
            - HTTP status code 404 (Not Found) if city doesn't exist

    Example response:
        {
            "city_name": "Chicago",
            "latitude": 41.8781,
            "longitude": -87.6298
        }
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
    """
    Retrieves a list of all city names from the database.

    Returns:
        tuple: A tuple containing:
            - JSON array of city names
            - HTTP status code 200 (OK)

    Example response:
        ["Chicago", "New York", "Los Angeles"]

    Note:
        This endpoint returns only the names of the cities, not their full details.
        For detailed information about a specific city, use the /cities/<id> endpoint.
    """
    cities = City.query.all()
    city_names = [city.city_name for city in cities]
    return jsonify(city_names)
