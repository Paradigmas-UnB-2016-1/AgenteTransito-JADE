import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Via extends Agent{
	private static final long serialVersionUID = 1422761164770948535L;
	
	//Atributos de Via
	private Integer quantidadeDePistas;
	private Integer quantidadeDeCarros;
	private boolean statusAberto;
	
	String TRAVAR_VIA_ID = "travar-via";
	
	public void setup()
	{	
		// Registra a via no Directory Facilitator
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("transito");
		sd.setName("JADE-agente-transito");
		dfd.addServices(sd);	
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		System.out.println("nome agente " + this.getName());
		
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
			quantidadeDePistas = (int) args[0];
			System.out.println("Via possui " + quantidadeDePistas.toString());
			quantidadeDeCarros = 0;

			// Comportamento para criar carros na via a cada 5 segundos
			addBehaviour(new TickerBehaviour(this, 5000) {
				private static final long serialVersionUID = 4570363654540512633L;

				protected void onTick() 
				{
					quantidadeDeCarros++;
				}
			});

			// Comportamento para retornar resposta do CFP
			addBehaviour(new ResponderQuantidadeCarros());
			
			// Comportamento para responder TravarVia
			addBehaviour(new TravarVia());
			
			// Comportamento para responder LiberarVia
			addBehaviour(new LiberarVia());
			
			// Comportamento para liberar carros na via a cada 1 segundo
			addBehaviour(new TickerBehaviour(this, 1000) {
				private static final long serialVersionUID = -5349501205618955138L;

				protected void onTick() 
				{
					if(statusAberto)
					{
						quantidadeDeCarros -= quantidadeDePistas;
						if(quantidadeDeCarros < 0)
						{
							statusAberto = false;
						}
					}
				}
			});
		}
		else 
		{
			System.out.println("Não foi especificada a quantidade de pistas da via.");
			doDelete();
		}
	}
	
	private class ResponderQuantidadeCarros extends CyclicBehaviour {
		private static final long serialVersionUID = -6370113631324227434L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				ACLMessage resposta = msg.createReply();

				if (quantidadeDeCarros > 0)
				{
					resposta.setPerformative(ACLMessage.PROPOSE);
					resposta.setContent(quantidadeDeCarros.toString());
				}
				else
				{
					resposta.setPerformative(ACLMessage.REFUSE);
					resposta.setContent("not-available");
				}
				myAgent.send(resposta);
			}
			else {
				block();
			}
		}
	}
	
	private class TravarVia extends OneShotBehaviour {
		private static final long serialVersionUID = 977022657561165112L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) 
			{
				ACLMessage reply = msg.createReply();

				if (quantidadeDeCarros != null) {
					statusAberto = false;
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("done");
				}
				else {
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}
	
	private class LiberarVia extends OneShotBehaviour {
		private static final long serialVersionUID = -4127848735753594009L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) 
			{
				ACLMessage reply = msg.createReply();

				if (quantidadeDeCarros > 0) {
					statusAberto = true;
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("done");
				}
				else {
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("Sem carros na via.");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}
}
