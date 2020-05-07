package javafxui;


import java.nio.IntBuffer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

public class RulerController {
	
	private int[] numArray;
	
	ArtGelController artGelController;
	
	@FXML
	GridPane grid;

	@FXML
    public void initialize() {
    }
	
	protected void handleSaveButtonAction(ActionEvent event) {
		System.out.println("funciono");
    }
    
    
	public void initData(int[] ruler, ArtGelController artGelController) {	
		this.artGelController = artGelController;
		numArray = ruler;
		for (int i = 0; i < ruler.length; i++) {
			Label label = new Label((i+1)+": ");
			TextField text = new TextField(""+(i+1));
			grid.add(label, 0, i, 1, 1);
			grid.add(text, 1, i, 1, 1);
		}
		Button button1 = new Button("Save");
		button1.setOnAction(new EventHandler<ActionEvent>() {

	        @Override
	        public void handle(ActionEvent event) {
	        	ObservableList<Node> childrens = grid.getChildren();
	        	int i = 0;
	        	for (Node node : childrens) {
	                if(i<numArray.length && grid.getRowIndex(node) == i && grid.getColumnIndex(node) == 1) {
	                	TextField tf = (TextField)node;
	                    numArray[i++] = Integer.parseInt(tf.getText());
	                    artGelController.changeRuler(numArray);
	                }
	            }
	        }
	    });
	       
		grid.add(button1, 1, ruler.length, 1, 1);
	}
}
