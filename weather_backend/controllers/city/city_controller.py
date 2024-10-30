from flask import jsonify, request
from pony.orm import *
from models import data_model
import asyncio
import os
from dotenv import load_dotenv
import requests

@db_session
def get_city(data):

    required = ["user","password","cityName","countryCode"]
    inside = []
    for elems in required:
        if elems not in data:
            inside.append(elems)
    if inside: return jsonify({"error": ' and '.join(inside) + " required"}), 400

    user = data.get('user')
    password = data.get('password')
    cityName = data.get('cityName')
    countryCode = data.get('countryCode')
    
    user_data = select(users for users in data_model.User if users.user == user and users.password == password)[:]
    if not user_data: return jsonify({"error": "City not found"}), 404
    
    data = [(city.cityName,city.countryCode,city.latitude,city.longitude) for city in user_data[0].cities if city.cityName == cityName]
    
    if not data: return jsonify({"error": "City not found"}), 404
    print(data)
    return jsonify({"cities":data}), 200

@db_session
def get_user_cities(data):

    required = ["user","password","cityName","countryCode"]
    inside = []
    for elems in required:
        if elems not in data:
            inside.append(elems)
    if inside: return jsonify({"error": ' and '.join(inside) + " required"}), 400

    user = data.get('user')
    password = data.get('password')
    cityName = data.get('cityName')
    countryCode = data.get('countryCode')
    
    user_data = select(users for users in data_model.User if users.user == user and users.password == password)[:]
    if not user_data: return jsonify({"error": "City not found"}), 404
    
    data = [(city.cityName,city.countryCode,city.latitude,city.longitude) for city in user_data[0].cities]
    
    if not data: return jsonify({"error": "City not found"}), 404
    print(data)
    return jsonify({"cities":data}), 200

@db_session
def get_weather(data):

    required = ["user","password","cityName","countryCode"]
    inside = []
    for elems in required:
        if elems not in data:
            inside.append(elems)
    if inside: return jsonify({"error": ' and '.join(inside) + " required"}), 400

    user = data.get('user')
    password = data.get('password')
    cityName = data.get('cityName')
    countryCode = data.get('countryCode')
    
    user_data = select(users for users in data_model.User if users.user == user and users.password == password)[:]
    if not user_data: return jsonify({"error": "City not found"}), 404
    
    data = [city for city in user_data[0].cities if city.cityName==cityName]
    
    if not data: return jsonify({"error": "City not found"}), 404
    
    load_dotenv()
    API_key = os.getenv('WEATHER_API_KEY')
    weather_data = asyncio.run(get_weather_data(data[0].latitude, data[0].longitude, API_key=API_key))
    
    return jsonify({"data":{"city_name":cityName,"weather_data":weather_data}}), 200
    
@db_session
def post_city(data):

    required = ["user","password","cityName","countryCode"]
    inside = []
    for elems in required:
        if elems not in data:
            inside.append(elems)
    if inside: return jsonify({"error": ' and '.join(inside) + " required"}), 400

    user = data.get('user')
    password = data.get('password')
    cityName = data.get('cityName')
    countryCode = data.get('countryCode')
    
    user_data = select(users for users in data_model.User if users.user == user)[:]
    print(user_data[0].cities)
    city_data = [city for city in user_data[0].cities if city.cityName==cityName]
    print(city_data)
    if city_data: return jsonify({"error": "City already exists"}), 400
    if not data_model.User.get(user=user,password=password):
        return jsonify({"error": "User does not exist"}), 400
    
    load_dotenv()
    API_key = os.getenv('WEATHER_API_KEY')
    
    geo_data = asyncio.run(get_city_data(city_name=cityName, state_code=None, country_code=countryCode, limit=1, API_key=API_key))
    if not geo_data: return jsonify({"error": "Weather API city geo_data endpoint error"}), 400
    print(cityName, countryCode, geo_data)
    city = data_model.City(user=data_model.User.get(user=user),cityName=cityName,countryCode=countryCode, latitude=geo_data['lat'], longitude=geo_data['lon'])
    user = data_model.User.get(user=user,password=password)
    user.cities.add(city)
    commit()
    return jsonify({"message": f"{cityName} created for {user.user}"}), 200

@db_session
def update_city(data):

    required = ["user","name","ingredients","country"]
    inside = []
    for elems in required:
        if elems not in data:
            inside.append(elems)
    if inside: return jsonify({"error": ''.join(inside) + " required"}), 400

    name = data.get('name')
    user = data.get('user')
    ingredients = data.get('ingredients')
    country = data.get('country')

    recipes = select(recipe for recipe in data_model.Recipe if recipe.name == name and recipe.user.user == user)[:]
    print(recipes)
    if not recipes:
        return jsonify({"error": "Recipe and User does not exists"}), 400
    if not data_model.Creator.get(user=user):
        return jsonify({"error": "User does not exist"}), 400
    recipe = data_model.Recipe.get(user=data_model.Creator.get(user=user),name=name)
    recipe.ingredients, recipe.country = ingredients, country
    commit()
    return jsonify({"message": f"Recipe for {name} created by {user} updated"}), 200

@db_session
def delete_city(data):

    required = ["user","password"]
    inside = []
    for elems in required:
        if elems not in data:
            inside.append(elems)
    if inside: return jsonify({"error": ''.join(inside) + " required"}), 400

    user = data.get('user')
    password = data.get('password')
    cityName = data.get('cityName')
    countryCode = data.get('countryCode')
    latitude = data.get('latitude')
    longitude = data.get('longitude')
    
    recipes = select(users for users in data_model.User if users.password == password and users.user == user)[:]
    if not recipes:
        return jsonify({"error": "Recipe and User does not exist in database"}), 400

    city = data_model.City.get(user=data_model.User.get(user=user),cityName=cityName,countryCode=countryCode)
    print(city)
    city.delete()
    
    commit()
    return jsonify({"message": f"City {cityName} created by {user} deleted"}), 200

@db_session
def get_all_recipes():

    data = select(recipe for recipe in data_model.Recipe)[:]
    data = [(recipe.name, recipe.user.user, recipe.ingredients, recipe.country) for recipe in data]
    return jsonify({"data":data}), 200

async def get_city_data(city_name, state_code, country_code, limit, API_key):
    if not state_code: state_code=''
    api_url = "http://api.openweathermap.org/geo/1.0/direct?q={city_name},{state_code},{country_code}&limit={limit}&appid={API_key}".format(city_name=city_name, state_code=state_code, country_code=country_code, limit=limit, API_key=API_key)
    data = requests.get(api_url) # {'lat':5, 'lon':5}
    data = data.json()
    if not data: return None
    return {'lat':data[0].get('lat'), 'lon':data[0].get('lon')}
    
async def get_weather_data(lat, lon, API_key):
    #data = await get_city_data(None,None,None,None,None)

    api_url = "https://api.openweathermap.org/data/3.0/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API_key}".format(lat=lat,lon=lon,part="minutely,hourly,daily,alerts",API_key=API_key)
    
    data = requests.get(api_url)
    data = data.json()
    print(data, lat, lon)
    current_data = data.get('current')
    
    return current_data

