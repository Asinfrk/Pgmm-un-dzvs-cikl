package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;



public class Main extends Application {
   static  Stage globalStage;
	@Override
    public void start(Stage primaryStage) {
        try {
        	globalStage = primaryStage;
            Parent root = FXMLLoader.load(getClass().getResource("ProjMain.fxml"));
            Scene scene = new Scene(root, 700, 480); 
            globalStage.setScene(scene);
            globalStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}