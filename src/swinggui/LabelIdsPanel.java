package swinggui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Geles.Well;

public class LabelIdsPanel extends JPanel {

	private List<String> ids;
	public LabelIdsPanel() {
		
	}
	
	public void repaintIds (List<String> ids) {
		this.removeAll();
		this.ids = ids;
		int n = ids.size();
		this.setPreferredSize(new Dimension(100, 40*(n+1)));
		this.setLayout(new GridLayout(n+1,1,5,5));
		add(new JLabel("Well sample ids"));
		for(int i=0;i<n;i++) {
			JLabel labWellId = new JLabel(""+(i+1)+": "+ids.get(i));
			add(labWellId);
		}
		updateUI();
	}

	public List<String> getIds() {
		return ids;
	}
}
