package uk.ac.ed.inf;

/**
 * A LongLat class represents a point with longitude and latitude.
 *
 * <p>A LongLat object could be used to represents a point or the position of a drone
 * with the two variables longitude and latitude.</p>
 *
 * @author Ruibo Zhang
 */
public class LongLat {


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
     * Returns {@code true} if the point within the drone confinement area.
     *
     * <p>The drone confinement area is a rectangle. There are some landmark of this area.
     * For latitude, Buccleuch St bus stop is the lower bound and KFC is the upper bound.
     * For longitude, Forrest hill is the lower bound and KFC is the upper bound.</p>
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
     * Return a Pythagorean distance in degrees between current point and the input parameter point.
     *
     * @param longLat a LongLat object represents a point
     * @return a double represents the pythagorean distance in degrees between two points
     */
    public double distanceTo(LongLat longLat) {
        double longitudeDifference = this.longitude - longLat.longitude;
        double latitudeDifference = this.latitude - longLat.latitude;
        return Math.sqrt(longitudeDifference * longitudeDifference + latitudeDifference * latitudeDifference);
    }

    /**
     * Return {@code true} if the distance between current point and the input parameter point
     * are less than a tolerant distance(0.00015 degrees).
     *
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
     * Return a new point depends on the input angle to determine the next position of the
     * drone.
     *
     * <p>The angle for hovering will be set to -999. The moving angle for the drone could
     * only be the multiple of 10 between 0 and 350</p>
     *
     * @param angle angle of the next movement of the drone
     * @return a LongLat object represents the next position of the drone
     */
    public LongLat nextPosition(int angle) {
        if (angle == Constants.HOVERING_ANGLE) {
            return this;
        } else if (angle < 0 || angle > 350 || angle % 10 != 0) {
            throw new IllegalArgumentException("Moving angle must be a multiple of 10 between 0 and 350.");
        } else {
            /*
               using Trigonometric function could get the next position easily
             */
            this.longitude += Constants.LENGTH_OF_DRONE_MOVE * Math.cos(Math.toRadians(angle));
            this.latitude += Constants.LENGTH_OF_DRONE_MOVE * Math.sin(Math.toRadians(angle));
        }
        return this;
    }
}