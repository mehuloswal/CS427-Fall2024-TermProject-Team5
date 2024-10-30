from flask import jsonify
from pony.orm import *
from models import data_model

def delete_tables(data):
	if 'key' not in data: return jsonify({"error":"no key present"}), 400
	if data['key'] != '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': return jsonify({"error"}), 400

	db = Database()
	db.bind(provider='sqlite', filename='../database.sqlite', create_db=True)
	db.generate_mapping(create_tables=True)

	db.drop_all_tables(with_all_data=True) #("Recipe",if_exists=True,with_all_data=False)
	commit()

	return jsonify({"message":"deleted tables"}), 200