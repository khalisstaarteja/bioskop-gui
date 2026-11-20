package main.java.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        java.util.logging.Logger.getLogger("javafx.css").setLevel(java.util.logging.Level.SEVERE);
        
        Parent root = FXMLLoader.load(new java.io.File("src/main/resources/login_view.fxml").toURI().toURL());
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Login Sistem Bioskop");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
