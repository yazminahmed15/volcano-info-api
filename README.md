🌋 Volcano Info API
A Java microservice that provides information about volcanoes and eruptions.

📖 Overview
The Volcano Info API is a RESTful web service that allows users to retrieve data about volcanoes and their eruptions. It is built using the Spark Java microservice framework and integrates with an SQLite database to store and retrieve geological data.

This project was developed as part of a hackathon assessment, focusing on Java microservices, JSON/XML parsing, and database handling.

🚀 Features
✅ Retrieve a list of all volcanoes
✅ Get detailed information about a specific volcano
✅ Retrieve eruption history
✅ Supports JSON & XML responses
✅ Built using Spark Java, SQLite, and RESTful principles

🛠️ Technologies Used
Java (Backend logic)
Spark Java (Lightweight microservice framework)
SQLite (Database for storing volcano data)
JDBC (SQLite Driver) (Database connection)
Reference JSON Parser (Handles JSON responses)
Java DOM XML Parser (Handles XML responses)

📂 Project Structure

volcano-info-api/
│── src/
│   ├── Main.java               # Entry point of the API
│   ├── DatabaseManager.java     # Handles SQLite database operations
│   ├── Volcano.java             # Model class for volcanoes
│   ├── ApiRoutes.java           # Defines API routes and handlers
│── database/
│   ├── volcanoes.db             # SQLite database file
│── README.md                    # Project documentation
│── pom.xml (if using Maven)      # Dependencies (if applicable)

📌 API Endpoints
Method	  Endpoint	         Description
GET	   /volcanoes	          Retrieve a list of all volcanoes
GET	   /volcanoes/{id}	  Get detailed information about a specific volcano
GET	   /eruptions	             Retrieve a list of all eruptions
GET	  /eruptions/{volcanoId}	  Get eruption history for a specific volcano

Example JSON Response:

{
  "id": 1,
  "name": "Mount St. Helens",
  "location": "Washington, USA",
  "last_eruption": "1980-05-18",
  "status": "Active"
}

Example XML Response:

<volcano>
    <id>1</id>
    <name>Mount St. Helens</name>
    <location>Washington, USA</location>
    <last_eruption>1980-05-18</last_eruption>
    <status>Active</status>
</volcano>

⚡ How to Run the API
1️⃣ Clone the Repository

git clone https://github.com/yourusername/volcano-info-api.git
cd volcano-info-api

2️⃣ Set Up the Database
Ensure the SQLite database (volcanoes.db) is present in the database/ directory. If missing, create it using the provided SQL schema.

3️⃣ Run the API
Compile and run the application:

javac -cp ".:lib/*" src/Main.java
java -cp ".:lib/*" Main

Or if using Maven:

mvn clean install
mvn exec:java -Dexec.mainClass="Main"

4️⃣ Test the API
Use Postman or a browser to make API requests:

http://localhost:4567/volcanoes
http://localhost:4567/volcanoes/1
🛠️ Future Improvements
🔹 Add authentication and API keys for access control
🔹 Implement a front-end UI for visualization
🔹 Optimize database queries for better performance
🔹 Expand dataset with more geological data

📜 License
This project is licensed under the MIT License.

⭐ Like this project? Give it a star on GitHub!
🚀 Happy coding! 😊
