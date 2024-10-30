from flask import request, jsonify, Blueprint
from controllers.user import user_controller


user_bp = Blueprint('user', __name__)

@user_bp.route('/user', methods=['GET'])
def get_user():
    return user_controller.get_user(request.get_json())

@user_bp.route('/user/all', methods=['GET'])
def get_all_users():
    return user_controller.get_all_users()

@user_bp.route('/user/recipes', methods=['GET'])
def get_all_user_recipes():
    return user_controller.get_all_user_recipes(request.get_json())

@user_bp.route('/user', methods=['POST'])
def add_user():
    return user_controller.post_user(request.get_json())

@user_bp.route('/user', methods=['PUT'])
def update_user():
    return user_controller.update_user(request.get_json())

@user_bp.route('/user', methods=['DELETE'])
def delete_user():
    return user_controller.delete_user(request.get_json())

