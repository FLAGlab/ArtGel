package javafxui;


import java.nio.IntBuffer;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Geles.IntensityProcessor;
import Geles.Well;
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

public class WellsController {
	
	private IntensityProcessor processor;
	
	ArtGelController artGelController;
	
	@FXML
	GridPane grid;

	@FXML
    public void initialize() {
    }
    
    
	public void initData(IntensityProcessor processor, ArtGelController artGelController) {	
		this.artGelController = artGelController;
		this.processor = processor;
		List<Well> wells = processor.getWells();
		for (int i = 0; i < wells.size(); i++) {
			Label label = new Label((i+1)+": ");
			TextField text = new TextField(wells.get(i).getSampleId());
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
	                if(i<wells.size() && grid.getRowIndex(node) == i && grid.getColumnIndex(node) == 1) {
	                	TextField tf = (TextField)node;
	                    wells.get(i).setSampleId(tf.getText());
	                    artGelController.changeWells();
	                }
	            }
	        }
	    });
	       
		grid.add(button1, 1, wells.size(), 1, 1);
	}
}
