package mymodule;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

public class EarthquakeCityMap extends PApplet {
	// You can ignore this.  It's to keep eclipse from reporting a warning
	private static final long serialVersionUID = 1L;
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	private static final boolean offline=false;
	UnfoldingMap map;
	public void setup()
	{
		size(1000,600,P2D);
		// This sets the background color for the Applet.  
		this.background(200,200,200);
		// Select a map provider
		AbstractMapProvider provider = new Google.GoogleTerrainProvider();
		int zoomLevel = 10;
		if(offline)
		{
			provider = new MBTilesMapProvider(mbTilesString);
		}
		map = new UnfoldingMap(this,50,50,800,500,provider);
		MapUtils.createDefaultEventDispatcher(this, map);
	}
	public void draw()
	{
		background(0,0,0);
		map.draw();
	}
}
