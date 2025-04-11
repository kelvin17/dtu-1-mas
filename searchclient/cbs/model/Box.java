package searchclient.cbs.model;

import searchclient.Color;

public class Box {
    private final char boxLetter;
    private final Color color;
    private final Location initlocation;
    private Location goallocation;

    public Box(char boxLetter, Color color, Location initlocation) {
        this.boxLetter = boxLetter;
        this.color = color;
        this.initlocation = initlocation;
    }

    public Location getGoallocation() {
        return goallocation;
    }

    public void setGoallocation(Location goallocation) {
        this.goallocation = goallocation;
    }
}
