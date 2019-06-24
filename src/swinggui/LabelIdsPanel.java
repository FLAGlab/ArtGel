package swinggui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
		//System.out.println("Ids: "+numIds);
		List<String> ids = new ArrayList<>(numIds);
		for(int i=0;i<numIds;i++) ids.add(""+(i+1));
		repaintIds(ids);
	}
	public void repaintIds (List<String> ids) {
		System.out.println("Painting "+ids.size()+" ids in label: "+panelId);
		this.removeAll();
		this.ids = ids;
		int n = ids.size();
		this.setPreferredSize(new Dimension(100, 40*(n+2)));
		this.setLayout(new BorderLayout());
		add(new JLabel(panelId), BorderLayout.NORTH);
		JPanel labelsPanel = new JPanel();
		labelsPanel.setLayout(new GridLayout(n,1,5,5));
		for(int i=0;i<n;i++) {
			JLabel labWellId = new JLabel(""+(i+1)+": "+ids.get(i));
			labelsPanel.add(labWellId);
		}
		add(labelsPanel, BorderLayout.CENTER);
		add(butChange,BorderLayout.SOUTH);
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
