package searchclient.cbs.model;


public class Box {

    private final MovableObj innerObj;
    private Location currentLocation;

    public Box(MovableObj innerObj, Location currentLocation) {
        this.innerObj = innerObj;
        this.currentLocation = currentLocation;
    }

    public static Box buildFromMovableObj(MovableObj box) {
        return new Box(box, box.getInitLocation());
    }

    public Box copy() {
        return new Box(this.innerObj, this.currentLocation.copy());
    }

    public Location getInitLocation() {
        return this.innerObj.getInitLocation();
    }

    public Location getGoalLocation() {
        return this.innerObj.getGoalLocation();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
