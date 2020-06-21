package mymodule;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;
public class OceanQuakeMarker extends EarthquakeMarker {
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		isOnLand = false;
	}

	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		pg.rect(x-radius, y-radius, 2*radius, 2*radius);
	}
}
