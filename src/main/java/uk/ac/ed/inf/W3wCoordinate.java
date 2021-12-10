package uk.ac.ed.inf;

/**
 * Class for deserializing the what3words Json string using the reflection api.
 */
public class W3wCoordinate {

    // public variable
    public Coordinates coordinates;

    // static inner class for deserialization
    public static class Coordinates {
        double lng;
        double lat;
    }

}
