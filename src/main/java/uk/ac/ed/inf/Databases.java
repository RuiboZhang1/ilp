package uk.ac.ed.inf;


import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;

public class Databases {

    private final String protocol = "jdbc:derby://";
    private String name;
    private String port;

    public Databases(String name, String port) {
        this.name = name;
        this.port = port;
    }

    /**
     * Given a date
     * Retrieve data from database
     * @param date
     */
    public void getDataFromDerby(Date date) {

    }


    /**
     * After performing the algorithm
     * write the data to deliveries and flightpath
     */
    public void writeDataToDerby() {

    }


}
