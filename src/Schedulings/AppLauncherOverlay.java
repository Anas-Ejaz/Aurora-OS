package Schedulings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AppLauncherOverlay extends VBox {

    public AppLauncherOverlay(StackPane rootStack) {
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(50));
        this.setStyle("-fx-background-color: rgba(30, 30, 30, 0.85); -fx-backdrop-filter: blur(25px);");

        Label headerTitle = new Label("CPU Schedulings Folder");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        headerTitle.setTextFill(Color.WHITE);
        headerTitle.setPadding(new Insets(0, 0, 30, 0));

        GridPane appsGrid = new GridPane();
        appsGrid.setHgap(25);
        appsGrid.setVgap(25);
        appsGrid.setAlignment(Pos.CENTER);

        String[] schedulers = {"SJF (Preemptive)", "SRTF (Non-Preemptive)", "FCFS", "Round Robin", "Priority"};

        int column = 0, row = 0;
        for (String type : schedulers) {
            Button appButton = new Button(type);
            appButton.setPrefSize(200, 110);
            appButton.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
            appButton.setTextFill(Color.WHITE);
            appButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 8; -fx-border-color: rgba(255, 255, 255, 0.15); -fx-border-radius: 8; -fx-cursor: hand;");

            // Handling polymorphism on click
            appButton.setOnAction(e -> {
                rootStack.getChildren().remove(this);
                BaseAlgorithmWindow window;
                
                // Update the switch statement inside AppLauncherOverlay's event listener block:
                switch (type) {
                    case "FCFS":
                        window = new FCFSWindow(rootStack);
                        break;
                    case "SJF (Preemptive)":
                        window = new SJFWindow(rootStack);
                        break;
                    case "SRTF (Non-Preemptive)":
                        window = new SRTFWindow(rootStack);
                        break;
                    case "Round Robin":
                        window = new RoundRobinWindow(rootStack);
                        break;
                    case "Priority":
                        window = new PriorityWindow(rootStack);
                        break;
                    default:
                        window = new FCFSWindow(rootStack);
                        break;
                }
                rootStack.getChildren().add(window);
            });

            appsGrid.add(appButton, column, row);
            column++;
            if (column > 2) { column = 0; row++; }
        }

        Button closeMenuBtn = new Button("Back to Desktop");
        closeMenuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: lightgray; -fx-cursor: hand; -fx-underline: true;");
        closeMenuBtn.setPadding(new Insets(30, 0, 0, 0));
        closeMenuBtn.setOnAction(e -> rootStack.getChildren().remove(this));

        this.getChildren().addAll(headerTitle, appsGrid, closeMenuBtn);
    }
}