package Geles;

import java.util.ArrayList;
import java.util.List;

public class AlleleBandCluster {

	private int position = 0;
	private List<Band> bands;
	
	private int molecularWeight = 0;
	
	public AlleleBandCluster (int position, List<Band> bands) {
		this.position = position;
		this.bands = new ArrayList<>(bands);
		molecularWeight = position+1;
	}
	
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the bands
	 */
	public List<Band> getBands() {
		return bands;
	}

	public void removeBand (Band b) {
		bands.remove(b);
	}

	/**
	 * @return the molecularWeight
	 */
	public int getMolecularWeight() {
		return molecularWeight;
	}

	/**
	 * @param molecularWeight the molecularWeight to set
	 */
	public void setMolecularWeight(int molecularWeight) {
		this.molecularWeight = molecularWeight;
	}
	
}
