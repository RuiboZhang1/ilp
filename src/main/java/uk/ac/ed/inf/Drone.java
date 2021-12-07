package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Drone {
    private int remainMove;
    private LongLat dronePos;
    private final LongLat terminatePos;

    private Map map;
    private Database database;
    private JsonParser jsonParser;
    private Menus menus;

    private ArrayList<Order> unfinishedOrders = new ArrayList<>();
    private ArrayList<Order> finishedOrders = new ArrayList<>();

    private List<Point> traveledPoints = new ArrayList<>();
    private List<Integer> traveledAngles = new ArrayList<>();


    public Drone(Map map, Database database, JsonParser jsonParser, Menus menus) {
        this.remainMove = Constants.MAXIMUM_MOVE_BY_DRONE;
        this.dronePos = new LongLat(Constants.APPLETON_TOWER_LONGITUDE, Constants.APPLETON_TOWER_LATITUDE);
        this.terminatePos = new LongLat(Constants.APPLETON_TOWER_LONGITUDE, Constants.APPLETON_TOWER_LATITUDE);
        this.map = map;
        this.database = database;
        this.jsonParser = jsonParser;
        this.menus = menus;
    }


    public void startDeliveries() throws SQLException {
        unfinishedOrders = database.getOrderList();

        // record the start position
        traveledPoints.add(dronePos.getPoint());
        traveledAngles.add(dronePos.getAngle());

        Order nextOrder = null;

        int databaseIndex = 0;

        while (!unfinishedOrders.isEmpty()) {
            // use sorting to find the best order to deliver first
            nextOrder = findBestOrder();

            // find the pickup location and target location of this order
            LongLat targetPos = findTargetPos(nextOrder);
            ArrayList<LongLat> pickUpPos = findPickUpPos(nextOrder);

            if (pickUpPos.size() > 1) {
                pickUpPos = orderPickUpPos(pickUpPos, this.dronePos, targetPos);
            }

            // get the list of landmarks longitude and latitude.
            List<LongLat> landmarksLngLat = new ArrayList<>();
            for (int i = 0; i < map.landmarks.size(); i++) {
                Point landmark = (Point) map.landmarks.get(i);
                LongLat landmarkLngLat = new LongLat(landmark.longitude(), landmark.latitude());
                landmarksLngLat.add(landmarkLngLat);
            }

            // record the origin position before sending a order
            LongLat originPos = dronePos;

            OrderTrack currOrderTrack = new OrderTrack();

            // To pickup through landmarks
            for (LongLat pickUp : pickUpPos) {
                currOrderTrack = getOrderMove(currOrderTrack, landmarksLngLat, pickUp);
            }

            // To delivery position through landmarks
            currOrderTrack = getOrderMove(currOrderTrack, landmarksLngLat, targetPos);

            // check if enough move to go back appleton tower
            LongLat tempPos = new LongLat(dronePos.getLongitude(), dronePos.getLatitude());
            int moveBackTermination = 0;
            while (!tempPos.closeTo(terminatePos)) {
                tempPos = tempPos.moveDrone(map, terminatePos);
                moveBackTermination++;
            }

            // if order is valid
            if (currOrderTrack.getOrderMove() + moveBackTermination <= remainMove) {
                unfinishedOrders.remove(nextOrder);
                finishedOrders.add(nextOrder);

                remainMove -= currOrderTrack.getOrderMove();
                traveledPoints.addAll(currOrderTrack.getOrderPoints());
                traveledAngles.addAll(currOrderTrack.getOrderAngles());

                writeToDeliveries(nextOrder.getOrderNo(), nextOrder.getDeliverTo(),
                        menus.getDeliveryCost(nextOrder.getItems()));

                writeToFlightpath(nextOrder.getOrderNo(), databaseIndex);

                databaseIndex = traveledPoints.size() - 1;
            } else {
                // if invalid, the drone return to original position before this order
                dronePos = originPos;
                unfinishedOrders.remove(nextOrder);
            }
        }

        // after finishing all order, now go back to Appleton tower
        while (!dronePos.closeTo(terminatePos)) {
            dronePos = dronePos.moveDrone(map, terminatePos);
            traveledPoints.add(dronePos.getPoint());
            traveledAngles.add(dronePos.getAngle());
            remainMove--;
        }
        dronePos = dronePos.nextPosition(-999);
        traveledPoints.add(dronePos.getPoint());
        traveledAngles.add(dronePos.getAngle());
        remainMove--;

        writeToFlightpath(nextOrder.getOrderNo(), databaseIndex);

        System.out.println("save deliveries and flightpath in database");
    }


    public Order findBestOrder() {
        ArrayList<Double> ordersDistance = new ArrayList<>();
        ArrayList<Integer> ordersPrice = new ArrayList<>();
        ArrayList<Double> ordersPriceDistanceRatio = new ArrayList<>();

        for (int i = 0; i < this.unfinishedOrders.size(); i++) {
            Order currOrder = unfinishedOrders.get(i);
            double orderDistance = 0;
            int orderPrice = menus.getDeliveryCost(currOrder.getItems());
            ordersPrice.add(orderPrice);

            // get start, pickup and target long lat

            LongLat targetPos = findTargetPos(currOrder);
            ArrayList<LongLat> pickUpPos = findPickUpPos(currOrder);

            if (pickUpPos.size() == 1) {
                double startToPickUp = dronePos.distanceTo(pickUpPos.get(0));
                double pickUpToTarget = pickUpPos.get(0).distanceTo(targetPos);
                orderDistance = startToPickUp + pickUpToTarget;
                ordersDistance.add(orderDistance);
            } else {
                // we need to select which pickUp point first
                double firstChoice = estimateStart2Pick2Target(dronePos, pickUpPos.get(0), pickUpPos.get(1),
                        targetPos);

                double secondChoice = estimateStart2Pick2Target(dronePos, pickUpPos.get(1), pickUpPos.get(0),
                        targetPos);
                orderDistance = Math.min(firstChoice, secondChoice);
                ordersDistance.add(orderDistance);
            }

            double priceDistanceRatio = orderPrice / orderDistance;
            ordersPriceDistanceRatio.add(priceDistanceRatio);
        }
        double maxValue = Collections.max(ordersPriceDistanceRatio);
        int maxIndex = ordersPriceDistanceRatio.indexOf(maxValue);
        Order nextOrder = unfinishedOrders.get(maxIndex);
        return nextOrder;
    }

    public LongLat findTargetPos(Order order) {
        jsonParser.readWordsLongLat(order.getDeliverTo());
        W3wCoordinate target = jsonParser.getWordLongLat();
        LongLat targetPos = LongLat.convertW3wCoordinateToLongLat(target);
        return targetPos;
    }

    public ArrayList<LongLat> findPickUpPos(Order order) {
        ArrayList<String> items = order.getItems();
        ArrayList<String> pickUpW3Ws = menus.getShopOfOrder(items);
        ArrayList<LongLat> pickUpPos = new ArrayList<>();

        for (String word : pickUpW3Ws) {
            jsonParser.readWordsLongLat(word);
            W3wCoordinate pickUp = jsonParser.getWordLongLat();
            pickUpPos.add(LongLat.convertW3wCoordinateToLongLat(pickUp));
        }

        return pickUpPos;
    }

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

    public double estimateStart2Pick2Target(LongLat startPos, LongLat pickUpPos1,
                                            LongLat pickUpPos2, LongLat targetPos) {
        double startToPickUp1 = startPos.distanceTo(pickUpPos1);
        double pickUp1ToPickUp2 = pickUpPos1.distanceTo(pickUpPos2);
        double pickUp2ToTarget = pickUpPos2.distanceTo(targetPos);
        return (startToPickUp1 + pickUp1ToPickUp2 + pickUp2ToTarget);
    }

    public String getGeoJsonPath() {

        LineString flightPathLineString = LineString.fromLngLats(traveledPoints);
        Geometry flightPathGeometry = (Geometry) flightPathLineString;
        Feature flightPathFeature = Feature.fromGeometry(flightPathGeometry);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(flightPathFeature);

        return featureCollection.toJson();
    }

    public LongLat findBetterLandmark(List<LongLat> landmarksLngLat, LongLat targetPos) {
        int[] droneToLandmarkToPickUp = new int[2];

        for (int i = 0; i < landmarksLngLat.size(); i++) {
            LongLat landmark = landmarksLngLat.get(i);
            LongLat tempPos = new LongLat(dronePos.getLongitude(), dronePos.getLatitude());
            List<Point> tempPoints = new ArrayList<>();
            List<Integer> tempAngles = new ArrayList<>();
            int move = 0;

            if (tempPos.isValidMove(landmark, map)) {
                while (!tempPos.closeTo(landmark)) {
                    tempPos = tempPos.moveDrone(map, landmark);
                    tempPoints.add(tempPos.getPoint());
                    tempAngles.add(tempPos.getAngle());
                    move++;
                }
            } else {
                droneToLandmarkToPickUp[i] = Integer.MAX_VALUE;
            }

            if (tempPos.isValidMove(targetPos, map)) {
                while (!tempPos.closeTo(targetPos)) {
                    tempPos = tempPos.moveDrone(map, targetPos);
                    tempPoints.add(tempPos.getPoint());
                    tempAngles.add(tempPos.getAngle());
                    move++;
                }
                tempPos = tempPos.nextPosition(-999);
                tempPoints.add(tempPos.getPoint());
                tempAngles.add(tempPos.getAngle());
                move++;

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

    public OrderTrack getOrderMove(OrderTrack orderTrack, List<LongLat> landmarksLngLat, LongLat targetPos) {
        boolean haveStraightLine = dronePos.isValidMove(targetPos, map);

        if (haveStraightLine) {
            while (!dronePos.closeTo(targetPos)) {
                dronePos = dronePos.moveDrone(map, targetPos);
                orderTrack.orderPoints.add(dronePos.getPoint());
                orderTrack.orderAngles.add(dronePos.getAngle());
                orderTrack.orderMove++;
            }
        } else {
            LongLat landmark = findBetterLandmark(landmarksLngLat, targetPos);

            while (!dronePos.closeTo(landmark)) {
                dronePos = dronePos.moveDrone(map, landmark);
                orderTrack.orderPoints.add(dronePos.getPoint());
                orderTrack.orderAngles.add(dronePos.getAngle());
                orderTrack.orderMove++;
            }

            while (!dronePos.closeTo(targetPos)) {
                dronePos = dronePos.moveDrone(map, targetPos);
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

    public void writeToDeliveries(String orderNo, String deliveredTo, Integer costInPence) throws SQLException {

        PreparedStatement prepStatement = database.getConnection().prepareStatement(
                "insert into deliveries values (?, ?, ?)");

        prepStatement.setString(1, orderNo);
        prepStatement.setString(2, deliveredTo);
        prepStatement.setInt(3, costInPence);
        prepStatement.execute();
    }

    public void writeToFlightpath(String orderNo, Integer startIndex) throws SQLException {
        PreparedStatement prepStatement = database.getConnection().prepareStatement(
                "insert into flightpath values (?, ?, ?, ?, ?, ?)");

        for (int i = startIndex; i < traveledPoints.size() - 1; i++) {
            prepStatement.setString(1, orderNo);
            prepStatement.setDouble(2, traveledPoints.get(i).longitude());
            prepStatement.setDouble(3, traveledPoints.get(i).latitude());
            prepStatement.setInt(4, traveledAngles.get(i + 1));
            prepStatement.setDouble(5, traveledPoints.get(i + 1).longitude());
            prepStatement.setDouble(6, traveledPoints.get(i + 1).latitude());
            prepStatement.execute();
        }
    }

    public static class OrderTrack {
        List<Point> orderPoints;
        List<Integer> orderAngles;
        Integer orderMove;

        public OrderTrack() {
            orderPoints = new ArrayList<>();
            orderAngles = new ArrayList<>();
            orderMove = 0;
        }

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

