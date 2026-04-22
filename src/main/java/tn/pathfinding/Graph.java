package tn.pathfinding;

import java.util.*;

public class Graph {
    private final Map<String, City>       cities    = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    public void addCity(City c) {
        cities.put(c.name, c);
        adjacency.put(c.name, new ArrayList<>());
    }

    public void addEdge(String a, String b, int dist) {
        City ca = cities.get(a);
        City cb = cities.get(b);
        if (ca == null || cb == null) return;
        adjacency.get(a).add(new Edge(cb, dist));
        adjacency.get(b).add(new Edge(ca, dist));
    }

    public City             getCity(String name)     { return cities.get(name); }
    public Collection<City> getCities()              { return cities.values();  }
    public Set<String>      getCityNames()           { return cities.keySet();  }

    public List<Edge> neighbors(String city) {
        return adjacency.getOrDefault(city, Collections.emptyList());
    }

    public int edgeWeight(String a, String b) {
        for (Edge e : neighbors(a))
            if (e.target.name.equals(b)) return e.distance;
        return Integer.MAX_VALUE;
    }
}
