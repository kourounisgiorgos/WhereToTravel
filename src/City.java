import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import weather.OpenWeatherMap;
import wikipedia.MediaWiki;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class City {

	String name;
	String country;
	private int[] terms_vector = new int[10];
	private double[] geodesic_vector = new double[2];

	public City(String name) {
		super();
		this.name = name;
		this.terms_vector = null;
		this.geodesic_vector = null;
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int[] getTerms_vector() {
		return terms_vector;
	}

	public void setTerms_vector(int[] terms_vector) {
		this.terms_vector = terms_vector;
	}

	public double[] getGeodesic_vector() {
		return geodesic_vector;
	}

	public void setGeodesic_vector(double[] geodesic_vector) {
		this.geodesic_vector = geodesic_vector;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	public Traveller compareTravellers() {
		Traveller bestTraveller = null;
		double result;
		double bestSimilarity=-1;
		for(int i=0;i<Main.allTravellers.size();i++) {
			result = Main.allTravellers.get(i).calculateFreeTicket(this);
			if(result > bestSimilarity) {
				bestSimilarity = result;
				bestTraveller = Main.allTravellers.get(i);
			}
			
		}
		
		return bestTraveller;
	}

	public void retrieveKnownGeo(String appid) throws IOException { //retrieve Geodesic info when the city name is known
		ObjectMapper mapper = new ObjectMapper();
		OpenWeatherMap weather_obj = mapper.readValue(new URL("http://api.openweathermap.org/data/2.5/weather?q="+this.name+"&APPID="+appid+""), OpenWeatherMap.class);

		double[] tempGeo = {weather_obj.getCoord().getLat(), weather_obj.getCoord().getLon()};
		setGeodesic_vector(tempGeo);

	}

	public static double[] retrieveUnknownGeo(String city, String appid) throws IOException { //retrieve Geodesic info when the city name is unknown
		ObjectMapper mapper = new ObjectMapper();
		OpenWeatherMap weather_obj = mapper.readValue(new URL("http://api.openweathermap.org/data/2.5/weather?q="+city+"&APPID="+appid+""), OpenWeatherMap.class);
		double[] tempGeo = {weather_obj.getCoord().getLat(), weather_obj.getCoord().getLon()};
		
		return tempGeo;
	}
	
	public int retrieveTemperature(String city, String appid) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		OpenWeatherMap weather_obj = mapper.readValue(new URL("http://api.openweathermap.org/data/2.5/weather?q="+city+"&APPID="+appid+""), OpenWeatherMap.class);
		int temperature;

		double celsius = weather_obj.getMain().getTemp()-273.15; //conversion from kelvin to celsius
		if(celsius >= 40) {
			temperature = 10;
		}else if(celsius >= 35) {
			temperature = 9;
		}else if(celsius >= 30) {
			temperature = 8;
		}else if(celsius >= 25) {
			temperature = 7;
		}else if(celsius >= 20) {
			temperature = 6;
		}else if(celsius >= 15) {
			temperature = 5;
		}else if(celsius >= 10) {
			temperature = 4;
		}else if(celsius >= 5) {
			temperature = 3;
		}else if(celsius >= 0) {
			temperature = 2;
		}else if(celsius >= -5) {
			temperature = 1;
		}else {
			temperature = 0;
		}

		return temperature;
	}
	
	public static int[] calculateTerms(String city,String appid) throws JsonParseException, JsonMappingException, MalformedURLException, IOException {
		int[] terms = new int[10];
		ObjectMapper mapper = new ObjectMapper(); 
		OpenWeatherMap weather_obj = mapper.readValue(new URL("http://api.openweathermap.org/data/2.5/weather?q="+city+","+"&APPID="+appid+""), OpenWeatherMap.class);
		MediaWiki mediaWiki_obj =  mapper.readValue(new URL("https://en.wikipedia.org/w/api.php?action=query&prop=extracts&titles="+city+"&format=json&formatversion=2"),MediaWiki.class);
		City thisCity = new City(city);

		terms[0] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"cafe");
		terms[1] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"sea");
		terms[2] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"museum");
		terms[3] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"restaurant");
		terms[4] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"stadium");
		terms[5] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"park");
		terms[6] = thisCity.retrieveTemperature(city,appid);
		terms[7] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"sports");
		terms[8] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"music");
		terms[9] = countCriterionfCity(mediaWiki_obj.getQuery().getPages().get(0).getExtract(),"technology");
		
		return terms;
	}
	
	private static int countCriterionfCity(String cityArticle, String criterion) {
	    cityArticle=cityArticle.toLowerCase();
	    int index = cityArticle.indexOf(criterion);
	    int count = 0;
	    while (index != -1) {
	        count++;
	        cityArticle = cityArticle.substring(index + 1);
	        index = cityArticle.indexOf(criterion);
	    }
	return count;
	} 

	
}
