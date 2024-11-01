# api/user_city.py
from flask import Blueprint, request, jsonify
from models import db, User, City, UserCity

user_city_bp = Blueprint('user_city', __name__)

@user_city_bp.route('/user_city', methods=['POST'])
def add_user_city():
    data = request.json
    user = User.query.filter_by(username=data['username']).first()
    city = City.query.filter_by(city_name=data['city_name']).first()

    if not user or not city:
        return jsonify({"error": "User or City not found"}), 404

    user_city = UserCity(user_id=user.id, city_id=city.id)
    db.session.add(user_city)
    db.session.commit()
    return jsonify({"message": "User-City relationship added"}), 201
