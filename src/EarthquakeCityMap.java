package mymodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;
public class EarthquakeCityMap<T> extends PApplet {
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	private UnfoldingMap map;
	private List<Marker> cityMarkers;
	private List<Marker> quakeMarkers;
	private List<Marker> countryMarkers;
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	boolean citymarker;
	public void setup() {		
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
			city.addProperty("markertype", "city");
		  cityMarkers.add(new CityMarker(city));
		}
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    for(PointFeature feature : earthquakes) {
		  if(isLand(feature)) {
			  feature.addProperty("markertype", "landquake");
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  else {
			  feature.addProperty("markertype", "oceanquake");
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }
	    //sortAndPrint(10);
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	} 
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	private void sortAndPrint(int numToPrint)
	{	
		Marker[] obj=  new Marker[quakeMarkers.size()];
		obj=quakeMarkers.toArray(obj);
		Arrays.sort(obj);
		int n = Math.min(numToPrint, quakeMarkers.size());
		for(int i=0;i<n;i++)
			System.out.println(obj[i]);
	}
	@Override
	public void mouseMoved()
	{
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	} 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		if (lastSelected != null) {
			return;
		}
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}
	
	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		else if (lastClicked == null) 
		{
			checkEarthquakesForClick();
			if (lastClicked == null) {
				checkCitiesForClick();
			}
		}
	}
	private void checkCitiesForClick()
	{
		if (lastClicked != null) return;
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;
				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker)mhide;
					if (quakeMarker.getDistanceTo(marker.getLocation()) 
							> quakeMarker.threatCircle()) {
						quakeMarker.setHidden(true);
					}
				}
				return;
			}
		}		
	}
	
	private void checkEarthquakesForClick()
	{
		if (lastClicked != null) return;
		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker)m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo(marker.getLocation()) 
							> marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				return;
			}
		}
	}
	
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	private void addKey() {	
		if(lastSelected!=null)
		{
			display();
		}
		fill(255, 250, 240);
		int xbase = 25;
		int ybase = 50;
		rect(xbase, ybase, 150, 250);
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);
		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);
		text("Past hour", xbase+50, ybase+200);
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);
		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
		
	}
	public void display()
	{
		if((String)lastSelected.getProperty("markertype")=="city")
			displayCity();
		else if((String)lastSelected.getProperty("markertype")=="landquake")
			displayLandquake();
		else
			displayOceanquake();
			
	}
	public void displayCity()
	{
		String name = (String)lastSelected.getProperty("name");
		String country = (String)lastSelected.getProperty("country");
		String population =(String)lastSelected.getProperty("population");
		fill(255, 250, 240);
		int xbase = 25;
		int ybase = 350;
		rect(xbase, ybase, 150, 150);
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(20);
		text("CityMarker", xbase+10, ybase+25);
		textSize(12);
		text("City: "+name,xbase+10,ybase+50);
		text("Country: "+country,xbase+10,ybase+75);
		text("Population: "+population+"M",xbase+10,ybase+100);
	}
	public void displayLandquake()
	{
		String country=(String)lastSelected.getProperty("country");
		String magnitude=lastSelected.getProperty("magnitude").toString();
		String radius=lastSelected.getProperty("radius").toString();
		String age=(String)lastSelected.getProperty("age").toString();
		fill(255, 250, 240);
		int xbase = 25;
		int ybase = 350;
		rect(xbase, ybase, 150, 150);
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(15);
		text("LandquakeMarker", xbase+10, ybase+25);
		textSize(12);
		text("country: "+country,xbase+10,ybase+50);
		text("magnitude :"+magnitude,xbase+10,ybase+75);
		text("radius: "+radius,xbase+10,ybase+100);
		text("age: "+age,xbase+10,ybase+125);
		
	}
	public void displayOceanquake()
	{
		String magnitude=lastSelected.getProperty("magnitude").toString();
		String radius=lastSelected.getProperty("radius").toString();
		String age=lastSelected.getProperty("age").toString();
		fill(255, 250, 240);
		int xbase = 25;
		int ybase = 350;
		rect(xbase, ybase, 150, 150);
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(15);
		text("OceanquakeMarker", xbase+10, ybase+25);
		textSize(12);
		text("magnitude :"+magnitude,xbase+10,ybase+50);
		text("radius: "+radius,xbase+10,ybase+75);
		text("age: "+age,xbase+10,ybase+100);
		
	}
	
	
	private boolean isLand(PointFeature earthquake) {
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		return false;
	}
	
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		Location checkLoc = earthquake.getLocation();
		if(country.getClass() == MultiMarker.class) {
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
					return true;
				}
			}
		}
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}
