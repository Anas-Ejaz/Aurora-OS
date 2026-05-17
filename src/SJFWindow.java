import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class SJFWindow extends BaseAlgorithmWindow {

    private final ObservableList<ProcessData> processList = FXCollections.observableArrayList();
    private final HBox ganttChart;

    @SuppressWarnings("unchecked")
    public SJFWindow(StackPane parentContainer) {
        super("Shortest Job First (SJF) - Preemptive", parentContainer);

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

        btnAdd.setOnAction(e -> {
            if (!txtId.getText().isEmpty() && !txtArrival.getText().isEmpty() && !txtBurst.getText().isEmpty()) {
                String id = txtId.getText();
                int arr = Integer.parseInt(txtArrival.getText());
                int brst = Integer.parseInt(txtBurst.getText());
                
                processList.add(new ProcessData(id, arr, brst, 0, 0));
                addGanttBlock(ganttChart, id, "Rem: " + brst, "#EA4335");
                
                txtId.clear(); txtArrival.clear(); txtBurst.clear();
            }
        });

        workspace.getChildren().addAll(new Label("Add New Process Entry:"), formRow, new Label("Gantt Timeline Snapshot:"), ganttChart, table);
    }
}