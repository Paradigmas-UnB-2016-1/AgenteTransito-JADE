
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Via extends Agent{

	//Atributos de Via
	private Integer quantidadeDePistas;
	private Integer quantidadeDeCarros;
	private boolean statusAberto;
	
	//Número de série do agente
	private static final long serialVersionUID = 1L;
	
	public void setup()
	{
		
		// Update the list of seller agents
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("transito-carros");
		template.addServices(sd);
		
		System.out.println("nome agente " +this.getName());
		
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
			quantidadeDePistas = (int) args[0];
			System.out.println("Via possui " + quantidadeDePistas.toString());
			quantidadeDeCarros = 0;

			// Comportamento para criar carros na via a cada 5 segundos
			addBehaviour(new TickerBehaviour(this, 5000) {

				private static final long serialVersionUID = 1L;

				protected void onTick() 
				{
					quantidadeDeCarros++;
				}
			} );
		}
		else 
		{
			System.out.println("Não foi especificada a quantidade de pistas da via.");
			doDelete();
		}
		
	}
}
