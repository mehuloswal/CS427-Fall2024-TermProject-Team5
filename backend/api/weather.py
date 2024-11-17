import requests
from flask import Blueprint, request, jsonify
weather_bp = Blueprint('weather', __name__)
@weather_bp.route('/getWeather', methods=['GET'])
def get_weather():
    # Get latitude and longitude from request parameters
    latitude = request.args.get('lat')
    longitude = request.args.get('lon')
    api_key = 'c4901b8ee0c15b38415e52209061d2ca'
    if not latitude or not longitude:
        return jsonify({"error": "Latitude and longitude are required"}), 400
    # Construct the URL with latitude and longitude
    # url = f"https://api.openweathermap.org/data/2.5/weather?lat={latitude}&lon={longitude}&appid={c4901b8ee0c15b38415e52209061d2ca}&units=metric"
    url = f"https://api.openweathermap.org/data/2.5/weather?lat={latitude}&lon={longitude}&appid={api_key}&units=metric"
    response = requests.get(url)
    if response.status_code == 200:
        weather_data = response.json()
        result = {
            "city": weather_data.get("name"),
            "temperature": weather_data["main"]["temp"],
            "weather": weather_data["weather"][0]["description"],
            "humidity": weather_data["main"]["humidity"],
            "wind_speed": weather_data["wind"]["speed"],
            "datetime": weather_data.get("dt")
        }
        return jsonify(result)
    else:
        return jsonify({"error": "Failed to fetch weather data"}), 500