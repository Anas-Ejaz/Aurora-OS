package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

public class PagingWindow extends BaseAlgorithmWindow {

    public static class PageMapping {
        private final int pageNum;
        private int frameNum;
        private String status;

        public PageMapping(int pageNum, int frameNum, String status) {
            this.pageNum = pageNum; 
            this.frameNum = frameNum; 
            this.status = status;
        }
        public int getPageNum() { return pageNum; }
        public int getFrameNum() { return frameNum; }
        public String getStatus() { return status; }
        
        public void setFrameNum(int frameNum) { this.frameNum = frameNum; }
        public void setStatus(String status) { this.status = status; }
    }

    private final ObservableList<PageMapping> pageTableData = FXCollections.observableArrayList();
    private final TableView<PageMapping> table = new TableView<>(pageTableData);
    
    // RAM Matrix Grid Layout Container (Holds the 16 fixed memory blocks)
    private final VBox ramGridContainer = new VBox(6);
    private final TextArea logArea = new TextArea();
    private final ProgressIndicator progress = new ProgressIndicator();
    
    private final int TOTAL_FRAMES = 16;
    private final int PAGE_SIZE_KB = 4; // 4KB Page Size Boundary Standard

    public PagingWindow(StackPane parentContainer) {
        super("Core OS Module: Fixed-Frame Paging Core & Page Fault Exception Monitor", parentContainer);

        // --- STEP 1: TOP CONTROL ACTIONS BAR ---
        HBox controlRow = new HBox(12);
        controlRow.setAlignment(Pos.CENTER_LEFT);
        controlRow.setPadding(new Insets(5, 0, 5, 0));

        Button btnPopupMap = new Button("➕ Map Page to RAM");
        btnPopupMap.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16 8 16;");

        Button btnPopupTranslate = new Button("🔍 MMU Address Translate");
        btnPopupTranslate.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16 8 16;");

        Button btnWipeCache = new Button("🗑️ Clear Caches");
        btnWipeCache.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 16 8 16;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        progress.setVisible(false);
        progress.setPrefSize(18, 18);
        progress.setStyle("-fx-progress-color: #EA4335;");

        Label lblMmuUnit = new Label("MMU STATUS: ONLINE");
        lblMmuUnit.setStyle("-fx-text-fill: #4285F4; -fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-background-color: rgba(66,133,244,0.1); -fx-padding: 6 12 6 12; -fx-background-radius: 4;");

        HBox systemStatusBlock = new HBox(10, progress, lblMmuUnit);
        systemStatusBlock.setAlignment(Pos.CENTER_LEFT);

        controlRow.getChildren().addAll(btnPopupMap, btnPopupTranslate, btnWipeCache, spacer, systemStatusBlock);

        // --- STEP 2: CONFIGURE LEFT ZONE (OS PAGE TABLE MATRIX) ---
        table.setStyle("-fx-background-color: #202124; -fx-border-color: #3c4043; -fx-border-radius: 4;");
        TableColumn<PageMapping, Integer> colPage = new TableColumn<>("Logical Page Index (p)");
        colPage.setCellValueFactory(new PropertyValueFactory<>("pageNum"));
        TableColumn<PageMapping, Integer> colFrame = new TableColumn<>("Physical Frame Target (f)");
        colFrame.setCellValueFactory(new PropertyValueFactory<>("frameNum"));
        TableColumn<PageMapping, String> colStatus = new TableColumn<>("Validation Bit Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colPage, colFrame, colStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox leftColumnLayout = new VBox(8);
        VBox.setVgrow(table, Priority.ALWAYS);
        Label lblTableText = new Label("Kernel Master Page Translation Registry Table:");
        lblTableText.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold;");
        leftColumnLayout.getChildren().addAll(lblTableText, table);

        // --- STEP 3: CONFIGURE RIGHT ZONE (STATIC RAM HARDWARE FRAMES BLOCK MAP) ---
        ScrollPane ramScroll = new ScrollPane(ramGridContainer);
        ramScroll.setFitToWidth(true);
        ramScroll.setStyle("-fx-background: #1e1f22; -fx-background-color: #1e1f22; -fx-border-color: #3c4043; -fx-border-radius: 4;");
        ramGridContainer.setPadding(new Insets(10));
        ramGridContainer.setStyle("-fx-background-color: #1e1f22;");
        
        VBox rightColumnLayout = new VBox(8);
        VBox.setVgrow(ramScroll, Priority.ALWAYS);
        Label lblLowerText = new Label("Physical RAM Hardware Frame Mapping Blocks:");
        lblLowerText.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold;");
        rightColumnLayout.getChildren().addAll(lblLowerText, ramScroll);

        // --- STEP 4: MAIN DASHBOARD GRID ARBITRATION (SIDE-BY-SIDE + BOTTOM SPLIT) ---
        GridPane dashboardGrid = new GridPane();
        dashboardGrid.setHgap(15);
        dashboardGrid.setVgap(15);
        VBox.setVgrow(dashboardGrid, Priority.ALWAYS);

        // Width allocations: Left Column (50%) and Right Column (50%)
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        dashboardGrid.getColumnConstraints().addAll(col1, col2);

        // Row allocations: Upper Simulation Zone (65%) and Lower Logs Zone (35%)
        RowConstraints rowUpper = new RowConstraints(); 
        rowUpper.setPercentHeight(65); // Fixed: Changed from setPercentWidth to setPercentHeight

        RowConstraints rowLower = new RowConstraints(); 
        rowLower.setPercentHeight(35); // Fixed: Changed from setPercentWidth to setPercentHeight

        dashboardGrid.getRowConstraints().addAll(rowUpper, rowLower);

        // Injecting the Side-by-Side elements into Row 0
        dashboardGrid.add(leftColumnLayout, 0, 0);
        dashboardGrid.add(rightColumnLayout, 1, 0);

        // --- STEP 5: BOTTOM FULL-WIDTH CONSOLE DIAGNOSTICS TERMINAL ---
        logArea.setEditable(false);
        logArea.setStyle("-fx-control-inner-background: #151517; -fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-text-fill: #34A853; -fx-border-color: #3c4043; -fx-border-radius: 4;");
        logArea.setText("⚡ Paging Subsystem Initialized.\n[Hardware Matrix]: 16 Static RAM Frames mapped [Frame 0 -> Frame 15] x 4KB boundaries side-by-side table matrix layout.");

        VBox bottomConsoleLayout = new VBox(6);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        Label lblLogTitle = new Label("Real-Time Operating System Hardware Diagnostics Stream Logs:");
        lblLogTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold;");
        bottomConsoleLayout.getChildren().addAll(lblLogTitle, logArea);

        // Spanning across both column vectors in Row 1
        dashboardGrid.add(bottomConsoleLayout, 0, 1, 2, 1);

        // Bootstrapping initial mock entries
        pageTableData.add(new PageMapping(0, 2, "1 - VALID (In RAM)"));
        pageTableData.add(new PageMapping(1, 5, "1 - VALID (In RAM)"));
        pageTableData.add(new PageMapping(2, -1, "0 - INVALID (Page Fault)"));
        
        // Render block spaces immediately
        updatePhysicalRamDisplay();

        // --- STEP 6: CONTROLLER POPUPS ACTIONS HANDLERS ---

        // A. Dynamic Registration Core
        btnPopupMap.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Configuration Desk: Map Logical Page Entry");

            VBox pane = new VBox(12);
            pane.setPadding(new Insets(18));
            pane.setStyle("-fx-background-color: #202124; -fx-border-color: #34A853; -fx-border-width: 2;");

            Label title = new Label("Inject Page Table Matrix Fields:");
            title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

            TextField popPage = new TextField(); popPage.setPromptText("Logical Page Index (e.g. 3)");
            TextField popFrame = new TextField(); popFrame.setPromptText("Target RAM Frame Index [0-15] (-1 for Swap)");

            String popTfStyle = "-fx-background-color: #2d2e31; -fx-text-fill: white; -fx-border-color: #3c4043; -fx-border-radius: 3;";
            popPage.setStyle(popTfStyle); popFrame.setStyle(popTfStyle);

            Button btnCommit = new Button("Commit Page Descriptor to MMU");
            btnCommit.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnCommit.setMaxWidth(Double.MAX_VALUE);

            btnCommit.setOnAction(evt -> {
                try {
                    if (!popPage.getText().isEmpty() && !popFrame.getText().isEmpty()) {
                        int p = Integer.parseInt(popPage.getText().trim());
                        int f = Integer.parseInt(popFrame.getText().trim());

                        if (f < -1 || f >= TOTAL_FRAMES) {
                            logArea.appendText(String.format("\n❌ [BUS ERROR]: Target Frame Slot %d out of Physical RAM boundary constraints [0-15].", f));
                            dialog.close();
                            return;
                        }

                        String status = (f == -1) ? "0 - INVALID (Page Fault)" : "1 - VALID (In RAM)";
                        pageTableData.add(new PageMapping(p, f, status));
                        logArea.appendText(String.format("\n[PAGE REGISTER]: Mapped Logical Page %d onto Physical Frame %d (%s)", p, f, status));
                        
                        updatePhysicalRamDisplay();
                        dialog.close();
                    }
                } catch (NumberFormatException ex) {
                    logArea.appendText("\n[REJECTED]: Numeric conversion exceptions raised during page layout configurations.");
                }
            });

            pane.getChildren().addAll(title, 
                new Label("Logical Page Index Target:") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, popPage,
                new Label("Physical RAM Frame Target Mapping Row:") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, popFrame, btnCommit);

            dialog.setScene(new Scene(pane, 360, 260));
            dialog.showAndWait();
        });

