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

public class FCFSWindow extends BaseAlgorithmWindow {

    // Is window ki apni reactive temporary execution list
    private final ObservableList<ProcessData> localProcessList = FXCollections.observableArrayList();
    private final HBox ganttChart;
    private final TableView<ProcessData> table;

    public FCFSWindow(StackPane parentContainer) {
        super("First-Come, First-Served (FCFS)", parentContainer);

        // --- STEP 1: CENTRAL SYNC CONTROL ROW ---
        HBox controlRow = new HBox(15);
        controlRow.setAlignment(Pos.CENTER_LEFT);
        controlRow.setPadding(new Insets(5, 0, 5, 0));

        Button btnSync = new Button("🔄 Load Central Processes");
        btnSync.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");

        Button btnRun = new Button("🚀 Run Simulation");
        btnRun.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");

        Label lblStatus = new Label("Ready to sync with global PCB pool.");
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

        // --- STEP 4: ACTION LOGIC INTERFACES ---

        // Central List se local execution list mein copy karne ka button logic
        btnSync.setOnAction(e -> {
            localProcessList.clear();
            ganttChart.getChildren().clear();
            
            if (ProcessData.getMasterList().isEmpty()) {
                lblStatus.setText("❌ Master List Empty! Please add processes in Process Manager first.");
                lblStatus.setStyle("-fx-text-fill: #EA4335;");
                return;
            }

            // Central pool se instances copy karna taake original values safe rahein
            for (ProcessData p : ProcessData.getMasterList()) {
                localProcessList.add(new ProcessData(p.getId(), p.getArrivalTime(), p.getBurstTime(), p.getPriority(), 0, 0));
            }
            lblStatus.setText("✅ Synchronized " + localProcessList.size() + " processes successfully.");
            lblStatus.setStyle("-fx-text-fill: #34A853;");
        });

        // FCFS Simulation Engine Logic
        btnRun.setOnAction(e -> {
            if (localProcessList.isEmpty()) {
                lblStatus.setText("❌ No processes to simulate. Click 'Load Central Processes' first.");
                lblStatus.setStyle("-fx-text-fill: #EA4335;");
                return;
            }
            ganttChart.getChildren().clear();

            List<ProcessData> sortedProcesses = new ArrayList<>(localProcessList);
            sortedProcesses.sort(Comparator.comparingInt(ProcessData::getArrivalTime));

            int currentTime = 0;
            for (ProcessData p : sortedProcesses) {
                // Handle CPU Idle State Context
                if (currentTime < p.getArrivalTime()) {
                    int idleStart = currentTime;
                    currentTime = p.getArrivalTime();
                    addGanttBlock(ganttChart, "IDLE", "t: " + idleStart + "-" + currentTime, "#3c4043");
                }
                
                int startTime = currentTime;
                currentTime += p.getBurstTime();
                
                int tat = currentTime - p.getArrivalTime();
                int wt = tat - p.getBurstTime();

                // Properties model update matrix execution
                localProcessList.stream()
                        .filter(pr -> pr.getId().equals(p.getId()))
                        .findFirst()
                        .ifPresent(pr -> {
                            pr.waitingTimeProperty().set(wt);
                            pr.turnaroundTimeProperty().set(tat);
                        });

                // Blue block layout for active executing processes
                addGanttBlock(ganttChart, p.getId(), "t: " + startTime + "-" + currentTime, "#4285F4");
            }
            table.refresh();
            lblStatus.setText("⚡ Simulation completed execution using FCFS scheduler.");
            lblStatus.setStyle("-fx-text-fill: #4285F4;");
        });

        // --- STEP 5: CLEAN DESIGN HIGHLIGHTED HEADERS ---
        Label lblActionTitle = new Label("Execution Control Desk:");
        lblActionTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Sequence Timeline Matrix:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblTableTitle = new Label("Calculated Scheduling Performance Registry:");
        lblTableTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Clear view injection setup to workspace panel container
        workspace.getChildren().addAll(
            lblActionTitle, controlRow, 
            new Region() {{ setPrefHeight(5); }}, 
            lblGanttTitle, ganttChart, 
            new Region() {{ setPrefHeight(5); }}, 
            lblTableTitle, table
        );
        
        // Auto-load data if anything exists in the centralized model instantly
        if (!ProcessData.getMasterList().isEmpty()) {
            btnSync.fire();
        }
    }
}