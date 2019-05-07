/*******************************************************************************
 * ArtGel - Artificial Intelligence Gel Analysis Tool
 * Copyright 2019 Hector A. Ruiz-Moreno, Cindy P. Ulloa-Guerrero, Jorge Duitama
 *
 * This file is part of ArtGel.
 *
 *     ArtGel is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ArtGel is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ArtGel.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package Geles;

import ngsep.clustering.Dendrogram;
import ngsep.clustering.DistanceMatrix;
import ngsep.clustering.NeighborJoining;
import ngsep.hmm.ConstantTransitionHMM;
import ngsep.hmm.HMM;
import ngsep.math.Distribution;
import ngsep.math.LogMath;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import JSci.maths.statistics.NormalDistribution;

/**
 * Processes gel image 
 * @author Cindy Ulloa, Hector Ruiz, Jorge Duitama
 *
 */
public class IntensityProcessor {
	
	//Image attributes
	private BufferedImage image;
	private int imageRows;
	private int imageColumns;
	private double  [] [] intensitiesMatrix;
	
	// Grey intensity distributions
	private NormalDistribution backgroundDistribution;
	private NormalDistribution signalDistribution;
	
	
	private Double [][] pixelSignalLogPosteriors;
	private Double [][] pixelBackgroundLogPosteriors;
 	
	// Binary signal image matrix
	private boolean [][] binarySignalMatrix;
	
	// Band locations image matrix
	private boolean [][] bandLocationsMatrix;
	
	//Typical band dimensions
	private int typicalBandHeight;
	private int typicalBandWidth;
	
	private List<Well> wells = new ArrayList<>();
	private List<Band> bands = new ArrayList<>();
	private static List<Color> bandColors = new ArrayList<>();
	
	private int numClusters;
	
	

	private double [][] samplesDistanceMatrix;
	
	private SampleClusteringAlgorithm sampleClustering;
	
    private double Pstart_signal=0.05;
    private double Ptransition=0.05;
    
	public static void main(String[] args) throws Exception {
		IntensityProcessor instance = new IntensityProcessor();
		String inputFile = args[0];
		String outputFilePrefix = args[1];
		instance.loadImage(inputFile);
		instance.processImage();
		instance.saveResults(outputFilePrefix);
		
		//GelImage gelImage= new GelImage(outputFile);

	}
	
	static {
		bandColors.add(Color.RED);
		bandColors.add(Color.CYAN);
		bandColors.add(Color.YELLOW);
		bandColors.add(Color.GREEN);
		bandColors.add(Color.MAGENTA);
		bandColors.add(Color.ORANGE);
		bandColors.add(Color.PINK);
		bandColors.add(new Color(255, 100, 100));
		bandColors.add(new Color(100, 255, 100));
		bandColors.add(new Color(100, 100, 255));
	}
	public void loadImage(String inputFile) throws IOException {
		image = ImageIO.read( new File (inputFile) );

        imageRows = image.getHeight();
        imageColumns = image.getWidth();
        System.out.println("Rows: "+imageRows+" Cols: "+imageColumns);
        
        //Calculate scores:Grey scales parameteres
        intensitiesMatrix = new double [imageRows][imageColumns];
        for( int j = 0; j < imageColumns; j++ )
        {
        	for( int i = 0; i < imageRows; i++ )
        	{
        		Color c = new Color(image.getRGB( j, i ) );
        		intensitiesMatrix[i][j] = getGrayIntensity(c);
        	}
        	//System.out.println("Col score pos "+j+": "+colScores[j]);
        }
        backgroundDistribution = signalDistribution = null;
        typicalBandHeight = typicalBandWidth = 0;
        
        wells = new ArrayList<>();
        bands = new ArrayList<>();
	}
	
	private double getGrayIntensity(Color c) {
		double sum = c.getRed()+c.getGreen()+c.getBlue();
		if(sum<0) System.out.println("RGB:"+c.getRed()+" "+c.getGreen()+" "+c.getBlue()+" sum: "+sum);
		return sum/3;
	}
	
