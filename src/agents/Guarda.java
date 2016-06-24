package agents;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

//Agente representa o guarda de trânsito que irá controlar as vias que devem ser fechadas e abertas
public class Guarda extends Agent{
	private static final long serialVersionUID = -3296457119186802280L;

	int QUANTIDADE_VIAS = 3;
	
	private AID[] listaVias = new AID[QUANTIDADE_VIAS]; 
	
	private AID viaAberta; // Representa a via com aberta
	private Integer quantidadeCarrosViaAberta = 0; // Quantidade de carros da via aberta
	
	private AID viaComMaisCarros; // Representa a via com mais carros
	private Integer maiorQuantidadeCarros; // Quantidade de carros da via mais populada
	
	private Date dataTroca = new Date();

	String CFP_CONVERSATION_ID = "carros-na-via";
	String TRAVAR_VIA_ID = "travar-via";
	String LIBERAR_VIA_ID = "liberar-via";
	MessageTemplate mt;

	final int QUANTIDADE_MINIMA_PARA_LIBERAR = 4;
	final int TEMPO_MINIMO_PARA_LIBERAR = 10; // Medida em segundos
	final int TEMPO_MAXIMO_PARA_LIBERAR = 15; // Medida em segundos
	
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
					// Lógica para inicializar via aberta
					/*int i;
					for(i=0;i<QUANTIDADE_VIAS;i++)
					{
						Via via = new Via();
						via = via.getClass().cast(listaVias[i]);
						if (via.statusAberto)
							viaAberta = listaVias[i];
					}*/
					int indiceViaAberta = 0;
					for(int i=0;i<listaVias.length;i++)
					{
						if(listaVias[0].getName().contains("Rio"))
						{
							indiceViaAberta = i;
							break;
						}
					}
					
					viaAberta = listaVias[indiceViaAberta];
				}
				else
				{
					System.out.println("As vias devem ser criadas antes do Guarda! Encerrando o programa...");
					try {
						System.exit(0);
					} catch (Throwable e) {
						
					}
				}
				
				SequentialBehaviour FuncaoGuarda = new SequentialBehaviour() {
					private static final long serialVersionUID = -4260990148601499366L;

					public int onEnd() {
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
				int quantidadePropostas = 0;
				viaComMaisCarros = null;
				maiorQuantidadeCarros = 0;
				
				while(quantidadePropostas < listaVias.length)
				{

					//System.out.println("recebe CFP.");
					
					// Recebe as respostas das vias
					ACLMessage resposta = myAgent.receive(mt);
					if (resposta != null) {
						if (resposta.getPerformative() == ACLMessage.PROPOSE) {
							
							//System.out.println("recebe propose.");
							
							int quantidadeCarrosAnalisada = Integer.parseInt(resposta.getContent());
							AID viaAnalisada = resposta.getSender();
							if (viaAnalisada.getName() == viaAberta.getName())
							{
								quantidadeCarrosViaAberta = quantidadeCarrosAnalisada;
								//System.out.println("Quantidade da Via Aberta: " + quantidadeCarrosViaAberta);
							}
							else if (viaComMaisCarros == null || quantidadeCarrosAnalisada > maiorQuantidadeCarros) {
								// Entra no if quando a via analisada possui maior quantidade de carros
								maiorQuantidadeCarros = quantidadeCarrosAnalisada;
								viaComMaisCarros = viaAnalisada;
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
			boolean temViaComCarros = viaComMaisCarros != null && maiorQuantidadeCarros > 0;
			boolean quantidadeMinima = maiorQuantidadeCarros >= QUANTIDADE_MINIMA_PARA_LIBERAR;
			boolean temViaAberta = viaAberta != null;
			Date dataAtual = new Date();
			long duracao = dataAtual.getTime() - dataTroca.getTime();
			long duracaoSegundos = TimeUnit.MILLISECONDS.toSeconds(duracao);
			boolean tempoMaiorQueTempoMaximo = duracaoSegundos > TEMPO_MAXIMO_PARA_LIBERAR;
			boolean tempoMaiorQueTempoMinimo = duracaoSegundos > TEMPO_MINIMO_PARA_LIBERAR;
			boolean temCarrosNaViaAberta = quantidadeCarrosViaAberta > 0;

			//System.out.println("Via Aberta: " + viaAberta);
			//System.out.println("Quantidade: " + quantidadeCarrosViaAberta); 
			
			if(!temCarrosNaViaAberta)
			{
				//System.out.println("NAO TEM CARRO NA VIA ABERTA");
			}
						
			if(temViaComCarros && (!temCarrosNaViaAberta || (tempoMaiorQueTempoMinimo && (!temViaAberta || quantidadeMinima || tempoMaiorQueTempoMaximo))))
			{
				String nomeVia = viaAberta.getName().substring(0, viaAberta.getName().indexOf("@"));
				System.out.println("Travando a " + nomeVia);
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
				String nomeVia = viaComMaisCarros.getName().substring(0, viaComMaisCarros.getName().indexOf("@"));
				System.out.println("Liberando a " + nomeVia);
				System.out.println("");
				System.out.println("");
				
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
				dataTroca = new Date();
			}
		}
	}
}
