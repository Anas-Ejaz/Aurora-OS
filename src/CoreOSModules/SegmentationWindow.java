package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class SegmentationWindow extends BaseAlgorithmWindow {

    public static class SegmentRow {
        private final int segmentId;
        private final String segmentName, baseAddress, limitSize;

        public SegmentRow(int segmentId, String segmentName, String baseAddress, String limitSize) {
            this.segmentId = segmentId;
            this.segmentName = segmentName;
            this.baseAddress = baseAddress;
            this.limitSize = limitSize;
        }

        public int getSegmentId() { return segmentId; }
        public String getSegmentName() { return segmentName; }
        public String getBaseAddress() { return baseAddress; }
        public String getLimitSize() { return limitSize; }
    }

    private final ObservableList<SegmentRow> segmentList = FXCollections.observableArrayList();
    
    // UI elements for execution checking
    private final Label lblMmuStatus = new Label("Segmentation MMU Unit: IDLE (Waiting for CPU request)");

    public SegmentationWindow(StackPane parentContainer) {
        super("Core OS Module: Segment Base & Limit Address Mapping Engine", parentContainer);

        // --- 1. Top Input Panel (Adding Segment Table Metadata) ---
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtSegId = createInputField(inputRow, "Seg ID (e.g. 0)", 100);
        TextField txtName = createInputField(inputRow, "Scope Name (e.g. Code)", 140);
        TextField txtBase = createInputField(inputRow, "Base (Hex, e.g. 2000)", 130);
        TextField txtLimit = createInputField(inputRow, "Limit (Hex, e.g. 0500)", 130);

        Button btnAddSeg = new Button("Add Segment");
        btnAddSeg.setStyle("-fx-background-color: #FBBC05; -fx-text-fill: #202124; -fx-font-weight: bold; -fx-cursor: hand;");
        inputRow.getChildren().add(btnAddSeg);

        // --- 2. Live Hardware Address Translator (The Logic Simulation) ---
        VBox mmuPanel = new VBox(12);
        mmuPanel.setPadding(new Insets(15));
        mmuPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: #3c4043;");
        
        HBox translateInputs = new HBox(12);
        translateInputs.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtTargetSeg = createInputField(translateInputs, "Target Seg ID", 110);
        TextField txtOffset = createInputField(translateInputs, "Offset (Hex, e.g. 01A0)", 150);
        
        Button btnTranslate = new Button("Calculate Physical Addr");
        btnTranslate.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        translateInputs.getChildren().add(btnTranslate);

        lblMmuStatus.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Consolas'; -fx-font-size: 13px;");

        mmuPanel.getChildren().addAll(new Label("HARDWARE SEGMENTATION TRANSLATOR LOGIC:"), translateInputs, lblMmuStatus);

        // --- 3. UI Matrix Table View Layout ---
        TableView<SegmentRow> table = new TableView<>(segmentList);
        table.setStyle("-fx-background-color: #202124;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<SegmentRow, Integer> colId = new TableColumn<>("Segment ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("segmentId"));
        TableColumn<SegmentRow, String> colName = new TableColumn<>("Segment Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("segmentName"));
        TableColumn<SegmentRow, String> colBase = new TableColumn<>("Base Address");
        colBase.setCellValueFactory(new PropertyValueFactory<>("baseAddress"));
        TableColumn<SegmentRow, String> colLimit = new TableColumn<>("Limit Sizing Bounds");
        colLimit.setCellValueFactory(new PropertyValueFactory<>("limitSize"));

        table.getColumns().addAll(colId, colName, colBase, colLimit);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Standard Default Hardcoded Mockup Rows (Cleaned & standardized to pure hexadecimal strings)
        segmentList.add(new SegmentRow(0, "Main Code Text", "2500", "0400"));
        segmentList.add(new SegmentRow(1, "System Stack Space", "5000", "1200"));

        // --- BUTTON INTERACTIVE LOGICS ---
        btnAddSeg.setOnAction(e -> {
            try {
                int id = Integer.parseInt(txtSegId.getText());
                // Strip out "0x" prefix if user types it manually to avoid parsing crash
                String baseClean = txtBase.getText().toLowerCase().replace("0x", "");
                String limitClean = txtLimit.getText().toLowerCase().replace("0x", "");
                
                segmentList.add(new SegmentRow(id, txtName.getText(), baseClean, limitClean));
                txtSegId.clear(); txtName.clear(); txtBase.clear(); txtLimit.clear();
            } catch (NumberFormatException ex) {
                // Ignore parsing bounds errors silently
            }
        });

        btnTranslate.setOnAction(e -> {
            try {
                int targetId = Integer.parseInt(txtTargetSeg.getText());
                String offsetStr = txtOffset.getText().toLowerCase().replace("0x", "");
                
                int offsetInt = Integer.parseInt(offsetStr, 16); // Parsing hex string values

                // Find targeted segment
                SegmentRow selectedSeg = null;
                for (SegmentRow row : segmentList) {
                    if (row.getSegmentId() == targetId) {
                        selectedSeg = row;
                        break;
                    }
                }

                if (selectedSeg == null) {
                    lblMmuStatus.setText("CRITICAL: Segment ID " + targetId + " not mapped inside descriptor table.");
                    lblMmuStatus.setStyle("-fx-text-fill: #EA4335;");
                    return;
                }

                int baseInt = Integer.parseInt(selectedSeg.getBaseAddress(), 16);
                int limitInt = Integer.parseInt(selectedSeg.getLimitSize(), 16);

                // TRAP LOGIC CONSTRAINTS CHECK: (Offset must be less than Limit boundary size)
                if (offsetInt >= limitInt) {
                    lblMmuStatus.setText(String.format("CRITICAL ERROR: [SEGMENTATION FAULT] Offset 0x%X >= Limit 0x%X! Execution Aborted (SEGV).", 
                        offsetInt, limitInt));
                    lblMmuStatus.setStyle("-fx-text-fill: #EA4335; -fx-font-weight: bold;");
                } else {
                    // Valid Memory Space Mapping calculation execution
                    int physicalAddr = baseInt + offsetInt;
                    lblMmuStatus.setText(String.format("SUCCESS: Logical [Seg: %d, Offset: 0x%X] -> Valid Base Frame! Physical Target Location: 0x%X", 
                        targetId, offsetInt, physicalAddr));
                    lblMmuStatus.setStyle("-fx-text-fill: #34A853;");
                }

            } catch (NumberFormatException ex) {
                lblMmuStatus.setText("ERROR: Ensure numeric target ID and alphanumeric Hex values are set.");
                lblMmuStatus.setStyle("-fx-text-fill: #EA4335;");
            }
        });

        Label sectionLabel = new Label("Kernel Segment Descriptor Register Cache:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(mmuPanel, sectionLabel, inputRow, table);
    }
}