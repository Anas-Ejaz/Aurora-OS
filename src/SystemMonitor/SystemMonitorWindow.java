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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.Random;

public class SystemMonitorWindow extends javafx.scene.layout.StackPane {

    private final VBox windowContent;
    private final VBox workspace;
    private boolean isMaximized = false;
    private final StackPane parentContainer;

    private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    
    // UPDATED DATA MODEL TYPE: Using ProcessLogData to show App Names and MB footprint
    private final ObservableList<ProcessLogData> logRecords = FXCollections.observableArrayList();
    private final TableView<ProcessLogData> logTable; // Declared at class level for scope access
    
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

        HBox titleBar = new HBox(8);
        titleBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 10 10 0 0;");

        Label titleLabel = new Label("System Resource Profiler & Active Process Engine");
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

        HBox dashboardRow = new HBox(15);
        VBox cpuCard = constructStatPanelCard("CPU core usage", lblCpuPercent, "#4285F4");
        VBox ramCard = constructStatPanelCard("allocated ram footprint", lblRamGigabytes, "#34A853");
        VBox diskCard = constructStatPanelCard("active disk io velocity", lblDiskSpeed, "#FBBC05");
        HBox.setHgrow(cpuCard, Priority.ALWAYS);
        HBox.setHgrow(ramCard, Priority.ALWAYS);
        HBox.setHgrow(diskCard, Priority.ALWAYS);
        dashboardRow.getChildren().addAll(cpuCard, ramCard, diskCard);

        xAxis.setLabel("Elapsed Sim Time (s)");
        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        yAxis.setLabel("Utilization Scale Ratio (%)");

        LineChart<Number, Number> performanceGraph = new LineChart<>(xAxis, yAxis);
        performanceGraph.setPrefHeight(180);
        performanceGraph.setStyle("-fx-background-color: #2d2e31; -fx-legend-side: TOP;");
        cpuSeries.setName("Actual CPU System Load");
        performanceGraph.getData().add(cpuSeries);

        // --- UPDATED PROCESS TABLE VIEW CONFIGURATION ---
        logTable = new TableView<>(logRecords);
        logTable.setStyle("-fx-background-color: #202124;");
        VBox.setVgrow(logTable, Priority.ALWAYS);

        TableColumn<ProcessLogData, String> colName = new TableColumn<>("Application Process Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("appName")); 
        
        TableColumn<ProcessLogData, String> colPid = new TableColumn<>("OS PID Token");
        colPid.setCellValueFactory(new PropertyValueFactory<>("pidString")); 

        TableColumn<ProcessLogData, String> colMemory = new TableColumn<>("RAM Memory Footprint");
        colMemory.setCellValueFactory(new PropertyValueFactory<>("memoryWorkingSet")); 

        TableColumn<ProcessLogData, String> colStatus = new TableColumn<>("Runtime Status Context");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText")); 

        logTable.getColumns().addAll(colName, colPid, colMemory, colStatus);
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label lblDashboardHeader = new Label("Hardware Resource Matrix State Monitors:");
        lblDashboardHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblChartHeader = new Label("Graphical Metric Visualizations:");
        lblChartHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label lblLogHeader = new Label("Live Background Applications Tracker Logs:");
        lblLogHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        workspace.getChildren().addAll(
            lblDashboardHeader, 
            dashboardRow, 
            lblChartHeader, 
            performanceGraph, 
            lblLogHeader, 
            logTable
        );

        // Continuous Loop Clock
        Timeline processingClock = new Timeline(new KeyFrame(Duration.seconds(1), event -> evaluateLiveMetrics()));
        processingClock.setCycleCount(Animation.INDEFINITE);
        processingClock.play();

        this.parentProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) processingClock.stop();
        });
    }

    private void evaluateLiveMetrics() {
        double cpuLoad = osBean.getCpuLoad();
        if (cpuLoad < 0) cpuLoad = 0.0;
        int generatedCpu = (int) (cpuLoad * 100);

        long totalMemoryBytes = osBean.getTotalMemorySize();
        long freeMemoryBytes = osBean.getFreeMemorySize();
        long usedMemoryBytes = totalMemoryBytes - freeMemoryBytes;

        double totalMemoryCapacity = totalMemoryBytes / (1024.0 * 1024.0 * 1024.0);
        double equivalentGigabytes = usedMemoryBytes / (1024.0 * 1024.0 * 1024.0);
        double generatedDiskIO = randomEngine.nextDouble() * 12.8;

        lblCpuPercent.setText(generatedCpu + "%");
        lblRamGigabytes.setText(String.format("%.2f GB / %.1f GB", equivalentGigabytes, totalMemoryCapacity));
        lblDiskSpeed.setText(String.format("%.1f MB/s", generatedDiskIO));

        cpuSeries.getData().add(new XYChart.Data<>(clockTick, generatedCpu));

        if (clockTick > 30) {
            xAxis.setLowerBound(clockTick - 30);
            xAxis.setUpperBound(clockTick);
        } else {
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(30);
        }
        if (cpuSeries.getData().size() > 31) {
            cpuSeries.getData().remove(0);
        }

        // --- REAL TIMELINE TASKLIST PARSER ENGINE ---
        fetchNativeOSProcesses();

        clockTick++;
    }

    private void fetchNativeOSProcesses() {
        logRecords.clear(); 

        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process process;
            
            if (os.contains("win")) {
                // FILTER HATADIA: Ab yeh direct saari running processes real-time uthayega
                process = Runtime.getRuntime().exec("tasklist /NH"); 
            } else {
                process = Runtime.getRuntime().exec("ps -eo comm,pid,rss");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int displayCounter = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("Image Name") || line.startsWith("====")) continue;

                if (os.contains("win")) {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 5) {
                        String name = tokens[0];
                        String pid = tokens[1];
                        
                        // Memory humesha aakhir se second token hota hai (e.g., "150,432 K")
                        String rawMemoryStr = tokens[tokens.length - 2].replace(",", "").replace(".", "");
                        
                        try {
                            double memoryMegabytes = Double.parseDouble(rawMemoryStr) / 1024.0;
                            
                            // UPDATED: Limit barha kar 150 aur threshold 10MB kar diya hai taake Chrome miss na ho
                            if (memoryMegabytes > 10.0 && displayCounter < 150) {
                                String formattedMem = String.format("%.1f MB", memoryMegabytes);
                                
                                // Agar name chrome.exe hai to uppercase karke highlight context de do
                                String status = name.toLowerCase().contains("chrome") ? "BROWSING ACTIVE" : "ACTIVE EXECUTING";
                                
                                logRecords.add(new ProcessLogData(name, pid, formattedMem, status));
                                displayCounter++;
                            }
                        } catch (NumberFormatException nfe) {
                            // Lines parse error skip karne ke liye
                        }
                    }
                } else {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 3) {
                        String name = tokens[0];
                        String pid = tokens[1];
                        try {
                            double mb = Double.parseDouble(tokens[2]) / 1024.0;
                            if (mb > 10.0 && displayCounter < 150) {
                                logRecords.add(new ProcessLogData(name, pid, String.format("%.1f MB", mb), "RUNNING"));
                                displayCounter++;
                            }
                        } catch (Exception e) {}
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            // Agar bilkul hi block ho jaye tabhi yeh mock data dikhayega
            logRecords.add(new ProcessLogData("SYSTEM_RESTRICTED.exe", "0000", "0.0 MB", "SECURITY BLOCK"));
        }

        logTable.refresh();
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