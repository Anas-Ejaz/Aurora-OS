package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
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
    private final int PAGE_SIZE_KB = 4; // Constant for simulation math

    // Live translation display elements
    private final Label lblCalcResult = new Label("Translation Engine Status: IDLE");
    private final ProgressIndicator progress = new ProgressIndicator();

    public PagingWindow(StackPane parentContainer) {
        super("Core OS Module: Paging Translation & Page Fault Monitor", parentContainer);

        // --- 1. Top Input Panel (Page Table Modifier) ---
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtPage = createInputField(inputRow, "Logical Page #", 120);
        TextField txtFrame = createInputField(inputRow, "Physical Frame #", 130);

        Button btnMap = new Button("Map Page to Frame");
        btnMap.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        inputRow.getChildren().add(btnMap);

        // --- 2. Live Translation Panel (The MMU Logic Display) ---
        VBox mmuPanel = new VBox(12);
        mmuPanel.setPadding(new Insets(15));
        mmuPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: #3c4043;");
        
        HBox translateInputs = new HBox(12);
        translateInputs.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtTargetPage = createInputField(translateInputs, "Target Page #", 120);
        TextField txtOffset = createInputField(translateInputs, "Offset (0-4095 Bytes)", 150);
        
        Button btnTranslate = new Button("MMU Translate");
        btnTranslate.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        translateInputs.getChildren().add(btnTranslate);

        progress.setVisible(false);
        progress.setPrefSize(20, 20);
        
        lblCalcResult.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        HBox statusLayout = new HBox(10, progress, lblCalcResult);
        statusLayout.setAlignment(Pos.CENTER_LEFT);

        mmuPanel.getChildren().addAll(new Label("CPU MEMORY MANAGEMENT UNIT (MMU) TRANSLATOR:"), translateInputs, statusLayout);

        // --- 3. Table View Layout ---
        TableView<PageMapping> table = new TableView<>(pageTableData);
        table.setStyle("-fx-background-color: #202124;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<PageMapping, Integer> colPage = new TableColumn<>("Logical Page Index");
        colPage.setCellValueFactory(new PropertyValueFactory<>("pageNum"));
        TableColumn<PageMapping, Integer> colFrame = new TableColumn<>("Physical Frame Index");
        colFrame.setCellValueFactory(new PropertyValueFactory<>("frameNum"));
        TableColumn<PageMapping, String> colStatus = new TableColumn<>("Validation Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colPage, colFrame, colStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Pre-populating setup data
        pageTableData.add(new PageMapping(0, 4, "1 - VALID (In RAM)"));
        pageTableData.add(new PageMapping(1, 9, "1 - VALID (In RAM)"));
        pageTableData.add(new PageMapping(2, -1, "0 - INVALID (Page Fault)"));

        // --- CONTROLS LOGIC ---
        btnMap.setOnAction(e -> {
            try {
                int p = Integer.parseInt(txtPage.getText());
                int f = Integer.parseInt(txtFrame.getText());
                pageTableData.add(new PageMapping(p, f, "1 - VALID (In RAM)"));
                txtPage.clear(); txtFrame.clear();
            } catch (NumberFormatException ex) {
                // validation fail silently
            }
        });

        btnTranslate.setOnAction(e -> {
            try {
                int targetP = Integer.parseInt(txtTargetPage.getText());
                int offset = Integer.parseInt(txtOffset.getText());
                
                if (offset < 0 || offset >= 4096) {
                    lblCalcResult.setText("ERROR: Offset must be between 0 and 4095 (4KB Page Boundary).");
                    lblCalcResult.setStyle("-fx-text-fill: #EA4335;");
                    return;
                }

                // Check page table records
                PageMapping match = null;
                for (PageMapping row : pageTableData) {
                    if (row.getPageNum() == targetP) {
                        match = row;
                        break;
                    }
                }

                if (match == null) {
                    lblCalcResult.setText("ERROR: Page " + targetP + " not defined inside OS Page Table.");
                    lblCalcResult.setStyle("-fx-text-fill: #EA4335;");
                } else if (match.getFrameNum() == -1) {
                    // Triggering Page Fault Routine Simulator
                    triggerPageFaultRoutine(match, offset);
                } else {
                    // Normal MMU Translation Math Calculation
                    int physicalAddress = (match.getFrameNum() * PAGE_SIZE_KB * 1024) + offset;
                    lblCalcResult.setText(String.format("SUCCESS: Logical Address [P: %d, O: %d] -> Maps to Physical RAM Address: %d bytes (Frame %d)", 
                        targetP, offset, physicalAddress, match.getFrameNum()));
                    lblCalcResult.setStyle("-fx-text-fill: #34A853;");
                }

            } catch (NumberFormatException ex) {
                lblCalcResult.setText("ERROR: Enter valid numerical Page and Offset components.");
                lblCalcResult.setStyle("-fx-text-fill: #EA4335;");
            }
        });

        Label sectionLabel = new Label("MMU (Memory Management Unit) Translation Table:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(mmuPanel, sectionLabel, inputRow, table);
    }

    private void triggerPageFaultRoutine(PageMapping missingPage, int offset) {
        progress.setVisible(true);
        lblCalcResult.setText("OS EXCEPTION: [PAGE FAULT TRAP] Page " + missingPage.getPageNum() + " not in RAM! Swapping from Disk...");
        lblCalcResult.setStyle("-fx-text-fill: #EA4335;");

        // Simulating the I/O cost of operating system secondary disk fetch
        PauseTransition diskIoSim = new PauseTransition(Duration.seconds(2.0));
        diskIoSim.setOnFinished(e -> {
            progress.setVisible(false);
            
            // Fix the page table assignment (OS updates frame pointer after loading page into empty frame)
            int simulatedFreeFrame = 15; 
            missingPage.setFrameNum(simulatedFreeFrame);
            missingPage.setStatus("1 - VALID (In RAM)");
            
            // Refresh table visually
            int index = pageTableData.indexOf(missingPage);
            pageTableData.set(index, missingPage);

            // Re-calculate the translation mapping address
            int physicalAddress = (simulatedFreeFrame * PAGE_SIZE_KB * 1024) + offset;
            lblCalcResult.setText(String.format("[RESOLVED] Disk IO complete. Loaded into Frame %d. Physical Address: %d", 
                simulatedFreeFrame, physicalAddress));
            lblCalcResult.setStyle("-fx-text-fill: #34A853;");
        });
        diskIoSim.play();
    }
}