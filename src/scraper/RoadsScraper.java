package scraper;

import java.io.IOException;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import db.DBCommon;

public class RoadsScraper extends WebPageScraper {

	private final static String URL = "https://roads.dot.ca.gov";
	private final static String COLLECTION = "roads";
	private String highway = "";

	public RoadsScraper(String highway) {
		super();
		
		this.highway = highway;
	}

	protected Document createDocFromConnection() throws IOException {
		Response response = 
				Jsoup.connect(URL)
				.method(Method.POST)
				.data("roadnumber", this.highway)
				.timeout(40000)
				.execute();
		
		Document doc = response.parse();
		return doc;
	}

	protected Map<String, String> extractDataFromDoc(Document doc) {
		Map<String, String> data  = new HashMap<String, String>();
		
		// Find data in document
		Elements reports = doc.getElementsByTag("p");
		reports.remove(0);	// first <p> is not data
		
		ArrayList<String> reportsArr = new ArrayList<String>();
		
		for (Element report : reports) {
			String reportData = report.ownText();
			
			reportsArr.add(reportData);
		}
		
		data.put("conditions", reportsArr.toString());
		
		return data;
	}

	protected void storeData(Map<String, String> data) {
		// Store data in MongoDB
		DBCommon dbCommon = new DBCommon();
		String roadConditions = data.get("conditions");
		
		org.bson.Document dbDoc = new org.bson.Document("highway", highway)
			.append("conditions", roadConditions)
			.append("lastUpdated", new Date());
		
		dbCommon.insertDocument(dbDoc, COLLECTION);
	}
	
}
