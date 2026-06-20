package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Aapki custom compiled model import statement
import Process.SegmentData;

public class SegmentationWindow extends BaseAlgorithmWindow {

    // Aapke structural SegmentData object array model par mapped list
    private final ObservableList<SegmentData> segmentList = FXCollections.observableArrayList();
    private final TableView<SegmentData> table = new TableView<>(segmentList);
    private final VBox memoryMapContainer = new VBox(6);
    private final TextArea logArea = new TextArea();

    public SegmentationWindow(StackPane parentContainer) {
        super("Core OS Module: Segment Base & Limit Address Mapping Engine", parentContainer);

        // --- STEP 1: TOP PREMIUM CONTROLLERS ROW (POPUP WINDOW RIGS) ---
        HBox controlRow = new HBox(12);
        controlRow.setAlignment(Pos.CENTER_LEFT);
        controlRow.setPadding(new Insets(5, 0, 5, 0));

        Button btnPopupAdd = new Button("➕ Add Segment Descriptor");
        btnPopupAdd.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16 8 16;");

        Button btnPopupTranslate = new Button("🔍 Translate Address Pointer");
        btnPopupTranslate.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16 8 16;");

        Button btnClearCache = new Button("🗑️ Clear Cache");
        btnClearCache.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16 8 16;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblMmuIndicator = new Label("MMU STATUS: ACTIVE");
        lblMmuIndicator.setStyle("-fx-text-fill: #34A853; -fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-background-color: rgba(52,168,83,0.1); -fx-padding: 6 12 6 12; -fx-background-radius: 4;");

        controlRow.getChildren().addAll(btnPopupAdd, btnPopupTranslate, btnClearCache, spacer, lblMmuIndicator);

        // --- STEP 2: REGISTER DATA TABLE MATRIX ---
        table.setStyle("-fx-background-color: #202124; -fx-border-color: #3c4043; -fx-border-radius: 4;");
        table.setPrefHeight(190);

        // Map directly with your SegmentData field properties identifiers
        TableColumn<SegmentData, String> colName = new TableColumn<>("Segment Scope Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("segmentName"));

        TableColumn<SegmentData, Integer> colBase = new TableColumn<>("Base Hex Address");
        colBase.setCellValueFactory(new PropertyValueFactory<>("baseAddress"));

        TableColumn<SegmentData, Integer> colLimit = new TableColumn<>("Limit Size Bounds");
        colLimit.setCellValueFactory(new PropertyValueFactory<>("limitSize"));

        table.getColumns().addAll(colName, colBase, colLimit);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- STEP 3: SPLIT RESPONSIVE VIEWPORTS LAYOUT (MAP VS DIAGNOSTICS) ---
        GridPane visualGrid = new GridPane();
        visualGrid.setHgap(15);
        VBox.setVgrow(visualGrid, Priority.ALWAYS);

        ColumnConstraints colMap = new ColumnConstraints();
        colMap.setPercentWidth(42); // Scaled allocation space for Visual Bar blocks
        ColumnConstraints colLog = new ColumnConstraints();
        colLog.setPercentWidth(58); // System diagnostics display console logs
        visualGrid.getColumnConstraints().addAll(colMap, colLog);

        // Hardware Real-time scrolling bar canvas layout container
        ScrollPane mapScroll = new ScrollPane(memoryMapContainer);
        mapScroll.setFitToWidth(true);
        mapScroll.setStyle("-fx-background: #1e1f22; -fx-background-color: #1e1f22; -fx-border-color: #3c4043; -fx-border-radius: 4;");
        memoryMapContainer.setPadding(new Insets(10));
        memoryMapContainer.setStyle("-fx-background-color: #1e1f22;");
        VBox.setVgrow(mapScroll, Priority.ALWAYS);

        // Alphanumeric Terminal Logging Area Workspace config
        logArea.setEditable(false);
        logArea.setStyle("-fx-control-inner-background: #151517; -fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-text-fill: #FBBC05; -fx-border-color: #3c4043; -fx-border-radius: 4;");
        logArea.setText("⚡ Segmentation Subsystem Initialized.\n[Model-Linked]: Successfully synchronized structural fields with Process.SegmentData context.");

        visualGrid.add(mapScroll, 0, 0);
        visualGrid.add(logArea, 1, 0);
        GridPane.setVgrow(mapScroll, Priority.ALWAYS);
        GridPane.setVgrow(logArea, Priority.ALWAYS);

        // Injection of safe mock data matrices on boot using your model constructors
        segmentList.add(new SegmentData("Main Code Text (Seg 0)", 2500, 400));
        segmentList.add(new SegmentData("System Stack Space (Seg 1)", 5000, 1200));
        updateMemoryVisualMap();

        // --- STEP 4: POPUP EVENTS TRAP LOGICS ---

        // A. Injection dialog launcher
        btnPopupAdd.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("MMU Desk: Register New Segmentation Boundaries");

            VBox pane = new VBox(12);
            pane.setPadding(new Insets(18));
            pane.setStyle("-fx-background-color: #202124; -fx-border-color: #34A853; -fx-border-width: 2;");

            Label title = new Label("Inject Descriptor Registry Fields:");
            title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

            TextField popName = new TextField(); popName.setPromptText("Scope Name (e.g., Code Segment)");
            TextField popBase = new TextField(); popBase.setPromptText("Base Hex Address (e.g., 2000)");
            TextField popLimit = new TextField(); popLimit.setPromptText("Limit Hex Size (e.g., 0500)");

            String popTfStyle = "-fx-background-color: #2d2e31; -fx-text-fill: white; -fx-border-color: #3c4043; -fx-border-radius: 3;";
            popName.setStyle(popTfStyle); popBase.setStyle(popTfStyle); popLimit.setStyle(popTfStyle);

            Button btnInject = new Button("Commit Descriptor to MMU");
            btnInject.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnInject.setMaxWidth(Double.MAX_VALUE);

            btnInject.setOnAction(evt -> {
                try {
                    if (!popName.getText().isEmpty() && !popBase.getText().isEmpty() && !popLimit.getText().isEmpty()) {
                        String name = popName.getText().trim();
                        String baseClean = popBase.getText().toLowerCase().replace("0x", "").trim();
                        String limitClean = popLimit.getText().toLowerCase().replace("0x", "").trim();

                        // Parse string text inputs into standard Base-16 integers cleanly
                        int baseInt = Integer.parseInt(baseClean, 16);
                        int limitInt = Integer.parseInt(limitClean, 16);

                        segmentList.add(new SegmentData(name, baseInt, limitInt));
                        logArea.appendText(String.format("\n[ALLOCATION]: Registered %s -> Base: 0x%X, Limit: 0x%X", 
                                name, baseInt, limitInt));
                        
                        updateMemoryVisualMap();
                        dialog.close();
                    }
                } catch (NumberFormatException ex) {
                    logArea.appendText("\n[REJECTED]: Hex payload configuration corrupted inside popup input context.");
                }
            });

            pane.getChildren().addAll(title, 
                new Label("Segment Identifier Title:") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, popName,
                new Label("Base Starting Frame Address (Hex String):") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, popBase,
                new Label("Limit Boundaries Capacity Range (Hex String):") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, popLimit, btnInject);

            dialog.setScene(new Scene(pane, 340, 330));
            dialog.showAndWait();
        });

