package uk.ac.mmu.advprog.hackathon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Handles database operations for volcano and eruption data.
 * 
 * This class uses prepared statements to prevent SQL injection and implements
 * AutoCloseable for automatic resource management.
 * 
 * @author Aqsa, Ahmed!
 */
public class DB implements AutoCloseable {

	// allows us to easily change the database used
	private static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:./data/volcanoes.db";

	// allows us to re-use the connection between queries if desired
	private Connection connection = null;

	/**
	 * Creates an instance of the DB object and connects to the database
	 */
	public DB() {
		try {
			connection = DriverManager.getConnection(JDBC_CONNECTION_STRING);
		} catch (SQLException sqle) {
			error(sqle);
		}
	}

    /**
     * Retrieves the total number of volcanos in the database.
     * @return Number of volcanos, or -1 if an error occurs.
     */
	public int getNumberOfVolcanoes() {
		int result = -1;
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM volcanoes");
			while (results.next()) { // will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("count"));
			}
		} catch (SQLException sqle) {
			error(sqle);

		}
		return result;
	}

	/**
	 * Returns the number of eruptions in the database, by counting rows
	 * @return The number of eruptions in the database, or -1 if empty
	 */
	public int getNumberOfEruptions() {
		int result = -1;
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM eruptions");
			while (results.next()) { // will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("count"));
			}
		} catch (SQLException sqle) {
			error(sqle);

		}
		return result;
	}
	
	
	
	/**
	 * Retrieves the number of volcanos in a specified country.
	 *
	 * @param country The name of the country to search for volcanos.
	 * @return The number of volcanos in the specified country, or 0 if none are found.
	 */
	public int getVolcanoesInCountry(String country) {
		int count = 0;
		String query = "SELECT COUNT(*) AS count FROM Volcanoes WHERE Country = ?";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, country);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt("count");
			}
		} catch (SQLException sqle) {
			error(sqle);
		}
		return count;
	}
	
	
	/**
	 * Retrieves eruptions that occurred within a specified range of years.
	 *
	 * @param fromYear The start year of the range.
	 * @param toYear   The end year of the range.
	 * @return A JSON array containing eruption details within the specified range.
	 */
	public JSONArray getEruptionsByYearRange(int fromYear, int toYear) {
	    String query = "SELECT * " +
	                   "FROM Eruptions " +
	                   "INNER JOIN Volcanoes ON Eruptions.Volcano_ID = Volcanoes.ID " +
	                   "WHERE CAST(substr(Date, 1, 4) AS INTEGER) >= ? " +
	                   "AND CAST(substr(Date, 1, 4) AS INTEGER) <= ? " +
	                   "ORDER BY Date ASC";

	    JSONArray jsonArray = new JSONArray();

	    try (PreparedStatement ps = connection.prepareStatement(query)) {
	        ps.setInt(1, fromYear);
	        ps.setInt(2, toYear);

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                JSONObject jsonObject = new JSONObject();
	                jsonObject.put("date", rs.getString("Date") != null ? rs.getString("Date") : "");
	                jsonObject.put("name", rs.getString("Name") != null ? rs.getString("Name") : "");

	                JSONObject locationObject = new JSONObject();
	                locationObject.put("latitude", rs.getObject("Latitude") != null ? rs.getDouble("Latitude") : 0.0);
	                locationObject.put("longitude", rs.getObject("Longitude") != null ? rs.getDouble("Longitude") : 0.0);
	                locationObject.put("elevation", rs.getObject("Elevation") != null ? rs.getInt("Elevation") : 0);
	                locationObject.put("country", rs.getString("Country") != null ? rs.getString("Country") : "");

	                jsonObject.put("location", locationObject);

	                jsonObject.put("deaths", rs.getObject("Deaths") != null ? rs.getInt("Deaths") : 0);
	                jsonObject.put("missing", rs.getObject("Missing") != null ? rs.getInt("Missing") : 0);
	                jsonObject.put("injuries", rs.getObject("Injuries") != null ? rs.getInt("Injuries") : 0);

	                jsonArray.put(jsonObject);
	            }
	        }
	    } catch (SQLException sqle) {
	        error(sqle);
	    }

	    return jsonArray;
	}
	
	
	/**
	 * Retrieves volcanos near a given location that erupted since a specified year.
	 *
	 * @param latitude     The latitude of the location.
	 * @param longitude    The longitude of the location.
	 * @param eruptedSince The year from which to search for eruptions.
	 * @return A JSON array containing details of nearby volcanos and their eruptions.
	 */
	
	public JSONArray getVolcanoesByLocation(double latitude, double longitude, int eruptedSince) {
	    String query = "SELECT " +
	                   "MAX(Date) AS Last_Erupted, " +
	                   "Volcanoes.ID AS Volcano_ID, Name, Country, Latitude, Longitude, Elevation, Type " +
	                   "FROM Eruptions " +
	                   "INNER JOIN Volcanoes ON Eruptions.Volcano_ID = Volcanoes.ID " +
	                   "WHERE CAST(substr(Date, 1, 4) AS INTEGER) >= ? " +
	                   "GROUP BY Volcanoes.ID " +
	                   "ORDER BY " +
	                   "( ((? - Latitude) * (? - Latitude)) + " +
	                   "(COS(RADIANS(?)) * ((? - Longitude) * (? - Longitude)))) ASC " +
	                   "LIMIT 10";

	    JSONArray resultArray = new JSONArray();

	    try (PreparedStatement ps = connection.prepareStatement(query)) {
	        ps.setInt(1, eruptedSince);
	        ps.setDouble(2, latitude);
	        ps.setDouble(3, latitude);
	        ps.setDouble(4, latitude);
	        ps.setDouble(5, longitude);
	        ps.setDouble(6, longitude);

	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                JSONObject volcanoObject = new JSONObject();
	                volcanoObject.put("id", rs.getInt("Volcano_ID"));
	                volcanoObject.put("name", rs.getString("Name"));
	                volcanoObject.put("lastErupted", rs.getString("Last_Erupted") != null ? rs.getString("Last_Erupted") : "");
	                volcanoObject.put("type", rs.getString("Type"));

	                JSONObject locationObject = new JSONObject();
	                locationObject.put("latitude", rs.getDouble("Latitude"));
	                locationObject.put("longitude", rs.getDouble("Longitude"));
	                locationObject.put("elevation", rs.getInt("Elevation"));
	                locationObject.put("country", rs.getString("Country"));

	                volcanoObject.put("location", locationObject);

	                resultArray.put(volcanoObject);
	            }
	        }
	    } catch (SQLException sqle) {
	        error(sqle);
	    }

	    return resultArray;
	}


	/**
	 * Closes the connection to the database, required by AutoCloseable interface.
	 */
	@Override
	public void close() {
		try {
			if (!connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException sqle) {
			error(sqle);
		}
	}

	/**
	 * Prints out the details of the SQL error that has occurred, and exits the
	 * programme
	 * 
	 * @param sqle Exception representing the error that occurred
	 */
	private void error(SQLException sqle) {
		System.err.println("Problem Accessing Database! " + sqle.getClass().getName());
		sqle.printStackTrace();
		System.exit(1);
	}

}
