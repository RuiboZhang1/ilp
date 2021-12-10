package uk.ac.ed.inf;

import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A LongLat class represents a point or the position of a drone with longitude and latitude.
 */
public class LongLat {

    // private variables
    private double longitude;
    private double latitude;
    private int angle=-999;

    /**
     * Constructs a LongLat object to represent a point with two parameters.
     *
     * @param longitude longitude of the point.
     * @param latitude  latitude of the point.
     */
    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }


    // getter
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getAngle() {
        return angle;
    }

    public Point getPoint() {
        return Point.fromLngLat(longitude, latitude);
    }


    /**
     * @param longLat a LongLat object represents a point
     * @return a double represents the pythagorean distance in degrees between two points
     */
    public double distanceTo(LongLat longLat) {
        double longitudeDifference = this.longitude - longLat.longitude;
        double latitudeDifference = this.latitude - longLat.latitude;
        return Math.sqrt(longitudeDifference * longitudeDifference + latitudeDifference * latitudeDifference);
    }

    /**
     * @param longLat a LongLat object represents a point
     * @return {@code true} if the distance between current point and the input parameter point
     *      * are less than a tolerant distance. distance between current point and the input parameter point
     * are less than a tolerant distance.
     */
    public boolean closeTo(LongLat longLat) {
        if (distanceTo(longLat) < Constants.DISTANCE_TOLERANCE) {
            return true;
        }
        return false;
    }

    /**
     * @param angle angle of the next movement of the drone
     * @return a LongLat object represents the next position of the drone
     */
    public LongLat nextPosition(int angle) {
        LongLat nextPos = new LongLat(0,0);

        if (angle == Constants.HOVERING_ANGLE) {
            this.angle = angle;
            return this;
        } else if (angle < 0 || angle > 350 || angle % 10 != 0) {
            System.err.println(angle + "is invalid for the angle");
            System.exit(1);
        } else {
            // using Trigonometric function could get the next position easily
            nextPos.longitude = this.longitude + Constants.LENGTH_OF_DRONE_MOVE * Math.cos(Math.toRadians(angle));
            nextPos.latitude = this.latitude + Constants.LENGTH_OF_DRONE_MOVE * Math.sin(Math.toRadians(angle));
            nextPos.angle = angle;
        }
        return nextPos;
    }

    /**
     * Check the line between dronePos and the endPos is not intersecting with confinement area or no fly zones
     * @param endPos the target position of the drone movement
     * @param map map contains the information of no fly zones and confinement area
     * @return {@code true} if two lines are not intersecting.
     */
    public boolean isValidMove(LongLat endPos, Map map) {
        boolean isValid = true;
        boolean crossConfinement = false;
        boolean crossNoFlyZone = false;

        // create the line between drone position and the target position
        Line2D curr2End = new Line2D.Double(this.latitude, this.longitude, endPos.latitude, endPos.longitude);

        // check if cross confinement area, loop through all the vertices of the confinement area
        ArrayList<Point> confinementArea = map.confinementArea;

        for (int i = 0; i < confinementArea.size(); i++) {
            int j = (i + 1) % confinementArea.size();
            Line2D confinementLine = new Line2D.Double(
                    confinementArea.get(i).coordinates().get(1),
                    confinementArea.get(i).coordinates().get(0),
                    confinementArea.get(j).coordinates().get(1),
                    confinementArea.get(j).coordinates().get(0));

            if (curr2End.intersectsLine(confinementLine)) {
                crossConfinement = true;
                break;
            }
        }

        // check if cross no fly zones, loop through all the vertices of no fly zones
        ArrayList<Geometry> noFlyZones = map.noFlyZones;
        for (int i = 0; i < noFlyZones.size(); i++) {
            Polygon noFlyZonePoly = (Polygon) noFlyZones.get(i);
            List<Point> polyPoints = noFlyZonePoly.coordinates().get(0);

            for (int j = 0; j < polyPoints.size()-1; j++) {
                Line2D noFlyZoneLine = new Line2D.Double(
                        polyPoints.get(j).latitude(),
                        polyPoints.get(j).longitude(),
                        polyPoints.get(j+1).latitude(),
                        polyPoints.get(j+1).longitude());

                if (curr2End.intersectsLine(noFlyZoneLine)) {
                    crossNoFlyZone = true;
                    break;
                }
            }
        }

        if (crossConfinement || crossNoFlyZone) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * make a single step of the drone
     * @param targetPos target position of the drone
     * @return the next position of the drone after moving a step
     */
    public LongLat moveDrone(LongLat targetPos) {
        this.angle = estimateCurrTargetAngle(this, targetPos);
        LongLat nextPos = nextPosition(this.angle);
        return nextPos;

    }

    /**
     * Return a rounded angle between current position and the target position
     * @param currentPos LongLat object of current position
     * @param targetPos LongLat object of target position
     * @return a rounded angle between 0-350 degrees
     */
    public int estimateCurrTargetAngle(LongLat currentPos, LongLat targetPos) {
        double lngDifference = targetPos.getLongitude() - currentPos.getLongitude();
        double latDifference = targetPos.getLatitude() - currentPos.getLatitude();

        int angle = (int) Math.toDegrees(Math.atan2(latDifference, lngDifference));

        // The angle must between 0 to 350 and the multiple of 10
        if (angle < 0) {
            angle += 360;
        }
        angle = Math.round(angle / 10) * 10;
        return angle;
    }

    /**
     * Convert the W3wCoordinate to a LongLat object
     * @param w3wCoordinate the coordinate of the what3words
     * @return a LongLat object represents the what3words coordinate
     */
    public static LongLat W3wToLongLat(W3wCoordinate w3wCoordinate) {
        return new LongLat(w3wCoordinate.coordinates.lng, w3wCoordinate.coordinates.lat);
    }
}