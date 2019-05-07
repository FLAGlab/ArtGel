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
import java.util.Arrays;
import java.util.List;

import ngsep.graphs.CliquesFinder;

/**
 * Groups together band signals
 * @author Cindy Ulloa, Hector Ruiz, Jorge Duitama
 */
public class HeuristicCliqueBandClusteringAlgorithm implements BandsClusteringAlgorithm {
	private int maxDistanceConsistent = 10;
	@Override
	public List<List<Band>> clusterBands(List<Band> bands) {
		List<List<Band>> answer = new ArrayList<>();
		double [][] distances = Band.calculateEuclideanDistances(bands);
		boolean[][] consistencyMatrix=calculateConsistencyMatrix(distances);
        List<List<Integer>> cliques = CliquesFinder.findCliques(consistencyMatrix);
        boolean [] inCluster = new boolean[bands.size()];
        Arrays.fill(inCluster, false);
        for(List<Integer> group:cliques) {
        	List<Band> cluster = new ArrayList<>(group.size());
        	for(Integer i:group){
        		inCluster[i]= true;
        		cluster.add(bands.get(i));
        	}
        	answer.add(cluster);
        }
        
        //Look for unclustered bands
        for(int i=0;i<inCluster.length;i++){
        	if(!inCluster[i]){
        		List<Band> single = new ArrayList<>(1);
        		single.add(bands.get(i));
        		answer.add(single);
        	}
        }
        return answer;
	}
	
	private boolean[][] calculateConsistencyMatrix(double [][] distances){
		
		boolean [][] consistencyMatrix = new boolean [distances.length][distances[0].length];
		for(int i=0; i<consistencyMatrix.length;i++){
			for(int j=0;j<consistencyMatrix[0].length;j++) {
				consistencyMatrix[i][j] = (distances[i][j]<=maxDistanceConsistent);
			}
		}
		return consistencyMatrix;
	}

	/**
	 * @return the maxDistanceConsistent
	 */
	public int getMaxDistanceConsistent() {
		return maxDistanceConsistent;
	}

	/**
	 * @param maxDistanceConsistent the maxDistanceConsistent to set
	 */
	public void setMaxDistanceConsistent(int maxDistanceConsistent) {
		this.maxDistanceConsistent = maxDistanceConsistent;
	}
	
	

	

}
