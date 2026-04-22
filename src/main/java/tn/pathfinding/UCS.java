package tn.pathfinding;

import java.util.*;

public class UCS {
    private record Node(String city, int g, List<String> path)
            implements Comparable<Node> {
        public int compareTo(Node o) { return Integer.compare(this.g, o.g); }
    }

    public static SearchResult search(Graph g, String start, String goal) {
        long t0 = System.currentTimeMillis();
        List<SearchStep> steps = new ArrayList<>();
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<String,Integer> bestG = new HashMap<>();
        Set<String> closed = new LinkedHashSet<>();
        int iter = 0;

        open.add(new Node(start, 0, new ArrayList<>(List.of(start))));
        bestG.put(start, 0);

        while (!open.isEmpty()) {
            Node n = open.poll();
            if (closed.contains(n.city)) continue;
            closed.add(n.city);

            if (n.city.equals(goal)) {
                List<String> openLabels = new ArrayList<>();
                for (Node x : open) openLabels.add(x.city+"("+x.g+"km)");
                steps.add(new SearchStep(iter++, n.city, openLabels, new LinkedHashSet<>(closed)));
                return new SearchResult("UCS", n.path, n.g, iter,
                                        System.currentTimeMillis()-t0, steps);
            }

            for (Edge e : g.neighbors(n.city)) {
                int ng = n.g + e.distance;
                if (ng < bestG.getOrDefault(e.target.name, Integer.MAX_VALUE)) {
                    bestG.put(e.target.name, ng);
                    List<String> np = new ArrayList<>(n.path);
                    np.add(e.target.name);
                    open.add(new Node(e.target.name, ng, np));
                }
            }

            List<String> openLabels = new ArrayList<>();
            for (Node x : open) openLabels.add(x.city+"("+x.g+"km)");
            steps.add(new SearchStep(iter++, n.city, openLabels, new LinkedHashSet<>(closed)));
        }
        return new SearchResult("UCS", iter, System.currentTimeMillis()-t0, steps);
    }
}
