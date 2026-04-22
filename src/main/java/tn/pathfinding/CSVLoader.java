package tn.pathfinding;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CSVLoader {

    // Loads cities.csv and distances.csv from the given folder
    public static Graph loadGraph(String dataFolder) throws IOException {
        Graph g = new Graph();

        // cities
        try (BufferedReader r = open(dataFolder, "cities.csv")) {
            String line = r.readLine(); // header
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                g.addCity(new City(p[0].trim(),
                                   Double.parseDouble(p[1].trim()),
                                   Double.parseDouble(p[2].trim())));
            }
        }

        // edges
        try (BufferedReader r = open(dataFolder, "distances.csv")) {
            String line = r.readLine(); // header
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                g.addEdge(p[0].trim(), p[1].trim(),
                          Integer.parseInt(p[2].trim()));
            }
        }

        return g;
    }

    // Loads heuristics.csv (estimated coords for manual heuristic)
    public static ManualHeuristic loadManualHeuristic(String dataFolder)
            throws IOException {
        ManualHeuristic mh = new ManualHeuristic();
        try (BufferedReader r = open(dataFolder, "heuristics.csv")) {
            String line = r.readLine(); // header
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                mh.loadCoords(p[0].trim(),
                              Double.parseDouble(p[1].trim()),
                              Double.parseDouble(p[2].trim()));
            }
        }
        return mh;
    }

    // Tries classpath first, then filesystem
    private static BufferedReader open(String folder, String filename)
            throws IOException {
        String resource = "/" + filename;
        InputStream is = CSVLoader.class.getResourceAsStream(resource);
        if (is != null)
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        return new BufferedReader(new FileReader(
               new File(folder, filename), StandardCharsets.UTF_8));
    }
}
