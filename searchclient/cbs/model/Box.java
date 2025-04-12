package searchclient.cbs.model;

import searchclient.Color;

public class Box extends MovableObj {

    public Box(char uniqueId, Color color) {
        super(ObjectType.BOX, uniqueId, color);
    }

    public Box copy() {
        return (Box) super.copy();
    }

}
