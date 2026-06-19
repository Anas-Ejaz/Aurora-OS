package SystemMonitor;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

// REAL HARDWARE UTILITIES
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.Random;

public class SystemMonitorWindow extends javafx.scene.layout.StackPane {

    private final VBox windowContent;
    private final VBox workspace;
    private boolean isMaximized = false;
    private final StackPane parentContainer;

    // Actual Operating System Monitoring Connection
    private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    private final ObservableList<ProcessLogData> logRecords = FXCollections.observableArrayList();
    
    // Making X-Axis an instance variable so we can dynamically change its bounds later
    private final NumberAxis xAxis = new NumberAxis(0, 30, 5); 
    private final XYChart.Series<Number, Number> cpuSeries = new XYChart.Series<>();
    
    private int clockTick = 0;
    private final Random randomEngine = new Random();

    private final Label lblCpuPercent = new Label("0%");
    private final Label lblRamGigabytes = new Label("0.00 GB");
    private final Label lblDiskSpeed = new Label("0.0 MB/s");

    public SystemMonitorWindow(StackPane parentContainer) {
        this.parentContainer = parentContainer;
        this.setMaxSize(850, 600);

        windowContent = new VBox();
        windowContent.setStyle(
            "-fx-background-color: #202124;" + 
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-border-color: #3c4043;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 5);"
        );

        // Title Bar Configuration
        HBox titleBar = new HBox(8);
        titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 10 10 0 0;");

        Label titleLabel = new Label("System Resource Profiler & Log Engine");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 13;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button maxBtn = new Button();
        maxBtn.setPrefSize(13, 13);
        maxBtn.setMinSize(13, 13);
        maxBtn.setStyle("-fx-background-color: #34A853; -fx-background-radius: 50%; -fx-cursor: hand;");

        Button closeBtn = new Button();
        closeBtn.setPrefSize(13, 13);
        closeBtn.setMinSize(13, 13);
        closeBtn.setStyle("-fx-background-color: #EA4335; -fx-background-radius: 50%; -fx-cursor: hand;");

        closeBtn.setOnAction(e -> parentContainer.getChildren().remove(this));
        maxBtn.setOnAction(e -> toggleWindowMaximize());

        titleBar.getChildren().addAll(titleLabel, spacer, maxBtn, closeBtn);

        workspace = new VBox(15);
        workspace.setPadding(new Insets(20));
        VBox.setVgrow(workspace, Priority.ALWAYS);
        windowContent.getChildren().addAll(titleBar, workspace);
        this.getChildren().add(windowContent);

        // --- Hardware Dash Cards Panel ---
        HBox dashboardRow = new HBox(15);
        VBox cpuCard = constructStatPanelCard("CPU core usage", lblCpuPercent, "#4285F4");
        VBox ramCard = constructStatPanelCard("allocated ram footprint", lblRamGigabytes, "#34A853");
        VBox diskCard = constructStatPanelCard("active disk io velocity", lblDiskSpeed, "#FBBC05");
        HBox.setHgrow(cpuCard, Priority.ALWAYS);
        HBox.setHgrow(ramCard, Priority.ALWAYS);
        HBox.setHgrow(diskCard, Priority.ALWAYS);
        dashboardRow.getChildren().addAll(cpuCard, ramCard, diskCard);

        // Chart Component Setup
        xAxis.setLabel("Elapsed Sim Time (s)");
        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        yAxis.setLabel("Utilization Scale Ratio (%)");

        LineChart<Number, Number> performanceGraph = new LineChart<>(xAxis, yAxis);
        performanceGraph.setPrefHeight(200);
        performanceGraph.setStyle("-fx-background-color: #2d2e31; -fx-legend-side: TOP;");
        cpuSeries.setName("Actual CPU System Load");
        
        performanceGraph.getData().add(cpuSeries);

        // Process Monitoring Table View
        TableView<ProcessLogData> logTable = new TableView<>(logRecords);
        logTable.setStyle("-fx-background-color: #202124;");
        VBox.setVgrow(logTable, Priority.ALWAYS);

        TableColumn<ProcessLogData, Integer> colTime = new TableColumn<>("Time Index");
        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        TableColumn<ProcessLogData, Double> colCpu = new TableColumn<>("Actual CPU (%)");
        colCpu.setCellValueFactory(new PropertyValueFactory<>("cpuUsage"));
        TableColumn<ProcessLogData, Double> colRam = new TableColumn<>("Memory Usage (%)");
        colRam.setCellValueFactory(new PropertyValueFactory<>("ramUsage"));
        TableColumn<ProcessLogData, String> colStatus = new TableColumn<>("Operating State");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        logTable.getColumns().addAll(colTime, colCpu, colRam, colStatus);
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // WHITE TEXT LABELS
        Label lblDashboardHeader = new Label("Hardware Resource Matrix State Monitors:");
        lblDashboardHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblChartHeader = new Label("Graphical Metric Visualizations:");
        lblChartHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblLogHeader = new Label("System Historical Performance Activity Log:");
        lblLogHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        workspace.getChildren().addAll(
            lblDashboardHeader, 
            dashboardRow, 
            lblChartHeader, 
            performanceGraph, 
            lblLogHeader, 
            logTable
        );

        // Fetch metrics every 1 second
        Timeline processingClock = new Timeline(new KeyFrame(Duration.seconds(1), event -> evaluateLiveMetrics()));
        processingClock.setCycleCount(Animation.INDEFINITE);
        processingClock.play();

        this.parentProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) processingClock.stop();
        });
    }

    private void evaluateLiveMetrics() {
        // 1. Get REAL CPU Load (Returns a value between 0.0 and 1.0)
        double cpuLoad = osBean.getCpuLoad();
        if (cpuLoad < 0) cpuLoad = 0.0; // Fail-safe fallback if OS is busy
        int generatedCpu = (int) (cpuLoad * 100);

        // 2. Get REAL RAM Footprint in Bytes
        long totalMemoryBytes = osBean.getTotalMemorySize();
        long freeMemoryBytes = osBean.getFreeMemorySize();
        long usedMemoryBytes = totalMemoryBytes - freeMemoryBytes;

        // Convert Bytes to Gigabytes (1 GB = 1024 * 1024 * 1024 Bytes)
        double totalMemoryCapacity = totalMemoryBytes / (1024.0 * 1024.0 * 1024.0);
        double equivalentGigabytes = usedMemoryBytes / (1024.0 * 1024.0 * 1024.0);
        double generatedRamPercent = ((double) usedMemoryBytes / totalMemoryBytes) * 100;

        // Simulated Disk Speed
        double generatedDiskIO = randomEngine.nextDouble() * 15.4;

        // Update UI Counters
        lblCpuPercent.setText(generatedCpu + "%");
        lblRamGigabytes.setText(String.format("%.2f GB / %.1f GB", equivalentGigabytes, totalMemoryCapacity));
        lblDiskSpeed.setText(String.format("%.1f MB/s", generatedDiskIO));

        // Push real CPU value into the line chart
        cpuSeries.getData().add(new XYChart.Data<>(clockTick, generatedCpu));

        // --- DYNAMIC SCROLLING LOGIC ---
        // Shifts the X-Axis bounds smoothly forward once data goes past 30 seconds
        if (clockTick > 30) {
            xAxis.setLowerBound(clockTick - 30);
            xAxis.setUpperBound(clockTick);
        } else {
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(30);
        }

        // Garbage clean graph cache list array size
        if (cpuSeries.getData().size() > 31) {
            cpuSeries.getData().remove(0);
        }

        // Context-driven operation state text
        String conditionText = (generatedCpu > 65) ? "HEAVY SYSTEM LOAD" : "OPTIMAL CORE STATE";
        
        // Log real metrics into table rows
        logRecords.add(0, new ProcessLogData(clockTick, generatedCpu, Math.round(generatedRamPercent * 10.0) / 10.0, conditionText));
        
        if (logRecords.size() > 50) {
            logRecords.remove(logRecords.size() - 1);
        }

        clockTick++;
    }

    private VBox constructStatPanelCard(String labelName, Label targetValueField, String lineHexAccent) {
        VBox frame = new VBox(4);
        frame.setPadding(new Insets(10));
        frame.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 6; -fx-border-color: " + lineHexAccent + "; -fx-border-width: 1;");
        
        Label tag = new Label(labelName.toUpperCase());
        tag.setStyle("-fx-text-fill: #e2e2e2; -fx-font-size: 10; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        targetValueField.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        
        frame.getChildren().addAll(tag, targetValueField);
        return frame;
    }

    private void toggleWindowMaximize() {
        if (!isMaximized) {
            this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(windowContent, Priority.ALWAYS);
            isMaximized = true;
        } else {
            this.setMaxSize(850, 600);
            isMaximized = false;
        }
    }
}