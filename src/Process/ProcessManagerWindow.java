package Process;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class ProcessManagerWindow extends StackPane {

    private final VBox windowContent;
    private final TableView<ProcessData> table;
    
    // Inputs fields
    private final TextField txtId = new TextField();
    private final TextField txtArrival = new TextField();
    private final TextField txtBurst = new TextField();
    private final TextField txtPriority = new TextField();

    public ProcessManagerWindow(StackPane parentContainer) {
        this.setMaxSize(750, 500);

        windowContent = new VBox();
        windowContent.setStyle(
            "-fx-background-color: #202124;" + 
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-border-color: #3c4043;" +
            "-fx-border-width: 1.5;"
        );

        // Title Bar
        HBox titleBar = new HBox(8);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 10 10 0 0;");

        Label titleLabel = new Label("Central PCB Repository - Global Process Control");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 13;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        closeBtn.setPrefSize(13, 13);
        closeBtn.setStyle("-fx-background-color: #EA4335; -fx-background-radius: 50%; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> parentContainer.getChildren().remove(this));

        titleBar.getChildren().addAll(titleLabel, spacer, closeBtn);

        // Main Body Form and Layout
        VBox body = new VBox(15);
        body.setPadding(new Insets(15));

        // Form fields with WHITE Labels
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);

        Label lblId = new Label("Process ID:"); lblId.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label lblArrival = new Label("Arrival Time:"); lblArrival.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label lblBurst = new Label("Burst Time:"); lblBurst.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        Label lblPriority = new Label("Priority:"); lblPriority.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Styling textfields slightly gray to match design
        String fieldStyle = "-fx-background-color: #2d2e31; -fx-text-fill: white; -fx-border-color: #3c4043; -fx-border-radius: 3;";
        txtId.setStyle(fieldStyle); txtArrival.setStyle(fieldStyle); txtBurst.setStyle(fieldStyle); txtPriority.setStyle(fieldStyle);

        inputGrid.add(lblId, 0, 0); inputGrid.add(txtId, 1, 0);
        inputGrid.add(lblArrival, 2, 0); inputGrid.add(txtArrival, 3, 0);
        inputGrid.add(lblBurst, 0, 1); inputGrid.add(txtBurst, 1, 1);
        inputGrid.add(lblPriority, 2, 1); inputGrid.add(txtPriority, 3, 1);

        // Buttons for CRUD
        HBox actionRow = new HBox(12);
        Button btnAdd = new Button("Add Process"); btnAdd.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        Button btnUpdate = new Button("Update Selected"); btnUpdate.setStyle("-fx-background-color: #FBBC05; -fx-text-fill: black; -fx-cursor: hand; -fx-font-weight: bold;");
        Button btnDelete = new Button("Delete Selected"); btnDelete.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        actionRow.getChildren().addAll(btnAdd, btnUpdate, btnDelete);

        // Master Table Config
        table = new TableView<>(ProcessData.getMasterList());
        table.setStyle("-fx-background-color: #2d2e31;");
        
        TableColumn<ProcessData, String> colId = new TableColumn<>("Process ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<ProcessData, Integer> colArr = new TableColumn<>("Arrival Time");
        colArr.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        
        TableColumn<ProcessData, Integer> colBst = new TableColumn<>("Burst Time");
        colBst.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        
        TableColumn<ProcessData, Integer> colPri = new TableColumn<>("Priority");
        colPri.setCellValueFactory(new PropertyValueFactory<>("priority"));

        table.getColumns().addAll(colId, colArr, colBst, colPri);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // --- CRUD Event Logics ---
        btnAdd.setOnAction(e -> {
            if(!txtId.getText().isEmpty() && !txtArrival.getText().isEmpty() && !txtBurst.getText().isEmpty()) {
                int arr = Integer.parseInt(txtArrival.getText().trim());
                int brst = Integer.parseInt(txtBurst.getText().trim());
                int prio = txtPriority.getText().isEmpty() ? 0 : Integer.parseInt(txtPriority.getText().trim());
                
                ProcessData.getMasterList().add(new ProcessData(txtId.getText().trim(), arr, brst, prio, 0, 0));
                clearInputs();
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtId.setText(newSelection.getId());
                txtArrival.setText(String.valueOf(newSelection.getArrivalTime()));
                txtBurst.setText(String.valueOf(newSelection.getBurstTime()));
                txtPriority.setText(String.valueOf(newSelection.getPriority()));
            }
        });

        btnUpdate.setOnAction(e -> {
            ProcessData selected = table.getSelectionModel().getSelectedItem();
            if(selected != null) {
                selected.setId(txtId.getText().trim());
                selected.setArrivalTime(Integer.parseInt(txtArrival.getText().trim()));
                selected.setBurstTime(Integer.parseInt(txtBurst.getText().trim()));
                selected.setPriority(txtPriority.getText().isEmpty() ? 0 : Integer.parseInt(txtPriority.getText().trim()));
                table.refresh();
                clearInputs();
            }
        });

        btnDelete.setOnAction(e -> {
            ProcessData selected = table.getSelectionModel().getSelectedItem();
            if(selected != null) {
                ProcessData.getMasterList().remove(selected);
                clearInputs();
            }
        });

        body.getChildren().addAll(inputGrid, actionRow, table);
        windowContent.getChildren().addAll(titleBar, body);
        this.getChildren().add(windowContent);
    }

    private void clearInputs() {
        txtId.clear();
        txtArrival.clear();
        txtBurst.clear();
        txtPriority.clear();
    }
}