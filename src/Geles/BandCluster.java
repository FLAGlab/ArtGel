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

import java.util.ArrayList;
import java.util.List;

// Band cluster used in k means clustering of wells

public class BandCluster{
	 private List<Band> bands = new ArrayList<Band>();
	 private int centroid;
	 
	 public BandCluster(int centroid){
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
