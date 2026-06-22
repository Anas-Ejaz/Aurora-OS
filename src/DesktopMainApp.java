import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Screen;
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

        // --- DESKTOP ICONS GRID (top-left, like Windows/Linux desktop icons) ---
        VBox desktopIconsContainer = new VBox(25);
        desktopIconsContainer.setAlignment(Pos.TOP_LEFT);
        desktopIconsContainer.setPadding(new Insets(25));
        StackPane.setAlignment(desktopIconsContainer, Pos.TOP_LEFT);

        // --- BUTTONS (now styled as desktop file/shortcut icons) ---
        Button btnProcessManager = createDesktopFileIcon("Process Manager", "\uD83D\uDDC2"); // NAYA CRUD BUTTON
        Button btnSchedulings = createDesktopFileIcon("Schedulings", "\uD83D\uDCC5");
        Button btnModules = createDesktopFileIcon("Core Modules", "\uD83D\uDCC1");
        Button btnApps = createDesktopFileIcon("System Monitor", "\uD83D\uDCCA");

        // --- POWER OFF as a corner icon button (bottom-right) ---
        Button btnPowerOff = createPowerOffIconButton();
        StackPane.setAlignment(btnPowerOff, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(btnPowerOff, new Insets(0, 20, 20, 0));

        // --- ACTIONS (unchanged) ---
        btnProcessManager.setOnAction(e -> triggerProcessManagerWindow()); // NAYA ACTION
        btnSchedulings.setOnAction(e -> triggerSchedulingsOverlay());
        btnModules.setOnAction(e -> triggerModulesOverlay());
        btnApps.setOnAction(e -> triggerSystemMonitorWindow());
        btnPowerOff.setOnAction(e -> Platform.exit());

        // Icons ko desktop grid mein add karna (Process Manager ko sabse pehle rakha hai)
        desktopIconsContainer.getChildren().addAll(btnProcessManager, btnSchedulings, btnModules, btnApps);

        mainDesktopCanvas.getChildren().addAll(desktopIconsContainer, btnPowerOff);

        // --- FIT TO LAPTOP SCREEN ---
        // Scene ko screen ke visible bounds ke hisaab se size karna, fixed 1100x750 ki jagah
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene mainScene = new Scene(mainDesktopCanvas, screenBounds.getWidth(), screenBounds.getHeight());

        primaryStage.setTitle("OS Simulator Desktop");
        primaryStage.setScene(mainScene);
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
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

    // Desktop file/shortcut-style icon: big emoji/icon on top, full label underneath, transparent background like a real OS icon
    private Button createDesktopFileIcon(String title, String iconGlyph) {
        Label iconLabel = new Label(iconGlyph);
        iconLabel.setStyle("-fx-font-size: 36px;");

        Label textLabel = new Label(title);
        textLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 3, 0.6, 0, 1);");
        textLabel.setWrapText(false);          // naam ko ek hi line mein poora dikhana
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setMaxWidth(Double.MAX_VALUE);

        VBox iconContent = new VBox(4);
        iconContent.setAlignment(Pos.CENTER);
        iconContent.getChildren().addAll(iconLabel, textLabel);

        Button baseButton = new Button();
        baseButton.setGraphic(iconContent);
        baseButton.setPrefSize(150, 90);       // chaurha kiya gaya hai poora naam fit karne ke liye
        baseButton.setMinWidth(150);
        baseButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-background-radius: 6;");

        // Hover effect like a real desktop icon highlight
        baseButton.setOnMouseEntered(e -> baseButton.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.15); -fx-cursor: hand; -fx-background-radius: 6;"));
        baseButton.setOnMouseExited(e -> baseButton.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-background-radius: 6;"));

        return baseButton;
    }

    // Power Off rendered purely as a circular icon button (no text label, no taskbar)
    private Button createPowerOffIconButton() {
        Label powerGlyph = new Label("\u23FB");
        powerGlyph.setStyle("-fx-text-fill: white; -fx-font-size: 22px;");

        Button baseButton = new Button();
        baseButton.setGraphic(powerGlyph);
        baseButton.setPrefSize(48, 48);
        baseButton.setStyle("-fx-background-color: rgba(209, 52, 56, 0.85); -fx-background-radius: 24; -fx-cursor: hand;"
                + "-fx-border-color: rgba(255,255,255,0.25); -fx-border-radius: 24; -fx-border-width: 1;");

        baseButton.setOnMouseEntered(e -> baseButton.setStyle(
                "-fx-background-color: rgba(209, 52, 56, 1.0); -fx-background-radius: 24; -fx-cursor: hand;"
                + "-fx-border-color: rgba(255,255,255,0.4); -fx-border-radius: 24; -fx-border-width: 1;"));
        baseButton.setOnMouseExited(e -> baseButton.setStyle(
                "-fx-background-color: rgba(209, 52, 56, 0.85); -fx-background-radius: 24; -fx-cursor: hand;"
                + "-fx-border-color: rgba(255,255,255,0.25); -fx-border-radius: 24; -fx-border-width: 1;"));

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