import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class Via extends Agent{

	//definir
	private int quantidadeDePistas;
	private int quantidadeDeCarros;
	private boolean statusAberto;
	
	private static final long serialVersionUID = 1L;
	
	public void setup(){
		
		System.out.println("nome agente " +this.getName());
	
	
		addBehaviour(new TickerBehaviour(this,1000) {
			
			//@Override
			//public boolean done() {
			//	return false;
			//}
			
			//@Override
			//public void action() {	
			//}
			
			@Override
			protected void onTick() {
				
		
			}
		});
	}
}
