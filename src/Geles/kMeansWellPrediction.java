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

import java.util.*;

public class kMeansWellPrediction {

	private int imageRows;
	private int imageColumns;
	private List<Band> bands;
	private int k;
	private double sumVariance;
	List<BandCluster> clusters= new ArrayList<BandCluster>();
	
	/**
	 * Constructor
	 * @param imageRows
	 * @param imageColumns
	 * @param bands
	 */
	public kMeansWellPrediction(int imageRows, int imageColumns, List<Band> bands, int k){
		this.imageRows=imageRows;
		this.imageColumns=imageColumns;
		this.bands=bands;
		this.k=k;
	}
	
	public void clusterWells(){
		// Initial centroids evenly distributed
		for(int i=0; i<k; i++){
			int c = (int)Math.round( (i+0.5) * imageColumns/k);
			BandCluster bandCluster = new BandCluster(c);
			clusters.add(bandCluster);
		}

		//iterate clustering
		int counter=0;
		boolean stable = false;
		while(counter < 100 && !stable){
			clearClusters(clusters);
			assignBands(clusters);
			stable = calculateCentroids(clusters);
			counter++;
		}
		//System.out.println("\t No. Iterations: " + counter);
		
		this.sumVariance=calculateVariance(clusters);
		 
	}

	private double calculateVariance(List<BandCluster> clusters) {
		double sum=0;
		for(BandCluster cluster:clusters){
			sum += cluster.calculateVarianceFromBands();
		}
		return sum;
	}

	private boolean calculateCentroids(List<BandCluster> clusters) {
		boolean stable = true;
		for(BandCluster cluster:clusters){
			if(cluster.getBands().size()==0) continue;
			int oldCentroid = cluster.getCentroid();
			int newCentroid = cluster.calculateCentroidFromBands();

			cluster.setCentroid(newCentroid);
			
			if(oldCentroid != newCentroid){
				stable = false;
			}
		}
		return stable;
	}

	private void assignBands(List<BandCluster> clusters) {
		
		//calculate distance to each center and assign to closest cluster

		for(Band band:bands){
			double distance;
			double dist = 100000;
			int closest=0;
			int column = band.getMiddleColumn();
			for(int i=0; i<k; i++){
				distance = Math.abs(column-clusters.get(i).getCentroid());
				if(distance<dist){
					closest=i;
					dist=distance;
				}		
			}
			clusters.get(closest).assignBand(band);
		}
	}

	private void clearClusters(List<BandCluster> clusters) {
		for(BandCluster cluster: clusters){
			cluster.clearCluster();
		}
		
	}
	
	public List<BandCluster> getClusters(){
		return clusters;
	}
	
	public double getVariance(){
		return sumVariance;
	}
	
}
