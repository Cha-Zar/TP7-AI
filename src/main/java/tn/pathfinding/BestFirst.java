package tn.pathfinding;

import java.util.*;

public class BestFirst {
    private record Node(String city, double h, List<String> path)
            implements Comparable<Node> {
        public int compareTo(Node o) { return Double.compare(this.h, o.h); }
    }

    public static SearchResult search(Graph g, String start, String goal,
                                      Heuristic h) {
        long t0 = System.currentTimeMillis();
        List<SearchStep> steps = new ArrayList<>();
        City goalCity = g.getCity(goal);
        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<String> closed = new LinkedHashSet<>();
        int iter = 0;

        open.add(new Node(start, h.estimate(g.getCity(start), goalCity),
                          new ArrayList<>(List.of(start))));

        while (!open.isEmpty()) {
            Node n = open.poll();
            if (closed.contains(n.city)) continue;
            closed.add(n.city);

            if (n.city.equals(goal)) {
                int cost = BFS.pathCost(g, n.path);
                List<String> openLabels = new ArrayList<>();
                for (Node x : open) openLabels.add(String.format("%s(h=%.0f)", x.city, x.h));
                steps.add(new SearchStep(iter++, n.city, String.format("h=%.0f", n.h), openLabels, new LinkedHashSet<>(closed)));
                return new SearchResult("Best-First", n.path, cost, iter,
                                        System.currentTimeMillis()-t0, steps);
            }

            for (Edge e : g.neighbors(n.city)) {
                if (!closed.contains(e.target.name)) {
                    double hn = h.estimate(e.target, goalCity);
                    List<String> np = new ArrayList<>(n.path);
                    np.add(e.target.name);
                    open.add(new Node(e.target.name, hn, np));
                }
            }

            List<String> openLabels = new ArrayList<>();
            for (Node x : open) openLabels.add(String.format("%s(h=%.0f)", x.city, x.h));
            steps.add(new SearchStep(iter++, n.city, String.format("h=%.0f", n.h), openLabels, new LinkedHashSet<>(closed)));
        }
        return new SearchResult("Best-First", iter, System.currentTimeMillis()-t0, steps);
    }
}
