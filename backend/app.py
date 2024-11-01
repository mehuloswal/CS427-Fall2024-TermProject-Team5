# app.py
from flask import Flask
from db import init_db
from api import api_bp

app = Flask(__name__)
init_db(app)  # Initialize the database
app.register_blueprint(api_bp, url_prefix='/api')  # Register API Blueprint

if __name__ == '__main__':
    app.run(debug=True)
