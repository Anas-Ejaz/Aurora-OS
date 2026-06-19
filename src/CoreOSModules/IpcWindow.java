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

public class IpcWindow extends BaseAlgorithmWindow {

    public static class IpcMessageRow {
        private final String senderPid, messageString, status;
        public IpcMessageRow(String senderPid, String messageString, String status) {
            this.senderPid = senderPid;
            this.messageString = messageString;
            this.status = status;
        }
        public String getSenderPid() { return senderPid; }
        public String getMessageString() { return messageString; }
        public String getStatus() { return status; }
    }

    private final ObservableList<IpcMessageRow> ipcStreamLog = FXCollections.observableArrayList();
    private final HBox visualPipe = new HBox(15); // Graphical Pipe Area
    private final Label lblBufferStatus = new Label("PIPE STATUS: EMPTY (IDLE)");

    public IpcWindow(StackPane parentContainer) {
        super("Core OS Module: IPC Inter-Process Communication Simulation", parentContainer);

        // --- 1. Input Section (Producer) ---
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtSender = createInputField(inputRow, "Sender PID", 120);
        TextField txtPayload = createInputField(inputRow, "Data Payload", 200);
        
        Button btnSendPipe = new Button("Transmit via Pipe");
        btnSendPipe.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        inputRow.getChildren().add(btnSendPipe);

        // --- 2. Visual Simulation Area (The Pipe) ---
        VBox simulationArea = new VBox(10);
        simulationArea.setPadding(new Insets(15));
        simulationArea.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: #3c4043; -fx-border-width: 1;");
        
        lblBufferStatus.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 12px; -fx-font-family: 'Consolas';");
        
        visualPipe.setPrefHeight(60);
        visualPipe.setAlignment(Pos.CENTER);
        visualPipe.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 8; -fx-border-color: #555; -fx-border-style: dashed;");
        
        Label lblConsumer = new Label("CONSUMER PROCESS (Receiver Active)");
        lblConsumer.setStyle("-fx-text-fill: #34A853; -fx-font-weight: bold;");

        simulationArea.getChildren().addAll(new Label("SHARED MEMORY PIPE (BUFFER):"), visualPipe, lblBufferStatus, lblConsumer);

        // --- 3. Logs Section ---
        TableView<IpcMessageRow> table = new TableView<>(ipcStreamLog);
        table.setStyle("-fx-background-color: #202124;");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<IpcMessageRow, String> colSender = new TableColumn<>("Producer (Sender)");
        colSender.setCellValueFactory(new PropertyValueFactory<>("senderPid"));
        TableColumn<IpcMessageRow, String> colPayload = new TableColumn<>("Message Content");
        colPayload.setCellValueFactory(new PropertyValueFactory<>("messageString"));
        TableColumn<IpcMessageRow, String> colStatus = new TableColumn<>("Transmission State");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colSender, colPayload, colStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- SIMULATION LOGIC ---
        btnSendPipe.setOnAction(e -> {
            String pid = txtSender.getText();
            String msg = txtPayload.getText();

            if (!pid.isEmpty() && !msg.isEmpty()) {
                startIpcSimulation(pid, msg);
                txtSender.clear();
                txtPayload.clear();
            }
        });

        Label sectionLabel = new Label("Kernel IPC Pipeline History:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(inputRow, simulationArea, sectionLabel, table);
    }

    private void startIpcSimulation(String pid, String msg) {
        // Step 1: Producer puts data into the Pipe
        Label dataBlock = new Label(msg);
        dataBlock.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-font-weight: bold;");
        
        visualPipe.getChildren().add(dataBlock);
        lblBufferStatus.setText("PIPE STATUS: [WRITING] Data from " + pid + " is in Shared Buffer...");
        lblBufferStatus.setStyle("-fx-text-fill: #FBBC05;");

        // Step 2: Simulation Delay (OS context switching / transmission time)
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            // Step 3: Consumer receives the data
            visualPipe.getChildren().remove(dataBlock);
            ipcStreamLog.add(0, new IpcMessageRow(pid, msg, "SUCCESSFULLY DELIVERED"));
            
            lblBufferStatus.setText("PIPE STATUS: [READ] Consumer processed data. Buffer EMPTY.");
            lblBufferStatus.setStyle("-fx-text-fill: #34A853;");
        });
        delay.play();
    }
}