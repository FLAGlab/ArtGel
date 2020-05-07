package javafxui;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class ArtGelInterface extends Application {

    @Override
    public void start(Stage stage) {
		try {
		URL url = new File("src/javafxui/artgel_structure.fxml").toURI().toURL();
		Parent root = FXMLLoader.load(url);
        stage.setTitle("ArtGel");
        stage.setScene(new Scene(root, 1300, 700));
        stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void main(String[] args) {
        launch(args);
    }

}