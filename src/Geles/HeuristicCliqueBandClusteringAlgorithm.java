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

import java.util.List;

import ngsep.graphs.CliquesFinder;

/**
 * Groups together band signals
 * @author Cindy Ulloa, Hector Ruiz, Jorge Duitama
 */
public class HeuristicCliqueBandClusteringAlgorithm implements BandsClusteringAlgorithm {

	@Override
	public int clusterBands(List<Band> bands) {
		double [][] distances = Band.calculateEuclideanDistances(bands);
		boolean[][] consistencyMatrix=calculateConsistencyMatrix(distances);
        List<List<Integer>> cliques = CliquesFinder.findCliques(consistencyMatrix);
        int c=0;
        for(List<Integer> group:cliques) {
        	for(Integer band:group){
        		bands.get(band).setAlleleClusterId(c);
        	}
        	c++;
        }
        
        //Look for unclustered bands
        for(Band b:bands){
        	if(b.getAlleleClusterId()==-1){
        		b.setAlleleClusterId(c);
        		c++;
        	}
        }
        return c;
	}
	
	private boolean[][] calculateConsistencyMatrix(double [][] distances){
		int threshold = 10;
		boolean [][] consistencyMatrix = new boolean [distances.length][distances[0].length];
		
		for(int i=0; i<consistencyMatrix.length;i++){
			for(int j=0;j<consistencyMatrix[0].length;j++) {
				consistencyMatrix[i][j] = (distances[i][j]<threshold);
			}
		}
		return consistencyMatrix;
	}

	

}
