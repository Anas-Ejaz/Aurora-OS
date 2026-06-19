package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class PcbWindow extends BaseAlgorithmWindow {

    public static class PcbRow {
        private final String pid;
        private String state;
        private String pc;
        private final String memory;

        public PcbRow(String pid, String state, String pc, String memory) {
            this.pid = pid; this.state = state; this.pc = pc; this.memory = memory;
        }
        public String getPid() { return pid; }
        public String getState() { return state; }
        public String getPc() { return pc; }
        public String getMemory() { return memory; }
        
        public void setState(String state) { this.state = state; }
        public void setPc(String pc) { this.pc = pc; }
    }

    private final ObservableList<PcbRow> pcbList = FXCollections.observableArrayList();
    
    // UI Elements for Context Switching Simulation
    private final Label lblCpuStatus = new Label("CPU STATE: IDLE");
    private final ProgressIndicator cpuProgress = new ProgressIndicator();

    public PcbWindow(StackPane parentContainer) {
        super("Core OS Module: Process Control Block (PCB) Registry", parentContainer);

        // --- 1. Top Controls Row for Inputs ---
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtPid = createInputField(inputRow, "Process ID (e.g. P1)", 120);
        
        ComboBox<String> cmbState = new ComboBox<>();
        cmbState.getItems().addAll("READY", "RUNNING", "WAITING", "TERMINATED");
        cmbState.setValue("READY");
        cmbState.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white;");
        inputRow.getChildren().add(cmbState);

        TextField txtPc = createInputField(inputRow, "Program Counter (Hex)", 150);
        TextField txtMem = createInputField(inputRow, "Memory Limit", 120);

        Button btnAllocate = new Button("Allocate PCB");
        btnAllocate.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        inputRow.getChildren().add(btnAllocate);

        // --- 2. Live Context Switch Module Panel ---
        VBox cpuPanel = new VBox(12);
        cpuPanel.setPadding(new Insets(15));
        cpuPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: #3c4043;");
        
        HBox cpuControls = new HBox(15);
        cpuControls.setAlignment(Pos.CENTER_LEFT);
        
        Button btnContextSwitch = new Button("Trigger Context Switch");
        btnContextSwitch.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        cpuProgress.setVisible(false);
        cpuProgress.setPrefSize(18, 18);
        lblCpuStatus.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Consolas'; -fx-font-size: 13px;");
        
        cpuControls.getChildren().addAll(btnContextSwitch, cpuProgress, lblCpuStatus);
        cpuPanel.getChildren().addAll(new Label("OS KERNEL CPU SCHEDULER DISPATCHER:"), cpuControls);

        // --- 3. Table View to hold PCB records ---
        TableView<PcbRow> table = new TableView<>(pcbList);
        table.setStyle("-fx-background-color: #202124;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<PcbRow, String> colPid = new TableColumn<>("PID");
        colPid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        TableColumn<PcbRow, String> colState = new TableColumn<>("Process State");
        colState.setCellValueFactory(new PropertyValueFactory<>("state"));
        TableColumn<PcbRow, String> colPc = new TableColumn<>("PC Register");
        colPc.setCellValueFactory(new PropertyValueFactory<>("pc"));
        TableColumn<PcbRow, String> colMem = new TableColumn<>("Memory Allocation Bounds");
        colMem.setCellValueFactory(new PropertyValueFactory<>("memory"));

        table.getColumns().addAll(colPid, colState, colPc, colMem);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Dummy initial simulation rows
        pcbList.add(new PcbRow("PID_01", "RUNNING", "0x00401A2C", "256MB"));
        pcbList.add(new PcbRow("PID_02", "READY", "0x00401FFF", "512MB"));

        // --- CORE LOGIC EVENT ACTIONS ---
        btnAllocate.setOnAction(e -> {
            if (!txtPid.getText().isEmpty() && !txtPc.getText().isEmpty()) {
                pcbList.add(new PcbRow(txtPid.getText(), cmbState.getValue(), txtPc.getText(), txtMem.getText()));
                txtPid.clear(); txtPc.clear(); txtMem.clear();
            }
        });

        btnContextSwitch.setOnAction(e -> {
            if (pcbList.size() >= 2) {
                simulateContextSwitch();
            } else {
                lblCpuStatus.setText("ERROR: Need at least 2 processes in registry to simulate switch.");
                lblCpuStatus.setStyle("-fx-text-fill: #EA4335;");
            }
        });

        Label sectionLabel = new Label("Active Kernel Process Control Blocks:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(cpuPanel, sectionLabel, inputRow, table);
    }

    private void simulateContextSwitch() {
        cpuProgress.setVisible(true);
        lblCpuStatus.setText("KERNEL: Saving State of current RUNNING process to its PCB...");
        lblCpuStatus.setStyle("-fx-text-fill: #FBBC05;");

        // Find the running process and the ready process
        PcbRow runningProc = null;
        PcbRow readyProc = null;

        for (PcbRow proc : pcbList) {
            if (proc.getState().equals("RUNNING") && runningProc == null) {
                runningProc = proc;
            } else if (proc.getState().equals("READY") && readyProc == null) {
                readyProc = proc;
            }
        }

        // Fallback safely if states are mixed up by user inputs
        if (runningProc == null) runningProc = pcbList.get(0);
        if (readyProc == null) readyProc = pcbList.get(1);

        final PcbRow finalRunning = runningProc;
        final PcbRow finalReady = readyProc;

        // OS Overhead Latency Simulation (1.5 seconds delay for context switches)
        PauseTransition kernelSwitchDelay = new PauseTransition(Duration.seconds(1.5));
        kernelSwitchDelay.setOnFinished(e -> {
            cpuProgress.setVisible(false);

            // Context Switching Logic:
            // 1. Update PC register values to simulate execution progression
            finalRunning.setPc("0x" + Integer.toHexString((int)(Math.random() * 0xFFFFFF)).toUpperCase());
            finalRunning.setState("READY");

            // 2. Load next process state components
            finalReady.setState("RUNNING");

            // Refresh Table structure UI
            tableRefreshHelper();

            lblCpuStatus.setText("SWITCH SUCCESS: " + finalRunning.getPid() + " -> READY | " + finalReady.getPid() + " -> RUNNING");
            lblCpuStatus.setStyle("-fx-text-fill: #34A853;");
        });
        kernelSwitchDelay.play();
    }

    private void tableRefreshHelper() {
        // Forces TableView to update and show modified model object rows updates
        if (!pcbList.isEmpty()) {
            PcbRow temp = pcbList.get(0);
            pcbList.set(0, temp);
        }
    }

    // --- Overriding and Implementing the helper method manually ---
    protected TextField createInputField(HBox targetRow, String promptText, double explicitWidth) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setPrefWidth(explicitWidth);
        field.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white; -fx-prompt-text-fill: #9aa0a6; -fx-background-radius: 4;");
        targetRow.getChildren().add(field);
        return field;
    }
}