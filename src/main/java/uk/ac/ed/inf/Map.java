package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Storing the information of the map which the drone will fly on.
 */
public class Map {

    // public final variables will not be change after assigning value.
    public final ArrayList<Point> confinementArea;
    public final ArrayList<Geometry> landmarks;
    public final ArrayList<Geometry> noFlyZones;


    /**
     * Constructor of Map
     * @param jsonParser jsonParser of the web server
     */
    public Map(JsonParser jsonParser) {
        // use the constants to build the confinement area
        this.confinementArea = new ArrayList<>(Arrays.asList(
                Point.fromLngLat(Constants.KFC_LONGITUDE, Constants.KFC_LATITUDE),
                Point.fromLngLat(Constants.FORREST_HILL_LONGITUDE, Constants.FORREST_HILL_LATITUDE),
                Point.fromLngLat(Constants.TOP_OF_MEADOWS_LONGITUDE, Constants.TOP_OF_MEADOWS_LATITUDE),
                Point.fromLngLat(Constants.BUCCLEUCH_ST_BUS_STOP_LONGITUDE, Constants.BUCCLEUCH_ST_BUS_STOP_LATITUDE)));

        this.landmarks = new ArrayList<Geometry>();
        this.noFlyZones = new ArrayList<Geometry>();

        // read and process the data from web server
        jsonParser.readLandmarksFromServer();
        jsonParser.readNoFlyZonesFromServer();

        List<Feature> landmarkList = jsonParser.landmarks;
        List<Feature> noFlyZoneList = jsonParser.noFlyZones;

        // Convert list of Feature into list of geometry
        for (int i=0; i<landmarkList.size(); i++){
            this.landmarks.add(landmarkList.get(i).geometry());
        }

        for (int i=0; i<noFlyZoneList.size(); i++){
            this.noFlyZones.add(noFlyZoneList.get(i).geometry());
        }
    }
}
