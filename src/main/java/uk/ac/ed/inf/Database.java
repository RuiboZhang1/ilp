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
    private Connection conn;
    private ArrayList<Order> orderList = new ArrayList<>();

    public Database(String name, String port, Menus menus, JsonParser jsonParser) {
        this.name = name;
        this.port = port;
        this.menus = menus;
        this.jsonParser = jsonParser;
    }

    public void connectToDatabase() throws SQLException {
        String jdbcString = PROTOCOL + this.name + ":" + this.port + DATABASE_NAME;
        Connection conn = DriverManager.getConnection(jdbcString);
        this.conn = conn;
        this.statement = conn.createStatement();
    }

    public void deleteExistedTable(String tableName) throws SQLException {
        DatabaseMetaData databaseMetadata = this.conn.getMetaData();

        ResultSet resultSet =
                databaseMetadata.getTables(null, null, tableName.toUpperCase(), null);
        if (resultSet.next()) {
            statement.execute("drop table " + tableName.toLowerCase());
        }
    }

    public void createDeliveriesAndFlightpath() throws SQLException {
        deleteExistedTable("deliveries");
        deleteExistedTable("flightpath");

        statement.execute("create table deliveries(" +
                "orderNo char(8), " +
                "deliveredTo varchar(19), " +
                "costInPence int)");

        statement.execute("create table flightpath(" +
                "orderNo char(8), " +
                "fromLongitude double, " +
                "fromLatitude double, " +
                "angle int, " +
                "toLongitude double, " +
                "toLatitude double)");
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
    }

    /**
     * Sorting the order of delivery depending on the Ratio of price and distance in a greedy way.
     */
    public void sortOrderList() {
        ArrayList<Double> ordersDistance = new ArrayList<>();
        ArrayList<Integer> ordersPrice = new ArrayList<>();
        ArrayList<Double> ordersPriceDistanceRatio = new ArrayList<>();
        ArrayList<Order> sortedOrderList = new ArrayList<>();

        LongLat startPos = new LongLat(Constants.APPLETON_TOWER_LONGITUDE, Constants.APPLETON_TOWER_LATITUDE);

        for (int i = 0; i < this.orderList.size(); i++) {
            Order currOrder = orderList.get(i);
            double orderDistance = 0;
            int orderPrice = menus.getDeliveryCost(currOrder.getItems());
            ordersPrice.add(orderPrice);

            // get start, pickup and target long lat
            W3wCoordinate target = jsonParser.getWordsLongLat(currOrder.getDeliverTo());
            LongLat targetPos = LongLat.convertW3wCoordinateToLongLat(target);
            ArrayList<String> items = currOrder.getItems();
            ArrayList<String> pickUpW3Ws = menus.getShopOfOrder(items);
            ArrayList<LongLat> pickUpPos = new ArrayList<>();
            for (String word : pickUpW3Ws) {
                W3wCoordinate pickUp = jsonParser.getWordsLongLat(word);
                pickUpPos.add(LongLat.convertW3wCoordinateToLongLat(pickUp));
            }

            if (pickUpPos.size() == 1) {
                double startToPickUp = startPos.distanceTo(pickUpPos.get(0));
                double pickUpToTarget = pickUpPos.get(0).distanceTo(targetPos);
                orderDistance = startToPickUp + pickUpToTarget;
                ordersDistance.add(orderDistance);
            } else {
                // we need to select which pickUp point first
                double startToPickUp1 = startPos.distanceTo(pickUpPos.get(0));
                double pickUp1ToPickUp2 = pickUpPos.get(0).distanceTo(pickUpPos.get(1));
                double pickUp2ToTarget = pickUpPos.get(1).distanceTo(targetPos);
                double firstChoice = startToPickUp1 + pickUp1ToPickUp2 + pickUp2ToTarget;

                double startToPickUp2 = startPos.distanceTo(pickUpPos.get(1));
                double pickUp2ToPickUp1 = pickUpPos.get(1).distanceTo(pickUpPos.get(0));
                double pickUp1ToTarget = pickUpPos.get(0).distanceTo(targetPos);
                double secondChoice = startToPickUp2 + pickUp2ToPickUp1 + pickUp1ToTarget;

                orderDistance = Math.min(firstChoice, secondChoice);
                ordersDistance.add(orderDistance);
            }

            double priceDistanceRatio = orderPrice / orderDistance;
            ordersPriceDistanceRatio.add(priceDistanceRatio);
        }

        // Init the element list
        ArrayList<Element> elements = new ArrayList<Element>();
        for (int i = 0; i < ordersPriceDistanceRatio.size(); i++) {
            elements.add(new Element(i, ordersPriceDistanceRatio.get(i)));
        }

        Collections.sort(elements);
        Collections.reverse(elements); // If you want reverse order
        for (Element element : elements) {
            sortedOrderList.add(orderList.get(element.index));
        }

        this.orderList = sortedOrderList;
        System.out.println("done");
    }

    class Element implements Comparable<Element> {

        int index;
        double value;

        Element(int index, double value) {
            this.index = index;
            this.value = value;
        }

        public int compareTo(Element e) {
            if (this.value < e.value)
                return -1;
            else if (e.value < this.value)
                return 1;
            return 0;
        }
    }

}
