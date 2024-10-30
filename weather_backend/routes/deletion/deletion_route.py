from flask import request, jsonify, Blueprint
from controllers.deletion import deletion_controller


deletion_bp = Blueprint('deletion', __name__)

@deletion_bp.route('/deletion', methods=['DELETE'])
def delete_tables():
    return deletion_controller.delete_tables(request.get_json())

