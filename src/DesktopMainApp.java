import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class DesktopMainApp extends Application {

    private StackPane mainDesktopCanvas;

    @Override
    public void start(Stage primaryStage) {
        String wallpaperUrl = getClass().getResource("/assets/wallppr.jpg").toExternalForm();

        mainDesktopCanvas = new StackPane();
        mainDesktopCanvas.setStyle("-fx-background-image: url('" + wallpaperUrl + "'); -fx-background-size: cover; -fx-background-position: center;");
        
        HBox systemTaskbar = new HBox(20);
        systemTaskbar.setAlignment(Pos.CENTER);
        systemTaskbar.setPadding(new Insets(15, 25, 15, 25));
        systemTaskbar.setStyle("-fx-background-color: rgba(20, 20, 20, 0.85); -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 1 0 0 0;");
        
        VBox desktopLayoutContainer = new VBox();
        desktopLayoutContainer.setAlignment(Pos.BOTTOM_CENTER);
        desktopLayoutContainer.getChildren().add(systemTaskbar);

        // Updated button names as per your request
        Button btnSchedulings = createDesktopSystemButton("Schedulings", "#0078d4");
        Button btnApps = createDesktopSystemButton("Applications", "#0078d4");
        Button btnPowerOff = createDesktopSystemButton("Power Off", "#d13438");

        btnSchedulings.setOnAction(e -> triggerSchedulingsOverlay());
        btnPowerOff.setOnAction(e -> Platform.exit());

        systemTaskbar.getChildren().addAll(btnSchedulings,btnApps, btnPowerOff);
        mainDesktopCanvas.getChildren().add(desktopLayoutContainer);

        Scene mainScene = new Scene(mainDesktopCanvas, 1100, 750);
        primaryStage.setTitle("Desktop Simulator Environment");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private Button createDesktopSystemButton(String title, String accentColor) {
        Button baseButton = new Button(title);
        baseButton.setPrefSize(150, 40);
        baseButton.setStyle("-fx-background-color: " + accentColor + "; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        return baseButton;
    }

    private void triggerSchedulingsOverlay() {
        AppLauncherOverlay overlayMenu = new AppLauncherOverlay(mainDesktopCanvas);
        if(mainDesktopCanvas.getChildren().stream().noneMatch(node -> node instanceof AppLauncherOverlay)) {
            mainDesktopCanvas.getChildren().add(overlayMenu);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
