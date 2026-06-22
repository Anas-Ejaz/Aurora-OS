package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import Process.ProcessData;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PcbWindow extends BaseAlgorithmWindow {

    // UI Reference Controls
    private final Label lblCpuStatus = new Label("CPU STATE: IDLE");
    private final ProgressIndicator cpuProgress = new ProgressIndicator();
    private final TextArea txtSimLog = new TextArea();
    private final HBox cpuVisualBox = new HBox(10);
    private final Label lblCurrentCpuProc = new Label("NONE");
    
    private TableView<ProcessData> table;

    public PcbWindow(StackPane parentContainer) {
        super("Core OS Module: Process Control Block (PCB) Registry", parentContainer);

        // Fallback: Simulation ke liye agar scheduling master list khali ho tou sample data insert krein
        if (ProcessData.getMasterList().isEmpty()) {
            ProcessData.getMasterList().add(new ProcessData("P1", 0, 8, 1, 0, 0));
            ProcessData.getMasterList().add(new ProcessData("P2", 1, 4, 2, 0, 0));
            
            // Shuru mein memory ko reset kar rahe hain taake user manually allocate kare
            ProcessData.getMasterList().get(0).setState("READY");
            ProcessData.getMasterList().get(1).setState("READY");
        }

        // --- 1. Top Controls Panel (Sirf Memory Allocation Ke Liye) ---
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        Label lblInfo = new Label("Select Process below & set Memory:");
        lblInfo.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 12px;");

        TextField txtMemInput = new TextField();
        txtMemInput.setPromptText("Memory Size (e.g., 256MB)");
        txtMemInput.setPrefWidth(180);
        txtMemInput.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white; -fx-prompt-text-fill: #9aa0a6; -fx-background-radius: 4;");

        Button btnAllocateMem = new Button("Assign & Allocate Memory");
        btnAllocateMem.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        inputRow.getChildren().addAll(lblInfo, txtMemInput, btnAllocateMem);

        // --- 2. Live Top Visual CPU Slot Panel ---
        VBox cpuPanel = new VBox(8);
        cpuPanel.setPadding(new Insets(12));
        cpuPanel.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 8; -fx-border-color: #3c4043;");
        
        HBox cpuControls = new HBox(15);
        cpuControls.setAlignment(Pos.CENTER_LEFT);
        
        Button btnContextSwitch = new Button("Trigger Context Switch");
        btnContextSwitch.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        cpuProgress.setVisible(false);
        cpuProgress.setPrefSize(18, 18);
        lblCpuStatus.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        
        // Graphic CPU core representations
        cpuVisualBox.setAlignment(Pos.CENTER);
        cpuVisualBox.setPadding(new Insets(5, 15, 5, 15));
        cpuVisualBox.setStyle("-fx-background-color: #202124; -fx-border-color: #FBBC05; -fx-background-radius: 4;");
        Label lblCpuTitle = new Label("CORE_0:");
        lblCpuTitle.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 11px; -fx-font-weight: bold;");
        lblCurrentCpuProc.setStyle("-fx-text-fill: #0F9D58; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-weight: bold;");
        cpuVisualBox.getChildren().addAll(lblCpuTitle, lblCurrentCpuProc);

        cpuControls.getChildren().addAll(btnContextSwitch, cpuProgress, lblCpuStatus, new Region(), cpuVisualBox);
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        
        cpuPanel.getChildren().addAll(new Label("OS KERNEL CPU SCHEDULER DISPATCHER:"), cpuControls);
        updateVisualCpuSlot();

        // --- 3. Split Layout for Table (Left) and Simulation Log (Right) ---
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(15);
        VBox.setVgrow(mainGrid, Priority.ALWAYS);
        
        // Columns setup: 65% Table, 35% Log Console
        ColumnConstraints colTable = new ColumnConstraints();
        colTable.setPercentWidth(65);
        ColumnConstraints colLog = new ColumnConstraints();
        colLog.setPercentWidth(35);
        mainGrid.getColumnConstraints().addAll(colTable, colLog);

        // --- Table Setup ---
        table = new TableView<>(ProcessData.getMasterList());
        table.setStyle("-fx-background-color: #202124;");
        
        TableColumn<ProcessData, String> colPid = new TableColumn<>("PID");
        colPid.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<ProcessData, String> colState = new TableColumn<>("Process State");
        colState.setCellValueFactory(new PropertyValueFactory<>("state"));
        
        TableColumn<ProcessData, String> colPc = new TableColumn<>("PC Register");
        colPc.setCellValueFactory(new PropertyValueFactory<>("pc"));
        
        TableColumn<ProcessData, String> colMem = new TableColumn<>("Memory Allocation Bounds");
        colMem.setCellValueFactory(new PropertyValueFactory<>("memory"));

        table.getColumns().addAll(colPid, colState, colPc, colMem);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        mainGrid.add(table, 0, 0);

        // --- Simulation Log Console Setup ---
        VBox logBox = new VBox(5);
        Label lblLogTitle = new Label("Kernel Simulation Terminal Output:");
        lblLogTitle.setStyle("-fx-text-fill: #8ab4f8; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        txtSimLog.setEditable(false);
        txtSimLog.setWrapText(true);
        txtSimLog.setStyle("-fx-control-inner-background: #171717; -fx-text-fill: #34A853; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");
        VBox.setVgrow(txtSimLog, Priority.ALWAYS);
        logBox.getChildren().addAll(lblLogTitle, txtSimLog);
        mainGrid.add(logBox, 1, 0);

        logWrite("Kernel Core PCB Module linked with Shared Scheduling Pipeline context.");

        // --- CORE LOGIC EVENT ACTIONS ---
        
        // Memory assign karne ka handler (Jo row selected hogi us par allocate hoga)
        btnAllocateMem.setOnAction(e -> {
            ProcessData selectedProc = table.getSelectionModel().getSelectedItem();
            String memText = txtMemInput.getText().trim();
            
            if (selectedProc == null) {
                logWrite("Error: Please select a process row from the table first to assign memory.");
                return;
            }
            if (memText.isEmpty()) {
                logWrite("Error: Memory size field cannot be empty.");
                return;
            }
            
            // Set memory value internally
            selectedProc.memoryProperty().set(memText);
            table.refresh();
            logWrite("Memory Management Unit (MMU): Allocated " + memText + " segment bounds for process [" + selectedProc.getId() + "]");
            txtMemInput.clear();
        });

        btnContextSwitch.setOnAction(e -> {
            if (ProcessData.getMasterList().size() >= 2) {
                simulateContextSwitch();
            } else {
                lblCpuStatus.setText("ERROR: Need >= 2 processes.");
                lblCpuStatus.setStyle("-fx-text-fill: #EA4335;");
                logWrite("Trap Error: Context switches requires minimum 2 active processes.");
            }
        });

        Label sectionLabel = new Label("Active Memory-Bounded Kernel Process Control Blocks:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(cpuPanel, sectionLabel, inputRow, mainGrid);
    }

    private void simulateContextSwitch() {
        // Find current RUNNING and next READY process from master list
        ProcessData runningProc = null;
        ProcessData readyProc = null;

        for (ProcessData proc : ProcessData.getMasterList()) {
            if ("RUNNING".equals(proc.getState()) && runningProc == null) {
                runningProc = proc;
            } else if ("READY".equals(proc.getState()) && readyProc == null) {
                readyProc = proc;
            }
        }

        // Fallbacks if states are unassigned or clean
        if (runningProc == null) runningProc = ProcessData.getMasterList().get(0);
        if (readyProc == null) readyProc = ProcessData.getMasterList().get(1);

        final ProcessData finalRunning = runningProc;
        final ProcessData finalReady = readyProc;

        // CRITICAL CHECK: Agar targets me se kisi ki memory allocated nahi hai, to context switch block ho jaye!
        String runMem = finalRunning.getMemory() != null ? finalRunning.getMemory().trim() : "";
        String readyMem = finalReady.getMemory() != null ? finalReady.getMemory().trim() : "";

        if (runMem.isEmpty() || readyMem.isEmpty()) {
            lblCpuStatus.setText("TRAP: MEMORY VIOLATION");
            lblCpuStatus.setStyle("-fx-text-fill: #EA4335;");
            logWrite("Execution Refused: Cannot schedule processes without explicit MMU memory boundaries allocated.");
            return;
        }

        // If validation clears, start context switch transition delay
        cpuProgress.setVisible(true);
        lblCpuStatus.setText("SAVING CPU STATE...");
        lblCpuStatus.setStyle("-fx-text-fill: #FBBC05;");
        
        logWrite("Context Switch Saved: Retained register offsets of " + finalRunning.getId() + " at stack frame " + finalRunning.getPc());

        // 1.5 Seconds execution latency simulator
        PauseTransition kernelSwitchDelay = new PauseTransition(Duration.seconds(1.5));
        kernelSwitchDelay.setOnFinished(e -> {
            cpuProgress.setVisible(false);

            // Step 1: Change old running context back to Ready queue branch
            finalRunning.setPc("0x0040" + String.format("%02X", (int)(Math.random() * 255)));
            finalRunning.setState("READY");

            // Step 2: Swap the next eligible memory-allocated node to execution
            finalReady.setState("RUNNING");

            // Refresh UI tables
            table.refresh();
            updateVisualCpuSlot();

            lblCpuStatus.setText("SWITCH SUCCESS!");
            lblCpuStatus.setStyle("-fx-text-fill: #34A853;");
            
            logWrite("Dispatcher Switch Finished: [Pushed: " + finalRunning.getId() + " -> READY] | [Dispatched: " + finalReady.getId() + " -> RUNNING]");
        });
        kernelSwitchDelay.play();
    }

    private void updateVisualCpuSlot() {
        String currentRunning = "IDLE";
        for (ProcessData p : ProcessData.getMasterList()) {
            if ("RUNNING".equals(p.getState())) {
                // Ensure text layout handles memory validation safe guards
                String memCheck = p.getMemory() != null ? p.getMemory() : "";
                if (!memCheck.isEmpty()) {
                    currentRunning = p.getId() + " (" + p.getPc() + ")";
                }
                break;
            }
        }
        lblCurrentCpuProc.setText(currentRunning);
        if ("IDLE".equals(currentRunning)) {
            cpuVisualBox.setStyle("-fx-background-color: #202124; -fx-border-color: #EA4335; -fx-background-radius: 4;");
        } else {
            cpuVisualBox.setStyle("-fx-background-color: #202124; -fx-border-color: #34A853; -fx-background-radius: 4;");
        }
    }

    // --- RESTORED LOGWRITE METHOD TO FIX COMPILATION ERROR ---
    private void logWrite(String logMessage) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        txtSimLog.appendText("[" + timestamp + "] " + logMessage + "\n");
    }
}