package uk.ac.ed.inf;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;

public class Database {

    private static final String PROTOCOL = "jdbc:derby://";
    private static final String DATABASE_NAME = "/derbyDB";
    private String name;
    private String port;
    private ArrayList<Orders> orderList = new ArrayList<>();

    public Database(String name, String port) {
        this.name = name;
        this.port = port;
    }

    public Connection connectToDatabase() throws SQLException {
        String jdbcString = PROTOCOL + this.name + ":" + this.port + DATABASE_NAME;
        Connection conn = DriverManager.getConnection(jdbcString);
        return conn;
    }

    /**
     * Given a date
     * Retrieve data from database
     *
     * @param date
     */
    public void getDataFromDatabase(Date date) throws SQLException, ParseException {
        Connection conn = connectToDatabase();

        String queryOrders = "select * from orders where deliveryDate=(?)";
        PreparedStatement psQuery = conn.prepareStatement(queryOrders);
        psQuery.setDate(1, date);
        ResultSet orderSet = psQuery.executeQuery();

        while (orderSet.next()) {

            String orderNumber = orderSet.getString("orderNo");
            Date deliveryDate = orderSet.getDate("deliveryDate");
            String customer = orderSet.getString("customer");
            String deliverTo = orderSet.getString("deliverTo");

            String queryOrderDetails = "select * from orders where orderNo=(?)";
            PreparedStatement ps = conn.prepareStatement(queryOrderDetails);
            ps.setString(1, orderNumber);
            ResultSet orderDetail = ps.executeQuery();

            ArrayList<String> items = new ArrayList<>();

            while (orderDetail.next()) {
                items.add(orderDetail.getString("item"));
            }

            Orders order = new Orders(orderNumber, deliveryDate, customer, deliverTo, items);

            this.orderList.add(order);
        }
    }
}
