package uk.ac.ed.inf;


import java.sql.*;
import java.util.ArrayList;

/**
 * Set up connection to the database and process the data.
 */
public class Database {
    // Constant of the database
    private static final String PROTOCOL = "jdbc:derby://";
    private static final String DATABASE_NAME = "/derbyDB";

    // Private variables
    private String name;
    private String port;
    private Statement statement;
    private Connection conn;
    private ArrayList<Order> orderList = new ArrayList<>();

    /**
     * Constructor of the database
     *
     * @param name machine name of the database
     * @param port port of the database
     */
    public Database(String name, String port) {
        this.name = name;
        this.port = port;
    }


    // getter
    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    public Connection getConnection() {
        return conn;
    }

    /**
     * establish connection to the database
     *
     * @throws SQLException
     */
    public void connectToDatabase() throws SQLException {
        String jdbcString = PROTOCOL + this.name + ":" + this.port + DATABASE_NAME;
        Connection conn = DriverManager.getConnection(jdbcString);
        this.conn = conn;
        this.statement = conn.createStatement();
        System.out.println("Success connect to database.");
    }

    /**
     * Given name of the table, delete it if existed in the database
     *
     * @param tableName the name of the table to be deleted
     * @throws SQLException
     */
    public void deleteExistedTable(String tableName) throws SQLException {
        DatabaseMetaData databaseMetadata = this.conn.getMetaData();

        ResultSet resultSet =
                databaseMetadata.getTables(null, null, tableName.toUpperCase(), null);
        if (resultSet.next()) {
            statement.execute("drop table " + tableName.toLowerCase());
            System.out.println("Delete existed table " + tableName);
        }
    }

    /**
     * Create two empty tables - deliveries and flightpath
     *
     * @throws SQLException
     */
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
     * Given a date, Retrieve orders of the date from database and store it in private variable
     *
     * @param date date of the order. dd-mm-yyyy
     */
    public void getOrderFromDatabase(Date date) throws SQLException {
        String queryOrders = "select * from orders where deliveryDate=(?)";
        PreparedStatement psQuery = conn.prepareStatement(queryOrders);
        psQuery.setDate(1, date);
        ResultSet orderSet = psQuery.executeQuery();

        // loop through the order set
        while (orderSet.next()) {
            String orderNumber = orderSet.getString("orderNo");
            Date deliveryDate = orderSet.getDate("deliveryDate");
            String customer = orderSet.getString("customer");
            String deliverTo = orderSet.getString("deliverTo");

            // get items from orderDetails table
            String queryOrderDetails = "select * from orderDetails where orderNo=(?)";
            PreparedStatement ps = conn.prepareStatement(queryOrderDetails);
            ps.setString(1, orderNumber);
            ResultSet orderDetail = ps.executeQuery();

            ArrayList<String> items = new ArrayList<>();
            while (orderDetail.next()) {
                items.add(orderDetail.getString("item"));
            }

            // initialise a new order to store the order detail and add it to the list
            Order order = new Order(orderNumber, deliveryDate, customer, deliverTo, items);
            this.orderList.add(order);
        }

        System.out.println("Success get order list from database");
    }
}
