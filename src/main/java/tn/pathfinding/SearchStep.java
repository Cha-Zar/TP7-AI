package tn.pathfinding;

import java.util.*;

public class SearchStep {
    public final int           iteration;
    public final String        current;
    public final String        currentDetails;
    public final List<String>  open;     // open list / frontier entries (label)
    public final Set<String>   closed;   // closed / visited set

    public SearchStep(int iteration, String current,
                      List<String> open, Set<String> closed) {
        this(iteration, current, null, open, closed);
    }

    public SearchStep(int iteration, String current, String currentDetails,
                      List<String> open, Set<String> closed) {
        this.iteration = iteration;
        this.current   = current;
        this.currentDetails = currentDetails;
        this.open      = new ArrayList<>(open);
        this.closed    = new LinkedHashSet<>(closed);
        printToConsole();
    }

    private void printToConsole() {
        System.out.println("Iteration " + (iteration + 1));
        if (currentDetails != null && !currentDetails.isBlank()) {
            System.out.println("Noeud courant : " + current + " | " + currentDetails);
        } else {
            System.out.println("Noeud courant : " + current);
        }
        System.out.println("Open list : " + open);
        System.out.println("Closed list: " + new ArrayList<>(closed));
        System.out.println();
    }
}
