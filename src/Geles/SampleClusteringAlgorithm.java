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

import java.io.IOException;
import java.util.List;

import ngsep.clustering.Dendrogram;
/**
 * Sample Clustering Interface
 * @author Cindy Ulloa, Hector Ruiz, Jorge Duitama
 *
 */
public interface SampleClusteringAlgorithm {
	
	/**
	 * Method for clustering the samples (cluster each well)
	 * @param wells The list of wells identified in the image
	 * @return Dendogram A dendogram of the clustering
	 */
	public Dendrogram clusterSamples(List<Well> wells);

	public void saveDistanceMatrix(String outputFilePrefix) throws IOException;
}
