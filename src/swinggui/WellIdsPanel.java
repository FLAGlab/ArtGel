package swinggui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import Geles.Well;

public class WellIdsPanel extends JPanel {

	private List<JTextField> wellIds = new ArrayList<>();
	public WellIdsPanel() {
		
	}
	
	public void repaintWellIds (List<Well> wells, int imageWidth) {
		List<String> oldIds = getWellIds();
		this.removeAll();
		int n = wells.size();
		int textWidth = imageWidth/n;
		System.out.println("Image width: "+imageWidth+" wells: "+n+" textWidth: "+textWidth);
		this.setLayout(new GridLayout(1, n));
		for(int i=0;i<n;i++) {
			JTextField text = new JTextField();
			text.setSize(textWidth, 30);
			if(i<oldIds.size()) text.setText(oldIds.get(i));
			add(text);
		}
		updateUI();
	}

	public List<String> getWellIds() {
		List<String> answer = new ArrayList<>();
		for(JTextField text:wellIds) {
			answer.add(text.getText());
		}
		return answer;
	}
}
