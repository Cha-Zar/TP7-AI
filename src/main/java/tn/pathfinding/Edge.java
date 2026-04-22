package tn.pathfinding;

public class Edge {
    public final City   target;
    public final int    distance;

    public Edge(City target, int distance) {
        this.target   = target;
        this.distance = distance;
    }
}
