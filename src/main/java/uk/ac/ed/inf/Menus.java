package uk.ac.ed.inf;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Using a HttpServer to get the menus from the server.
 *
 * <p> A Menus class could establish a connection to server and get the menus Json file.
 * After getting the menus, the Menus class could deserialise the file and do further processing.</p>5
 */
public class Menus {
    // Just have one HttpServer, shared between all HttpRequests
    private static final HttpClient client = HttpClient.newHttpClient();

    // variables
    private String name;
    private String port;
    private ArrayList<Shop> shopList;

    /**
     * Construct a Menus object with two parameters
     *
     * @param name name of the server. E.g: LocalHost
     * @param port port where the web server is running. E.g: 9898
     */
    public Menus(String name, String port) {
        this.name = name;
        this.port = port;
    }

    /**
     * Return the int cost in pence for one delivery.
     *
     * <p> Grouping all the items and prices from different shops and store them into a hashmap.
     * Accepts a variable number of ordered item names and check the prices from the hashmap.
     * Add up all the prices of the item and plus the base delivery cost(50p).</p>
     *
     * @param orders variable number of String represents the ordered items.
     * @return the int cost in pence of having all of these items delivered by drone,
     * including the standard delivery charge of 50p per delivery
     */
    public int getDeliveryCost(String... orders) {
        getMenusFromServer();

        int deliveryCost = 50; // base delivery cost is 50p

        // create the HashMap to store all the items and prices from the menus
        HashMap<String, Integer> itemsAndPrices = new HashMap<String, Integer>();

        for (Shop shop : shopList) {
            for (Shop.Menu menuDetail : shop.menu) {
                itemsAndPrices.put(menuDetail.item, menuDetail.pence);
            }
        }

        for (String order : orders) {
            deliveryCost += itemsAndPrices.get(order);
        }

        return deliveryCost;
    }

    /**
     * parsing the menus.json to a list of json object
     *
     * <p>Establish a connection to server and retrieve the menus.json from the server.
     * If the connection success, then parsing the json file into an ArrayList of Shop object.</p>
     */
    @ Deprecated
    public void getMenusFromServer() {
        String menusUrl = "http://" + this.name + ":" + this.port + "/menus/menus.json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(menusUrl))
                .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Type listType = new TypeToken<ArrayList<Shop>>() {}.getType();
                this.shopList = new Gson().fromJson(response.body(), listType);
            } else {
                System.out.println("Status code:" + response.statusCode() +
                        ". Unable to get the Menus from the server, please check the status code.");
                System.exit(1);
            }
        } catch (InterruptedException| IOException e) {
            System.out.println("Fatal error: Unable to connect to " + this.name + " at port " +
                    this.port + ".");
            e.printStackTrace();
            System.exit(1); // Exit the application
        }
    }

    // getter
    public String getName() {
        return name;
    }

    public String getPort() {
        return port;
    }

    public ArrayList<Shop> getShopList() {
        return shopList;
    }
}
