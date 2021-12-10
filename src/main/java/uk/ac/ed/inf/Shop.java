package uk.ac.ed.inf;

/**
 * The Shop class stores the data after deserializing the menus.json using the reflection api.
 *
 * <p> The menus.json file include several records of shops, each record includes a String name
 * of the shop, a String location of the shop, a list of Menu objects.</p>
 *
 * */
public class Shop {

    public String name;
    public String location;
    public Food[] menu;

    /**
     * A Menu object stores the name of the item and its price in pence.
     */
    public static class Food {
        String item;
        int pence;
    }

}
