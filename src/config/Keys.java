package config;

public class Keys {	
	public static String getDarkSkyKey() {
		return System.getenv("DARK_SKY_KEY");
	}
	
	public static String getGoogleMapsKey() {
		return System.getenv("GOOGLE_MAPS_KEY");
	}
}
