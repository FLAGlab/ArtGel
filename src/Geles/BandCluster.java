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
 *     NGSEP is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with NGSEP.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package Geles;

import java.util.*;

// Band cluster used in k means clustering of wells

public class BandCluster{
	 private List<Band> bands = new ArrayList<Band>();
	 private int centroid;
	 private boolean done = false;
	 private double sumVariance;
	 
	 public BandCluster(int centroid){
		 this.centroid=centroid;
	 }

	 public int calculateCentroid(){
		 double sum=0;
		 for(Band b:bands){
			 int[] bandC = b.getCentroid();
			 sum += bandC[1];
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

	 public boolean isStable(){
		return done;
	 }

	 public void setEnded(boolean end) {
		this.done = end;
	 }

	 public void clearCluster() {
	  	bands.clear();
	 }
	 
	 public void calculateVariance(){
		 double sum =0;
		 for(Band b:bands){
			 double var = Math.pow((centroid-b.getCentroid()[1]),2);
			 sum += var;
		 }
		 
		 this.sumVariance=sum;
	 }
	 
	 public double getSumVariance(){
		 return sumVariance;
	 }
}
