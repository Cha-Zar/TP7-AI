package tn.pathfinding;

import java.util.*;

public class BFS {
    public static SearchResult search(Graph g, String start, String goal) {
        long t0 = System.currentTimeMillis();
        List<SearchStep> steps = new ArrayList<>();
        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new LinkedHashSet<>();
        int iter = 0;

        queue.add(new ArrayList<>(List.of(start)));
        visited.add(start);

        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String cur = path.get(path.size()-1);

            List<String> openLabels = new ArrayList<>();
            for (List<String> p : queue) openLabels.add(p.get(p.size()-1));
            steps.add(new SearchStep(iter++, cur, openLabels, new LinkedHashSet<>(visited)));

            if (cur.equals(goal)) {
                int cost = pathCost(g, path);
                return new SearchResult("BFS", path, cost, iter,
                                        System.currentTimeMillis()-t0, steps);
            }

            for (Edge e : g.neighbors(cur)) {
                if (!visited.contains(e.target.name)) {
                    visited.add(e.target.name);
                    List<String> np = new ArrayList<>(path);
                    np.add(e.target.name);
                    queue.add(np);
                }
            }
        }
        return new SearchResult("BFS", iter, System.currentTimeMillis()-t0, steps);
    }

    static int pathCost(Graph g, List<String> path) {
        int total = 0;
        for (int i = 0; i < path.size()-1; i++)
            total += g.edgeWeight(path.get(i), path.get(i+1));
        return total;
    }
}
