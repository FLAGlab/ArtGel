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

import java.util.ArrayList;
import java.util.List;

import ngsep.variants.Sample;

public class NeighborJoiningBandsClusteringAlgorithm implements BandsClusteringAlgorithm {

	@Override
	public int clusterBands(List<Band> bands) {
		double [][] distances = Band.calculateEuclideanDistances(bands);
		List<Sample> bandSamples = new ArrayList<Sample>();
        for(int i=0; i<bands.size(); i++){
        	Sample s = new Sample(Integer.toString(i));
        	bandSamples.add(s);
        }
        /*DistanceMatrix matrix=new DistanceMatrix(bandSamples, distances);
		NeighborJoining njDendogram = new NeighborJoining();
		njDendogram.loadMatrix(matrix);
		Dendrogram njTree = njDendogram.constructNJTree();
		njTree.printTree(System.out);
		*/
        return -1;
	}

}
