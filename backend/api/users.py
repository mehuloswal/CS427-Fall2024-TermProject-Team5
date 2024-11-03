# api/users.py
from flask import Blueprint, request, jsonify
from models import db, User

users_bp = Blueprint('users', __name__)

@users_bp.route('/users', methods=['POST'])
def create_user():
    """
    Create a new user in the database.
    
    Expects a JSON payload with the following fields:
    - username: str (required)
    - email: str (required)
    - theme: bool (optional, defaults to True)
    - extended_theme: Any (optional)
    
    Returns:
        tuple: (JSON response, HTTP status code)
        - Success: ({"message": "User created successfully"}, 201)
    """
    data = request.json
    new_user = User(
        username=data['username'],
        email=data['email'],
        theme=data.get('theme', True),
        extended_theme=data.get('extended_theme')
    )
    db.session.add(new_user)
    db.session.commit()
    return jsonify({"message": "User created successfully"}), 201

@users_bp.route('/users/<int:id>', methods=['GET'])
def get_user(id):
    """
    Retrieve a user by their ID.
    
    Args:
        id (int): The user's ID
    
    Returns:
        tuple: (JSON response, HTTP status code)
        - Success: ({"username": str, "email": str, "theme": bool, "extended_theme": Any}, 200)
        - Error: ({"error": "User not found"}, 404)
    """
    user = User.query.get(id)
    if not user:
        return jsonify({"error": "User not found"}), 404
    return jsonify({
        "username": user.username,
        "email": user.email,
        "theme": user.theme,
        "extended_theme": user.extended_theme
    })
