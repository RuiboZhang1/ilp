package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for controlling the flight of drone
 */
public class Drone {
    // private variables
    private int remainMove;
    private LongLat dronePos;
    private final LongLat terminatePos;

    private Map map;
    private Database database;
    private JsonParser jsonParser;
    private Menus menus;

    // these three variables could used to calculate the percentage monetary value
    private ArrayList<Order> unfinishedOrders = new ArrayList<>();
    private ArrayList<Order> finishedOrders = new ArrayList<>();
    private ArrayList<Order> passedOrders = new ArrayList<>();

    // these two variables are used to write data to database
    private List<Point> travelledPoints = new ArrayList<>();
    private List<Integer> travelledAngles = new ArrayList<>();

    /**
     * Constructor of Drone
     * @param map storing the map information, drone could use it to avoid crossing restricted area
     * @param database storing the orders
     * @param jsonParser jsonParser could be used to search the position of w3w
     * @param menus menus could be used to calculate price of the order.
     */
    public Drone(Map map, Database database, JsonParser jsonParser, Menus menus) {
        this.remainMove = Constants.MAXIMUM_MOVE_BY_DRONE;
        this.dronePos = new LongLat(Constants.APPLETON_TOWER_LONGITUDE, Constants.APPLETON_TOWER_LATITUDE);
        this.terminatePos = new LongLat(Constants.APPLETON_TOWER_LONGITUDE, Constants.APPLETON_TOWER_LATITUDE);
        this.map = map;
        this.database = database;
        this.jsonParser = jsonParser;
        this.menus = menus;
    }

    /**
     * The main method for the delivery
     * @throws SQLException
     */
    public void startDeliveries() throws SQLException {
        // initialise the variable
        Order nextOrder = null;

        // Recording the starting position while adding the points and angles into database
        int databaseIndex = 0;

        // record the start position
        travelledPoints.add(dronePos.getPoint());
        travelledAngles.add(dronePos.getAngle());

        // get the list of landmarks longitude and latitude.
        List<LongLat> landmarksLngLat = getLandmarksLngLat();

        unfinishedOrders = database.getOrderList();
        // loop through all the orders
        while (!unfinishedOrders.isEmpty()) {
            nextOrder = findBestOrder();
            LongLat targetPos = findTargetPos(nextOrder);
            ArrayList<LongLat> pickUpPos = findPickUpPos(nextOrder);

            if (pickUpPos.size() > 1) {
                pickUpPos = orderPickUpPos(pickUpPos, this.dronePos, targetPos);
            }

            // record the origin position before sending a order to prevent an invalid order
            LongLat originPos = dronePos;

            // initialise a OrderTrack object to track current order
            OrderTrack currOrderTrack = new OrderTrack();

            // pick up the items
            for (LongLat pickUp : pickUpPos) {
                currOrderTrack = getOrderMove(currOrderTrack, landmarksLngLat, pickUp);
            }

            // get delivery to target position
            currOrderTrack = getOrderMove(currOrderTrack, landmarksLngLat, targetPos);

            // check the number of movement to go back appleton tower
            LongLat tempPos = new LongLat(dronePos.getLongitude(), dronePos.getLatitude());
            int moveBackTermination = 0;
            while (!tempPos.closeTo(terminatePos)) {
                tempPos = tempPos.moveDrone(terminatePos);
                moveBackTermination++;
            }

            // if have enough remaining move, the order is  valid
            if (currOrderTrack.getOrderMove() + moveBackTermination <= remainMove) {
                unfinishedOrders.remove(nextOrder);
                finishedOrders.add(nextOrder);

                remainMove -= currOrderTrack.getOrderMove();
                travelledPoints.addAll(currOrderTrack.getOrderPoints());
                travelledAngles.addAll(currOrderTrack.getOrderAngles());

                // write the order data to database
                writeToDeliveries(nextOrder.getOrderNo(), nextOrder.getDeliverTo(),
                        menus.getDeliveryCost(nextOrder.getItems()));
                writeToFlightpath(nextOrder.getOrderNo(), databaseIndex);

                databaseIndex = travelledPoints.size() - 1;
            } else {
                // if invalid, the drone return to original position before this order
                dronePos = originPos;
                unfinishedOrders.remove(nextOrder);
                passedOrders.add(nextOrder);
            }
        }

        // after finishing all order, now go back to Appleton tower
        while (!dronePos.closeTo(terminatePos)) {
            dronePos = dronePos.moveDrone(terminatePos);
            travelledPoints.add(dronePos.getPoint());
            travelledAngles.add(dronePos.getAngle());
            remainMove--;
        }
        dronePos = dronePos.nextPosition(-999);
        travelledPoints.add(dronePos.getPoint());
        travelledAngles.add(dronePos.getAngle());
        remainMove--;

        writeToFlightpath(nextOrder.getOrderNo(), databaseIndex);

        System.out.println("save deliveries and flightpath in database");
    }

