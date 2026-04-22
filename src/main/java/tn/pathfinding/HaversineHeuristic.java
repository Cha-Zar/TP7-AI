package tn.pathfinding;

public class HaversineHeuristic implements Heuristic {
    private static final double R = 6371.0;

    @Override
    public double estimate(City a, City b) {
        double lat1 = Math.toRadians(a.lat);
        double lat2 = Math.toRadians(b.lat);
        double dLat = Math.toRadians(b.lat - a.lat);
        double dLon = Math.toRadians(b.lon - a.lon);
        double x = Math.sin(dLat/2)*Math.sin(dLat/2)
                 + Math.cos(lat1)*Math.cos(lat2)*Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1-x));
    }

    @Override public String label() { return "Haversine (GPS)"; }
}
