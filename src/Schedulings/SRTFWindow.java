package Schedulings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;

public class SRTFWindow extends BaseAlgorithmWindow {

    private final ObservableList<ProcessData> processList = FXCollections.observableArrayList();
    private final HBox ganttChart;

    @SuppressWarnings("unchecked")
    public SRTFWindow(StackPane parentContainer) {
        super("Shortest Remaining Time First (SRTF) - Preemptive Space", parentContainer);

        HBox formRow = new HBox(10);
        formRow.setPadding(new Insets(5, 0, 5, 0));
        
        TextField txtId = createInputField(formRow, "PID", 100);
        TextField txtArrival = createInputField(formRow, "Arrival", 120);
        TextField txtBurst = createInputField(formRow, "Burst Time", 120);

        Button btnAdd = new Button("Add Process");
        btnAdd.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnAdd);

        Button btnRun = new Button("Run Simulation");
        btnRun.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
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

            List<ProcessData> list = new ArrayList<>(processList);
            int n = list.size();
            int[] remBurst = new int[n];
            for (int i = 0; i < n; i++) remBurst[i] = list.get(i).getBurstTime();

            int complete = 0, currentTime = 0, minm = Integer.MAX_VALUE;
            int shortest = 0, finishTime;
            boolean check = false;

            String lastRunningPID = "";
            int blockStartTime = 0;

            while (complete != n) {
                for (int j = 0; j < n; j++) {
                    if ((list.get(j).getArrivalTime() <= currentTime) && (remBurst[j] < minm) && remBurst[j] > 0) {
                        minm = remBurst[j];
                        shortest = j;
                        check = true;
                    }
                }

                if (!check) {
                    if (!lastRunningPID.equals("IDLE")) {
                        if (!lastRunningPID.isEmpty()) {
                            addGanttBlock(ganttChart, lastRunningPID, "t: " + blockStartTime + "-" + currentTime, lastRunningPID.equals("IDLE") ? "#3c4043" : "#E67E22");
                        }
                        lastRunningPID = "IDLE";
                        blockStartTime = currentTime;
                    }
                    currentTime++;
                    continue;
                }

                String currentPID = list.get(shortest).getId();
                if (!currentPID.equals(lastRunningPID)) {
                    if (!lastRunningPID.isEmpty()) {
                        addGanttBlock(ganttChart, lastRunningPID, "t: " + blockStartTime + "-" + currentTime, lastRunningPID.equals("IDLE") ? "#3c4043" : "#E67E22");
                    }
                    lastRunningPID = currentPID;
                    blockStartTime = currentTime;
                }

                remBurst[shortest]--;
                minm = remBurst[shortest];
                if (minm == 0) minm = Integer.MAX_VALUE;

                if (remBurst[shortest] == 0) {
                    complete++;
                    check = false;
                    finishTime = currentTime + 1;

                    int tat = finishTime - list.get(shortest).getArrivalTime();
                    int wt = tat - list.get(shortest).getBurstTime();

                    list.get(shortest).waitingTimeProperty().set(wt);
                    list.get(shortest).turnaroundTimeProperty().set(tat);
                }
                currentTime++;
            }
            if (!lastRunningPID.isEmpty()) {
                addGanttBlock(ganttChart, lastRunningPID, "t: " + blockStartTime + "-" + currentTime, "#E67E22");
            }
            table.refresh();
        });

        Label lblInputTitle = new Label("Add New Process Entry:");
        lblInputTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Progression:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Pass the updated white label objects to your workspace layout
        workspace.getChildren().addAll(lblInputTitle, formRow, lblGanttTitle, ganttChart, table);
    }
}