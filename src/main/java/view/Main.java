package main.java.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/main/resources/fonts/BricolageGrotesque-Regular.ttf"), 14);
        javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/main/resources/fonts/BricolageGrotesque-Bold.ttf"), 14);

        Parent root = FXMLLoader.load(getClass().getResource("/main/resources/login_view.fxml"));
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Login Sistem Bioskop");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        com.sun.javafx.util.Logging.getJavaFXLogger().disableLogging();
        launch(args);
    }
}
