package Schedulings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;

import Process.ProcessData;

public class SRTFWindow extends BaseAlgorithmWindow {

    // Central control room se link karne ke liye local execution data list
    private final ObservableList<ProcessData> localProcessList = FXCollections.observableArrayList();
    private final HBox ganttChart;
    private final TableView<ProcessData> table;

    @SuppressWarnings("unchecked")
    public SRTFWindow(StackPane parentContainer) {
        super("Shortest Remaining Time First (SRTF) - Preemptive Space", parentContainer);

        // --- STEP 1: CENTRAL SYNC CONTROL DESK ROW ---
        HBox controlRow = new HBox(15);
        controlRow.setAlignment(Pos.CENTER_LEFT);
        controlRow.setPadding(new Insets(5, 0, 5, 0));

        Button btnSync = new Button("🔄 Load Central Processes");
        btnSync.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");

        Button btnRun = new Button("🚀 Run Simulation");
        btnRun.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");

        Label lblStatus = new Label("Ready to parse preemptive matrix streams from global PCB pool.");
        lblStatus.setStyle("-fx-text-fill: #9aa0a6; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        controlRow.getChildren().addAll(btnSync, btnRun, lblStatus);

        // --- STEP 2: GANTT SEQUENCE VIEW TIMELINE ---
        ganttChart = new HBox(6);
        ganttChart.setPrefHeight(65);
        ganttChart.setAlignment(Pos.CENTER_LEFT);
        ganttChart.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-padding: 10; -fx-border-color: #3c4043; -fx-border-width: 1;");

        // --- STEP 3: METRICS PERFORMANCE TABLE VIEW ---
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

        // --- STEP 4: PREEMPTIVE INTERFACES ENGINE LOGIC ---

        // Central Pool Synchronization Handler
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
            lblStatus.setText("✅ Synchronized " + localProcessList.size() + " preemptive tracking registers.");
            lblStatus.setStyle("-fx-text-fill: #34A853;");
        });

        // SRTF Core Algorithmic Loop Context
        btnRun.setOnAction(e -> {
            if (localProcessList.isEmpty()) {
                lblStatus.setText("❌ No workspace metadata injected. Sync with master data pool first.");
                lblStatus.setStyle("-fx-text-fill: #EA4335;");
                return;
            }
            ganttChart.getChildren().clear();

            List<ProcessData> list = new ArrayList<>(localProcessList);
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

                    // Push attributes into current active simulation model
                    list.get(shortest).waitingTimeProperty().set(wt);
                    list.get(shortest).turnaroundTimeProperty().set(tat);
                }
                currentTime++;
            }
            if (!lastRunningPID.isEmpty()) {
                addGanttBlock(ganttChart, lastRunningPID, "t: " + blockStartTime + "-" + currentTime, "#E67E22");
            }
            table.refresh();
            lblStatus.setText("⚡ Simulation completed execution using SRTF Preemptive evaluation rule.");
            lblStatus.setStyle("-fx-text-fill: #E67E22;");
        });

        // --- STEP 5: VISUAL CLEAN HIGHLIGHTED WHITE HEADERS ---
        Label lblActionTitle = new Label("Execution Control Desk:");
        lblActionTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Preemptive Progression Timeline:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblTableTitle = new Label("Calculated SRTF Performance Registry:");
        lblTableTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(
            lblActionTitle, controlRow, 
            new Region() {{ setPrefHeight(5); }}, 
            lblGanttTitle, ganttChart, 
            new Region() {{ setPrefHeight(5); }}, 
            lblTableTitle, table
        );

        // Auto load configuration if elements exist in main database instantiation context
        if (!ProcessData.getMasterList().isEmpty()) {
            btnSync.fire();
        }
    }
}