package uk.ac.ed.inf;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

public class App {
    public static final String NAME = "localhost";

    public static void writeFile(String geoJsonPath, String day, String month, String year) throws IOException {
        FileWriter myWriter = new FileWriter(
                "drone-" + day + "-" + month + "-" + year + ".geojson");
        myWriter.write(geoJsonPath);
        myWriter.close();
        System.out.println("GeoJson file created");
    }

    public static void main(String[] args) throws SQLException, IOException {
        // reading the args
//        String day = args[0];
//        String month = args[1];
//        String year = args[2];
//        String webPort = args[3];
//        String databasePort = args[4];


        String day = "01";
        String month = "01";
        String year = "2022";
        String webPort = "9898";
        String databasePort = "9876";

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

        // start delivery and get the geoJson string of the flightpath
        drone.startDeliveries();
        String geoJsonPath = drone.getGeoJsonPath();
        writeFile(geoJsonPath, day, month, year);
    }
}
