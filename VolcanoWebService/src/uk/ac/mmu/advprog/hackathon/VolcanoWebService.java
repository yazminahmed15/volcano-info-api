package uk.ac.mmu.advprog.hackathon;

import static spark.Spark.get;
import static spark.Spark.port;


import org.json.JSONArray;
import org.json.JSONObject;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Main web service class that sets up routes for the Volcano information API.
 * Handles HTTP requests and delegates database interactions to the DB class.
 * 
 * Routes:
 * - /test: Verifies service status.
 * - /country: Retrieves the number of volcanos in a specified country.
 * - /year: Retrieves eruption data within a specified year range.
 * - /location: Retrieves volcano data near specified coordinates and erupted since a given year.
 * 
 * @author Aqsa, Ahmed!
 */
public class VolcanoWebService {

	/**
	 * Main program entry point, starts the web service
	 * @param args not used
	 */
	public static void main(String[] args) {
		port(8088);	// Set the port to 8088.
		
		// Test route for verifying the web service status.
		get("/test", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					return 	"Number of volcanoes: " + db.getNumberOfVolcanoes() + 
							"<br>" +
							"Number of eruptions: " + db.getNumberOfEruptions();
				}
			}
		});
		
        // Route for getting the number of volcanos in a specified country.
        get("/country", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                String country = request.queryParams("search");
                if (country == null || country.trim().isEmpty()) {
                    return "Invalid Country";
                }

                try (DB db = new DB()) {
                    int count = db.getVolcanoesInCountry(java.net.URLDecoder.decode(country, "UTF-8"));
                    return String.valueOf(count);
                }
            }
        });
        
        
     // Route for retrieving eruption data within a year range
        get("/year", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                String fromYearStr = request.queryParams("from");
                String toYearStr = request.queryParams("to");

                // Validate input parameters
                if (fromYearStr == null || toYearStr == null) {
                    response.status(400);
                    return "Invalid year range";
                }

                try {
                    int fromYear = Integer.parseInt(fromYearStr);
                    int toYear = Integer.parseInt(toYearStr);

                    if (fromYear > toYear) {
                        response.status(400);
                        return "Invalid year range";
                    }

                try (DB db = new DB()) {
                        JSONArray jsonResponse = db.getEruptionsByYearRange(fromYear, toYear);
                        response.type("application/json");
                        return jsonResponse.length() > 0 ? jsonResponse.toString() : "[]";

                    }
                } catch (NumberFormatException e) {
                    response.status(400);
                    return "Invalid year range";
                }
            }
        });
        

        // Route to find volcanos near a location that erupted after a specific year.
        get("/location", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                String latitudeStr = request.queryParams("latitude");
                String longitudeStr = request.queryParams("longitude");
                String eruptedSinceStr = request.queryParams("erupted_since");

                // Validate parameters.
                if (latitudeStr == null || longitudeStr == null || eruptedSinceStr == null) {
                    response.status(400);
                    return "Invalid parameters";
                }

                try {
                    double latitude = Double.parseDouble(latitudeStr);
                    double longitude = Double.parseDouble(longitudeStr);
                    int eruptedSince = Integer.parseInt(eruptedSinceStr);

                    try (DB db = new DB()) {
                        JSONArray volcanoes = db.getVolcanoesByLocation(latitude, longitude, eruptedSince);

                        response.type("application/xml");
                        if (volcanoes.length() > 0) {
                            // Convert JSON to XML for the response.
                            StringBuilder xmlResponse = new StringBuilder();
                            xmlResponse.append("<Volcanoes>");
                            for (int i = 0; i < volcanoes.length(); i++) {
                                JSONObject volcano = volcanoes.getJSONObject(i);
                                xmlResponse.append("<Volcano id=\"").append(volcano.getInt("id")).append("\">")
                                           .append("<Name>").append(volcano.getString("name")).append("</Name>")
                                           .append("<LastErupted>").append(volcano.getString("lastErupted")).append("</LastErupted>")
                                           .append("<Type>").append(volcano.getString("type")).append("</Type>")
                                           .append("<Location>")
                                           .append("<Latitude>").append(volcano.getJSONObject("location").getDouble("latitude")).append("</Latitude>")
                                           .append("<Longitude>").append(volcano.getJSONObject("location").getDouble("longitude")).append("</Longitude>")
                                           .append("<Elevation>").append(volcano.getJSONObject("location").getInt("elevation")).append("</Elevation>")
                                           .append("<Country>").append(volcano.getJSONObject("location").getString("country")).append("</Country>")
                                           .append("</Location>")
                                           .append("</Volcano>");
                            }
                            xmlResponse.append("</Volcanoes>");
                            return xmlResponse.toString();
                        } else {
                            return "<Volcanoes></Volcanoes>";
                        }
                    }
                } catch (NumberFormatException e) {
                    response.status(400);
                    return "Invalid parameters";
                }
            }
        });
        
		
		System.out.println("Web Service Started. Don't forget to kill it when done testing!");
	}
	
}
