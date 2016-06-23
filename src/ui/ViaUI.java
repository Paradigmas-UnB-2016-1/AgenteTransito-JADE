package ui;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ViaUI extends JFrame{

	private static final long serialVersionUID = -4389284477318222160L;
	
	private JPanel panel;
	private JLabel statusAberto;
	private JLabel nrCarros;

	public ViaUI(){
		super();
		
		this.initialize();
		this.setVisible(true);
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setBounds(100, 100, 300, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		nrCarros = new JLabel("QtdCarros");
		nrCarros.setBounds(121, 50, 100, 15);
		getContentPane().add(nrCarros);
		
		statusAberto = new JLabel("StatusAberto");
		statusAberto.setBounds(12, 5, 510, 15);
		getContentPane().add(statusAberto);
	}
	
	public void showGui() {
		pack();
		super.setBounds(100, 100, 450, 300);
		super.setVisible(true);
	}
	
	public void atualizar(int nrCarros, boolean aberto){
		this.nrCarros.setText(Integer.toString(nrCarros));
		if(aberto)
		{
			this.panel.setBackground(Color.GREEN);
			this.statusAberto.setBackground(Color.green);
			this.statusAberto.setText("ABERTO");
		}
		else
		{
			this.panel.setBackground(Color.RED);
			this.statusAberto.setText("FECHADO");
		}
		this.update(getGraphics());
	}
}