package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class LruPageReplacementWindow extends BaseAlgorithmWindow {

    public static class PageFrameRow {
        private final int stepNumber;
        private final String incomingPage;
        private final String frameState;
        private final String statusResult; // HIT or FAULT

        public PageFrameRow(int stepNumber, String incomingPage, String frameState, String statusResult) {
            this.stepNumber = stepNumber;
            this.incomingPage = incomingPage;
            this.frameState = frameState;
            this.statusResult = statusResult;
        }

        public int getStepNumber() { return stepNumber; }
        public String getIncomingPage() { return incomingPage; }
        public String getFrameState() { return frameState; }
        public String getStatusResult() { return statusResult; }
    }

    private final ObservableList<PageFrameRow> executionHistory = FXCollections.observableArrayList();
    private final FlowPane visualCacheContainer = new FlowPane();
    private final TextArea txtSimLog = new TextArea();
    private final Label lblStatsCounter = new Label("Page Faults: 0 | Page Hits: 0 | Ratio: 0.0%");

    // LRU Engine State Simulation Storage
    private final int MAX_FRAMES = 4;
    private final ArrayList<Integer> cacheFrames = new ArrayList<>();
    private final HashMap<Integer, Integer> pageLastUsedTimeMap = new HashMap<>();
    private int logicalClock = 0;
    private int totalFaults = 0;
    private int totalHits = 0;
    private int stepCounter = 0;

    public LruPageReplacementWindow(StackPane parentContainer) {
        super("Core OS Module: Least Recently Used (LRU) Page Replacement Simulator", parentContainer);

        // --- 1. Top Panel Inputs ---
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        Label lblPrompt = new Label("Request Page Frame Reference Number:");
        lblPrompt.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 12px;");

        TextField txtPageId = new TextField();
        txtPageId.setPromptText("Page (e.g. 7)");
        txtPageId.setPrefWidth(120);
        txtPageId.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white; -fx-prompt-text-fill: #9aa0a6; -fx-background-radius: 4;");

        Button btnRequestPage = new Button("Access Page Frame");
        btnRequestPage.setStyle("-fx-background-color: #00E676; -fx-text-fill: #121212; -fx-font-weight: bold; -fx-cursor: hand;");
        
        Button btnClearCache = new Button("Flush MMU Cache Cache");
        btnClearCache.setStyle("-fx-background-color: rgba(234, 67, 53, 0.2); -fx-text-fill: #FF8A80; -fx-border-color: #EA4335; -fx-background-radius: 4; -fx-border-radius: 4; -fx-cursor: hand;");

        inputRow.getChildren().addAll(lblPrompt, txtPageId, btnRequestPage, btnClearCache);

        // --- 2. Live Cache Registry Status Bar ---
        VBox statusPanel = new VBox(10);
        statusPanel.setPadding(new Insets(12));
        statusPanel.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 8; -fx-border-color: #3c4043;");
        
        lblStatsCounter.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        statusPanel.getChildren().addAll(new Label("RAM PHYSICAL PAGE FRAMES LOOKUP (Capacity Bound Size: 4):"), visualCacheContainer, lblStatsCounter);

        // --- 3. Split Layout: History Log Matrix Table & Console Terminal ---
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(15);
        VBox.setVgrow(mainGrid, Priority.ALWAYS);

        ColumnConstraints colTable = new ColumnConstraints();
        colTable.setPercentWidth(55);
        ColumnConstraints colLog = new ColumnConstraints();
        colLog.setPercentWidth(45);
        mainGrid.getColumnConstraints().addAll(colTable, colLog);

        // History Data Table Map
        TableView<PageFrameRow> table = new TableView<>(executionHistory);
        table.setStyle("-fx-background-color: #202124; -fx-border-color: #3c4043;");
        GridPane.setVgrow(table, Priority.ALWAYS);

        TableColumn<PageFrameRow, Integer> colStep = new TableColumn<>("Step");
        colStep.setCellValueFactory(new PropertyValueFactory<>("stepNumber"));
        colStep.setPrefWidth(60);

        TableColumn<PageFrameRow, String> colIncoming = new TableColumn<>("Referenced Node");
        colIncoming.setCellValueFactory(new PropertyValueFactory<>("incomingPage"));

        TableColumn<PageFrameRow, String> colState = new TableColumn<>("Memory Allocation Vector Snapshot");
        colState.setCellValueFactory(new PropertyValueFactory<>("frameState"));

        TableColumn<PageFrameRow, String> colRes = new TableColumn<>("MMU Interruption Status");
        colRes.setCellValueFactory(new PropertyValueFactory<>("statusResult"));

        table.getColumns().addAll(colStep, colIncoming, colState, colRes);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        mainGrid.add(table, 0, 0);

        // Right Side System Logger Terminal Box
        VBox logBox = new VBox(5);
        Label lblLogTitle = new Label("Kernel MMU Page Allocation Log Terminal:");
        lblLogTitle.setStyle("-fx-text-fill: #8ab4f8; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        txtSimLog.setEditable(false);
        txtSimLog.setWrapText(true);
        txtSimLog.setStyle("-fx-control-inner-background: #151515; -fx-text-fill: #00E676; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");
        VBox.setVgrow(txtSimLog, Priority.ALWAYS);
        logBox.getChildren().addAll(lblLogTitle, txtSimLog);
        mainGrid.add(logBox, 1, 0);

        // Preload layout visuals rendering configuration
        renderVisualCacheBlocks();
        logWrite("Memory Management Unit: Virtual LRU Core Engine online. Mapped segment frame pointers initialized.");

        // --- INTERACTIVE EVENTS ---
        btnRequestPage.setOnAction(e -> {
            String rawInput = txtPageId.getText().trim();
            if (rawInput.isEmpty()) return;

            try {
                int pageVal = Integer.parseInt(rawInput);
                triggerPageRequest(pageVal);
                txtPageId.clear();
            } catch (NumberFormatException ex) {
                logWrite("Format Trap Error: Referenced page tag must be numerical identifiers.");
            }
        });

        btnClearCache.setOnAction(e -> {
            cacheFrames.clear();
            pageLastUsedTimeMap.clear();
            executionHistory.clear();
            logicalClock = 0; totalFaults = 0; totalHits = 0; stepCounter = 0;
            lblStatsCounter.setText("Page Faults: 0 | Page Hits: 0 | Ratio: 0.0%");
            renderVisualCacheBlocks();
            logWrite("Cache Flush Interruption Vector executed. RAM reference matrices wiped clean.");
        });

        Label sectionLabel = new Label("LRU Cache Segment Execution Transition Trace Matrix:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(statusPanel, sectionLabel, inputRow, mainGrid);
    }

    private void triggerPageRequest(int pageValue) {
        logicalClock++;
        stepCounter++;
        boolean isHit = cacheFrames.contains(pageValue);
        String actionStateResult;
        
        pageLastUsedTimeMap.put(pageValue, logicalClock); // Update Last Used timestamp marker register

        if (isHit) {
            totalHits++;
            actionStateResult = "PAGE HIT";
            logWrite("MMU Hit Event: Target Page [" + pageValue + "] resides in L1 physical cache block matrix. Recycled registry offset timestamp reference.");
        } else {
            totalFaults++;
            actionStateResult = "PAGE FAULT";
            
            if (cacheFrames.size() < MAX_FRAMES) {
                cacheFrames.add(pageValue);
                logWrite("MMU Trap Fault: Page [" + pageValue + "] not in memory core pool. Free slot detected -> Pushed onto hardware frame partition index.");
            } else {
                // Find LRU element index victim
                int victimPage = -1;
                int minTimeValue = Integer.MAX_VALUE;
                
                for (int cachedPage : cacheFrames) {
                    int lastUsedTime = pageLastUsedTimeMap.get(cachedPage);
                    if (lastUsedTime < minTimeValue) {
                        minTimeValue = lastUsedTime;
                        victimPage = cachedPage;
                    }
                }
                
                int victimIndex = cacheFrames.indexOf(victimPage);
                cacheFrames.set(victimIndex, pageValue);
                logWrite("LRU Eviction Engine Engaged: Cache saturation threshold reached. Victim Node identified [" + victimPage + "] (Least Recently Accessed). Swapped with [" + pageValue + "].");
            }
        }

        // Calculate dynamic statistic metrics safely
        double hitRatio = ((double) totalHits / (totalHits + totalFaults)) * 100;
        lblStatsCounter.setText(String.format("Page Faults: %d | Page Hits: %d | Hit Ratio: %.1f%%", totalFaults, totalHits, hitRatio));

        // Format snapshot for layout data row mapping tracking update
        String frameStateSnapshot = cacheFrames.toString();
        executionHistory.add(0, new PageFrameRow(stepCounter, String.valueOf(pageValue), frameStateSnapshot, actionStateResult));
        
        renderVisualCacheBlocks();
    }

    private void renderVisualCacheBlocks() {
        visualCacheContainer.getChildren().clear();
        visualCacheContainer.setHgap(10);
        visualCacheContainer.setVgap(10);

        for (int i = 0; i < MAX_FRAMES; i++) {
            VBox frameBox = new VBox(5);
            frameBox.setAlignment(Pos.CENTER);
            frameBox.setPadding(new Insets(8));
            frameBox.setPrefSize(120, 75);

            Label lblSlotTitle = new Label("FRAME_INDEX_" + i);
            lblSlotTitle.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 10px; -fx-font-weight: bold;");

            Label lblContent = new Label();
            lblContent.setStyle("-fx-text-fill: white; -fx-font-family: 'Consolas'; -fx-font-size: 15px; -fx-font-weight: bold;");

            Label lblAgeMarker = new Label();
            lblAgeMarker.setStyle("-fx-text-fill: #FBBC05; -fx-font-size: 9px;");

            if (i < cacheFrames.size()) {
                int pageNum = cacheFrames.get(i);
                lblContent.setText("PAGE: " + pageNum);
                lblAgeMarker.setText("Age/Last Access Ref: t=" + pageLastUsedTimeMap.get(pageNum));
                frameBox.setStyle("-fx-background-color: #1a73e8; -fx-background-radius: 6; -fx-border-color: #8ab4f8; -fx-border-width: 1.5;");
            } else {
                lblContent.setText("NULL");
                lblAgeMarker.setText("Empty Block Segment");
                frameBox.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-border-color: #4f5053; -fx-border-width: 1;");
            }

            frameBox.getChildren().addAll(lblSlotTitle, lblContent, lblAgeMarker);
            visualCacheContainer.getChildren().add(frameBox);
        }
    }

    private void logWrite(String logMessage) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        txtSimLog.appendText("[" + timestamp + "] " + logMessage + "\n");
    }
}