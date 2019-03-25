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

public class KMeansWellPrediction {

	private int imageRows;
	private int imageColumns;
	private List<Band> bands;
	private int k;
	private List<KMeansVerticalBandCluster> clusters= new ArrayList<KMeansVerticalBandCluster>();
	
	/**
	 * Constructor
	 * @param imageRows
	 * @param imageColumns
	 * @param bands
	 */
	public KMeansWellPrediction(int imageRows, int imageColumns, List<Band> bands, int k){
		this.imageRows=imageRows;
		this.imageColumns=imageColumns;
		this.bands=bands;
		this.k=k;
	}
	
	public void verticalBandClustering(){
		// Initial centroids evenly distributed
		for(int i=0; i<k; i++){
			int c = (int)Math.round( (i+0.5) * imageColumns/k);
			KMeansVerticalBandCluster bandCluster = new KMeansVerticalBandCluster(c);
			clusters.add(bandCluster);
		}

		//iterate clustering
		int counter=0;
		boolean stable = false;
		while(counter < 100 && !stable){
			clearClusters();
			assignBands();
			stable = calculateCentroids();
			counter++;
		}
	}
	private void clearClusters() {
		for(KMeansVerticalBandCluster cluster: clusters){
			cluster.clearCluster();
		}	
	}
	
	private void assignBands() {	
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

	private boolean calculateCentroids() {
		boolean stable = true;
		for(KMeansVerticalBandCluster cluster:clusters){
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

	public List<KMeansVerticalBandCluster> getClusters(){
		return clusters;
	}
	
	public double calculateVariance() {
		double sum=0;
		for(KMeansVerticalBandCluster cluster:clusters){
			sum += cluster.calculateVarianceFromBands();
		}
		return sum;
	}
	
	public List<Well> createWells() {
		List<Well> answer = new ArrayList<>();
		int co=0;
		for(KMeansVerticalBandCluster cluster:clusters) {
			if(cluster.getBands().size()==0) {
				System.err.println("Empty cluster");
				continue;
			}
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
			Well well = new Well(0,colFirst, imageRows, colLast-colFirst+1, answer.size());
			int wellID = well.getWellID();
			for(Band b:cluster.getBands()){
				System.out.println("band#: " + b.getBandID());
				b.setWellID(wellID);
				well.addBand(b);
			}
	        answer.add(well);
		}
		return answer;
	}	
}
class KMeansVerticalBandCluster {
	 private List<Band> bands = new ArrayList<Band>();
	 private int centroid;
	 
	 public KMeansVerticalBandCluster(int centroid){
		 this.centroid = centroid;
	 }

	 public int calculateCentroidFromBands(){
		 double sum=0;
		 for(Band b:bands){
			 sum += b.getMiddleColumn();
		 }
		 int newCentroid = (int)Math.round(sum/bands.size());
		 return newCentroid;
	 }
	 
	 public int getCentroid(){
		return centroid;
	 }

	 public void setCentroid(int centroid){
		this.centroid = centroid;
	 }

	    public List<Band> getBands(){
		return bands;
	 }

	 public void clearCluster() {
	  	bands.clear();
	 }
	 
	 public void assignBand (Band b) {
		 bands.add(b);
	 }
	 
	 public double calculateVarianceFromBands(){
		 if(bands.size()<=1) return 0;
		 double sum = 0;
		 double sum2 = 0;
		 for(Band b:bands){
			 double d = b.getMiddleColumn();
			 sum += d;
			 sum2 += d*d;
		 }
		 return (sum2-sum*sum/bands.size())/(bands.size()-1);
	 }
}
