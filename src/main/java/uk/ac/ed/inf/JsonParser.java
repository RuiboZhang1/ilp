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
    private ArrayList<Shop> shops;
    private W3wCoordinate wordLongLat;


    public JsonParser(HttpServer httpServer) {
        this.httpServer = httpServer;
        this.noFlyZones = new ArrayList<>();
        this.landmarks = new ArrayList<>();
        this.shops = new ArrayList<>();
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

    public ArrayList<Shop> getShops() {
        return this.shops;
    }


    public void getLandmarksFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/buildings/landmarks.geojson");
        this.noFlyZones = FeatureCollection.fromJson(this.httpServer.getJsonResponse()).features();
    }

    public void getNoFlyZonesFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/buildings/no-fly-zones.geojson");
        this.noFlyZones = FeatureCollection.fromJson(this.httpServer.getJsonResponse()).features();
    }

    public void getMenusFromServer() {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/menus/menus.json");
        Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
        this.shops = new Gson().fromJson(this.httpServer.getJsonResponse(), listType);
    }

    public void getWordsLongLat(String w1, String w2, String w3) {
        this.httpServer.retrieveJsonFromServer(httpServer.getHttpServer() + "/words/" + w1 + "/" + w2 + "/" + w3
                + "/details.json");

        this.wordLongLat = new Gson().fromJson(this.httpServer.getJsonResponse(), W3wCoordinate.class);
    }


    public static void main(String[] args) {
        HttpServer httpServer = new HttpServer("localhost", "9898");
        JsonParser jsonParser = new JsonParser(httpServer);

        jsonParser.getWordsLongLat("army","monks","grapes");

        System.out.println(jsonParser.wordLongLat.coordinates.lng);
        System.out.println(jsonParser.wordLongLat.coordinates.lat);
    }
}
