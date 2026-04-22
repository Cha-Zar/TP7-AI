package tn.pathfinding;

import java.util.*;

public class DFS {
    public static SearchResult search(Graph g, String start, String goal) {
        long t0 = System.currentTimeMillis();
        List<SearchStep> steps = new ArrayList<>();
        Deque<List<String>> stack = new ArrayDeque<>();
        Set<String> visited = new LinkedHashSet<>();
        int iter = 0;

        stack.push(new ArrayList<>(List.of(start)));
        visited.add(start);

        while (!stack.isEmpty()) {
            List<String> path = stack.pop();
            String cur = path.get(path.size()-1);

            List<String> openLabels = new ArrayList<>();
            for (List<String> p : stack) openLabels.add(p.get(p.size()-1));
            steps.add(new SearchStep(iter++, cur, openLabels, new LinkedHashSet<>(visited)));

            if (cur.equals(goal)) {
                int cost = BFS.pathCost(g, path);
                return new SearchResult("DFS", path, cost, iter,
                                        System.currentTimeMillis()-t0, steps);
            }

            for (Edge e : g.neighbors(cur)) {
                if (!visited.contains(e.target.name)) {
                    visited.add(e.target.name);
                    List<String> np = new ArrayList<>(path);
                    np.add(e.target.name);
                    stack.push(np);
                }
            }
        }
        return new SearchResult("DFS", iter, System.currentTimeMillis()-t0, steps);
    }
}