    /**
     * Get the list of LongLat object of Landmarks
     * @return list of LongLat of landmarks
     */
    private List<LongLat> getLandmarksLngLat() {
        List<LongLat> landmarksLngLat = new ArrayList<>();
        for (int i = 0; i < map.landmarks.size(); i++) {
            Point landmark = (Point) map.landmarks.get(i);
            LongLat landmarkLngLat = new LongLat(landmark.longitude(), landmark.latitude());
            landmarksLngLat.add(landmarkLngLat);
        }
        return landmarksLngLat;
    }

    /**
     * Find the best order in a greedy way
     * @return the order with highest price distance ratio.
     */
    public Order findBestOrder() {
        ArrayList<Double> ordersPriceDistanceRatio = new ArrayList<>();

        for (int i = 0; i < this.unfinishedOrders.size(); i++) {
            Order currOrder = unfinishedOrders.get(i);
            double orderDistance = 0;
            int orderPrice = menus.getDeliveryCost(currOrder.getItems());

            // get pickup and target long lat
            LongLat targetPos = findTargetPos(currOrder);
            ArrayList<LongLat> pickUpPos = findPickUpPos(currOrder);

            if (pickUpPos.size() == 1) {
                double startToPickUp = dronePos.distanceTo(pickUpPos.get(0));
                double pickUpToTarget = pickUpPos.get(0).distanceTo(targetPos);
                orderDistance = startToPickUp + pickUpToTarget;
            } else {
                // we need to select which pickUp point first
                double firstChoice = estimateStart2Pick2Target(dronePos, pickUpPos.get(0), pickUpPos.get(1),
                        targetPos);

                double secondChoice = estimateStart2Pick2Target(dronePos, pickUpPos.get(1), pickUpPos.get(0),
                        targetPos);
                orderDistance = Math.min(firstChoice, secondChoice);
            }
            double priceDistanceRatio = orderPrice / orderDistance;
            ordersPriceDistanceRatio.add(priceDistanceRatio);
        }
        double maxValue = Collections.max(ordersPriceDistanceRatio);
        int maxIndex = ordersPriceDistanceRatio.indexOf(maxValue);
        Order nextOrder = unfinishedOrders.get(maxIndex);
        return nextOrder;
    }

    /**
     * Find the target position of the order
     * @param order current order
     * @return the LongLat object of the target position of the order
     */
    public LongLat findTargetPos(Order order) {
        jsonParser.readWordsLongLat(order.getDeliverTo());
        W3wCoordinate target = jsonParser.getWordLongLat();
        LongLat targetPos = LongLat.W3wToLongLat(target);
        return targetPos;
    }

    /**
     * Find the pick up position of the order
     * @param order current order
     * @return the ArrayList of LongLat object of the pick up position of the order
     */
    public ArrayList<LongLat> findPickUpPos(Order order) {
        ArrayList<String> items = order.getItems();
        ArrayList<String> pickUpW3Ws = menus.getShopOfOrder(items);
        ArrayList<LongLat> pickUpPos = new ArrayList<>();

        for (String word : pickUpW3Ws) {
            jsonParser.readWordsLongLat(word);
            W3wCoordinate pickUp = jsonParser.getWordLongLat();
            pickUpPos.add(LongLat.W3wToLongLat(pickUp));
        }

        return pickUpPos;
    }

