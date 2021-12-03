package uk.ac.ed.inf;


import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

public class Database {

    private static final String PROTOCOL = "jdbc:derby://";
    private static final String DATABASE_NAME = "/derbyDB";
    private String name;
    private String port;
    private Menus menus;
    private JsonParser jsonParser;
    private Statement statement;
    private PreparedStatement prepStatement;
    private Connection conn;
    private ArrayList<Order> orderList = new ArrayList<>();

    public Database(String name, String port, Menus menus, JsonParser jsonParser) {
        this.name = name;
        this.port = port;
        this.menus = menus;
        this.jsonParser = jsonParser;
    }


    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    public Connection getConnection() {
        return conn;
    }

    public void connectToDatabase() throws SQLException {
        String jdbcString = PROTOCOL + this.name + ":" + this.port + DATABASE_NAME;
        Connection conn = DriverManager.getConnection(jdbcString);
        this.conn = conn;
        this.statement = conn.createStatement();
        System.out.println("Success connect to database.");
    }

    public void deleteExistedTable(String tableName) throws SQLException {
        DatabaseMetaData databaseMetadata = this.conn.getMetaData();

        ResultSet resultSet =
                databaseMetadata.getTables(null, null, tableName.toUpperCase(), null);
        if (resultSet.next()) {
            statement.execute("drop table " + tableName.toLowerCase());
            System.out.println("Delete existed table " + tableName);
        }
    }

    public void createDeliveriesAndFlightpath() throws SQLException {
        deleteExistedTable("deliveries");
        deleteExistedTable("flightpath");

        statement.execute("create table deliveries(" +
                "orderNo char(8), " +
                "deliveredTo varchar(19), " +
                "costInPence int)");

        System.out.println("Create table deliveries");

        statement.execute("create table flightpath(" +
                "orderNo char(8), " +
                "fromLongitude double, " +
                "fromLatitude double, " +
                "angle int, " +
                "toLongitude double, " +
                "toLatitude double)");

        System.out.println("Create table flightpath");
    }

    /**
     * Given a date
     * Retrieve data from database
     *
     * @param date
     */
    public void getOrderFromDatabase(Date date) throws SQLException {
        String queryOrders = "select * from orders where deliveryDate=(?)";
        PreparedStatement psQuery = conn.prepareStatement(queryOrders);
        psQuery.setDate(1, date);
        ResultSet orderSet = psQuery.executeQuery();

        while (orderSet.next()) {

            String orderNumber = orderSet.getString("orderNo");
            Date deliveryDate = orderSet.getDate("deliveryDate");
            String customer = orderSet.getString("customer");
            String deliverTo = orderSet.getString("deliverTo");

            String queryOrderDetails = "select * from orderDetails where orderNo=(?)";
            PreparedStatement ps = conn.prepareStatement(queryOrderDetails);
            ps.setString(1, orderNumber);
            ResultSet orderDetail = ps.executeQuery();

            ArrayList<String> items = new ArrayList<>();

            while (orderDetail.next()) {
                items.add(orderDetail.getString("item"));
            }

            Order order = new Order(orderNumber, deliveryDate, customer, deliverTo, items);

            this.orderList.add(order);
        }

        System.out.println("Success get order list from database");
    }
}
