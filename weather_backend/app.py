from flask import Flask

from routes.city.city_route import city_bp
from routes.user.user_route import user_bp
from routes.deletion.deletion_route import deletion_bp

app = Flask(__name__)

app.register_blueprint(city_bp)
app.register_blueprint(user_bp)
app.register_blueprint(deletion_bp)

if __name__ == '__main__':
    app.run(debug=True)
"""
# PROD
if __name__ == '__main__':
    # When running the app locally (not using Gunicorn), use this block
    # Gunicorn will directly run 'app' without calling this block
    app.run(host='0.0.0.0', port=8000, debug=False)
"""