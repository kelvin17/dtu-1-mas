package searchclient.cbs.model;

public class LowLevelGoalLocation {

    private final char letter;
    private final Location location;

    public LowLevelGoalLocation(char letter, Location location) {
        this.letter = letter;
        this.location = location;
    }

    public char getLetter() {
        return letter;
    }

    public Location getLocation() {
        return location;
    }
}