	public void processImage() throws Exception {
		wells = new ArrayList<>();
		bands = new ArrayList<>();
		bandLocationsMatrix = new boolean[imageRows][imageColumns];
		for(int i=0;i<imageRows;i++) Arrays.fill(bandLocationsMatrix[i], false);
		int correctionRounds = 1;
		for(int i=0;i<=correctionRounds;i++) {
			// STEP 1: Predict distributions of background and signal combined in the image
			predictIntensityDistributions();
			
			// STEP 2: Build HMM to identify wells and bands
	        HMM hmm = buildHMM();
	        
	        // STEP 3: Identify signal
	    	predictSignalHMM(hmm);
	    	
	        //STEP 4: Identify typical band dimensions
	    	estimateBandDimensions();
	    	
	    	// Perform corrections on the image based on the learned values
	    	if(i<correctionRounds) correctImage();
		}
        
    	// STEP 5: Identify bands
    	//predictBands(1, 0.8); //0.8 threshold
    	
    	//predictBands(2, 0.95);
    	
    	predictBands(0,imageColumns, typicalBandHeight, 3, 0.5);
    	
    	if(bands.size()==0) throw new Exception ("No bands were found");
    	
    	// STEP 6: Identify wells performing vertical clustering of bands
    	createWells();
    	
    	// STEP 7: Identify missing bands from registered alleles
    	discoverMissingBands(0.7);
    	
    	// STEP 8: Identify missing bands small size
    	for(Well well:wells) {
    		predictBands(well.getStartCol(), well.getStartCol()+well.getWellWidth(), Math.max(5, typicalBandHeight/2), 3, 0.5);
    	}
    	
    	// STEP 7: Identify missing bands from registered alleles
    	discoverMissingBands(0.6);
    	
    	// STEP 6: Identify wells performing vertical clustering of bands
    	createWells();
    	
    	// STEP 9: Cluster alleles and samples
    	clusterAlleles();
    	
    	// STEP 10: Cluster samples
    	clusterSamples();
    	
    	//Print for testing
    	System.out.println("");
    	System.out.println("Wells \t Bands \t Clusters");
    	System.out.println(wells.size() + "\t" + bands.size() + "\t" + numClusters);
    	
//    	createWellsKmeans();
//    	
//    	System.out.println("wells#: " + wells.size());
//        
//        //System.out.println("Total number of bands:\t"+bands.size());
//        //STEP 5: Cluster bands looking for variant alleles
//        BandsClusteringAlgorithm bandsClustering = new HeuristicCliqueBandClusteringAlgorithm();
//        int numClusters = bandsClustering.clusterBands(bands);
//        System.out.println("Total cliques: "+numClusters);
//        
//        //STEP 6: Cluster samples (Wells)
//        sampleClustering = new DiceSampleClustering();
//        clusteringTree=sampleClustering.clusterSamples(wells);
        
        
	}

	private void predictIntensityDistributions() {
		// backgroundDistribution = new NormalDistribution(0,1600);
        // signalDistribution= new NormalDistribution(254,1600);
		// Mixture model to predict background and signal distributions
        DynamicDistribution intensityDistribution = new DynamicDistribution(intensitiesMatrix);
        double bwdMean = intensityDistribution.getBwdMean();
        double bwdVar = intensityDistribution.getBwdVar();
        double signalMean = intensityDistribution.getSignalMean();
        double signalVar = intensityDistribution.getsignalVar();
        backgroundDistribution = new NormalDistribution(bwdMean,bwdVar);
        signalDistribution = new NormalDistribution(signalMean,signalVar);
	}
	
