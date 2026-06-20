package Schedulings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import Process.ProcessData;

public class RoundRobinWindow extends BaseAlgorithmWindow {

    // Central repository sync karne ke liye local tracking matrix
    private final ObservableList<ProcessData> localProcessList = FXCollections.observableArrayList();
    private final HBox ganttChart;
    private final TableView<ProcessData> table;
    private int currentTimeQuantum = 2;

    @SuppressWarnings("unchecked")
    public RoundRobinWindow(StackPane parentContainer) {
        super("Round Robin (RR) Cyclic Scheduling", parentContainer);

        // --- STEP 1: QUANTUM CONFIGURATION CONTROL ROW ---
        HBox configRow = new HBox(12);
        configRow.setAlignment(Pos.CENTER_LEFT);
        configRow.setPadding(new Insets(5, 0, 5, 0));
        
        Label lblQuantum = new Label("Time Quantum (t):");
        lblQuantum.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 13px;");
        
        TextField txtQuantum = new TextField(String.valueOf(currentTimeQuantum));
        txtQuantum.setPrefWidth(60);
        txtQuantum.setStyle("-fx-background-color: #2d2e31; -fx-text-fill: white; -fx-border-color: #3c4043; -fx-border-radius: 3; -fx-alignment: center; -fx-font-weight: bold;");
        
        Button btnUpdateQuantum = new Button("Apply Quantum");
        btnUpdateQuantum.setStyle("-fx-background-color: #FBBC05; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");

        Region separatorSpacer = new Region();
        HBox.setHgrow(separatorSpacer, Priority.ALWAYS);

        Button btnSync = new Button("🔄 Load Central Processes");
        btnSync.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12 6 12;");

        Button btnRun = new Button("🚀 Run Simulation");
        btnRun.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12 6 12;");

        configRow.getChildren().addAll(lblQuantum, txtQuantum, btnUpdateQuantum, separatorSpacer, btnSync, btnRun);

        // Status Banner bar below configurations
        Label lblStatus = new Label("Configure parameters or pull dynamic processes from global PCB pool.");
        lblStatus.setStyle("-fx-text-fill: #9aa0a6; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");

        // --- STEP 2: GANTT VIEW TIMELINE CONTAINER ---
        ganttChart = new HBox(5);
        ganttChart.setPrefHeight(65);
        ganttChart.setAlignment(Pos.CENTER_LEFT);
        ganttChart.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-padding: 10; -fx-border-color: #3c4043; -fx-border-width: 1;");

        // --- STEP 3: METRICS DATA TABLE VIEW ---
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

        // --- STEP 4: ACTION CONTROLLERS & SCHEDULING ENGINES ---

        btnUpdateQuantum.setOnAction(e -> {
            if(!txtQuantum.getText().isEmpty()) {
                currentTimeQuantum = Integer.parseInt(txtQuantum.getText().trim());
                lblStatus.setText("🎯 Time Quantum updated to: " + currentTimeQuantum + " units.");
                lblStatus.setStyle("-fx-text-fill: #FBBC05;");
            }
        });

        // Sync repository elements context logic handler
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
            lblStatus.setText("✅ Loaded " + localProcessList.size() + " processes dynamically. Ready to simulate.");
            lblStatus.setStyle("-fx-text-fill: #34A853;");
        });

        // Preemptive Round Robin Loop Algorithm Engine
        btnRun.setOnAction(e -> {
            if (localProcessList.isEmpty()) {
                lblStatus.setText("❌ No processes evaluated inside workspace. Click 'Load Central Processes' first.");
                lblStatus.setStyle("-fx-text-fill: #EA4335;");
                return;
            }
            ganttChart.getChildren().clear();

            List<ProcessData> source = new ArrayList<>(localProcessList);
            source.sort((p1, p2) -> Integer.compare(p1.getArrivalTime(), p2.getArrivalTime()));

            int[] remBurst = new int[source.size()];
            for (int i = 0; i < source.size(); i++) remBurst[i] = source.get(i).getBurstTime();

            Queue<Integer> queue = new LinkedList<>();
            int currentTime = 0;
            int idx = 0;

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

                while (idx < source.size() && source.get(idx).getArrivalTime() <= currentTime) {
                    queue.add(idx);
                    idx++;
                }

                if (remBurst[currentIdx] > 0) {
                    queue.add(currentIdx);
                } else {
                    int tat = currentTime - p.getArrivalTime();
                    int wt = tat - p.getBurstTime();
                    
                    // Push metrics values back properties models parameters
                    localProcessList.stream()
                            .filter(pr -> pr.getId().equals(p.getId()))
                            .findFirst()
                            .ifPresent(pr -> {
                                pr.waitingTimeProperty().set(wt);
                                pr.turnaroundTimeProperty().set(tat);
                            });
                }
            }
            table.refresh();
            lblStatus.setText("⚡ Preemptive Cyclic Simulation completed successfully via Quantum size (" + currentTimeQuantum + ").");
            lblStatus.setStyle("-fx-text-fill: #9B59B6;");
        });

        // --- STEP 5: VISUAL CLEAN HIGHLIGHTED HEADERS ---
        Label lblConfigTitle = new Label("Scheduler Optimization & Engine Control Desk:");
        lblConfigTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Time-Slice Multiplex Timeline:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label lblTableTitle = new Label("Calculated Round Robin Quantum Metrics Registry:");
        lblTableTitle.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(
            lblConfigTitle, configRow, lblStatus,
            new Region() {{ setPrefHeight(5); }}, 
            lblGanttTitle, ganttChart, 
            new Region() {{ setPrefHeight(5); }}, 
            lblTableTitle, table
        );

        // Auto load initial initialization triggers
        if (!ProcessData.getMasterList().isEmpty()) {
            btnSync.fire();
        }
    }
}