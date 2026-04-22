package tn.pathfinding;
 
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
 
public class MainApp extends Application {
 
    //  Interface themes — UI panels/controls only; map is always LIGHT 
    enum InterfaceTheme {
        DARK ("#111418","#181c22","#2a2f38","#d8dce4","#7a8294","#c8a96e","#111418"),
        LIGHT("#f5f2ed","#e8e4dc","#c0b8a8","#222222","#5a5a6a","#8b5e1a","#ddd9d0"),
        SAND ("#1a1508","#221b0a","#3a2e14","#ede5cc","#9e8a64","#e8c36a","#1a1508"),
        SLATE("#0d1b2a","#122233","#1e3548","#cde0f0","#6a8aaa","#ffcc44","#0d1b2a");
 
        final String bg, panel, border, text, sub, accent, input;
        InterfaceTheme(String bg, String panel, String border,
                       String text, String sub, String accent, String input) {
            this.bg = bg; this.panel = panel; this.border = border;
            this.text = text; this.sub = sub; this.accent = accent; this.input = input;
        }
 
        /** Generates a full CSS stylesheet string for this interface theme. */
        String toCSS() {
            return ("""
.root { -fx-background-color: BG; }
.if-panel-r { -fx-background-color: PANEL; -fx-border-color: BORDER; -fx-border-width: 0 1 0 0; }
.if-panel-l { -fx-background-color: PANEL; -fx-border-color: BORDER; -fx-border-width: 0 0 0 1; }
.if-header  { -fx-background-color: PANEL; -fx-border-color: BORDER; -fx-border-width: 0 0 1 0; }
.if-result  { -fx-background-color: PANEL; -fx-border-color: BORDER; -fx-border-width: 1 0 0 0; }
.if-section { -fx-background-color: PANEL; -fx-border-color: BORDER; -fx-border-width: 0 0 1 0; }
.if-text    { -fx-text-fill: TEXT; }
.if-sub     { -fx-text-fill: SUB; }
.if-accent  { -fx-text-fill: ACCENT; }
.if-list    { -fx-background-color: INPUT; -fx-border-color: BORDER;
              -fx-control-inner-background: INPUT; }
.if-list .list-cell { -fx-background-color: INPUT; -fx-text-fill: TEXT;
                      -fx-font-family: 'IBM Plex Mono'; -fx-font-size: 10px; }
.if-list .list-cell:filled:selected { -fx-background-color: PANEL; }
.if-list .list-cell:filled:hover    { -fx-background-color: BORDER; }
.if-combo .combo-box-base { -fx-background-color: INPUT; -fx-border-color: BORDER; }
.if-combo .combo-box-base .list-cell {
    -fx-text-fill: TEXT; -fx-background-color: INPUT;
    -fx-font-family: 'IBM Plex Sans'; -fx-font-size: 13px; }
.if-combo .combo-box-base .arrow-button { -fx-background-color: INPUT; }
.if-combo .combo-box-base .arrow-button .arrow { -fx-background-color: SUB; }
.if-combo .combo-box-popup .list-view { -fx-background-color: PANEL; -fx-border-color: BORDER; }
.if-combo .combo-box-popup .list-cell {
    -fx-background-color: PANEL; -fx-text-fill: TEXT; -fx-font-family: 'IBM Plex Sans'; }
.if-combo .combo-box-popup .list-cell:filled:hover { -fx-background-color: BORDER; }
.if-btn-primary         { -fx-background-color: ACCENT; -fx-text-fill: BG; }
.if-btn-primary:hover   { -fx-background-color: derive(ACCENT, 15%); }
.if-btn-secondary       { -fx-background-color: BORDER; -fx-text-fill: SUB; }
.if-btn-secondary:hover { -fx-background-color: derive(BORDER, 25%); }
.if-radio               { -fx-text-fill: TEXT; }
.if-radio .radio        { -fx-border-color: SUB; }
.if-radio:selected .dot { -fx-background-color: ACCENT; }
.if-slider .track       { -fx-background-color: BORDER; }
.if-slider .thumb       { -fx-background-color: ACCENT; -fx-border-color: derive(ACCENT,-20%); }
.if-sep > .line         { -fx-border-color: BORDER; -fx-border-width: 1; }
""")
            .replace("BG",     bg)
            .replace("PANEL",  panel)
            .replace("BORDER", border)
            .replace("TEXT",   text)
            .replace("SUB",    sub)
            .replace("ACCENT", accent)
            .replace("INPUT",  input);
        }
    }
 
