package swinggui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Geles.Well;

public class WellIdsPanel extends JPanel {

	private List<JTextField> wellIds = new ArrayList<>();
	public WellIdsPanel() {
		
	}
	
	public void repaintWellIds (List<Well> wells) {
		List<String> oldIds = getWellIds();
		this.removeAll();
		int n = wells.size();
		this.setPreferredSize(new Dimension(200, 40*(n+1)));
		this.setLayout(new GridLayout(n+1,2,5,5));
		add(new JLabel("Well number"));
		add(new JLabel("Sample id"));
		for(int i=0;i<n;i++) {
			JLabel labWellId = new JLabel(""+(i+1));
			add(labWellId);
			JTextField text = new JTextField();
			text.setSize(200, 30);
			if(i<oldIds.size()) text.setText(oldIds.get(i));
			wellIds.add(text);
			add(text);
		}
		updateUI();
	}

	public List<String> getWellIds() {
		List<String> answer = new ArrayList<>();
		for(JTextField text:wellIds) {
			String nextId = text.getText().trim();
			if(nextId.length()==0) nextId = ""+(answer.size()+1);
			answer.add(nextId);
		}
		return answer;
	}
}
