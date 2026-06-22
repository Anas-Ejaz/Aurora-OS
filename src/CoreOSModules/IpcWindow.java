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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
    private final HBox visualPipe = new HBox(15); 
    private final Label lblBufferStatus = new Label("PIPE BUFFER REGISTRY: EMPTY (IDLE STATE)");
    private final TextArea txtSimLog = new TextArea(); // Kernel Simulation Terminal Logs

    public IpcWindow(StackPane parentContainer) {
        super("Core OS Module: IPC Inter-Process Communication Simulation", parentContainer);

        // --- 1. Input Panel (IPC Data Producer Setup) ---
        HBox inputRow = new HBox(12);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        TextField txtSender = createInputField(inputRow, "Sender PID (e.g. P1)", 130);
        TextField txtPayload = createInputField(inputRow, "Message / Data Payload", 220);
        
        Button btnSendPipe = new Button("Transmit via Pipe");
        btnSendPipe.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        inputRow.getChildren().add(btnSendPipe);

        // --- 2. Enhanced Visual Layout Framework ---
        VBox simulationArea = new VBox(12);
        simulationArea.setPadding(new Insets(15));
        simulationArea.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10; -fx-border-color: #3c4043; -fx-border-width: 1;");
        
        lblBufferStatus.setStyle("-fx-text-fill: #FBBC05; -fx-font-size: 12px; -fx-font-family: 'Consolas';");
        
        // Horizontal architecture map showing Sender -> Pipe Bus -> Receiver
        HBox pipeArchitectureRow = new HBox(10);
        pipeArchitectureRow.setAlignment(Pos.CENTER);
        
        Label lblProducerNode = new Label("PRODUCER\n(Sender Context)");
        lblProducerNode.setAlignment(Pos.CENTER);
        lblProducerNode.setStyle("-fx-background-color: #3c4043; -fx-text-fill: #8ab4f8; -fx-padding: 10; -fx-background-radius: 5; -fx-font-weight: bold; -fx-text-alignment: center; -fx-font-size: 11px;");
        lblProducerNode.setPrefSize(120, 55);

        visualPipe.setPrefHeight(55);
        visualPipe.setAlignment(Pos.CENTER);
        visualPipe.setStyle("-fx-background-color: #171717; -fx-background-radius: 6; -fx-border-color: #FF5722; -fx-border-style: dashed; -fx-border-width: 1.5;");
        HBox.setHgrow(visualPipe, Priority.ALWAYS); // Pipe fills up center layout automatically
        
        Label lblConsumerNode = new Label("CONSUMER\n(Receiver Active)");
        lblConsumerNode.setAlignment(Pos.CENTER);
        lblConsumerNode.setStyle("-fx-background-color: #3c4043; -fx-text-fill: #34A853; -fx-padding: 10; -fx-background-radius: 5; -fx-font-weight: bold; -fx-text-alignment: center; -fx-font-size: 11px;");
        lblConsumerNode.setPrefSize(120, 55);
        
        pipeArchitectureRow.getChildren().addAll(lblProducerNode, visualPipe, lblConsumerNode);
        simulationArea.getChildren().addAll(new Label("SHARED KERNEL MEMORY DATA PIPELINE BUS:"), pipeArchitectureRow, lblBufferStatus);

        // --- 3. Split Screen: Left Side (Pipeline Matrix Table) & Right Side (Simulation Terminal Log) ---
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(15);
        VBox.setVgrow(mainGrid, Priority.ALWAYS);

        ColumnConstraints colTable = new ColumnConstraints();
        colTable.setPercentWidth(55); // 55% space for history tracking table
        ColumnConstraints colLogConsole = new ColumnConstraints();
        colLogConsole.setPercentWidth(45); // 45% space for pure terminal outputs
        mainGrid.getColumnConstraints().addAll(colTable, colLogConsole);

        // Left Side History Map Table
        TableView<IpcMessageRow> table = new TableView<>(ipcStreamLog);
        table.setStyle("-fx-background-color: #202124; -fx-border-color: #3c4043; -fx-border-radius: 4;");
        GridPane.setVgrow(table, Priority.ALWAYS);

        TableColumn<IpcMessageRow, String> colSender = new TableColumn<>("Producer (Source)");
        colSender.setCellValueFactory(new PropertyValueFactory<>("senderPid"));
        TableColumn<IpcMessageRow, String> colPayload = new TableColumn<>("Payload Content");
        colPayload.setCellValueFactory(new PropertyValueFactory<>("messageString"));
        TableColumn<IpcMessageRow, String> colStatus = new TableColumn<>("Kernel State");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(colSender, colPayload, colStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        mainGrid.add(table, 0, 0);

        // Right Side Real-time Console Setup
        VBox logBox = new VBox(5);
        Label lblLogTitle = new Label("Kernel IPC Core Terminal logs:");
        lblLogTitle.setStyle("-fx-text-fill: #8ab4f8; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        txtSimLog.setEditable(false);
        txtSimLog.setWrapText(true);
        txtSimLog.setStyle("-fx-control-inner-background: #121212; -fx-text-fill: #0F9D58; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");
        VBox.setVgrow(txtSimLog, Priority.ALWAYS);
        logBox.getChildren().addAll(lblLogTitle, txtSimLog);
        mainGrid.add(logBox, 1, 0);

        // --- System Initial Wake Log ---
        logWrite("System Boot: Inter-Process Communication Channel established. Pipe Buffer Ready.");

        // --- SIMULATION RUN INTERACTION ---
        btnSendPipe.setOnAction(e -> {
            String pid = txtSender.getText().trim();
            String msg = txtPayload.getText().trim();

            if (!pid.isEmpty() && !msg.isEmpty()) {
                startIpcSimulation(pid, msg);
                txtSender.clear();
                txtPayload.clear();
            }
        });

        Label sectionLabel = new Label("Kernel IPC Pipeline Transmission Control Registry:");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(inputRow, simulationArea, sectionLabel, mainGrid);
    }

    private void startIpcSimulation(String pid, String msg) {
        // Step 1: Producer injects payload context into Shared Bus channel
        Label dataBlock = new Label("[" + pid + "] : " + msg);
        dataBlock.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-family: 'Consolas'; -fx-font-size: 11px;");
        
        visualPipe.getChildren().add(dataBlock);
        lblBufferStatus.setText("PIPE STATUS: [BUSY / WRITING] Packet data injected from " + pid + ". Routing stream context...");
        lblBufferStatus.setStyle("-fx-text-fill: #FBBC05;");
        
        logWrite("IPC Core Gate: Process Context [" + pid + "] acquired mutex lock. Writing payload data: \"" + msg + "\" into bounded pipe buffer memory.");

        // Step 2: Context switching delay block simulation
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            // Step 3: Receiver/Consumer intercepts and consumes data node
            visualPipe.getChildren().remove(dataBlock);
            ipcStreamLog.add(0, new IpcMessageRow(pid, msg, "DELIVERED SUCCESSFULLY"));
            
            lblBufferStatus.setText("PIPE STATUS: [CLEAN / READ] Consumer intercepted context. Shared Pipe reclaimed.");
            lblBufferStatus.setStyle("-fx-text-fill: #34A853;");
            
            logWrite("IPC Core Dispatcher: Consumer process triggered read interrupt vector. Extracted payload from buffer. Memory address cleared for next I/O vector thread.");
        });
        delay.play();
    }

    // --- Helper Simulation Logger Engine Logic ---
    private void logWrite(String logMessage) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        txtSimLog.appendText("[" + timestamp + "] " + logMessage + "\n");
    }

    // --- Helper UI Generator Layout Method ---
    protected TextField createInputField(HBox targetRow, String promptText, double explicitWidth) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setPrefWidth(explicitWidth);
        field.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white; -fx-prompt-text-fill: #9aa0a6; -fx-background-radius: 4;");
        targetRow.getChildren().add(field);
        return field;
    }
}