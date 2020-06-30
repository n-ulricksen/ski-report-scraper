package scraper;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

import config.Keys;
import db.DBCommon;

public class Weather implements Runnable {
	private final static String MAPS_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
	private final static String WEATHER_URL = "https://api.darksky.net/forecast";
	private final static String COLLECTION = "weather";
	
	private Thread thread = null;
	private String zipcode;
	
	public Weather(String zipcode) {
		this.zipcode = zipcode;
		
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	@Override
	public void run() {
		getWeather(this.zipcode);
	}

	protected static void getWeather(String zipcode) {
		Map<String, String> data = new HashMap<String, String>();
		String latLong = getLatLong(zipcode);
		
		String darkSkyUrl = WEATHER_URL + "/" + Keys.getDarkSkyKey() + "/" + latLong;
		String weatherResponse = getHttpData(darkSkyUrl);
		
		System.out.println(weatherResponse);
		JSONObject weatherJson = new JSONObject(weatherResponse);

		JSONObject dailyData = weatherJson.getJSONObject("daily").getJSONArray("data").getJSONObject(0);
		String temp = Float.toString(weatherJson.getJSONObject("currently").getFloat("temperature"));
		String tempHigh = Float.toString(dailyData.getFloat("temperatureHigh"));
		String tempLow = Float.toString(dailyData.getFloat("temperatureLow"));
		data.put("zipCode", zipcode);
		data.put("currentTemp", temp);
		data.put("highTemp", tempHigh);
		data.put("lowTemp", tempLow);
		
		storeData(zipcode, data);
	}
	
	private static String getLatLong(String zipcode) {
		String latLong = "";
		
		String uri = MAPS_URL + zipcode + "&key=" + Keys.getGoogleMapsKey();
		String contentString = getHttpData(uri);
		
		// Parse the data - extract latitude and longitude
		JSONObject json = new JSONObject(contentString);
		JSONObject location = json.getJSONArray("results").getJSONObject(0)
				.getJSONObject("geometry").getJSONObject("location");
		
		float latitude = location.getFloat("lat");
		float longitude = location.getFloat("lng");
		latLong = Float.toString(latitude) + "," + Float.toString(longitude);
	
		return latLong;
	}
	
	private static String getHttpData(String uri) {
		String data = "";
		HttpURLConnection conn;
		
		try {
			URL url = new URL(uri);
			conn = (HttpURLConnection) url.openConnection();
			
			// Set connection attributes
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			
			Reader streamReader = null;

			// Check if request was successful
			int status = conn.getResponseCode();
			if (status > 299) {
				streamReader = new InputStreamReader(conn.getErrorStream());
			} else {
				streamReader = new InputStreamReader(conn.getInputStream());
			}
			
			// Parse the request, build a StringBuffer containing information in response
			BufferedReader in = new BufferedReader(streamReader);
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			
			// Close the connection
			in.close();
			conn.disconnect();
			
			data = content.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}

	private static void storeData(String title, Map<String, String> data) {
		// Store data in MongoDB
		DBCommon dbCommon = new DBCommon();
		
		org.bson.Document dbDoc = new org.bson.Document();
		for (String key : data.keySet()) {
			String value = data.get(key);
			dbDoc.append(key, value);
		}		
		dbDoc.append("lastUpdated", new Date());
		
		dbCommon.insertDocument(dbDoc, COLLECTION);
	}
}