        // B. Translation hardware computation modal
        btnPopupTranslate.setOnAction(e -> {
            if (segmentList.isEmpty()) {
                logArea.appendText("\n[ABORTED]: Relocation query halted. Descriptor table parameters are empty.");
                return;
            }

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Hardware Emulator: Address Relocation Core");

            VBox pane = new VBox(12);
            pane.setPadding(new Insets(18));
            pane.setStyle("-fx-background-color: #202124; -fx-border-color: #4285F4; -fx-border-width: 2;");

            Label title = new Label("Translate Address Pointer Mapping Core:");
            title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            ComboBox<String> selector = new ComboBox<>();
            for (SegmentData sd : segmentList) {
                selector.getItems().add(sd.getSegmentName());
            }
            selector.getSelectionModel().selectFirst();
            selector.setStyle("-fx-background-color: #2d2e31; -fx-text-fill: white;");
            selector.setMaxWidth(Double.MAX_VALUE);

            TextField popOffset = new TextField();
            popOffset.setPromptText("Offset Boundary Hex (e.g. 00A5)");
            popOffset.setStyle("-fx-background-color: #2d2e31; -fx-text-fill: white; -fx-border-color: #3c4043;");

            Button btnCompute = new Button("Execute Transformation Pipeline");
            btnCompute.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnCompute.setMaxWidth(Double.MAX_VALUE);

            btnCompute.setOnAction(evt -> {
                try {
                    String selectedName = selector.getValue();
                    String offsetStr = popOffset.getText().toLowerCase().replace("0x", "").trim();
                    int offsetInt = Integer.parseInt(offsetStr, 16);

                    SegmentData target = segmentList.stream()
                            .filter(s -> s.getSegmentName().equals(selectedName))
                            .findFirst().orElse(null);

                    if (target != null) {
                        int baseInt = target.getBaseAddress();
                        int limitInt = target.getLimitSize();

                        logArea.appendText("\n\n--- Bus Transaction Logic Cycle ---");
                        logArea.appendText(String.format("\n[CPU Request]: Target Component: %s, Logical Offset: 0x%X", selectedName, offsetInt));
                        logArea.appendText(String.format("\n[MMU Hardware Gate Check]: If (Offset: 0x%X < Limit size boundary: 0x%X)", offsetInt, limitInt));

                        // Mathematical Trap Protection constraint check loop execution
                        if (offsetInt < limitInt) {
                            int physicalAddr = baseInt + offsetInt;
                            logArea.appendText("\n[STATUS]: COMPLIANCE PASSED. Offset resides within safe allocation matrix block.");
                            logArea.appendText(String.format("\n[Formula]: 0x%X (Base) + 0x%X (Offset) = Physical Mapping Location -> 0x%X", baseInt, offsetInt, physicalAddr));
                            logArea.appendText(String.format("\n🎯 SUCCESS: Address mapped successfully to Physical Cell RAM address => 0x%s", Integer.toHexString(physicalAddr).toUpperCase()));
                        } else {
                            logArea.appendText(String.format("\n❌ [TRAP EXCEPTION]: INTERRUPT SIGNAL FIRE -> GENERAL PROTECTION FAULT (SIGSEGV)."));
                            logArea.appendText(String.format("\n[Reason Violation]: Memory segment bounds breached. Offset 0x%X out of bound limit (0x%X). Execution safe-stopped.", offsetInt, limitInt));
                        }
                    }
                    dialog.close();
                } catch (Exception ex) {
                    logArea.appendText("\n[MMU ERROR]: Relocation crashed due to improper verification string hex formatting inputs.");
                }
            });

            pane.getChildren().addAll(title, 
                new Label("Target Segment Descriptor Vector Component:") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, selector,
                new Label("Logical Vector Offset Address Pointer (Hex):") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, popOffset, btnCompute);

            dialog.setScene(new Scene(pane, 360, 260));
            dialog.showAndWait();
        });