        // B. Address Resolution Processing Unit Sandbox
        btnPopupTranslate.setOnAction(e -> {
            if (pageTableData.isEmpty()) {
                logArea.appendText("\n[ABORTED]: Address transformation halted. Page descriptor table properties are blank.");
                return;
            }

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("MMU Core Sandbox: Execution Pipeline Translators");

            VBox pane = new VBox(12);
            pane.setPadding(new Insets(18));
            pane.setStyle("-fx-background-color: #202124; -fx-border-color: #4285F4; -fx-border-width: 2;");

            Label title = new Label("Translate Address Matrices Calculations Core:");
            title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            ComboBox<Integer> pageSelector = new ComboBox<>();
            for (PageMapping pm : pageTableData) {
                pageSelector.getItems().add(pm.getPageNum());
            }
            pageSelector.getSelectionModel().selectFirst();
            pageSelector.setStyle("-fx-background-color: #2d2e31; -fx-text-fill: white;");
            pageSelector.setMaxWidth(Double.MAX_VALUE);

            TextField popOffset = new TextField();
            popOffset.setPromptText("Displacement Offset Target (0 - 4095 Bytes)");
            popOffset.setStyle("-fx-background-color: #2d2e31; -fx-text-fill: white; -fx-border-color: #3c4043;");

            Button btnTranslateRun = new Button("Execute MMU Translation Phase");
            btnTranslateRun.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnTranslateRun.setMaxWidth(Double.MAX_VALUE);

            btnTranslateRun.setOnAction(evt -> {
                try {
                    int targetP = pageSelector.getValue();
                    int offset = Integer.parseInt(popOffset.getText().trim());

                    if (offset < 0 || offset >= 4096) {
                        logArea.appendText(String.format("\n❌ [OFFSET PROTECTION FAULT]: Displaced pointer %d breaches page limits boundary [0-4095].", offset));
                        dialog.close();
                        return;
                    }

                    PageMapping match = pageTableData.stream()
                            .filter(pm -> pm.getPageNum() == targetP)
                            .findFirst().orElse(null);

                    if (match != null) {
                        if (match.getFrameNum() == -1) {
                            dialog.close();
                            triggerPageFaultRoutine(match, offset);
                        } else {
                            int physicalAddress = (match.getFrameNum() * PAGE_SIZE_KB * 1024) + offset;
                            logArea.appendText("\n\n--- Bus Transaction Logic Cycle ---");
                            logArea.appendText(String.format("\n[CPU Request]: Query Page: %d, Offset Displacement: %d Bytes", targetP, offset));
                            logArea.appendText(String.format("\n[MMU Hardware Verification]: Logic Check Passed -> Page present in RAM Frame Slot %d", match.getFrameNum()));
                            logArea.appendText(String.format("\n[Mathematical Formula]: (Frame index %d * 4096) + Offset %d Bytes", match.getFrameNum(), offset));
                            logArea.appendText(String.format("\n🎯 SUCCESS: Dynamic Memory target located at cell offset location -> %d Bytes", physicalAddress));
                            dialog.close();
                        }
                    }
                } catch (Exception ex) {
                    logArea.appendText("\n[MMU CRASH EXCEPTION]: Target displacement string contains corrupt entities payload format.");
                }
            });

            pane.getChildren().addAll(title, 
                new Label("Target Logical Page Element Index Vector:") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, pageSelector,
                new Label("Logical Index Offset Boundary displacement:") {{ setStyle("-fx-text-fill: #9aa0a6;"); }}, popOffset, btnTranslateRun);

            dialog.setScene(new Scene(pane, 370, 260));
            dialog.showAndWait();
        });

