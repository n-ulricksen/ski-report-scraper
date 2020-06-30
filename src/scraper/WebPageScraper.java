package scraper;

import java.io.IOException;
import java.util.*;

import org.jsoup.nodes.Document;

public abstract class WebPageScraper implements Runnable {
	private Thread thread = null;
	
	public WebPageScraper() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	@Override
	public void run() {
		scrapePageData();
	}
	
	protected void scrapePageData() {
		Map<String, String> data;
		
		try {
			Document doc = createDocFromConnection();
			
			data = extractDataFromDoc(doc);
			
			storeData(data);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			data = null;
		}
	}
	
	protected abstract Document createDocFromConnection() throws IOException;
	
	protected abstract Map<String, String> extractDataFromDoc(Document doc);
	
	protected abstract void storeData(Map<String, String> data);

}
