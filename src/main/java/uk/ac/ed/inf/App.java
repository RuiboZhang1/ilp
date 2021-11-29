package uk.ac.ed.inf;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

public class App {
    private static final String NAME = "localhost";


    public static void main(String[] args) throws SQLException {
        // reading the args
        String day = args[0];
        String month = args[1];
        String year = args[2];
        String webPort = args[3];
        String databasePort = args[4];

        // initialise objects
        HttpServer httpServer = new HttpServer(NAME, webPort);
        JsonParser jsonParser = new JsonParser(httpServer);
        Database dataBase = new Database(NAME, databasePort);
        Map map = new Map(jsonParser);
        Menus menus = new Menus(jsonParser);
        Drone drone = new Drone(map);


        // create the empty table of deliveries and flightpath
        dataBase.connectToDatabase();
        dataBase.createDeliveriesAndFlightpath();

        // get orders and details from database
        String dateStr = year + "-" + month + "-" + day;
        Date date = Date.valueOf(dateStr);
        dataBase.getOrderFromDatabase(date);


    }
}
