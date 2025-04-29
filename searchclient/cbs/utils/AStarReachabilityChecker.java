package searchclient.cbs.utils;

import searchclient.cbs.model.Environment;
import searchclient.cbs.model.Location;

import java.util.*;

public class AStarReachabilityChecker {

    private static class Node {
        Location loc;
        int g;

        Node(Location loc, int g) {
            this.loc = loc;
            this.g = g;
        }
    }

    /**
     * A* search to determine whether two points are reachable.
     *
     * @param start starting location (agent start)
     * @param goal  target location (box location)
     * @param env   environment with wall info
     * @return true if reachable, false otherwise
     */
    public boolean reachable(Location start, Location goal, Environment env) {
        if (start.equals(goal)) {
            return true;
        }

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(n -> n.g + heuristic(n.loc, goal)));
        Set<Location> visited = new HashSet<>();

        frontier.add(new Node(start, 0));
        visited.add(start);

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            if (current.loc.equals(goal)) {
                return true;
            }

            for (Location neighbor : getNeighbors(current.loc, env)) {
                if (!visited.contains(neighbor)) {
                    frontier.add(new Node(neighbor, current.g + 1));
                    visited.add(neighbor); // Add here to avoid duplicates
                }
            }
        }

        return false;
    }

    private List<Location> getNeighbors(Location loc, Environment env) {
        List<Location> neighbors = new ArrayList<>();
        int[] dr = {-1, 1, 0, 0}; // up, down
        int[] dc = {0, 0, -1, 1}; // left, right

        for (int i = 0; i < 4; i++) {
            int nr = loc.getRow() + dr[i];
            int nc = loc.getCol() + dc[i];
            Location next = new Location(nr, nc);
            if (!env.isWall(next)) {
                neighbors.add(next);
            }
        }
        return neighbors;
    }

    private int heuristic(Location from, Location to) {
        return Math.abs(from.getRow() - to.getRow()) + Math.abs(from.getCol() - to.getCol());
    }
}
