package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    private HttpServer httpServer;
    private List<Feature> noFlyZones;
    private List<Feature> landmarks;
    private ArrayList<Shop> menus;
    private W3wCoordinate wordLongLat;


    public JsonParser(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.noFlyZones = new ArrayList<>();
        this.landmarks = new ArrayList<>();
        this.menus = new ArrayList<>();
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


    public void getLandmarksFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/buildings/landmarks.geojson");
        this.landmarks = FeatureCollection.fromJson(this.httpServer.getJsonResponse()).features();
    }

    public void getNoFlyZonesFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/buildings/no-fly-zones.geojson");
        this.noFlyZones = FeatureCollection.fromJson(this.httpServer.getJsonResponse()).features();
    }

    public void getMenusFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/menus/menus.json");
        Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
        this.menus =  new Gson().fromJson(this.httpServer.getJsonResponse(), listType);
    }

    public W3wCoordinate getWordsLongLat(String w1, String w2, String w3) {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/words/" + w1 + "/" + w2 + "/" + w3
                + "/details.json");

        W3wCoordinate wordLongLat = new Gson().fromJson(this.httpServer.getJsonResponse(), W3wCoordinate.class);
        return wordLongLat;
    }


    // for testing
//    public static void main(String[] args) {
//        HttpServer httpServer = new HttpServer("localhost", "9898");
//        JsonParser jsonParser = new JsonParser(httpServer);
//
//        jsonParser.getWordsLongLat("army","monks","grapes");
//
//        System.out.println(jsonParser.wordLongLat.coordinates.lng);
//        System.out.println(jsonParser.wordLongLat.coordinates.lat);
//    }
}
