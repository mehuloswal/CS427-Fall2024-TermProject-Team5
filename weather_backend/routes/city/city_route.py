from flask import request, jsonify, Blueprint
from controllers.city import city_controller


city_bp = Blueprint('city', __name__)

@city_bp.route('/city', methods=['GET'])
def get_city():
    return city_controller.get_city(request.get_json())

@city_bp.route('/city/weather', methods=['GET'])
def get_city_weather():
    return city_controller.get_weather(request.get_json())

@city_bp.route('/city/all', methods=['GET'])
def get_all_citys():
    return city_controller.get_user_cities(request.get_json())

@city_bp.route('/city', methods=['POST'])
def add_city():
    return city_controller.post_city(request.get_json())

@city_bp.route('/city', methods=['PUT'])
def update_city():
    return city_controller.update_city(request.get_json())

@city_bp.route('/city', methods=['DELETE'])
def delete_city():
    return city_controller.delete_city(request.get_json())

