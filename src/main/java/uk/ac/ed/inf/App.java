package uk.ac.ed.inf;

import java.awt.geom.Line2D;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;

public class App {
    private static final String NAME = "localhost";


    public static void main(String[] args) throws SQLException {
        // reading the args
//        String day = args[0];
//        String month = args[1];
//        String year = args[2];
//        String webPort = args[3];
//        String databasePort = args[4];

        String day = "12";
        String month = "12";
        String year = "2022";
        String webPort = "9898";
        String databasePort = "9876";

        // initialise objects
        HttpServer httpServer = new HttpServer(NAME, webPort);
        JsonParser jsonParser = new JsonParser(httpServer);
        Map map = new Map(jsonParser);
        Menus menus = new Menus(jsonParser);
        Database database = new Database(NAME, databasePort, menus, jsonParser);
        Drone drone = new Drone(map, database, jsonParser, menus);
        Dro dro = new Dro(map, database, jsonParser, menus);


        // create the empty table of deliveries and flightpath
        database.connectToDatabase();
        database.createDeliveriesAndFlightpath();

        // get orders and details from database
        String dateStr = year + "-" + month + "-" + day;
        Date date = Date.valueOf(dateStr);
        database.getOrderFromDatabase(date);
        //drone.startDeliveries();
        dro.startDeliveries();
        //dro.test();
        String geoJsonPath = dro.getGeoJsonPath();

        try {
            FileWriter myWriter = new FileWriter(
                    "drone-" + day + "-" + month + "-" + year + ".geojson");
            myWriter.write(geoJsonPath);
            myWriter.close();
            System.out.println("GeoJson successfully created!");
        } catch (IOException e) {
            System.out.println("Fatal error: Readings GeoJson wasn't created.");
            e.printStackTrace();
        }




    }
}
