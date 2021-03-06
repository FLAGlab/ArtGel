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
package swinggui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import Geles.Band;
import Geles.Well;

public class ImagePanel extends JPanel implements MouseListener {
	private IntensityProcessorInterface parent;
	private BufferedImage image;
	private List<Band> bands = new ArrayList<>();
	private Band selectedBand = null;
	
	private int rowNewBand = 0;
	private int columnNewBand = 0;
	private Band bandToCreate = null;
	
	
	public ImagePanel (IntensityProcessorInterface parent) {
		this.parent = parent;
		
		addMouseListener(this);
		//loadImage(new BufferedImage(1000,1000,BufferedImage.TYPE_BYTE_BINARY));
	}

	public void loadImage(BufferedImage image) {
		this.image = image;
		bands =new ArrayList<>();
		repaint();
	}

	public void updateImage(BufferedImage image, List<Band> bands){
		this.image = image;
		this.bands = bands;
		selectedBand = null;
		bandToCreate = null;
		repaint();
	}

	/**
	 * @return the selectedBand
	 */
	public Band getSelectedBand() {
		return selectedBand;
	}

	/**
	 * @return the bandToCreate
	 */
	public Band getBandToCreate() {
		return bandToCreate;
	}

	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image==null) return;
        Graphics2D g2DPanel = (Graphics2D) g;
		g2DPanel.drawImage(image, 0, 0, null);
        if(selectedBand!=null) g2DPanel.fillRect(selectedBand.getStartColumn(), selectedBand.getStartRow(), selectedBand.getEndColumn()-selectedBand.getStartColumn(), selectedBand.getEndRow()-selectedBand.getStartRow());
		if (bandToCreate!=null) {
			g2DPanel.setColor(Color.WHITE);
			Band band = bandToCreate;
			g2DPanel.drawRect(band.getStartColumn(), band.getStartRow(), band.getEndColumn()-band.getStartColumn(), band.getEndRow()-band.getStartRow());
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		bandToCreate = null;
		int column = e.getX();
		int row = e.getY();
		System.out.println("Selected row: "+row+" column: "+column);
		selectedBand = null;
		for(Band band:bands) {
			//System.out.println("Start row: "+band.getStartRow()+" column: "+band.getStartColumn());
			if(band.getStartRow()<=row && band.getEndRow()>=row && band.getStartColumn()<=column && band.getEndColumn()>=column) {
				selectedBand=band;
				break;
			}
		}
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		columnNewBand = e.getX();
		rowNewBand = e.getY();
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int column = e.getX();
		int row = e.getY();
		if(rowNewBand>=row || columnNewBand>=column) return;
		bandToCreate = new Band(rowNewBand, row, columnNewBand, column, 0);
		repaint();
	}
	
	@Override
    public Dimension getPreferredSize() {
		if(image==null) return new Dimension(300, 300);
		return new Dimension(image.getWidth(), image.getHeight());
    }
	
	
}
