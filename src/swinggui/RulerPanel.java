package swinggui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Geles.Well;

public class RulerPanel extends JPanel {

	private List<JLabel> rulerValues = new ArrayList<>();
	public RulerPanel() {
		
	}
	
	public void repaintRuler (List<String> values, int imageHeight) {
		this.removeAll();
		rulerValues.clear();
		int n = values.size();
		int textHeight = imageHeight/n;
		System.out.println("Image height: "+imageHeight+" values: "+n+" textWidth: "+textHeight);
		this.setLayout(new GridLayout(n, 1));
		for(int i=0;i<n;i++) {
			JLabel label = new JLabel(values.get(i));
			label.setSize(50, textHeight);
			add(label);
			rulerValues.add(label);
		}
		updateUI();
	}

	public List<String> getValues() {
		List<String> answer = new ArrayList<>();
		for(JLabel label:rulerValues) {
			answer.add(label.getText());
		}
		return answer;
	}
}
