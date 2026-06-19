package Schedulings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public abstract class BaseAlgorithmWindow extends StackPane {

    protected final VBox windowContent;
    protected final VBox workspace;
    private boolean isMaximized = false;
    private final StackPane parentContainer;

    public BaseAlgorithmWindow(String windowTitle, StackPane parentContainer) {
        this.parentContainer = parentContainer;
        this.setMaxSize(850, 600);

        // Core Window Shell
        windowContent = new VBox();
        windowContent.setStyle(
            "-fx-background-color: #202124;" + 
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;" +
            "-fx-border-color: #3c4043;" +
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 5);"
        );

        // Title Bar
        HBox titleBar = new HBox(8);
        titleBar.setAlignment(Pos.CENTER_LEFT); // Keeps text left-aligned
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.setStyle("-fx-background-color: #2d2e31; -fx-background-radius: 10 10 0 0;");

        // Window Title Label
        Label titleLabel = new Label(windowTitle);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        titleLabel.setTextFill(Color.WHITE);

        // Spacer pushes everything after it (the buttons) all the way to the right side
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // System Buttons (Placed on the right)
        Button maxBtn = createCircleControl("#34A853");
        Button closeBtn = createCircleControl("#EA4335");

        closeBtn.setOnAction(e -> parentContainer.getChildren().remove(this));
        maxBtn.setOnAction(e -> toggleMaximize());

        // Reordered children layout: Title -> Spacer -> Max Button -> Close Button
        titleBar.getChildren().addAll(titleLabel, spacer, maxBtn, closeBtn);

        // Workspace for children to populate
        workspace = new VBox(20);
        workspace.setPadding(new Insets(20));
        VBox.setVgrow(workspace, Priority.ALWAYS);

        windowContent.getChildren().addAll(titleBar, workspace);
        this.getChildren().add(windowContent);
    }

    private Button createCircleControl(String colorHex) {
        Button btn = new Button();
        btn.setPrefSize(13, 13);
        btn.setMinSize(13, 13);
        btn.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 50%; -fx-cursor: hand;");
        return btn;
    }

    private void toggleMaximize() {
        if (!isMaximized) {
            this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(windowContent, Priority.ALWAYS);
            isMaximized = true;
        } else {
            this.setMaxSize(850, 600);
            isMaximized = false;
        }
    }
    
    protected javafx.scene.control.TextField createInputField(HBox targetRow, String promptText, double explicitWidth) {
        javafx.scene.control.TextField field = new javafx.scene.control.TextField();
        field.setPromptText(promptText);
        field.setPrefWidth(explicitWidth);
        field.setStyle("-fx-background-color: #3c4043; -fx-text-fill: white; -fx-prompt-text-fill: #9aa0a6; -fx-background-radius: 4;");
        targetRow.getChildren().add(field);
        return field;
    }

    protected void addGanttBlock(HBox chart, String pId, String duration, String colorHex) {
        VBox block = new VBox(2);
        block.setAlignment(javafx.geometry.Pos.CENTER);
        block.setPrefWidth(100);
        block.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 4;");

        Label idLabel = new Label(pId);
        idLabel.setTextFill(Color.WHITE);
        idLabel.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 12));

        Label timeLabel = new Label(duration);
        timeLabel.setTextFill(Color.WHITE);
        timeLabel.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.NORMAL, 10));

        block.getChildren().addAll(idLabel, timeLabel);
        chart.getChildren().add(block);
    }
}