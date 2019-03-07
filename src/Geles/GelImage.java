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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Stores image intensity and size information from a text file
 * @author Cindy Ulloa, H�ctor Ruiz, Jorge Duitama
 */
public class GelImage {
	private int totalRows;
	private int totalCols;
	private int totalWells;
	private String filename;
	
	/**
	 * Creates a new image with the given information
	 * @param filename Path to text file containing image information
	 * @throws IOException 
	 */
	public GelImage(String filename) throws IOException{
		this.filename =filename;
		processImageWell();
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void processImageWell() throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String linea1= in.readLine();
		String[] primera = linea1.split("\t");
		totalRows=Integer.parseInt(primera[0]);
		totalCols=Integer.parseInt(primera[1]);
		totalWells=Integer.parseInt(primera[2]);
		in.close();
	}
	
	/**
	 * @return int Number of rows of pixels in the image 
	 */
	public int getTotalRows(){
		return totalRows;
	}
	
	/**
	 * @return int Number of columns of pixels in the image
	 */
	public int getTotalCols(){
		return totalCols;
	}
	
	/**
	 * @return int Number of wells in the gel image
	 */
	public int getTotalWells(){
		return totalWells;
	}
	
}
