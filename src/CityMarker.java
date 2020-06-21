package mymodule;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;


public class CityMarker extends CommonMarker {
	
	public static int TRI_SIZE = 5;
	public CityMarker(Location location) {
		super(location);
	}
	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	}
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.pushStyle();
		pg.fill(150, 30, 30);
		pg.triangle(x, y-TRI_SIZE, x-TRI_SIZE, y+TRI_SIZE, x+TRI_SIZE, y+TRI_SIZE);
		pg.popStyle();
	}
	public void showTitle(PGraphics pg, float x, float y)
	{
		String name = getCity() + " " + getCountry() + " ";
		String pop = "Pop: " + getPopulation() + " Million";
		pg.pushStyle();
		pg.fill(255, 255, 255);
		pg.textSize(12);
		pg.rectMode(PConstants.CORNER);
		pg.rect(x, y-TRI_SIZE-39, Math.max(pg.textWidth(name), pg.textWidth(pop)) + 6, 39);
		pg.fill(0, 0, 0);
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.text(name, x+3, y-TRI_SIZE-33);
		pg.text(pop, x+3, y - TRI_SIZE -18);
		pg.popStyle();
	}
	private String getCity()
	{
		return getStringProperty("name");
	}
	private String getCountry()
	{
		return getStringProperty("country");
	}
	private float getPopulation()
	{
		return Float.parseFloat(getStringProperty("population"));
	}
}
