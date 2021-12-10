package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * JsonParser used for parsing the Json String read from http server
 */
public class JsonParser {

    // public final variables, will not change after assigning value
    public final HttpServer httpServer;
    public final List<Feature> noFlyZones;
    public final List<Feature> landmarks;
    public final ArrayList<Shop> menus;

    // private variables, can be change the value later
    private W3wCoordinate wordLongLat;

    // Constructor of JsonParser
    public JsonParser(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.noFlyZones = readNoFlyZonesFromServer();
        this.landmarks = readLandmarksFromServer();
        this.menus = readMenusFromServer();
    }

    // Getter
    public W3wCoordinate getWordLongLat() {
        return this.wordLongLat;
    }

    /**
     * Parse landmarks.geojson from the server and store in the variable
     * @return List of Feature represents the landmarks
     */
    public List<Feature> readLandmarksFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/buildings/landmarks.geojson");
        List<Feature> landmarks = FeatureCollection.fromJson(this.httpServer.getJsonResponse()).features();
        System.out.println("Read landmarks from server success");
        return landmarks;
    }

    /**
     * Parse no-fly-zones.geojson from the server and store in the variable
     * @return List of Feature represents the no fly zones
     */
    public List<Feature> readNoFlyZonesFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/buildings/no-fly-zones.geojson");
        List<Feature> noFlyZones = FeatureCollection.fromJson(this.httpServer.getJsonResponse()).features();
        System.out.println("Read no fly zones from server success");
        return noFlyZones;
    }

    /**
     * Parse menus.json from the server and store in the variable
     * @return
     */
    public ArrayList<Shop> readMenusFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/menus/menus.json");
        Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
        ArrayList<Shop> menus =  new Gson().fromJson(this.httpServer.getJsonResponse(), listType);
        System.out.println("Read menus from server success");
        return menus;
    }

    /**
     * Parse the details.json of the input what3words and store in the variable
     * @param words String of what3words
     */
    public void readWordsLongLat(String words) {
        String[] wordList = words.split("\\.");
        String w1 = wordList[0];
        String w2 = wordList[1];
        String w3 = wordList[2];

        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/words/" + w1 + "/" + w2 + "/" + w3
                + "/details.json");

        this.wordLongLat = new Gson().fromJson(this.httpServer.getJsonResponse(), W3wCoordinate.class);
    }
}
