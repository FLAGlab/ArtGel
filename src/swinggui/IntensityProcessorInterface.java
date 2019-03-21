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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import Geles.IntensityProcessor;
import Geles.Well;
import Geles.Band;

public class IntensityProcessorInterface extends JFrame {

	private WellIdsPanel wellIdsPanel;
	private RulerPanel rulerPanel;
	private ImagePanel imagePanel;
	private ButtonsPanel buttonsPanel;
	private IntensityProcessor processor;
	public IntensityProcessorInterface () {
		processor = new IntensityProcessor();
		setSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		wellIdsPanel = new WellIdsPanel();
		add(wellIdsPanel, BorderLayout.NORTH);
		wellIdsPanel.setVisible(false);
		rulerPanel = new RulerPanel();
		add(rulerPanel, BorderLayout.EAST);
		rulerPanel.setVisible(false);
		imagePanel = new ImagePanel(this);
		add (imagePanel, BorderLayout.CENTER);
		buttonsPanel = new ButtonsPanel(this);
		add(buttonsPanel, BorderLayout.SOUTH);
	}
	
	public void load() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File("./"));
		int answer = jfc.showOpenDialog(this);
		if(answer != JFileChooser.APPROVE_OPTION) return;
		try {
			processor.loadImage(jfc.getSelectedFile().getAbsolutePath());
			imagePanel.loadImage(processor.getImage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void calculate() {
		processor.processImage();
		List<Band> bands = processor.getBands();
		imagePanel.paintBands(bands);
		List<Well> wells = processor.getWells();
		imagePanel.paintWells(wells);
		wellIdsPanel.repaintWellIds(wells,processor.getImage().getWidth());
		wellIdsPanel.setVisible(true);
		
		
		
	}
	public void save() {
		JFileChooser jfc = new JFileChooser();
		int answer = jfc.showSaveDialog(this);
		if(answer != JFileChooser.APPROVE_OPTION) return;
		try {
			processor.saveResults(jfc.getSelectedFile().getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		IntensityProcessorInterface frame = new IntensityProcessorInterface();
		frame.setVisible(true);

	}

	public void deleteSelectedBand() {
		Band selected = imagePanel.getSelectedBand();
		if(selected == null) return;
		processor.deleteBand(selected);
		List<Band> bands = processor.getBands();
		imagePanel.paintBands(bands);
		
	}

	public void addBand() {
		// TODO Auto-generated method stub
		Band toCreate = imagePanel.getBandToCreate();
		if(toCreate == null) return;
		processor.addBand(toCreate);
		List<Band> bands = processor.getBands();
		imagePanel.paintBands(bands);
	}

	public void clusterBands() {
		processor.clusterBands();
		processor.clusterAlleles();
		List<Band> bands = processor.getBands();
		imagePanel.paintBands(bands);
		List<Well> wells = processor.getWells();
		imagePanel.paintWells(wells);
	}

	public void setRulerValues(List<String> rulerValues) {
		// TODO Auto-generated method stub
		rulerPanel.repaintRuler(rulerValues, processor.getImage().getHeight());
		rulerPanel.setVisible(true);
	}

	

}
