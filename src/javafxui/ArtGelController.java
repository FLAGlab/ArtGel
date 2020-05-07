package javafxui;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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
import javafx.stage.Modality;
import javafx.stage.Stage;


public class ArtGelController {

    @FXML
    private Button loadButtom;
    
    @FXML
    private Button preprocessButton;
    
    @FXML
    private Button rulerChangeButton;
    
    @FXML
    private Button wellsChangeButton;
    
    @FXML
    private ImageView imageContainer;
    
    @FXML
    private StackPane imagePanel;
    
    @FXML
    private ListView<String> listRuler;
    
    @FXML
    private ListView<String> listWells;
    
    private WritableImage image;
    
    private Stage rulerStage;
    
    private Image originalImage;
    private WritableImage modifiedImage;
    
    private Band selectedBand;
    
    private double xInicial = -1;
    private double yInicial = -1;
    private double x1 = -1;
    private double y1 = -1;
    private double x2 = -1;
    private double y2 = -1;
    
    private int[] ruler;
    
    @FXML
    public void initialize() {
    	if(rulerChangeButton!=null && wellsChangeButton!=null) {
    		rulerChangeButton.setDisable(true);
        	wellsChangeButton.setDisable(true);
    	}
    }
    
    final FileChooser fileChooser = new FileChooser();
	final IntensityProcessor processor = new IntensityProcessor();
	
