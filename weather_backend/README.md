# Recipe storage backend
This is a simple Flask application for storing recipes and their ingredients. It uses Pony ORM for database interactions and SQLite as the database engine. The application allows users to add, view, and delete recipes, which are stored in a relational database.

## Project Structure
The project is organized into several folders to keep the code modular and maintainable:


## Structure
```
project/
├── app.py                 # Main entry point for running the Flask app
├── models/
│   └── models.py          # Defines the database entities using Pony ORM
├── controllers/
│   └── recipe_controller.py  # Contains business logic for handling recipes
├── routes/
│   └── recipe_routes.py   # Defines the Flask routes (endpoints) for the application
├── requirements.txt
└── README.md             # Project documentation
```
## Folder Overview
app.py: The main entry point of the application. This script initializes the Flask app and sets up the routes.
models/: Contains the database models defined using Pony ORM. The models are mapped to the SQLite database.
controllers/: Contains business logic for handling recipe-related operations, such as adding, retrieving, and deleting recipes.
routes/: Defines the Flask routes where the HTTP endpoints are specified, mapping URLs to controller functions.

## Getting Started
Prerequisites
Python 3.x
Flask
Pony ORM
You can install the required dependencies using pip:

Setting Up the Project
```
pip install -r requirements.txt
git clone https://github.com/yourusername/recipe-storage-flask.git
cd recipe-storage-flask
```
Set Up the Database

The project uses SQLite as the database engine. The database will be automatically created and initialized when the app runs for the first time.

Run the Application

Start the Flask application by running:

```
python app.py
```
The app should now be running at http://127.0.0.1:5000/.
