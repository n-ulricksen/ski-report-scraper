package scraper;

import java.util.*;

public class Scraper {
	private static int scrapePeriodInMinutes = 60;

	public static void main(String[] args) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				scrapeAllData();
			}
		}, 0, 1000 * 60 * scrapePeriodInMinutes);
	}
	
	private static void scrapeAllData() {
		// TODO: Make data fields dynamic (user input?)
		// Data to scrape:
		
		// TODO: get all roads in state?
		String[] roads = {"108", "4", "88", "50", "80", "89"};
		
		// TODO: lookup all resorts?
		Map<String, String> locationZips = new HashMap<String, String>();
		locationZips.put("Dodge Ridge", "95364");
		locationZips.put("Bear Valley", "95223");
		locationZips.put("Kirkwood", "95646");
		locationZips.put("Heavenly", "96150");
		locationZips.put("Squaw Valley", "96146");
		
		Map<String, String> resortLookup = new HashMap<String, String>();
		resortLookup.put("Dodge Ridge", "dodge-ridge");
		resortLookup.put("Bear Valley", "bear-valley");
		resortLookup.put("Kirkwood", "kirkwood");
		resortLookup.put("Heavenly", "heavenly-mountain-resort");
		resortLookup.put("Squaw Valley", "squaw-valley-usa");
		
		// Scrape road data		
		for (String road : roads) {
			new RoadsScraper(road);
		}
		
		// Scrape weather data
		for (String location : locationZips.keySet()) {
			String zip = locationZips.get(location);
			new Weather(zip);
		}
		
		// Scrape resort info 
		for (String resort : resortLookup.keySet()) {
			String lookup = resortLookup.get(resort);
			
			new ResortScraper(resort, lookup);
			new ResortDataScraper(resort, lookup);
		}
	}
}
