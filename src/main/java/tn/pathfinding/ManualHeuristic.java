package tn.pathfinding;

import java.util.HashMap;
import java.util.Map;

// Uses rounded estimated coordinates loaded from heuristics.csv.
// Distance computed as flat-earth Euclidean (what you'd measure on a paper map).
public class ManualHeuristic implements Heuristic {
    private final Map<String, double[]> coords = new HashMap<>();

    // Surcharge pour TP7 : charge h(n) direct
    public void loadCoords(String city, double h, double dummy) {
        coords.put(city, new double[]{h, 0});
    }

    // Pour format (Ville,h_km,Justification)
    public void loadHeuristicValue(String city, double h) {
        coords.put(city, new double[]{h, 0});
    }

    @Override
    public double estimate(City a, City b) {
        double[] ca = coords.get(a.name);
        double[] cb = coords.get(b.name);
        // Si format TP7 (h(n)), retourne la valeur absolue de h(a) - h(b)
        if (ca != null && cb != null && ca[1] == 0 && cb[1] == 0) {
            return Math.abs(ca[0] - cb[0]);
        }
        // Sinon, comportement original (coords)
        if (ca == null || cb == null) {
            ca = new double[]{a.lat, a.lon};
            cb = new double[]{b.lat, b.lon};
        }
        double dlat = (ca[0] - cb[0]) * 111.0;
        double dlon = (ca[1] - cb[1]) * 92.0;
        return Math.sqrt(dlat*dlat + dlon*dlon);
    }

    @Override public String label() { return "Manual (flat-earth, rounded)"; }
}
