from pony.orm import *
from decimal import Decimal

db = Database()

class User(db.Entity):
    user = Required(str, unique=True)
    password = Required(str, unique=True)
    cities = Set('City')
    

class City(db.Entity):
    user = Required(User)
    cityName = Required(str)
    stateCode = Optional(str, nullable=True)
    countryCode = Required(str)
    latitude = Required(Decimal)
    longitude = Required(Decimal)

db.bind(provider='sqlite', filename='../database.sqlite', create_db=True)
db.generate_mapping(create_tables=True)
