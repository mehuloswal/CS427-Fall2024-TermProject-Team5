# api/user_city.py
from flask import Blueprint, request, jsonify
from models import db, User, City, UserCity

user_city_bp = Blueprint('user_city', __name__)

@user_city_bp.route('/getCity', methods=['GET'])
def get_city():
    """
    Retrieve all cities associated with a user.

    Args:
        userEmail (str): The email address of the user (passed as URL parameter)

    Returns:
        tuple: A tuple containing:
            - JSON response with list of city names if successful
            - HTTP status code 200 if successful, 404 if user not found

    Raises:
        404: If the user is not found
    """
    user_email = request.args.get('userEmail')
    user = User.query.filter_by(email=user_email).first()

    if not user:
        return jsonify({"error": "User not found"}), 404

    # Fetching all cities associated with the user
    user_cities = UserCity.query.filter_by(user_id=user.id).all()
    city_names = [City.query.get(user_city.city_id).city_name for user_city in user_cities]
    
    return jsonify(city_names)

@user_city_bp.route('/addCity', methods=['POST'])
def add_city():
    """
    Add a new city to a user's list of cities.

    Args:
        JSON payload containing:
            - cityName (str): Name of the city to add
            - userEmail (str): Email address of the user

    Returns:
        tuple: A tuple containing:
            - JSON response with success/error message
            - HTTP status code:
                * 200: Success (city added or already exists)
                * 404: User not found

    Notes:
        - Creates a new city entry if it doesn't exist
        - Creates a new user-city relationship if it doesn't exist
        - Uses default coordinates (0.0, 0.0) for new cities
    """
    data = request.get_json()
    city_name = data.get('cityName')
    user_email = data.get('userEmail')

    # Check if user exists
    user = User.query.filter_by(email=user_email).first()
    if not user:
        return jsonify({"error": "User not found"}), 404

    # Check if city exists or create it if it doesn't
    city = City.query.filter_by(city_name=city_name).first()
    if not city:
        city = City(city_name=city_name, latitude=0.0, longitude=0.0)  # Default lat/lon for now
        db.session.add(city)
        db.session.commit()

    # Check if the user-city relationship already exists
    user_city = UserCity.query.filter_by(user_id=user.id, city_id=city.id).first()
    if user_city:
        return jsonify({"message": "City already added for the user"}), 200

    # Create new UserCity relationship
    new_user_city = UserCity(user_id=user.id, city_id=city.id)
    db.session.add(new_user_city)
    db.session.commit()

    return jsonify({"message": "City added successfully"}), 200

@user_city_bp.route('/removeCity', methods=['DELETE'])
def remove_city():
    data = request.get_json()
    city_name = data.get('cityName')
    user_email = data.get('userEmail')

    # Check if user exists
    user = User.query.filter_by(email=user_email).first()
    if not user:
        return jsonify({"error": "User not found"}), 404

    # Check if city exists
    city = City.query.filter_by(city_name=city_name).first()
    if not city:
        return jsonify({"error": "City not found"}), 404

    # Check if the user-city relationship exists
    user_city = UserCity.query.filter_by(user_id=user.id, city_id=city.id).first()
    if not user_city:
        return jsonify({"error": "City not associated with the user"}), 404

    # Remove the UserCity relationship
    db.session.delete(user_city)
    db.session.commit()

    return jsonify({"message": "City removed successfully"}), 200
