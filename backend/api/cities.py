# api/cities.py

from flask import Blueprint, request, jsonify
from models import db, City

cities_bp = Blueprint('cities', __name__)

@cities_bp.route('/cities', methods=['POST'])
def create_city():
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
    cities = City.query.all()
    city_names = [city.city_name for city in cities]
    return jsonify(city_names)