	final EventHandler<MouseEvent> clickEvent = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent e) {
        	xInicial = -1;
            yInicial = -1;

        	int column = (int)e.getX();
        	int row = (int)e.getY();
        	System.out.println("Selected row: "+row+" column: "+column);
        	selectedBand = null;
        	for(Band band:processor.getBands()) {
        		//System.out.println("Start row: "+band.getStartRow()+" column: "+band.getStartColumn());
        		if(band.getStartRow()<=row && band.getEndRow()>=row && band.getStartColumn()<=column && band.getEndColumn()>=column) {
        			selectedBand=band;
        			break;
        		}
        	}
        	if(selectedBand!=null) {
        		int sourceWidth = (int) modifiedImage.getWidth();
        		int sourceHeight = (int) modifiedImage.getHeight();
        		PixelReader pixelReader = modifiedImage.getPixelReader();
        		PixelWriter pixelWriter = image.getPixelWriter();
        		WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();
        		int[] buffer = new int[sourceWidth * sourceHeight];
        		pixelReader.getPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);
        		int color = (255 << 24) | (100 << 16 ) | (128<<8) | 200;
    			for(int i = selectedBand.getStartColumn(); i<selectedBand.getEndColumn();i++) {
    				for(int j = selectedBand.getStartRow(); j<selectedBand.getEndRow();j++) {
        				buffer[((int)j)*sourceWidth+((int)i)]=color;
            		}
        		}		
        		pixelWriter.setPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);
        		imageContainer.setImage(image);
        	}
        }
    };
	
	final EventHandler<MouseEvent> dragInitEvent = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {

            ImageView bb = (ImageView) event.getSource();
            Image img = bb.getImage();
            xInicial= event.getX();
            yInicial= event.getY();
            System.out.println("I got detected: ("+xInicial+","+yInicial+") ("+img.getHeight()+","+img.getWidth()+")");
            
        }
    };
    final EventHandler<MouseEvent> draggedEvent = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent event) {
            int sourceWidth = (int) modifiedImage.getWidth();
    		int sourceHeight = (int) modifiedImage.getHeight();
            x1= Math.max(0, Math.min(sourceWidth-1, event.getX()));
            y1= Math.max(0, Math.min(sourceHeight-1,event.getY()));
    		PixelReader pixelReader = modifiedImage.getPixelReader();
    		PixelWriter pixelWriter = image.getPixelWriter();
    		WritablePixelFormat<IntBuffer> format = WritablePixelFormat.getIntArgbInstance();
    		int[] buffer = new int[sourceWidth * sourceHeight];
    		pixelReader.getPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);
    		int color = (255 << 24) | (0 << 16 ) | (128<<8) | 200;
    		if( xInicial>=0 && yInicial>=0) {
    			for(int i = Math.min((int)xInicial, (int)x1); i<Math.max((int)xInicial, (int)x1);i++) {
    				buffer[((int)yInicial)*sourceWidth+((int)i)]=color;
    				buffer[((int)y1)*sourceWidth+((int)i)]=color;
        		}
    			for(int j = Math.min((int)yInicial, (int)y1); j<Math.max((int)yInicial, (int)y1);j++) {
    				buffer[((int)j)*sourceWidth+((int)xInicial)]=color;
    				buffer[((int)j)*sourceWidth+((int)x1)]=color;
        		}
    		}	
    		pixelWriter.setPixels(0, 0, sourceWidth, sourceHeight, format, buffer, 0, sourceWidth);
    		imageContainer.setImage(image);
    		x2=xInicial;
    		y2=yInicial;
        }
    };

	

    @FXML
    protected void handleLoadButtonAction(ActionEvent event) {
        Window owner = loadButtom.getScene().getWindow();
        File file = fileChooser.showOpenDialog(owner);
        originalImage = new Image(file.toURI().toString());
		int sourceWidth = (int) originalImage.getWidth();
		int sourceHeight = (int) originalImage.getHeight();
		PixelReader pixelReader = originalImage.getPixelReader();
		image = new WritableImage(sourceWidth,sourceHeight);
		modifiedImage = new WritableImage(sourceWidth,sourceHeight);
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
		BufferedImage modifiedImg = processor.getModifiedImage();
		SwingFXUtils.toFXImage(modifiedImg, image);
		SwingFXUtils.toFXImage(modifiedImg, modifiedImage);
		
		listRuler.getItems().clear();
		listWells.getItems().clear();
		
		imageContainer.setImage(image);
		imageContainer.setOnDragDetected(dragInitEvent);
		imageContainer.setOnMouseDragged(draggedEvent);
		imageContainer.setOnMouseClicked(clickEvent);
		rulerChangeButton.setDisable(false);
    	wellsChangeButton.setDisable(false);
    	ruler = new int[processor.getNumClusters()];
    	for(int i = 1 ; i<=ruler.length;i++) {
    		ruler[i-1]=i;
    	}
    	updateRuler();
    	updateWells();
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
    
    @FXML
    protected void handleAddButtonAction(ActionEvent event) {
		try {
			int startRow =(int) Math.min(y1, y2);
			int endRow =(int)Math.max(y1, y2);
			int startColumn=(int)Math.min(x1, x2);
			int endColumn=(int)Math.max(x1, x2);
			Band newBand = new Band(startRow, endRow, startColumn, endColumn, 0);
			processor.addBand(newBand);
			BufferedImage modifiedImg = processor.getModifiedImage();
			SwingFXUtils.toFXImage(modifiedImg, image);
			SwingFXUtils.toFXImage(modifiedImg, modifiedImage);
			
			imageContainer.setImage(image);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @FXML
    protected void handleClusterButtonAction(ActionEvent event) {
		try {
			processor.createWells();
			processor.clusterAlleles();
			BufferedImage modifiedImg = processor.getModifiedImage();
			SwingFXUtils.toFXImage(modifiedImg, image);
			SwingFXUtils.toFXImage(modifiedImg, modifiedImage);
			
			imageContainer.setImage(image);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @FXML
    protected void handleDeleteButtonAction(ActionEvent event) {
		try {
			if(selectedBand == null) return;
			processor.deleteBand(selectedBand);
			BufferedImage modifiedImg = processor.getModifiedImage();
			SwingFXUtils.toFXImage(modifiedImg, image);
			SwingFXUtils.toFXImage(modifiedImg, modifiedImage);
			
			imageContainer.setImage(image);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @FXML
    protected void handlePreprocessButtonAction(ActionEvent event) {
		preprocessButton.setText("Deprocess");
		preprocessButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// TODO Auto-generated method stub
				handleDeprocessButtonAction(arg0);
			}
		});
		try {
			processor.preprocessImage();
			BufferedImage modifiedImg = processor.getPreprocessedImage();
			SwingFXUtils.toFXImage(modifiedImg, image);
			imageContainer.setImage(image);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    protected void handleDeprocessButtonAction(ActionEvent event) {
    	preprocessButton.setText("Preprocess");
		preprocessButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// TODO Auto-generated method stub
				handlePreprocessButtonAction(arg0);
			}
		});
		
		try {
			processor.deprocessImage();
			BufferedImage modifiedImg = processor.getImage();
			SwingFXUtils.toFXImage(modifiedImg, image);
			imageContainer.setImage(image);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    }
    
    @FXML
    protected void handleRulerChangeButtonAction(ActionEvent event) {
    	Stage stage = new Stage();
		try {
		FXMLLoader loader = new FXMLLoader(
			    getClass().getResource(
			      "artgel_ruler_modal.fxml"
			    ));
		stage.setScene(new Scene(loader.load(), 600, 600));
        stage.setTitle("Ruler");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
        
        RulerController controller = loader.<RulerController>getController();
        controller.initData(ruler,this);
        rulerStage = stage;
        rulerStage.show(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @FXML
    protected void handleWellsChangeButtonAction(ActionEvent event) {
    	Stage stage = new Stage();
		try {
		FXMLLoader loader = new FXMLLoader(
			    getClass().getResource(
			      "artgel_wells_modal.fxml"
			    ));
		stage.setScene(new Scene(loader.load(), 600, 600));
        stage.setTitle("Wells");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node)event.getSource()).getScene().getWindow() );
        
        WellsController controller = loader.<WellsController>getController();
        controller.initData(processor,this);
        rulerStage = stage;
        rulerStage.show(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    void changeRuler(int[] ruler) {
    	this.ruler = ruler;
    	updateRuler();
    	rulerStage.close();
    }
    
    void changeWells() {
    	listWells.getItems().clear();
    	updateWells();
    }
    
    private void updateRuler() {
    	listRuler.getItems().clear();
    	for(int i = 1 ; i<=ruler.length;i++) {
    		listRuler.getItems().add(i+": "+ruler[i-1]);
    	}
    }
    
    private void updateWells() {
    	List<Well> wells = processor.getWells();
    	for(int i = 1 ; i<=wells.size();i++) {
    		listWells.getItems().add(i+": "+wells.get(i-1).getSampleId());
    	}
    }
}