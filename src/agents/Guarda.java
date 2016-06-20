package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Guarda extends Agent{
	private static final long serialVersionUID = -3296457119186802280L;
	
	private AID viaAberta;
	private AID[] listaVias = new AID[2]; 
	
	private AID viaComMaisCarros; // Representa a via com mais carros
	private Integer maiorQuantidadeCarros; // Quantidade de carros da via mais populada

	String CFP_CONVERSATION_ID = "carros-na-via";
	String TRAVAR_VIA_ID = "travar-via";
	String LIBERAR_VIA_ID = "liberar-via";
	int QUANTIDADE_VIAS = 2;
	MessageTemplate mt;
	
	final int QUANTIDADE_MINIMA_PARA_LIBERAR = 10;
	
	protected void setup() {
		
		addBehaviour(new OneShotBehaviour() {
			private static final long serialVersionUID = 1155280362385738241L;

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
				
				if(listaVias.length == QUANTIDADE_VIAS)
				{
					viaAberta = listaVias[0];
				}
				else
				{
					System.out.println("Vias não foram totalmente criadas!");
					try {
						this.finalize();
						this.done();
					} catch (Throwable e) {
						
					}
				}
				
				SequentialBehaviour FuncaoGuarda = new SequentialBehaviour() {
					private static final long serialVersionUID = -4260990148601499366L;

					public int onEnd() {
						System.out.println("Acabou sequential");
					    reset();
					    myAgent.addBehaviour(this);
					    return super.onEnd();
					  }
				};

				FuncaoGuarda.addSubBehaviour(new RealizarCFP());
				FuncaoGuarda.addSubBehaviour(new ReceberCFP());
				FuncaoGuarda.addSubBehaviour(new TravarVia());
				FuncaoGuarda.addSubBehaviour(new LiberarVia());
				
				addBehaviour(FuncaoGuarda);
			}
		});
	}
		
	private class RealizarCFP extends OneShotBehaviour {

		private static final long serialVersionUID = -484638705659064794L;

		@Override
		public void action() {
			if (listaVias.length > 0)
			{
				System.out.println("Inicia CFP!");
				// Envia mensagem CallForProposal para todas as vias
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < listaVias.length; ++i) {
					cfp.addReceiver(listaVias[i]);
				}
				cfp.setContent("");
				cfp.setConversationId(CFP_CONVERSATION_ID);
				cfp.setReplyWith("cfp"+System.currentTimeMillis());
				
				myAgent.send(cfp);
				
				// Prepara o template para receber as propostas
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId(CFP_CONVERSATION_ID),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));

			}
			else
			{
				System.out.println("Não tem vias");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private class ReceberCFP extends OneShotBehaviour {

		private static final long serialVersionUID = -3669567902089076183L;

		@Override
		public void action() {
			if (listaVias.length > 0)
			{
				System.out.println("Inicia recebimento CFP!");
				
				int quantidadePropostas = 0;
				viaComMaisCarros = null;
				maiorQuantidadeCarros = 0;
				
				while(quantidadePropostas < listaVias.length)
				{
					System.out.println("Analisar uma via.");
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
					}
					quantidadePropostas++;
				}
			}
			else
			{
				// Não tem vias
			}
			
		}
	}

	
	private class TravarVia extends OneShotBehaviour
	{
		private static final long serialVersionUID = 5340802759041394271L;

		public void action() {
			if(viaComMaisCarros != null && maiorQuantidadeCarros >= QUANTIDADE_MINIMA_PARA_LIBERAR)
			{
				System.out.println("Travando a via " + viaAberta.getName());
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.addReceiver(viaAberta);
				request.setConversationId(TRAVAR_VIA_ID);
				request.setReplyWith("request"+System.currentTimeMillis());
				myAgent.send(request);
				// Prepara o template para receber a resposta
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TRAVAR_VIA_ID),
						MessageTemplate.MatchInReplyTo(request.getReplyWith()));
				
				viaAberta = null;
			}
		}
	}
	
	private class LiberarVia extends OneShotBehaviour
	{
		private static final long serialVersionUID = -8519518993368192920L;

		public void action() {
			if (viaAberta == null && viaComMaisCarros != null)
			{
				System.out.println("Liberando a via " + viaComMaisCarros.getName().toString());
				
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.addReceiver(viaComMaisCarros);
				request.setConversationId(LIBERAR_VIA_ID);
				request.setReplyWith("request"+System.currentTimeMillis());
				myAgent.send(request);
				// Prepara o template para receber a resposta
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TRAVAR_VIA_ID),
						MessageTemplate.MatchInReplyTo(request.getReplyWith()));

				viaAberta = viaComMaisCarros;
				viaComMaisCarros = null;
			}
		}
	}
}