    /**
     * find the best order to pick up using a greedy algorithm
     * @param pickUpPos arraylist of Longlat object of the pick up position
     * @param startPos LongLat of the starting position
     * @param targetPos LongLat of the target position
     * @return the ArrayList of LongLat object of ordered pick up position
     */
    public ArrayList<LongLat> orderPickUpPos(ArrayList<LongLat> pickUpPos, LongLat startPos, LongLat targetPos) {
        double firstChoice = estimateStart2Pick2Target(startPos, pickUpPos.get(0), pickUpPos.get(1),
                targetPos);

        double secondChoice = estimateStart2Pick2Target(startPos, pickUpPos.get(1), pickUpPos.get(0),
                targetPos);

        if (firstChoice > secondChoice) {
            Collections.reverse(pickUpPos);
        }
        return pickUpPos;
    }

    /**
     * Calculate the sum of straight line distance between 4 points
     * @param startPos LongLat of starting position
     * @param pickUpPos1 LongLat of pickup position 1
     * @param pickUpPos2 LongLat of pickup position 2
     * @param targetPos LongLat of target position
     * @return double of the sum of total straight line distance between 4 points
     */
    public double estimateStart2Pick2Target(LongLat startPos, LongLat pickUpPos1,
                                            LongLat pickUpPos2, LongLat targetPos) {
        double startToPickUp1 = startPos.distanceTo(pickUpPos1);
        double pickUp1ToPickUp2 = pickUpPos1.distanceTo(pickUpPos2);
        double pickUp2ToTarget = pickUpPos2.distanceTo(targetPos);
        return (startToPickUp1 + pickUp1ToPickUp2 + pickUp2ToTarget);
    }

    /**
     * Find a better landmark to reach the target position.
     * @param landmarksLngLat list of LongLat of landmarks
     * @param targetPos Longlat of target position
     * @return LongLat of the landmark which has shorter movement from
     * dronePos to landmark, and from landmark reach target position.
     */
    public LongLat findBetterLandmark(List<LongLat> landmarksLngLat, LongLat targetPos) {
        int[] droneToLandmarkToPickUp = new int[landmarksLngLat.size()];

        for (int i = 0; i < landmarksLngLat.size(); i++) {
            LongLat landmark = landmarksLngLat.get(i);
            LongLat tempPos = new LongLat(dronePos.getLongitude(), dronePos.getLatitude());
            int move = 0;

            // trying to move to landmark first
            if (tempPos.isValidMove(landmark, map)) {
                while (!tempPos.closeTo(landmark)) {
                    tempPos = tempPos.moveDrone(landmark);
                    move++;
                }
            } else {
                // if the line between drone and landmark crosses the restricted area
                // set the distance become maximum.
                droneToLandmarkToPickUp[i] = Integer.MAX_VALUE;
            }

            // then trying to move to target position
            if (tempPos.isValidMove(targetPos, map)) {
                while (!tempPos.closeTo(targetPos)) {
                    tempPos = tempPos.moveDrone(targetPos);
                    move++;
                }
                droneToLandmarkToPickUp[i] = move;
            } else {
                droneToLandmarkToPickUp[i] = Integer.MAX_VALUE;
            }
        }

        if (droneToLandmarkToPickUp[0] < droneToLandmarkToPickUp[1]) {
            return landmarksLngLat.get(0);
        } else {
            return landmarksLngLat.get(1);
        }
    }

    /**
     * Move the drone to the target position and update the OrderTrack object
     * @param orderTrack OrderTrack object for tracking the order
     * @param landmarksLngLat list of LongLat of landmarks
     * @param targetPos LongLat of targetPos
     * @return OrderTrack object of the current order
     */
    public OrderTrack getOrderMove(OrderTrack orderTrack, List<LongLat> landmarksLngLat, LongLat targetPos) {
        // check if the straight line between drone position and the target position is crossing the restricted area
        boolean haveStraightLine = dronePos.isValidMove(targetPos, map);

        if (haveStraightLine) {
            // directly move to target position
            while (!dronePos.closeTo(targetPos)) {
                dronePos = dronePos.moveDrone(targetPos);
                orderTrack.orderPoints.add(dronePos.getPoint());
                orderTrack.orderAngles.add(dronePos.getAngle());
                orderTrack.orderMove++;
            }
            // Hovering
            dronePos = dronePos.nextPosition(-999);
            orderTrack.orderPoints.add(dronePos.getPoint());
            orderTrack.orderAngles.add(dronePos.getAngle());
            orderTrack.orderMove++;
        } else {
            // find a landmark, move to landmark and then move to target positon
            LongLat landmark = findBetterLandmark(landmarksLngLat, targetPos);

            while (!dronePos.closeTo(landmark)) {
                dronePos = dronePos.moveDrone(landmark);
                orderTrack.orderPoints.add(dronePos.getPoint());
                orderTrack.orderAngles.add(dronePos.getAngle());
                orderTrack.orderMove++;
            }

            while (!dronePos.closeTo(targetPos)) {
                dronePos = dronePos.moveDrone(targetPos);
                orderTrack.orderPoints.add(dronePos.getPoint());
                orderTrack.orderAngles.add(dronePos.getAngle());
                orderTrack.orderMove++;
            }
            dronePos = dronePos.nextPosition(-999);
            orderTrack.orderPoints.add(dronePos.getPoint());
            orderTrack.orderAngles.add(dronePos.getAngle());
            orderTrack.orderMove++;
        }

        return orderTrack;
    }

