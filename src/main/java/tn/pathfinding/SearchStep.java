package tn.pathfinding;

import java.util.*;

public class SearchStep {
    public final int           iteration;
    public final String        current;
    public final List<String>  open;     // open list / frontier entries (label)
    public final Set<String>   closed;   // closed / visited set

    public SearchStep(int iteration, String current,
                      List<String> open, Set<String> closed) {
        this.iteration = iteration;
        this.current   = current;
        this.open      = new ArrayList<>(open);
        this.closed    = new LinkedHashSet<>(closed);
    }
}
