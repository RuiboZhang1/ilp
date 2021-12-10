package uk.ac.ed.inf;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.NumberFormat;

/**
 * The main class for running the application
 */
public class App {
    // Constants name of the server and database
    public static final String NAME = "localhost";

    /**
     * Write the GeoJson string to a geojson file in local
     *
     * @param geoJsonPath the flight path of the drone
     * @param day         day of the order
     * @param month       month of the order
     * @param year        year of the order
     * @throws IOException
     */
    public static void writeFile(String geoJsonPath, String day, String month, String year) throws IOException {
        FileWriter myWriter = new FileWriter(
                "drone-" + day + "-" + month + "-" + year + ".geojson");
        myWriter.write(geoJsonPath);
        myWriter.close();
        System.out.println("GeoJson file created");
    }

    /**
     * Main method to start
     *
     * @param args <ul>
     *                  <li>args[0] - day of the order</li>
     *                  <li>args[1] - month of the order</li>
     *                  <li>args[2] - year of the order</li>
     *                  <li>args[3] - port of the web server</li>
     *                  <li>args[4] - port of the database</li>
     *              </ul>
     * @throws SQLException
     * @throws IOException
     */
    public static void main(String[] args) throws SQLException, IOException {
        // reading the args
        String day = args[0];
        String month = args[1];
        String year = args[2];
        String webPort = args[3];
        String databasePort = args[4];


        // initialise objects
        HttpServer httpServer = new HttpServer(NAME, webPort);
        JsonParser jsonParser = new JsonParser(httpServer);
        Map map = new Map(jsonParser);
        Menus menus = new Menus(jsonParser);
        Database database = new Database(NAME, databasePort);
        Drone drone = new Drone(map, database, jsonParser, menus);

        // create the empty table of deliveries and flightpath
        database.connectToDatabase();
        database.createDeliveriesAndFlightpath();

        // get orders and details from database
        String dateStr = year + "-" + month + "-" + day;
        Date date = Date.valueOf(dateStr);
        database.getOrderFromDatabase(date);

        // start delivery, get the geoJson flightpath and write in file
        drone.startDeliveries();
        String geoJsonPath = drone.getGeoJsonPath();
        writeFile(geoJsonPath, day, month, year);

        // print the percentage monetary value
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
        System.out.println("The percentage monetary value: " + formatter.format(drone.getPercentageMonetary()));
    }
}
