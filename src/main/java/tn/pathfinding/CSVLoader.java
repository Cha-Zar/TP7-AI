package tn.pathfinding;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CSVLoader {

    // Loads cities.csv and distances.csv from the given folder
    public static Graph loadGraph(String dataFolder) throws IOException {
        Graph g = new Graph();

        // Choix des fichiers selon mode TP7 ou non
        String citiesFile = "cities.csv";
        String distancesFile = "distances.csv";
        String folder = dataFolder;
        if (MainApp.useTP7Static()) {
            citiesFile = "cities_tp7.csv";
            distancesFile = "distances_tp7.csv";
            folder = "data"; // Toujours lire dans data/
        }

        // cities
        try (BufferedReader r = open(folder, citiesFile)) {
            String header = r.readLine(); // header
            String line;
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
        try (BufferedReader r = open(folder, distancesFile)) {
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
        String heuristicsFile = MainApp.useTP7Static() ? "heuristics_tp7.csv" : "heuristics.csv";
        String heuristicsFolder = MainApp.useTP7Static() ? "data" : dataFolder;
        try (BufferedReader r = open(heuristicsFolder, heuristicsFile)) {
            String header = r.readLine();
            boolean isTP7 = header.toLowerCase().contains("h_km");
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (isTP7) {
                    mh.loadHeuristicValue(p[0].trim(), Double.parseDouble(p[1].trim()));
                } else {
                    mh.loadCoords(p[0].trim(),
                                 Double.parseDouble(p[1].trim()),
                                 Double.parseDouble(p[2].trim()));
                }
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
