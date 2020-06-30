package scraper;

import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import db.DBCommon;

public class ResortDataScraper extends WebPageScraper {

	// other URL is same, but ends with /skireport.html
	private final static String URL = "https://www.onthesnow.com/california/";
	private final static String COLLECTION = "resortsdata";
	private String resort = "";
	private String lookup = "";

	public ResortDataScraper(String resort, String lookup) {
		super();
		
		this.resort = resort;
		this.lookup = lookup;
	}
	
	protected Document createDocFromConnection() throws IOException {
		Response response = 
				Jsoup.connect(URL + lookup + "/" + "skireport.html")
				.method(Method.GET)
				.execute();
		
		Document doc = response.parse();
		
		return doc;
	}

	protected Map<String, String> extractDataFromDoc(Document doc) {
		Map<String, String> data = new HashMap<String, String>();
		
		// todaySnowfall (.predicted_snowfall)
		Elements todaySnowfallElements = doc.select(".today .predicted_snowfall");
		String todaySnowfall = todaySnowfallElements.first().text();
		data.put("todaySnowfall", todaySnowfall);
		
		// upperSnowDepth (.elevation.upper > bluePill)
		Elements upperDepthElements = doc.select(".elevation.upper .bluePill");
		String upperDepth;
		if (upperDepthElements.isEmpty()) {
			upperDepth = "";
		} else {
			upperDepth = upperDepthElements.first().text();
		}
		data.put("upperDepth", upperDepth);
		
		// middleSnowDepth (.elevation.middle > bluePill)
		Elements middleDepthElements = doc.select(".elevation.middle .bluePill");
		String middleDepth;
		if (middleDepthElements.isEmpty()) {
			middleDepth = "";
		} else {
			middleDepth = middleDepthElements.first().text();
		}
		data.put("middleDepth", middleDepth);
		
		// lowerSnowDepth (.elevation.lower > bluePill)
		Elements lowerDepthElements = doc.select(".elevation.lower .bluePill");
		String lowerDepth;
		if (lowerDepthElements.isEmpty()) {
			lowerDepth = "";
		} else {
			lowerDepth = lowerDepthElements.first().text();
		}
		data.put("lowerDepth", lowerDepth);
		
		return data;
	}

	protected void storeData(Map<String, String> data) {
		// Store data in MongoDB
		DBCommon dbCommon = new DBCommon();
		
		org.bson.Document dbDoc = new org.bson.Document("resort", resort);
		for (String key : data.keySet()) {
			String value = data.get(key);
			dbDoc.append(key, value);
		}		
		dbDoc.append("lastUpdated", new Date());
		
		dbCommon.insertDocument(dbDoc, COLLECTION);
	}
}
