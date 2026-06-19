package Schedulings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PriorityWindow extends BaseAlgorithmWindow {

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

        Button btnRun = new Button("Run Simulation");
        btnRun.setStyle("-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnRun);

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
        
        TableColumn<PriorityProcessRecord, Integer> colWait = new TableColumn<>("Waiting Time");
        colWait.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));
        TableColumn<PriorityProcessRecord, Integer> colTurn = new TableColumn<>("Turnaround Time");
        colTurn.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));

        table.getColumns().addAll(colId, colPriority, colArr, colBurst, colWait, colTurn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        btnAdd.setOnAction(e -> {
            if (!txtId.getText().isEmpty() && !txtArrival.getText().isEmpty() && !txtBurst.getText().isEmpty() && !txtPriority.getText().isEmpty()) {
                String id = txtId.getText();
                int arr = Integer.parseInt(txtArrival.getText());
                int brst = Integer.parseInt(txtBurst.getText());
                int pri = Integer.parseInt(txtPriority.getText());
                
                processList.add(new PriorityProcessRecord(id, arr, brst, pri));
                txtId.clear(); txtArrival.clear(); txtBurst.clear(); txtPriority.clear();
            }
        });

        btnRun.setOnAction(e -> {
            if (processList.isEmpty()) return;
            ganttChart.getChildren().clear();

            List<PriorityProcessRecord> remainingList = new ArrayList<>(processList);
            int currentTime = 0;

            while (!remainingList.isEmpty()) {
                // Find candidates that have arrived up to currentTime
                int finalCurrentTime = currentTime;
                List<PriorityProcessRecord> available = new ArrayList<>();
                for (PriorityProcessRecord p : remainingList) {
                    if (p.getArrivalTime() <= finalCurrentTime) {
                        available.add(p);
                    }
                }

                if (available.isEmpty()) {
                    // CPU Idle step logic handler
                    int nextArrival = remainingList.stream().mapToInt(ProcessData::getArrivalTime).min().orElse(currentTime);
                    addGanttBlock(ganttChart, "IDLE", "t: " + currentTime + "-" + nextArrival, "#3c4043");
                    currentTime = nextArrival;
                    continue;
                }

                // Choose process based on Lower Priority Number = Higher Priority Rule
                PriorityProcessRecord highestPriorityProcess = available.stream()
                        .min(Comparator.comparingInt(PriorityProcessRecord::getPriorityNum)
                                .thenComparingInt(ProcessData::getArrivalTime))
                        .orElse(available.get(0));

                int startTime = currentTime;
                currentTime += highestPriorityProcess.getBurstTime();

                int tat = currentTime - highestPriorityProcess.getArrivalTime();
                int wt = tat - highestPriorityProcess.getBurstTime();

                highestPriorityProcess.waitingTimeProperty().set(wt);
                highestPriorityProcess.turnaroundTimeProperty().set(tat);

                addGanttBlock(ganttChart, highestPriorityProcess.getId(), "t: " + startTime + "-" + currentTime, "#1ABC9C");
                remainingList.remove(highestPriorityProcess);
            }
            table.refresh();
        });

        Label lblInputTitle = new Label("Add New Process Entry with Priority Level:");
        lblInputTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Ordered Preview:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Pass the updated white label objects to the layout workspace
        workspace.getChildren().addAll(lblInputTitle, formRow, lblGanttTitle, ganttChart, table);
    }
}