        // C. Clean System
        btnWipeCache.setOnAction(e -> {
            pageTableData.clear();
            logArea.setText("🗑️ Hardware descriptor registries flushed cleanly. All 16 RAM frames reset back to unallocated state.");
            updatePhysicalRamDisplay();
        });

        // --- STEP 7: INJECT WORKSPACE COMPONENTS ROW ---
        Label lblControlText = new Label("MMU Hardware Paging Logic Console Action Desk:");
        lblControlText.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(
            lblControlText, controlRow,
            new Region() {{ setPrefHeight(2); }},
            dashboardGrid
        );
    }

    // --- INTERRUPT INTERACTION ROUTINE: DISK PAGE SWAP TRAP REFETCH LOOP ---
    private void triggerPageFaultRoutine(PageMapping missingPage, int offset) {
        progress.setVisible(true);
        logArea.appendText("\n\n⚠️ ─── KERNEL OS TRAP INTERRUPT FIRE ───");
        logArea.appendText(String.format("\n[PAGE FAULT]: Requested Logical Page %d lacks valid hardware frame cache reference (Valid bit = 0).", missingPage.getPageNum()));
        logArea.appendText("\n[Action Routine]: Suspending execution paths... Activating Kernel page-swapper scheduler.");
        logArea.appendText("\n✈️ [Disk IO Pipeline]: Scanning swap space sector indices for source data payload tracks...");

        PauseTransition diskIoSim = new PauseTransition(Duration.seconds(2.0));
        diskIoSim.setOnFinished(e -> {
            progress.setVisible(false);
            
            // Scan for an open unallocated hardware frame slot dynamically
            int allocatedFrame = -1;
            for (int i = 0; i < TOTAL_FRAMES; i++) {
                final int checkedFrame = i;
                boolean isOccupied = pageTableData.stream().anyMatch(p -> p.getFrameNum() == checkedFrame);
                if (!isOccupied) {
                    allocatedFrame = i;
                    break;
                }
            }

            if (allocatedFrame == -1) allocatedFrame = 7; // Fallback rule block index safely

            missingPage.setFrameNum(allocatedFrame);
            missingPage.setStatus("1 - VALID (In RAM)");
            
            int index = pageTableData.indexOf(missingPage);
            pageTableData.set(index, missingPage);

            int physicalAddress = (allocatedFrame * PAGE_SIZE_KB * 1024) + offset;
            
            logArea.appendText("\n[RESOLVED]: Secondary disk sector read sequence terminated. Context blocks extracted.");
            logArea.appendText(String.format("\n[Action]: Injected data block into empty Physical RAM Frame Slot: %d. Switched Validation Bit -> 1.", allocatedFrame));
            logArea.appendText(String.format("\n🎯 SUCCESS: Resuming MMU execution path calculation -> Resolved RAM Cell Location: %d Bytes", physicalAddress));
            
            updatePhysicalRamDisplay();
        });
        diskIoSim.play();
    }

    // --- REAL-TIME RE-RENDER ENGINE FOR THE 16 FIXED HARDWARE RAM FRAMES ---
    private void updatePhysicalRamDisplay() {
        ramGridContainer.getChildren().clear();

        for (int frameIdx = 0; frameIdx < TOTAL_FRAMES; frameIdx++) {
            final int currentFrame = frameIdx;
            
            PageMapping assignedPage = pageTableData.stream()
                    .filter(p -> p.getFrameNum() == currentFrame)
                    .findFirst().orElse(null);

            VBox frameBox = new VBox(2);
            frameBox.setPadding(new Insets(8, 12, 8, 12));
            
            int baseByteAddress = currentFrame * 4096;
            int endByteAddress = baseByteAddress + 4095;

            if (assignedPage != null) {
                // ALLOCATED STATE (Highlight active frame blocks)
                String bgThemeColor = (assignedPage.getPageNum() % 2 == 0) ? "#0f52ba" : "#2e7d32"; 
                String borderThemeColor = (assignedPage.getPageNum() % 2 == 0) ? "#4285F4" : "#34A853";
                
                frameBox.setStyle(String.format(
                    "-fx-background-color: %s; -fx-background-radius: 4; -fx-border-color: %s; -fx-border-width: 1.5;", 
                    bgThemeColor, borderThemeColor
                ));

                Label lblFrameTitle = new Label(String.format("🖥️ PHYSICAL FRAME %02d ─── [ ALLOCATED ]", currentFrame));
                lblFrameTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 11px;");

                Label lblAllocationDetails = new Label(String.format("📄 Contains: Logical Page %d  |  Memory Map Space: %d ──> %d Bytes", 
                        assignedPage.getPageNum(), baseByteAddress, endByteAddress));
                lblAllocationDetails.setStyle("-fx-text-fill: #e8eaed; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");

                frameBox.getChildren().addAll(lblFrameTitle, lblAllocationDetails);
            } else {
                // HOLLOW UNALLOCATED FACTORY STATE (Frame empty block)
                frameBox.setStyle("-fx-background-color: #252629; -fx-background-radius: 4; -fx-border-color: #3c4043; -fx-border-width: 1;");

                Label lblFrameTitle = new Label(String.format("🖥️ PHYSICAL FRAME %02d ─── [ FREE FRAME ]", currentFrame));
                lblFrameTitle.setStyle("-fx-text-fill: #5f6368; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 11px;");

                Label lblAllocationDetails = new Label(String.format("System Space Range Blocks: %d ──> %d Bytes", baseByteAddress, endByteAddress));
                lblAllocationDetails.setStyle("-fx-text-fill: #5f6368; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");

                frameBox.getChildren().addAll(lblFrameTitle, lblAllocationDetails);
            }

            ramGridContainer.getChildren().add(frameBox);
        }
    }
}