	private HMM buildHMM() {
        List<DistributionState> states = new ArrayList<>();
        DistributionState background = new DistributionState("background",1-Pstart_signal,backgroundDistribution);
        DistributionState signal = new DistributionState("signal",Pstart_signal,signalDistribution);
    	
    	states.add(background);
    	states.add(signal);
    	
    	ConstantTransitionHMM hmm = new ConstantTransitionHMM(states);
    	Double[][] tMatrix= new Double[2][2];
    	Double tChange=LogMath.log10(Ptransition);
    	Double tStay=LogMath.log10(1-Ptransition);
    	tMatrix[0][0]=tMatrix[1][1]=tStay;
    	tMatrix[0][1]=tMatrix[1][0]=tChange;
    	
    	hmm.setTransitions(tMatrix);
    	return hmm;
	}

	
	
	public void predictSignalHMM(HMM hmm) {
		List<Double> observations = new ArrayList<>();
		binarySignalMatrix= new boolean[imageRows][imageColumns];
		pixelBackgroundLogPosteriors = new Double[imageRows][imageColumns];
		pixelSignalLogPosteriors = new Double[imageRows][imageColumns];
		for(int i=0; i<imageRows; i++){
			for(int j=0; j<imageColumns; j++){
				observations.add(intensitiesMatrix[i][j]);
			}
			
			Double [][] logPosteriors = new Double [imageColumns][2];
			hmm.calculatePosteriorLogs(observations, logPosteriors);
			
			
			//PRINT POSTERIORS
			
	    	for(int j=0;j<imageColumns;j++){
	    		//System.out.println("posBwd: \t" + posteriors[j][0] + "\t posSig: \t" + posteriors[j][1] );
	    		LogMath.normalizeLogs(logPosteriors[j]);
	    		pixelBackgroundLogPosteriors[i][j] = logPosteriors[j][0];
	    		pixelSignalLogPosteriors[i][j] = logPosteriors[j][1];
	    		
	    		binarySignalMatrix[i][j]= LogMath.power10(logPosteriors[j][1])>0.99;
	    		
	    	}
	    	observations.clear();  	
		}
		//saveBinSignal(binarySignalMatrix);
		
		//Save posterior matrices
		/*String outputFile1 = "PosteriorBWD.csv";
		try (PrintStream out = new PrintStream(outputFile1)) {
			for(int i=0; i<posBwdMatrix.length; i++){
				for(int j=0; j<posBwdMatrix[0].length; j++){
					out.print(posBwdMatrix[i][j]+";");
				}
				out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String outputFile2 = "PosteriorSIGNAL.csv";
		try (PrintStream out = new PrintStream(outputFile2)) {
			for(int i=0; i<posSigMatrix.length; i++){
				for(int j=0; j<posSigMatrix[0].length; j++){
					out.print(posSigMatrix[i][j]+";");
				}
				out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
	}
	
	private void estimateBandDimensions() throws Exception {
		//Mean
		ArrayList<Integer> rowBandWidths = new ArrayList<>();
		ArrayList<Integer> colBandHeights=new ArrayList<>();
		
		//Estimate column width
		for(int i=0; i<imageRows; i++){
			int rowCounter=1; //Count number of adjacent signal pixels in each row
			
			for(int j=0; j<imageColumns-1; j++){
				
					if(binarySignalMatrix[i][j] && binarySignalMatrix[i][j+1]){
						rowCounter++;
					} else if(binarySignalMatrix[i][j]){
						if(rowCounter >= 5) {
							//System.out.println("New width: " + rowCounter+" at row: "+i+" col: "+j);
							rowBandWidths.add(rowCounter);
						}
						
						rowCounter=1;
					}	
			}
			if(rowCounter > 1){
				rowBandWidths.add(rowCounter);
			}
		}
		// Estimate row height 
		for(int j=0; j<imageColumns; j++){
			int colCounter=1; //Count number of adjacent signal pixels in each row
			for(int i=0; i<imageRows-1; i++){
				
					if(binarySignalMatrix[i][j] && binarySignalMatrix[i+1][j]){
						colCounter++;
					} else if(binarySignalMatrix[i][j]){
						if(colCounter > 1){
							colBandHeights.add(colCounter);
						}
						//System.out.println("rowHeightN: \t" + colCounter);
						colCounter=1;
					}	
			}
			if(colCounter > 1){
				colBandHeights.add(colCounter);
			}
		}
		if(colBandHeights.size()==0 || rowBandWidths.size()==0) throw new Exception ("No signal detected in this image");
		//Calculate signal statistics
		Collections.sort(rowBandWidths);
		int medianW = rowBandWidths.get(rowBandWidths.size()/2);
		double sumW = 0;
		double sumW2 = 0;
		for(int w:rowBandWidths){
			sumW = sumW+w;
			sumW2 = sumW2 + w*w;
		}
		double nW = rowBandWidths.size();
		double meanW = sumW/nW;
		double varianceW = 0;
		if(nW>1) varianceW = (sumW2-sumW*sumW/nW)/(nW-1);
		System.out.println("Width statistics. Median: "+medianW+" Mean: "+meanW+" Variance: "+varianceW);
		
		typicalBandWidth = medianW;
		if (meanW > medianW) typicalBandWidth += (meanW-medianW)/2;
		Collections.sort(colBandHeights);
		int medianH = colBandHeights.get(colBandHeights.size()/2);
		double sumH = 0;
		double sumH2 = 0;
		for(int h:colBandHeights){
			sumH = sumH+h;
			sumH2 = sumH2 + h*h;
		}
		double nH = colBandHeights.size();
		double meanH = sumH/nH;
		double varianceH = 0;
		if(nH>1) varianceH = (sumH2-sumH*sumH/nH)/(nH-1);
		System.out.println("Height statistics. Median: "+medianH+" Mean: "+meanH+" Variance: "+varianceH);
		typicalBandHeight = medianH;
		//minimum band width
		if(imageColumns>1500 && typicalBandWidth<50) { 
			typicalBandWidth=50;
		} else if(imageColumns>1000 && typicalBandWidth<40) { 
			typicalBandWidth=40;
		} else if(imageColumns>500 && typicalBandWidth<20) { 
			typicalBandWidth=20;
		} else if (typicalBandWidth < 10 ){
			typicalBandWidth=10;
		}
		System.out.println("Estimated band width: " + typicalBandWidth + "\t height: " + typicalBandHeight );
	}
	
	/**
	 * Corrects the image intensities having typical band dimensions already calculated
	 */
	private void correctImage() {
		// TODO Auto-generated method stub
		backgroundDistribution.getMean();
		for(int i=0; i<imageRows; i++){
			correctRow(i);
		}
	}

	private void correctRow(int i) {
		int k = 0;
		int intensityDifference = (int) Math.round(signalDistribution.getMean()-backgroundDistribution.getMean());
		for(int j=0; j<imageColumns; j++){
			if(!binarySignalMatrix[i][j]) {
				int d = j-k;
				if(d>0 && d<5) {
					// Correct small signal, probably noise
					for(int l=k;l<j;l++) intensitiesMatrix[i][l]= 0; 
				} else if(d>2*typicalBandWidth) {
					// Reduce intensity of very long regions
					for(int l=k;l<j;l++) {
						intensitiesMatrix[i][l]= Math.max(0, intensitiesMatrix[i][l]-intensityDifference); 
					}
				}
				k=j+1;
			}
		}
	}

	/**
	 * Creates bands according to hmm binary signal observations 
	 * @param threshold double minimum fraction of signal in sliding window to create a band
	 */
	public void predictBands(int startColumn, int endColumn, int bandHeight, int method, double threshold) {
		
		
		List<PixelWithRealValue> pixels = new ArrayList<>();
		double averageValue = 0;
		int numValues = 0;
		Distribution distLogFold = new Distribution(0, 2000, 100);
		for(int i=0; i<imageRows-bandHeight; i++){ //start loop along all signal matrix
			for(int j=startColumn; j<endColumn-typicalBandWidth; j++){
				if(method == 1) {
					if(!binarySignalMatrix[i][j]) continue;
					double proportion = calculateProportionSignalRegion (i,i+bandHeight,j,j+typicalBandWidth);
					pixels.add(new PixelWithRealValue(i, j, proportion));
					averageValue+=proportion;
					numValues++;
				} else if (method == 2) {
					double avgPostRegion = calculateProbabilityAverageSignalMixtureModel(i,i+bandHeight,j,j+typicalBandWidth);
					pixels.add(new PixelWithRealValue(i, j, avgPostRegion));
					averageValue+=avgPostRegion;
					numValues++;
				} else if (method == 3) {
					double proportion = calculateProportionSignalRegion (i,i+bandHeight,j,j+typicalBandWidth);
					if(proportion < threshold) continue;
					double logCondChange = calculateLogFoldChangeMixtureModel (i,i+bandHeight,j,j+typicalBandWidth);
					distLogFold.processDatapoint(logCondChange);
					pixels.add(new PixelWithRealValue(i, j, logCondChange));
					averageValue+=logCondChange;
					numValues++;
				} else if (method == 4) {
					Double logPostRegion = calculateHMMLogPosteriorRegion (i,i+bandHeight,j,j+typicalBandWidth);
					if(logPostRegion==null) continue;
					pixels.add(new PixelWithRealValue(i, j, LogMath.power10(logPostRegion)));
					averageValue+=logPostRegion;
					numValues++;
				} 
				
			}
		}
		Collections.sort(pixels);
		if(method == 3) {
			//distLogFold.printDistributionInt(System.out);
			threshold = averageValue / numValues;
			//System.out.println("Average log fold: "+threshold);
		}
		for(PixelWithRealValue pixel:pixels) {
			int i = pixel.getRow();
			int j = pixel.getColumn();
			if(pixel.getValue()>threshold) addBandWithinRegion(i, i+bandHeight, j, j+typicalBandWidth);
		}
	}

	private double calculateProportionSignalRegion(int startRow, int endRow, int startColumn, int endColumn) {
		double totPixels=(endRow-startRow)*(endColumn-startColumn);
		double ones = 0;
		for(int y=startRow; y<endRow; y++){ //opens window
			for(int x=startColumn; x<endColumn; x++){
				if(binarySignalMatrix[y][x]){
					ones++;
				}
			}
		}
		return ones/totPixels;
	}

	private Double calculateHMMLogPosteriorRegion(int startRow, int endRow, int startColumn, int endColumn) {
		Double answer = 0.0;
		Double logPostBkg = 0.0;
		for(int i=startRow; i<endRow; i++){ //opens window
			for(int j=startColumn; j<endColumn; j++){
				answer = LogMath.logProduct(answer, pixelSignalLogPosteriors[i][j]);
				logPostBkg = LogMath.logProduct(logPostBkg, pixelBackgroundLogPosteriors[i][j]);
			}
		}
		Double sum = LogMath.logSum(answer, logPostBkg);
		//answer = LogMath.logProduct(answer,-sum); 
		return answer;
	}
	
	private double calculateLogFoldChangeMixtureModel(int startRow, int endRow, int startColumn, int endColumn) {
		DistributionState signalState = new DistributionState("", 0.5, signalDistribution);
		DistributionState backgroundState = new DistributionState("", 0.5, backgroundDistribution);
		Double logCondSignal = 0.0;
		Double logCondBkg = 0.0;
		for(int i=startRow; i<endRow; i++){ //opens window
			for(int j=startColumn; j<endColumn; j++) {
				double intensity = intensitiesMatrix[i][j];
				logCondSignal = LogMath.logProduct(logCondSignal, signalState.getEmission(intensity, 0));
				logCondBkg = LogMath.logProduct(logCondBkg, backgroundState.getEmission(intensity, 0));
			}
		}
		if(logCondSignal==null) return 0;
		if(logCondBkg == null) return -logCondSignal;
		return logCondSignal-logCondBkg;
	}
	
	public double calculateProbabilityAverageSignalMixtureModel (int startRow, int endRow, int startColumn, int endColumn) {
		DistributionState signalState = new DistributionState("", 0.5, signalDistribution);
		DistributionState backgroundState = new DistributionState("", 0.5, backgroundDistribution);
		double avgIntensity = 0;
		int datapoints = 0;
		for(int i=startRow; i<endRow; i++){ //opens window
			for(int j=startColumn; j<endColumn; j++) {
				avgIntensity += intensitiesMatrix[i][j];
				datapoints++;
			}
		}
		if(datapoints>0) avgIntensity /=datapoints;
		Double logCondSignal = signalState.getEmission(avgIntensity, 0);
		Double logCondBkg = backgroundState.getEmission(avgIntensity, 0);
		Double sum = LogMath.logSum(logCondSignal, logCondBkg);
		return LogMath.power10(LogMath.logProduct(logCondSignal, -sum));
	}

	private void addBandWithinRegion(int startRow, int endRow, int startColumn, int endColumn) {
		for(int y=startRow; y<endRow; y++){ //opens window
			for(int x=startColumn; x<endColumn; x++){
				if(bandLocationsMatrix[y][x]) return;
			}
		}
		Band band = new Band(startRow, endRow, startColumn, endColumn, bands.size());
		bands.add(band);
		for(int y=startRow; y<endRow; y++) {
			for(int x=startColumn; x<endColumn; x++) {
				bandLocationsMatrix[y][x] = true;
			}
		}
		
	}

	private void discoverMissingBands(double threshold) {
		List<Band> currentBands = new ArrayList<>(bands);
		for(Band b:currentBands) {
			int bandsWell = b.getWellID();
			int startRow = b.getStartRow();
			int endRow = b.getEndRow();
			int endLastWell = 0;
			for(Well w:wells){
				int startColumn = w.getStartCol();
				//Check space before well
				for(int j=endLastWell+5;j<startColumn-typicalBandWidth-5;j++) {
					double proportion = calculateProportionSignalRegion (startRow,endRow,j,j+typicalBandWidth);
					if(proportion < threshold) continue;
					addBandWithinRegion(startRow, endRow, j, j+typicalBandWidth);
				}
				int endColumn = startColumn + w.getWellWidth();	
				if(w.getWellID() != bandsWell) {
					double proportion = calculateProportionSignalRegion (startRow,endRow,startColumn,startColumn+typicalBandWidth);
					if(proportion < threshold) continue;
					addBandWithinRegion(startRow, endRow, startColumn, startColumn+typicalBandWidth);
				}
				endLastWell = endColumn;
			}
			for(int j=endLastWell+5;j<imageColumns-typicalBandWidth-1;j++) {
				double proportion = calculateProportionSignalRegion (startRow,endRow,j,j+typicalBandWidth);
				if(proportion < threshold) continue;
				addBandWithinRegion(startRow, endRow, j, j+typicalBandWidth);
			}
		}
	}
	public void createWells() {
		wells = new ArrayList<>();
    	createWellsKmeans();   
	}
	private void createWellsKmeans() {
		//Select best K value
		int highKvalue = (int)Math.round(imageColumns/typicalBandWidth);
		System.out.println("highK: " + highKvalue);
		
		double[] var = new double[highKvalue+1];
		double[] logVar = new double[var.length];
		for(int k=1; k<=highKvalue; k++){
			KMeansWellPrediction kmeansK = new KMeansWellPrediction(imageRows, imageColumns, bands, k);
			kmeansK.verticalBandClustering();
			var[k]=kmeansK.calculateVariance();
			if(var[k]==0) logVar[k]=logVar[k-1];
			else logVar[k]=Math.log10(var[k]);
		}
		var[0] = var[1];
		logVar[0] = logVar[1];
		//Calculate deltas
		double[] deltaVar = new double[var.length];
		double[] deltaLogVar = new double[var.length];
		deltaVar[0]=0;
		deltaLogVar[0]=0;
		for(int k=1; k<var.length; k++){
			deltaVar[k]=var[k-1]-var[k];
			deltaLogVar[k]=Math.abs(logVar[k-1]-logVar[k]);
			//System.out.println("k: "+k+" stdev "+Math.sqrt(var[k])+" logvar: "+logVar[k]+" delta logVar: " +deltaLogVar[k]);
		}
		double maxdLogSE=deltaLogVar[0];
		int selectedK=0;
		for(int k=1; k<deltaLogVar.length; k++){
			if(Math.sqrt(var[k])>2*typicalBandWidth) continue;
			double dse = deltaLogVar[k];
			if(dse>maxdLogSE){
				maxdLogSE = dse;
				selectedK=k;
			}
		}
		System.out.println("\t seletedK = " + selectedK);
		
//		for(int i=deltaVar.length-1; i>0;i--){
//			double dse = deltaVar[i];
//			if(dse>=maxdSE){
//				maxdSE=dse;
//				selectedK=i+lowKvalue;
//			}
//			if(dse>2*var[var.length-1]){
//				break;
//			}
//		}
		
		KMeansWellPrediction km = new KMeansWellPrediction(imageRows, imageColumns, bands, selectedK);
		km.verticalBandClustering();
		wells = km.createWells();	
	}
	public void clusterAlleles(){
		//STEP : Cluster bands looking for variant alleles
    	for (Band b:bands) b.setAlleleClusterId(-1);
        BandsClusteringAlgorithm bandsClustering = new HeuristicCliqueBandClusteringAlgorithm();
        List<List<Band>> alleleClusters = bandsClustering.clusterBands(bands);
        Collections.sort(alleleClusters,new Comparator<List<Band>>() {

			@Override
			public int compare(List<Band> l1, List<Band> l2) {
				double average1=getAverageRow(l1);
				double average2=getAverageRow(l2);
				if(average1<average2)return -1;
				else if(average1>average2)return 1;
				return 0;
			}

			private double getAverageRow(List<Band> list) {
				double avg = 0;
				for(Band b:list) avg+=b.getMiddleRow();
				return avg/list.size();
			}
		});
        numClusters = alleleClusters.size();
        System.out.println("Total clusters: "+numClusters);
        for(int i=0;i<numClusters;i++) {
        	List<Band> cluster = alleleClusters.get(i);
        	for(Band b:cluster) b.setAlleleClusterId(i+1);
        }
	}

	public void clusterSamples() {
		//STEP 6: Cluster samples (Wells)
        //sampleClustering = new DiceSampleClustering();
        sampleClustering = new PosteriorMeanMixtureModelSampleClustering();
        samplesDistanceMatrix=sampleClustering.clusterSamples(this);
	}
	
	public void setWellSampleIds(List<String> wellIds) {
		if(wellIds.size()!=wells.size()) throw new IllegalArgumentException("Inconsistent size of well ids. Expected: "+wells.size()+" given: "+wellIds.size());
		for(int i=0;i<wellIds.size();i++) {
			wells.get(i).setSampleId(wellIds.get(i));
		}
	}
	public BufferedImage getModifiedImage () {
		BufferedImage answer = new BufferedImage(imageColumns, imageRows, BufferedImage.TYPE_INT_RGB);
		for( int i = 0; i < imageRows; i++ )
        {
            for( int j = 0; j < imageColumns; j++ )
            {
            	int grayIntensity = (int)Math.round(intensitiesMatrix[i][j]);
                answer.setRGB( j, i, new Color(grayIntensity,grayIntensity,grayIntensity).getRGB() );
            }
        }
		Graphics2D g2DImage = (Graphics2D) answer.createGraphics();
		g2DImage.setStroke(new BasicStroke(2));
		for(Band band: bands) {
			int alleleCluster = band.getAlleleClusterId();
			if(alleleCluster>=0) {
				g2DImage.setColor(bandColors.get(alleleCluster%10));
				g2DImage.drawString(""+alleleCluster, band.getMiddleColumn(), band.getMiddleRow());
			} else {
				g2DImage.setColor(Color.RED);
			}
			
			g2DImage.drawRect(band.getStartColumn(), band.getStartRow(), band.getEndColumn()-band.getStartColumn(), band.getEndRow()-band.getStartRow());
		}
		
		for(Well well:wells) {
			g2DImage.setColor(Color.WHITE);
			g2DImage.drawRect(well.getStartCol(), well.getStartRow(), well.getWellWidth(), well.getWellHeight());
		}
        return answer;
	}
	public void saveResults(String outputFilePrefix) throws IOException {
		File outFileImage = new File(outputFilePrefix+"_image.png");
		ImageIO.write(getModifiedImage(), "png", outFileImage);
		
		String outputFileDendrogam = outputFilePrefix+"_tree.nwk";
		List<String> sampleIds = new ArrayList<>();
		for(Well well:wells) sampleIds.add(well.getSampleId());
		DistanceMatrix matrix=new DistanceMatrix(sampleIds, samplesDistanceMatrix);
		NeighborJoining njClustering = new NeighborJoining();
		njClustering.loadMatrix(matrix);
		Dendrogram njDendogram = njClustering.constructNJTree();
		try (PrintStream out = new PrintStream(outputFileDendrogam)) {
			njDendogram.printTree(out);
		}
		
		String outputFileMatrix = outputFilePrefix+"_distances.txt";
		int n = sampleIds.size();
		try (PrintStream out = new PrintStream(outputFileMatrix)) {
			out.println(n);
			for(int i=0; i<n; i++) {
				out.print(sampleIds.get(i));
				for(int j=0; j<n; j++){
					out.print(" "+samplesDistanceMatrix[i][j]);
				}
				out.println();
			}
		}
		sampleClustering.saveClusteringData(outputFilePrefix);
	}
	
	public void saveBinSignal(int[][] binarySignalMatrix){
		String outputFile = "BinarySingal.csv";
		try (PrintStream out = new PrintStream(outputFile)) {
			for(int i=0; i<binarySignalMatrix.length; i++){
				for(int j=0; j<binarySignalMatrix[0].length; j++){
					out.print(binarySignalMatrix[i][j]+",");
				}
				out.println();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	public double [][] getIntensities() {
		return intensitiesMatrix;
	}
	public List<Well> getWells() {
		return wells;
	}
	public List<Band> getBands(){
		return bands;
	}
	/**
	 * @return the numClusters
	 */
	public int getNumClusters() {
		return numClusters;
	}

	public void deleteBand(Band selected) {
		bands.remove(selected);
		int wellId = selected.getWellID();
		for(Well well:wells) {
			if(well.getWellID()==wellId) {
				well.removeBand(selected);
			}
		}
	}
	public void addBand(Band bandCoordinates) {
		Band band = new Band(bandCoordinates.getStartRow(), bandCoordinates.getEndRow(), bandCoordinates.getStartColumn(), bandCoordinates.getEndColumn(), bands.size());
		bands.add(band);
		
	}

		
}
class PixelWithRealValue implements Comparable<PixelWithRealValue> {
	private int row;
	private int column;
	private double value;
	public PixelWithRealValue(int row, int column, double value) {
		super();
		this.row = row;
		this.column = column;
		this.value = value;
	}
	/**
	 * @return the row
	 */
	public int getRow() {
		return row;
	}
	/**
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}
	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	@Override
	public int compareTo(PixelWithRealValue pixel) {
		if(value > pixel.value) return -1;
		else if (value < pixel.value) return 1;
		return 0;
	}
	
	
}
