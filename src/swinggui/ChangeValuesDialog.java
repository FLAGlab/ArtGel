package swinggui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ChangeValuesDialog extends JDialog implements ActionListener {
	private static final String ACTION_CONFIRM = "Confirm";
	private static final String ACTION_CANCEL = "Cancel";
	private static final String ACTION_CALCULATE = "Calculate";
	
	private IntensityProcessorInterface parent;
	private List<JTextField> textFields = new ArrayList<>();
	private JTextField textMinimum = new JTextField();
	private JTextField textMaximum = new JTextField();
	private JButton btnCalculate;
	private JButton btnConfirm;
	private JButton btnCancel;
	private boolean confirmed = false;
	
	public ChangeValuesDialog (IntensityProcessorInterface parent, int numIds, List<String> oldIds, boolean calculate) {
		this.parent = parent;
		setLayout(new BorderLayout());
		//setDefaultCloseOperation();
		JPanel idsPanel = new JPanel();
		idsPanel.setLayout(new GridLayout(numIds, 1));
		for(int i=0;i<numIds;i++) {
			JTextField text = new JTextField();
			if(i<oldIds.size()) text.setText(oldIds.get(i));
			textFields.add(text);
			idsPanel.add(text);
		}
		
		add(idsPanel,BorderLayout.CENTER);
		
		JPanel calculatePanel = new JPanel();
		calculatePanel.setLayout(new GridLayout(1, 3));
		calculatePanel.add(textMinimum);
		calculatePanel.add(textMaximum);
		btnCalculate = new JButton(ACTION_CALCULATE);
		btnCalculate.setActionCommand(ACTION_CALCULATE);
		btnCalculate.addActionListener(this);
		calculatePanel.add(btnCalculate);
		calculatePanel.setVisible(calculate);
		
		JPanel proceedPanel = new JPanel();
		proceedPanel.setLayout(new GridLayout(1, 2));
		btnConfirm = new JButton(ACTION_CONFIRM);
		btnConfirm.setActionCommand(ACTION_CONFIRM);
		btnConfirm.addActionListener(this);
		proceedPanel.add(btnConfirm);
		
		btnCancel = new JButton(ACTION_CANCEL);
		btnCancel.setActionCommand(ACTION_CANCEL);
		btnCancel.addActionListener(this);
		proceedPanel.add(btnCancel);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(2, 1));
		southPanel.add(calculatePanel);
		southPanel.add(proceedPanel);
		
		add(southPanel,BorderLayout.SOUTH);
		
		setTitle( "Change ids" );
		setModal( true );
        pack( );
        //setResizable( false );
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(ACTION_CALCULATE.equals(command)) {
			calculateRulerValues();
		}
		if(ACTION_CONFIRM.equals(command)) {
			confirmed = true;
			System.out.println("Action to change values");
			setVisible(false);
			dispose();
		}
		if(ACTION_CANCEL.equals(command)) {
			confirmed = false;
			setVisible(false);
			dispose();
		}
		
	}
	private void calculateRulerValues() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @return the confirmed
	 */
	public boolean isConfirmed() {
		return confirmed;
	}
	public List<String> getValues() {
		List<String> values = new ArrayList<>();
		for(JTextField text:textFields ) {
			values.add(text.getText().trim());
		}
		return values;
	}
}
