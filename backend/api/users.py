# api/users.py
from flask import Blueprint, request, jsonify
from models import db, User

users_bp = Blueprint('users', __name__)


@users_bp.route('/users', methods=['POST'])
def create_user():
    """
    Creates a new user in the database.

    Expects a JSON payload with the following structure:
    {
        "username": str,
        "email": str,
        "theme": bool (optional, defaults to True),
        "extended_theme": str (optional)
    }

    Returns:
        tuple: A tuple containing:
            - JSON response with success message
            - HTTP status code 201 (Created)

    Example response:
        {
            "message": "User created successfully"
        }
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
    Retrieves user details by their ID.

    Args:
        id (int): The unique identifier of the user to retrieve.

    Returns:
        tuple: A tuple containing:
            - JSON response with user details
            - HTTP status code 200 (OK) if found
            - HTTP status code 404 (Not Found) if user doesn't exist

    Example response:
        {
            "username": "john_doe",
            "email": "john@example.com",
            "theme": true,
            "extended_theme": "dark"
        }
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


# /users/userEmail='test1gmail.com'
@users_bp.route('/user/theme/', methods=['GET'])
def get_user_by_email():
    """
    Retrieves a user's theme preference by their email address.

    Args:
        userEmail (str): The email address of the user (passed as query parameter)

    Returns:
        tuple: A tuple containing:
            - JSON response with user's theme preference
            - HTTP status code 200 (OK) if found
            - HTTP status code 404 (Not Found) if user doesn't exist

    Example usage:
        GET /user/theme/?userEmail=test@gmail.com
    """
    email = request.args.get('userEmail')
    user = User.query.filter_by(email=email).first()
    if not user:
        return jsonify({"error": "User not found"}), 404
    return jsonify({
        "theme": user.theme,
    })


@users_bp.route('/updateUserTheme', methods=['POST'])
def update_user_theme():
    """
    Updates a user's theme preference.

    Args:
        userEmail (str): The email address of the user (passed as query parameter)
        theme (str): The new theme preference as string 'true' or 'false' (passed as query parameter)

    Returns:
        tuple: A tuple containing:
            - JSON response with success/error message
            - HTTP status code:
                200: Theme successfully updated
                404: User not found

    Example usage:
        POST /updateUserTheme?userEmail=test@gmail.com&theme=true

    Raises:
        404: If the user with the provided email is not found
    """
    email = request.args.get('userEmail')
    theme = request.args.get('theme') == 'true'  # Convert string to boolean
    user = User.query.filter_by(email=email).first()
    if not user:
        return jsonify({"error": "User not found"}), 404
    user.theme = theme
    db.session.commit()
    return jsonify({"message": "Theme updated successfully"}), 200