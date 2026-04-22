package tn.pathfinding;

import javafx.scene.paint.Color;

public enum Theme {
    DARK (
        Color.web("#111418"), Color.web("#181c22"), Color.web("#d8dce4"),
        Color.rgb(255,255,255,0.12), Color.web("#5b9bd5"), Color.web("#e8b84b"),
        Color.web("#4caf7d"), Color.web("#e05252"),
        Color.web("#e87d3e"), Color.web("#9e6ecc"), Color.web("#e0e0e0")
    ),
    LIGHT(
        Color.web("#f0ede8"), Color.web("#e4e0d8"), Color.web("#1e1e1e"),
        Color.rgb(0,0,0,0.12), Color.web("#2b6dac"), Color.web("#d4880f"),
        Color.web("#1e7d48"), Color.web("#c03030"),
        Color.web("#e87d3e"), Color.web("#7c4dbc"), Color.web("#333333")
    ),
    SAND (
        Color.web("#1a1508"), Color.web("#221b0a"), Color.web("#ede5cc"),
        Color.rgb(255,220,100,0.1), Color.web("#e87d3e"), Color.web("#f0d060"),
        Color.web("#6cbf6c"), Color.web("#e05252"),
        Color.web("#e8c36a"), Color.web("#b07ad8"), Color.web("#ede5cc")
    ),
    SLATE(
        Color.web("#0d1b2a"), Color.web("#122233"), Color.web("#cde0f0"),
        Color.rgb(100,181,246,0.1), Color.web("#64b5f6"), Color.web("#ffcc44"),
        Color.web("#81c784"), Color.web("#ef5350"),
        Color.web("#ffb74d"), Color.web("#ba68c8"), Color.web("#cde0f0")
    );

    public final Color bg, panel, text, edge, cityDot, pathColor;
    public final Color startColor, goalColor, openColor, closedColor, label;

    Theme(Color bg, Color panel, Color text, Color edge, Color cityDot,
          Color pathColor, Color startColor, Color goalColor,
          Color openColor, Color closedColor, Color label) {
        this.bg         = bg;
        this.panel      = panel;
        this.text       = text;
        this.edge       = edge;
        this.cityDot    = cityDot;
        this.pathColor  = pathColor;
        this.startColor = startColor;
        this.goalColor  = goalColor;
        this.openColor  = openColor;
        this.closedColor= closedColor;
        this.label      = label;
    }

    public String cssRoot() {
        return String.format(
            ".root { -fx-base: %s; -fx-background: %s; }",
            toHex(panel), toHex(bg));
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
            (int)(c.getRed()*255),
            (int)(c.getGreen()*255),
            (int)(c.getBlue()*255));
    }
}
