package tn.pathfinding;
 
import java.util.*;
 
/**
 * Bidirectional A*.
 * Two simultaneous A* searches: forward (start→goal) and backward (goal→start).
 * Stops when the best known meeting-path is confirmed optimal.
 *
 * BUG FIX: previously stored meetCity = fn.city (the forward expander) instead
 * of the actual junction node, dropping the connecting edge and producing paths
 * through non-adjacent cities (e.g. Siliana→Tataouine).
 * Now we track meetFwd (last forward node) + meetBwd (first backward node)
 * separately so the full edge is preserved in path reconstruction.
 */
public class BidirectionalAStar {
 
    private record Node(String city, int g, double f, String parent)
            implements Comparable<Node> {
        public int compareTo(Node o) { return Double.compare(this.f, o.f); }
    }
 
    public static SearchResult search(Graph g, String start, String goal,
                                      Heuristic h) {
        long t0 = System.currentTimeMillis();
        List<SearchStep> steps = new ArrayList<>();
        City startCity = g.getCity(start);
        City goalCity  = g.getCity(goal);
        int iter = 0;
 
        // ── forward structures 
        PriorityQueue<Node> fOpen   = new PriorityQueue<>();
        Map<String,Integer> fDist   = new HashMap<>();
        Map<String,String>  fPrev   = new HashMap<>();
        Set<String>         fClosed = new LinkedHashSet<>();
 
        // ── backward structures ─────────────────────────────────────────────
        PriorityQueue<Node> bOpen   = new PriorityQueue<>();
        Map<String,Integer> bDist   = new HashMap<>();
        Map<String,String>  bPrev   = new HashMap<>();
        Set<String>         bClosed = new LinkedHashSet<>();
 
        fDist.put(start, 0);
        fOpen.add(new Node(start, 0, h.estimate(startCity, goalCity), null));
 
        bDist.put(goal, 0);
        bOpen.add(new Node(goal,  0, h.estimate(goalCity,  startCity), null));
 
        int    best     = Integer.MAX_VALUE;
        String meetFwd  = null;   // last forward  node on the meeting edge
        String meetBwd  = null;   // first backward node on the meeting edge
 
        while (!fOpen.isEmpty() && !bOpen.isEmpty()) {
 
            // ── forward step ──
            Node fn = fOpen.poll();
            if (!fClosed.contains(fn.city)) {
                fClosed.add(fn.city);
                if (fn.parent != null) fPrev.put(fn.city, fn.parent);
 
                recordStep(steps, iter++, "[F] " + fn.city,
                           fOpen, bOpen, fClosed, bClosed);
 
                for (Edge e : g.neighbors(fn.city)) {
                    int ng = fn.g + e.distance;
 
                    // relax forward distance
                    if (ng < fDist.getOrDefault(e.target.name, Integer.MAX_VALUE)) {
                        fDist.put(e.target.name, ng);
                        double nf = ng + h.estimate(e.target, goalCity);
                        fOpen.add(new Node(e.target.name, ng, nf, fn.city));
                    }
 
                    // meeting-point check: fn.city --edge--> e.target in bClosed
                    if (bClosed.contains(e.target.name)) {
                        int bCost = bDist.getOrDefault(e.target.name, Integer.MAX_VALUE);
                        if (bCost < Integer.MAX_VALUE) {
                            int total = fn.g + e.distance + bCost;
                            if (total < best) {
                                best    = total;
                                meetFwd = fn.city;          // last forward  node
                                meetBwd = e.target.name;    // first backward node
                            }
                        }
                    }
                }
            }
 
            // ── backward step ─
            Node bn = bOpen.poll();
            if (!bClosed.contains(bn.city)) {
                bClosed.add(bn.city);
                if (bn.parent != null) bPrev.put(bn.city, bn.parent);
 
                recordStep(steps, iter++, "[B] " + bn.city,
                           fOpen, bOpen, fClosed, bClosed);
 
                for (Edge e : g.neighbors(bn.city)) {
                    int ng = bn.g + e.distance;
 
                    // relax backward distance
                    if (ng < bDist.getOrDefault(e.target.name, Integer.MAX_VALUE)) {
                        bDist.put(e.target.name, ng);
                        double nf = ng + h.estimate(e.target, startCity);
                        bOpen.add(new Node(e.target.name, ng, nf, bn.city));
                    }
 
                    // meeting-point check: bn.city --edge--> e.target in fClosed
                    if (fClosed.contains(e.target.name)) {
                        int fCost = fDist.getOrDefault(e.target.name, Integer.MAX_VALUE);
                        if (fCost < Integer.MAX_VALUE) {
                            int total = bn.g + e.distance + fCost;
                            if (total < best) {
                                best    = total;
                                meetFwd = e.target.name;    // last forward  node
                                meetBwd = bn.city;          // first backward node
                            }
                        }
                    }
                }
            }
 
            //termination condition
            double fBest = fOpen.isEmpty() ? Double.MAX_VALUE : fOpen.peek().f;
            double bBest = bOpen.isEmpty() ? Double.MAX_VALUE : bOpen.peek().f;
            if (meetFwd != null && fBest + bBest >= best) break;
        }
 
        if (meetFwd == null)
            return new SearchResult("Bidir-A*", iter,
                                    System.currentTimeMillis()-t0, steps);
 
        List<String> path = buildPath(start, goal, meetFwd, meetBwd, fPrev, bPrev);
        return new SearchResult("Bidir-A*", path, best, iter,
                                System.currentTimeMillis()-t0, steps);
    }
 
