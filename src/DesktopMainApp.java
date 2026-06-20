import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import SystemMonitor.SystemMonitorWindow;
import CoreOSModules.ModulesOverlay;
import Schedulings.AppLauncherOverlay;
import Process.ProcessManagerWindow; // NAYA IMPORT

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

        // --- BUTTONS ---
        Button btnProcessManager = createDesktopSystemButton("Process Manager", "#107c41"); // NAYA CRUD BUTTON (Green Accent)
        Button btnSchedulings = createDesktopSystemButton("Schedulings", "#0078d4");
        Button btnModules = createDesktopSystemButton("Core Modules", "#0078d4"); 
        Button btnApps = createDesktopSystemButton("System Monitor", "#0078d4");
        Button btnPowerOff = createDesktopSystemButton("Power Off", "#d13438");

        // --- ACTIONS ---
        btnProcessManager.setOnAction(e -> triggerProcessManagerWindow()); // NAYA ACTION
        btnSchedulings.setOnAction(e -> triggerSchedulingsOverlay());
        btnModules.setOnAction(e -> triggerModulesOverlay()); 
        btnApps.setOnAction(e -> triggerSystemMonitorWindow());
        btnPowerOff.setOnAction(e -> Platform.exit());

        // Buttons ko taskbar mein add karna (Process Manager ko sabse pehle rakha hai)
        systemTaskbar.getChildren().addAll(btnProcessManager, btnSchedulings, btnModules, btnApps, btnPowerOff);
        mainDesktopCanvas.getChildren().add(desktopLayoutContainer);

        Scene mainScene = new Scene(mainDesktopCanvas, 1100, 750);
        primaryStage.setTitle("OS Simulator Desktop");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    // NAYA FUNCTION: Process Manager CRUD Window kholne ke liye
    private void triggerProcessManagerWindow() {
        ProcessManagerWindow managerWindow = new ProcessManagerWindow(mainDesktopCanvas);
        if(mainDesktopCanvas.getChildren().stream().noneMatch(node -> node instanceof ProcessManagerWindow)) {
            mainDesktopCanvas.getChildren().add(managerWindow);
        }
    }

    private void triggerModulesOverlay() {
        ModulesOverlay modulesMenu = new ModulesOverlay(mainDesktopCanvas);
        if(mainDesktopCanvas.getChildren().stream().noneMatch(node -> node instanceof ModulesOverlay)) {
            mainDesktopCanvas.getChildren().add(modulesMenu);
        }
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

    private void triggerSystemMonitorWindow() {
        SystemMonitorWindow monitorWindow = new SystemMonitorWindow(mainDesktopCanvas);
        if(mainDesktopCanvas.getChildren().stream().noneMatch(node -> node instanceof SystemMonitorWindow)) {
            mainDesktopCanvas.getChildren().add(monitorWindow);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}