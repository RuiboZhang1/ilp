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

public class Menus {

    private static final HttpClient client = HttpClient.newHttpClient();

    public String name;
    public String port;

    public Menus(String name, String port) {
        this.name = name;
        this.port = port;
    }

    public int getDeliveryCost(String... orders) {
        final String MENUS_URL = "http://" + this.name + ":" + this.port + "/menus/menus.json";
        int deliveryCost = 0;
        HashMap<String, Integer> itemsAndPrices = new HashMap<String, Integer>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MENUS_URL))
                .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            Type listType = new TypeToken<ArrayList<Shop>>() {
            }.getType();
            ArrayList<Shop> shopList = new Gson().fromJson(response.body(), listType);

            /**
             * create a new hashmap, loop through shopList, add the item and pence into the hashmap
             * then loop through the orders, find the price and add to deliveryCost
             */
            for (Shop shop : shopList) {
                for (Shop.Menu menuDetail : shop.menu) {
                    itemsAndPrices.put(menuDetail.item, menuDetail.pence);
                }
            }

            for (String order : orders) {
                deliveryCost += itemsAndPrices.get(order);
            }

            deliveryCost += 50;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return deliveryCost;
    }
}
