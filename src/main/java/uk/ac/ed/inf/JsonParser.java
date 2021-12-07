package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    public final HttpServer httpServer;
    public final List<Feature> noFlyZones;
    public final List<Feature> landmarks;
    public final ArrayList<Shop> menus;
    private W3wCoordinate wordLongLat;


    public JsonParser(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.noFlyZones = readNoFlyZonesFromServer();
        this.landmarks = readLandmarksFromServer();
        this.menus = readMenusFromServer();
    }

    // Getter
    public HttpServer getHttpServer() {
        return this.httpServer;
    }

    public List<Feature> getNoFlyZones() {
        return this.noFlyZones;
    }

    public List<Feature> getLandmarks() {
        return this.landmarks;
    }

    public ArrayList<Shop> getMenus() {
        return this.menus;
    }

    public W3wCoordinate getWordLongLat() {
        return this.wordLongLat;
    }


    public List<Feature> readLandmarksFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/buildings/landmarks.geojson");
        List<Feature> landmarks = FeatureCollection.fromJson(this.httpServer.getJsonResponse()).features();
        System.out.println("Read landmarks from server success");
        return landmarks;
    }

    public List<Feature> readNoFlyZonesFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/buildings/no-fly-zones.geojson");
        List<Feature> noFlyZones = FeatureCollection.fromJson(this.httpServer.getJsonResponse()).features();
        System.out.println("Read no fly zones from server success");
        return noFlyZones;
    }

    public ArrayList<Shop> readMenusFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/menus/menus.json");
        Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
        ArrayList<Shop> menus =  new Gson().fromJson(this.httpServer.getJsonResponse(), listType);
        System.out.println("Read menus from server success");
        return menus;
    }

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
