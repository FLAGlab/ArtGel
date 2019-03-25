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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DiceSampleClustering implements SampleClusteringAlgorithm {


	@Override
	/**
	 * 
	 */
	public double [][] clusterSamples(IntensityProcessor processor) {
		List<Well> wells = processor.getWells();
		List<Band> bands= new ArrayList<>();
		for(Well w:wells){
			for(Band b:w.getBands()){
				bands.add(b);
			}
		}
        int numBandAlleleClusters = 0;
        for(Band band:bands){
        	if(numBandAlleleClusters<band.getAlleleClusterId()+1){
        		numBandAlleleClusters=band.getAlleleClusterId()+1;
        	}
        }
        int [][] binaryMatrix = new int [numBandAlleleClusters][wells.size()];
		for(int i=0;i<binaryMatrix.length;i++) Arrays.fill(binaryMatrix[i], 0);
		
		for(Band band:bands) {
			binaryMatrix[band.getAlleleClusterId()][band.getWellID()]=1;
		}
			
        //Print
        /*
		System.out.println("BinaryMatrix: ");
        for(int i=0;i<binaryMatrix.length;i++){
        	for(int j=0; j<binaryMatrix[0].length;j++){
        		System.out.print(binaryMatrix[i][j] +" ");
        	}
        	System.out.println();
        }*/
        
        return calculateDiceDistance(binaryMatrix);
	        
		}

	/**
	 * 
	 * @param binaryMatrix
	 * @return
	 */
	private double[][] calculateDiceDistance(int[][] binaryMatrix) {
		int wellNum = binaryMatrix[0].length;
		double [][] dice = new double[wellNum][wellNum];
		
		for(int i=0; i<wellNum;i++){
			for(int j=0; j<wellNum;j++){
				double numerator=0;
				double denominator=0;
				for(int row=0; row<binaryMatrix.length; row++){
					if(binaryMatrix[row][i] == binaryMatrix[row][j] && binaryMatrix[row][i]== 1){
						//System.out.println("a: " + binaryMatrix[row][i] + "\t b: " + binaryMatrix[row][j] );
						numerator++;
					}
					if(binaryMatrix[row][i]==1){
						denominator++;
					}
					if(binaryMatrix[row][j]==1){
						denominator++;
					}
				}
				//System.out.println("num: "+ numerator + "\t den: " + denominator);
				double value = (2*numerator);
				if(denominator>0) value/=denominator;
				dice[i][j]=dice[j][i]=value;
			}
		}
		/*System.out.println("Dice matrix: ");
		
		for(int i=0; i<dice.length; i++){
			for(int j=0; j<dice[i].length; j++){
				System.out.print(dice[i][j]+" ");
			}
			System.out.println();
		}*/
			
		return dice;
	}

	@Override
	public void saveClusteringData(String outputFilePrefix) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
