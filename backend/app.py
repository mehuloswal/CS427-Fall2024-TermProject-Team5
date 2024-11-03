# app.py
from flask import Flask
from db import init_db
from api import api_bp

app = Flask(__name__)
init_db(app)  # Initialize the database
app.register_blueprint(api_bp, url_prefix='/api')  # Register API Blueprint

if __name__ == '__main__':
    """
    Starts the Flask application server if this file is run directly.

    The server will:
    - Listen on all network interfaces (0.0.0.0)
    - Use port 5001
    - Run in debug mode

    Warning:
        Debug mode should be disabled in production environments.
    """
    app.run(host='0.0.0.0', port=5001,debug=True)
