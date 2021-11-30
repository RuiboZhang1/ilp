package uk.ac.ed.inf;

/**
 * A LongLat class represents a point or the position of a drone with longitude and latitude.
 */
public class LongLat {

    // variables
    public double longitude;
    public double latitude;

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

    /**
     * Lower bound for the latitude -- Buccleuch St bus stop
     * <br>Upper bound for the latitude -- KFC
     *
     * <br>Lower bound for the longitude -- Forrest hill
     * <br>Upper bound for the longitude -- KFC
     *
     * @return {@code true} if the point within the drone confinement area
     */
    public boolean isConfined() {
        if (this.latitude > Constants.BUCCLEUCH_ST_BUS_STOP_LATITUDE
                && this.latitude < Constants.KFC_LATITUDE
                && this.longitude > Constants.FORREST_HILL_LONGITUDE
                && this.longitude < Constants.KFC_LONGITUDE) {
            return true;
        } else {
            return false;
        }
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
        if (angle == Constants.HOVERING_ANGLE) {
            return this;
        } else if (angle < 0 || angle > 350 || angle % 10 != 0) {
            System.out.println(angle + "is invalid for the angle");
            System.exit(1);
        } else {
            // using Trigonometric function could get the next position easily
            this.longitude += Constants.LENGTH_OF_DRONE_MOVE * Math.cos(Math.toRadians(angle));
            this.latitude += Constants.LENGTH_OF_DRONE_MOVE * Math.sin(Math.toRadians(angle));
        }
        return this;
    }

    public static LongLat convertW3wCoordinateToLongLat(W3wCoordinate w3wCoordinate) {
        return new LongLat(w3wCoordinate.coordinates.lng, w3wCoordinate.coordinates.lat);
    }
}