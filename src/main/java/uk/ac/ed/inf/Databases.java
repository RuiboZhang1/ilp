package uk.ac.ed.inf;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Databases {

    private final String protocol = "jdbc:derby://";
    private final String databaseName = "/derbyDB";
    private String name;
    private String port;

    public Databases(String name, String port) {
        this.name = name;
        this.port = port;
    }

    public Connection connectToDatabase() throws SQLException {
        String jdbcString = protocol + this.name + ":" + this.port + databaseName;
        Connection conn = DriverManager.getConnection(jdbcString);

        return conn;
    }

    /**
     * Given a date
     * Retrieve data from database
     * @param date
     */
    public ArrayList<String> getDataFromDatabase(Date date) throws SQLException, ParseException {
        Connection conn = connectToDatabase();

        String query = "select * from orders where deliveryDate=(?)";

        PreparedStatement psQuery = conn.prepareStatement(query);

        psQuery.setDate(1, date);

        ArrayList<String> orderNumberList = new ArrayList<>();
        ResultSet rs = psQuery.executeQuery();

        while(rs.next()) {
            String orderNumber = rs.getString("orderNo");
            orderNumberList.add(orderNumber);
        }

        return orderNumberList;
    }

    public static void main(String[] args) throws SQLException, ParseException {
        Date date = new Date(2022-1900, 9, 22);
        Databases databases = new Databases("localhost", "9876");

        ArrayList<String> orderNumberList = databases.getDataFromDatabase(date);

        for  (String orderNumber : orderNumberList) {
            System.out.println(orderNumber);
        }
    }


//    /**
//     * After performing the algorithm
//     * write the data to deliveries and flightpath
//     */
//    public void writeDataToDerby() {
//
//    }


}
