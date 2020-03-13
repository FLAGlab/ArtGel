package javafxui;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import Geles.Band;
import Geles.IntensityProcessor;
import Geles.Well;
import javafx.stage.FileChooser;


public class ArtGelController {

    @FXML
    private Button loadButtom;
    
    @FXML
    private ImageView imageContainer;
    
    @FXML
    private StackPane imagePanel;
    
    private WritableImage image;
    
    private Image originalImage;
    
    final FileChooser fileChooser = new FileChooser();
	final IntensityProcessor processor = new IntensityProcessor();
	

    @FXML
    protected void handleLoadButtonAction(ActionEvent event) {
        Window owner = loadButtom.getScene().getWindow();
        File file = fileChooser.showOpenDialog(owner);
        originalImage = new Image(file.toURI().toString());
		int sourceWidth = (int) originalImage.getWidth();
		int sourceHeight = (int) originalImage.getHeight();
		PixelReader pixelReader = originalImage.getPixelReader();
		image = new WritableImage(sourceWidth,sourceHeight);
		PixelWriter pixelWriter = image.getPixelWriter();
		WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();

		int[] buffer = new int[sourceWidth * sourceHeight];
		pixelReader.getPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);
		pixelWriter.setPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);        try {
			processor.loadImage(file.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        imageContainer.setImage(image);
    }
    
    @FXML
    protected void handleCalculateButtonAction(ActionEvent event) {
		try {
			processor.processImage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		BufferedImage modifiedImage = processor.getModifiedImage();
		SwingFXUtils.toFXImage(modifiedImage, image);
		
//		PixelReader pixelReader = originalImage.getPixelReader();
//		
//		PixelWriter pixelWriter = image.getPixelWriter();
//		WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();
//
//		int[] buffer = new int[sourceWidth * sourceHeight];
//		
//		pixelReader.getPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);
//		pixelWriter.setPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);
//		(alpha << 24) | (red << 16 ) | (green<<8) | blue;
//		for(int i = 1;i<buffer.length;i++) {
//			int argb = buffer[i];
//			int red = 0xFF & ( argb >> 16);
//			int alpha = 0xFF & (argb >> 24);
//			int blue = 0xFF & (argb >> 0 );
//			int green = 0xFF & (argb >> 8 );
//		}
		imageContainer.setImage(image);
    }
    
    @FXML
    protected void handleSaveButtonAction(ActionEvent event) {
    	Window owner = loadButtom.getScene().getWindow();
        File file = fileChooser.showSaveDialog(owner);
		if(file == null) {
			return;
		}
		try {
			processor.saveResults(file.getAbsolutePath());
			System.out.println("Imagen Guardada");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}