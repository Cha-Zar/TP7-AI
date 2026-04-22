package tn.pathfinding;

import java.util.*;

public class SearchResult {
    public final boolean        found;
    public final List<String>   path;
    public final int            cost;
    public final int            nodesExplored;
    public final long           timeMs;
    public final List<SearchStep> steps;
    public final String         algorithm;

    // success
    public SearchResult(String algorithm, List<String> path, int cost,
                        int explored, long timeMs, List<SearchStep> steps) {
        this.found         = true;
        this.algorithm     = algorithm;
        this.path          = path;
        this.cost          = cost;
        this.nodesExplored = explored;
        this.timeMs        = timeMs;
        this.steps         = steps;
    }

    // failure
    public SearchResult(String algorithm, int explored, long timeMs,
                        List<SearchStep> steps) {
        this.found         = false;
        this.algorithm     = algorithm;
        this.path          = Collections.emptyList();
        this.cost          = -1;
        this.nodesExplored = explored;
        this.timeMs        = timeMs;
        this.steps         = steps;
    }
}
