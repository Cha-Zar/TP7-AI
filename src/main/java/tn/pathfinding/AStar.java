package tn.pathfinding;

import java.util.*;

public class AStar {
    private record Node(String city, int g, double f, List<String> path)
            implements Comparable<Node> {
        public int compareTo(Node o) { return Double.compare(this.f, o.f); }
    }

    public static SearchResult search(Graph g, String start, String goal,
                                      Heuristic h) {
        long t0 = System.currentTimeMillis();
        List<SearchStep> steps = new ArrayList<>();
        City goalCity = g.getCity(goal);
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<String,Double> bestF = new HashMap<>();
        Set<String> closed = new LinkedHashSet<>();
        int iter = 0;

        double h0 = h.estimate(g.getCity(start), goalCity);
        open.add(new Node(start, 0, h0, new ArrayList<>(List.of(start))));
        bestF.put(start, h0);

        while (!open.isEmpty()) {
            Node n = open.poll();
            if (closed.contains(n.city)) continue;
            closed.add(n.city);

            if (n.city.equals(goal)) {
                // Record the step before returning (with open/closed in final state)
                List<String> openLabels = new ArrayList<>();
                for (Node x : open)
                    openLabels.add(String.format("%s(g=%d,f=%.0f)", x.city, x.g, x.f));
                steps.add(new SearchStep(iter++, n.city, openLabels, new LinkedHashSet<>(closed)));
                return new SearchResult("A*", n.path, n.g, iter,
                                        System.currentTimeMillis()-t0, steps);
            }

            for (Edge e : g.neighbors(n.city)) {
                if (closed.contains(e.target.name)) continue;
                int ng = n.g + e.distance;
                double hv = h.estimate(e.target, goalCity);
                double nf = ng + hv;
                if (nf < bestF.getOrDefault(e.target.name, Double.MAX_VALUE)) {
                    bestF.put(e.target.name, nf);
                    List<String> np = new ArrayList<>(n.path);
                    np.add(e.target.name);
                    open.add(new Node(e.target.name, ng, nf, np));
                }
            }

            // Now record the step after neighbors are added
            List<String> openLabels = new ArrayList<>();
            for (Node x : open)
                openLabels.add(String.format("%s(g=%d,f=%.0f)", x.city, x.g, x.f));
            steps.add(new SearchStep(iter++, n.city, openLabels, new LinkedHashSet<>(closed)));
        }
        return new SearchResult("A*", iter, System.currentTimeMillis()-t0, steps);
    }
}
