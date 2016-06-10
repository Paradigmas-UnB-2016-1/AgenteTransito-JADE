import java.util.ArrayList;
import java.util.Hashtable;
import jade.core.AID;
//import Guarda.RequestPerformer;
//import Via.OfferRequestsServer;
//import Via.PurchaseOrdersServer;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Guarda extends Agent{
	
	private AID[] listaVias; 
	
	protected void setup() {
		
		addBehaviour(new OneShotBehaviour() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				// Retorna lista de vias cadastradas no serviço DF
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("transito");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template); 
					System.out.println("Retornou as vias:");
					listaVias = new AID[result.length];
					for (int i = 0; i < result.length; ++i) {
						listaVias[i] = result[i].getName();
						System.out.println(listaVias[i].getName());
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		});

	}
	
}
