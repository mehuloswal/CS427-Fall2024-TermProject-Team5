# scripts/uploadDataToDb.py

import csv
import sys
from os.path import dirname, abspath, join

# Add the project root to sys.path so that we can import app, db, and models
project_root = dirname(dirname(abspath(__file__)))
sys.path.append(project_root)

from models import db, City
from app import app

def upload_cities_to_db(csv_file_path):
    with app.app_context():
        # Open the CSV file
        with open(csv_file_path, mode='r') as file:
            csv_reader = csv.DictReader(file)
            
            for row in csv_reader:
                city_name = row['city']
                latitude = float(row['lat'])
                longitude = float(row['lng'])

                # Check if city already exists
                city = City.query.filter_by(city_name=city_name).first()
                if not city:
                    # Create a new City entry if it doesn't exist
                    city = City(city_name=city_name, latitude=latitude, longitude=longitude)
                    db.session.add(city)

            # Commit all changes to the database
            db.session.commit()
            print("Cities have been uploaded to the database successfully.")

# Path to your CSV file
csv_file_path = join(project_root, 'scripts', 'uscities.csv')

# Run the upload function
if __name__ == '__main__':
    upload_cities_to_db(csv_file_path)
