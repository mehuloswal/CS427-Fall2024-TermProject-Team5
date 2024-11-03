import sqlite3

# Connect to the SQLite database (or create it if it doesn't exist)
conn = sqlite3.connect('instance/local.db')
cursor = conn.cursor()

# Example: Create a table
cursor.execute('''
DELETE FROM users;
''')

cursor.execute('''
DELETE FROM user_city;
''')

conn.commit()  # Commit changes to the database

# Close the connection
conn.close()