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

public class PriorityWindow extends BaseAlgorithmWindow {

    // Central core list ke sath mapping karne ke liye local reactive list
    private final ObservableList<ProcessData> localProcessList = FXCollections.observableArrayList();
    private final HBox ganttChart;
    private final TableView<ProcessData> table;

    @SuppressWarnings("unchecked")
    public PriorityWindow(StackPane parentContainer) {
        super("Priority Scheduling Space (Non-Preemptive)", parentContainer);

        // --- STEP 1: CENTRAL SYNC CONTROL ROW ---
        HBox controlRow = new HBox(15);
        controlRow.setAlignment(Pos.CENTER_LEFT);
        controlRow.setPadding(new Insets(5, 0, 5, 0));

        Button btnSync = new Button("🔄 Load Central Processes");
        btnSync.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");

        Button btnRun = new Button("🚀 Run Simulation");
        btnRun.setStyle("-fx-background-color: #1ABC9C; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");

        Label lblStatus = new Label("Ready to parse priority sequences from global PCB pool.");
        lblStatus.setStyle("-fx-text-fill: #9aa0a6; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        controlRow.getChildren().addAll(btnSync, btnRun, lblStatus);

        // --- STEP 2: GANTT VIEW CONTAINER ---
        ganttChart = new HBox(6);
        ganttChart.setPrefHeight(65);
        ganttChart.setAlignment(Pos.CENTER_LEFT);
        ganttChart.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-padding: 10; -fx-border-color: #3c4043; -fx-border-width: 1;");

        // --- STEP 3: METRICS TABLE VIEW ---
        table = new TableView<>(localProcessList);
        table.setStyle("-fx-background-color: #202124; -fx-border-color: #3c4043; -fx-border-radius: 4;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<ProcessData, String> colId = new TableColumn<>("Process ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<ProcessData, Integer> colPriority = new TableColumn<>("Priority Level");
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        
        TableColumn<ProcessData, Integer> colArr = new TableColumn<>("Arrival Time");
        colArr.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        
        TableColumn<ProcessData, Integer> colBurst = new TableColumn<>("Burst Time");
        colBurst.setCellValueFactory(new PropertyValueFactory<>("burstTime"));
        
        TableColumn<ProcessData, Integer> colWait = new TableColumn<>("Waiting Time");
        colWait.setCellValueFactory(new PropertyValueFactory<>("waitingTime"));
        
        TableColumn<ProcessData, Integer> colTurn = new TableColumn<>("Turnaround Time");
        colTurn.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));

        table.getColumns().addAll(colId, colPriority, colArr, colBurst, colWait, colTurn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- STEP 4: ACTION SYNC & ENGINE LOGIC ---

        // Central pool se instances pull karne ka sync layout action
        btnSync.setOnAction(e -> {
            localProcessList.clear();
            ganttChart.getChildren().clear();
            
            if (ProcessData.getMasterList().isEmpty()) {
                lblStatus.setText("❌ Master List Empty! Add processes via Process Manager on Desktop.");
                lblStatus.setStyle("-fx-text-fill: #EA4335;");
                return;
            }

            for (ProcessData p : ProcessData.getMasterList()) {
                // Global repository se data direct properties model mein map ho raha hai
                localProcessList.add(new ProcessData(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(), 0, 0));
            }
            lblStatus.setText("✅ Synchronized " + localProcessList.size() + " priority execution models.");
            lblStatus.setStyle("-fx-text-fill: #34A853;");
        });

        // Priority Engine Loop Context
        btnRun.setOnAction(e -> {
            if (localProcessList.isEmpty()) {
                lblStatus.setText("❌ No tracking matrix found. Pull data using central button first.");
                lblStatus.setStyle("-fx-text-fill: #EA4335;");
                return;
            }
            ganttChart.getChildren().clear();

            List<ProcessData> remainingList = new ArrayList<>(localProcessList);
            int currentTime = 0;

            while (!remainingList.isEmpty()) {
                int finalCurrentTime = currentTime;
                List<ProcessData> available = new ArrayList<>();
                for (ProcessData p : remainingList) {
                    if (p.getArrivalTime() <= finalCurrentTime) {
                        available.add(p);
                    }
                }

                if (available.isEmpty()) {
                    int nextArrival = remainingList.stream().mapToInt(ProcessData::getArrivalTime).min().orElse(currentTime);
                    addGanttBlock(ganttChart, "IDLE", "t: " + currentTime + "-" + nextArrival, "#3c4043");
                    currentTime = nextArrival;
                    continue;
                }

                // Rule: Lower Priority Number = Higher Executive Preference
                ProcessData highestPriorityProcess = available.stream()
                        .min(Comparator.comparingInt(ProcessData::getPriority)
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
            lblStatus.setText("⚡ Simulation completed execution using Priority evaluation rule.");
            lblStatus.setStyle("-fx-text-fill: #1ABC9C;");
        });

        // --- STEP 5: VISUAL CLEAN HIGHLIGHTED HEADERS ---
        Label lblActionTitle = new Label("Execution Control Desk:");
        lblActionTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Priority Order Timeline Matrix:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblTableTitle = new Label("Calculated Priority Performance Registry:");
        lblTableTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(
            lblActionTitle, controlRow, 
            new Region() {{ setPrefHeight(5); }}, 
            lblGanttTitle, ganttChart, 
            new Region() {{ setPrefHeight(5); }}, 
            lblTableTitle, table
        );

        // Auto fire on instantiation context
        if (!ProcessData.getMasterList().isEmpty()) {
            btnSync.fire();
        }
    }
}