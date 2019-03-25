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
import ngsep.hmm.ConstantTransitionHMM;
import ngsep.hmm.HMM;
import ngsep.math.LogMath;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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
	
	// Binary signal image matrix
	private int [][] binarySignalMatrix;
	
	//Typical band dimensions
	private int typicalBandHeight;
	private int typicalBandWidth;
	
	private List<Well> wells = new ArrayList<>();
	private List<Band> bands = new ArrayList<>();
	
	private int numClusters;
	
	private SampleClusteringAlgorithm sampleClustering;

	private Dendrogram clusteringTree;
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
        
    	// STEP 5: Identify bands using sliding window
    	predictBandsSlidingWindow(0.90); //0.9 threshold
    	
    	// STEP 6: Identify wells performing vertical clustering of bands
    	createWellsBandClusters();
    	
    	// STEP 7: Identify missing bands from registered alleles
    	//discoverMissingBands(0.8);
    	
    	// STEP 8: Identify missing bands small size
    	//int[] bandSize = typicalBandDimensions;
    	//bandSize[0] = bandSize[0] / 1;
    	//double signalThreshold = 0.7;
    	//predictBandsSlidingWindow(bandSize, signalThreshold);
    	
    	// STEP 7: Identify missing bands from registered alleles
    	//discoverMissingBands(0.7);
    	
    	// STEP 8: Identify missing bands small size
    	//bandSize = typicalBandDimensions;
    	//bandSize[0] = bandSize[0] / 2;
    	//signalThreshold = 0.8;
    	//predictBandsSlidingWindow(bandSize, signalThreshold);
    	
    	// STEP 7: Identify missing bands from registered alleles
    	//discoverMissingBands(0.6);
    	
    	// STEP 6: Identify wells performing vertical clustering of bands
    	// createWellsBandClusters();
    	
    	// STEP 9: Cluster alleles and samples
    	clusterAlleles();
    	
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
		binarySignalMatrix= new int[imageRows][imageColumns];
		double [][] posBwdMatrix = new double[imageRows][imageColumns];
		double [][] posSigMatrix = new double[imageRows][imageColumns];
		for(int i=0; i<imageRows; i++){
			for(int j=0; j<imageColumns; j++){
				observations.add(intensitiesMatrix[i][j]);
			}
			double [][] posteriors = new double [imageColumns][2];
			hmm.calculatePosteriors(observations, posteriors);
			
			//PRINT POSTERIORS
			
	    	for(int j=0;j<imageColumns;j++){
	    		//System.out.println("posBwd: \t" + posteriors[j][0] + "\t posSig: \t" + posteriors[j][1] );
	    		posBwdMatrix[i][j] = posteriors[j][0];
	    		posSigMatrix[i][j] = posteriors[j][1];
	    		
	    		if(posteriors[j][1]>0.99){
	    			binarySignalMatrix[i][j]=1; 
	    		} else {
	    			binarySignalMatrix[i][j]=0; 
	    		}
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
		int[][] matrix = binarySignalMatrix;
		ArrayList<Integer> rowBandWidths = new ArrayList<>();
		ArrayList<Integer> colBandHeights=new ArrayList<>();
		
		//Estimate column width
		for(int i=0; i<imageRows; i++){
			int rowCounter=1; //Count number of adjacent signal pixels in each row
			
			for(int j=0; j<imageColumns-1; j++){
				
					if(matrix[i][j]==1 && matrix[i][j] == matrix[i][j+1]){
						rowCounter++;
					}
					if(matrix[i][j]==1 && matrix[i][j] != matrix[i][j+1]){
						if(rowCounter > 1) {
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
				
					if(matrix[i][j]==1 && matrix[i][j] == matrix[i+1][j]){
						colCounter++;
					}
					if(matrix[i][j]==1 && matrix[i][j] != matrix[i+1][j]){
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
		/*
		Calculate average
		
		int sumH=0;
		for(int h:colBandHeights){
			sumH=sumH+h;
		}
		answer[1]=sumW/rowBandWidths.size();
		answer[0]=sumH/colBandHeights.size();
		*/
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
		if(imageColumns>500 && typicalBandWidth<20) { 
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
			if(binarySignalMatrix[i][j]==0) {
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
	 * @param typicalBand int array contain int [0]:windowHeight; [1]: windowWidth
	 * @param threshold double minimum fraction of signal in sliding window to create a band
	 */
	private void predictBandsSlidingWindow(double threshold) {
		int totPixels=typicalBandHeight*typicalBandWidth;
		double ones = 0;
		double twos = 0;
		int count = 0;
		
		for(int i=0; i<imageRows-typicalBandHeight; i++){ //start loop along all signal matrix
			for(int j=0; j<imageColumns-typicalBandWidth; j++){
				
				if(binarySignalMatrix[i][j] == 1){ // check if pixel is signal
					for(int y=0; y<typicalBandHeight; y++){ //opens window
						for(int x=0; x<typicalBandWidth;x++){
							if(binarySignalMatrix[i+y][j+x] == 1){
								ones++;
							}
							else if(binarySignalMatrix[i+y][j+x] == 2){
								twos++;
								break;
							}
						}
					}
				}
				
				//check if window has signal over threshold and no visited pixels
				if(twos == 0){
					if(ones/totPixels >= threshold){
						count++;
						Band band = new Band(i, i+typicalBandHeight-1, j, j+typicalBandWidth-1, count);
						bands.add(band);
						for(int y=0; y<typicalBandHeight; y++){ //update observation matrix
							for(int x=0; x<typicalBandWidth; x++){
								if(binarySignalMatrix[i+y][j+x] == 1){
									binarySignalMatrix[i+y][j+x] = 2;
								}
							}
						}
					}
				}
				ones=0;
				twos=0;
				
			}
		}
		
	}
	
	public void createWellsKmeans() {
		//int lowKvalue = (int)Math.round(imageColumns/(2*bWidth));
		int lowKvalue = 1;
		System.out.println("lowK: " + lowKvalue);
		int highKvalue = (int)Math.round(imageColumns/typicalBandWidth);
		System.out.println("highK: " + highKvalue);
		
		double[] var = new double[highKvalue-lowKvalue];

		for(int i=lowKvalue; i<highKvalue; i++){
			kMeansWellPrediction kmeansK = new kMeansWellPrediction(imageRows, imageColumns, bands, i);
			kmeansK.clusterWells();
			var[i-lowKvalue]=kmeansK.getVariance();

			//System.out.println("K: " + i + "\t logSE: " + Math.log10(var[i-lowKvalue]));
		}
		
		double[] deltaVar = new double[var.length];
		deltaVar[0]=0;
		for(int i=1; i<var.length; i++){
			deltaVar[i]=var[i-1]-var[i];
		}
		double[] logVar = new double[var.length];
		for(int i=0; i<var.length; i++){
			logVar[i]=Math.log10(var[i]);
		}
		double[] deltaLogVar = new double[var.length];
		deltaLogVar[0]=0;
		for(int i=1; i<logVar.length; i++){
			deltaLogVar[i]=Math.abs(logVar[i-1]-logVar[i]);
			System.out.println("dLogVar: " +deltaLogVar[i]);
		}
		deltaLogVar[1]=0;
		double maxdLogSE=deltaLogVar[0];
		int selectedK=0;
		for(int i=0; i<deltaLogVar.length; i++){
			double dse = deltaLogVar[i];
			if(dse>maxdLogSE){
				maxdLogSE = dse;
				selectedK=i+lowKvalue;
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
		

		kMeansWellPrediction km = new kMeansWellPrediction(imageRows, imageColumns, bands, selectedK);
		km.clusterWells();
		
		List<BandCluster> clusters = km.getClusters();
		int co=0;
		for(BandCluster cluster:clusters){
			co++;
			System.out.println("Cluster#: " + co);
			int colFirst=imageColumns;
			int colLast=0;
			for(Band b:cluster.getBands()){
				int firstC=b.getStartColumn();
				int lastC=b.getEndColumn();
				if(firstC<colFirst){
					colFirst=firstC;
				}
				if(lastC>colLast){
					colLast=lastC;
				}
			}
			Well well = new Well(0,colFirst,imageRows,colLast-colFirst+1,wells.size());
			int wellID = well.getWellID();
			for(Band b:cluster.getBands()){
				System.out.println("band#: " + b.getBandID());
				b.setWellID(wellID);
				well.addBand(b);
			}
	        wells.add(well);
		}
		
	}
	
	public void saveResults(String outputFilePrefix) throws IOException {
		String outputFile = outputFilePrefix+"_tree.nwk";
		try (PrintStream out = new PrintStream(outputFile)) {
			clusteringTree.printTree(out);
		}
		
		sampleClustering.saveDistanceMatrix(outputFilePrefix);
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
	public void createWellsBandClusters() {
		
		//STEP 1: Identify wells
		wells = new ArrayList<>();
    	createWellsKmeans();
  
 
        
	}
	private void discoverMissingBands(double threshold) {
		int count = 0;
		List<Band> newBands = new ArrayList<>();
		for(Band b:bands){
			int prevN = bands.size();
			int bandsWell = b.getWellID();
			int startR = b.getStartRow();
			int endR = b.getEndRow();
			
			for(Well w:wells){
				if(w.getWellID() != bandsWell){
					int startC = w.getStartCol();
					int endC = startC + w.getWellWidth();
					
					int totPixels=(endR-startR)*(endC-startC);
					double ones = 0;
					double twos = 0;
					
					//open window
					for(int y=startR; y<endR+1; y++){ //opens window
						for(int x=startC; x<endC+1;x++){
							if(binarySignalMatrix[y][x] == 1){
								ones++;
							}
							else if(binarySignalMatrix[y][x] == 2){
								twos++;
								break;
							}
						}
					}
					//check if window has signal over threshold and no visited pixels
					if(twos == 0){
						if(ones/totPixels >= threshold){
							count++;
							Band newBand = new Band(startR, endR, startC, endC, count+prevN);
							newBands.add(newBand);
							for(int y=startR; y<endR+1; y++){ //update observation matrix
								for(int x=startC; x<endC+1; x++){
									if(binarySignalMatrix[y][x] == 1){
										binarySignalMatrix[y][x] = 2;
									}
								}
							}
						}
					}
					ones=0;
					twos=0;
					
				}
			}
			
		}
		for(Band newBand:newBands) bands.add(newBand);
	}

	public void clusterAlleles(){
		//STEP : Cluster bands looking for variant alleles
    	for (Band b:bands) b.setAlleleClusterId(-1);
        BandsClusteringAlgorithm bandsClustering = new HeuristicCliqueBandClusteringAlgorithm();
        numClusters = bandsClustering.clusterBands(bands);
        System.out.println("Total cliques: "+numClusters);
        
        //STEP 6: Cluster samples (Wells)
        sampleClustering = new DiceSampleClustering();
        clusteringTree=sampleClustering.clusterSamples(wells);
	}
	
}
