package swinggui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Geles.Well;

public class LabelIdsPanel extends JPanel implements ActionListener {

	private static final String ACTION_CHANGE = "Change";
	
	private IntensityProcessorInterface parent;
	private String panelId;
	
	private List<String> ids;
	private JButton butChange = new JButton("Change");
	
	public LabelIdsPanel(IntensityProcessorInterface parent, String panelId) {
		this.parent = parent;
		this.panelId = panelId;
		butChange.setActionCommand(ACTION_CHANGE);
		butChange.addActionListener(this);
	}
	public void repaintDefaultIds (int numIds) {
		List<String> ids = new ArrayList<>(numIds);
		for(int i=0;i<ids.size();i++) ids.add(""+i);
		repaintIds(ids);
	}
	public void repaintIds (List<String> ids) {
		this.removeAll();
		this.ids = ids;
		int n = ids.size();
		this.setPreferredSize(new Dimension(100, 40*(n+2)));
		this.setLayout(new GridLayout(n+2,1,5,5));
		add(new JLabel(panelId));
		for(int i=0;i<n;i++) {
			JLabel labWellId = new JLabel(""+(i+1)+": "+ids.get(i));
			add(labWellId);
		}
		add(butChange);
		updateUI();
	}

	public List<String> getIds() {
		return ids;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals(ACTION_CHANGE)) {
			parent.changeIds(panelId);
		}
		
	}
}