        // C. Clear Registries control action
        btnClearCache.setOnAction(e -> {
            segmentList.clear();
            logArea.setText("🗑️ Kernel descriptor registries completely purged. MMU set back to factory initialization state.");
            updateMemoryVisualMap();
        });

        // --- STEP 5: CLEAN COHESIVE SYSTEM GRAPHICAL WHITE HEADERS ---
        Label lblControlTitle = new Label("MMU Segmentation Logic Controller Panel Desk:");
        lblControlTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblTableTitle = new Label("Active Memory Segment Table Matrix Descriptor Array Cache:");
        lblTableTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblLowerTitle = new Label("Physical RAM Hardware Memory Frame Map Layout vs. Real-Time Diagnostics:");
        lblLowerTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(
            lblControlTitle, controlRow,
            new Region() {{ setPrefHeight(2); }},
            lblTableTitle, table,
            new Region() {{ setPrefHeight(2); }},
            lblLowerTitle, visualGrid
        );
    }

    // --- RE-RENDER PROGRESSIVE GRAPHICAL COLOR-SEGMENTED BAR STACK MATRIX ---
    private void updateMemoryVisualMap() {
        memoryMapContainer.getChildren().clear();

        if (segmentList.isEmpty()) {
            VBox emptyContainer = new VBox();
            emptyContainer.setAlignment(Pos.CENTER);
            emptyContainer.setPadding(new Insets(40, 0, 40, 0));
            Label lblEmpty = new Label("🔒 NO SEGMENTS MAPPED\nPhysical Memory Space Cleared");
            lblEmpty.setStyle("-fx-text-fill: #5f6368; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-alignment: center;");
            emptyContainer.getChildren().add(lblEmpty);
            memoryMapContainer.getChildren().add(emptyContainer);
            return;
        }

        // Generate layered visual block nodes maps context
        int colorCounter = 0;
        for (SegmentData sd : segmentList) {
            try {
                int baseVal = sd.getBaseAddress();
                int limitVal = sd.getLimitSize();
                int endVal = baseVal + limitVal;

                VBox segmentBarCell = new VBox(3);
                segmentBarCell.setPadding(new Insets(10));
                
                // Toggle hex visual block colors based on counter tracking indexes
                String themeColor = (colorCounter % 2 == 0) ? "#1a73e8" : "#9b59b6";
                String borderCol = (colorCounter % 2 == 0) ? "#4285F4" : "#af7ac5";
                colorCounter++;
                
                segmentBarCell.setStyle(String.format(
                    "-fx-background-color: %s; -fx-background-radius: 5; -fx-border-color: %s; -fx-border-width: 1;", 
                    themeColor, borderCol
                ));

                Label lblHeaderName = new Label(String.format("🔹 Component Scope: %s", sd.getSegmentName()));
                lblHeaderName.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 12px;");

                Label lblRangeMatrix = new Label(String.format("Hex Bounds: 0x%X ─── [Limit Window: 0x%X] ───> End Target: 0x%X", 
                        baseVal, limitVal, endVal));
                lblRangeMatrix.setStyle("-fx-text-fill: #e8eaed; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");

                segmentBarCell.getChildren().addAll(lblHeaderName, lblRangeMatrix);
                memoryMapContainer.getChildren().add(segmentBarCell);

            } catch (Exception ex) {
                // Fail-safe protection boundary catch blocks skip
            }
        }
    }
}