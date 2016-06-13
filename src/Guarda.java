import java.util.ArrayList;
import java.util.Hashtable;
import jade.core.AID;
//import Via.OfferRequestsServer;
//import Via.PurchaseOrdersServer;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Guarda extends Agent{
	
	private AID viaAberta;
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
				
				if(listaVias.length > 0)
				{
					viaAberta = listaVias[0];
				}
				else
				{
					System.out.println("Nenhuma via retornada!");
				}
			}
		});
		
		addBehaviour(new CyclicBehaviour() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {

				// Perform the request
				myAgent.addBehaviour(new RequestPerformer());				
			}
		});
		
	}
		
	/**
	   Inner class RequestPerformer.
	   Comportamento para requisitar a quantidade de carros para as Vias
	 */
	private class RequestPerformer extends Behaviour {

		private static final long serialVersionUID = 1L;
		private AID viaComMaisCarros; // Representa a via com mais carros
		private Integer maiorQuantidadeCarros; // Quantidade de carros da via mais populada
		private int quantidadeRequisicoes = 0; // Contador das vias analisadas
		private MessageTemplate mt;
		private int step = 0;
		private String CONVERSATION_ID = "carros-na-via";

		public void action() {
			switch (step) {
			case 0:
				// Envia mensagem CallForProposal para todas as vias
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < listaVias.length; ++i) {
					cfp.addReceiver(listaVias[i]);
				}
				cfp.setConversationId(CONVERSATION_ID);
				cfp.setReplyWith("cfp"+System.currentTimeMillis());
				myAgent.send(cfp);
				// Prepara o template para receber as propostas
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Recebe as respostas das vias
				ACLMessage resposta = myAgent.receive(mt);
				if (resposta != null) {
					if (resposta.getPerformative() == ACLMessage.PROPOSE) {
						int quantidadeCarros = Integer.parseInt(resposta.getContent());
						if (viaComMaisCarros == null || quantidadeCarros > maiorQuantidadeCarros) {
							// Entra no if quando a via analisada possui maior quantidade de carros
							maiorQuantidadeCarros = quantidadeCarros;
							viaComMaisCarros = resposta.getSender();
						}
					}
					quantidadeRequisicoes++;
					if (quantidadeRequisicoes >= listaVias.length) {
						// Todas vias foram analisadas
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2:
				step = 3;
				break;
			case 3:
				step = 4;
				break;
			}        
		}
		
		public boolean done() 
		{				
			return ((step == 2 && viaComMaisCarros == null) || step == 4);
		}
	}
}
