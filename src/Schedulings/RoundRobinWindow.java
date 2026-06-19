package Schedulings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobinWindow extends BaseAlgorithmWindow {

    private final ObservableList<ProcessData> processList = FXCollections.observableArrayList();
    private final HBox ganttChart;
    private int currentTimeQuantum = 2;

    @SuppressWarnings("unchecked")
    public RoundRobinWindow(StackPane parentContainer) {
        super("Round Robin (RR) Scheduling", parentContainer);

        HBox configRow = new HBox(15);
        configRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label lblQuantum = new Label("Set Time Quantum (t):");
        lblQuantum.setStyle("-fx-text-fill: #FBBC05; -fx-font-weight: bold;");
        TextField txtQuantum = createInputField(configRow, "e.g. 2", 80);
        txtQuantum.setText(String.valueOf(currentTimeQuantum));
        
        Button btnUpdateQuantum = new Button("Apply");
        btnUpdateQuantum.setStyle("-fx-background-color: #FBBC05; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        configRow.getChildren().add(btnUpdateQuantum);

        HBox formRow = new HBox(10);
        formRow.setPadding(new Insets(5, 0, 5, 0));
        TextField txtId = createInputField(formRow, "PID", 100);
        TextField txtArrival = createInputField(formRow, "Arrival", 120);
        TextField txtBurst = createInputField(formRow, "Burst Time", 120);

        Button btnAdd = new Button("Add Process");
        btnAdd.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnAdd);

        Button btnRun = new Button("Run Simulation");
        btnRun.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
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
                txtId.clear(); txtArrival.clear(); txtBurst.clear();
            }
        });

        btnRun.setOnAction(e -> {
            if (processList.isEmpty()) return;
            ganttChart.getChildren().clear();

            List<ProcessData> source = new ArrayList<>(processList);
            source.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));

            int[] remBurst = new int[source.size()];
            for (int i = 0; i < source.size(); i++) remBurst[i] = source.get(i).getBurstTime();

            Queue<Integer> queue = new LinkedList<>();
            int currentTime = 0;
            int idx = 0;

            // Seed initial processes arriving at t=0 or closest start time boundary
            if (!source.isEmpty()) {
                if (source.get(0).getArrivalTime() > currentTime) {
                    addGanttBlock(ganttChart, "IDLE", "t: 0-" + source.get(0).getArrivalTime(), "#3c4043");
                    currentTime = source.get(0).getArrivalTime();
                }
                while (idx < source.size() && source.get(idx).getArrivalTime() <= currentTime) {
                    queue.add(idx);
                    idx++;
                }
            }

            while (!queue.isEmpty() || idx < source.size()) {
                if (queue.isEmpty()) {
                    if (source.get(idx).getArrivalTime() > currentTime) {
                        addGanttBlock(ganttChart, "IDLE", "t: " + currentTime + "-" + source.get(idx).getArrivalTime(), "#3c4043");
                        currentTime = source.get(idx).getArrivalTime();
                    }
                    while (idx < source.size() && source.get(idx).getArrivalTime() <= currentTime) {
                        queue.add(idx);
                        idx++;
                    }
                }

                int currentIdx = queue.poll();
                ProcessData p = source.get(currentIdx);
                int slice = Math.min(remBurst[currentIdx], currentTimeQuantum);

                int startTime = currentTime;
                currentTime += slice;
                remBurst[currentIdx] -= slice;

                addGanttBlock(ganttChart, p.getId(), "t: " + startTime + "-" + currentTime, "#9B59B6");

                // Check and push newly arrived elements while this process was executing
                while (idx < source.size() && source.get(idx).getArrivalTime() <= currentTime) {
                    queue.add(idx);
                    idx++;
                }

                if (remBurst[currentIdx] > 0) {
                    queue.add(currentIdx);
                } else {
                    int tat = currentTime - p.getArrivalTime();
                    int wt = tat - p.getBurstTime();
                    p.waitingTimeProperty().set(wt);
                    p.turnaroundTimeProperty().set(tat);
                }
            }
            table.refresh();
        });

        Label lblInputTitle = new Label("Add New Process Entry:");
        lblInputTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Cyclic Gantt View:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Pass the updated white labels into your workspace along with configRow, separator, and components
        workspace.getChildren().addAll(configRow, new Separator(), lblInputTitle, formRow, lblGanttTitle, ganttChart, table);
    }
}