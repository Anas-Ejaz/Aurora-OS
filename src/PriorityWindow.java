import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class PriorityWindow extends BaseAlgorithmWindow {

    // Internal data schema adjustment subclassed directly for Priority tracking configurations
    public static class PriorityProcessRecord extends ProcessData {
        private final javafx.beans.property.SimpleIntegerProperty priorityNum;
        public PriorityProcessRecord(String id, int arrival, int burst, int priorityNum) {
            super(id, arrival, burst, 0, 0);
            this.priorityNum = new javafx.beans.property.SimpleIntegerProperty(priorityNum);
        }
        public int getPriorityNum() { return priorityNum.get(); }
        public javafx.beans.property.SimpleIntegerProperty priorityNumProperty() { return priorityNum; }
    }

    private final ObservableList<PriorityProcessRecord> processList = FXCollections.observableArrayList();
    private final HBox ganttChart;

    @SuppressWarnings("unchecked")
    public PriorityWindow(StackPane parentContainer) {
        super("Priority Scheduling Space", parentContainer);

        HBox formRow = new HBox(10);
        formRow.setPadding(new Insets(5, 0, 5, 0));
        
        TextField txtId = createInputField(formRow, "PID", 90);
        TextField txtArrival = createInputField(formRow, "Arrival", 100);
        TextField txtBurst = createInputField(formRow, "Burst Time", 100);
        TextField txtPriority = createInputField(formRow, "Priority Value", 110);

        Button btnAdd = new Button("Add Process");
        btnAdd.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnAdd);

        ganttChart = new HBox(4);
        ganttChart.setPrefHeight(60);
        ganttChart.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-padding: 10;");

        TableView<PriorityProcessRecord> table = new TableView<>(processList);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<PriorityProcessRecord, String> colId = new TableColumn<>("Process ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<PriorityProcessRecord, Integer> colPriority = new TableColumn<>("Priority");
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priorityNum"));
        TableColumn<PriorityProcessRecord, Integer> colArr = new TableColumn<>("Arrival Time");
        colArr.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        TableColumn<PriorityProcessRecord, Integer> colBurst = new TableColumn<>("Burst Time");
        colBurst.setCellValueFactory(new PropertyValueFactory<>("burstTime"));

        table.getColumns().addAll(colId, colPriority, colArr, colBurst);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        btnAdd.setOnAction(e -> {
            if (!txtId.getText().isEmpty() && !txtArrival.getText().isEmpty() && !txtBurst.getText().isEmpty() && !txtPriority.getText().isEmpty()) {
                String id = txtId.getText();
                int arr = Integer.parseInt(txtArrival.getText());
                int brst = Integer.parseInt(txtBurst.getText());
                int pri = Integer.parseInt(txtPriority.getText());
                
                processList.add(new PriorityProcessRecord(id, arr, brst, pri));
                addGanttBlock(ganttChart, id, "Pri: " + pri, "#1ABC9C");
                
                txtId.clear(); txtArrival.clear(); txtBurst.clear(); txtPriority.clear();
            }
        });

        workspace.getChildren().addAll(new Label("Add New Process Entry with Priority Level:"), formRow, new Label("Gantt Ordered Preview:"), ganttChart, table);
    }
}