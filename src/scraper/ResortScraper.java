package scraper;

import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import db.DBCommon;

public class ResortScraper extends WebPageScraper {

	// other URL is same, but ends with /skireport.html
	private final static String URL = "https://www.onthesnow.com/california/";
	private final static String COLLECTION = "resortsinfo";
	private String resort = "";
	private String lookup = "";

	public ResortScraper(String resort, String lookup) {
		super();
		
		this.resort = resort;
		this.lookup = lookup;
	}
	
	protected Document createDocFromConnection() throws IOException {
		Response response = 
				Jsoup.connect(URL + lookup + "/" + "ski-resort.html")
				.method(Method.GET)
				.execute();
		
		Document doc = response.parse();
		return doc;
	}

	protected Map<String, String> extractDataFromDoc(Document doc) {
		Map<String, String> data = new HashMap<String, String>();
		
		// Find data in document
		Elements currentStatusElements = doc.select(".current_status");
		String currentStatus = currentStatusElements.first().ownText();
		data.put("currentStatus", currentStatus);
		
		Elements resortWebsiteElements = doc.select(".resortContact");
		String resortWebsite;
		if (resortWebsiteElements.isEmpty()) {
			resortWebsite = "";
		} else {
			resortWebsite = resortWebsiteElements.first().ownText();			
		}
		data.put("resortWebsite", resortWebsite);
		
		Elements resortTelephoneElements = doc.select(".resortTel");
		String resortTelephone;
		if (resortWebsiteElements.isEmpty()) {
			resortTelephone = "";
		} else {
			resortTelephone = resortTelephoneElements.first().ownText();
		}
		data.put("resortTelephone", resortTelephone);
		
		Elements ratingElements = doc.select(".rating");
		String rating = ratingElements.first().text();
		data.put("rating", rating);
		
		Elements descElements = doc.select(".resort_description");
		String desc = descElements.first().text();
		data.put("desc", desc);

		Elements totalLiftsElements = doc.select("#resort_lifts > ul > li.total .liftTotal");
		String totalLifts = totalLiftsElements.first().ownText();
		data.put("totalLifts", totalLifts);
		
		Elements projectedOpenCloseElements = doc.select(".projOpenClose > *");
		String projectedOpen = projectedOpenCloseElements.get(2).text();
		data.put("projectedOpen", projectedOpen);
		String projectedClose = projectedOpenCloseElements.get(3).text();
		data.put("projectedClose", projectedClose);
		
		return data;
	}

	// refactor for resort scraping
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