    /**
     * write data into deliveries table in database
     * @param orderNo order number of this order
     * @param deliveredTo what3words string of the target position
     * @param costInPence the price of the order in pence
     * @throws SQLException
     */
    public void writeToDeliveries(String orderNo, String deliveredTo, Integer costInPence) throws SQLException {

        PreparedStatement prepStatement = database.getConnection().prepareStatement(
                "insert into deliveries values (?, ?, ?)");

        prepStatement.setString(1, orderNo);
        prepStatement.setString(2, deliveredTo);
        prepStatement.setInt(3, costInPence);
        prepStatement.execute();
    }

    /**
     * write data into flightpath table in database
     * @param orderNo order number of this order
     * @param startIndex the starting index of travelledPoints.
     * @throws SQLException
     */
    public void writeToFlightpath(String orderNo, Integer startIndex) throws SQLException {
        PreparedStatement prepStatement = database.getConnection().prepareStatement(
                "insert into flightpath values (?, ?, ?, ?, ?, ?)");

        for (int i = startIndex; i < travelledPoints.size() - 1; i++) {
            prepStatement.setString(1, orderNo);
            prepStatement.setDouble(2, travelledPoints.get(i).longitude());
            prepStatement.setDouble(3, travelledPoints.get(i).latitude());
            prepStatement.setInt(4, travelledAngles.get(i + 1));
            prepStatement.setDouble(5, travelledPoints.get(i + 1).longitude());
            prepStatement.setDouble(6, travelledPoints.get(i + 1).latitude());
            prepStatement.execute();
        }
    }

    /**
     * Convert the list of travelled points into a GeoJson String
     * @return GeoJson String of the flight path
     */
    public String getGeoJsonPath() {

        LineString flightPathLineString = LineString.fromLngLats(travelledPoints);
        Geometry flightPathGeometry = (Geometry) flightPathLineString;
        Feature flightPathFeature = Feature.fromGeometry(flightPathGeometry);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(flightPathFeature);

        return featureCollection.toJson();
    }

    /**
     * get the percentage monetary value by dividing delivered Price from total price
     * @return double represents the percentage monetary price.
     */
    public double getPercentageMonetary() {
        double totalPrice = 0;
        double deliveriedPrice = 0;

        for (Order deliveriedOrder : finishedOrders) {
            double orderPrice = menus.getDeliveryCost(deliveriedOrder.getItems());
            deliveriedPrice += orderPrice;
        }

        for (Order order : passedOrders) {
            double orderPrice = menus.getDeliveryCost(order.getItems());
            totalPrice += orderPrice;
        }
        totalPrice += deliveriedPrice;

        return deliveriedPrice / totalPrice;
    }

    /**
     * static inner class to track each order delivery
     */
    public static class OrderTrack {
        // variables recording the points, angles and the movement of flights
        List<Point> orderPoints;
        List<Integer> orderAngles;
        int orderMove;

        /**
         * Constructor of OrderTrack
         */
        public OrderTrack() {
            orderPoints = new ArrayList<>();
            orderAngles = new ArrayList<>();
            orderMove = 0;
        }

        // getter
        public List<Point> getOrderPoints() {
            return orderPoints;
        }

        public List<Integer> getOrderAngles() {
            return orderAngles;
        }

        public Integer getOrderMove() {
            return orderMove;
        }
    }


}

