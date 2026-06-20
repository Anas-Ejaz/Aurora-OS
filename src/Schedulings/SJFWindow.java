package Schedulings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Process.ProcessData;

public class SJFWindow extends BaseAlgorithmWindow {

    // Central repository map karne ke liye local simulation list
    private final ObservableList<ProcessData> localProcessList = FXCollections.observableArrayList();
    private final HBox ganttChart;
    private final TableView<ProcessData> table;

    @SuppressWarnings("unchecked")
    public SJFWindow(StackPane parentContainer) {
        super("Shortest Job First (SJF) - Non-Preemptive Simulation", parentContainer);

        // --- STEP 1: CENTRAL SYNC CONTROL ROW ---
        HBox controlRow = new HBox(15);
        controlRow.setAlignment(Pos.CENTER_LEFT);
        controlRow.setPadding(new Insets(5, 0, 5, 0));

        Button btnSync = new Button("🔄 Load Central Processes");
        btnSync.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");

        Button btnRun = new Button("🚀 Run Simulation");
        btnRun.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");

        Label lblStatus = new Label("Ready to extract burst job sequences from global PCB pool.");
        lblStatus.setStyle("-fx-text-fill: #9aa0a6; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        controlRow.getChildren().addAll(btnSync, btnRun, lblStatus);

        // --- STEP 2: GANTT SEQUENCE VIEW TIMELINE ---
        ganttChart = new HBox(6);
        ganttChart.setPrefHeight(65);
        ganttChart.setAlignment(Pos.CENTER_LEFT);
        ganttChart.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-padding: 10; -fx-border-color: #3c4043; -fx-border-width: 1;");

        // --- STEP 3: METRICS PERFORMANCE TABLE ---
        table = new TableView<>(localProcessList);
        table.setStyle("-fx-background-color: #202124; -fx-border-color: #3c4043; -fx-border-radius: 4;");
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

        // --- STEP 4: ACTION INTERFACE LOGIC LOOPS ---

        // Central data state repository synchronization
        btnSync.setOnAction(e -> {
            localProcessList.clear();
            ganttChart.getChildren().clear();
            
            if (ProcessData.getMasterList().isEmpty()) {
                lblStatus.setText("❌ Master List Empty! Add processes via Process Manager on Desktop first.");
                lblStatus.setStyle("-fx-text-fill: #EA4335;");
                return;
            }

            for (ProcessData p : ProcessData.getMasterList()) {
                localProcessList.add(new ProcessData(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(), 0, 0));
            }
            lblStatus.setText("✅ Synchronized " + localProcessList.size() + " shortest job records safely.");
            lblStatus.setStyle("-fx-text-fill: #34A853;");
        });

        // Non-Preemptive Shortest Job First Calculation Core
        btnRun.setOnAction(e -> {
            if (localProcessList.isEmpty()) {
                lblStatus.setText("❌ No simulation data available. Pull matrix records first.");
                lblStatus.setStyle("-fx-text-fill: #EA4335;");
                return;
            }
            ganttChart.getChildren().clear();

            List<ProcessData> remainingList = new ArrayList<>(localProcessList);
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

                // Sorting Rule: Shortest Burst Time -> Ties resolved via earlier Arrival Time
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
            lblStatus.setText("⚡ Simulation completed execution using SJF evaluation rule.");
            lblStatus.setStyle("-fx-text-fill: #EA4335;");
        });

        // --- STEP 5: VISUAL CLEAN HIGHLIGHTED WHITE HEADERS ---
        Label lblActionTitle = new Label("Execution Control Desk:");
        lblActionTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Shortest Sequence Timeline Matrix:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblTableTitle = new Label("Calculated SJF Performance Registry:");
        lblTableTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(
            lblActionTitle, controlRow, 
            new Region() {{ setPrefHeight(5); }}, 
            lblGanttTitle, ganttChart, 
            new Region() {{ setPrefHeight(5); }}, 
            lblTableTitle, table
        );

        // Instant initializer handler trigger
        if (!ProcessData.getMasterList().isEmpty()) {
            btnSync.fire();
        }
    }
}