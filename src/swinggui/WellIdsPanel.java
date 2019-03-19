package swinggui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class WellIdsPanel extends JPanel {

	private List<JTextField> wellIds = new ArrayList<>();
	public WellIdsPanel() {
		
	}
	
	public void repaintWellIds (int number) {
		List<String> oldIds = getWellIds();
		this.removeAll();
		this.setLayout(new GridLayout(1, number));
		for(int i=0;i<number;i++) {
			JTextField text = new JTextField();
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