    //  helpers 
 
    private static void recordStep(List<SearchStep> steps, int iter,
                                   String current,
                                   PriorityQueue<Node> fOpen,
                                   PriorityQueue<Node> bOpen,
                                   Set<String> fClosed, Set<String> bClosed) {
        List<String> openLabels = new ArrayList<>();
        for (Node n : fOpen) openLabels.add("F:" + n.city);
        for (Node n : bOpen) openLabels.add("B:" + n.city);
        Set<String> allClosed = new LinkedHashSet<>(fClosed);
        allClosed.addAll(bClosed);
        steps.add(new SearchStep(iter, current, openLabels, allClosed));
    }
 
    /**
     * Reconstruct: start → ... → meetFwd → meetBwd → ... → goal
     *
     * fPrev traces backward from meetFwd to start.
     * bPrev traces backward from meetBwd to goal.
     * The edge meetFwd→meetBwd is inserted explicitly (they may be the same node
     * when both searches closed on the exact same city, in which case it's skipped).
     */
    private static List<String> buildPath(String start, String goal,
                                          String meetFwd, String meetBwd,
                                          Map<String,String> fPrev,
                                          Map<String,String> bPrev) {
        // forward half: meetFwd → start (reversed)
        LinkedList<String> fHalf = new LinkedList<>();
        String cur = meetFwd;
        while (cur != null) {
            fHalf.addFirst(cur);
            cur = fPrev.get(cur);
        }
        // safety: if start wasn't traced all the way, prepend it
        if (!fHalf.isEmpty() && !fHalf.getFirst().equals(start))
            fHalf.addFirst(start);
 
        // backward half: meetBwd → goal (forward direction)
        List<String> bHalf = new ArrayList<>();
        // only add meetBwd if it's different from meetFwd (avoid duplicate)
        if (!meetBwd.equals(meetFwd))
            bHalf.add(meetBwd);
 
        cur = bPrev.get(meetBwd);
        while (cur != null) {
            bHalf.add(cur);
            cur = bPrev.get(cur);
        }
        // safety: ensure goal is at the end
        if (bHalf.isEmpty() || !bHalf.get(bHalf.size()-1).equals(goal))
            bHalf.add(goal);
 
        List<String> full = new ArrayList<>(fHalf);
        full.addAll(bHalf);
        return full;
    }
}