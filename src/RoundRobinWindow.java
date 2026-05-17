import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class RoundRobinWindow extends BaseAlgorithmWindow {

    private final ObservableList<ProcessData> processList = FXCollections.observableArrayList();
    private final HBox ganttChart;
    private int currentTimeQuantum = 2; // default fallback value

    @SuppressWarnings("unchecked")
    public RoundRobinWindow(StackPane parentContainer) {
        super("Round Robin (RR) Scheduling", parentContainer);

        // CONFIGURATION SECTION: Time Quantum Configuration Input
        HBox configRow = new HBox(15);
        configRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label lblQuantum = new Label("Set Time Quantum (t):");
        lblQuantum.setStyle("-fx-text-fill: #FBBC05; -fx-font-weight: bold;");
        TextField txtQuantum = createInputField(configRow, "e.g. 2", 80);
        txtQuantum.setText(String.valueOf(currentTimeQuantum));
        
        Button btnUpdateQuantum = new Button("Apply");
        btnUpdateQuantum.setStyle("-fx-background-color: #FBBC05; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        configRow.getChildren().add(btnUpdateQuantum);

        // PROCESS SUBMISSION FORM
        HBox formRow = new HBox(10);
        formRow.setPadding(new Insets(5, 0, 5, 0));
        TextField txtId = createInputField(formRow, "PID", 100);
        TextField txtArrival = createInputField(formRow, "Arrival", 120);
        TextField txtBurst = createInputField(formRow, "Burst Time", 120);

        Button btnAdd = new Button("Add Process");
        btnAdd.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnAdd);

        ganttChart = new HBox(4);
        ganttChart.setPrefHeight(60);
        ganttChart.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-padding: 10;");

        TableView<ProcessData> table = new TableView<>(processList);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<ProcessData, String> colId = new TableColumn<>("Process ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<ProcessData, Integer> colArr = new TableColumn<>("Arrival Time");
        colArr.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        TableColumn<ProcessData, Integer> colBurst = new TableColumn<>("Burst Time");
        colBurst.setCellValueFactory(new PropertyValueFactory<>("burstTime"));

        table.getColumns().addAll(colId, colArr, colBurst);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Action Listeners
        btnUpdateQuantum.setOnAction(e -> {
            if(!txtQuantum.getText().isEmpty()) {
                currentTimeQuantum = Integer.parseInt(txtQuantum.getText());
                System.out.println("Time Quantum updated to: " + currentTimeQuantum);
            }
        });

        btnAdd.setOnAction(e -> {
            if (!txtId.getText().isEmpty() && !txtArrival.getText().isEmpty() && !txtBurst.getText().isEmpty()) {
                String id = txtId.getText();
                int arr = Integer.parseInt(txtArrival.getText());
                int brst = Integer.parseInt(txtBurst.getText());
                
                processList.add(new ProcessData(id, arr, brst, 0, 0));
                // Shows visualization based on the configured time slice limit boundary
                addGanttBlock(ganttChart, id, "Slice (q=" + currentTimeQuantum + ")", "#9B59B6");
                
                txtId.clear(); txtArrival.clear(); txtBurst.clear();
            }
        });

        workspace.getChildren().addAll(configRow, new Separator(), new Label("Add New Process Entry:"), formRow, new Label("Cyclic Gantt View:"), ganttChart, table);
    }
}