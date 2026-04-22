package tn.pathfinding;

public interface Heuristic {
    double estimate(City from, City to);
    String label();
}
