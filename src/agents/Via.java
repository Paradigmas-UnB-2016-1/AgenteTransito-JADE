package agents;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
	public boolean statusAberto;
	
	String TRAVAR_VIA_ID = "travar-via";
	String LIBERAR_VIA_ID = "liberar-via";

	int TEMPO_SAIDA_CARROS = 1000; //em milisegundos
	int TEMPO_CHEGADA_CARROS = 3000; //em milisegundos
	
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
		
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
			quantidadeDePistas = Integer.parseInt((String) args[0]);
			
			String abertoFechado = (String) args[1];
			if (abertoFechado == "aberta")
				statusAberto = true;
			else
				statusAberto = false;

			TEMPO_SAIDA_CARROS = Integer.parseInt((String) args[2]);
			TEMPO_CHEGADA_CARROS = Integer.parseInt((String) args[3]);
			
			quantidadeDeCarros = Integer.parseInt((String) args[4]);
			
			String nomeVia = this.getAID().getName().substring(0, this.getAID().getName().indexOf("@"));
			System.out.println("Criada " + nomeVia + " com " + quantidadeDePistas.toString() + " pistas e atualmente " + abertoFechado + ".");

			addBehaviour(new DiminuirQuantidadeCarrosViaAberta(this,TEMPO_SAIDA_CARROS));

			addBehaviour(new AumentarQuantidadeCarrosViaFechada(this,TEMPO_CHEGADA_CARROS));

			// Comportamento para retornar resposta do CFP
			addBehaviour(new ResponderQuantidadeCarros());
			
			// Comportamento para responder TravarVia
			addBehaviour(new TravarVia());
			
			// Comportamento para responder LiberarVia
			addBehaviour(new LiberarVia());
		}
		else 
		{
			System.out.println("Não foi especificada a quantidade de pistas da via.");
			doDelete();
		}
	}

	private class DiminuirQuantidadeCarrosViaAberta extends TickerBehaviour {

		public DiminuirQuantidadeCarrosViaAberta(Agent a, long period) {
			super(a, period);
		}

		private static final long serialVersionUID = 3017985306175066844L;

		@Override
		protected void onTick() {
			if(statusAberto)
			{
				quantidadeDeCarros -= quantidadeDePistas;
				//try {
					//Thread.sleep(1000);
				//} catch (InterruptedException e) {
				//}
				if(quantidadeDeCarros < 0)
				{
					quantidadeDeCarros = 0;
				}
				String nomeVia = this.getAgent().getName().substring(0, this.getAgent().getName().indexOf("@"));
				System.out.println("Quantidade de carros na " + nomeVia + ": " + quantidadeDeCarros.toString());
			}
		}		
	}

	private class AumentarQuantidadeCarrosViaFechada extends TickerBehaviour {

		public AumentarQuantidadeCarrosViaFechada(Agent a, long period) {
			super(a, period);
		}

		private static final long serialVersionUID = 3017985306175066844L;

		@Override
		protected void onTick() {
			if(!statusAberto)
			{
				quantidadeDeCarros++;
				String nomeVia = this.getAgent().getName().substring(0, this.getAgent().getName().indexOf("@"));
				System.out.println("Quantidade de carros na " + nomeVia + ": " + quantidadeDeCarros.toString());
				//try {
					//Thread.sleep(1000);
				//} catch (InterruptedException e) {
				//}
			}
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
	
	private class TravarVia extends CyclicBehaviour {
		private static final long serialVersionUID = 977022657561165112L;

		public void action() {

			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId(TRAVAR_VIA_ID),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
			
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
	
	private class LiberarVia extends CyclicBehaviour {
		private static final long serialVersionUID = -4127848735753594009L;

		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId(LIBERAR_VIA_ID),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
			
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
