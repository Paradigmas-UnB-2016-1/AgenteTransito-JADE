package agents;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


public class InitAgent extends Agent
{

	private static final long serialVersionUID = 2963709068114684396L;

	protected void setup()
    {
		
        Runtime rt = Runtime.instance(); 
        Profile profile = new ProfileImpl();     
        
        ContainerController container = rt.createAgentContainer(profile); 
        
        Object argsVia1[] = new Object[6];
        argsVia1[0] = "4"; //Quantidade de pistas
        argsVia1[1] = "aberta"; //Status aberta/fechada
        argsVia1[2] = "2000"; //Tempo saída em ms
        argsVia1[3] = "2000"; //Tempo chegada em ms
        argsVia1[4] = "52"; //Quantidade inicial de carros
        argsVia1[5] = "1"; //Identificador Via
       
        Object argsVia2[] = new Object[6];
        argsVia2[0] = "3"; //Quantidade de pistas
        argsVia2[1] = "fechada"; //Status aberta/fechada
        argsVia2[2] = "2000"; //Tempo saída em ms
        argsVia2[3] = "3000"; //Tempo chegada em ms
        argsVia2[4] = "100"; //Quantidade inicial de carros
        argsVia2[5] = "2"; //Identificador Via
       
        Object argsVia3[] = new Object[6];
        argsVia3[0] = "3"; //Quantidade de pistas
        argsVia3[1] = "fechada"; //Status aberta/fechada
        argsVia3[2] = "2000"; //Tempo saída em ms
        argsVia3[3] = "3000"; //Tempo chegada em ms
        argsVia3[4] = "100"; //Quantidade inicial de carros
        argsVia3[5] = "3"; //Identificador Via
       
        Object argsVia4[] = new Object[6];
        argsVia4[0] = "3"; //Quantidade de pistas
        argsVia4[1] = "fechada"; //Status aberta/fechada
        argsVia4[2] = "2000"; //Tempo saída em ms
        argsVia4[3] = "3000"; //Tempo chegada em ms
        argsVia4[4] = "100"; //Quantidade inicial de carros
        argsVia4[5] = "4"; //Identificador Via
        
        Object argsGuarda[] = new Object[0];
        AgentController via1, via2, via3, via4, guarda;
        
        try {
            via1 = container.createNewAgent("Avenida Rio", "agents.Via", argsVia1);
            via1.start();
            
            Thread.sleep(100);
            
            via2 = container.createNewAgent("Avenida Minas", "agents.Via", argsVia2);
            via2.start();
            
            Thread.sleep(100);
            
            via3 = container.createNewAgent("Avenida Brasília", "agents.Via", argsVia3);
            via3.start();
            
            Thread.sleep(100);
            
            via4 = container.createNewAgent("Avenida São Paulo", "agents.Via", argsVia4);
            via4.start();
            
            Thread.sleep(500);
            
            guarda = container.createNewAgent("Guarda", "agents.Guarda", argsGuarda);
            guarda.start();
            
        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}