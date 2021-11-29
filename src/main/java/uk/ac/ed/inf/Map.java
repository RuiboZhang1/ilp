package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Map {

    private List<Point> confinementArea;
    private List<Geometry> landmarks;
    private List<Geometry> noFlyZones;


    /**
     * Constructor of Map object
     * @param jsonParser
     */
    public Map(JsonParser jsonParser) {
        this.confinementArea = new ArrayList<>(Arrays.asList(
                Point.fromLngLat(Constants.KFC_LONGITUDE, Constants.KFC_LATITUDE),
                Point.fromLngLat(Constants.FORREST_HILL_LONGITUDE, Constants.FORREST_HILL_LATITUDE),
                Point.fromLngLat(Constants.TOP_OF_MEADOWS_LONGITUDE, Constants.TOP_OF_MEADOWS_LATITUDE),
                Point.fromLngLat(Constants.BUCCLEUCH_ST_BUS_STOP_LONGITUDE, Constants.BUCCLEUCH_ST_BUS_STOP_LATITUDE),
                Point.fromLngLat(Constants.KFC_LONGITUDE, Constants.KFC_LATITUDE)));

        jsonParser.getLandmarksFromServer();
        jsonParser.getNoFlyZonesFromServer();

        List<Feature> landmarkList = jsonParser.getLandmarks();
        List<Feature> noFlyZoneList = jsonParser.getNoFlyZones();

        for (int i=0; i<landmarkList.size(); i++){
            this.landmarks.add(landmarkList.get(i).geometry());
        }

        for (int i=0; i<noFlyZoneList.size(); i++){
            this.noFlyZones.add(noFlyZoneList.get(i).geometry());
        }
    }


}
