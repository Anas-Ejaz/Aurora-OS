package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MemoryManagementWindow extends BaseAlgorithmWindow {

    public static class MemoryBlockRow {
        private final String blockName;
        private final int totalSize;
        private int remainingSize;
        private String allocationStatus;
        private String assignedPid = ""; // Track PID for layout representation

        public MemoryBlockRow(String blockName, int totalSize, String allocationStatus) {
            this.blockName = blockName;
            this.totalSize = totalSize;
            this.remainingSize = totalSize;
            this.allocationStatus = allocationStatus;
        }

        public String getBlockName() { return blockName; }
        public String getBlockSize() { return totalSize + " KB"; }
        public String getAllocationStatus() { return allocationStatus; }
        
        public int getTotalSize() { return totalSize; }
        public int getRemainingSize() { return remainingSize; }
        public String getAssignedPid() { return assignedPid; }
        
        public void allocate(int size, String pid) {
            this.remainingSize = totalSize - size;
            this.assignedPid = pid;
            this.allocationStatus = "ALLOCATED (" + pid + ") | Fragment: " + remainingSize + " KB";
        }
        
        public void setAllocationStatus(String status) {
            this.allocationStatus = status;
            if(status.equals("FREE / UNALLOCATED")) {
                this.assignedPid = "";
                this.remainingSize = totalSize;
            }
        }
    }

    private final ObservableList<MemoryBlockRow> memoryBlocks = FXCollections.observableArrayList();
    private final Label lblAllocStatus = new Label("Allocation Engine Status: IDLE (Awaiting Request)");
    
    // UI Elements for replacement and logs
    private final FlowPane visualMemoryContainer = new FlowPane();
    private final TextArea txtSimLog = new TextArea();

    public MemoryManagementWindow(StackPane parentContainer) {
        super("Core OS Module: Dynamic Fixed Partition Memory Allocation Tracker", parentContainer);

        // --- 1. Top Panel (Carving New Memory Blocks) ---
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtBlockName = createInputField(inputRow, "Block ID (e.g. Block 1)", 140);
        TextField txtBlockSize = createInputField(inputRow, "Capacity Size (KB)", 140);

        Button btnInsertBlock = new Button("Carve Memory Block");
        btnInsertBlock.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        inputRow.getChildren().add(btnInsertBlock);

        // --- 2. Simulation Logic Panel (First, Best & Worst Fit Engine) ---
        VBox algoPanel = new VBox(12);
        algoPanel.setPadding(new Insets(15));
        algoPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: #3c4043;");
        
        HBox requestRow = new HBox(12);
        requestRow.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtReqPid = createInputField(requestRow, "Request PID (e.g. P5)", 110);
        TextField txtReqSize = createInputField(requestRow, "Required Size (KB)", 130);
        
        ComboBox<String> cmbAlgo = new ComboBox<>();
        // Badlao: ComboBox me Worst-Fit Strategy ko include kiya gya hai
        cmbAlgo.getItems().addAll("First-Fit Strategy", "Best-Fit Strategy", "Worst-Fit Strategy");
        cmbAlgo.setValue("First-Fit Strategy");
        cmbAlgo.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white;");
        requestRow.getChildren().add(cmbAlgo);
        
        Button btnRequestMem = new Button("Request Allocation");
        btnRequestMem.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        requestRow.getChildren().add(btnRequestMem);

        lblAllocStatus.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        algoPanel.getChildren().addAll(new Label("OS MEMORY KERNEL ALLOCATION SIMULATOR:"), requestRow, lblAllocStatus);

        // Tip Label for Right-Click Feature Discovery
        Label lblTip = new Label("* Tip: Right-click any block to Free or Delete it.");
        lblTip.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 11px; -fx-font-style: italic;");
        algoPanel.getChildren().add(lblTip);

        // --- 3. Split Screen: Left Side Layout Map (Visual Blocks) & Right Side (Log Console) ---
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(15);
        VBox.setVgrow(mainGrid, Priority.ALWAYS);

        ColumnConstraints colVisuals = new ColumnConstraints();
        colVisuals.setPercentWidth(60); // 60% Space for boxes
        ColumnConstraints colLogConsole = new ColumnConstraints();
        colLogConsole.setPercentWidth(40); // 40% Space for Kernel Terminal Logs
        mainGrid.getColumnConstraints().addAll(colVisuals, colLogConsole);

        // Visual FlowPane Setup Wrapper
        ScrollPane scrollPane = new ScrollPane(visualMemoryContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #202124; -fx-background-color: #202124; -fx-border-color: #3c4043; -fx-border-radius: 6;");
        visualMemoryContainer.setPadding(new Insets(15));
        visualMemoryContainer.setHgap(12);
        visualMemoryContainer.setVgap(12);
        visualMemoryContainer.setStyle("-fx-background-color: #202124;");
        GridPane.setVgrow(scrollPane, Priority.ALWAYS);
        mainGrid.add(scrollPane, 0, 0);

        // Simulation Terminal Setup
        VBox logBox = new VBox(5);
        Label lblLogTitle = new Label("Kernel MMU Terminal Output Logs:");
        lblLogTitle.setStyle("-fx-text-fill: #8ab4f8; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        txtSimLog.setEditable(false);
        txtSimLog.setWrapText(true);
        txtSimLog.setStyle("-fx-control-inner-background: #171717; -fx-text-fill: #34A853; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");
        VBox.setVgrow(txtSimLog, Priority.ALWAYS);
        logBox.getChildren().addAll(lblLogTitle, txtSimLog);
        mainGrid.add(logBox, 1, 0);

        // --- 4. ObservableList Listener for Automatic Visual UI Rendering ---
        memoryBlocks.addListener((ListChangeListener<MemoryBlockRow>) change -> renderVisualMemoryBlocks());

        // Preloaded Initial Simulation Nodes
        memoryBlocks.add(new MemoryBlockRow("Block_A", 150, "FREE / UNALLOCATED"));
        memoryBlocks.add(new MemoryBlockRow("Block_B", 500, "FREE / UNALLOCATED"));
        memoryBlocks.add(new MemoryBlockRow("Block_C", 200, "FREE / UNALLOCATED"));

        logWrite("System Boot: Memory Management Unit (MMU) initial registers ready.");

        // --- INTERACTIVE BUTTON LOGIC OPERATIONS ---
        btnInsertBlock.setOnAction(e -> {
            try {
                if (!txtBlockName.getText().isEmpty() && !txtBlockSize.getText().isEmpty()) {
                    int size = Integer.parseInt(txtBlockSize.getText());
                    String bName = txtBlockName.getText().trim();
                    memoryBlocks.add(new MemoryBlockRow(bName, size, "FREE / UNALLOCATED"));
                    logWrite("Kernel Config: Carved hardware sector segment [" + bName + "] mapped with base register size " + size + " KB.");
                    txtBlockName.clear(); txtBlockSize.clear();
                }
            } catch (NumberFormatException ex) {
                logWrite("Format Error: Segment partition width parameters must be numerical dimensions.");
            }
        });

        btnRequestMem.setOnAction(e -> {
            try {
                String pid = txtReqPid.getText().trim();
                String txtSizeRaw = txtReqSize.getText().trim();
                String selectedAlgo = cmbAlgo.getValue();

                if (pid.isEmpty() || txtSizeRaw.isEmpty()) return;
                int reqSize = Integer.parseInt(txtSizeRaw);

                MemoryBlockRow targetBlock = null;

                if (selectedAlgo.equals("First-Fit Strategy")) {
                    for (MemoryBlockRow block : memoryBlocks) {
                        if (block.getAllocationStatus().startsWith("FREE") && block.getTotalSize() >= reqSize) {
                            targetBlock = block;
                            break;
                        }
                    }
                } else if (selectedAlgo.equals("Best-Fit Strategy")) {
                    int minLeftover = Integer.MAX_VALUE;
                    for (MemoryBlockRow block : memoryBlocks) {
                        if (block.getAllocationStatus().startsWith("FREE") && block.getTotalSize() >= reqSize) {
                            int leftover = block.getTotalSize() - reqSize;
                            if (leftover < minLeftover) {
                                minLeftover = leftover;
                                targetBlock = block;
                            }
                        }
                    }
                } else if (selectedAlgo.equals("Worst-Fit Strategy")) {
                    // WORST-FIT LOGIC: Sab se bada available partition dhoondna jo size hold kar sake
                    int maxLeftover = -1;
                    for (MemoryBlockRow block : memoryBlocks) {
                        if (block.getAllocationStatus().startsWith("FREE") && block.getTotalSize() >= reqSize) {
                            int leftover = block.getTotalSize() - reqSize;
                            if (leftover > maxLeftover) {
                                maxLeftover = leftover;
                                targetBlock = block;
                            }
                        }
                    }
                }

                if (targetBlock != null) {
                    targetBlock.allocate(reqSize, pid);
                    lblAllocStatus.setText(String.format("SUCCESS: Allocated %s (%d KB) inside %s via %s.", 
                        pid, reqSize, targetBlock.getBlockName(), selectedAlgo));
                    lblAllocStatus.setStyle("-fx-text-fill: #34A853;");
                    
                    logWrite(String.format("MMU Dispatcher: Process [%s] requested %d KB -> Binding Context successfully targeted onto [%s] using %s. Remaining Segment Internal Fragmentation: %d KB.",
                            pid, reqSize, targetBlock.getBlockName(), selectedAlgo, targetBlock.getRemainingSize()));
                    
                    renderVisualMemoryBlocks();
                } else {
                    lblAllocStatus.setText("CRITICAL: [MEMORY OUT OF BOUNDS] Insufficient space for " + reqSize + " KB!");
                    lblAllocStatus.setStyle("-fx-text-fill: #EA4335; -fx-font-weight: bold;");
                    logWrite(String.format("Trap Interruption Vector: Core Execution Exception for Process [%s]. Allocation request for %d KB failed. Reason: Contiguous Sector Boundary Overflow via %s.", pid, reqSize, selectedAlgo));
                }

                txtReqPid.clear(); txtReqSize.clear();

            } catch (NumberFormatException ex) {
                lblAllocStatus.setText("ERROR: Input components must contain valid numerical dimensions.");
                lblAllocStatus.setStyle("-fx-text-fill: #EA4335;");
            }
        });

        Label sectionLabel = new Label("Physical Hardware Map Matrix & Shared Segment Vector Blocks View:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(algoPanel, sectionLabel, inputRow, mainGrid);
    }

    // --- Dynamic Diagram Block Renderer Implementation ---
    private void renderVisualMemoryBlocks() {
        visualMemoryContainer.getChildren().clear();

        for (MemoryBlockRow block : memoryBlocks) {
            VBox blockBox = new VBox(6);
            blockBox.setAlignment(Pos.CENTER);
            blockBox.setPadding(new Insets(12));
            blockBox.setPrefSize(165, 120);
            
            Label nameLbl = new Label(block.getBlockName().toUpperCase());
            nameLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px;");
            
            Label capLbl = new Label("Max Cap: " + block.getBlockSize());
            capLbl.setStyle("-fx-text-fill: #e8eaed; -fx-font-size: 11px;");

            Label statusLbl = new Label();
            statusLbl.setWrapText(true);
            statusLbl.setAlignment(Pos.CENTER);
            
            if (!block.getAssignedPid().isEmpty()) {
                blockBox.setStyle("-fx-background-color: #1a73e8; -fx-background-radius: 6; -fx-border-color: #8ab4f8; -fx-border-width: 2; -fx-cursor: hand;");
                statusLbl.setText("Bound: " + block.getAssignedPid() + "\nFrag: " + block.getRemainingSize() + " KB");
                statusLbl.setStyle("-fx-text-fill: #ccffcc; -fx-font-family: 'Consolas'; -fx-font-size: 11px; -fx-text-alignment: center;");
            } else {
                blockBox.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-border-color: #34A853; -fx-border-width: 2; -fx-cursor: hand;");
                statusLbl.setText("FREE / READY");
                statusLbl.setStyle("-fx-text-fill: #34A853; -fx-font-weight: bold; -fx-font-size: 11px;");
            }

            // --- CONTEXT MENU (RIGHT-CLICK ACTIONS) FOR FREE & DELETE ---
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deallocateItem = new MenuItem("Free / Deallocate Process Context");
            MenuItem deleteItem = new MenuItem("Delete Memory Block Entirely");

            deallocateItem.setDisable(block.getAssignedPid().isEmpty());

            deallocateItem.setOnAction(event -> {
                String oldPid = block.getAssignedPid();
                block.setAllocationStatus("FREE / UNALLOCATED");
                lblAllocStatus.setText("Deallocated: " + block.getBlockName() + " is now clean.");
                lblAllocStatus.setStyle("-fx-text-fill: #FBBC05;");
                logWrite("MMU Interrupt: Process [" + oldPid + "] manually flushed from segment [" + block.getBlockName() + "]. Space reclaimed.");
                renderVisualMemoryBlocks();
            });

            deleteItem.setOnAction(event -> {
                memoryBlocks.remove(block);
                lblAllocStatus.setText("Deleted: " + block.getBlockName() + " removed from registry.");
                lblAllocStatus.setStyle("-fx-text-fill: #EA4335;");
                logWrite("Kernel Config: Hardware segment partition [" + block.getBlockName() + "] wiped out from MMU lookup registry.");
            });

            contextMenu.getItems().addAll(deallocateItem, deleteItem);
            blockBox.setOnContextMenuRequested(e -> contextMenu.show(blockBox, e.getScreenX(), e.getScreenY()));

            blockBox.getChildren().addAll(nameLbl, capLbl, statusLbl);
            visualMemoryContainer.getChildren().add(blockBox);
        }
    }

    // --- Helper Simulation Logger Engine Logic ---
    private void logWrite(String logMessage) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        txtSimLog.appendText("[" + timestamp + "] " + logMessage + "\n");
    }

    // --- Helper UI Generator Layout Method ---
    protected TextField createInputField(HBox targetRow, String promptText, double explicitWidth) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setPrefWidth(explicitWidth);
        field.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white; -fx-prompt-text-fill: #9aa0a6; -fx-background-radius: 4;");
        targetRow.getChildren().add(field);
        return field;
    }
}