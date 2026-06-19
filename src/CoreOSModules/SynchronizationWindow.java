package CoreOSModules;

import Schedulings.BaseAlgorithmWindow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SynchronizationWindow extends BaseAlgorithmWindow {

    private int bufferCount = 0;
    private final int BUFFER_CAPACITY = 5;
    private boolean isMutexLocked = false;

    // UI Updates
    private final Label lblBufferVisual = new Label("[ _ ] [ _ ] [ _ ] [ _ ] [ _ ]");
    private final Label lblMutexState = new Label("MUTEX STATUS: 1 (UNLOCKED)");
    private final Label lblCountingSemaphore = new Label("EMPTY SLOTS: 5  |  FULL SLOTS: 0");
    private final TextArea txtTerminalLog = new TextArea();

    public SynchronizationWindow(StackPane parentContainer) {
        super("Core OS Module: Process Synchronization (Mutex & Semaphores)", parentContainer);

        // --- 1. Top Controls Panel ---
        HBox controlRow = new HBox(20);
        controlRow.setAlignment(Pos.CENTER);

        Button btnProduce = new Button("Execute Producer()");
        btnProduce.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 20;");

        Button btnConsume = new Button("Execute Consumer()");
        btnConsume.setStyle("-fx-background-color: #EA4335; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 20;");

        controlRow.getChildren().addAll(btnProduce, btnConsume);

        // --- 2. Live Semaphore Monitor (Glass Look Panel) ---
        VBox monitorPanel = new VBox(15);
        monitorPanel.setPadding(new Insets(20));
        monitorPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: #3c4043;");
        monitorPanel.setAlignment(Pos.CENTER);

        lblMutexState.setStyle("-fx-text-fill: #34A853; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-weight: bold;");
        lblCountingSemaphore.setStyle("-fx-text-fill: #FBBC05; -fx-font-family: 'Consolas'; -fx-font-size: 14px;");
        
        lblBufferVisual.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Consolas'; -fx-font-size: 22px; -fx-font-weight: bold; -fx-letter-spacing: 2;");

        monitorPanel.getChildren().addAll(
            new Label("CRITICAL SECTION & SHARED BOUNDED BUFFER:"),
            lblBufferVisual,
            new Separator(),
            lblMutexState,
            lblCountingSemaphore
        );

        // --- 3. Terminal Execution Logger ---
        txtTerminalLog.setEditable(false);
        txtTerminalLog.setPrefHeight(200);
        txtTerminalLog.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: #A7F3D0; -fx-font-family: 'Consolas';");
        logMessage("Kernel Semaphore Initialized. Mutex=1, Empty=5, Full=0.");

        // --- BUTTON ACTIONS (The Algorithm Simulation) ---
        btnProduce.setOnAction(e -> handleProducer());
        btnConsume.setOnAction(e -> handleConsumer());

        Label sectionLabel = new Label("Race Condition Prevention Logs (Terminal View):");
        sectionLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold;");

        workspace.getChildren().addAll(controlRow, monitorPanel, sectionLabel, txtTerminalLog);
    }

    private void handleProducer() {
        logMessage("\n[THREAD]: Producer calling wait(empty)...");
        if (bufferCount >= BUFFER_CAPACITY) {
            logMessage("[CRITICAL ERROR]: Buffer Full! wait(empty) blocks Producer Thread.");
            return;
        }

        logMessage("[LOCK]: wait(mutex) executed. Mutex locked.");
        isMutexLocked = true;
        updateUIState("MUTEX LOCKED (0)", "#EA4335");

        // Simulate item entry inside critical section
        bufferCount++;
        logMessage("[CRITICAL SECTION]: Producer added item to buffer slot " + bufferCount);

        logMessage("[UNLOCK]: signal(mutex) executed. Mutex unlocked.");
        logMessage("[SIGNAL]: signal(full) incremented.");
        isMutexLocked = false;
        
        updateBufferGraphic();
    }

    private void handleConsumer() {
        logMessage("\n[THREAD]: Consumer calling wait(full)...");
        if (bufferCount <= 0) {
            logMessage("[CRITICAL ERROR]: Buffer Empty! wait(full) blocks Consumer Thread.");
            return;
        }

        logMessage("[LOCK]: wait(mutex) executed. Mutex locked.");
        isMutexLocked = true;
        updateUIState("MUTEX LOCKED (0)", "#EA4335");

        // Simulate item removal inside critical section
        logMessage("[CRITICAL SECTION]: Consumer removed item from buffer slot " + bufferCount);
        bufferCount--;

        logMessage("[UNLOCK]: signal(mutex) executed. Mutex unlocked.");
        logMessage("[SIGNAL]: signal(empty) incremented.");
        isMutexLocked = false;

        updateBufferGraphic();
    }

    private void updateBufferGraphic() {
        StringBuilder visual = new StringBuilder();
        for (int i = 0; i < BUFFER_CAPACITY; i++) {
            if (i < bufferCount) visual.append("[■] "); // Occupied slot
            else visual.append("[_] ");                 // Free slot
        }
        lblBufferVisual.setText(visual.toString().trim());
        
        lblCountingSemaphore.setText("EMPTY SLOTS: " + (BUFFER_CAPACITY - bufferCount) + "  |  FULL SLOTS: " + bufferCount);
        updateUIState("1 (UNLOCKED)", "#34A853");
    }

    private void updateUIState(String text, String colorHex) {
        lblMutexState.setText("MUTEX STATUS: " + text);
        lblMutexState.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-font-weight: bold;");
    }

    private void logMessage(String msg) {
        txtTerminalLog.appendText(msg + "\n");
    }
}