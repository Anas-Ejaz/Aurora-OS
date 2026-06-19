package CoreOSModules;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class ModulesOverlay extends StackPane {

    private final StackPane parentCanvas;

    public ModulesOverlay(StackPane parentCanvas) {
        this.parentCanvas = parentCanvas;
        
        // Full screen background blur dark tint
        this.setStyle("-fx-background-color: rgba(12, 12, 14, 0.75);");
        
        // Main Container (The Glass Folder Panel)
        VBox folderContent = new VBox(35);
        folderContent.setAlignment(Pos.CENTER);
        folderContent.setMaxSize(850, 520);
        folderContent.setPadding(new Insets(40));
        
        // Glassmorphism design for the folder panel
        folderContent.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.06);" + // High transparency white fill
            "-fx-background-radius: 24;" +
            "-fx-border-radius: 24;" +
            "-fx-border-color: rgba(255, 255, 255, 0.15);" +    // Frosty edge reflection
            "-fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 30, 0, 0, 10);"
        );

        Label title = new Label("Core OS Simulation Modules");
        title.setStyle("-fx-text-fill: #F3F4F6; -fx-font-family: 'Segoe UI'; -fx-font-size: 26px; -fx-font-weight: bold; -fx-letter-spacing: 0.5;");

        // Grid layout for Module Glass Tiles
        GridPane tilesGrid = new GridPane();
        tilesGrid.setHgap(30);
        tilesGrid.setVgap(30);
        tilesGrid.setAlignment(Pos.CENTER);

        // Creating Glass Tiles with precise colored accent glow indicators instead of solid fills
        tilesGrid.add(createGlassModuleTile("PCB Monitor", "#4285F4"), 0, 0);
        tilesGrid.add(createGlassModuleTile("Paging Engine", "#34A853"), 1, 0);
        tilesGrid.add(createGlassModuleTile("Segmentation", "#FBBC05"), 2, 0);
        tilesGrid.add(createGlassModuleTile("Mem Management", "#9C27B0"), 0, 1);
        tilesGrid.add(createGlassModuleTile("IPC Stream", "#FF5722"), 1, 1);
        tilesGrid.add(createGlassModuleTile("Synchronization", "#22c0ff"), 2, 1);

        // Glass Styled Close Button
        Button btnClose = new Button("Back to Desktop");
        btnClose.setPrefSize(220, 44);
        btnClose.setStyle(
            "-fx-background-color: rgba(209, 52, 56, 0.2);" +
            "-fx-text-fill: #FFA4A6;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: rgba(209, 52, 56, 0.4);" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        
        // Hover effects for Close Button
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: rgba(209, 52, 56, 0.4); -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(209, 52, 56, 0.6); -fx-border-width: 1; -fx-cursor: hand;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: rgba(209, 52, 56, 0.2); -fx-text-fill: #FFA4A6; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(209, 52, 56, 0.4); -fx-border-width: 1; -fx-cursor: hand;"));
        btnClose.setOnAction(e -> parentCanvas.getChildren().remove(this));

        folderContent.getChildren().addAll(title, tilesGrid, btnClose);
        this.getChildren().add(folderContent);
    }

    private Button createGlassModuleTile(String name, String accentColor) {
        Button tile = new Button(name);
        tile.setPrefSize(190, 110);
        
        // Base Glassmorphism Tile Style (Translucent White + Subtle Accent Left Border Indicator)
        String baseStyle = 
            "-fx-background-color: rgba(255, 255, 255, 0.04);" +
            "-fx-text-fill: #E5E7EB;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 16;" +
            "-fx-border-radius: 16;" +
            "-fx-border-color: rgba(255, 255, 255, 0.1);" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;";
            
        tile.setStyle(baseStyle);

        // Smooth Mouse Interactive States
        tile.setOnMouseEntered(e -> {
            tile.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.12);" + // Lights up slightly
                "-fx-text-fill: white;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 16;" +
                "-fx-border-radius: 16;" +
                "-fx-border-color: " + accentColor + ";" +           // Border glows with the specific feature color!
                "-fx-border-width: 1.5;" +
                "-fx-cursor: hand;"
            );
        });

        tile.setOnMouseExited(e -> tile.setStyle(baseStyle));

        tile.setOnAction(e -> {
           parentCanvas.getChildren().remove(this);

            switch (name) {
                case "PCB Monitor":
                    parentCanvas.getChildren().add(new PcbWindow(parentCanvas));
                    break;
                case "Paging Engine":
                    parentCanvas.getChildren().add(new PagingWindow(parentCanvas));
                    break;
                case "Segmentation":
                    parentCanvas.getChildren().add(new SegmentationWindow(parentCanvas)); // LINKED!
                    break;
                case "Mem Management":
                    parentCanvas.getChildren().add(new MemoryManagementWindow(parentCanvas)); // LINKED!
                    break;
                case "IPC Stream":
                    parentCanvas.getChildren().add(new IpcWindow(parentCanvas)); // LINKED!
                    break;
                case "Synchronization":
                    parentCanvas.getChildren().add(new SynchronizationWindow(parentCanvas)); // LINKED!
                    break;
            }
        });

        return tile;
    }
}