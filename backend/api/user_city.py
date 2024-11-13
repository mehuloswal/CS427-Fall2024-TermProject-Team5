# api/user_city.py
from flask import Blueprint, request, jsonify
from models import db, User, City, UserCity

user_city_bp = Blueprint('user_city', __name__)


@user_city_bp.route('/getCity', methods=['GET'])
def get_city():
    """
    Retrieves all cities associated with a specific user.

    Args:
        userEmail (str): The email address of the user (passed as query parameter)

    Returns:
        tuple: A tuple containing:
            - JSON array of city names associated with the user
            - HTTP status code 200 (OK) if successful
            - HTTP status code 404 (Not Found) if user doesn't exist

    Example response:
        ["Chicago", "New York", "San Francisco"]

    Raises:
        404: If the user with the provided email is not found
    """
    user_email = request.args.get('userEmail')
    user = User.query.filter_by(email=user_email).first()

    if not user:
        return jsonify({"error": "User not found"}), 404

    # Fetching all cities associated with the user
    user_cities = UserCity.query.filter_by(user_id=user.id).all()
    cities_list = []
    for user_city in user_cities:
        city = City.query.get(user_city.city_id)
        if city:
            city_data = {
                "city_name": city.city_name,
                "latitude": city.latitude,
                "longitude": city.longitude
            }
            cities_list.append(city_data)
    return jsonify(cities_list)


@user_city_bp.route('/addCity', methods=['POST'])
def add_city():
    """
    Associates a city with a user. Creates the city if it doesn't exist.

    Expects a JSON payload with the following structure:
    {
        "cityName": str,
        "userEmail": str
    }

    Returns:
        tuple: A tuple containing:
            - JSON response with success/error message
            - HTTP status code:
                200: City successfully added or already exists
                404: User not found

    Note:
        If the city doesn't exist in the database, it will be created with
        default latitude and longitude values (0.0, 0.0).
        If the city is already associated with the user, returns success
        without creating duplicate association.

    Raises:
        404: If the user with the provided email is not found
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
    """
    Removes the association between a user and a city.

    Expects a JSON payload with the following structure:
    {
        "cityName": str,
        "userEmail": str
    }

    Returns:
        tuple: A tuple containing:
            - JSON response with success/error message
            - HTTP status code:
                200: City successfully removed
                404: User not found, city not found, or city not associated with user

    Note:
        This endpoint only removes the association between the user and the city.
        It does not delete the city from the database.

    Raises:
        404: If the user is not found, the city is not found, or the city
             is not associated with the user
    """
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
