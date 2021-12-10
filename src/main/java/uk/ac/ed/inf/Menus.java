package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Storing the information of all shops
 */
public class Menus {
    // public final variables, will not change after assigning the value
    public final ArrayList<Shop> shopList;
    public final HashMap<String, Integer> itemsAndPrices = new HashMap<String, Integer>();

    /**
     * Constructor of Menus class
     *
     * @param jsonParser JsonParser storing the data
     */
    public Menus(JsonParser jsonParser) {
        this.shopList = jsonParser.menus;
        createItemsAndPrices();
    }

    /**
     * create the HashMap to store all the items and prices from the menus
     */
    public void createItemsAndPrices() {
        for (Shop shop : shopList) {
            for (Shop.Food foodDetail : shop.menu) {
                itemsAndPrices.put(foodDetail.item, foodDetail.pence);
            }
        }
    }

    /**
     * Return the int cost in pence for one delivery.
     *
     * @param orders arraylist of String represents the ordered items.
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
     * Get the W3W address of the shop to be collected.
     *
     * @param orders arraylist of String represents the ordered items.
     * @return the arraylist of W3W string of the order shops
     */
    public ArrayList<String> getShopOfOrder(ArrayList<String> orders) {
        ArrayList<String> shopLocation = new ArrayList<>();

        // loop through each shop
        for (Shop shop : this.shopList) {

            // get the menus items in a list for each shop
            ArrayList<String> menuItems = new ArrayList<>();
            for (Shop.Food foodDetail : shop.menu) {
                menuItems.add(foodDetail.item);
            }

            for (String order : orders) {
                // no more than two shop in one order
                if (shopLocation.size() == 2) {
                    break;
                }

                // if the shop contains the order item, add the shop to the return list
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