    //  Fields 
    private Graph              graph;
    private HaversineHeuristic haversine = new HaversineHeuristic();
 
    private MapCanvas            mapCanvas;
    private ComboBox<String>     cmbStart, cmbGoal;
    private ToggleGroup          algoGroup    = new ToggleGroup();
    private ToggleGroup          heurGroup    = new ToggleGroup();
    private ToggleGroup          ifThemeGroup = new ToggleGroup();
    private ListView<String>     lstOpen, lstClosed;
    private Label                lblStep, lblAlgo, lblCost, lblExplored, lblTime, lblPath;
    private Button               btnPrev, btnNext, btnPlay;
    private Slider               stepSlider;
 
    private String               clickPhase  = "start";
    private SearchResult         lastResult;
    private int                  currentStep = 0;
    private boolean              playing     = false;
    private ScheduledExecutorService scheduler;
 
    private Scene                scene;
 
    //  Lifecycle 
    @Override
    public void start(Stage stage) throws Exception {
        graph     = CSVLoader.loadGraph("data");
        mapCanvas = new MapCanvas(760, 900);
        mapCanvas.setGraph(graph);
        mapCanvas.setOnCityClick(this::handleCityClick);
 
        BorderPane root = new BorderPane();
        root.setLeft(buildLeftPanel());
        root.setCenter(buildMapArea());
        root.setRight(buildStepPanel());
        root.setBottom(buildResultBar());
        root.setTop(buildHeader());
 
        scene = new Scene(root, 1280, 820);
        applyInterfaceTheme(InterfaceTheme.DARK);
 
        stage.setTitle("Tunisia Pathfinding");
        stage.setScene(scene);
        stage.show();
    }
 
    /**
     * Swaps the scene stylesheet to the chosen interface theme.
     * The map canvas is ALWAYS rendered in Theme.LIGHT regardless.
     */
    private void applyInterfaceTheme(InterfaceTheme t) {
        String encoded = URLEncoder.encode(t.toCSS(), StandardCharsets.UTF_8)
                                   .replace("+", "%20");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("data:text/css," + encoded);
        mapCanvas.setTheme(Theme.LIGHT);   // map never changes
    }
 
    //  UI builders 
 
    private Node buildHeader() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("if-header");
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
 
        Label title = new Label("TUNISIA  —  PATHFINDING");
        title.getStyleClass().add("if-accent");
        title.setStyle("-fx-font-weight:600;-fx-letter-spacing:0.12em;" +
                       "-fx-font-family:'IBM Plex Mono',monospace;-fx-font-size:13px;");
 
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
 
        Label thLbl = new Label("INTERFACE");
        thLbl.getStyleClass().add("if-sub");
        thLbl.setStyle("-fx-font-size:10px;-fx-font-family:'IBM Plex Mono';");
 
        HBox themeBar = new HBox(8);
        themeBar.setAlignment(Pos.CENTER);
        for (InterfaceTheme t : InterfaceTheme.values()) {
            RadioButton rb = new RadioButton(t.name());
            rb.setToggleGroup(ifThemeGroup);
            rb.setUserData(t);
            rb.getStyleClass().add("if-radio");
            rb.setStyle("-fx-font-size:10px;-fx-font-family:'IBM Plex Mono';");
            if (t == InterfaceTheme.DARK) rb.setSelected(true);
            rb.setOnAction(e -> applyInterfaceTheme((InterfaceTheme) rb.getUserData()));
            themeBar.getChildren().add(rb);
        }
 
