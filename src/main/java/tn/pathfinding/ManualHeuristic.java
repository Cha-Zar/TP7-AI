package tn.pathfinding;

import java.util.HashMap;
import java.util.Map;

// Uses rounded estimated coordinates loaded from heuristics.csv.
// Distance computed as flat-earth Euclidean (what you'd measure on a paper map).
public class ManualHeuristic implements Heuristic {
    private final Map<String, double[]> coords = new HashMap<>();

    public void loadCoords(String city, double latEst, double lonEst) {
        coords.put(city, new double[]{latEst, lonEst});
    }

    @Override
    public double estimate(City a, City b) {
        double[] ca = coords.get(a.name);
        double[] cb = coords.get(b.name);
        if (ca == null || cb == null) {
            // fall back to true coords if city not in table
            ca = new double[]{a.lat, a.lon};
            cb = new double[]{b.lat, b.lon};
        }
        double dlat = (ca[0] - cb[0]) * 111.0;
        double dlon = (ca[1] - cb[1]) * 92.0;   // ~cos(34°)*111
        return Math.sqrt(dlat*dlat + dlon*dlon);
    }

    @Override public String label() { return "Manual (flat-earth, rounded)"; }
}
