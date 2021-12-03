package uk.ac.ed.inf;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
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
    private JsonParser jsonParser;
    private ArrayList<Shop> shopList;
    private HashMap<String, Integer> itemsAndPrices = new HashMap<String, Integer>();

    /**
     * Construct a Menus object with two parameters
     *
     * @param name name of the server. E.g: LocalHost
     * @param port port where the web server is running. E.g: 9898
     */
    public Menus(JsonParser jsonParser) {
        jsonParser.readMenusFromServer();
        this.shopList = jsonParser.getMenus();
        createItemsAndPrices();
    }

    public void createItemsAndPrices() {
        // create the HashMap to store all the items and prices from the menus
        for (Shop shop : shopList) {
            for (Shop.Menu menuDetail : shop.menu) {
                itemsAndPrices.put(menuDetail.item, menuDetail.pence);
            }
        }
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
    public int getDeliveryCost(ArrayList<String> orders) {
        int deliveryCost = 50; // base delivery cost is 50p
        for (String order : orders) {
            deliveryCost += this.itemsAndPrices.get(order);
        }

        return deliveryCost;
    }

    /**
     * Return the W3W of the order shop
     * @param orders
     * @return
     */
    public ArrayList<String> getShopOfOrder(ArrayList<String> orders) {
        ArrayList<String> shopLocation = new ArrayList<>();

        for (Shop shop : this.shopList) {
            ArrayList<String> menuItems = new ArrayList<>();
            for (Shop.Menu menuDetail : shop.menu) {
                menuItems.add(menuDetail.item);
            }

            for (String order : orders) {
                // no more than two shop in one order
                if (shopLocation.size() == 2) {
                    break;
                }

                if (menuItems.contains(order)) {
                    if (!shopLocation.contains(shop.location)) {
                        shopLocation.add(shop.location);
                    }
                }
            }
        }
        return shopLocation;
    }


    // getter
    public ArrayList<Shop> getShopList() {
        return this.shopList;
    }

    public HashMap<String, Integer> getItemsAndPrices() {
        return this.itemsAndPrices;
    }
}
