package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Using a HttpServer to get the menus from the server.
 *
 * <p> A Menus class could establish a connection to server and get the menus Json file.
 * After getting the menus, the Menus class could deserialise the file and do further processing.</p>5
 */
public class Menus {
    public final ArrayList<Shop> shopList;
    public final HashMap<String, Integer> itemsAndPrices = new HashMap<String, Integer>();

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
     *
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
}