        bar.getChildren().addAll(title, spacer, thLbl, themeBar);
        return bar;
    }
 
    private VBox buildLeftPanel() {
        VBox panel = new VBox();
        panel.setPrefWidth(240);
        panel.getStyleClass().add("if-panel-r");
        panel.getChildren().addAll(
            section("ALGORITHM", buildAlgoGrid()),
            section("ROUTE",     buildRouteSection()),
            section("HEURISTIC", buildHeuristicSection()),
            buildActionSection()
        );
        return panel;
    }
 
    private VBox buildAlgoGrid() {
        String[][] algos = {
            {"BFS","bfs"},  {"DFS","dfs"},
            {"UCS","ucs"},  {"Best-First","bestfirst"},
            {"A*","astar"},  {"Bidir A*","bidir"}
        };
        GridPane grid = new GridPane();
        grid.setHgap(6); grid.setVgap(6);
        int col = 0, row = 0;
        for (String[] a : algos) {
            RadioButton btn = algoBtn(a[0], a[1]);
            if (a[1].equals("bfs")) btn.setSelected(true);
            grid.add(btn, col, row);
            if (++col == 2) { col = 0; row++; }
        }
        return new VBox(grid);
    }
 
    private VBox buildRouteSection() {
        VBox box = new VBox(6);
        List<String> names = new ArrayList<>(graph.getCityNames());
        Collections.sort(names);
 
        cmbStart = styledCombo(names, "Select or click map");
        cmbGoal  = styledCombo(names, "Select or click map");
 
        cmbStart.setOnAction(e -> { mapCanvas.setStart(cmbStart.getValue()); clickPhase = "goal";  });
        cmbGoal .setOnAction(e -> { mapCanvas.setGoal (cmbGoal .getValue()); clickPhase = "start"; });
 
        box.getChildren().addAll(sub("START"), cmbStart, sub("DESTINATION"), cmbGoal);
        return box;
    }
 
    private VBox buildHeuristicSection() {
        VBox box = new VBox(6);
        RadioButton rbHav = heurBtn("Haversine (GPS)", "haversine");
        rbHav.setSelected(true);
        Label note = new Label("(used for Best-First, Bidir-A*)");
        note.getStyleClass().add("if-sub");
        note.setStyle("-fx-font-size:9px;-fx-font-family:'IBM Plex Mono';");
        note.setWrapText(true);
        box.getChildren().addAll(rbHav, note);
        return box;
    }
 
    private VBox buildActionSection() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(14, 16, 14, 16));
 
        Button btnRun   = actionBtn("RUN",   true);
        Button btnClear = actionBtn("CLEAR", false);
        btnPrev  = actionBtn("PREV",  false);
        btnNext  = actionBtn("NEXT",  false);
        btnPlay  = actionBtn("PLAY",  false);
 
        stepSlider = new Slider(0, 0, 0);
        stepSlider.setBlockIncrement(1);
        stepSlider.setShowTickMarks(false);
        stepSlider.getStyleClass().add("if-slider");
 
        btnRun.setMaxWidth(Double.MAX_VALUE);
        btnClear.setMaxWidth(Double.MAX_VALUE);
 
        HBox stepCtrl = new HBox(4, btnPrev, btnNext, btnPlay);
        stepCtrl.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnNext, Priority.ALWAYS);
 
        btnRun  .setOnAction(e -> runSearch());
        btnClear.setOnAction(e -> clearAll());
        btnPrev .setOnAction(e -> gotoStep(currentStep - 1));
        btnNext .setOnAction(e -> gotoStep(currentStep + 1));
        btnPlay .setOnAction(e -> togglePlay());
        stepSlider.valueProperty().addListener((obs, o, n) -> gotoStep(n.intValue()));
 
        setStepControlsEnabled(false);
 
        box.getChildren().addAll(btnRun, btnClear,
            separator(), stepCtrl, stepSlider,
            separator(), buildLegend());
        return box;
    }
 
    private VBox buildLegend() {
        VBox box = new VBox(5);
        box.getChildren().add(sub("LEGEND"));
        String[][] items = {
            {"#5b9bd5","City node"},
            {"#4caf7d","Start"},
            {"#e05252","Destination"},
            {"#e8b84b","Path"},
            {"#e87d3e","Open (frontier)"},
            {"#9e6ecc","Closed (visited)"}
        };
        for (String[] item : items) {
            Label dot = new Label("  ");
            dot.setStyle("-fx-background-color:" + item[0] + ";" +
                         "-fx-background-radius:50%;-fx-min-width:10;-fx-min-height:10;" +
                         "-fx-max-width:10;-fx-max-height:10;");
            Label lbl = new Label(item[1]);
            lbl.getStyleClass().add("if-sub");
            lbl.setStyle("-fx-font-size:10px;-fx-font-family:'IBM Plex Mono';");
            HBox row = new HBox(8, dot, lbl);
            row.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(row);
        }
        return box;
    }
 
    private StackPane buildMapArea() {
        StackPane sp = new StackPane(mapCanvas);
        mapCanvas.widthProperty().bind(sp.widthProperty());
        mapCanvas.heightProperty().bind(sp.heightProperty());
        return sp;
    }
 
    private VBox buildStepPanel() {
        VBox panel = new VBox();
        panel.setPrefWidth(220);
        panel.getStyleClass().add("if-panel-l");
        panel.setPadding(new Insets(0));
 
        lblStep = new Label("Step —");
        lblStep.getStyleClass().add("if-accent");
        lblStep.setStyle("-fx-font-size:11px;-fx-font-family:'IBM Plex Mono',monospace;");
        lblStep.setPadding(new Insets(14, 16, 8, 16));
 
        lstOpen   = styledListView();
        lstClosed = styledListView();
 
        VBox openBox   = section("OPEN LIST",  new VBox(lstOpen));
        VBox closedBox = section("CLOSED SET", new VBox(lstClosed));
 
        VBox.setVgrow(openBox,   Priority.ALWAYS);
        VBox.setVgrow(closedBox, Priority.ALWAYS);
        VBox.setVgrow(lstOpen,   Priority.ALWAYS);
        VBox.setVgrow(lstClosed, Priority.ALWAYS);
 
        panel.getChildren().addAll(lblStep, openBox, closedBox);
        return panel;
    }
 
    private HBox buildResultBar() {
        HBox bar = new HBox(24);
        bar.getStyleClass().add("if-result");
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
 
        lblAlgo     = resultLabel("—");
        lblCost     = resultLabel("—");
        lblExplored = resultLabel("—");
        lblTime     = resultLabel("—");
        lblPath     = resultLabel("—");
 
        bar.getChildren().addAll(
            resultGroup("ALGORITHM", lblAlgo),
            resultGroup("DISTANCE",  lblCost),
            resultGroup("NODES",     lblExplored),
            resultGroup("TIME",      lblTime),
            resultGroup("PATH",      lblPath)
        );
        return bar;
    }
 
    //  Search 
 
    private void runSearch() {
        String s = cmbStart.getValue();
        String g = cmbGoal.getValue();
        if (s == null || s.isEmpty() || g == null || g.isEmpty() || s.equals(g)) return;
 
        String algo = getSelectedAlgo();
        SearchResult r = switch (algo) {
            case "bfs"       -> BFS.search(graph, s, g);
            case "dfs"       -> DFS.search(graph, s, g);
            case "ucs"       -> UCS.search(graph, s, g);
            case "bestfirst" -> BestFirst.search(graph, s, g, haversine);
            case "astar"     -> AStar.search(graph, s, g, haversine);
            case "bidir"     -> BidirectionalAStar.search(graph, s, g, haversine);
            default          -> BFS.search(graph, s, g);
        };
 
        lastResult  = r;
        currentStep = 0;
        playing     = false;
 
        stepSlider.setMax(r.steps.size() - 1);
        stepSlider.setValue(0);
        setStepControlsEnabled(!r.steps.isEmpty());
 
        if (r.found) {
            mapCanvas.showPath(r.path);
            lblPath.setText(String.join(" > ", r.path));
        } else {
            mapCanvas.showPath(null);
            lblPath.setText("No path found");
        }
 
        lblAlgo.setText(r.algorithm);
        lblCost.setText(r.found ? r.cost + " km" : "—");
        lblExplored.setText(r.nodesExplored + " nodes");
        lblTime.setText(r.timeMs + " ms");
 
        if (!r.steps.isEmpty()) gotoStep(0);
    }
 
    private void gotoStep(int idx) {
        if (lastResult == null || lastResult.steps.isEmpty()) return;
        idx = Math.max(0, Math.min(idx, lastResult.steps.size() - 1));
        currentStep = idx;
        stepSlider.setValue(idx);
 
        SearchStep step = lastResult.steps.get(idx);
        lblStep.setText(String.format("Step %d / %d   current: %s",
            idx + 1, lastResult.steps.size(), step.current));
 
        lstOpen.setItems(FXCollections.observableArrayList(step.open));
        lstClosed.setItems(FXCollections.observableArrayList(new ArrayList<>(step.closed)));
        mapCanvas.showStep(step);
 
        if (lastResult.found && idx == lastResult.steps.size() - 1)
            mapCanvas.showPath(lastResult.path);
    }
 
    private void togglePlay() {
        if (playing) {
            playing = false;
            btnPlay.setText("PLAY");
            if (scheduler != null) scheduler.shutdownNow();
        } else {
            playing = true;
            btnPlay.setText("PAUSE");
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
                if (currentStep < lastResult.steps.size() - 1) {
                    gotoStep(currentStep + 1);
                } else {
                    playing = false;
                    btnPlay.setText("PLAY");
                    scheduler.shutdownNow();
                }
            }), 0, 300, TimeUnit.MILLISECONDS);
        }
    }
 
    private void clearAll() {
        cmbStart.setValue(null);
        cmbGoal.setValue(null);
        mapCanvas.setStart(null);
        mapCanvas.setGoal(null);
        mapCanvas.clearHighlights();
        lastResult  = null;
        currentStep = 0;
        playing     = false;
        lstOpen.setItems(FXCollections.emptyObservableList());
        lstClosed.setItems(FXCollections.emptyObservableList());
        lblStep.setText("Step —");
        lblAlgo.setText("—"); lblCost.setText("—");
        lblExplored.setText("—"); lblTime.setText("—"); lblPath.setText("—");
        setStepControlsEnabled(false);
        clickPhase = "start";
    }
 
    private void handleCityClick(String city) {
        if (clickPhase.equals("start")) {
            cmbStart.setValue(city); mapCanvas.setStart(city); clickPhase = "goal";
        } else {
            cmbGoal.setValue(city);  mapCanvas.setGoal(city);  clickPhase = "start";
        }
    }
 
    //  Helpers 
 
    private String getSelectedAlgo() {
        Toggle t = algoGroup.getSelectedToggle();
        return t == null ? "bfs" : (String) t.getUserData();
    }
 
    private RadioButton algoBtn(String label, String key) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(algoGroup);
        rb.setUserData(key);
        rb.getStyleClass().add("if-radio");
        rb.setStyle("-fx-font-size:11px;-fx-font-family:'IBM Plex Mono',monospace;");
        return rb;
    }
 
    private RadioButton heurBtn(String label, String key) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(heurGroup);
        rb.setUserData(key);
        rb.getStyleClass().add("if-radio");
        rb.setStyle("-fx-font-size:11px;-fx-font-family:'IBM Plex Mono',monospace;");
        return rb;
    }
 
    private ComboBox<String> styledCombo(List<String> items, String prompt) {
        ComboBox<String> cb = new ComboBox<>();
        cb.setPromptText(prompt);
        cb.getItems().addAll(items);
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.getStyleClass().add("if-combo");
        cb.setStyle("-fx-font-family:'IBM Plex Sans',sans-serif;-fx-font-size:13px;");
        return cb;
    }
 
    private ListView<String> styledListView() {
        ListView<String> lv = new ListView<>();
        lv.setPrefHeight(140);
        lv.getStyleClass().add("if-list");
        return lv;
    }
 
    private Button actionBtn(String text, boolean primary) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add(primary ? "if-btn-primary" : "if-btn-secondary");
        b.setStyle("-fx-font-family:'IBM Plex Mono',monospace;" +
                   "-fx-font-size:11px;-fx-font-weight:600;-fx-padding:8;");
        return b;
    }
 
    private void setStepControlsEnabled(boolean on) {
        btnPrev.setDisable(!on);
        btnNext.setDisable(!on);
        btnPlay.setDisable(!on);
        stepSlider.setDisable(!on);
    }
 
    /** Section container — only layout is inline; colors come from CSS class. */
    private VBox section(String title, javafx.scene.Node content) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14, 16, 14, 16));
        box.getStyleClass().add("if-section");
        box.getChildren().addAll(sub(title), content);
        return box;
    }
 
    /** Small secondary/subtext label. */
    private Label sub(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("if-sub");
        l.setStyle("-fx-font-size:10px;-fx-font-family:'IBM Plex Mono',monospace;-fx-font-weight:500;");
        return l;
    }
 
    private Label resultLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("if-accent");
        l.setStyle("-fx-font-family:'IBM Plex Mono',monospace;-fx-font-size:12px;-fx-font-weight:600;");
        l.setMaxWidth(200);
        l.setWrapText(true);
        return l;
    }
 
    private VBox resultGroup(String key, Label val) {
        return new VBox(2, sub(key), val);
    }
 
    private Separator separator() {
        Separator sep = new Separator();
        sep.getStyleClass().add("if-sep");
        return sep;
    }
 
    @Override
    public void stop() {
        if (scheduler != null) scheduler.shutdownNow();
    }
 
    public static void main(String[] args) { launch(args); }
}