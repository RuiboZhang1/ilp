package uk.ac.ed.inf;

public class Drone {

    private int remainMovement;
    private LongLat dronePos;
    private Map map;


    public Drone(Map map) {
        this.remainMovement = Constants.MAXIMUM_MOVE_BY_DRONE;
        this.dronePos = new LongLat(Constants.APPLETON_TOWER_LONGITUDE, Constants.APPLETON_TOWER_LATITUDE);
        this.map = map;
    }


}
