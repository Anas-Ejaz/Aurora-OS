package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class MemoryManagementWindow extends BaseAlgorithmWindow {

    public static class MemoryBlockRow {
        private final String blockName;
        private final int totalSize;
        private int remainingSize;
        private String allocationStatus;

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
        
        public void allocate(int size, String pid) {
            this.remainingSize = totalSize - size;
            this.allocationStatus = "ALLOCATED (" + pid + ") | Fragment: " + remainingSize + " KB";
        }
        
        public void setAllocationStatus(String status) {
            this.allocationStatus = status;
        }
    }

    private final ObservableList<MemoryBlockRow> memoryBlocks = FXCollections.observableArrayList();
    private final Label lblAllocStatus = new Label("Allocation Engine Status: IDLE (Awaiting Request)");

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

        // --- 2. Simulation Logic Panel (First-Fit & Best-Fit Engine) ---
        VBox algoPanel = new VBox(12);
        algoPanel.setPadding(new Insets(15));
        algoPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: #3c4043;");
        
        HBox requestRow = new HBox(12);
        requestRow.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtReqPid = createInputField(requestRow, "Request PID (e.g. P5)", 110);
        TextField txtReqSize = createInputField(requestRow, "Required Size (KB)", 130);
        
        ComboBox<String> cmbAlgo = new ComboBox<>();
        cmbAlgo.getItems().addAll("First-Fit Strategy", "Best-Fit Strategy");
        cmbAlgo.setValue("First-Fit Strategy");
        cmbAlgo.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white;");
        requestRow.getChildren().add(cmbAlgo);
        
        Button btnRequestMem = new Button("Request Allocation");
        btnRequestMem.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        requestRow.getChildren().add(btnRequestMem);

        lblAllocStatus.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        algoPanel.getChildren().addAll(new Label("OS MEMORY KERNEL ALLOCATION SIMULATOR:"), requestRow, lblAllocStatus);

        // --- 3. Main Data Matrix Table View ---
        TableView<MemoryBlockRow> table = new TableView<>(memoryBlocks);
        table.setStyle("-fx-background-color: #202124;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<MemoryBlockRow, String> colName = new TableColumn<>("Memory Slot Target");
        colName.setCellValueFactory(new PropertyValueFactory<>("blockName"));
        TableColumn<MemoryBlockRow, String> colSize = new TableColumn<>("Max Capacity Allocation");
        colSize.setCellValueFactory(new PropertyValueFactory<>("blockSize"));
        TableColumn<MemoryBlockRow, String> colStatus = new TableColumn<>("Current Allocation State");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("allocationStatus"));

        table.getColumns().addAll(colName, colSize, colStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Preloaded Initial Simulation Nodes
        memoryBlocks.add(new MemoryBlockRow("Kernel Block Block_A", 150, "FREE / UNALLOCATED"));
        memoryBlocks.add(new MemoryBlockRow("Kernel Block Block_B", 500, "FREE / UNALLOCATED"));
        memoryBlocks.add(new MemoryBlockRow("Kernel Block Block_C", 200, "FREE / UNALLOCATED"));

        // --- INTERACTIVE BUTTON LOGIC OPERATIONS ---
        btnInsertBlock.setOnAction(e -> {
            try {
                if (!txtBlockName.getText().isEmpty() && !txtBlockSize.getText().isEmpty()) {
                    int size = Integer.parseInt(txtBlockSize.getText());
                    memoryBlocks.add(new MemoryBlockRow(txtBlockName.getText(), size, "FREE / UNALLOCATED"));
                    txtBlockName.clear(); txtBlockSize.clear();
                }
            } catch (NumberFormatException ex) {
                // Ignore parsing boundary bugs safely
            }
        });

        btnRequestMem.setOnAction(e -> {
            try {
                String pid = txtReqPid.getText();
                int reqSize = Integer.parseInt(txtReqSize.getText());
                String selectedAlgo = cmbAlgo.getValue();

                if (pid.isEmpty()) return;

                MemoryBlockRow targetBlock = null;

                if (selectedAlgo.equals("First-Fit Strategy")) {
                    // FIRST-FIT LOGIC: Find the very first block that fits the process size
                    for (MemoryBlockRow block : memoryBlocks) {
                        if (block.getAllocationStatus().equals("FREE / UNALLOCATED") && block.getTotalSize() >= reqSize) {
                            targetBlock = block;
                            break;
                        }
                    }
                } else {
                    // BEST-FIT LOGIC: Find the block that leaves the smallest possible leftover space
                    int minLeftover = Integer.MAX_VALUE;
                    for (MemoryBlockRow block : memoryBlocks) {
                        if (block.getAllocationStatus().equals("FREE / UNALLOCATED") && block.getTotalSize() >= reqSize) {
                            int leftover = block.getTotalSize() - reqSize;
                            if (leftover < minLeftover) {
                                minLeftover = leftover;
                                targetBlock = block;
                            }
                        }
                    }
                }

                // Process Allocation Result Response
                if (targetBlock != null) {
                    targetBlock.allocate(reqSize, pid);
                    lblAllocStatus.setText(String.format("SUCCESS: Allocated %s (%d KB) inside %s via %s.", 
                        pid, reqSize, targetBlock.getBlockName(), selectedAlgo));
                    lblAllocStatus.setStyle("-fx-text-fill: #34A853;");
                    
                    // Trigger table layout refresh UI manually
                    int index = memoryBlocks.indexOf(targetBlock);
                    memoryBlocks.set(index, targetBlock);
                } else {
                    lblAllocStatus.setText("CRITICAL: [MEMORY OUT OF BOUNDS] Insufficient contiguous space for " + reqSize + " KB!");
                    lblAllocStatus.setStyle("-fx-text-fill: #EA4335; -fx-font-weight: bold;");
                }

                txtReqPid.clear(); txtReqSize.clear();

            } catch (NumberFormatException ex) {
                lblAllocStatus.setText("ERROR: Input components must contain valid numerical dimensions.");
                lblAllocStatus.setStyle("-fx-text-fill: #EA4335;");
            }
        });

        Label sectionLabel = new Label("Physical Hardware Fragmentation & Memory Block Partitions:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(algoPanel, sectionLabel, inputRow, table);
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