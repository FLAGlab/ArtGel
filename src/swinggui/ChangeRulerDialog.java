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

public class ChangeRulerDialog extends JDialog implements ActionListener {
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
	
	public ChangeRulerDialog (IntensityProcessorInterface parent, int numBands) {
		this.parent = parent;
		setLayout(new BorderLayout());
		//setDefaultCloseOperation();
		JPanel rulerPanel = new JPanel();
		rulerPanel.setLayout(new GridLayout(numBands, 1));
		for(int i=0;i<numBands;i++) {
			JTextField text = new JTextField();
			textFields.add(text);
			rulerPanel.add(text);
		}
		
		add(rulerPanel,BorderLayout.CENTER);
		
		JPanel calculatePanel = new JPanel();
		calculatePanel.setLayout(new GridLayout(1, 3));
		calculatePanel.add(textMinimum);
		calculatePanel.add(textMaximum);
		btnCalculate = new JButton(ACTION_CALCULATE);
		btnCalculate.setActionCommand(ACTION_CALCULATE);
		btnCalculate.addActionListener(this);
		calculatePanel.add(btnCalculate);
		
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
		
		setTitle( "Change ruler" );
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
			parent.setRulerValues(getRulerValues());
			setVisible(false);
			dispose();
		}
		if(ACTION_CANCEL.equals(command)) {
			setVisible(false);
			dispose();
		}
		
	}
	private void calculateRulerValues() {
		// TODO Auto-generated method stub
		
	}
	private List<String> getRulerValues() {
		List<String> values = new ArrayList<>();
		for(JTextField text:textFields ) {
			values.add(text.getText());
		}
		return values;
	}
}
