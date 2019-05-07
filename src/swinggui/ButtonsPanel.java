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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ButtonsPanel extends JPanel implements ActionListener {
	private static final String ACTION_LOAD = "Load";
	private static final String ACTION_CALCULATE = "Calculate";
	private static final String ACTION_CLUSTER = "Cluster";
	private static final String ACTION_WELLIDS = "Change well ids";
	private static final String ACTION_SAVE = "Save";
	private static final String ACTION_DELETE_BAND = "Delete";
	private static final String ACTION_ADD_BAND = "Add";
	
	private IntensityProcessorInterface parent;
	private JButton butLoad = new JButton("Load");
	private JButton butCalculate = new JButton("Calculate");
	private JButton butCluster = new JButton("Cluster");
	private JButton butChangeWellIds = new JButton("Change well Ids");
	private JButton butSave = new JButton("Save");
	
	private JButton butAddBand = new JButton("Add");
	private JButton butDeleteBand = new JButton("Delete");
	
	
	
	public ButtonsPanel(IntensityProcessorInterface parent) {
		this.parent = parent;
		setLayout(new GridLayout(2, 5, 5, 5));
		butLoad.setActionCommand(ACTION_LOAD);
		butLoad.addActionListener(this);
		add(butLoad);
		
		butCalculate.setActionCommand(ACTION_CALCULATE);
		butCalculate.addActionListener(this);
		add(butCalculate);
		
		butCluster.setActionCommand(ACTION_CLUSTER);
		butCluster.addActionListener(this);
		add(butCluster);
		
		butChangeWellIds.setActionCommand(ACTION_WELLIDS);
		butChangeWellIds.addActionListener(this);
		add(butChangeWellIds);
		
		butSave.setActionCommand(ACTION_SAVE);
		butSave.addActionListener(this);
		add(butSave);
		
		add(new JLabel("Band actions"));
		
		butAddBand.setActionCommand(ACTION_ADD_BAND);
		butAddBand.addActionListener(this);
		add(butAddBand);
		
		butDeleteBand.setActionCommand(ACTION_DELETE_BAND);
		butDeleteBand.addActionListener(this);
		add(butDeleteBand);
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals(ACTION_LOAD)) {
			parent.load();
		}
		if(command.equals(ACTION_CALCULATE)) {
			parent.calculate();
		}
		if(command.equals(ACTION_CLUSTER)) {
			parent.clusterBands();
		}
		if(command.equals(ACTION_WELLIDS)) {
			parent.changeWellIds();
		}
		if(command.equals(ACTION_SAVE)) {
			parent.save();
		}
		if(command.equals(ACTION_ADD_BAND)) {
			parent.addBand();
		}
		if(command.equals(ACTION_DELETE_BAND)) {
			parent.deleteSelectedBand();
		}
		
	}

}
