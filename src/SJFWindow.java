import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SJFWindow extends BaseAlgorithmWindow {

    private final ObservableList<ProcessData> processList = FXCollections.observableArrayList();
    private final HBox ganttChart;

    @SuppressWarnings("unchecked")
    public SJFWindow(StackPane parentContainer) {
        super("Shortest Job First (SJF) - Non-Preemptive Simulation", parentContainer);

        HBox formRow = new HBox(10);
        formRow.setPadding(new Insets(5, 0, 5, 0));
        
        TextField txtId = createInputField(formRow, "PID", 100);
        TextField txtArrival = createInputField(formRow, "Arrival", 120);
        TextField txtBurst = createInputField(formRow, "Burst Time", 120);

        Button btnAdd = new Button("Add Process");
        btnAdd.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnAdd);

        Button btnRun = new Button("Run Simulation");
        btnRun.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnRun);

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
        
        TableColumn<ProcessData, Integer> colWait = new TableColumn<>("Waiting Time");
        colWait.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));
        TableColumn<ProcessData, Integer> colTurn = new TableColumn<>("Turnaround Time");
        colTurn.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));

        table.getColumns().addAll(colId, colArr, colBurst, colWait, colTurn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        btnAdd.setOnAction(e -> {
            if (!txtId.getText().isEmpty() && !txtArrival.getText().isEmpty() && !txtBurst.getText().isEmpty()) {
                String id = txtId.getText();
                int arr = Integer.parseInt(txtArrival.getText());
                int brst = Integer.parseInt(txtBurst.getText());
                
                processList.add(new ProcessData(id, arr, brst, 0, 0));
                txtId.clear(); txtArrival.clear(); txtBurst.clear();
            }
        });

        btnRun.setOnAction(e -> {
            if (processList.isEmpty()) return;
            ganttChart.getChildren().clear();

            List<ProcessData> remainingList = new ArrayList<>(processList);
            int currentTime = 0;

            while (!remainingList.isEmpty()) {
                int finalCurrentTime = currentTime;
                List<ProcessData> readyQueue = new ArrayList<>();
                for (ProcessData p : remainingList) {
                    if (p.getArrivalTime() <= finalCurrentTime) {
                        readyQueue.add(p);
                    }
                }

                if (readyQueue.isEmpty()) {
                    int nextArrival = remainingList.stream().mapToInt(ProcessData::getArrivalTime).min().orElse(currentTime);
                    addGanttBlock(ganttChart, "IDLE", "t: " + currentTime + "-" + nextArrival, "#3c4043");
                    currentTime = nextArrival;
                    continue;
                }

                // Non-preemptive shortest burst selector boundary
                ProcessData shortestJob = readyQueue.stream()
                        .min(Comparator.comparingInt(ProcessData::getBurstTime)
                                .thenComparingInt(ProcessData::getArrivalTime))
                        .orElse(readyQueue.get(0));

                int startTime = currentTime;
                currentTime += shortestJob.getBurstTime();

                int tat = currentTime - shortestJob.getArrivalTime();
                int wt = tat - shortestJob.getBurstTime();

                shortestJob.waitingTimeProperty().set(wt);
                shortestJob.turnaroundTimeProperty().set(tat);

                addGanttBlock(ganttChart, shortestJob.getId(), "t: " + startTime + "-" + currentTime, "#EA4335");
                remainingList.remove(shortestJob);
            }
            table.refresh();
        });

        Label lblInputTitle = new Label("Add New Process Entry:");
        lblInputTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Timeline Snapshot:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Pass the updated white label objects to your workspace container layout
        workspace.getChildren().addAll(lblInputTitle, formRow, lblGanttTitle, ganttChart, table);
    }
}