import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FCFSWindow extends BaseAlgorithmWindow {

    private final ObservableList<ProcessData> processList = FXCollections.observableArrayList();
    private final HBox ganttChart;

    public FCFSWindow(StackPane parentContainer) {
        super("First-Come, First-Served (FCFS)", parentContainer);

        // Input Form Panel
        HBox formRow = new HBox(10);
        formRow.setPadding(new Insets(5, 0, 5, 0));
        
        TextField txtId = createInputField(formRow, "PID (e.g. P1)", 100);
        TextField txtArrival = createInputField(formRow, "Arrival Time", 120);
        TextField txtBurst = createInputField(formRow, "Burst Time", 120);

        Button btnAdd = new Button("Add Process");
        btnAdd.setStyle("-fx-background-color: #34A853; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnAdd);

        Button btnRun = new Button("Run Simulation");
        btnRun.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        formRow.getChildren().add(btnRun);

        // Gantt View Container
        ganttChart = new HBox(4);
        ganttChart.setPrefHeight(60);
        ganttChart.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-padding: 10;");

        // Metrics Table View
        TableView<ProcessData> table = new TableView<>(processList);
        table.setStyle("-fx-background-color: #202124;");
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

        // Add Action Control Logic
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

            List<ProcessData> sortedProcesses = new ArrayList<>(processList);
            sortedProcesses.sort(Comparator.comparingInt(ProcessData::getArrivalTime));

            int currentTime = 0;
            for (ProcessData p : sortedProcesses) {
                if (currentTime < p.getArrivalTime()) {
                    int idleTime = p.getArrivalTime() - currentTime;
                    addGanttBlock(ganttChart, "IDLE", "t: " + currentTime + "-" + p.getArrivalTime(), "#3c4043");
                    currentTime = p.getArrivalTime();
                }
                
                int startTime = currentTime;
                currentTime += p.getBurstTime();
                
                int tat = currentTime - p.getArrivalTime();
                int wt = tat - p.getBurstTime();

                // Reflect computed calculation metrics back into table model properties
                processList.stream()
                        .filter(pr -> pr.getId().equals(p.getId()))
                        .findFirst()
                        .ifPresent(pr -> {
                            pr.waitingTimeProperty().set(wt);
                            pr.turnaroundTimeProperty().set(tat);
                        });

                addGanttBlock(ganttChart, p.getId(), "t: " + startTime + "-" + currentTime, "#4285F4");
            }
            table.refresh();
        });

        Label lblInputTitle = new Label("Add New Process Entry:");
        lblInputTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblGanttTitle = new Label("Gantt Sequence Preview:");
        lblGanttTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        // Add the white-colored label objects to the workspace instead of plain strings
        workspace.getChildren().addAll(lblInputTitle, formRow, lblGanttTitle, ganttChart, table);
    }
}