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

import ngsep.hmm.HMMState;
import ngsep.math.LogMath;
import JSci.maths.statistics.*;



public class DistributionState implements HMMState {
	private String id;
	private double startProb;
	private ProbabilityDistribution distribution;
	private Double[] log10Values;

	public DistributionState(String id, double startProb, ProbabilityDistribution distribution){
		this.distribution = distribution;
		this.startProb = startProb;
		this.id = id;
		double[] cumulativeValues = new double[256];
		for(int i = 0; i<256;i++) {
			cumulativeValues[i]= distribution.cumulative(i);
		}
		this.log10Values = new Double[256];
		for(int i = 0; i<256;i++) {
			double c1 =cumulativeValues[Math.max(0, i-2)];
			double c2 =cumulativeValues[Math.min(255, i+2)];
			log10Values[i] = LogMath.log10(c2-c1);
			//logP=distribution.probability(intensityValue);
		}
	}
	
	/**
	 * Returns the base 10 logarithm of the probability of emission of the given intensity value
	 * @param intensity value
	 * @param step At which the value is emitted
	 * @return Double log10 of the probability of observing the given value
	 * Null if the probability is zero
	 */
	public Double getEmission(Object value, int step){
		int intensityValue=(Integer)value;
		return log10Values[intensityValue];
	}
	
	
	/**
	 * @return startProb
	 */
	public double getStartProb(){
		return startProb;
	}
	
	/**
	 * @return id
	 */
	public String getId(){
		return id;
	}
	
	/**
	 * @return normal distribution
	 */
	public ProbabilityDistribution getDistribution(){
		return distribution;
	}


	@Override
	public Double getLogStart() {
		Double logStartP=LogMath.log10(startProb);
		return logStartP;
	}

	@Override
	public void setLogStart(Double arg0) {
		startProb=arg0;
		
	}
}
