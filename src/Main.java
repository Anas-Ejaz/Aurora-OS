import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        Label text = new Label("Hello JavaFX");

        StackPane root = new StackPane();
        root.getChildren().add(text);

        Scene scene = new Scene(root, 400, 300);

        stage.setTitle("JavaFX App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}