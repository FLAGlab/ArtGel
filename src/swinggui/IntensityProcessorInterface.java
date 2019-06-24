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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.UIManager;

import Geles.IntensityProcessor;
import Geles.Well;
import Geles.AlleleBandCluster;
import Geles.Band;

public class IntensityProcessorInterface extends JFrame {

	private LabelIdsPanel wellIdsPanel;
	private LabelIdsPanel rulerPanel;
	private ImagePanel imagePanel;
	private ButtonsPanel buttonsPanel;
	private JScrollPane scrollPane;
	private IntensityProcessor processor;
	public IntensityProcessorInterface () {
		processor = new IntensityProcessor();
		setSize(new Dimension(800, 600));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		//setResizable(false);
		BorderLayout layout = new BorderLayout();
		layout.setHgap(10);
		layout.setVgap(10);
		setLayout(layout);
		wellIdsPanel = new LabelIdsPanel(this,"Wells");
		//wellIdsPanel.setPreferredSize(new Dimension(200, 300));
		add(wellIdsPanel, BorderLayout.EAST);
		wellIdsPanel.setVisible(false);
		rulerPanel = new LabelIdsPanel(this,"Ruler");
		add(rulerPanel, BorderLayout.WEST);
		rulerPanel.setVisible(false);
		imagePanel = new ImagePanel(this);
		scrollPane = new JScrollPane(imagePanel);
		scrollPane.setPreferredSize(new Dimension( 400,300));
		scrollPane.setLayout(new ScrollPaneLayout());
		scrollPane.setViewportView(imagePanel);
		add (scrollPane, BorderLayout.CENTER);
		buttonsPanel = new ButtonsPanel(this);
		add(buttonsPanel, BorderLayout.SOUTH);
	}
	
	public void load() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File("./"));
		int answer = jfc.showOpenDialog(this);
		if(answer != JFileChooser.APPROVE_OPTION) return;
		try {
			File f = jfc.getSelectedFile();
			processor.loadImage(f.getAbsolutePath());
			setTitle(f.getName());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		imagePanel.loadImage(processor.getImage());
		scrollPane.setViewportView(imagePanel);
		scrollPane.repaint();
		wellIdsPanel.setVisible(false);
		rulerPanel.setVisible(false);
	}

	public void calculate() {
		try {
			processor.processImage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
			return;
		}
		List<Band> bands = processor.getBands();
		imagePanel.updateImage(processor.getModifiedImage(), bands);
		repaintWellIds();
		//System.out.println("Final clusters: "+processor.getNumClusters());
		repaintMolecularWeights();
	}
	
	private void repaintWellIds() {
		List<Well> wells = processor.getWells();
		List<String> ids = new ArrayList<>();
		for(Well well:wells) {
			ids.add(well.getSampleId());
		}
		wellIdsPanel.repaintIds(ids);
		wellIdsPanel.setVisible(true);
	}
	
	private void repaintMolecularWeights() {
		List<AlleleBandCluster> clusters = processor.getAlleleClusters();
		List<String> values = new ArrayList<>();
		for(AlleleBandCluster cluster:clusters) {
			values.add(""+cluster.getMolecularWeight());
		}
		rulerPanel.repaintIds(values);
		rulerPanel.setVisible(true);
	}

	public void clusterBands() {
		processor.createWells();
		processor.clusterAlleles();
		List<Band> bands = processor.getBands();
		imagePanel.updateImage(processor.getModifiedImage(), bands);
		repaintWellIds();
		rulerPanel.repaintDefaultIds(processor.getNumClusters());
		rulerPanel.setVisible(true);
	}
	
	public void save() {
		JFileChooser jfc = new JFileChooser();
		int answer = jfc.showSaveDialog(this);
		if(answer != JFileChooser.APPROVE_OPTION) return;
		try {
			processor.setWellSampleIds (wellIdsPanel.getIds());
			processor.clusterSamples();
			processor.saveResults(jfc.getSelectedFile().getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName( ) );
		IntensityProcessorInterface frame = new IntensityProcessorInterface();
		frame.setVisible(true);

	}

	public void deleteSelectedBand() {
		Band selected = imagePanel.getSelectedBand();
		if(selected == null) return;
		processor.deleteBand(selected);
		List<Band> bands = processor.getBands();
		imagePanel.updateImage(processor.getModifiedImage(), bands);
		
	}

	public void addBand() {
		Band toCreate = imagePanel.getBandToCreate();
		if(toCreate == null) return;
		processor.addBand(toCreate);
		List<Band> bands = processor.getBands();
		imagePanel.updateImage(processor.getModifiedImage(), bands);
	}

	public void changeIds(String panelId) {
		if("Wells".equals(panelId)) changeWellIds();
		else changeRuler();
	}

	public void changeWellIds() {
		List<Well> wells = processor.getWells();
		List<String> oldIds = new ArrayList<>();
		for(Well well:wells) {
			oldIds.add(well.getSampleId());
		}
		ChangeValuesDialog dialog = new ChangeValuesDialog(this, wells.size(), oldIds, "Change sample ids");
		dialog.setVisible(true);
		if(dialog.isConfirmed()) {
			System.out.println("Changing sample ids: "+dialog.getValues());
			processor.setWellSampleIds(dialog.getValues());
		}
		repaintWellIds();
	}

	public void changeRuler() {
		List<AlleleBandCluster> clusters = processor.getAlleleClusters();
		List<String> oldValues = new ArrayList<>();
		for(AlleleBandCluster cluster:clusters) {
			oldValues.add(""+cluster.getMolecularWeight());
		}
		int n = processor.getNumClusters();
		ChangeValuesDialog dialog = new ChangeValuesDialog(this, n, oldValues, "Change molecular weights");
		dialog.setVisible(true);
		if(dialog.isConfirmed()) {
			List<Integer> molecularWeights = new ArrayList<>();
			for(String value:dialog.getValues()) {
				molecularWeights.add(Integer.parseInt(value));
			}
			processor.setMolecularWeights(molecularWeights);
		}
		repaintMolecularWeights();
	}
}
