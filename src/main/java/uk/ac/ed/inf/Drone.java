package uk.ac.ed.inf;

import com.mapbox.geojson.*;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Drone {

    private int remainMove;
    private LongLat dronePos;
    private LongLat terminatePos;
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

        // load the order list from database to unfinishedOrders
        unfinishedOrders = database.getOrderList();
        Order nextOrder = null;

        traveledPoints.add(dronePos.getPoint());
        traveledAngles.add(dronePos.getAngle());

        while (!unfinishedOrders.isEmpty()) {
            // use sorting to find the best order to deliver first
            nextOrder = findBestOrder();
            List<Point> orderPoints = new ArrayList<>();
            List<Integer> orderAngles = new ArrayList<>();

            // find the pickup location and target location
            LongLat targetPos = findTargetPos(nextOrder);
            ArrayList<LongLat> pickUpPos = findPickUpPos(nextOrder);
            if (pickUpPos.size() > 1) {
                pickUpPos = orderPickUpPos(pickUpPos, this.dronePos, targetPos);
            }

            int orderMove = 0;
            LongLat originPos = dronePos;

            // use moveDrone to move towards pickup location first, then go to target location
            for (LongLat pickUp : pickUpPos) {
                while (!dronePos.closeTo(pickUp)) {
                    dronePos = dronePos.moveDrone(map, pickUp);
                    orderPoints.add(dronePos.getPoint());
                    orderAngles.add(dronePos.getAngle());
                    orderMove++;
                }
                dronePos = dronePos.nextPosition(-999);
                orderPoints.add(dronePos.getPoint());
                orderAngles.add(dronePos.getAngle());
                orderMove++;
            }

            while (!dronePos.closeTo(targetPos)) {
                dronePos = dronePos.moveDrone(map, targetPos);
                orderPoints.add(dronePos.getPoint());
                orderAngles.add(dronePos.getAngle());
                orderMove++;
            }
            dronePos = dronePos.nextPosition(-999);
            orderPoints.add(dronePos.getPoint());
            orderAngles.add(dronePos.getAngle());
            orderMove++;

            LongLat tempPos = dronePos;

            int moveBackTermination = 0;
            // find the distance to go back appleton
            while (!tempPos.closeTo(terminatePos)) {
                tempPos = tempPos.moveDrone(map, terminatePos);
                moveBackTermination++;
            }

            // if the order is valid to deliver
            if (orderMove + moveBackTermination <= remainMove) {
                remainMove -= orderMove;
                unfinishedOrders.remove(nextOrder);
                finishedOrders.add(nextOrder);
                traveledPoints.addAll(orderPoints);
                traveledAngles.addAll(orderAngles);

                PreparedStatement prepStatement = database.getConnection().prepareStatement(
                        "insert into deliveries values (?, ?, ?)");

                prepStatement.setString(1, nextOrder.getOrderNo());
                prepStatement.setString(2, nextOrder.getDeliverTo());
                prepStatement.setInt(3, menus.getDeliveryCost(nextOrder.getItems()));
                prepStatement.execute();

                prepStatement = database.getConnection().prepareStatement(
                        "insert into flightpath values (?, ?, ?, ?, ?, ?)");

                for (int i=0; i < traveledPoints.size()-1; i++) {
                    prepStatement.setString(1, nextOrder.getOrderNo());
                    prepStatement.setDouble(2, traveledPoints.get(i).longitude());
                    prepStatement.setDouble(3, traveledPoints.get(i).latitude());
                    prepStatement.setInt(4, traveledAngles.get(i));
                    prepStatement.setDouble(5, traveledPoints.get(i+1).longitude());
                    prepStatement.setDouble(6, traveledPoints.get(i+1).latitude());
                    prepStatement.execute();
                }
            } else {
                dronePos = originPos;
                unfinishedOrders.remove(nextOrder);
            }
        }

        int returnPoint = traveledPoints.size();

        while (!dronePos.closeTo(terminatePos)) {
            dronePos.moveDrone(map, terminatePos);
            traveledPoints.add(dronePos.getPoint());
            traveledAngles.add(dronePos.getAngle());
            remainMove--;
        }
        dronePos.nextPosition(-999);
        traveledPoints.add(dronePos.getPoint());
        traveledAngles.add(dronePos.getAngle());
        remainMove--;

        PreparedStatement prepStatement = database.getConnection().prepareStatement(
                "insert into flightpath values (?, ?, ?, ?, ?, ?)");
        for (int i=returnPoint; i<traveledPoints.size()-1; i++) {
            prepStatement.setString(1, nextOrder.getOrderNo());
            prepStatement.setDouble(2, traveledPoints.get(i).longitude());
            prepStatement.setDouble(3, traveledPoints.get(i).latitude());
            prepStatement.setInt(4, traveledAngles.get(i));
            prepStatement.setDouble(5, traveledPoints.get(i+1).longitude());
            prepStatement.setDouble(6, traveledPoints.get(i+1).latitude());
            prepStatement.execute();
        }

        System.out.println("save deliveries and flightpath in database");

        // remember the amount of movement they made

        // record the point of movement

        // remove the order from unfinished order, add it into finished orders

        // determine if the remain movement enough to do next deliveries or go back to appleton

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
        LineString lineString = LineString.fromLngLats(traveledPoints);
        Geometry geometry = (Geometry) lineString;
        Feature feature = Feature.fromGeometry(geometry);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);

        return featureCollection.toJson();

    }


}
