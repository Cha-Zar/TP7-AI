package tn.pathfinding;
 
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.scene.shape.StrokeLineCap;
import java.util.*;
 
public class MapCanvas extends Canvas {
 
    // Tunisia bounding box (degrees) mapped onto the 687x1040 GIF
    private static final double LAT_MAX = 37.72, LAT_MIN = 29.80;
    private static final double LON_MIN = 7.30,  LON_MAX = 12.05;
    private static final double IMG_W = 687, IMG_H = 1040;
    private static final double PX_L = 28, PX_R = 678;
    private static final double PY_T = 6,  PY_B = 935;
 
    private Graph  graph;
    private Image  mapImage;
    private Theme  theme = Theme.LIGHT;  // map is always LIGHT
 
    private String startCity = null;
    private String goalCity  = null;
    private Set<String> openSet   = new HashSet<>();
    private Set<String> closedSet = new HashSet<>();
    private List<String> path     = new ArrayList<>();
 
    // callback when a city is clicked
    private java.util.function.Consumer<String> onCityClick;
 
    public MapCanvas(double w, double h) {
        super(w, h);
        widthProperty().addListener(e -> draw());
        heightProperty().addListener(e -> draw());
        setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
        setCursor(javafx.scene.Cursor.CROSSHAIR);
 
        try {
            mapImage = new Image(
                MapCanvas.class.getResourceAsStream("/Carte.gif"),
                0, 0, true, true);
        } catch (Exception ex) {
            mapImage = null;
        }
    }
 
    public void setGraph(Graph g)                  { this.graph = g; draw(); }
    public void setTheme(Theme t)                  { this.theme = t; draw(); }
    public void setStart(String s)                 { startCity = s; draw(); }
    public void setGoal(String g)                  { goalCity  = g; draw(); }
    public void setOnCityClick(java.util.function.Consumer<String> cb) { onCityClick = cb; }
 
    public void showStep(SearchStep step) {
        closedSet = new HashSet<>(step.closed);
        openSet   = new HashSet<>();
        for (String s : step.open) {
            // labels may have suffix like "(g=50)" — strip it
            String name = s.split("\\(")[0].replaceAll("^[FB]:", "").trim();
            openSet.add(name);
        }
        draw();
    }
 
    public void showPath(List<String> p) {
        path = p == null ? new ArrayList<>() : p;
        draw();
    }
 
    public void clearHighlights() {
        openSet.clear(); closedSet.clear(); path.clear(); draw();
    }
 
    // converts lat/lon to canvas pixel, scaled to actual canvas size
    private double[] toPixel(double lat, double lon) {
        double mapX = PX_L + (lon - LON_MIN) / (LON_MAX - LON_MIN) * (PX_R - PX_L);
        double mapY = PY_T + (LAT_MAX - lat) / (LAT_MAX - LAT_MIN) * (PY_B - PY_T);
        double sx = getWidth()  / IMG_W;
        double sy = getHeight() / IMG_H;
        return new double[]{ mapX * sx, mapY * sy };
    }
 
    public void draw() {
        if (graph == null) return;
        double W = getWidth(), H = getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);
 
        // background
        gc.setFill(theme.bg);
        gc.fillRect(0, 0, W, H);
 
        // map image
        if (mapImage != null && !mapImage.isError()) {
            gc.setGlobalAlpha(theme == Theme.LIGHT ? 0.92 : 0.85);
            gc.drawImage(mapImage, 0, 0, W, H);
            gc.setGlobalAlpha(1.0);
        }
 
        // edges
        gc.setStroke(theme.edge);
        gc.setLineWidth(0.8);
        Set<String> drawn = new HashSet<>();
        for (City c : graph.getCities()) {
            for (Edge e : graph.neighbors(c.name)) {
                String key = c.name.compareTo(e.target.name) < 0
                           ? c.name + "|" + e.target.name
                           : e.target.name + "|" + c.name;
                if (drawn.contains(key)) continue;
                drawn.add(key);
                double[] a = toPixel(c.lat, c.lon);
                double[] b = toPixel(e.target.lat, e.target.lon);
                gc.strokeLine(a[0], a[1], b[0], b[1]);
            }
        }
 
        // path
        if (path.size() > 1) {
            gc.setStroke(theme.pathColor);
            gc.setLineWidth(3.0);
            gc.setLineCap(StrokeLineCap.ROUND);
            for (int i = 0; i < path.size()-1; i++) {
                City a = graph.getCity(path.get(i));
                City b = graph.getCity(path.get(i+1));
                if (a == null || b == null) continue;
                double[] pa = toPixel(a.lat, a.lon);
                double[] pb = toPixel(b.lat, b.lon);
                gc.strokeLine(pa[0], pa[1], pb[0], pb[1]);
            }
        }
 
        // cities
        gc.setFont(Font.font("IBM Plex Mono", FontWeight.NORMAL, 9));
        for (City c : graph.getCities()) {
            double[] p = toPixel(c.lat, c.lon);
            Color fill = theme.cityDot;
            double r = 4;
 
            if (c.name.equals(startCity))       { fill = theme.startColor; r = 6; }
            else if (c.name.equals(goalCity))   { fill = theme.goalColor;  r = 6; }
            else if (path.contains(c.name))     { fill = theme.pathColor;  r = 5; }
            else if (openSet.contains(c.name))  { fill = theme.openColor;  r = 5; }
            else if (closedSet.contains(c.name)){ fill = theme.closedColor;r = 5; }
 
            gc.setFill(fill);
            gc.setStroke(theme.bg);
            gc.setLineWidth(1.2);
            gc.fillOval(p[0]-r, p[1]-r, r*2, r*2);
            gc.strokeOval(p[0]-r, p[1]-r, r*2, r*2);
 
            gc.setFill(theme.label);
            gc.fillText(c.name, p[0]+6, p[1]-4);
        }
    }
 
    private void handleClick(double mx, double my) {
        if (graph == null || onCityClick == null) return;
        double threshold = 14;
        for (City c : graph.getCities()) {
            double[] p = toPixel(c.lat, c.lon);
            double d = Math.hypot(mx - p[0], my - p[1]);
            if (d < threshold) { onCityClick.accept(c.name); return; }
        }
    }
 
    @Override public boolean isResizable()               { return true; }
    @Override public double prefWidth(double h)          { return getWidth(); }
    @Override public double prefHeight(double w)         { return getHeight(); }
}
 