package ui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ViaUI extends JFrame{

	private static final long serialVersionUID = -4389284477318222160L;
	
	private JPanel panel;
	private JLabel statusAberto;
	private JLabel nrCarros;
	private JLabel descricaoVia;

	public ViaUI(){
		super();
		
		this.initialize();
		this.setVisible(true);
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setBounds(100, 100, 200, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		panel.setOpaque(true);
		
		nrCarros = new JLabel("QtdCarros");
		nrCarros.setBounds(70, 90, 50, 30);
		nrCarros.setHorizontalAlignment(SwingConstants.CENTER);
		Font myFont = new Font("Serif", Font.BOLD, 22);
		nrCarros.setFont(myFont);
		getContentPane().add(nrCarros);
		
		statusAberto = new JLabel("StatusAberto");
		statusAberto.setBounds(50, 30, 70, 15);
		statusAberto.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(statusAberto);
		
		descricaoVia = new JLabel("DescricaoVia");
		descricaoVia.setBounds(0, 0, 100, 15);
		descricaoVia.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(descricaoVia);
	}
	
	public void showGui(int nrVia, String nomeVia) {
		pack();
		super.setBounds(210 * nrVia, 200, 220, 180);
		super.setVisible(true);
		this.descricaoVia.setText(nomeVia);		
	}
	
	public void atualizar(int nrCarros, boolean aberto){
		this.nrCarros.setText(Integer.toString(nrCarros));
		if(aberto)
		{
			this.setTitle("ABERTO");
			panel.setOpaque(true);
			this.getContentPane().setBackground(Color.GREEN);
			this.statusAberto.setText("ABERTO");
		}
		else
		{
			this.setTitle("FECHADO");
			panel.setOpaque(true);
			this.getContentPane().setBackground(Color.RED);
			this.statusAberto.setText("FECHADO");
		}
		this.update(getGraphics());
	}
}